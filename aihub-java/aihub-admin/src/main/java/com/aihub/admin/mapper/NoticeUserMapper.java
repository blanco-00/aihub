package com.aihub.admin.mapper;

import com.aihub.admin.entity.NoticeUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户通知关联Mapper接口
 */
@Mapper
public interface NoticeUserMapper extends BaseMapper<NoticeUser> {
    
    /**
     * 根据用户ID和已读状态查询通知ID列表
     */
    List<Long> selectNoticeIdsByUserId(@Param("userId") Long userId,
                                       @Param("isRead") Integer isRead,
                                       @Param("offset") Long offset,
                                       @Param("size") Integer size);
    
    /**
     * 统计用户的通知数量
     */
    Long countNoticesByUserId(@Param("userId") Long userId,
                              @Param("isRead") Integer isRead);
    
    /**
     * 统计未读通知数量
     */
    Long countUnreadByUserId(@Param("userId") Long userId);
}
