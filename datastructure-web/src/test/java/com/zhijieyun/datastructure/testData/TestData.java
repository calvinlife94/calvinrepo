/*package com.zhijieyun.datastructure.testData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.zhijieyun.datastructure.constants.Constants;
import com.zhijieyun.datastructure.service.HospitalizationService;
import com.zhijieyun.datastructure.service.OutpatientService;
import com.zhijieyun.datastructure.service.PublicHealthService;
import com.zhijieyun.datastructure.service.TemplateStructureService;
import com.zhijieyun.datastructure.util.JobCommonUtil;
import com.zhijieyun.datastructure.util.ZjyLogUtil;
import com.zhijieyun.zjylog.domain.entity.ZjyLog;
import com.zhijieyun.zjylog.service.IZjyLogService;

@SuppressWarnings({"serial","unused","resource","unchecked"})
public class TestData {
	private static  ExecutorService cachedThreadPool = Executors.newFixedThreadPool(50);  
	
	private static Logger logger = Logger.getLogger(TestData.class);
	@Autowired
	private HospitalizationService hospitalizationService;
	@Autowired
	private OutpatientService outpatientService;
	@Autowired
	private PublicHealthService publicHealthService;
	@Autowired
	private TemplateStructureService templateStructureService;
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Before
	public void init() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");
		hospitalizationService = (HospitalizationService) context.getBean("hospitalizationService");
		outpatientService = (OutpatientService) context.getBean("outpatientService");
		publicHealthService = (PublicHealthService) context.getBean("publicHealthService");
		templateStructureService = (TemplateStructureService) context.getBean("templateStructureService");
		mongoTemplate = (MongoTemplate) context.getBean("mongoTemplate");
		
	}
	@Test
	public void testMongoDB(){
		
		DBObject query= new BasicDBObject();
		query.put("mpiid", "101054");
		DBObject update= new BasicDBObject();
		Map map1=new HashMap<>();
		update.put("$addToSet", map1);
		Map map=new HashMap<>();
		map.put("test4", "value4");
				List list=new ArrayList<>();
		list.add(map);
		map1.put("show.infectiousDiseaseReportList", map);
		map1.put("show.infectiousDiseaseReportList2", map);
		collection.update(query, update);
		BulkOperations updateHealthTreeBulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "test2");
		List<Pair<Query, Update>> s=new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			
			Update update = new Update();
			update.addToSet("ok","show");
			update.addToSet("ok1","show1");
			s.add(Pair.of(query, update));
		}
		updateHealthTreeBulkOperations.insert(s);
		updateHealthTreeBulkOperations.execute();
		
		Query query = new Query(Criteria.where("m1").is(10));
		
		Query query1 = new Query(Criteria.where("m12").is(10));
		Update m2 = new Update();
		Object[] list=new Object[10];
		for(int i=0;i<10;i++){
			Map map=new HashMap<>();
			map.put(i+"", i+"");
			list[i]=(map);
		}
		
		m2.pushAll("show", list);
		m2.pushAll("show1", list);
		 // BulkMode.UNORDERED:表示并行处理，遇到错误时能继续执行不影响其他操作；BulkMode.ORDERED：表示顺序执行，遇到错误时会停止所有执行
		 BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "test");

		 ops.updateOne(query,m2);
		 
		 ops.updateOne(query1,m2);
		 
		 // 执行操作
		 ops.execute();

		System.out.println(mongoTemplate.count(new Query(), "items"));
	}
	
	@Test
	public void test(){
		Map<String, Object> map=new HashMap<>();
		map.put("reExtract", true);
		map.put("jobName", "checkingTempIdea"+"all");
		map.put("tempTableName", "tempTableName");
		try {
			templateStructureService.createTemplateStructure(map);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void shop(){

		try {
			long allTimeStart=System.currentTimeMillis();
			String jobParameter = "extractSize=5,iotype=all,tempTableName=tempTableName,reExtract=false";
			Map<String, Object> jobParamMap=JobCommonUtil.paramBuild(jobParameter);//参数构建
			int shardingTotalCount = 1;
			int shardingItem = 0;
			HashMap<String, Object > param = new HashMap<String, Object>(){{
			    put("jobName","test"+jobParamMap.get("iotype"));put("shardingItem",shardingItem);    
			}};
			Long lastMaxid = 11700L;
			
			*//**
			 * 构建查询参数
			 *//*
			Map<String, Object> map=new HashMap<>();
			map.put("tempTableName", jobParamMap.get("tempTableName"));
			map.put("extractSize", Integer.parseInt(jobParamMap.get("extractSize").toString()));
			map.put("shardingItem", shardingItem);
			map.put("shardingTotalCount", shardingTotalCount);
			map.put("maxid", lastMaxid);
			
			List<Map<String, Object>> data = templateStructureService.queryTemplateStructure(map);
			if(data.isEmpty()) {
				System.out.println("数据抽取完毕1");
			    System.exit(0);
			}//为空时结束数据抽取
			Long newmaxid=Long.valueOf(data.get(data.size()-1).get("rownumcp").toString());//次批量数据最大rownumcp
			Map<String, List<Integer>>  searchMap = JobCommonUtil.dataBuild(data);
			List<Callable<List<Map<String, Object>>>> callableList=new ArrayList<>();
			
			Map<String, Object> linkedData =new LinkedHashMap<String, Object>();
			if(searchMap.containsKey(Constants.PERSONALINFO)){//个人信息查询
					Callable<List<Map<String, Object>>> personalsBuildCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//个人基本信息构建
							long timeStart=System.currentTimeMillis();
							logger.info("个人信息查询开始!");
							List<Map<String, Object>> maps=publicHealthService.queryBatchPersonals(searchMap.get(Constants.PERSONALINFO));
							long timeEnd=System.currentTimeMillis();
							logger.info("个人信息查询结束!数据量为:"+searchMap.get(Constants.PERSONALINFO).size()+" 耗时:"+(timeEnd-timeStart));
							return maps;
						}
					};
					callableList.add(personalsBuildCallable);
					linkedData.put(Constants.PERSONALINFO, null);
			}
			if(searchMap.containsKey(Constants.OUTPATIENT)){//门诊信息
					Callable<List<Map<String, Object>>> outpatientsBuildCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//个人基本信息构建
							long timeStart=System.currentTimeMillis();
							logger.info("门诊信息查询开始!");
							List<Map<String, Object>>  maps=outpatientService.queryBatchOutpatients(searchMap.get(Constants.OUTPATIENT));
							long timeEnd=System.currentTimeMillis();
							logger.info("门诊信息查询结束!数据量为:"+searchMap.get(Constants.OUTPATIENT).size()+" 耗时:"+(timeEnd-timeStart));
							return maps;
						}
					};
					callableList.add(outpatientsBuildCallable);
					linkedData.put(Constants.OUTPATIENT, null);
			}
			if(searchMap.containsKey(Constants.HOSPITALIZATION)){//住院信息
					Callable<List<Map<String, Object>>> outpatientsBuildCallable=new Callable<List<Map<String, Object>>>() {
						@Override
						public List<Map<String, Object>> call() throws Exception {
							//个人基本信息构建
							long timeStart=System.currentTimeMillis();
							logger.info("住院信息查询开始!");
							List<Map<String, Object>>  maps=hospitalizationService.queryBatchHospitalizations(searchMap.get(Constants.HOSPITALIZATION));
							long timeEnd=System.currentTimeMillis();
							logger.info("住院信息查询结束!数据量为:"+searchMap.get(Constants.HOSPITALIZATION).size()+" 耗时:"+(timeEnd-timeStart));
							return maps;
						}
					};
					callableList.add(outpatientsBuildCallable);
					linkedData.put(Constants.HOSPITALIZATION, null);
			}
			
			
			List<Future<List<Map<String, Object>>>> futures=cachedThreadPool.invokeAll(callableList);
            int i=0;
            for(Entry<String, Object> entry:linkedData.entrySet()){
           	    linkedData.put(entry.getKey(), futures.get(i).get());
           	    i++;
            }
            if(searchMap.containsKey(Constants.PUBLICHEALTH)){//公卫信息(特殊处理)
            	linkedData.put(Constants.PUBLICHEALTH, null);
            	for(Entry<String, List<Integer>> entry:searchMap.entrySet()){
            		if(!Constants.HOSPITALIZATION.equals(entry.getKey())&&
            		   !Constants.OUTPATIENT.equals(entry.getKey())&&
            		   !Constants.PERSONALINFO.equals(entry.getKey())){
            			linkedData.put(entry.getKey(), entry.getValue());
            		}
            	}
			}
            List<Map<String, Object>> linkedDatas=new ArrayList<>();
            if(!linkedData.isEmpty()){
            	 if(linkedData.containsKey(Constants.PUBLICHEALTH)){
            		 linkedData.put(Constants.PUBLICHEALTHDATA,data); 
            	 }
            	 linkedData.put(Constants.MAXID,newmaxid);
            	 linkedDatas.add(linkedData);
            }else{
				System.out.println("数据抽取完毕2");
			    System.exit(0);//当数据为空时 结束数据抽取
            }
            long allTimeEnd=System.currentTimeMillis();
            logger.info("单次数据量:"+data.size()+"---总耗时:"+(allTimeEnd-allTimeStart));
            processData(linkedDatas);
		} catch (Exception e) {
			logger.error("处理数据时出错!",e);
		    System.exit(0);//结束数据抽取
		}
	}
	
	
	public void processData(List<Map<String, Object>> data) throws InterruptedException, ExecutionException{

		Map<String, Object>	dataHandle= data.get(0);
		int i=0;
		i=dataHandle.containsKey(Constants.PERSONALINFO)?i+1:i;
		i=dataHandle.containsKey(Constants.OUTPATIENT)?i+1:i;
		i=dataHandle.containsKey(Constants.HOSPITALIZATION)?i+1:i;
		i=dataHandle.containsKey(Constants.PUBLICHEALTH)?i+1:i;
		CountDownLatch countDownLatch = new CountDownLatch(i);
		//进行计数
	    if(dataHandle.containsKey(Constants.PERSONALINFO)){//个人信息构建
	  	      Runnable ab=	new Runnable() {
						public void run() {
							long timeStart=System.currentTimeMillis();
							logger.info("个人信息构建开始!");
							try {
								publicHealthService.personalInfoBuild((List<Map<String, Object>>)dataHandle.get(Constants.PERSONALINFO));
							} catch (Exception e) {
								e.printStackTrace();
							}
							long timeEnd=System.currentTimeMillis();
							logger.info("个人信息构建结束! 耗时:"+(timeEnd-timeStart));
							countDownLatch.countDown();
						}
					};
					 cachedThreadPool.submit(ab);
		}
	    if(dataHandle.containsKey(Constants.OUTPATIENT)){//批次构建门诊信息
  	      Runnable ab=	new Runnable() {
					public void run() {
						long timeStart=System.currentTimeMillis();
						logger.info("门诊信息构建开始!");
						Map<String, Object> param=new HashMap<>();
						param.put("ioType", "mz");
						param.put("data", dataHandle.get(Constants.OUTPATIENT));
						param.put("param", System.currentTimeMillis()+" as param");
						try {
							outpatientService.singleOutpatientRecordBuild((List<Map<String, Object>>)dataHandle.get(Constants.OUTPATIENT), param);
						} catch (Exception e) {
							logger.error("批次构建门诊信息失败!"+param,e);
						};
						long timeEnd=System.currentTimeMillis();
						logger.info("门诊信息构建结束! 耗时:"+(timeEnd-timeStart));
						countDownLatch.countDown();
					}
				};
				cachedThreadPool.execute(ab);
		}
	    if(dataHandle.containsKey(Constants.HOSPITALIZATION)){//批次构建住院信息
	    	      Runnable ab=	new Runnable() {
						public void run() {
							long timeStart=System.currentTimeMillis();
							logger.info("住院信息构建开始!");
							Map<String, Object> param=new HashMap<>();
							param.put("ioType", "zy");
							param.put("data", dataHandle.get(Constants.HOSPITALIZATION));
							param.put("param", System.currentTimeMillis()+" as param");
							try {
							hospitalizationService.singleInhosRecordBuild((List<Map<String, Object>>)dataHandle.get(Constants.HOSPITALIZATION), param);
							} catch (Exception e) {
								logger.error("批次构建住院信息失败!"+param,e);
							};
							long timeEnd=System.currentTimeMillis();
							logger.info("住院信息构建结束! 耗时:"+(timeEnd-timeStart));
							countDownLatch.countDown();
						}
					};
					 cachedThreadPool.execute(ab);
		}
	    if(dataHandle.containsKey(Constants.PUBLICHEALTH)){//批次构建公卫信息
  	                Runnable ab=	new Runnable() {
						public void run() {
							try {
								long timeStart=System.currentTimeMillis();
								publicHealthService.personalPublicHealthBuild(dataHandle);
								long timeEnd=System.currentTimeMillis();
								logger.info("公卫信息构建结束! 耗时:"+(timeEnd-timeStart));
							} catch (Exception e) {
								logger.error("批次构建公卫信息失败!"+dataHandle,e);
							}
							countDownLatch.countDown();
						}
					};
					cachedThreadPool.execute(ab);
		}
	    *//**
	     * 等等我!
	     *//*
	    try {
			countDownLatch.await();
			//等待完成后操作(保存此批次最大处理的rowid)
			try {
				//String jobParameter = shardingContext.getJobParameter();
				//Map<String, Object> jobParamMap=JobCommonUtil.paramBuild(jobParameter);//参数构建
				//templateStructureService.saveBatchMaxid(Long.valueOf(dataHandle.get(Constants.MAXID).toString()), shardingContext.getJobName()+jobParamMap.get("iotype"), shardingContext.getShardingItem());
			} catch (Exception e) {
				logger.error("保存此批次最大数据处理",e);
			}
		} catch (InterruptedException e) {
			logger.error("同步异常!",e);
		}
	}
	
	*//**
     * 判断对象中属性值是否全为空
     *
     * @param object
     * @return
     *//*
    public static boolean checkObjAllFieldsIsNull(Object object) {
        if (null == object) {
            return true;
        }
 
        try {
            for (Field f : object.getClass().getDeclaredFields()) {
                f.setAccessible(true);
 
                System.out.print(f.getName() + ":");
                System.out.println(f.get(object));
 
                if (f.get(object) != null && StringUtils.isNotBlank(f.get(object).toString())) {
                    return false;
                }
 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return true;
    }
}




*/