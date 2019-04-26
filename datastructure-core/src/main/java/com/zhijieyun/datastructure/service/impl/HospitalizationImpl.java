package com.zhijieyun.datastructure.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.zhijieyun.datastructure.dao.mapper.CommonTempMapper;
import com.zhijieyun.datastructure.dao.mapper.HospitalizationMapper;
import com.zhijieyun.datastructure.service.HospitalizationService;
import com.zhijieyun.datastructure.util.MapUtils;

import net.sf.json.JSONObject;

@Service("hospitalizationService")
@SuppressWarnings({"rawtypes","unchecked"})
public class HospitalizationImpl implements HospitalizationService{
	private static Logger logger = Logger.getLogger(HospitalizationImpl.class);
	
	private static  ExecutorService cachedThreadPool = Executors.newFixedThreadPool(50);  
	
	@Autowired
	public	HospitalizationMapper hospitalizationMapper;
	
	@Autowired
	public	CommonTempMapper commonTempMapper;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<Map<String, Object>> queryBatchHospitalizations(List<Integer> list)throws Exception {
		return hospitalizationMapper.queryBatchHospitalizations(list);
	}

	
	@Override
	public List<Map<String, Object>> queryOutHospitalRecordsByMap(Map map) throws Exception{
		return hospitalizationMapper.queryOutHospitalRecordsByMap(map);
	}
	
