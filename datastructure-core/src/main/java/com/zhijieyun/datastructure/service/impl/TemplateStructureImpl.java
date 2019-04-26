package com.zhijieyun.datastructure.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhijieyun.datastructure.dao.mapper.CommonTempMapper;
import com.zhijieyun.datastructure.dao.mapper.TemplateStructureMapper;
import com.zhijieyun.datastructure.service.TemplateStructureService;

@Service("templateStructureService")
public class TemplateStructureImpl implements TemplateStructureService{
	@Autowired
	private TemplateStructureMapper templateStructureMapper;
	
	@Autowired
	private CommonTempMapper commonTempMapper;

	@Override
	public List<Map<String, Object>> queryTemplateStructure(Map<String, Object> map)throws Exception {
		return templateStructureMapper.queryTemplateStructure(map);
	}

	@Override
	public void createTemplateStructure(Map<String, Object> map) throws Exception {
		templateStructureMapper.createTemplateStructure(map);		
	}

	@Override
	public void saveBatchMaxid(Long maxValue, String jobName, int shardingItem) throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("maxid", maxValue);
		paramMap.put("jobName", jobName);
		paramMap.put("shardingItem", shardingItem);
		templateStructureMapper.saveBatchMaxid(paramMap);
	}

	@Override
	public List<Map<String,Object>> queryBuildingItems(Map<String, Object> map) throws Exception{
		return commonTempMapper.queryTbItemPrice(map);
	}

	@Override
	public List<Map<String,Object>> queryBuildingDrugs(Map<String, Object> map) throws Exception{
          return commonTempMapper.queryTbDrug(map);
	}

	@Override
	public void createIncrementTemplateStructure(Map<String, Object> map) throws Exception {
		templateStructureMapper.createIncrementTemplateStructure(map);
	}

	@Override
	public List<Map<String, Object>> queryIncrementTemplateStructure(Map<String, Object> map) throws Exception {
		return templateStructureMapper.queryIncrementTemplateStructure(map);
	}

	@Override
	public void mergeIncrementData(Map<String, Object> map) throws Exception {
		templateStructureMapper.mergeIncrementData(map);
	}
}
