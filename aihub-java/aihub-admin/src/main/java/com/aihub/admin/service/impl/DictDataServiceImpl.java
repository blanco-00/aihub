package com.aihub.admin.service.impl;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.CreateDictDataRequest;
import com.aihub.admin.dto.request.UpdateDictDataRequest;
import com.aihub.admin.dto.response.DictDataResponse;
import com.aihub.admin.entity.DictData;
import com.aihub.admin.entity.DictType;
import com.aihub.common.web.exception.BusinessException;
import com.aihub.admin.mapper.DictDataMapper;
import com.aihub.admin.mapper.DictTypeMapper;
import com.aihub.admin.service.DictDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典数据服务实现
 */
@Slf4j
@Service
public class DictDataServiceImpl implements DictDataService {
    
    @Autowired
    private DictDataMapper dictDataMapper;
    
    @Autowired
    private DictTypeMapper dictTypeMapper;
    
    @Override
    public PageResult<DictDataResponse> getDictDataListByType(String dictType, Integer current, Integer size) {
        // 计算偏移量
        Long offset = (long) (current - 1) * size;
        
        // 查询字典数据列表
        List<DictDataResponse> dictDataList = dictDataMapper.selectDictDataListByType(
            dictType, offset, size
        );
        
        // 统计总数
        Long total = dictDataMapper.countDictDataListByType(dictType);
        
        // 计算总页数
        Long pages = (total + size - 1) / size;
        
        // 构建分页结果
        PageResult<DictDataResponse> result = new PageResult<>();
        result.setRecords(dictDataList);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages(pages);
        
        return result;
    }
    
    @Override
    public DictDataResponse getDictDataById(Long id) {
        DictData dictData = dictDataMapper.selectById(id);
        if (dictData == null || dictData.getIsDeleted() == 1) {
            throw new BusinessException("字典数据不存在");
        }
        
        DictDataResponse response = new DictDataResponse();
        response.setId(dictData.getId());
        response.setDictType(dictData.getDictType());
        response.setDictLabel(dictData.getDictLabel());
        response.setDictValue(dictData.getDictValue());
        response.setSortOrder(dictData.getSortOrder());
        response.setStatus(dictData.getStatus());
        response.setRemark(dictData.getRemark());
        response.setCreatedAt(dictData.getCreatedAt());
        response.setUpdatedAt(dictData.getUpdatedAt());
        
        return response;
    }
    
    @Override
    @Transactional
    public void createDictData(CreateDictDataRequest request) {
        // 检查字典类型是否存在
        DictType dictType = dictTypeMapper.selectOne(
            new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictType, request.getDictType())
                .eq(DictType::getIsDeleted, 0)
        );
        
        if (dictType == null) {
            log.warn("创建字典数据失败：字典类型不存在: dictType={}", request.getDictType());
            throw new BusinessException("字典类型不存在");
        }
        
        // 检查同一字典类型下字典键值是否已存在
        DictData existing = dictDataMapper.selectOne(
            new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictType, request.getDictType())
                .eq(DictData::getDictValue, request.getDictValue())
                .eq(DictData::getIsDeleted, 0)
        );
        
        if (existing != null) {
            log.warn("创建字典数据失败：字典键值已存在: dictType={}, dictValue={}", 
                request.getDictType(), request.getDictValue());
            throw new BusinessException("该字典类型下字典键值已存在");
        }
        
        // 创建字典数据
        DictData dictData = new DictData();
        dictData.setDictType(request.getDictType());
        dictData.setDictLabel(request.getDictLabel());
        dictData.setDictValue(request.getDictValue());
        dictData.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        dictData.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        dictData.setRemark(request.getRemark());
        dictData.setIsDeleted(0);
        dictData.setCreatedAt(LocalDateTime.now());
        dictData.setUpdatedAt(LocalDateTime.now());
        
        dictDataMapper.insert(dictData);
        log.info("创建字典数据成功: id={}, dictType={}, dictLabel={}, dictValue={}", 
            dictData.getId(), dictData.getDictType(), dictData.getDictLabel(), dictData.getDictValue());
    }
    
    @Override
    @Transactional
    public void updateDictData(Long id, UpdateDictDataRequest request) {
        // 查询字典数据
        DictData dictData = dictDataMapper.selectById(id);
        if (dictData == null || dictData.getIsDeleted() == 1) {
            throw new BusinessException("字典数据不存在");
        }
        
        // 如果字典键值有变化，检查新键值是否已被使用
        if (!dictData.getDictValue().equals(request.getDictValue()) || 
            !dictData.getDictType().equals(request.getDictType())) {
            DictData existing = dictDataMapper.selectOne(
                new LambdaQueryWrapper<DictData>()
                    .eq(DictData::getDictType, request.getDictType())
                    .eq(DictData::getDictValue, request.getDictValue())
                    .eq(DictData::getIsDeleted, 0)
                    .ne(DictData::getId, id)
            );
            
            if (existing != null) {
                log.warn("更新字典数据失败：字典键值已存在: dictType={}, dictValue={}, id={}", 
                    request.getDictType(), request.getDictValue(), id);
                throw new BusinessException("该字典类型下字典键值已存在");
            }
        }
        
        // 更新字典数据
        dictData.setDictType(request.getDictType());
        dictData.setDictLabel(request.getDictLabel());
        dictData.setDictValue(request.getDictValue());
        if (request.getSortOrder() != null) {
            dictData.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            dictData.setStatus(request.getStatus());
        }
        dictData.setRemark(request.getRemark());
        dictData.setUpdatedAt(LocalDateTime.now());
        
        dictDataMapper.updateById(dictData);
        log.info("更新字典数据成功: id={}, dictType={}, dictLabel={}, dictValue={}", 
            id, dictData.getDictType(), dictData.getDictLabel(), dictData.getDictValue());
    }
    
    @Override
    @Transactional
    public void deleteDictData(Long id) {
        // 查询字典数据
        DictData dictData = dictDataMapper.selectById(id);
        if (dictData == null || dictData.getIsDeleted() == 1) {
            throw new BusinessException("字典数据不存在");
        }
        
        // 逻辑删除
        dictData.setIsDeleted(1);
        dictData.setUpdatedAt(LocalDateTime.now());
        dictDataMapper.updateById(dictData);
        
        log.info("删除字典数据成功: id={}, dictType={}, dictLabel={}, dictValue={}", 
            id, dictData.getDictType(), dictData.getDictLabel(), dictData.getDictValue());
    }
}
