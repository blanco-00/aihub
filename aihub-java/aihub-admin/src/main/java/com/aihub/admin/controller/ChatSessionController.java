package com.aihub.admin.controller;

import com.aihub.admin.dto.request.CreateSessionRequest;
import com.aihub.admin.dto.request.SaveMessageRequest;
import com.aihub.admin.dto.request.SendMessageRequest;
import com.aihub.admin.dto.request.SessionListRequest;
import com.aihub.admin.dto.response.ChatMessageResponse;
import com.aihub.admin.dto.response.ChatSessionResponse;
import com.aihub.admin.service.ChatSessionService;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.dto.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天会话控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat/session")
public class ChatSessionController {

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 查询会话列表
     */
    @GetMapping("/list")
    public Result<PageResult<ChatSessionResponse>> list(SessionListRequest request) {
        Long userId = getCurrentUserId();
        PageResult<ChatSessionResponse> result = chatSessionService.list(request, userId);
        return Result.success(result);
    }

    /**
     * 根据ID获取会话详情
     */
    @GetMapping("/{id}")
    public Result<ChatSessionResponse> getById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        ChatSessionResponse response = chatSessionService.getById(id, userId);
        return Result.success(response);
    }

    /**
     * 创建会话
     */
    @PostMapping("/create")
    public Result<Long> createSession(@RequestBody CreateSessionRequest request) {
        Long userId = getCurrentUserId();
        Long sessionId = chatSessionService.createSession(request, userId);
        return Result.success(sessionId);
    }

    /**
     * 删除会话（软删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSession(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        chatSessionService.deleteSession(id, userId);
        return Result.success();
    }

    /**
     * 获取会话的消息列表
     */
    @GetMapping("/{id}/messages")
    public Result<List<ChatMessageResponse>> getMessages(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        List<ChatMessageResponse> messages = chatSessionService.getMessages(id, userId);
        return Result.success(messages);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<ChatMessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        Long userId = getCurrentUserId();
        ChatMessageResponse response = chatSessionService.sendMessage(request, userId);
        return Result.success(response);
    }

    /**
     * 更新会话标题
     */
    @PutMapping("/{id}/title")
    public Result<Void> updateTitle(@PathVariable Long id, @RequestParam String title) {
        Long userId = getCurrentUserId();
        chatSessionService.updateSessionTitle(id, title, userId);
        return Result.success();
    }

    /**
     * 保存消息（供前端在SSE流完成后调用）
     */
    @PostMapping("/message/save")
    public Result<Void> saveMessage(@RequestBody @Valid SaveMessageRequest request) {
        Long userId = getCurrentUserId();
        chatSessionService.saveMessage(request, userId);
        return Result.success();
    }

    /**
     * 获取当前登录用户ID
     * TODO: 后续需要从安全上下文获取
     */
    private Long getCurrentUserId() {
        return 1L;
    }
}
