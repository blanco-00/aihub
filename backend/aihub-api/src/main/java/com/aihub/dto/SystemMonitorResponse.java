package com.aihub.dto;

import lombok.Data;
import java.util.Map;

/**
 * 系统监控响应DTO
 */
@Data
public class SystemMonitorResponse {
    
    /**
     * 主机信息
     */
    private HostInfo host;
    
    /**
     * JVM信息
     */
    private JvmInfo jvm;
    
    /**
     * MySQL信息
     */
    private DatabaseInfo mysql;
    
    /**
     * Redis信息
     */
    private CacheInfo redis;
    
    /**
     * 主机信息
     */
    @Data
    public static class HostInfo {
        private String osName;
        private String osVersion;
        private String osArch;
        private String hostName;
        private String ip;
        private Integer cpuCores;
        private Double cpuUsage;
        private Long totalMemory;
        private Long usedMemory;
        private Long freeMemory;
        private Double memoryUsage;
        private Long totalDisk;
        private Long usedDisk;
        private Long freeDisk;
        private Double diskUsage;
        private Long uptime;
    }
    
    /**
     * JVM信息
     */
    @Data
    public static class JvmInfo {
        private String jvmName;
        private String jvmVersion;
        private String javaVersion;
        private Long heapTotal;
        private Long heapUsed;
        private Long heapFree;
        private Double heapUsage;
        private Long nonHeapTotal;
        private Long nonHeapUsed;
        private Long nonHeapFree;
        private Double nonHeapUsage;
        private Integer threadCount;
        private Long gcCount;
        private Long gcTime;
    }
    
    /**
     * 数据库信息
     */
    @Data
    public static class DatabaseInfo {
        private String status;
        private String version;
        private Integer activeConnections;
        private Integer maxConnections;
        private Integer idleConnections;
        private Double connectionUsage;
    }
    
    /**
     * 缓存信息
     */
    @Data
    public static class CacheInfo {
        private String status;
        private String version;
        private Long usedMemory;
        private Long maxMemory;
        private Double memoryUsage;
        private Integer connectedClients;
        private Long totalKeys;
        private Long hitCount;
        private Long missCount;
        private Double hitRate;
    }
}