	@Override
	public void singleInhosRecordBuild(List<Map<String, Object>> outpatientParam,Map paramMap)throws Exception {
			DBCollection coll_inhos = mongoTemplate.getCollection("singleInhosRecord");
			DBCollection coll_recentRecords = mongoTemplate.getCollection("recentRecords");//最近就诊
			DBCollection coll_exams = mongoTemplate.getCollection("exams");//检查
			DBCollection coll_inspects = mongoTemplate.getCollection("inspects");//检验
			DBCollection coll_orders = mongoTemplate.getCollection("orders");//医嘱
			DBCollection coll_inHosDiagnosis = mongoTemplate.getCollection("inHosDiagnosis");//住院诊断
			DBCollection coll_operations = mongoTemplate.getCollection("operations");//手术
			Map buildMpiidData=buildMpiidData(outpatientParam);
			/*1住院处方*/
			Callable<Map<String, List<Map>>> homePageBuildCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					//住院处方
					logger.info("开始构建住院数据-病案首页");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> homePageMapList= hospitalizationMapper.getHomePage(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建住院数据-病案首页"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入住院数据-病案首页至mongo"+"数据大小"+homePageMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> homePageMap=changeDataPattern(homePageMapList,null,null);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入住院数据-病案首页至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return homePageMap;
				}
			};
			/*2入院记录*/
			Callable<Map<String, List<Map>>> admissionRecordCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					//入院记录
					logger.info("开始构建住院数据-入院记录");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> admissionRecordMapList=hospitalizationMapper.queryAdmissionRecord(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建住院数据-入院记录"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入住院数据-入院记录至mongo"+"数据大小"+admissionRecordMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> admissionRecordMap=changeDataPattern(admissionRecordMapList,null,null);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入住院数据-入院记录至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return admissionRecordMap;
				}
			};
			/*3住院诊断*/
			Callable<Map<String, List<Map>>> inHosDiagnoseListJsonBuildCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					//住院诊断
					logger.info("开始构建住院数据-住院诊断");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> inHosDiagnoseMapList=hospitalizationMapper.queryInHosDiagnose(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建住院数据-住院诊断"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入住院数据-住院诊断至mongo"+"数据大小"+inHosDiagnoseMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> inHosDiagnoseMap=changeDataPattern(inHosDiagnoseMapList,coll_inHosDiagnosis,buildMpiidData);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入住院数据-住院诊断至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return inHosDiagnoseMap;
				}
			};
			/*4医嘱*/
			Callable<Map<String, List<Map>>> ordersRecordBuildCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					// 医嘱
					logger.info("开始构建住院数据-医嘱");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> ordersRecordMapList =hospitalizationMapper.getOrdersByMap(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建住院数据-医嘱"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入住院数据-医嘱至mongo"+"数据大小"+ordersRecordMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> ordersRecordMap=changeDataPattern(ordersRecordMapList,coll_orders,buildMpiidData);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入住院数据-医嘱至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return ordersRecordMap;
				}
			};
			/*5住院费用*/
			Callable<Map<String, List<Map>>> costDetailExamListBuildCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					// 住院费用
					logger.info("开始构建住院数据-住院费用");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> costDetailExamMapList=hospitalizationMapper.getInhostCostByMap(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建住院数据-住院费用"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入住院数据-住院费用至mongo"+"数据大小"+costDetailExamMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> costDetailExamMap=changeDataPattern(costDetailExamMapList,null,null);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入住院数据-住院费用至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return costDetailExamMap;
				}
			};
			/*6住院检查*/
			Callable<Map<String, List<Map>>> inExamCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					// 住院检查
					logger.info("开始构建住院数据-住院检查");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> inExamMapList=commonTempMapper.getCommonExamByMap(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建住院数据-住院检查"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入住院数据-住院检查至mongo"+"数据大小"+inExamMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> inExamMap=changeDataPattern(inExamMapList,coll_exams,buildMpiidData);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入住院数据-住院检查至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return inExamMap;
				}
			};
			
			/*7住院检验*/
			Callable<Map<String, List<Map>>> inInspectCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					// 住院检验
					logger.info("开始构建住院数据-住院检验");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> inInspectMapList=commonTempMapper.getCommonInspectByFormMap(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建住院数据-住院检验"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入住院数据-住院检验至mongo"+"数据大小"+inInspectMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> inInspectMap=changeDataPattern(inInspectMapList,coll_inspects,buildMpiidData);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入住院数据-住院检验至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return inInspectMap;
				}
			};
			/*8住院手术*/
			Callable<Map<String, List<Map>>> inOperationCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					// 住院手术
					logger.info("开始构建住院数据-住院手术");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> inSurgeryRecordMapList=commonTempMapper.queryCommonSurgeryRecord(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建住院数据-住院手术"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入住院数据-住院手术至mongo"+"数据大小"+inSurgeryRecordMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> inSurgeryRecordMap=changeDataPattern(inSurgeryRecordMapList,coll_operations,buildMpiidData);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入住院数据-住院手术至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return inSurgeryRecordMap;
				}
			};
			
			List<Callable<Map<String, List<Map>>>> callableList=new ArrayList<>();
			callableList.add(homePageBuildCallable);/*1住院处方*/
			callableList.add(admissionRecordCallable);/*2入院记录*/
			callableList.add(inHosDiagnoseListJsonBuildCallable);/*3住院诊断*/
			callableList.add(ordersRecordBuildCallable);/*4医嘱*/
			callableList.add(costDetailExamListBuildCallable);/*5住院费用*/
			callableList.add(inExamCallable);/*6住院检查*/
			callableList.add(inInspectCallable);/*7住院检验*/
			callableList.add(inOperationCallable);/*8住院手术*/
			List<Future<Map<String, List<Map>>>> futures=cachedThreadPool.invokeAll(callableList);
			Map<String, List<Map>> inHomePageByMap= futures.get(0).get(15, TimeUnit.SECONDS);//1
			Map<String, List<Map>> inAdmissionRecordMap= futures.get(1).get(15, TimeUnit.SECONDS);//2
			Map<String, List<Map>> inHosDiagnoseListMap= futures.get(2).get(15, TimeUnit.SECONDS);//3
			Map<String, List<Map>> inOrdersRecordMap= futures.get(3).get(15, TimeUnit.SECONDS);//4
			Map<String, List<Map>> inCostDetailExamMap= futures.get(4).get(15, TimeUnit.SECONDS);//5
			Map<String, List<Map>> inExamMap= futures.get(5).get(15, TimeUnit.SECONDS);//6
			Map<String, List<Map>> inInspectMap= futures.get(6).get(15, TimeUnit.SECONDS);//7
			Map<String, List<Map>> inOperationMap= futures.get(7).get(15, TimeUnit.SECONDS);//8

			List<DBObject> dbObjList = new ArrayList<DBObject>();
			List<DBObject> dbRecentRecordList = new ArrayList<DBObject>();
			logger.info("主表数据量"+outpatientParam.size());
			for(int i=0;i<outpatientParam.size();i++){
				Map map=outpatientParam.get(i);
				Map<String, Object> singleInhosRecord = new ConcurrentHashMap<>();
				MapUtils.mapExcludeNull(map);
				singleInhosRecord.put("mpiid", map.get("mpiid"));
				singleInhosRecord.put("idCard", map.get("idcardNum"));
				singleInhosRecord.put("name", map.get("name"));
				singleInhosRecord.put("sexCode", map.get("sexCode"));
				singleInhosRecord.put("serviceTime", map.get("serviceTime"));
				singleInhosRecord.put("disChargeDate", map.get("dischargeDate"));
				singleInhosRecord.put("orgName", map.get("orgName"));
				singleInhosRecord.put("doctorName", map.get("doctorName"));
				singleInhosRecord.put("deptName", map.get("deptName"));
				singleInhosRecord.put("deptCode", map.get("deptCode"));
				singleInhosRecord.put("zyId", map.get("zyId"));
				singleInhosRecord.put("orgCode", map.get("orgCode"));
				String dataGetParam= "op"+map.get("zyId")+map.get("orgCode");
				List<Map> inCostDetail=mapData4Null(inCostDetailExamMap.get(dataGetParam));
				List<Map> inHosDiagnose=mapData4Null(inHosDiagnoseListMap.get(dataGetParam));
				singleInhosRecord.put("medicalRecordHomePageList",mapData4Null(inHomePageByMap.get(dataGetParam)));;/*1住院处方*/
				singleInhosRecord.put("admissionRecordList",mapData4Null(inAdmissionRecordMap.get(dataGetParam)));/*2入院记录*/
				singleInhosRecord.put("inHosDiagnosisList",inHosDiagnose);/*3住院诊断*/
				singleInhosRecord.put("orderList",mapData4Null(inOrdersRecordMap.get(dataGetParam)));/*4医嘱*/
				singleInhosRecord.put("inHosFeeList",inCostDetail);/*5住院费用*/
				singleInhosRecord.put("operationList",mapData4Null(inOperationMap.get(dataGetParam)));/*6住院检查*/
				singleInhosRecord.put("inspectList",mapData4Null(inInspectMap.get(dataGetParam)));/*7住院检验*/
				singleInhosRecord.put("examList",mapData4Null(inExamMap.get(dataGetParam)));/*8住院手术*/
				if(map.get("pedId")!=null&&!"".equals(map.get("pedId"))){/*正常数据都有出院小结*/ //特殊数据特殊处理
					singleInhosRecord.put("dischargeList", map);//出院小结
				}
				dbRecentRecordList.add(recentRecordsHandle(map, inCostDetail, inHosDiagnose));
				dbObjList.add(BasicDBObject.parse(JSONObject.fromObject(singleInhosRecord).toString()));
			}
			logger.info("开始插入最近就诊数据至mongo"+"最近就诊数据大小"+dbObjList.size());
			insertData(dbRecentRecordList, coll_recentRecords);
			logger.info("结束插入最近就诊数据至mongo");
			logger.info("开始插入住院数据主表至mongo"+"主表数据大小"+dbObjList.size());
			insertData(dbObjList,coll_inhos);
			logger.info("结束插入住院数据主表至mongo");
	};
	/**
	 * 构建key mpiid
	 * @param outpatientParam
	 * @return
	 */
	private Map buildMpiidData(List<Map<String, Object>> outpatientParam){
		Map currentMap=new ConcurrentHashMap<>();
		for(int i=0;i<outpatientParam.size();i++){
			Map map=outpatientParam.get(i);
			String dataGetParam= "op"+map.get("zyId")+map.get("orgCode");
			String mpiid=map.get("mpiid")==null?"":map.get("mpiid").toString();
			currentMap.put(dataGetParam, mpiid);
		}
		return currentMap;
	}

	/**
	 * 最近就诊内存构建处理
	 */
	public BasicDBObject recentRecordsHandle(Map map,List<Map> outpatientFee,List<Map> outpatientVisit){
		Map recentRecord = new HashMap<String, Object>();
		if (outpatientVisit.size() > 0) {
			StringBuffer strBuff = new StringBuffer();
			for (Map visitObj : outpatientVisit) {
				strBuff.append(visitObj.get("diagnosisName")).append(",");
			}
			strBuff.deleteCharAt(strBuff.length() - 1);
			recentRecord.put("diagnosisName", strBuff.toString());
		}
		recentRecord.put("patientSource", "住院");
		recentRecord.put("orgCode", map.get("orgCode"));
		recentRecord.put("orgName", map.get("orgName"));
		recentRecord.put("regTime", map.get("serviceTime"));
		recentRecord.put("deptCode", map.get("deptCode"));
		recentRecord.put("deptName", map.get("deptName"));
		recentRecord.put("doctorName", map.get("doctorName"));
		recentRecord.put("outpatientNum", "");
		recentRecord.put("inpatientNum", map.get("inpatientNum"));
		if (outpatientFee.size() > 0) {
			recentRecord.put("settlementAmount", outpatientFee.get(0).get("settlementAmount"));
		}
		MapUtils.mapExcludeNull(recentRecord);
		return new BasicDBObject("mpiid",map.get("mpiid")).append("record",BasicDBObject.parse(JSONObject.fromObject(recentRecord).toString()));
	}
	/**
	 * 无法改变数据后，进一步解决下mpiid不存在问题
	 */
	
	/**
	 * 改变数据展示模式获取方便 并保存数据
	 */

	Map<String, List<Map>> changeDataPattern(List<Map<String, Object>> list,DBCollection collection,Map buildMpiidData){
		MapUtils.mapExcludeNullRecursion(list);
		Map<String, List<Map>> dataMapTemp=new HashMap();
		List<DBObject> dbObjList = new ArrayList<DBObject>();
		for(Map recipe :list){
			String dataGetParam="op"+recipe.get("inpatientNum")+recipe.get("orgCode");
			if(dataMapTemp.containsKey(dataGetParam)){
				dataMapTemp.get(dataGetParam).add(recipe);
			}else{
				List value=new LinkedList<>();
				value.add(recipe);
				dataMapTemp.put(dataGetParam, value);
			}
			if(buildMpiidData!=null&&buildMpiidData.get(dataGetParam)!=null){
				dbObjList.add(new BasicDBObject("mpiid",buildMpiidData.get(dataGetParam)).
				         append("orgCode", recipe.get("orgCode")).
				         append("zyId", recipe.get("inpatientNum")).
				         append("mzId", "").
				         append("record", BasicDBObject.parse(JSONObject.fromObject(recipe).toString()))
		         );
			}

		}
		insertData(dbObjList,collection);
		return dataMapTemp;
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
	 * 当值为空时返回空数组
	 */
	List mapData4Null(Object object){
		return  object!=null?(List) object:new LinkedList();
	}
}
