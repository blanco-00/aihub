package com.aihub.admin.service;

import com.aihub.admin.dto.request.CreateSessionRequest;
import com.aihub.admin.dto.request.SaveMessageRequest;
import com.aihub.admin.dto.request.SendMessageRequest;
import com.aihub.admin.dto.request.SessionListRequest;
import com.aihub.admin.dto.response.ChatMessageResponse;
import com.aihub.admin.dto.response.ChatSessionResponse;
import com.aihub.common.web.dto.PageResult;

import java.util.List;

/**
 * 聊天会话服务接口
 */
public interface ChatSessionService {

    /**
     * 查询会话列表
     *
     * @param request 查询请求
     * @param userId  用户ID
     * @return 会话列表
     */
    PageResult<ChatSessionResponse> list(SessionListRequest request, Long userId);

    /**
     * 根据ID获取会话详情
     *
     * @param id     会话ID
     * @param userId 用户ID
     * @return 会话详情
     */
    ChatSessionResponse getById(Long id, Long userId);

    /**
     * 创建会话
     *
     * @param request 创建请求
     * @param userId  用户ID
     * @return 会话ID
     */
    Long createSession(CreateSessionRequest request, Long userId);

    /**
     * 删除会话（软删除）
     *
     * @param id     会话ID
     * @param userId 用户ID
     */
    void deleteSession(Long id, Long userId);

    /**
     * 获取会话的消息列表
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 消息列表
     */
    List<ChatMessageResponse> getMessages(Long sessionId, Long userId);

    /**
     * 发送消息
     *
     * @param request 发送消息请求
     * @param userId  用户ID
     * @return AI回复消息
     */
    ChatMessageResponse sendMessage(SendMessageRequest request, Long userId);

    /**
     * 更新会话标题
     *
     * @param id     会话ID
     * @param title  新标题
     * @param userId 用户ID
     */
    void updateSessionTitle(Long id, String title, Long userId);

    /**
     * 保存消息（供前端在SSE流完成后调用）
     *
     * @param request 保存消息请求
     * @param userId  用户ID
     */
    void saveMessage(SaveMessageRequest request, Long userId);
}
