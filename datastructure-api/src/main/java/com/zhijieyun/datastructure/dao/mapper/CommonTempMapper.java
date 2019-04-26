package com.zhijieyun.datastructure.dao.mapper;

import java.util.List;
import java.util.Map;
@SuppressWarnings("rawtypes")
public interface CommonTempMapper {
	/**
	 * 创建或新增(删除数据)'临时表'并填充数据
	 * @param map
	 * @return
	 */

	int  createInsertIOTempTableName(Map map)throws Exception; 
	
	/**
	 * 查询和保存MaxId
	 * @param jobName
	 * @param shardingItem
	 */
	void queryAndSaveMaxId(Map map)throws Exception; 
	
	/**
	 * 查询已插入数据的最大id
	 * @param param
	 * @return
	 */
	Long queryLastBatchMaxIdByJobName(Map<String, Object> param)throws Exception;
	
    /**
     * 
     * <p>Title: getOpExamByMap</p>  
     * <p>Description: 门诊,住院检查查询</p>  
     * <p>MergeBy: Zhangweijie</p> 
     * @param hashMap key:orgCode、key:(zyId或mzId)
     * @return List<Map>
     */
	List<Map<String, Object>> getCommonExamByMap(Map hashMap)throws Exception;
    /**
     * 
     * <p>Title: getOpInspectByFormMap</p>  
     * <p>Description:  门诊,住院检验（主表兼从表）查询</p>  
     * <p>MergeBy: Zhangweijie</p> 
     * @param hashMap key:orgCode、key:(zyId或mzId)
     * @return List<Map>
     */
	List<Map<String, Object>> getCommonInspectByFormMap(Map hashMap)throws Exception;
    /**
     * 
     * <p>Title: querySurgeryRecord</p>  
     * <p>Description:  门诊,住院手术（主表兼从表）查询</p>  
     * <p>MergeBy: Zhangweijie</p> 
     * @param hashMap key:orgCode、key:(zyId或mzId) 、key:operationId(可选)
     * @return List<Map>
     */
	List<Map<String, Object>> queryCommonSurgeryRecord(Map hashMap)throws Exception;
	
	
	/**
	 * 价格公示-药品
	 */
	List<Map<String,Object>> queryTbDrug(Map<String, Object> map)throws Exception;

	/**
	 * 价格公示-服务
	 */
	List<Map<String, Object>> queryTbItemPrice(Map<String, Object> map)throws Exception;
	
}
