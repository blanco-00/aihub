package com.aihub.admin.mapper;

import com.aihub.admin.dto.response.ChatMessageResponse;
import com.aihub.admin.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 查询会话的消息列表
     */
    List<ChatMessageResponse> selectMessagesBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 统计会话的消息数量
     */
    Long countBySessionId(@Param("sessionId") Long sessionId);
}
