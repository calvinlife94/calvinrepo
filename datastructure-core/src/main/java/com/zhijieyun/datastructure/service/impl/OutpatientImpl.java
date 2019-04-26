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
import com.zhijieyun.datastructure.dao.mapper.OutpatientMapper;
import com.zhijieyun.datastructure.service.OutpatientService;
import com.zhijieyun.datastructure.util.MapUtils;

import net.sf.json.JSONObject;

@Service("outpatientService")
@SuppressWarnings({"rawtypes","unchecked"})
public class OutpatientImpl implements OutpatientService{
	private static Logger logger = Logger.getLogger(OutpatientImpl.class);
	
	private static  ExecutorService cachedThreadPool = Executors.newFixedThreadPool(50);  
	
	@Autowired
	public	OutpatientMapper outpatientMapper;
	
	@Autowired
	public	CommonTempMapper commonTempMapper;
	
	@Autowired
	private MongoTemplate mongoTemplate;



	@Override
	public List<Map<String, Object>> queryBatchOutpatients(List<Integer> list)throws Exception {
		return outpatientMapper.queryBatchOutpatients(list);
	}
	
	public List<Map<String, Object>> getOpRegisterByMap(Map map)throws Exception{
		return outpatientMapper.getOpRegisterByMap(map);
	}
	
	
	@Override
	public void singleOutpatientRecordBuild(List<Map<String, Object>> outpatientParam,Map paramMap) throws Exception{
			DBCollection coll_outpatient = mongoTemplate.getCollection("singleOutpatientRecord");
			DBCollection coll_recentRecords = mongoTemplate.getCollection("recentRecords");
			DBCollection coll_exams = mongoTemplate.getCollection("exams");
			DBCollection coll_inspects = mongoTemplate.getCollection("inspects");
			DBCollection coll_recipes = mongoTemplate.getCollection("recipes");
			DBCollection coll_outpatientVisits = mongoTemplate.getCollection("outpatientVisits");
			DBCollection coll_operations = mongoTemplate.getCollection("operations");
			Map buildMpiidData=buildMpiidData(outpatientParam);
			Callable<Map<String, List<Map>>> opRecipeCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					//门诊处方
					logger.info("开始构建门诊数据-门诊处方");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> opRecipeByMapList= outpatientMapper.getOpRecipeByMap(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建门诊数据-门诊处方"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入门诊数据-门诊处方至mongo"+"数据大小"+opRecipeByMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> opRecipeByMap=changeDataPattern(opRecipeByMapList,coll_recipes,buildMpiidData);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入门诊数据-门诊处方至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return opRecipeByMap;
				}
			};
			Callable<Map<String, List<Map>>> opChargeCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					//门诊费用
					logger.info("开始构建门诊数据-门诊费用");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> opChargeByMapList=outpatientMapper.getOpChargeByMap(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建门诊数据-门诊费用"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入门诊数据-门诊费用至mongo"+"数据大小"+opChargeByMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> opChargeByMap=changeDataPattern(opChargeByMapList,null,null);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入门诊数据-门诊费用至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return opChargeByMap;
				}
			};
			
			Callable<Map<String, List<Map>>> outPatientRecordCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					//门诊就诊
					logger.info("开始构建门诊数据-门诊就诊");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> outPatientRecordMapList=outpatientMapper.queryOutPatientRecord(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建门诊数据-门诊就诊"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入门诊数据-门诊就诊至mongo"+"数据大小"+outPatientRecordMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> outPatientRecordMap=changeDataPattern(outPatientRecordMapList,coll_outpatientVisits,buildMpiidData);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入门诊数据-门诊就诊至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return outPatientRecordMap;
				}
			};
			
			Callable<Map<String, List<Map>>> opEmroEmeCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					// 门（急）诊病历
					logger.info("开始构建门诊数据-门（急）诊病历");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> opEmroEmeByMapList=outpatientMapper.getOpEmroEmeByMap(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建门诊数据-门（急）诊病历"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入门诊数据-门（急）诊病历至mongo"+"数据大小"+opEmroEmeByMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> opEmroEmeByMap=changeDataPattern(opEmroEmeByMapList,null,null);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入门诊数据-门（急）诊病历至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return opEmroEmeByMap;
				}
			};
			
			Callable<Map<String, List<Map>>> opExamCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					// 门诊检查
					logger.info("开始构建门诊数据-门诊检查");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> opExamMapList=commonTempMapper.getCommonExamByMap(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建门诊数据-门诊检查"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入门诊数据-门诊检查至mongo"+"数据大小"+opExamMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> opExamMap=changeDataPattern(opExamMapList,coll_exams,buildMpiidData);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入门诊数据-门诊检查至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return opExamMap;
				}
			};
			
			Callable<Map<String, List<Map>>> opInspectCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					// 门诊检验
					logger.info("开始构建门诊数据-门诊检验");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> opInspectMapList=commonTempMapper.getCommonInspectByFormMap(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建门诊数据-门诊检验"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入门诊数据-门诊检验至mongo"+"数据大小"+opInspectMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> opInspectMap=changeDataPattern(opInspectMapList,coll_inspects,buildMpiidData);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入门诊数据-门诊检验至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return opInspectMap;
				}
			};
			
			Callable<Map<String, List<Map>>> outOperationCallable=new Callable<Map<String, List<Map>>>() {
				@Override
				public Map<String, List<Map>> call() throws Exception {
					
					// 门诊手术
					logger.info("开始构建门诊数据-门诊手术");
					long timeStart=System.currentTimeMillis();
					List<Map<String, Object>> opSurgeryRecordMapList=commonTempMapper.queryCommonSurgeryRecord(paramMap);
					long timeEnd=System.currentTimeMillis();
					logger.info("结束构建门诊数据-门诊手术"+"耗时:"+(timeEnd-timeStart));
					logger.info("开始插入门诊数据-门诊手术至mongo"+"数据大小"+opSurgeryRecordMapList.size());
					long timeInStart=System.currentTimeMillis();
					Map<String, List<Map>> opSurgeryRecordMap=changeDataPattern(opSurgeryRecordMapList,coll_operations,buildMpiidData);
					long timeInEnd=System.currentTimeMillis();
					logger.info("结束插入门诊数据-门诊手术至mongo"+"耗时:"+(timeInEnd-timeInStart));
					
					return opSurgeryRecordMap;
				}
			};
			List<Callable<Map<String, List<Map>>>> callableList=new ArrayList<>();
			callableList.add(opRecipeCallable);//1
			callableList.add(opChargeCallable);//2
			callableList.add(outPatientRecordCallable);//3
			callableList.add(opEmroEmeCallable);//4
			callableList.add(opExamCallable);//5
			callableList.add(opInspectCallable);//6
			callableList.add(outOperationCallable);//7
			List<Future<Map<String, List<Map>>>> futures=cachedThreadPool.invokeAll(callableList);

			Map<String, List<Map>> opRecipeByMap= futures.get(0).get(15, TimeUnit.SECONDS);//1  
			Map<String, List<Map>> opChargeByMap= futures.get(1).get(15, TimeUnit.SECONDS);//2
			Map<String, List<Map>> outPatientRecordMap= futures.get(2).get(15, TimeUnit.SECONDS);//3
			Map<String, List<Map>> opEmroEmeByMap= futures.get(3).get(15, TimeUnit.SECONDS);//4
			Map<String, List<Map>> opExamMap= futures.get(4).get(15, TimeUnit.SECONDS);//5
			Map<String, List<Map>> opInspectMap= futures.get(5).get(15, TimeUnit.SECONDS);//6
			Map<String, List<Map>> opSurgeryRecordMap= futures.get(6).get(15, TimeUnit.SECONDS);//7

			List<DBObject> dbObjList = new ArrayList<DBObject>();
			List<DBObject> dbRecentRecordList = new ArrayList<DBObject>();
			logger.info("主表数据量"+outpatientParam.size());
			for(int i=0;i<outpatientParam.size();i++){
				Map map=outpatientParam.get(i);
				String dataGetParam= "op"+map.get("mzId")+map.get("orgCode");
				MapUtils.mapExcludeNull(map);
				List<Map> outpatientFee=mapData4Null(opChargeByMap.get(dataGetParam));
				List<Map> outpatientVisit=mapData4Null(outPatientRecordMap.get(dataGetParam));
				map.put("outpatientFeeList",outpatientFee);//1
				map.put("outpatientVisitList",outpatientVisit);//2
				map.put("examList",mapData4Null(opExamMap.get(dataGetParam)));//3
				map.put("inspectList",mapData4Null(opInspectMap.get(dataGetParam)));//4
				map.put("recipeList",mapData4Null(opRecipeByMap.get(dataGetParam)));//5
				map.put("outpatientMedicalRecordList",mapData4Null(opEmroEmeByMap.get(dataGetParam)));//6
				map.put("operationList",mapData4Null(opSurgeryRecordMap.get(dataGetParam)));//7
				dbRecentRecordList.add(recentRecordsHandle(map, outpatientFee, outpatientVisit));
				dbObjList.add(BasicDBObject.parse(JSONObject.fromObject(map).toString()));
			}
			logger.info("开始插入最近就诊数据至mongo"+"最近就诊数据大小"+dbObjList.size());
			insertData(dbRecentRecordList, coll_recentRecords);
			logger.info("结束插入最近就诊数据至mongo");
			logger.info("开始插入门诊数据主表至mongo"+"主表数据大小"+dbObjList.size());
			insertData(dbObjList,coll_outpatient);
			logger.info("结束插入门诊数据主表至mongo");
		
	};
	/**
	 * 最近就诊内存构建处理
	 */
	public BasicDBObject recentRecordsHandle(Map map,List<Map> outpatientFee,List<Map> outpatientVisit){
		Map recentRecord = new HashMap<String, Object>();
		if (outpatientVisit.size() > 0) {
			StringBuffer strBuff = new StringBuffer();
			for (Map visitObj : outpatientVisit) {
				strBuff.append(visitObj.get("adminIllnessName")).append(",");
			}
			strBuff.deleteCharAt(strBuff.length() - 1);
			recentRecord.put("diagnosisName", strBuff.toString());
		}
		recentRecord.put("patientSource", "门诊");
		recentRecord.put("orgCode", map.get("orgCode"));
		recentRecord.put("orgName", map.get("orgName"));
		recentRecord.put("regTime", map.get("serviceTime"));
		recentRecord.put("deptCode", map.get("deptCode"));
		recentRecord.put("deptName", map.get("deptName"));//
		recentRecord.put("doctorName", map.get("doctorName"));
		recentRecord.put("outpatientNum", map.get("mzId"));
		recentRecord.put("inpatientNum", "");
		if (outpatientFee.size() > 0) {
			recentRecord.put("settlementAmount", outpatientFee.get(0).get("settlementAmount"));
		}
		MapUtils.mapExcludeNull(recentRecord);
		return new BasicDBObject("mpiid",map.get("mpiid")).append("record",BasicDBObject.parse(JSONObject.fromObject(recentRecord).toString()));
	}
	
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
	 * 改变数据展示模式获取方便 并保存数据
	 */

	Map<String, List<Map>> changeDataPattern(List<Map<String, Object>> list,DBCollection collection,Map buildMpiidData){
		MapUtils.mapExcludeNullRecursion(list);
		Map<String, List<Map>> dataMapTemp=new HashMap();
		List<DBObject> dbObjList = new ArrayList<DBObject>();
		for(Map recipe :list){
			String dataGetParam="op"+recipe.get("mzId")+recipe.get("orgCode");
			if(dataMapTemp.containsKey(dataGetParam)){
				dataMapTemp.get(dataGetParam).add(recipe);
			}else{
				List value=new LinkedList<>();
				value.add(recipe);
				dataMapTemp.put(dataGetParam, value);
			}
			if(buildMpiidData!=null&&buildMpiidData.get(dataGetParam) != null){
				dbObjList.add(new BasicDBObject("mpiid",buildMpiidData.get(dataGetParam)).
				         append("orgCode", recipe.get("orgCode")).
				         append("zyId", "").
				         append("mzId", recipe.get("mzId")).
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
