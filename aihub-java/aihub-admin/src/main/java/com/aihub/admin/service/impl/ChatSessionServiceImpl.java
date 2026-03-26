package com.aihub.admin.service.impl;

import com.aihub.admin.dto.request.CreateSessionRequest;
import com.aihub.admin.dto.request.SaveMessageRequest;
import com.aihub.admin.dto.request.SendMessageRequest;
import com.aihub.admin.dto.request.SessionListRequest;
import com.aihub.admin.dto.response.ChatMessageResponse;
import com.aihub.admin.dto.response.ChatSessionResponse;
import com.aihub.admin.entity.ChatMessage;
import com.aihub.admin.entity.ChatSession;
import com.aihub.admin.entity.ModelConfig;
import com.aihub.admin.mapper.ChatMessageMapper;
import com.aihub.admin.mapper.ChatSessionMapper;
import com.aihub.admin.mapper.ModelConfigMapper;
import com.aihub.admin.service.ChatSessionService;
import com.aihub.ai.infrastructure.ModelGateway;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天会话服务实现
 */
@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ModelConfigMapper modelConfigMapper;

    @Autowired
    private ModelGateway modelGateway;

    @Override
    public PageResult<ChatSessionResponse> list(SessionListRequest request, Long userId) {
        long startTime = System.currentTimeMillis();

        Integer current = request.getCurrent() != null ? request.getCurrent() : 1;
        Integer size = request.getSize() != null ? request.getSize() : 10;
        Long offset = (long) (current - 1) * size;

        List<ChatSessionResponse> sessions = chatSessionMapper.selectSessionList(
            userId, request.getKeyword(), request.getModelId(), offset, size);

        Long total = chatSessionMapper.countSessionList(
            userId, request.getKeyword(), request.getModelId());

        Long pages = (total + size - 1) / size;

        PageResult<ChatSessionResponse> result = new PageResult<>();
        result.setRecords(sessions);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages(pages);

        long totalTime = System.currentTimeMillis() - startTime;
        if (totalTime > 500) {
            log.warn("[性能警告] 会话列表查询耗时: {}ms, userId={}", totalTime, userId);
        }

        return result;
    }

    @Override
    public ChatSessionResponse getById(Long id, Long userId) {
        ChatSession session = chatSessionMapper.selectByIdAndUserId(id, userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        ChatSessionResponse response = new ChatSessionResponse();
        BeanUtils.copyProperties(session, response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSession(CreateSessionRequest request, Long userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(request.getTitle() != null ? request.getTitle() : "新会话");
        session.setModelId(request.getModelId());
        session.setPromptTemplateId(request.getPromptTemplateId());
        session.setMessageCount(0);
        session.setTotalTokens(0);
        session.setIsDeleted(0);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        // 获取模型名称
        if (request.getModelId() != null) {
            ModelConfig model = modelConfigMapper.selectById(request.getModelId());
            if (model != null) {
                session.setModelName(model.getName());
            }
        }

        chatSessionMapper.insert(session);

        log.info("创建会话成功: sessionId={}, userId={}", session.getId(), userId);
        return session.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long id, Long userId) {
        ChatSession session = chatSessionMapper.selectByIdAndUserId(id, userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        // 软删除
        chatSessionMapper.update(null,
                new LambdaUpdateWrapper<ChatSession>()
                        .eq(ChatSession::getId, id)
                        .set(ChatSession::getIsDeleted, 1)
                        .set(ChatSession::getUpdatedAt, LocalDateTime.now()));

        log.info("删除会话成功: sessionId={}, userId={}", id, userId);
    }

    @Override
    public List<ChatMessageResponse> getMessages(Long sessionId, Long userId) {
        // 权限校验
        ChatSession session = chatSessionMapper.selectByIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        return chatMessageMapper.selectMessagesBySessionId(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageResponse sendMessage(SendMessageRequest request, Long userId) {
        long startTime = System.currentTimeMillis();

        // 权限校验
        ChatSession session = chatSessionMapper.selectByIdAndUserId(request.getSessionId(), userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        // 确定使用的模型
        Long modelId = request.getModelId() != null ? request.getModelId() : session.getModelId();
        if (modelId == null) {
            throw new BusinessException("请选择模型");
        }

        ModelConfig model = modelConfigMapper.selectById(modelId);
        if (model == null || model.getStatus() != 1) {
            throw new BusinessException("模型不存在或已禁用");
        }

        // 保存用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(request.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(request.getContent());
        userMessage.setTokens(0);
        userMessage.setModelId(modelId);
        userMessage.setModelName(model.getName());
        userMessage.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(userMessage);

        // 调用AI获取回复
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(request.getSessionId());
        assistantMessage.setRole("assistant");
        assistantMessage.setModelId(modelId);
        assistantMessage.setModelName(model.getName());
        assistantMessage.setCreatedAt(LocalDateTime.now());

        try {
            String response = modelGateway.chat(
                    model.getVendor(),
                    model.getModelId(),
                    model.getApiKey(),
                    model.getBaseUrl(),
                    request.getContent()
            );

            assistantMessage.setContent(response);
            assistantMessage.setResponseTimeMs((int) (System.currentTimeMillis() - startTime));

            log.info("AI回复成功: sessionId={}, modelId={}, responseTime={}ms",
                    request.getSessionId(), modelId, assistantMessage.getResponseTimeMs());

        } catch (Exception e) {
            log.error("AI回复失败: sessionId={}, modelId={}, error={}",
                    request.getSessionId(), modelId, e.getMessage(), e);

            assistantMessage.setContent("抱歉，AI服务出现异常：" + e.getMessage());
            assistantMessage.setResponseTimeMs((int) (System.currentTimeMillis() - startTime));
            throw new BusinessException("AI服务异常: " + e.getMessage());
        }

        chatMessageMapper.insert(assistantMessage);

        // 更新会话统计
        int messageCount = session.getMessageCount() != null ? session.getMessageCount() : 0;
        chatSessionMapper.update(null,
                new LambdaUpdateWrapper<ChatSession>()
                        .eq(ChatSession::getId, request.getSessionId())
                        .set(ChatSession::getMessageCount, messageCount + 2)
                        .set(ChatSession::getLastMessageAt, LocalDateTime.now())
                        .set(ChatSession::getUpdatedAt, LocalDateTime.now()));

        // 转换响应
        ChatMessageResponse response = new ChatMessageResponse();
        BeanUtils.copyProperties(assistantMessage, response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSessionTitle(Long id, String title, Long userId) {
        ChatSession session = chatSessionMapper.selectByIdAndUserId(id, userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        chatSessionMapper.update(null,
                new LambdaUpdateWrapper<ChatSession>()
                        .eq(ChatSession::getId, id)
                        .set(ChatSession::getTitle, title)
                        .set(ChatSession::getUpdatedAt, LocalDateTime.now()));

        log.info("更新会话标题成功: sessionId={}, title={}, userId={}", id, title, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(SaveMessageRequest request, Long userId) {
        ChatSession session = chatSessionMapper.selectByIdAndUserId(request.getSessionId(), userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        ChatMessage message = new ChatMessage();
        message.setSessionId(request.getSessionId());
        message.setRole(request.getRole());
        message.setContent(request.getContent());
        message.setTokens(request.getTokens());
        message.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(message);

        chatSessionMapper.updateMessageInfo(request.getSessionId());

        log.info("保存消息成功: sessionId={}, role={}, userId={}", request.getSessionId(), request.getRole(), userId);
    }
}
