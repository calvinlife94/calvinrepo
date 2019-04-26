package com.zhijieyun.datastructure.service;

import java.util.List;
import java.util.Map;

public interface TemplateStructureService {
	/** 
	 * createTemplateStructure
	 * 新建临时表中归总数据
	 */
	void createTemplateStructure(Map<String, Object> map)throws Exception;
	
	/** createIncrementTemplateStructure
	 * 增量数据创建临时数据
	 */
	void createIncrementTemplateStructure(Map<String, Object> map)throws Exception;
	
	/**
	 * queryTemplateStructure
	 * 查询临时时表中归总数据
	 */
	List<Map<String, Object>> queryTemplateStructure(Map<String, Object> map) throws Exception;
	
	/** queryTemplateStructure
	 * 查询临时增量表中归总数据
	 */
	List<Map<String, Object>> queryIncrementTemplateStructure(Map<String, Object> map)throws Exception;
	
	/**
	 * 保存此批次最大处理的row
	 * @param maxValue
	 * @param jobName
	 * @param shardingItem
	 */
	 void saveBatchMaxid(Long maxValue, String jobName, int shardingItem) throws Exception;
	 
	 /**
	  * 保存价格公示-服务
	  * @param map
	  * @return
	  */
	 List<Map<String,Object>> queryBuildingItems(Map<String, Object> map)throws Exception ;
	 /**
	  * 保存价格公示-药品
	  * @param map
	  * @return
	  */
	 List<Map<String,Object>> queryBuildingDrugs(Map<String, Object> map)throws Exception ;
	
	/**
	 * 同步增量数据
	 * @param map
	 */
	 void mergeIncrementData(Map<String, Object> map)throws Exception ;
}
