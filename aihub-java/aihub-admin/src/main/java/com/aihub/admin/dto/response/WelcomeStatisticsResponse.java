package com.aihub.admin.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 欢迎页面统计数据响应DTO
 */
@Data
public class WelcomeStatisticsResponse {
    
    /**
     * 统计卡片数据
     */
    private List<StatisticsCard> cards;
    
    /**
     * 图表数据（7天趋势）
     */
    private ChartData chartData;
    
    /**
     * 表格数据（30天历史）
     */
    private List<TableDataItem> tableData;
    
    /**
     * 最新动态（14天）
     */
    private List<LatestNewsItem> latestNews;
    
    /**
     * 统计卡片
     */
    @Data
    public static class StatisticsCard {
        /**
         * 名称
         */
        private String name;
        
        /**
         * 当前值
         */
        private Long value;
        
        /**
         * 增长率（百分比，如：88表示+88%）
         */
        private Double percent;
        
        /**
         * 趋势数据（7天）
         */
        private List<Long> data;
    }
    
    /**
     * 图表数据
     */
    @Data
    public static class ChartData {
        /**
         * 上周数据
         */
        private WeekData lastWeek;
        
        /**
         * 本周数据
         */
        private WeekData thisWeek;
    }
    
    /**
     * 周数据
     */
    @Data
    public static class WeekData {
        /**
         * 需求数据（7天）
         */
        private List<Long> requireData;
        
        /**
         * 问题数据（7天）
         */
        private List<Long> questionData;
    }
    
    /**
     * 表格数据项
     */
    @Data
    public static class TableDataItem {
        /**
         * 日期（YYYY-MM-DD）
         */
        private String date;
        
        /**
         * 需求数量
         */
        private Long requiredNumber;
        
        /**
         * 问题数量
         */
        private Long questionNumber;
        
        /**
         * 解决数量
         */
        private Long resolveNumber;
        
        /**
         * 满意度（百分比，如：95表示95%）
         */
        private Integer satisfaction;
    }
    
    /**
     * 最新动态项
     */
    @Data
    public static class LatestNewsItem {
        /**
         * 日期（YYYY-MM-DD 周X）
         */
        private String date;
        
        /**
         * 需求数量
         */
        private Long requiredNumber;
        
        /**
         * 解决数量
         */
        private Long resolveNumber;
    }
}
