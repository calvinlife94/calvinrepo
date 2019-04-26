package com.zhijieyun.datastructure.service;

import java.util.Map;

public interface CommonTempService {
	/**
	 * 创建或新增(删除数据)'临时表'并填充数据
	 * @param map
	 * @return
	 */

	int  createInsertIOTempTableName(Map<String, Object> map)throws Exception; 
	
	/**
	 * 查询和保存MaxId
	 * @param jobName
	 * @param shardingItem
	 */
	void queryAndSaveMaxId(Map<String, Object> map)throws Exception; 
	
	/**
	 * 查询已插入数据的最大id
	 * @param param
	 * @return
	 */
	Long queryLastBatchMaxIdByJobName(Map<String, Object> param)throws Exception;
}
