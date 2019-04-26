package com.zhijieyun.datastructure.elasticJobLite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhijieyun.datastructure.constants.Constants;
import com.zhijieyun.datastructure.service.CommonTempService;
import com.zhijieyun.datastructure.service.HospitalizationService;
import com.zhijieyun.datastructure.service.OutpatientService;
import com.zhijieyun.datastructure.service.PublicHealthService;
import com.zhijieyun.datastructure.service.TemplateStructureService;
import com.zhijieyun.datastructure.util.JobCommonUtil;
import com.zhijieyun.datastructure.util.ZjyLogUtil;
import com.zhijieyun.zjylog.service.IZjyLogService;

import io.elasticjob.lite.api.ShardingContext;
import io.elasticjob.lite.api.dataflow.DataflowJob;

/**
 * 
 * @author ZWJ
 * 启动入口
 * 根据不同参数构建不同类型
 */
@SuppressWarnings({"serial","unchecked"})
public class DataStructure implements DataflowJob<Map<String, Object>>{
	private static Logger logger = Logger.getLogger(DataStructure.class);
	
	private static  ExecutorService cachedThreadPool = Executors.newFixedThreadPool(10);  
	
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	private HospitalizationService hospitalizationService;
	@Autowired
	private OutpatientService outpatientService;
	@Autowired
	private PublicHealthService publicHealthService;
	@Autowired
	private TemplateStructureService templateStructureService;
	@Autowired
	private	CommonTempService commonTempService;
	@Autowired
	private IZjyLogService zjyLogService;

	
	@Override
	public List<Map<String, Object>> fetchData(ShardingContext shardingContext) {
		try {
			long allTimeStart=System.currentTimeMillis();
			String jobParameter = shardingContext.getJobParameter();
			Map<String, Object> jobParamMap=JobCommonUtil.paramBuild(jobParameter);//参数构建
			int shardingTotalCount = shardingContext.getShardingTotalCount();
			int shardingItem = shardingContext.getShardingItem();
			HashMap<String, Object > param = new HashMap<String, Object>(){{
			    put("jobName",shardingContext.getJobName()+jobParamMap.get("iotype"));put("shardingItem",shardingItem);    
			}};
			Long lastMaxid = commonTempService.queryLastBatchMaxIdByJobName(param);
			
			/**
			 * 构建查询参数
			 */
			Map<String, Object> map=new HashMap<>();
			map.put("tempTableName", jobParamMap.get("tempTableName"));
			map.put("extractSize", Integer.parseInt(jobParamMap.get("extractSize").toString()));
			map.put("shardingItem", shardingItem);
			map.put("shardingTotalCount", shardingTotalCount);
			map.put("maxid", lastMaxid);
			
			List<Map<String, Object>> data = templateStructureService.queryTemplateStructure(map);
			if(data.isEmpty()) return null;//为空时结束数据抽取
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
					/*标记是否需要构建个人信息的作用*/
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
					/*标记是否需要构建门诊信息的作用*/
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
					/*标记是否需要构建住院信息的作用*/
					linkedData.put(Constants.HOSPITALIZATION, null);
			}
			List<Future<List<Map<String, Object>>>> futures=cachedThreadPool.invokeAll(callableList);
            int i=0;
            for(Entry<String, Object> entry:linkedData.entrySet()){
            	/*对标记变量  进行覆盖*/
           	    linkedData.put(entry.getKey(), futures.get(i).get());
           	    i++;
            }
            if(searchMap.containsKey(Constants.PUBLICHEALTH)||searchMap.containsKey(Constants.HEALTHTREE)){//公卫或健康树信息(特殊处理)
            	//标记健康树或公卫需要处理
            	linkedData.put(Constants.PUBLICHEALTH, null);
            	for(Entry<String, List<Integer>> entry:searchMap.entrySet()){
            		if(!Constants.HOSPITALIZATION.equals(entry.getKey())&&
            		   !Constants.OUTPATIENT.equals(entry.getKey())&&
            		   !Constants.PERSONALINFO.equals(entry.getKey())&&
            		   !Constants.HEALTHTREE.equals(entry.getKey())){
            			linkedData.put(entry.getKey(), entry.getValue());
            		}
            	}
			}
            /*因为数据返回需要List,进行再次封装*/
            List<Map<String, Object>> linkedDatas=new ArrayList<>();
            if(!linkedData.isEmpty()){
            	 if(linkedData.containsKey(Constants.PUBLICHEALTH)){
            		 linkedData.put(Constants.PUBLICHEALTHDATA,data); 
            	 }
            	 linkedData.put(Constants.MAXID,newmaxid);
            	 linkedDatas.add(linkedData);
            }else{
            	return null;//当数据为空时 结束数据抽取
            }
            long allTimeEnd=System.currentTimeMillis();
            logger.info("单次数据量:"+data.size()+"---总耗时:"+(allTimeEnd-allTimeStart));
            return linkedDatas;
		} catch (Exception e) {
			logger.error("处理数据时出错!",e);
			return null;//结束数据抽取
		}
	}

	@Override
	public void processData(ShardingContext shardingContext, List<Map<String, Object>> data) {
        /*对封装数据进行取值*/
		Map<String, Object>	dataHandle= data.get(0);
		int i=0;
		i=dataHandle.containsKey(Constants.PERSONALINFO)?i+1:i;
		i=dataHandle.containsKey(Constants.OUTPATIENT)?i+1:i;
		i=dataHandle.containsKey(Constants.HOSPITALIZATION)?i+1:i;
		i=dataHandle.containsKey(Constants.PUBLICHEALTH)?i+1:i;
		//进行计数
		CountDownLatch countDownLatch = new CountDownLatch(i);
	    if(dataHandle.containsKey(Constants.PERSONALINFO)){//个人信息构建
	  	            Runnable abpersonalInfoBuild=	new Runnable() {
						public void run() {
							long timeStart=System.currentTimeMillis();
							logger.info("个人信息构建开始!");
							try {
								publicHealthService.personalInfoBuild((List<Map<String, Object>>)dataHandle.get(Constants.PERSONALINFO));
								Map<String, Object> jobParam = new HashMap<String, Object>();
								jobParam.put("frequency", 0);
								jobParam.put("jobname", shardingContext.getJobName());
								jobParam.put("shardingItem", shardingContext.getShardingItem());
								jobParam.put("successful_number",((List<Map<String, Object>>)dataHandle.get(Constants.PERSONALINFO)).size());
								jobParam.put("updateTime", df.format(new Date()));
								ZjyLogUtil.saveJobRecordNumShardingItem(jobParam);
							} catch (Exception e) {
								//保存出错数据
								Map<String, Object> myExceptionMap=new HashMap<>();
								myExceptionMap.put("iotype", Constants.PERSONALINFO);
								myExceptionMap.put("param", System.currentTimeMillis()+" as param");
								myExceptionMap.put("data", JobCommonUtil.getSerializedBytes(dataHandle.get(Constants.PERSONALINFO)));	
								myExceptionMap.put("exception", e.toString());
								try {
									zjyLogService.saveJobAbnormalExtraction(myExceptionMap);
								} catch (Exception e2) {
									logger.error("异常信息保存失败",e2);
								}
								logger.error("个人信息构建失败!",e);
							}
							long timeEnd=System.currentTimeMillis();
							logger.info("个人信息构建结束! 耗时:"+(timeEnd-timeStart));
							countDownLatch.countDown();
						}
					};
					 cachedThreadPool.execute(abpersonalInfoBuild);
		}
	    if(dataHandle.containsKey(Constants.OUTPATIENT)){//批次构建门诊信息
  	           Runnable absingleOutpatientRecordBuild=	new Runnable() {
					public void run() {
						long timeStart=System.currentTimeMillis();
						logger.info("门诊信息构建开始!");
						Map<String, Object> param=new HashMap<>();
						param.put("ioType", "mz");
						param.put("data", dataHandle.get(Constants.OUTPATIENT));
						param.put("param", System.currentTimeMillis()+" as param");
						try {
							outpatientService.singleOutpatientRecordBuild((List<Map<String, Object>>)dataHandle.get(Constants.OUTPATIENT), param);
							Map<String, Object> jobParam = new HashMap<String, Object>();
							jobParam.put("frequency", 0);
							jobParam.put("jobname", shardingContext.getJobName());
							jobParam.put("shardingItem", shardingContext.getShardingItem());
							jobParam.put("successful_number",((List<Map<String, Object>>)dataHandle.get(Constants.OUTPATIENT)).size());
							jobParam.put("updateTime", df.format(new Date()));
							ZjyLogUtil.saveJobRecordNumShardingItem(jobParam);
						} catch (Exception e) {
							//保存出错数据
							Map<String, Object> myExceptionMap=new HashMap<>();
							myExceptionMap.put("iotype", Constants.OUTPATIENT);
							myExceptionMap.put("param", System.currentTimeMillis()+" as param");
							myExceptionMap.put("data", JobCommonUtil.getSerializedBytes(param));
							myExceptionMap.put("exception", e.toString());
							try {
								zjyLogService.saveJobAbnormalExtraction(myExceptionMap);
							} catch (Exception e2) {
								logger.error("异常信息保存失败",e2);
							}
							logger.error("批次构建门诊信息失败!",e);
						};
						long timeEnd=System.currentTimeMillis();
						System.out.println("门诊信息构建结束! 耗时:"+(timeEnd-timeStart));
						countDownLatch.countDown();
					}
				};
				cachedThreadPool.execute(absingleOutpatientRecordBuild);
		}
	    if(dataHandle.containsKey(Constants.HOSPITALIZATION)){//批次构建住院信息
	    	      Runnable absingleInhosRecordBuild=	new Runnable() {
						public void run() {
							long timeStart=System.currentTimeMillis();
							logger.info("住院信息构建开始!");
							Map<String, Object> param=new HashMap<>();
							param.put("ioType", "zy");
							param.put("data", dataHandle.get(Constants.HOSPITALIZATION));
							param.put("param", System.currentTimeMillis()+" as param");
							try {
							hospitalizationService.singleInhosRecordBuild((List<Map<String, Object>>)dataHandle.get(Constants.HOSPITALIZATION), param);
							Map<String, Object> jobParam = new HashMap<String, Object>();
							jobParam.put("frequency", 0);
							jobParam.put("jobname", shardingContext.getJobName());
							jobParam.put("shardingItem", shardingContext.getShardingItem());
							jobParam.put("successful_number",((List<Map<String, Object>>)dataHandle.get(Constants.HOSPITALIZATION)).size());
							jobParam.put("updateTime", df.format(new Date()));
							ZjyLogUtil.saveJobRecordNumShardingItem(jobParam);
							} catch (Exception e) {
								//保存出错数据
								Map<String, Object> myExceptionMap=new HashMap<>();
								myExceptionMap.put("iotype", Constants.HOSPITALIZATION);
								myExceptionMap.put("param", System.currentTimeMillis()+" as param");
								myExceptionMap.put("data", JobCommonUtil.getSerializedBytes(param));
								myExceptionMap.put("exception", e.toString());
								try {
									zjyLogService.saveJobAbnormalExtraction(myExceptionMap);
								} catch (Exception e2) {
									logger.error("异常信息保存失败",e2);
								}
								logger.error("批次构建住院信息失败!",e);
							};
							long timeEnd=System.currentTimeMillis();
							System.out.println("住院信息构建结束! 耗时:"+(timeEnd-timeStart));
							countDownLatch.countDown();
						}
					};
					 cachedThreadPool.execute(absingleInhosRecordBuild);
		}
	    if(dataHandle.containsKey(Constants.PUBLICHEALTH)){//批次构建公卫信息
  	                Runnable personalPublicHealthBuild=	new Runnable() {
						public void run() {
							try {
								long timeStart=System.currentTimeMillis();
								publicHealthService.personalPublicHealthBuild(dataHandle);
								long timeEnd=System.currentTimeMillis();
								Map<String, Object> jobParam = new HashMap<String, Object>();
								jobParam.put("frequency", 0);
								jobParam.put("jobname", shardingContext.getJobName());
								jobParam.put("shardingItem", shardingContext.getShardingItem());
								jobParam.put("successful_number", dataHandle.size());
								jobParam.put("updateTime", df.format(new Date()));
								ZjyLogUtil.saveJobRecordNumShardingItem(jobParam);
								System.out.println("公卫信息构建结束! 耗时:"+(timeEnd-timeStart));
							} catch (Exception e) {
								Map<String, Object> myExceptionMap=new HashMap<>();
								myExceptionMap.put("iotype", Constants.PUBLICHEALTH);
								myExceptionMap.put("param", System.currentTimeMillis()+" as param");
								myExceptionMap.put("data", JobCommonUtil.getSerializedBytes(dataHandle));
								myExceptionMap.put("exception", e.toString());
								try {//此异常数据 将 会对数据重新采集影响
									zjyLogService.saveJobAbnormalExtraction(myExceptionMap);
								} catch (Exception e2) {
									logger.error("异常信息保存失败",e2);
								}
								logger.error("批次构建公卫信息失败!",e);
							}
							countDownLatch.countDown();
						}
					};
					cachedThreadPool.execute(personalPublicHealthBuild);
		}
	    /**
	     * 等等我!
	     */
	    try {
			countDownLatch.await();
			//等待完成后操作(保存此批次最大处理的rowid)
			try {
				String jobParameter = shardingContext.getJobParameter();
				Map<String, Object> jobParamMap=JobCommonUtil.paramBuild(jobParameter);//参数构建
				templateStructureService.saveBatchMaxid(Long.valueOf(dataHandle.get(Constants.MAXID).toString()), shardingContext.getJobName()+jobParamMap.get("iotype"), shardingContext.getShardingItem());
			} catch (Exception e) {
				logger.error("保存此批次最大数据处理",e);
			}
		} catch (InterruptedException e) {
			logger.error("同步异常!",e);
		}
	}
}
