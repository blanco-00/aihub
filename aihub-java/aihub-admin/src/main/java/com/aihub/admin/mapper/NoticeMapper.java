package com.aihub.admin.mapper;

import com.aihub.admin.dto.request.NoticeListRequest;
import com.aihub.admin.dto.response.NoticeListResponse;
import com.aihub.admin.entity.Notice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知公告Mapper接口
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
    
    /**
     * 查询通知列表（分页、搜索、筛选）
     */
    List<NoticeListResponse> selectNoticeList(@Param("request") NoticeListRequest request,
                                               @Param("offset") Long offset,
                                               @Param("size") Integer size);
    
    /**
     * 统计通知总数
     */
    Long countNoticeList(@Param("request") NoticeListRequest request);
}
