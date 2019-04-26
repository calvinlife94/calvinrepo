package com.zhijieyun.datastructure.dao.mapper;

import java.util.List;
import java.util.Map;

public interface PublicHealthMapper {

	/**
	 * 查询健康体检主表兼从表
	 * 
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> getJktjExaminfoByIdMap(Map<String, List<Integer>> map)throws Exception;
	
	/**
	 * 个人信息（主表）查询
	 * 
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> getChssInfoByMap(Map<String, String> map)throws Exception;
	
	/**
	 * 查询健康档案-传染病详情子表
	 * 
	 * @param map
	 * @return
	 */
	List<Map<String,Object>> queryServiceInfectiousDiseaseManagementById(Map<String, List<Integer>> map)throws Exception;
	
	/**
	 * 查询健康档案-预防接种详情主表
	 * 
	 * @param map
	 * @return
	 */
	List<Map<String,Object>> queryVaccinationRecordById(Map<String, List<Integer>> map)throws Exception;
	
	/**
	 * 健康档案-产后42天检查
	 * @return
	 */
	List<Map<String, Object>> queryServicePostpartumExaminationRecordById(Map<String, List<Integer>> map)throws Exception;
	
	/**
	 * 健康档案-新生儿访视基本信息（主表）
	 * @return
	 */
	List<Map<String, Object>> queryNeonatalVisitRecordById(Map<String, List<Integer>> map)throws Exception;
	
	/**
	 * 健康档案-产后访视
	 * @return
	 */
	List<Map<String, Object>> queryServicePostpartumVisitRecordById(Map<String, List<Integer>> map)throws Exception;
	
	/**
	 * 健康档案-残疾人管理
	 * @return
	 */
	List<Map<String, Object>> querydisabledManagementById(Map<String, List<Integer>> map)throws Exception;
	
	/**
	 * 服务活动索引数据查询metaSetCode
	 * 
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> getMetaSetCodeByHrn(Map<String, String> map)throws Exception;
	
	/**
	 * 糖尿病管理卡查询
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> queryServiceDiabetesManagementCardDtoById(Map<String, List<Integer>> map)throws Exception;
	
	
	
	
	/**
	 * 糖尿病随访卡查询
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> queryServiceDiabetesVisitCardDtoById(Map<String, String> map)throws Exception;

	List<Map<String, Object>> queryHeavyPsychiatricVisitById(Map<String, List<Integer>> map)throws Exception;

	List<Map<String, Object>> queryHeavyPsychiatricVisitDtoByCid(Map<String, String> mapPare)throws Exception;

	List<Map<String, Object>> queryHeavyPsychiatricVisitDrugDtoById(Map<String, String> mapParee)throws Exception;

	List<Map<String, Object>> queryServiceMadmenManagingCardDtoById(Map<String, List<Integer>> map)throws Exception;

	List<Map<String, Object>> queryHypertensivePatVisit(Map<String, String> mappare)throws Exception;

	List<Map<String, Object>> queryBatchPersonals(List<Integer> ids)throws Exception;
	
	/**
	 * 新增数据预览
	 * @param mappara
	 * @return
	 */
	List<Map<String, Object>> getGynecologicalDiseasesSurveyByMap(Map<String, List<Integer>> mappara)throws Exception;

	List<Map<String, Object>> getChildHealthExaminationByMap(Map<String, List<Integer>> mappara)throws Exception;

	List<Map<String, Object>> getChdManagerByMap(Map<String, List<Integer>> mappara)throws Exception;

	/**
	 * 死亡证明
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> querydeathCertificateById(Map<String, List<Integer>> map);
	/**
	 * 出生医学证明
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> querybirthMedicalCertById(Map<String, List<Integer>> map);
	//职业病报告卡
	List<Map<String, Object>> queryoccupationalDiseaseById(Map<String, List<Integer>> map);

	/**
	 * 食源性疾病报告卡
	 * @param map
	 * @return
	 */
	List<Map<String, Object>> queryFoodborneDiseaseReportById(Map<String, List<Integer>> map);
}
