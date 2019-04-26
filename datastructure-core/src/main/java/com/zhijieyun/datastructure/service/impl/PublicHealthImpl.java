package com.zhijieyun.datastructure.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.zhijieyun.datastructure.constants.Constants;
import com.zhijieyun.datastructure.dao.mapper.PublicHealthMapper;
import com.zhijieyun.datastructure.service.PublicHealthService;
import com.zhijieyun.datastructure.util.JobCommonUtil;
import com.zhijieyun.datastructure.util.MapUtils;
import com.zhijieyun.gwc.util.StringUtil;

import net.sf.json.JSONObject;

@Service("publicHealthService")
@SuppressWarnings("unchecked")
public class PublicHealthImpl implements PublicHealthService{
	
	private static Logger logger = Logger.getLogger(PublicHealthImpl.class);
	
	private static  ExecutorService cachedThreadPool = Executors.newFixedThreadPool(50);  
	
	@Autowired
	private PublicHealthMapper publicHealthMapper; 
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	public final static List<String> METASETCODES = Arrays.asList("CHM130112","H02","H24","H42","H16","CHM130129","CHM130102","H09","H17","H04","H13","H53","H54","H29","H06","H36","H46");


	@Override
	public List<Map<String, Object>> queryBatchPersonals(List<Integer> ids) throws Exception {
		return publicHealthMapper.queryBatchPersonals(ids);
	}

