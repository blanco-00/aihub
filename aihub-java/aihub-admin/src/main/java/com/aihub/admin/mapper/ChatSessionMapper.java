package com.aihub.admin.mapper;

import com.aihub.admin.dto.response.ChatSessionResponse;
import com.aihub.admin.entity.ChatSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 查询用户的会话列表
     */
    List<ChatSessionResponse> selectSessionList(@Param("userId") Long userId,
                                                 @Param("keyword") String keyword,
                                                 @Param("modelId") Long modelId,
                                                 @Param("offset") Long offset,
                                                 @Param("limit") Integer limit);

    /**
     * 统计用户的会话总数
     */
    Long countSessionList(@Param("userId") Long userId,
                          @Param("keyword") String keyword,
                          @Param("modelId") Long modelId);

    /**
     * 根据ID和用户ID查询会话（权限校验）
     */
    ChatSession selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 更新会话消息统计信息
     */
    void updateMessageInfo(@Param("sessionId") Long sessionId);
}
