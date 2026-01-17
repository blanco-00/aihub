package com.aihub.admin.service.impl;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.NoticeListRequest;
import com.aihub.admin.dto.request.CreateNoticeRequest;
import com.aihub.admin.dto.request.UpdateNoticeRequest;
import com.aihub.admin.dto.request.PublishNoticeRequest;
import com.aihub.admin.dto.response.NoticeListResponse;
import com.aihub.admin.dto.response.NoticeDetailResponse;
import com.aihub.admin.dto.response.MyNoticeResponse;
import com.aihub.admin.entity.Notice;
import com.aihub.admin.entity.NoticeScope;
import com.aihub.admin.entity.NoticeUser;
import com.aihub.common.web.exception.BusinessException;
import com.aihub.admin.mapper.NoticeMapper;
import com.aihub.admin.mapper.NoticeScopeMapper;
import com.aihub.admin.mapper.NoticeUserMapper;
import com.aihub.admin.mapper.NoticeCategoryMapper;
import com.aihub.admin.mapper.UserMapper;
import com.aihub.admin.mapper.UserRoleMapper;
import com.aihub.admin.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知公告服务实现
 */
@Slf4j
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements com.aihub.admin.service.NoticeService {
    
    @Autowired
    private NoticeMapper noticeMapper;
    
    @Autowired
    private NoticeScopeMapper noticeScopeMapper;
    
    @Autowired
    private NoticeUserMapper noticeUserMapper;
    
    @Autowired
    private NoticeCategoryMapper noticeCategoryMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Override
    public PageResult<NoticeListResponse> getNoticeList(NoticeListRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 计算偏移量
        Long offset = (long) (request.getCurrent() - 1) * request.getSize();
        
        // 查询通知列表
        List<NoticeListResponse> notices = noticeMapper.selectNoticeList(
            request, offset, request.getSize());
        
        // 统计总数
        Long total = noticeMapper.countNoticeList(request);
        
        // 计算总页数
        Long pages = (total + request.getSize() - 1) / request.getSize();
        
        // 构建分页结果
        PageResult<NoticeListResponse> result = new PageResult<>();
        result.setRecords(notices);
        result.setTotal(total);
        result.setCurrent(request.getCurrent());
        result.setSize(request.getSize());
        result.setPages(pages);
        
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 500) {
            log.warn("查询通知列表耗时较长: duration={}ms", duration);
        }
        
        return result;
    }
    
    @Override
    public NoticeDetailResponse getNoticeById(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException("通知不存在");
        }
        
        NoticeDetailResponse response = new NoticeDetailResponse();
        BeanUtils.copyProperties(notice, response);
        
        // 设置分类名称
        if (notice.getCategoryId() != null) {
            var category = noticeCategoryMapper.selectById(notice.getCategoryId());
            if (category != null && category.getIsDeleted() == 0) {
                response.setCategoryName(category.getName());
            }
        }
        
        // 加载发布范围信息（用于编辑时回显）
        if (notice.getPublishType() != null && notice.getPublishType() != 1) {
            List<NoticeScope> scopes = noticeScopeMapper.selectList(
                new LambdaQueryWrapper<NoticeScope>()
                    .eq(NoticeScope::getNoticeId, id)
            );
            
            if (notice.getPublishType() == 2) {
                // 指定部门
                response.setDepartmentIds(scopes.stream()
                    .filter(s -> s.getScopeType() == 1)
                    .map(NoticeScope::getScopeId)
                    .collect(Collectors.toList()));
            } else if (notice.getPublishType() == 3) {
                // 指定角色
                response.setRoleIds(scopes.stream()
                    .filter(s -> s.getScopeType() == 2)
                    .map(NoticeScope::getScopeId)
                    .collect(Collectors.toList()));
            } else if (notice.getPublishType() == 4) {
                // 指定用户
                response.setUserIds(scopes.stream()
                    .filter(s -> s.getScopeType() == 3)
                    .map(NoticeScope::getScopeId)
                    .collect(Collectors.toList()));
            }
        }
        
        return response;
    }
    
    @Override
    @Transactional
    public void createNotice(CreateNoticeRequest request, Long publisherId, String publisherName) {
        // 创建通知
        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setCategoryId(request.getCategoryId());
        notice.setType(request.getType() != null ? request.getType() : 1);
        notice.setPublishType(request.getPublishType() != null ? request.getPublishType() : 1);
        notice.setPublisherId(publisherId);
        notice.setPublisherName(publisherName);
        notice.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        notice.setExpireTime(request.getExpireTime());
        notice.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        notice.setViewCount(0);
        notice.setIsDeleted(0);
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        
        noticeMapper.insert(notice);
        
        // 保存发布范围（如果指定了范围）
        if (request.getPublishType() != null && request.getPublishType() != 1) {
            saveNoticeScope(notice.getId(), request.getPublishType(), 
                request.getDepartmentIds(), request.getRoleIds(), request.getUserIds());
        }
        
        log.info("创建通知成功: id={}, title={}, publisherId={}", 
            notice.getId(), notice.getTitle(), publisherId);
    }
    
    @Override
    @Transactional
    public void updateNotice(Long id, UpdateNoticeRequest request) {
        // 查询通知
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException("通知不存在");
        }
        
        // 只能修改草稿状态的通知
        if (notice.getStatus() != 0) {
            throw new BusinessException("只能修改草稿状态的通知");
        }
        
        // 更新通知
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setCategoryId(request.getCategoryId());
        if (request.getType() != null) {
            notice.setType(request.getType());
        }
        if (request.getPublishType() != null) {
            notice.setPublishType(request.getPublishType());
        }
        notice.setExpireTime(request.getExpireTime());
        if (request.getSortOrder() != null) {
            notice.setSortOrder(request.getSortOrder());
        }
        notice.setUpdatedAt(LocalDateTime.now());
        
        noticeMapper.updateById(notice);
        
        // 更新发布范围（删除旧的，创建新的）
        if (request.getPublishType() != null && request.getPublishType() != 1) {
            // 删除旧的发布范围
            noticeScopeMapper.delete(
                new LambdaQueryWrapper<NoticeScope>()
                    .eq(NoticeScope::getNoticeId, id)
            );
            // 保存新的发布范围
            saveNoticeScope(id, request.getPublishType(), 
                request.getDepartmentIds(), request.getRoleIds(), request.getUserIds());
        }
        
        log.info("更新通知成功: id={}, title={}", id, notice.getTitle());
    }
    
    @Override
    @Transactional
    public void publishNotice(Long id, PublishNoticeRequest request) {
        // 查询通知
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException("通知不存在");
        }
        
        // 只能发布草稿状态的通知
        if (notice.getStatus() != 0) {
            throw new BusinessException("只能发布草稿状态的通知");
        }
        
        // 更新发布范围（如果请求中提供了）
        if (request != null && notice.getPublishType() != 1) {
            // 删除旧的发布范围
            noticeScopeMapper.delete(
                new LambdaQueryWrapper<NoticeScope>()
                    .eq(NoticeScope::getNoticeId, id)
            );
            // 保存新的发布范围
            saveNoticeScope(id, notice.getPublishType(), 
                request.getDepartmentIds(), request.getRoleIds(), request.getUserIds());
        }
        
        // 计算接收用户并创建 notice_user 记录
        List<Long> targetUserIds = calculateTargetUsers(id, notice.getPublishType());
        
        // 批量创建 notice_user 记录
        if (!targetUserIds.isEmpty()) {
            List<NoticeUser> noticeUsers = targetUserIds.stream()
                .map(userId -> {
                    NoticeUser noticeUser = new NoticeUser();
                    noticeUser.setNoticeId(id);
                    noticeUser.setUserId(userId);
                    noticeUser.setIsRead(0);
                    noticeUser.setCreatedAt(LocalDateTime.now());
                    noticeUser.setUpdatedAt(LocalDateTime.now());
                    return noticeUser;
                })
                .collect(Collectors.toList());
            
            // 批量插入
            for (NoticeUser noticeUser : noticeUsers) {
                noticeUserMapper.insert(noticeUser);
            }
        }
        
        // 更新通知状态为已发布
        notice.setStatus(1);
        notice.setPublishTime(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
        
        log.info("发布通知成功: id={}, title={}, targetUserCount={}", 
            id, notice.getTitle(), targetUserIds.size());
    }
    
    @Override
    @Transactional
    public void withdrawNotice(Long id) {
        // 查询通知
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException("通知不存在");
        }
        
        // 只能撤回已发布状态的通知
        if (notice.getStatus() != 1) {
            throw new BusinessException("只能撤回已发布状态的通知");
        }
        
        // 更新通知状态为已撤回
        notice.setStatus(2);
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
        
        log.info("撤回通知成功: id={}, title={}", id, notice.getTitle());
    }
    
    @Override
    @Transactional
    public void deleteNotice(Long id) {
        // 查询通知
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException("通知不存在");
        }
        
        // 逻辑删除
        notice.setIsDeleted(1);
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
        
        log.info("删除通知成功: id={}, title={}", id, notice.getTitle());
    }
    
    @Override
    public PageResult<MyNoticeResponse> getMyNotices(Long userId, Integer current, Integer size, Integer isRead) {
        // 计算偏移量
        Long offset = (long) (current - 1) * size;
        
        // 查询通知ID列表
        List<Long> noticeIds = noticeUserMapper.selectNoticeIdsByUserId(userId, isRead, offset, size);
        
        if (noticeIds.isEmpty()) {
            PageResult<MyNoticeResponse> result = new PageResult<>();
            result.setRecords(new ArrayList<>());
            result.setTotal(0L);
            result.setCurrent(current);
            result.setSize(size);
            result.setPages(0L);
            return result;
        }
        
        // 查询通知详情
        List<Notice> notices = noticeMapper.selectBatchIds(noticeIds);
        
        // 查询已读状态
        List<NoticeUser> noticeUsers = noticeUserMapper.selectList(
            new LambdaQueryWrapper<NoticeUser>()
                .eq(NoticeUser::getUserId, userId)
                .in(NoticeUser::getNoticeId, noticeIds)
        );
        
        // 构建响应
        List<MyNoticeResponse> responses = new ArrayList<>();
        for (Notice notice : notices) {
            if (notice.getIsDeleted() == 1 || notice.getStatus() != 1) {
                continue;
            }
            
            MyNoticeResponse response = new MyNoticeResponse();
            BeanUtils.copyProperties(notice, response);
            
            // 设置已读状态
            NoticeUser noticeUser = noticeUsers.stream()
                .filter(nu -> nu.getNoticeId().equals(notice.getId()))
                .findFirst()
                .orElse(null);
            if (noticeUser != null) {
                response.setIsRead(noticeUser.getIsRead());
                response.setReadTime(noticeUser.getReadTime());
            }
            
            // 设置分类名称
            if (notice.getCategoryId() != null) {
                var category = noticeCategoryMapper.selectById(notice.getCategoryId());
                if (category != null && category.getIsDeleted() == 0) {
                    response.setCategoryName(category.getName());
                }
            }
            
            responses.add(response);
        }
        
        // 统计总数
        Long total = noticeUserMapper.countNoticesByUserId(userId, isRead);
        
        // 计算总页数
        Long pages = (total + size - 1) / size;
        
        // 构建分页结果
        PageResult<MyNoticeResponse> result = new PageResult<>();
        result.setRecords(responses);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages(pages);
        
        return result;
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        return noticeUserMapper.countUnreadByUserId(userId);
    }
    
    @Override
    @Transactional
    public NoticeDetailResponse getNoticeDetail(Long noticeId, Long userId) {
        // 查询通知
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException("通知不存在");
        }
        
        // 检查用户是否有权限查看（通过 notice_user 表）
        NoticeUser noticeUser = noticeUserMapper.selectOne(
            new LambdaQueryWrapper<NoticeUser>()
                .eq(NoticeUser::getNoticeId, noticeId)
                .eq(NoticeUser::getUserId, userId)
        );
        
        if (noticeUser == null) {
            throw new BusinessException("无权查看此通知");
        }
        
        // 自动标记为已读
        if (noticeUser.getIsRead() == 0) {
            noticeUser.setIsRead(1);
            noticeUser.setReadTime(LocalDateTime.now());
            noticeUser.setUpdatedAt(LocalDateTime.now());
            noticeUserMapper.updateById(noticeUser);
        }
        
        // 增加查看次数
        notice.setViewCount(notice.getViewCount() + 1);
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
        
        // 构建响应
        NoticeDetailResponse response = new NoticeDetailResponse();
        BeanUtils.copyProperties(notice, response);
        
        // 设置分类名称
        if (notice.getCategoryId() != null) {
            var category = noticeCategoryMapper.selectById(notice.getCategoryId());
            if (category != null && category.getIsDeleted() == 0) {
                response.setCategoryName(category.getName());
            }
        }
        
        return response;
    }
    
    @Override
    @Transactional
    public void markAsRead(Long noticeId, Long userId) {
        NoticeUser noticeUser = noticeUserMapper.selectOne(
            new LambdaQueryWrapper<NoticeUser>()
                .eq(NoticeUser::getNoticeId, noticeId)
                .eq(NoticeUser::getUserId, userId)
        );
        
        if (noticeUser == null) {
            throw new BusinessException("通知不存在或无权访问");
        }
        
        if (noticeUser.getIsRead() == 0) {
            noticeUser.setIsRead(1);
            noticeUser.setReadTime(LocalDateTime.now());
            noticeUser.setUpdatedAt(LocalDateTime.now());
            noticeUserMapper.updateById(noticeUser);
        }
    }
    
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<NoticeUser> noticeUsers = noticeUserMapper.selectList(
            new LambdaQueryWrapper<NoticeUser>()
                .eq(NoticeUser::getUserId, userId)
                .eq(NoticeUser::getIsRead, 0)
        );
        
        for (NoticeUser noticeUser : noticeUsers) {
            noticeUser.setIsRead(1);
            noticeUser.setReadTime(LocalDateTime.now());
            noticeUser.setUpdatedAt(LocalDateTime.now());
            noticeUserMapper.updateById(noticeUser);
        }
        
        log.info("全部标记为已读成功: userId={}, count={}", userId, noticeUsers.size());
    }
    
    /**
     * 保存通知发布范围
     */
    private void saveNoticeScope(Long noticeId, Integer publishType, 
                                 List<Long> departmentIds, List<Long> roleIds, List<Long> userIds) {
        List<NoticeScope> scopes = new ArrayList<>();
        
        if (publishType == 2 && departmentIds != null && !departmentIds.isEmpty()) {
            // 指定部门
            for (Long departmentId : departmentIds) {
                NoticeScope scope = new NoticeScope();
                scope.setNoticeId(noticeId);
                scope.setScopeType(1); // 1-部门
                scope.setScopeId(departmentId);
                scope.setCreatedAt(LocalDateTime.now());
                scopes.add(scope);
            }
        } else if (publishType == 3 && roleIds != null && !roleIds.isEmpty()) {
            // 指定角色
            for (Long roleId : roleIds) {
                NoticeScope scope = new NoticeScope();
                scope.setNoticeId(noticeId);
                scope.setScopeType(2); // 2-角色
                scope.setScopeId(roleId);
                scope.setCreatedAt(LocalDateTime.now());
                scopes.add(scope);
            }
        } else if (publishType == 4 && userIds != null && !userIds.isEmpty()) {
            // 指定用户
            for (Long userId : userIds) {
                NoticeScope scope = new NoticeScope();
                scope.setNoticeId(noticeId);
                scope.setScopeType(3); // 3-用户
                scope.setScopeId(userId);
                scope.setCreatedAt(LocalDateTime.now());
                scopes.add(scope);
            }
        }
        
        // 批量插入
        for (NoticeScope scope : scopes) {
            noticeScopeMapper.insert(scope);
        }
    }
    
    /**
     * 计算接收用户列表
     */
    private List<Long> calculateTargetUsers(Long noticeId, Integer publishType) {
        List<Long> targetUserIds = new ArrayList<>();
        
        if (publishType == 1) {
            // 全部用户：查询所有启用且未删除的用户
            List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .eq(User::getStatus, 1)
                    .eq(User::getIsDeleted, 0)
            );
            targetUserIds = users.stream().map(User::getId).collect(Collectors.toList());
        } else {
            // 从 notice_scope 表获取范围
            List<NoticeScope> scopes = noticeScopeMapper.selectList(
                new LambdaQueryWrapper<NoticeScope>()
                    .eq(NoticeScope::getNoticeId, noticeId)
            );
            
            if (publishType == 2) {
                // 指定部门
                List<Long> departmentIds = scopes.stream()
                    .filter(s -> s.getScopeType() == 1)
                    .map(NoticeScope::getScopeId)
                    .collect(Collectors.toList());
                
                if (!departmentIds.isEmpty()) {
                    List<User> users = userMapper.selectList(
                        new LambdaQueryWrapper<User>()
                            .in(User::getDepartmentId, departmentIds)
                            .eq(User::getStatus, 1)
                            .eq(User::getIsDeleted, 0)
                    );
                    targetUserIds = users.stream().map(User::getId).collect(Collectors.toList());
                }
            } else if (publishType == 3) {
                // 指定角色
                List<Long> roleIds = scopes.stream()
                    .filter(s -> s.getScopeType() == 2)
                    .map(NoticeScope::getScopeId)
                    .collect(Collectors.toList());
                
                if (!roleIds.isEmpty()) {
                    // 通过 user_role 表查询用户
                    List<com.aihub.admin.entity.UserRole> userRoles = userRoleMapper.selectList(
                        new LambdaQueryWrapper<com.aihub.admin.entity.UserRole>()
                            .in(com.aihub.admin.entity.UserRole::getRoleId, roleIds)
                    );
                    targetUserIds = userRoles.stream()
                        .map(com.aihub.admin.entity.UserRole::getUserId)
                        .distinct()
                        .collect(Collectors.toList());
                }
            } else if (publishType == 4) {
                // 指定用户
                targetUserIds = scopes.stream()
                    .filter(s -> s.getScopeType() == 3)
                    .map(NoticeScope::getScopeId)
                    .collect(Collectors.toList());
            }
        }
        
        return targetUserIds;
    }
}