	@Override
	public void personalPublicHealthBuild(Map<String, Object> param) throws Exception {
		   DBCollection coll_publicHealth = mongoTemplate.getCollection("personalPublicHealth");
		   
		   DBCollection coll_healthTree = mongoTemplate.getCollection("personalHealthTreeIndexJson");

			List<Callable<List<Map<String, Object>>>> callableList=new ArrayList<>();
			Map<String, List<Integer>> map=new LinkedHashMap<>();
			for ( Entry<String, Object> entry : param.entrySet()) {
				if(METASETCODES.contains(entry.getKey())){
					map.put(entry.getKey(),(List<Integer>) entry.getValue());
				}
			}
			//对查询数据重新分组
			for(Entry<String, List<Integer>> entry : map.entrySet()){//有序的map
				switch (entry.getKey()) {
				case "CHM130112":
					// 构建健康档案-糖尿病随访卡详情
					Callable<List<Map<String, Object>>> diabetesManagementCardListCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//List<Object> diabetesManagementCardList = buildDiabetesManagementCard(map, mpiId);//1
							logger.info("糖尿病 开始构建.");
							List<Map<String, Object>> diabetesManagementCardMapList = publicHealthMapper
									.queryServiceDiabetesManagementCardDtoById(map);
							logger.info("糖尿病 构建完毕.");
							if(!diabetesManagementCardMapList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(diabetesManagementCardMapList);
							}else{
								diabetesManagementCardMapList = new  ArrayList<Map<String, Object>>();
							}
							return diabetesManagementCardMapList;
						}
					};
					callableList.add(diabetesManagementCardListCallable);//1
					break;
				case "H02":
					// 构建健康档案-健康体检详情
					Callable<List<Map<String, Object>>> healthExaminationCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//List<Object> healthExamination = buildHealthExamination(map, mpiId);//2
							logger.info("健康体检 开始构建.");
							List<Map<String, Object>> healthExamination = publicHealthMapper.getJktjExaminfoByIdMap(map);
							logger.info("健康体检构建完毕.");
							if(!healthExamination.isEmpty()){
								MapUtils.mapExcludeNullRecursion(healthExamination);
							}else{
								healthExamination = new  ArrayList<Map<String, Object>>();
							}
							return healthExamination;
						}
					};
					callableList.add(healthExaminationCallable);//2
					break;
				case "H24":
					// 构建健康档案-传染病报告卡详情
					Callable<List<Map<String, Object>>> infectiousDiseaseManagementListCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//List<Object> infectiousDiseaseManagementList = buildInfectiousDiseaseManagement(map, mpiId);//3
							logger.info("传染病报告开始构建.");
							List<Map<String, Object>> infectiousDiseaseManagementMapList = publicHealthMapper.queryServiceInfectiousDiseaseManagementById(map);
							logger.info("传染病报告构建完毕.");
							if(!infectiousDiseaseManagementMapList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(infectiousDiseaseManagementMapList);
							}else{
								infectiousDiseaseManagementMapList = new  ArrayList<Map<String, Object>>();
							}
							return infectiousDiseaseManagementMapList;
						}
					};
					callableList.add(infectiousDiseaseManagementListCallable);//3
					break;
				case "H42":
					// 构建健康档案-儿童免疫接种信息
					Callable<List<Map<String, Object>>> vaccinationRecordListCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//List<Object> vaccinationRecordList = buildVaccinationRecord(map, mpiId);//4
							logger.info("儿童免疫接种开始构建.");
							List<Map<String, Object>> vaccinationRecordMapList = publicHealthMapper.queryVaccinationRecordById(map);
							logger.info("传儿童免疫接种构建完毕.");
							if(!vaccinationRecordMapList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(vaccinationRecordMapList);
							}else{
								vaccinationRecordMapList = new  ArrayList<Map<String, Object>>();
							}
							return vaccinationRecordMapList;
						}
					};
					callableList.add(vaccinationRecordListCallable);//4
					break;
				case "H16":
					// 构建健康档案-产后42天健康检查信息
					Callable<List<Map<String, Object>>> postpartumExaminationListCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//List<Object> postpartumExaminationList = buildPostpartumExamination(map, mpiId);//5
							logger.info("产后42天开始构建.");
							List<Map<String, Object>> postpartumExaminationRecordMapList = publicHealthMapper.queryServicePostpartumExaminationRecordById(map);
							logger.info("产后42天构建完毕.");
							if(!postpartumExaminationRecordMapList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(postpartumExaminationRecordMapList);
							}else{
								postpartumExaminationRecordMapList = new  ArrayList<Map<String, Object>>();
							}
							return postpartumExaminationRecordMapList;
						}
					};
					callableList.add(postpartumExaminationListCallable);//5
					break;
				case "CHM130129":
					// 构建健康档案-重性精神疾病患者管理卡
					Callable<List<Map<String, Object>>> heavyPsychiatricVisitCardListCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//List<Object> heavyPsychiatricVisitCardList = buildHeavyPsychiatricVisitCard(map);//6
							logger.info("重性精神疾病开始构建.");
							List<Map<String, Object>> heavyPsychiatricVisitList = publicHealthMapper.queryHeavyPsychiatricVisitById(map);
							logger.info("重性精神疾病构建完毕.");
							if(!heavyPsychiatricVisitList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(heavyPsychiatricVisitList);
							}else{
								heavyPsychiatricVisitList = new  ArrayList<Map<String, Object>>();
							}
							return heavyPsychiatricVisitList;
						}
					};
					callableList.add(heavyPsychiatricVisitCardListCallable);//6
					break;
				case "CHM130102":
					// 构建健康档案-高血压患者管理卡
					Callable<List<Map<String, Object>>> buildMadmenManagingCardCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//List<Object> buildMadmenManagingCard = buildMadmenManagingCard(map);//7
							logger.info("高血压患者开始构建.");
							List<Map<String, Object>> madmenManagingCardMapList = publicHealthMapper.queryServiceMadmenManagingCardDtoById(map);
							logger.info("高血压患者构建完毕.");
							if(!madmenManagingCardMapList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(madmenManagingCardMapList);
							}else{
								madmenManagingCardMapList = new  ArrayList<Map<String, Object>>();
							}
							return madmenManagingCardMapList;
						}
					};
					callableList.add(buildMadmenManagingCardCallable);//7
					break;
				case "H09":
					// 构建健康档案-新生儿访视基本信息
					Callable<List<Map<String, Object>>> buildNeonatalVisitCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//List<Object> buildNeonatalVisit = buildNeonatalVisit(map);//8
							logger.info("新生儿访视开始构建.");
							List<Map<String, Object>> neonatalVisitRecordMapList = publicHealthMapper.queryNeonatalVisitRecordById(map);
							logger.info("新生儿访视构建完毕.");
							if(!neonatalVisitRecordMapList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(neonatalVisitRecordMapList);
							}else{
								neonatalVisitRecordMapList = new  ArrayList<Map<String, Object>>();
							}
							return neonatalVisitRecordMapList;
						}
					};
					callableList.add(buildNeonatalVisitCallable);//8
					break;
				case "H17":
					// 构建健康档案-产后访视
					Callable<List<Map<String, Object>>> buildPostpartumVisitRecordCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//List<Object> buildPostpartumVisitRecord = buildPostpartumVisitRecord(map);//9
							logger.info("产后访视开始构建.");
							List<Map<String, Object>> PostpartumVisitRecordMapList = publicHealthMapper.queryServicePostpartumVisitRecordById(map);
							logger.info("产后访视构建完毕.");
							if(!PostpartumVisitRecordMapList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(PostpartumVisitRecordMapList);
							}else{
								PostpartumVisitRecordMapList = new  ArrayList<Map<String, Object>>();
							}
							return PostpartumVisitRecordMapList;
						}
					};
					callableList.add(buildPostpartumVisitRecordCallable);//9
					break;
				case "H04":
					// 构建健康档案-儿童健康体检
					Callable<List<Map<String,Object>>> buildChildHealthExaminationCallable=new Callable<List<Map<String,Object>>>() {
						@Override
						public List<Map<String,Object>> call() throws Exception {
							logger.info("产后访视开始构建.");
							List<Map<String, Object>> childHealthExaminationList =  publicHealthMapper.getChildHealthExaminationByMap(map);//10
							logger.info("产后访视构建完毕.");
							if(!childHealthExaminationList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(childHealthExaminationList);
							}else{
								childHealthExaminationList = new  ArrayList<Map<String, Object>>();
							}
							return childHealthExaminationList;
						}
					};
					callableList.add(buildChildHealthExaminationCallable);//10
					break;
				case "H13":
					// 构建健康档案-妇女病普查表
					Callable<List<Map<String, Object>>> buildGynecologicalDiseasesSurveyCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							logger.info("健康档案-妇女病普查表开始构建.");
							List<Map<String, Object>> gynecologicalDiseasesSurveyList = publicHealthMapper.getChildHealthExaminationByMap(map);//11
							logger.info("健康档案-妇女病普查表构建完毕.");
							if(!gynecologicalDiseasesSurveyList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(gynecologicalDiseasesSurveyList);
							}else{
								gynecologicalDiseasesSurveyList = new  ArrayList<Map<String, Object>>();
							}
							return gynecologicalDiseasesSurveyList;
						}
					};
					callableList.add(buildGynecologicalDiseasesSurveyCallable);//11
					break;
				case "H53":
					// 构建健康档案-冠心病患者信息
					Callable<List<Map<String, Object>>> buildChdManagerCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							logger.info("冠心病患者开始构建.");
							List<Map<String, Object>> chdManagerList = publicHealthMapper.getChdManagerByMap(map);//11
							logger.info("冠心病患者构建完毕.");
							if(!chdManagerList.isEmpty()){
								MapUtils.mapExcludeNullRecursion(chdManagerList);
							}else{
								chdManagerList = new  ArrayList<Map<String, Object>>();
							}
							return chdManagerList;
						}
					};
					callableList.add(buildChdManagerCallable);//12
					break;
				case "H54":
					// 构建健康档案-残疾人管理
					Callable<List<Map<String, Object>>> buildDisabledManagementRecordCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							logger.info("残疾人管理开始构建.");
							List<Map<String, Object>> disabledManagementRecord = publicHealthMapper.querydisabledManagementById(map);//12
							logger.info("残疾人管理构建完毕.");
							if(!disabledManagementRecord.isEmpty()){
								MapUtils.mapExcludeNullRecursion(disabledManagementRecord);
							}else{
								disabledManagementRecord = new  ArrayList<Map<String, Object>>();
							}
							return disabledManagementRecord;
						}
					};
					callableList.add(buildDisabledManagementRecordCallable);//13
					break;
				case "H29":
					// 构建健康档案-死亡证明
					Callable<List<Map<String, Object>>> deathCertificateManagementRecordCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							logger.info("死亡证明开始构建.");
							List<Map<String, Object>> deathCertificateManagementRecord = publicHealthMapper.querydeathCertificateById(map);//12
							logger.info("死亡证明构建完毕.");
							if(!deathCertificateManagementRecord.isEmpty()){
								MapUtils.mapExcludeNullRecursion(deathCertificateManagementRecord);
							}else{
								deathCertificateManagementRecord = new  ArrayList<Map<String, Object>>();
							}
							return deathCertificateManagementRecord;
						}
					};
					callableList.add(deathCertificateManagementRecordCallable);//14
					break;
				case "H06":
					// 构建健康档案-出生医学证明
					Callable<List<Map<String, Object>>> birthMedicalCertManagementRecordCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							logger.info("出生医学证明开始构建.");
							List<Map<String, Object>> birthMedicalCertManagementRecord = publicHealthMapper.querybirthMedicalCertById(map);//12
							logger.info("出生医学证明构建完毕.");
							if(!birthMedicalCertManagementRecord.isEmpty()){
								MapUtils.mapExcludeNullRecursion(birthMedicalCertManagementRecord);
							}else{
								birthMedicalCertManagementRecord = new  ArrayList<Map<String, Object>>();
							}
							return birthMedicalCertManagementRecord;
						}
					};
					callableList.add(birthMedicalCertManagementRecordCallable);//15
					break;
				case "H36":
					// 构建健康档案-职业病报告卡
					Callable<List<Map<String, Object>>> occupationalDiseaseManagementRecordCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							logger.info("职业病报告卡开始构建.");
							List<Map<String, Object>> occupationalDiseaseManagementRecord = publicHealthMapper.queryoccupationalDiseaseById(map);//12
							logger.info("职业病报告卡构建完毕.");
							if(!occupationalDiseaseManagementRecord.isEmpty()){
								MapUtils.mapExcludeNullRecursion(occupationalDiseaseManagementRecord);
							}else{
								occupationalDiseaseManagementRecord = new  ArrayList<Map<String, Object>>();
							}
							return occupationalDiseaseManagementRecord;
						}
					};
					callableList.add(occupationalDiseaseManagementRecordCallable);//16
					break;
				case "H46":
					// 构建健康档案-职业病报告卡
					Callable<List<Map<String, Object>>> FoodborneDiseaseReportCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							logger.info("职业病报告卡开始构建.");
							List<Map<String, Object>> FoodborneDiseaseReport = publicHealthMapper.queryFoodborneDiseaseReportById(map);//12
							logger.info("职业病报告卡构建完毕.");
							if(!FoodborneDiseaseReport.isEmpty()){
								MapUtils.mapExcludeNullRecursion(FoodborneDiseaseReport);
							}else{
								FoodborneDiseaseReport = new  ArrayList<Map<String, Object>>();
							}
							return FoodborneDiseaseReport;
						}
					};
					callableList.add(FoodborneDiseaseReportCallable);//16
					break;
				default:
					break;
				}
				
			}
			
			List<Future<List<Map<String, Object>>>> futures=cachedThreadPool.invokeAll(callableList);
			Map<String, Map<String, Object>>  diabetesManagementData = null;//1
			Map<String, Map<String, Object>>  healthCheckupData = null;//2
			Map<String, Map<String, Object>>  infectiousDiseaseReportData = null;//3
			Map<String, Map<String, Object>>  childImmunizatioData = null;//4
			Map<String, Map<String, Object>>  postpartumExaminationData = null;//5
			Map<String, Map<String, Object>>  severeMentalIllnessManagementData = null;//6
			Map<String, Map<String, Object>>  hypertensiveManagementData = null;//7
			Map<String, Map<String, Object>>  neonateVisitData = null;//8
			Map<String, Map<String, Object>>  postpartumVisitData = null;//9
			Map<String, Map<String, Object>>  childHealthExaminationData = null;//10
			Map<String, Map<String, Object>>  gynecologicalDiseasesSurveyData = null;//11
			Map<String, Map<String, Object>>  chdManagerData = null;//12
			Map<String, Map<String, Object>>  disabledManagementRecordData = null;//13
			Map<String, Map<String, Object>>  deathCertificateManagementRecordData = null;//14
			Map<String, Map<String, Object>>  birthMedicalCertManagementRecordData = null;//15
			Map<String, Map<String, Object>>  occupationalDiseaseManagementRecordData = null;//16
			Map<String, Map<String, Object>>  FoodborneDiseaseReportData = null;//17
			int index=0;
			for(String key : map.keySet()){//有序的map
				List<Map<String, Object>> data=futures.get(index).get(15, TimeUnit.SECONDS);
				switch (key) {
					case "CHM130112":
						diabetesManagementData=changeDataPattern(data);//1
						break;
					case "H02":
						healthCheckupData=changeDataPattern(data);//2
						// 构建健康档案-健康体检详情
						break;
					case "H24":
						infectiousDiseaseReportData=changeDataPattern(data);//3
						// 构建健康档案-传染病报告卡详情
						break;
					case "H42":
						// 构建健康档案-儿童免疫接种信息
						childImmunizatioData=changeDataPattern(data);//4
						break;
					case "H16":
						// 构建健康档案-产后42天健康检查信息
						postpartumExaminationData=changeDataPattern(data);//5
						break;
					case "CHM130129":
						// 构建健康档案-重性精神疾病患者管理卡
						severeMentalIllnessManagementData=changeDataPattern(data);//6
						break;
					case "CHM130102":
						// 构建健康档案-高血压患者管理卡
						hypertensiveManagementData=changeDataPattern(data);//7
						break;
					case "H09":
						// 构建健康档案-新生儿访视基本信息
						neonateVisitData=changeDataPattern(data);//8
						break;
					case "H17":
						// 构建健康档案-产后访视
						postpartumVisitData=changeDataPattern(data);//9
						break;
					case "H04":
						// 构建健康档案-儿童健康体检
						childHealthExaminationData=changeDataPattern(data);//10
						break;
					case "H13":
						// 构建健康档案-妇女病普查表
						gynecologicalDiseasesSurveyData=changeDataPattern(data);//11
						break;
					case "H53":
						// 构建健康档案-冠心病患者信息
						chdManagerData=changeDataPattern(data);//12
						break;
					case "H54":
						//  构建健康档案-残疾人管理
						disabledManagementRecordData=changeDataPattern(data);//13
						break;
					case "H29":
						//  构建健康档案-死亡证明管理
						deathCertificateManagementRecordData=changeDataPattern(data);//14
						break;
					case "H06":
						//  构建健康档案-出生医学证明管理
						birthMedicalCertManagementRecordData=changeDataPattern(data);//15
						break;
					case "H36":
						//  构建健康档案-职业病报告卡管理
						occupationalDiseaseManagementRecordData=changeDataPattern(data);//15
						break;
					case "H46":
						//  构建健康档案-食源性疾病报告卡
						FoodborneDiseaseReportData=changeDataPattern(data);//15
						break;
					default:
						break;
			      }
				  index=index+1;
			}
			Map<String, Object> hospitalizationData= JobCommonUtil.extractTreeInfoById((List<Map<String, Object>>) param.get(Constants.HOSPITALIZATION), Constants.HOSPITALIZATION);
			Map<String, Object> outpatientData=JobCommonUtil.extractTreeInfoById((List<Map<String, Object>>) param.get(Constants.OUTPATIENT), Constants.OUTPATIENT);
			/**
			 * 
			 */
			List<Map<String, Object>> data = (List<Map<String, Object>>) param.get(Constants.PUBLICHEALTHDATA);
			List<DBObject> list=new ArrayList<DBObject>(data.size());
			List<DBObject> healthTreeList=new ArrayList<DBObject>(data.size());
			
			List<DBObject> updateHealthTreeList=new ArrayList<DBObject>(data.size());
			List<DBObject> updateList=new ArrayList<DBObject>(data.size());
			for(Map<String, Object> personalInfo:data){
				String temstr=(String) personalInfo.get("temstr");
				Map<String, List<Integer>> strings = null;
				if(temstr != null){
					strings = com.alibaba.fastjson.JSONObject.parseObject("{"+temstr+"}",Map.class);
				}else{
					break; 
				}
				
				//存储个人数据
				 Object mpiid=personalInfo.get("mpiid");//数据的mpiid
				 //Object hrn=personalInfo.get("mpiid");//数据的hrn
				 Map<String, Object> personalPublicHealth = new ConcurrentHashMap<>();
				 Map<String, Object> healthTree = new ConcurrentHashMap<String, Object>(){
					private static final long serialVersionUID = 1L;
					{
					 put("mpiid",mpiid);
					 put("healthTree",new ConcurrentHashMap<String, List<Map<String, Object>>>(){
						private static final long serialVersionUID = 1L;
						{
							 put("childHealth",new ArrayList<>());
							 put("diseaseControl",new ArrayList<>());
							 put("diseaseManage",new ArrayList<>());
							 put("healthCheckup",new ArrayList<>());
							 put("inpatientRecords",new ArrayList<>());
							 put("outpatientRecords",new ArrayList<>());
							 put("womenHealth",new ArrayList<>());
						 }
					});
				 }};
				 for(Entry<String, List<Integer>> entry: strings.entrySet()){
						if(METASETCODES.contains(entry.getKey())){
							List<Object> diseaseControlMapList= (List<Object>) ((Map<String,Object>)healthTree.get("healthTree")).get("diseaseControl");
							List<Object> diseaseManageMapList= (List<Object>) ((Map<String,Object>)healthTree.get("healthTree")).get("diseaseManage");
							List<Object> womenHealthMapList= (List<Object>) ((Map<String,Object>)healthTree.get("healthTree")).get("womenHealth");
							List<Object> healthCheckupMapList= (List<Object>) ((Map<String,Object>)healthTree.get("healthTree")).get("healthCheckup");
							List<Object> childHealthMapList= (List<Object>) ((Map<String,Object>)healthTree.get("healthTree")).get("childHealth");
							List<Map<String, Object>> owntdata=new ArrayList<>();
							switch (entry.getKey()) {
							case "CHM130112":
								for (Integer integer : entry.getValue()) {
									if(diabetesManagementData.get(integer.toString()) != null){
										owntdata.add(diabetesManagementData.get(integer.toString()));//公卫节点添加
										//树节点构建
										Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"CHM130112",diabetesManagementData.get(integer.toString()));
										diseaseManageMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("diabetesManagementDataList",owntdata);//1
								break;
							case "H02":

								for (Integer integer : entry.getValue()) {
									if(healthCheckupData.get(integer.toString()) != null){
									   owntdata.add(healthCheckupData.get(integer.toString()));
									   Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H02",healthCheckupData.get(integer.toString()));
									   healthCheckupMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("healthCheckupList",owntdata);//2
								// 构建健康档案-健康体检详情
								break;
							case "H24":
							    ////3
								for (Integer integer : entry.getValue()) {
									if(infectiousDiseaseReportData.get(integer.toString()) != null){
									    owntdata.add(infectiousDiseaseReportData.get(integer.toString()));
									    Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H24",infectiousDiseaseReportData.get(integer.toString()));
									    diseaseControlMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("infectiousDiseaseReportList",owntdata);
								// 构建健康档案-传染病报告卡详情
								break;
							case "H42":
								// 构建健康档案-儿童免疫接种信息
								diseaseControlMapList= (List<Object>) ((Map<String,Object>)healthTree.get("healthTree")).get("diseaseControl");
								for (Integer integer : entry.getValue()) {
									if(childImmunizatioData.get(integer.toString()) != null){
									  owntdata.add(childImmunizatioData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H42",childImmunizatioData.get(integer.toString()));
									  diseaseControlMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("childImmunizatioList",owntdata);//4
								break;
							case "H16":
								// 构建健康档案-产后42天健康检查信息
								for (Integer integer : entry.getValue()) {
									if(postpartumExaminationData.get(integer.toString()) != null){
									  Map<String, Object> womenHealthDataMap= 	postpartumExaminationData.get(integer.toString());
									  owntdata.add(womenHealthDataMap);
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H16",womenHealthDataMap);
									  diseaseControlMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("postpartumExaminationList", owntdata);
								break;
							case "CHM130129":
								// 构建健康档案-重性精神疾病患者管理卡
								for (Integer integer : entry.getValue()) {
									if(severeMentalIllnessManagementData.get(integer.toString()) != null){
									      owntdata.add(severeMentalIllnessManagementData.get(integer.toString()));
										  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"CHM130129",severeMentalIllnessManagementData.get(integer.toString()));
										  diseaseControlMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("severeMentalIllnessManagementList",owntdata);//6
								break;
							case "CHM130102":
								// 构建健康档案-高血压患者管理卡
								for (Integer integer : entry.getValue()) {
									if(hypertensiveManagementData.get(integer.toString()) != null){
										owntdata.add(hypertensiveManagementData.get(integer.toString()));
										Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"CHM130102",hypertensiveManagementData.get(integer.toString()));
										diseaseManageMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("hypertensiveManagementList",owntdata);//7
								break;
							case "H09":
								// 构建健康档案-新生儿访视基本信息
								for (Integer integer : entry.getValue()) {
									if(neonateVisitData.get(integer.toString()) != null){
									  owntdata.add(neonateVisitData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H09",neonateVisitData.get(integer.toString()));
									  childHealthMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("neonateVisitList",owntdata);//8
								break;
							case "H17":
								// 构建健康档案-产后访视
								for (Integer integer : entry.getValue()) {
									if(postpartumVisitData.get(integer.toString()) != null){
									  owntdata.add(postpartumVisitData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H17",postpartumVisitData.get(integer.toString()));
									  womenHealthMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("postpartumVisitList",owntdata);//9
								break;
							case "H04":
								// 构建健康档案-儿童健康体检
								for (Integer integer : entry.getValue()) {
									if(childHealthExaminationData.get(integer.toString()) != null){
									  owntdata.add(childHealthExaminationData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H04",childHealthExaminationData.get(integer.toString()));
									  childHealthMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("childHealthExaminationMapList",owntdata);//9
								break;
							case "H13":
								// 构建健康档案-妇女病普查表
								for (Integer integer : entry.getValue()) {
									if(gynecologicalDiseasesSurveyData.get(integer.toString()) != null){
									  owntdata.add(gynecologicalDiseasesSurveyData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H13",gynecologicalDiseasesSurveyData.get(integer.toString()));
									  womenHealthMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("gynecologicalDiseasesSurveyMapList",owntdata);//9
								break;
							case "H53":
								// 构建健康档案-冠心病患者信息
								for (Integer integer : entry.getValue()) {
									if(chdManagerData.get(integer.toString()) != null){
									  owntdata.add(chdManagerData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H53",chdManagerData.get(integer.toString()));
									  diseaseManageMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("chdManagerMapList",owntdata);//9
								break;
							case "H54":
								// 构建健康档案-残疾人管理
								for (Integer integer : entry.getValue()) {
									if(disabledManagementRecordData.get(integer.toString()) != null){
									  owntdata.add(disabledManagementRecordData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H54",disabledManagementRecordData.get(integer.toString()));
									  diseaseManageMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("disabledManagementList",owntdata);//9
								break;
							case "H29":
								// 构建健康档案-死亡医学证明管理
								for (Integer integer : entry.getValue()) {
									if(deathCertificateManagementRecordData.get(integer.toString()) != null){
									  owntdata.add(deathCertificateManagementRecordData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H29",deathCertificateManagementRecordData.get(integer.toString()));
									  diseaseManageMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("deathCertificateManagementList",owntdata);//9
								break;
							case "H06":
								// 构建健康档案-出生医学证明管理
								for (Integer integer : entry.getValue()) {
									if(birthMedicalCertManagementRecordData.get(integer.toString()) != null){
									  owntdata.add(birthMedicalCertManagementRecordData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H06",birthMedicalCertManagementRecordData.get(integer.toString()));
									  diseaseManageMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("birthMedicalCertManagementList",owntdata);//9
								break;
							case "H36":
								// 构建健康档案-职业病报告卡管理
								for (Integer integer : entry.getValue()) {
									if(occupationalDiseaseManagementRecordData.get(integer.toString()) != null){
									  owntdata.add(occupationalDiseaseManagementRecordData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H06",occupationalDiseaseManagementRecordData.get(integer.toString()));
									  diseaseManageMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("occupationalDiseaseManagementList",owntdata);//9
								break;
							case "H46":
								// 构建健康档案-职业病报告卡管理
								for (Integer integer : entry.getValue()) {
									if(FoodborneDiseaseReportData.get(integer.toString()) != null){
									  owntdata.add(FoodborneDiseaseReportData.get(integer.toString()));
									  Map<String, Object> nodeMap= JobCommonUtil.extractTreeInfoById(mpiid,"H46",FoodborneDiseaseReportData.get(integer.toString()));
									  diseaseManageMapList.add(nodeMap);
									}
								}
								personalPublicHealth.put("FoodborneDiseaseReportList",owntdata);//9
								break;
							default:
								break;
					      }
						}else if(Constants.HOSPITALIZATION.equals(entry.getKey())){
							List<Object> healthTreeMapList= (List<Object>) ((Map<String,Object>)healthTree.get("healthTree")).get("inpatientRecords");
							for (Integer integer : entry.getValue()) {
								if(hospitalizationData.get(integer.toString())!=null){
								   healthTreeMapList.add(hospitalizationData.get(integer.toString()));
								}
							}
						}else if(Constants.OUTPATIENT.equals(entry.getKey())){
							List<Object> healthTreeMapList= (List<Object>) ((Map<String,Object>)healthTree.get("healthTree")).get("outpatientRecords");
							for (Integer integer : entry.getValue()) {
								if(outpatientData.get(integer.toString())!=null){
									healthTreeMapList.add(outpatientData.get(integer.toString()));
								}
							}
						}
				 }
				 if (!personalPublicHealth.isEmpty()) {
					 personalInfo.remove("temstr");//对此数据进行移除
					 personalInfo.putAll(personalPublicHealth);//合并数据
					 if("1".equals(personalInfo.get("existsit"))){
						 updateList.add(BasicDBObject.parse(JSONObject.fromObject(personalInfo).toString()));
					 }else{
						 list.add(BasicDBObject.parse(JSONObject.fromObject(personalInfo).toString()));
					 }
				 }
				 if(!healthTree.isEmpty()){//健康树的构建
					 if("1".equals(personalInfo.get("existsit"))){
						 updateHealthTreeList.add(BasicDBObject.parse(JSONObject.fromObject(healthTree).toString()));
					 }else{
						 healthTreeList.add(BasicDBObject.parse(JSONObject.fromObject(healthTree).toString()));
					 }
				 }
			}
			/**
			 * 新增公卫信息
			 */
			insertData(list, coll_publicHealth);
			/**
			 * 更新公卫信息
			 */
			long updatePublicHealthTimeStart=System.currentTimeMillis();
			updatePublicHealthData(updateList, coll_publicHealth);
			long updatePublicHealthTimeEnd=System.currentTimeMillis();
			logger.info("更新公卫信息,耗时:"+(updatePublicHealthTimeEnd-updatePublicHealthTimeStart));
			/**
			 * 健康树构建
			 */
			insertData(healthTreeList,coll_healthTree);
			/**
			 * 更新健康树
			 */
			long updateHealthTreeTimeStart=System.currentTimeMillis();
			updateHealthTreeData(updateHealthTreeList,coll_healthTree);
			long updateHealthTreeTimeEnd=System.currentTimeMillis();
			logger.info("更新健康树信息,耗时:"+(updateHealthTreeTimeEnd-updateHealthTreeTimeStart));
	}

	@Override
	public void personalInfoBuild(List<Map<String, Object>> list) throws Exception {
		MapUtils.mapExcludeNullRecursion(list);
		DBCollection coll_operations = mongoTemplate.getCollection("personalInfo");
		List<DBObject> dbObjList = new ArrayList<DBObject>();
		for(Map<String, Object> map: list){
			BasicDBObject dbObj = BasicDBObject.parse(JSONObject.fromObject(map).toString());
			dbObjList.add(dbObj);
		}
		insertData(dbObjList, coll_operations);
	}
	/**
	 * id做键 
	 *  整个数据做值
	 */
	Map<String, Map<String, Object>> changeDataPattern(List<Map<String, Object>> list){
		Map<String, Map<String, Object>> map=new HashMap<>();
		if(!list.isEmpty()){
			MapUtils.mapExcludeNullRecursion(list);
			for(Map<String, Object> recipe :list){
				map.put(recipe.get("ID").toString(), recipe);
			}
		}
		return map;
	}
	/**
	 * 向mongodb插入数据
	 * @param data
	 * @param collection
	 */
	void insertData(List<DBObject> data,DBCollection collection){
		if(collection!=null && !data.isEmpty()){
			Runnable r=new Runnable() {
				public void run() {
					collection.insert(data);
				}
			};
			r.run();
		}
	};
	
	/**
	 * 批量更新公卫信息
	 * @param data
	 * @param collection
	 */
	void updatePublicHealthData(List<DBObject> data,DBCollection collection){
		if(!data.isEmpty()){
			BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,collection.getName());
			boolean isHavaData=false;
			for(DBObject dbObject: data){
				Query query = new Query(Criteria.where("mpiid").is(dbObject.get("mpiid")));
				Update update = new Update();
				Set<String> strings=dbObject.keySet();
				//update对象不为空    引入booleans标记记录是否有值
				boolean booleans=false;
				for(String string:strings){
					if(!StringUtil.isEmpty(string)&&string.endsWith("List")){
						if(dbObject.get(string)  instanceof List){
							List<Object> list=(List<Object>) dbObject.get(string);
							Object[] list1=list.toArray();
							if(!list.isEmpty()){
								isHavaData=true;
								booleans=true;
								update.pushAll(string, list1);
							}
						}
					}
				}
				if(booleans)ops.updateOne(query,update);
			}
			if(isHavaData){
				Runnable r=new Runnable() {
					public void run() {
					  logger.info("批量更新公卫信息开始");
				      ops.execute();
				      logger.info("批量更新公卫信息结束");
					}
				};
				r.run();
			}
		}
    };
    /**
     * 批量更新健康树信息
     * @param data
     * @param collection
     */
	void updateHealthTreeData(List<DBObject> data,DBCollection collection){
		if(!data.isEmpty()){
			BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,collection.getName());
		    boolean isHavaData=false;
			for(DBObject dbObject: data){
				Query query = new Query(Criteria.where("mpiid").is(dbObject.get("mpiid")));
				Update update = new Update();
				BasicDBObject basicDBObject= (BasicDBObject) dbObject.get("healthTree");
				Set<String> strings=basicDBObject.keySet();
				//update对象不为空    引入booleans标记记录是否有值
				boolean booleans=false;
				for(String string:strings){
					if(!StringUtil.isEmpty(string)){
						if(basicDBObject.get(string) instanceof List){
							List<Object> list=(List<Object>) basicDBObject.get(string);
							Object[] list1=list.toArray();
							if(!list.isEmpty()){
								isHavaData=true;
								booleans=true;
								update.pushAll("healthTree."+string, list1);
							}
						}
					}
				}
				if(booleans) ops.updateOne(query,update);
			}
			if(isHavaData){
				Runnable r=new Runnable() {
					public void run() {
						logger.info("批量更新健康树开始");
						ops.execute();
				        logger.info("批量更新健康树结束");
					}
				};
				r.run();
			}
		}
    };
}


