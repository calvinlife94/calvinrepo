package com.zhijieyun.datastructure.service;

import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface OutpatientService {

	
	/**
	 * 根据临时表查询数据(getOpRegisterByMap)
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> getOpRegisterByMap(Map map) throws Exception;
	
	/**
	 * 构建数据
	 * @param map
	 * @return
	 */
	void singleOutpatientRecordBuild(List<Map<String, Object>> outpatientParam,Map param) throws Exception;

	/**
	 * 查询门诊信息主表数据
	 * @param list
	 * @return
	 * @throws Exception
	 */
	List<Map<String, Object>> queryBatchOutpatients(List<Integer> list) throws Exception;
	
}
