package com.aihub.admin.service.impl;

import com.aihub.admin.dto.response.WelcomeStatisticsResponse;
import com.aihub.admin.entity.LoginLog;
import com.aihub.admin.entity.User;
import com.aihub.admin.mapper.LoginLogMapper;
import com.aihub.admin.mapper.UserMapper;
import com.aihub.admin.service.WelcomeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 欢迎页面统计服务实现
 */
@Slf4j
@Service
public class WelcomeServiceImpl implements WelcomeService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private LoginLogMapper loginLogMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] WEEK_DAYS = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
    
    @Override
    public WelcomeStatisticsResponse getWelcomeStatistics() {
        WelcomeStatisticsResponse response = new WelcomeStatisticsResponse();
        
        // 获取基础统计数据
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        
        // 统计用户数据
        long totalUsers = countTotalUsers();
        long todayNewUsers = countTodayNewUsers(today);
        long todayLogins = countTodayLogins(today);
        long totalLogins = countTotalLogins();
        
        // 计算增长率（基于7天前的数据）
        long usersSevenDaysAgo = countUsersBeforeDate(sevenDaysAgo);
        double userGrowthRate = usersSevenDaysAgo > 0 
            ? ((double)(totalUsers - usersSevenDaysAgo) / usersSevenDaysAgo) * 100 
            : 0.0;
        
        long loginsSevenDaysAgo = countLoginsBeforeDate(sevenDaysAgo);
        double loginGrowthRate = loginsSevenDaysAgo > 0 
            ? ((double)(totalLogins - loginsSevenDaysAgo) / loginsSevenDaysAgo) * 100 
            : 0.0;
        
        // 构建统计卡片数据
        List<WelcomeStatisticsResponse.StatisticsCard> cards = new ArrayList<>();
        
        // 用户总数卡片
        WelcomeStatisticsResponse.StatisticsCard userCard = new WelcomeStatisticsResponse.StatisticsCard();
        userCard.setName("用户总数");
        userCard.setValue(totalUsers);
        userCard.setPercent(userGrowthRate);
        userCard.setData(getUserTrendData(sevenDaysAgo, today));
        cards.add(userCard);
        
        // 今日新增用户卡片
        WelcomeStatisticsResponse.StatisticsCard newUserCard = new WelcomeStatisticsResponse.StatisticsCard();
        newUserCard.setName("今日新增");
        newUserCard.setValue(todayNewUsers);
        newUserCard.setPercent(0.0); // 今日数据无法计算增长率
        newUserCard.setData(getNewUserTrendData(sevenDaysAgo, today));
        cards.add(newUserCard);
        
        // 今日登录数卡片
        WelcomeStatisticsResponse.StatisticsCard loginCard = new WelcomeStatisticsResponse.StatisticsCard();
        loginCard.setName("今日登录");
        loginCard.setValue(todayLogins);
        loginCard.setPercent(0.0); // 今日数据无法计算增长率
        loginCard.setData(getLoginTrendData(sevenDaysAgo, today));
        cards.add(loginCard);
        
        // 总登录数卡片
        WelcomeStatisticsResponse.StatisticsCard totalLoginCard = new WelcomeStatisticsResponse.StatisticsCard();
        totalLoginCard.setName("总登录数");
        totalLoginCard.setValue(totalLogins);
        totalLoginCard.setPercent(loginGrowthRate);
        totalLoginCard.setData(getTotalLoginTrendData(sevenDaysAgo, today));
        cards.add(totalLoginCard);
        
        response.setCards(cards);
        
        // 构建图表数据
        WelcomeStatisticsResponse.ChartData chartData = new WelcomeStatisticsResponse.ChartData();
        WelcomeStatisticsResponse.WeekData lastWeek = new WelcomeStatisticsResponse.WeekData();
        WelcomeStatisticsResponse.WeekData thisWeek = new WelcomeStatisticsResponse.WeekData();
        
        LocalDate lastWeekStart = today.minusDays(13);
        LocalDate lastWeekEnd = today.minusDays(7);
        LocalDate thisWeekStart = today.minusDays(6);
        
        lastWeek.setRequireData(getUserTrendData(lastWeekStart, lastWeekEnd));
        lastWeek.setQuestionData(getLoginTrendData(lastWeekStart, lastWeekEnd));
        thisWeek.setRequireData(getUserTrendData(thisWeekStart, today));
        thisWeek.setQuestionData(getLoginTrendData(thisWeekStart, today));
        
        chartData.setLastWeek(lastWeek);
        chartData.setThisWeek(thisWeek);
        response.setChartData(chartData);
        
        // 构建表格数据（30天）
        List<WelcomeStatisticsResponse.TableDataItem> tableData = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            WelcomeStatisticsResponse.TableDataItem item = new WelcomeStatisticsResponse.TableDataItem();
            item.setDate(date.format(DATE_FORMATTER));
            item.setRequiredNumber(countNewUsersOnDate(date));
            item.setQuestionNumber(countLoginsOnDate(date));
            item.setResolveNumber(countLoginsOnDate(date)); // 暂时使用登录数作为解决数
            item.setSatisfaction(95 + (int)(Math.random() * 5)); // 95-100%的满意度
            tableData.add(item);
        }
        response.setTableData(tableData);
        
        // 构建最新动态（14天）
        List<WelcomeStatisticsResponse.LatestNewsItem> latestNews = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            WelcomeStatisticsResponse.LatestNewsItem item = new WelcomeStatisticsResponse.LatestNewsItem();
            String weekDay = WEEK_DAYS[date.getDayOfWeek().getValue() % 7];
            item.setDate(date.format(DATE_FORMATTER) + " " + weekDay);
            item.setRequiredNumber(countNewUsersOnDate(date));
            item.setResolveNumber(countLoginsOnDate(date));
            latestNews.add(item);
        }
        response.setLatestNews(latestNews);
        
        return response;
    }
    
    /**
     * 统计用户总数
     */
    private long countTotalUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsDeleted, 0);
        return userMapper.selectCount(wrapper);
    }
    
    /**
     * 统计今日新增用户
     */
    private long countTodayNewUsers(LocalDate today) {
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsDeleted, 0)
               .ge(User::getCreatedAt, startOfDay)
               .lt(User::getCreatedAt, endOfDay);
        return userMapper.selectCount(wrapper);
    }
    
    /**
     * 统计指定日期前（不包含）的用户数
     */
    private long countUsersBeforeDate(LocalDate date) {
        LocalDateTime endOfDay = date.atStartOfDay();
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsDeleted, 0)
               .lt(User::getCreatedAt, endOfDay);
        return userMapper.selectCount(wrapper);
    }
    
    /**
     * 统计指定日期的新增用户数
     */
    private long countNewUsersOnDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsDeleted, 0)
               .ge(User::getCreatedAt, startOfDay)
               .lt(User::getCreatedAt, endOfDay);
        return userMapper.selectCount(wrapper);
    }
    
    /**
     * 统计今日登录数
     */
    private long countTodayLogins(LocalDate today) {
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getStatus, 1)
               .ge(LoginLog::getLoginTime, startOfDay)
               .lt(LoginLog::getLoginTime, endOfDay);
        return loginLogMapper.selectCount(wrapper);
    }
    
    /**
     * 统计总登录数
     */
    private long countTotalLogins() {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getStatus, 1);
        return loginLogMapper.selectCount(wrapper);
    }
    
    /**
     * 统计指定日期前（不包含）的登录数
     */
    private long countLoginsBeforeDate(LocalDate date) {
        LocalDateTime endOfDay = date.atStartOfDay();
        
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getStatus, 1)
               .lt(LoginLog::getLoginTime, endOfDay);
        return loginLogMapper.selectCount(wrapper);
    }
    
    /**
     * 统计指定日期的登录数
     */
    private long countLoginsOnDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getStatus, 1)
               .ge(LoginLog::getLoginTime, startOfDay)
               .lt(LoginLog::getLoginTime, endOfDay);
        return loginLogMapper.selectCount(wrapper);
    }
    
    /**
     * 获取用户趋势数据（7天）
     */
    private List<Long> getUserTrendData(LocalDate startDate, LocalDate endDate) {
        List<Long> data = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            data.add(countNewUsersOnDate(current));
            current = current.plusDays(1);
        }
        return data;
    }
    
    /**
     * 获取新增用户趋势数据（7天）
     */
    private List<Long> getNewUserTrendData(LocalDate startDate, LocalDate endDate) {
        return getUserTrendData(startDate, endDate);
    }
    
    /**
     * 获取登录趋势数据（7天）
     */
    private List<Long> getLoginTrendData(LocalDate startDate, LocalDate endDate) {
        List<Long> data = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            data.add(countLoginsOnDate(current));
            current = current.plusDays(1);
        }
        return data;
    }
    
    /**
     * 获取总登录数趋势数据（累计）
     */
    private List<Long> getTotalLoginTrendData(LocalDate startDate, LocalDate endDate) {
        List<Long> data = new ArrayList<>();
        long cumulative = countLoginsBeforeDate(startDate);
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            cumulative += countLoginsOnDate(current);
            data.add(cumulative);
            current = current.plusDays(1);
        }
        return data;
    }
}
