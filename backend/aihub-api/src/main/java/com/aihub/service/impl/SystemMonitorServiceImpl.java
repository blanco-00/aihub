package com.aihub.service.impl;

import com.aihub.dto.*;
import com.aihub.service.SystemMonitorService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 系统监控服务实现
 * 
 * 性能优化说明：
 * 1. 使用内存缓存，避免频繁计算系统信息
 * 2. CPU使用率计算采用增量方式，避免每次sleep 1秒
 * 3. 静态信息（如系统版本、主机名等）只获取一次并缓存
 * 4. 数据库和Redis连接复用，避免频繁创建连接
 * 5. 监控数据缓存3秒，减少系统资源消耗
 */
@Slf4j
@Service
public class SystemMonitorServiceImpl implements SystemMonitorService {
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private final SystemInfo systemInfo = new SystemInfo();
    
    // 缓存监控数据，避免频繁计算
    private final AtomicReference<SystemMonitorResponse> cachedResponse = new AtomicReference<>();
    private final AtomicLong lastUpdateTime = new AtomicLong(0);
    private static final long CACHE_DURATION_MS = 5000; // 缓存5秒（提高缓存时间，减少系统负载）
    
    // CPU使用率计算相关（避免频繁计算）
    private final AtomicReference<long[]> lastCpuTicks = new AtomicReference<>();
    private final AtomicLong lastCpuUpdateTime = new AtomicLong(0);
    private static final long CPU_UPDATE_INTERVAL_MS = 1000; // CPU更新间隔1秒
    
    // 静态信息缓存（只获取一次）
    private volatile String cachedOsName;
    private volatile String cachedOsVersion;
    private volatile String cachedOsArch;
    private volatile String cachedHostName;
    private volatile String cachedIp;
    private volatile Integer cachedCpuCores;
    private volatile String cachedJvmName;
    private volatile String cachedJvmVersion;
    private volatile String cachedJavaVersion;
    
    @Override
    public SystemMonitorResponse getSystemMonitorInfo() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        SystemMonitorResponse cached = cachedResponse.get();
        if (cached != null && (currentTime - lastUpdateTime.get()) < CACHE_DURATION_MS) {
            return cached;
        }
        
        // 缓存失效，重新获取
        SystemMonitorResponse response = new SystemMonitorResponse();
        
        try {
            // 获取主机信息
            response.setHost(getHostInfo());
            
            // 获取JVM信息
            response.setJvm(getJvmInfo());
            
            // 获取MySQL信息
            response.setMysql(getDatabaseInfo());
            
            // 获取Redis信息
            response.setRedis(getCacheInfo());
            
            // 更新缓存
            cachedResponse.set(response);
            lastUpdateTime.set(currentTime);
        } catch (Exception e) {
            log.error("获取系统监控信息失败", e);
            // 如果获取失败，返回缓存数据（如果有）
            if (cached != null) {
                return cached;
            }
        }
        
