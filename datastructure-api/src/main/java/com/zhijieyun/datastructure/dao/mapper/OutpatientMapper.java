package com.zhijieyun.datastructure.dao.mapper;

import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface OutpatientMapper {
	/**
	 * 根据人员批量查询门诊信息(queryBatchOutpatients)
	 * @param list
	 * @return
	 */
	List<Map<String, Object>> queryBatchOutpatients(List<Integer> list)throws Exception;
	
	/**
	 * 根据临时表查询数据(getOpRegisterByMap)
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> getOpRegisterByMap(Map map)throws Exception;
	
    /**
     * 
     * <p>Title: getOpRecipeByMap</p>  
     * <p>Description: 门诊处方（主表兼从表）查询(1000)</p>  
     * <p>MergeBy: Zhangweijie</p> 
     * @param hashMap  key: mzId key:orgCode key:recipeId(可选参数)
     * @return List<Map>
     */
	List<Map<String, Object>> getOpRecipeByMap(Map hashMap)throws Exception;
	
    /**
     * 
     * <p>Title: getOpChargeByMap</p>  
     * <p>Description:  门诊费用（主表兼从表）查询(1000)</p>  
     * <p>MergeBy: Zhangweijie</p> 
     * @param hashMap key:mzId、key:orgCode
     * @return List<Map>
     */
	List<Map<String, Object>> getOpChargeByMap(Map hashMap)throws Exception;
	
    /**
     * 
     * <p>Title: queryOutPatientRecord</p>  
     * <p>Description:  门诊就诊查询(1000)</p>  
     * <p>MergeBy: Zhangweijie</p> 
     * @param hashMap key:mzId、key:orgCode
     * @return List<Map>
     */
	List<Map<String, Object>> queryOutPatientRecord(Map hashMap)throws Exception;
	
    /**
     * 
     * <p>Title: getOpEmroEmeByMap</p>  
     * <p>Description:  门（急）诊病历表查询(1000)</p>  
     * <p>MergeBy: Zhangweijie</p> 
     * @param hashMap key:mzId、key:orgCode
     * @return List<Map>
     */
	List<Map<String, Object>> getOpEmroEmeByMap(Map hashMap)throws Exception;

}
