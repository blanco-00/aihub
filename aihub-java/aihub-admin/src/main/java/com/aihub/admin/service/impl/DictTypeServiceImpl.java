package com.aihub.admin.service.impl;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.DictTypeListRequest;
import com.aihub.admin.dto.request.CreateDictTypeRequest;
import com.aihub.admin.dto.request.UpdateDictTypeRequest;
import com.aihub.admin.dto.response.DictTypeResponse;
import com.aihub.admin.entity.DictType;
import com.aihub.common.web.exception.BusinessException;
import com.aihub.admin.mapper.DictTypeMapper;
import com.aihub.admin.mapper.DictDataMapper;
import com.aihub.admin.service.DictTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典类型服务实现
 */
@Slf4j
@Service
public class DictTypeServiceImpl implements DictTypeService {
    
    @Autowired
    private DictTypeMapper dictTypeMapper;
    
    @Autowired
    private DictDataMapper dictDataMapper;
    
    @Override
    public PageResult<DictTypeResponse> getDictTypeList(DictTypeListRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 计算偏移量
        Long offset = (long) (request.getCurrent() - 1) * request.getSize();
        
        // 查询字典类型列表
        List<DictTypeResponse> dictTypes = dictTypeMapper.selectDictTypeList(
            request.getDictName(),
            request.getDictType(),
            request.getStatus(),
            request.getStartTime(),
            request.getEndTime(),
            offset,
            request.getSize()
        );
        
        // 统计总数
        Long total = dictTypeMapper.countDictTypeList(
            request.getDictName(),
            request.getDictType(),
            request.getStatus(),
            request.getStartTime(),
            request.getEndTime()
        );
        
        // 计算总页数
        Long pages = (total + request.getSize() - 1) / request.getSize();
        
        // 构建分页结果
        PageResult<DictTypeResponse> result = new PageResult<>();
        result.setRecords(dictTypes);
        result.setTotal(total);
        result.setCurrent(request.getCurrent());
        result.setSize(request.getSize());
        result.setPages(pages);
        
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 500) {
            log.warn("查询字典类型列表耗时较长: duration={}ms, dictName={}, dictType={}", 
                duration, request.getDictName(), request.getDictType());
        }
        
        return result;
    }
    
    @Override
    public DictTypeResponse getDictTypeById(Long id) {
        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null || dictType.getIsDeleted() == 1) {
            throw new BusinessException("字典类型不存在");
        }
        
        DictTypeResponse response = new DictTypeResponse();
        response.setId(dictType.getId());
        response.setDictName(dictType.getDictName());
        response.setDictType(dictType.getDictType());
        response.setStatus(dictType.getStatus());
        response.setRemark(dictType.getRemark());
        response.setCreatedAt(dictType.getCreatedAt());
        response.setUpdatedAt(dictType.getUpdatedAt());
        
        return response;
    }
    
    @Override
    @Transactional
    public void createDictType(CreateDictTypeRequest request) {
        // 检查字典类型是否已存在
        DictType existing = dictTypeMapper.selectOne(
            new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictType, request.getDictType())
                .eq(DictType::getIsDeleted, 0)
        );
        
        if (existing != null) {
            log.warn("创建字典类型失败：字典类型已存在: dictType={}", request.getDictType());
            throw new BusinessException("字典类型已存在");
        }
        
        // 创建字典类型
        DictType dictType = new DictType();
        dictType.setDictName(request.getDictName());
        dictType.setDictType(request.getDictType());
        dictType.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        dictType.setRemark(request.getRemark());
        dictType.setIsDeleted(0);
        dictType.setCreatedAt(LocalDateTime.now());
        dictType.setUpdatedAt(LocalDateTime.now());
        
        dictTypeMapper.insert(dictType);
        log.info("创建字典类型成功: id={}, dictName={}, dictType={}", 
            dictType.getId(), dictType.getDictName(), dictType.getDictType());
    }
    
    @Override
    @Transactional
    public void updateDictType(Long id, UpdateDictTypeRequest request) {
        // 查询字典类型
        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null || dictType.getIsDeleted() == 1) {
            throw new BusinessException("字典类型不存在");
        }
        
        // 如果字典类型有变化，检查新类型是否已被使用
        if (!dictType.getDictType().equals(request.getDictType())) {
            DictType existing = dictTypeMapper.selectOne(
                new LambdaQueryWrapper<DictType>()
                    .eq(DictType::getDictType, request.getDictType())
                    .eq(DictType::getIsDeleted, 0)
                    .ne(DictType::getId, id)
            );
            
            if (existing != null) {
                log.warn("更新字典类型失败：字典类型已存在: dictType={}, id={}", request.getDictType(), id);
                throw new BusinessException("字典类型已存在");
            }
        }
        
        // 更新字典类型
        dictType.setDictName(request.getDictName());
        dictType.setDictType(request.getDictType());
        if (request.getStatus() != null) {
            dictType.setStatus(request.getStatus());
        }
        dictType.setRemark(request.getRemark());
        dictType.setUpdatedAt(LocalDateTime.now());
        
        dictTypeMapper.updateById(dictType);
        log.info("更新字典类型成功: id={}, dictName={}, dictType={}", 
            id, dictType.getDictName(), dictType.getDictType());
    }
    
    @Override
    @Transactional
    public void deleteDictType(Long id) {
        // 查询字典类型
        DictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null || dictType.getIsDeleted() == 1) {
            throw new BusinessException("字典类型不存在");
        }
        
        // 检查是否有字典数据关联
        Long count = dictDataMapper.countDictDataListByType(dictType.getDictType());
        if (count > 0) {
            log.warn("删除字典类型失败：存在关联的字典数据: id={}, dictType={}, count={}", 
                id, dictType.getDictType(), count);
            throw new BusinessException("该字典类型下存在字典数据，无法删除");
        }
        
        // 逻辑删除
        dictType.setIsDeleted(1);
        dictType.setUpdatedAt(LocalDateTime.now());
        dictTypeMapper.updateById(dictType);
        
        log.info("删除字典类型成功: id={}, dictName={}, dictType={}", 
            id, dictType.getDictName(), dictType.getDictType());
    }
    
    @Override
    public void refreshCache() {
        // TODO: 实现字典缓存刷新逻辑
        // 这里可以清除Redis中的字典缓存，或者重新加载字典数据到缓存
        log.info("刷新字典缓存");
    }
}
