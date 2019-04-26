package com.zhijieyun.datastructure.service;

import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface HospitalizationService {
	/**
	 * 根据临时表查询数据(queryOutHospitalRecordsByMap)
	 * @param map
	 * @return
	 */

	List<Map<String, Object>> queryOutHospitalRecordsByMap(Map map) throws Exception; 

	/**
	 * 构建数据
	 * @param map
	 * @return
	 */
	void singleInhosRecordBuild(List<Map<String, Object>> outpatientParam,Map param) throws Exception;

	/**
	 * 查询住院信息主表数据
	 * @param list
	 * @return
	 * @throws Exception
	 */
	List<Map<String, Object>> queryBatchHospitalizations(List<Integer> list) throws Exception;
	
	
}
