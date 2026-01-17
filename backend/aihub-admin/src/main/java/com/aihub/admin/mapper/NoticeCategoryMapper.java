package com.aihub.admin.mapper;

import com.aihub.admin.dto.request.NoticeCategoryListRequest;
import com.aihub.admin.dto.response.NoticeCategoryResponse;
import com.aihub.admin.entity.NoticeCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知分类Mapper接口
 */
@Mapper
public interface NoticeCategoryMapper extends BaseMapper<NoticeCategory> {
    
    /**
     * 查询通知分类列表（分页、搜索、筛选）
     */
    List<NoticeCategoryResponse> selectNoticeCategoryList(@Param("request") NoticeCategoryListRequest request,
                                                          @Param("offset") Long offset,
                                                          @Param("size") Integer size);
    
    /**
     * 统计通知分类总数
     */
    Long countNoticeCategoryList(@Param("request") NoticeCategoryListRequest request);
}
