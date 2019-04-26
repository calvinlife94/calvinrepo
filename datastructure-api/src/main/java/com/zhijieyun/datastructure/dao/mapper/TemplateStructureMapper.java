package com.zhijieyun.datastructure.dao.mapper;

import java.util.List;
import java.util.Map;

public interface TemplateStructureMapper {
	
	/** createTemplateStructure
	 * 新建临时时表中归总数据
	 */
	void createTemplateStructure(Map<String, Object> map)throws Exception;
	
	/** createIncrementTemplateStructure
	 * 增量数据创建临时数据
	 */
	void createIncrementTemplateStructure(Map<String, Object> map)throws Exception;
	
	/** queryTemplateStructure
	 * 查询临时时表中归总数据
	 */
	List<Map<String, Object>> queryTemplateStructure(Map<String, Object> map)throws Exception;
	
	
	/** queryTemplateStructure
	 * 查询临时增量表中归总数据
	 */
	List<Map<String, Object>> queryIncrementTemplateStructure(Map<String, Object> map)throws Exception;
	
	/**
	 * 查询已插入数据的最大id
	 * @param param
	 * @return
	 */
	Long queryLastBatchMaxIdByJobName(Map<String, Object> param)throws Exception;
	
	/**
	 * 保存此批次最大处理的row
	 * @param maxValue
	 * @param jobName
	 * @param shardingItem
	 */
	 void saveBatchMaxid(Map<String, Object> map)throws Exception;
	 /**
	  * 同步数据 同步增量数据和原始数据
	  * @param map
	  * @throws Exception
	  */
	 void mergeIncrementData(Map<String, Object> map)throws Exception;
}
