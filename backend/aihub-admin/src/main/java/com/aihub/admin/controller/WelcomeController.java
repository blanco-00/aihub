package com.aihub.admin.controller;

import com.aihub.admin.dto.response.WelcomeStatisticsResponse;
import com.aihub.admin.service.WelcomeService;
import com.aihub.common.web.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 欢迎页面控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/welcome")
public class WelcomeController {
    
    @Autowired
    private WelcomeService welcomeService;
    
    /**
     * 获取欢迎页面统计数据
     */
    @GetMapping("/statistics")
    public Result<WelcomeStatisticsResponse> getWelcomeStatistics() {
        try {
            WelcomeStatisticsResponse statistics = welcomeService.getWelcomeStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取欢迎页面统计数据失败", e);
            throw e;
        }
    }
}
