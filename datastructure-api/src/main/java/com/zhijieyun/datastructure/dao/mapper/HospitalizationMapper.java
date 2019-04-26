package com.zhijieyun.datastructure.dao.mapper;

import java.util.List;
import java.util.Map;

public interface HospitalizationMapper {
	/**
	 * 根据人员批量查询住院信息(queryBatchHospitalizations)
	 * @param list
	 * @return
	 */
	List<Map<String, Object>> queryBatchHospitalizations(List<Integer> list)throws Exception;

	/**
	 * 关联查询病案首页主子表
	 * 
	 * @return
	 */
	List<Map<String, Object>> getHomePage(Map<String, String> map)throws Exception;
	
	/**
	 * 查询住院出院小结详情
	 * 
	 * @param map
	 * @return
	 */
	List<Map<String,Object>> queryOutHospitalRecordsByMap(Map<String, String> map)throws Exception;
	/**
	 * 查询住院入院记录主表
	 * 
	 * @param map
	 * @return
	 */
	List<Map<String,Object>> queryAdmissionRecord(Map<String, String> map)throws Exception;
	/**
	 * 查询住院住院诊断详情
	 * 
	 * @param map
	 * @return
	 */
	List<Map<String,Object>> queryInHosDiagnose(Map<String, String> map)throws Exception;
	/**
	 * test医嘱主表查询
	 * 
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> getOrdersByMap(Map<String, String> map)throws Exception;
	/**
	 * 住院医嘱
	 * @return
	 */
	List<Map<String, Object>> selectListOrders(Map<String, String> map)throws Exception;
	
	/**
	 * test住院费用主表查询
	 * 
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> getInhostCostByMap(Map<String, String> map)throws Exception;

}
