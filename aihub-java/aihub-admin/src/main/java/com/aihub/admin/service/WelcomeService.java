package com.aihub.admin.service;

import com.aihub.admin.dto.response.WelcomeStatisticsResponse;

/**
 * 欢迎页面统计服务接口
 */
public interface WelcomeService {
    
    /**
     * 获取欢迎页面统计数据
     */
    WelcomeStatisticsResponse getWelcomeStatistics();
}