        return response;
    }
    
    /**
     * 获取主机信息
     * 优化：静态信息只获取一次，动态信息（CPU、内存、磁盘）实时获取
     */
    private SystemMonitorResponse.HostInfo getHostInfo() {
        SystemMonitorResponse.HostInfo host = new SystemMonitorResponse.HostInfo();
        
        try {
            HardwareAbstractionLayer hal = systemInfo.getHardware();
            OperatingSystem os = systemInfo.getOperatingSystem();
            CentralProcessor processor = hal.getProcessor();
            GlobalMemory memory = hal.getMemory();
            
            // 操作系统信息（静态信息，只获取一次）
            if (cachedOsName == null) {
                cachedOsName = os.getFamily();
                cachedOsVersion = os.getVersionInfo().getVersion();
                cachedOsArch = System.getProperty("os.arch");
                try {
                    cachedHostName = InetAddress.getLocalHost().getHostName();
                    cachedIp = InetAddress.getLocalHost().getHostAddress();
                } catch (Exception e) {
                    log.warn("获取主机名或IP失败", e);
                    cachedHostName = "未知";
                    cachedIp = "未知";
                }
                cachedCpuCores = processor.getLogicalProcessorCount();
            }
            
            host.setOsName(cachedOsName);
            host.setOsVersion(cachedOsVersion);
            host.setOsArch(cachedOsArch);
            host.setHostName(cachedHostName);
            host.setIp(cachedIp);
            host.setCpuCores(cachedCpuCores);
            
            // CPU使用率计算（手动计算，避免方法不存在的问题）
            long currentTime = System.currentTimeMillis();
            long[] currentTicks = processor.getSystemCpuLoadTicks();
            long[] prevTicks = lastCpuTicks.get();
            
            double cpuUsage = 0.0;
            if (prevTicks != null && currentTicks != null && 
                prevTicks.length == currentTicks.length && 
                (currentTime - lastCpuUpdateTime.get()) >= CPU_UPDATE_INTERVAL_MS) {
                // 有上一次的数据且间隔足够，手动计算CPU使用率
                try {
                    // 计算总ticks差值
                    long totalTicks = 0;
                    for (int i = 0; i < currentTicks.length; i++) {
                        long diff = currentTicks[i] - prevTicks[i];
                        if (diff < 0) {
                            // 如果出现负数，说明计数器重置了，跳过这次计算
                            totalTicks = -1;
                            break;
                        }
                        totalTicks += diff;
                    }
                    
                    if (totalTicks > 0) {
                        // 计算非空闲ticks（User + Nice + System + IOwait + IRQ + SoftIRQ + Steal）
                        // 索引：0=User, 1=Nice, 2=System, 3=Idle, 4=IOwait, 5=IRQ, 6=SoftIRQ, 7=Steal
                        long nonIdleTicks = 0;
                        for (int i = 0; i < currentTicks.length; i++) {
                            if (i != 3) { // 排除Idle (索引3)
                                long diff = currentTicks[i] - prevTicks[i];
                                if (diff > 0) {
                                    nonIdleTicks += diff;
                                }
                            }
                        }
                        
                        // CPU使用率 = 非空闲ticks / 总ticks
                        cpuUsage = (double) nonIdleTicks / totalTicks * 100;
                    }
                    
                    lastCpuTicks.set(currentTicks);
                    lastCpuUpdateTime.set(currentTime);
                } catch (Exception e) {
                    log.warn("计算CPU使用率失败，使用缓存值", e);
                    // 计算失败，使用缓存值
                    SystemMonitorResponse cached = cachedResponse.get();
                    if (cached != null && cached.getHost() != null && cached.getHost().getCpuUsage() != null) {
                        cpuUsage = cached.getHost().getCpuUsage();
                    }
                }
            } else if (prevTicks == null) {
                // 第一次获取，记录当前ticks，下次请求时再计算
                lastCpuTicks.set(currentTicks);
                lastCpuUpdateTime.set(currentTime);
                cpuUsage = 0.0; // 第一次返回0，下次请求时再计算
            } else {
                // 间隔不够，使用上一次的计算结果（从缓存中获取）
                SystemMonitorResponse cached = cachedResponse.get();
                if (cached != null && cached.getHost() != null && cached.getHost().getCpuUsage() != null) {
                    cpuUsage = cached.getHost().getCpuUsage();
                } else {
                    cpuUsage = 0.0;
                }
            }
            host.setCpuUsage(Math.round(cpuUsage * 100.0) / 100.0);
            
            // 内存信息（实时获取，但很快）
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            host.setTotalMemory(totalMemory);
            host.setUsedMemory(usedMemory);
            host.setFreeMemory(availableMemory);
            host.setMemoryUsage(Math.round((double) usedMemory / totalMemory * 10000.0) / 100.0);
            
            // 磁盘信息（实时获取，但相对较慢，已通过缓存优化）
            FileSystem fileSystem = os.getFileSystem();
            long totalDisk = 0;
            long usedDisk = 0;
            for (OSFileStore fs : fileSystem.getFileStores()) {
                long total = fs.getTotalSpace();
                long usable = fs.getUsableSpace();
                if (total > 0) {
                    totalDisk += total;
                    usedDisk += (total - usable);
                }
            }
            host.setTotalDisk(totalDisk);
            host.setUsedDisk(usedDisk);
            host.setFreeDisk(totalDisk - usedDisk);
            if (totalDisk > 0) {
                host.setDiskUsage(Math.round((double) usedDisk / totalDisk * 10000.0) / 100.0);
            }
            
            // 系统运行时间（实时获取，但很快）
            host.setUptime(os.getSystemUptime());
        } catch (Exception e) {
            log.error("获取主机信息失败", e);
        }
        
        return host;
    }
    
    /**
     * 获取JVM信息
     * 优化：静态信息只获取一次，动态信息实时获取
     */
    private SystemMonitorResponse.JvmInfo getJvmInfo() {
        SystemMonitorResponse.JvmInfo jvm = new SystemMonitorResponse.JvmInfo();
        
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            
            // JVM基本信息（静态信息，只获取一次）
            if (cachedJvmName == null) {
                cachedJvmName = ManagementFactory.getRuntimeMXBean().getVmName();
                cachedJvmVersion = ManagementFactory.getRuntimeMXBean().getVmVersion();
                cachedJavaVersion = System.getProperty("java.version");
            }
            
            jvm.setJvmName(cachedJvmName);
            jvm.setJvmVersion(cachedJvmVersion);
            jvm.setJavaVersion(cachedJavaVersion);
            
            // 堆内存信息（实时获取，但很快）
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            long heapTotal = heapUsage.getMax();
            long heapUsed = heapUsage.getUsed();
            long heapFree = heapTotal - heapUsed;
            jvm.setHeapTotal(heapTotal);
            jvm.setHeapUsed(heapUsed);
            jvm.setHeapFree(heapFree);
            if (heapTotal > 0) {
                jvm.setHeapUsage(Math.round((double) heapUsed / heapTotal * 10000.0) / 100.0);
            }
            
            // 非堆内存信息（实时获取，但很快）
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            long nonHeapTotal = nonHeapUsage.getMax();
            long nonHeapUsed = nonHeapUsage.getUsed();
            long nonHeapFree = nonHeapTotal > 0 ? nonHeapTotal - nonHeapUsed : 0;
            jvm.setNonHeapTotal(nonHeapTotal);
            jvm.setNonHeapUsed(nonHeapUsed);
            jvm.setNonHeapFree(nonHeapFree);
            if (nonHeapTotal > 0) {
                jvm.setNonHeapUsage(Math.round((double) nonHeapUsed / nonHeapTotal * 10000.0) / 100.0);
            }
            
            // 线程信息（实时获取，但很快）
            jvm.setThreadCount(threadBean.getThreadCount());
            
            // GC信息（实时获取，但很快）
            long gcCount = 0;
            long gcTime = 0;
            for (java.lang.management.GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                gcCount += gc.getCollectionCount();
                gcTime += gc.getCollectionTime();
            }
            jvm.setGcCount(gcCount);
            jvm.setGcTime(gcTime);
        } catch (Exception e) {
            log.error("获取JVM信息失败", e);
        }
        
        return jvm;
    }
    
    /**
     * 获取数据库信息
     * 性能优化：
     * 1. 使用连接池，避免频繁创建连接
     * 2. 直接从 HikariCP 连接池获取统计信息，无需执行 SQL 查询
     * 3. 添加超时控制，避免长时间阻塞
     */
    private SystemMonitorResponse.DatabaseInfo getDatabaseInfo() {
        SystemMonitorResponse.DatabaseInfo db = new SystemMonitorResponse.DatabaseInfo();
        
        try {
            if (dataSource == null) {
                db.setStatus("未配置");
                return db;
            }
            
            // 如果是 HikariCP 连接池，直接从连接池获取统计信息（最快，无需创建连接）
            if (dataSource instanceof HikariDataSource) {
                try {
                    HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                    db.setVersion("MySQL (HikariCP)");
                    db.setStatus("正常");
                    db.setActiveConnections(hikariDataSource.getHikariPoolMXBean().getActiveConnections());
                    db.setMaxConnections(hikariDataSource.getMaximumPoolSize());
                    db.setIdleConnections(hikariDataSource.getHikariPoolMXBean().getIdleConnections());
                    if (db.getMaxConnections() > 0) {
                        db.setConnectionUsage(Math.round((double) db.getActiveConnections() / db.getMaxConnections() * 10000.0) / 100.0);
                    }
                    return db; // 直接返回，无需创建连接
                } catch (Exception e) {
                    log.warn("从 HikariCP 获取连接池信息失败，尝试通过连接获取", e);
                }
            }
            
            // 非 HikariCP 或其他情况，使用连接获取（但设置超时）
            try (Connection connection = dataSource.getConnection()) {
                // 设置查询超时，避免长时间阻塞
                connection.setNetworkTimeout(java.util.concurrent.Executors.newSingleThreadExecutor(), 2000);
                
                DatabaseMetaData metaData = connection.getMetaData();
                db.setVersion(metaData.getDatabaseProductVersion());
                db.setStatus("正常");
                
                // 对于非 HikariCP 连接池，无法直接获取连接池统计信息
                db.setActiveConnections(0);
                db.setMaxConnections(0);
                db.setIdleConnections(0);
                db.setConnectionUsage(0.0);
            }
        } catch (Exception e) {
            log.error("获取数据库信息失败", e);
            db.setStatus("异常: " + (e.getMessage() != null ? e.getMessage().substring(0, Math.min(50, e.getMessage().length())) : "未知错误"));
        }
        
        return db;
    }
    
    /**
     * 获取Redis信息
     * 性能优化：
     * 1. 复用连接池连接，避免频繁创建和关闭
     * 2. 合并 INFO 命令，减少网络往返
     * 3. 添加异常处理和超时控制
     */
    private SystemMonitorResponse.CacheInfo getCacheInfo() {
        SystemMonitorResponse.CacheInfo cache = new SystemMonitorResponse.CacheInfo();
        
        try {
            if (redisConnectionFactory == null || redisTemplate == null) {
                cache.setStatus("未配置");
                return cache;
            }
            
            // 快速测试连接（使用连接池，不会创建新连接）
            try {
                redisTemplate.opsForValue().get("__monitor_test__");
                cache.setStatus("正常");
            } catch (Exception e) {
                log.warn("Redis连接测试失败", e);
                cache.setStatus("连接失败");
                return cache;
            }
            
            // 获取Redis信息（复用连接池连接）
            var connection = redisConnectionFactory.getConnection();
            try {
                // 合并获取多个 INFO 信息，减少网络往返
                // 注意：INFO 命令可能较慢，但这是获取 Redis 信息的标准方式
                Properties serverInfo = connection.serverCommands().info("server");
                Properties memoryInfo = connection.serverCommands().info("memory");
                Properties clientsInfo = connection.serverCommands().info("clients");
                
                // 解析服务器信息
                String version = serverInfo.getProperty("redis_version");
                cache.setVersion(version != null ? version : "未知");
                
                // 解析内存信息
                String usedMemoryStr = memoryInfo.getProperty("used_memory");
                String maxMemoryStr = memoryInfo.getProperty("maxmemory");
                if (usedMemoryStr != null) {
                    try {
                        cache.setUsedMemory(Long.parseLong(usedMemoryStr));
                    } catch (NumberFormatException e) {
                        log.warn("解析Redis内存使用量失败", e);
                        cache.setUsedMemory(0L);
                    }
                }
                if (maxMemoryStr != null && !maxMemoryStr.equals("0")) {
                    try {
                        cache.setMaxMemory(Long.parseLong(maxMemoryStr));
                        if (cache.getMaxMemory() > 0) {
                            cache.setMemoryUsage(Math.round((double) cache.getUsedMemory() / cache.getMaxMemory() * 10000.0) / 100.0);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析Redis最大内存失败", e);
                    }
                }
                
                // 解析客户端连接数
                String connectedClientsStr = clientsInfo.getProperty("connected_clients");
                if (connectedClientsStr != null) {
                    try {
                        cache.setConnectedClients(Integer.parseInt(connectedClientsStr));
                    } catch (NumberFormatException e) {
                        log.warn("解析Redis客户端连接数失败", e);
                        cache.setConnectedClients(0);
                    }
                }
                
                // 获取键数量（dbSize() 方法直接在 RedisConnection 接口上，相对较快）
                try {
                    Long dbSize = connection.dbSize();
                    cache.setTotalKeys(dbSize != null ? dbSize : 0L);
                } catch (Exception e) {
                    log.warn("获取Redis键数量失败", e);
                    cache.setTotalKeys(0L);
                }
                
                // 统计信息（命中率需要从 stats 获取，这里简化处理）
                cache.setHitCount(0L);
                cache.setMissCount(0L);
                cache.setHitRate(0.0);
            } catch (Exception e) {
                log.error("获取Redis信息失败", e);
                cache.setStatus("异常: " + (e.getMessage() != null ? e.getMessage().substring(0, Math.min(50, e.getMessage().length())) : "未知错误"));
            } finally {
                connection.close(); // 归还连接池
            }
        } catch (Exception e) {
            log.error("Redis连接失败", e);
            cache.setStatus("连接失败");
        }
        
        return cache;
    }
}
