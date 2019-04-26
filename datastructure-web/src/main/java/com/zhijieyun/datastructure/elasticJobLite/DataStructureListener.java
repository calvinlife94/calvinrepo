package com.zhijieyun.datastructure.elasticJobLite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.zhijieyun.datastructure.constants.Constants;
import com.zhijieyun.datastructure.service.TemplateStructureService;
import com.zhijieyun.datastructure.util.JobCommonUtil;
import com.zhijieyun.datastructure.util.JobOperateAPIUtil;

import io.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import io.elasticjob.lite.executor.ShardingContexts;

public class DataStructureListener extends AbstractDistributeOnceElasticJobListener {
	private static Logger logger = Logger.getLogger(DataStructureListener.class);
    
	@Autowired
	private TemplateStructureService templateStructureService;
	@Autowired
	private JobOperateAPIUtil jobOperateAPIUtil;
	@Autowired
	private MongoTemplate mongoTemplate;

	public DataStructureListener(long startedTimeoutMilliseconds, long completedTimeoutMilliseconds) {
		super(startedTimeoutMilliseconds, completedTimeoutMilliseconds);
	}

	@Override
	public void doBeforeJobExecutedAtLastStarted(ShardingContexts shardingContexts) {
		/**
		 * 根据分片内容构建所需的索引表模板
		 */
		Map<String, Object> map=new HashMap<>();
		
		Map<String, Object> jobParameter=JobCommonUtil.paramBuild(shardingContexts.getJobParameter());
		map.put("tempTableName", jobParameter.get("tempTableName"));
		map.put("jobName", shardingContexts.getJobName()+jobParameter.get("iotype"));
		try {
			System.out.println("提示:开始创建临时表中,系统将启动原始数据抽取,原始数据抽取完毕后将自动增量数据抽取,请勿再次此触发!");
			Long startTime=System.currentTimeMillis();
			templateStructureService.createTemplateStructure(map);
			Long endTime=System.currentTimeMillis();
			createIndex();
			//手动触发药品,服务
			jobOperateAPIUtil.getJobOperateAPI().trigger(Optional.of(Constants.ITEMSINFO),  Optional.<String>absent());
			jobOperateAPIUtil.getJobOperateAPI().trigger(Optional.of(Constants.DRUGSINFO),  Optional.<String>absent());
			System.out.println("耗时(ms):"+(endTime-startTime)+",临时表创建成功!");
		} catch (Exception e) {
			logger.error("创建临时表失败:"+e);
		}
	}
	
	//创建索引
	public void	createIndex(){
		List<DBCollection> collections=new ArrayList<DBCollection>();
		//DBCollection drugs = mongoTemplate.getCollection("drugs");	collections.add(drugs);
		DBCollection exams = mongoTemplate.getCollection("exams");	collections.add(exams);
		DBCollection inHosDiagnosis = mongoTemplate.getCollection("inHosDiagnosis");	collections.add(inHosDiagnosis);
		DBCollection inspects = mongoTemplate.getCollection("inspects");	collections.add(inspects);
		//DBCollection items = mongoTemplate.getCollection("items");	collections.add(items);
		DBCollection orders = mongoTemplate.getCollection("orders");	collections.add(orders);
		DBCollection personalHealthTreeIndexJson = mongoTemplate.getCollection("personalHealthTreeIndexJson");	collections.add(personalHealthTreeIndexJson);
		DBCollection personalInfo = mongoTemplate.getCollection("personalInfo");	collections.add(personalInfo);
		DBCollection personalPublicHealth = mongoTemplate.getCollection("personalPublicHealth");	collections.add(personalPublicHealth);
		DBCollection recentRecords = mongoTemplate.getCollection("recentRecords");	collections.add(recentRecords);
		DBCollection singleInhosRecord = mongoTemplate.getCollection("singleInhosRecord");	collections.add(singleInhosRecord);
		DBCollection singleOutpatientRecord = mongoTemplate.getCollection("singleOutpatientRecord");	collections.add(singleOutpatientRecord);
		for(DBCollection dbCollection: collections){
			Boolean indexBuiltFlag = false;
			List<DBObject> indexList = dbCollection.getIndexInfo();
			if(!CollectionUtils.isEmpty(indexList)){
				for(DBObject index : indexList){
					@SuppressWarnings("unchecked")
					Map<String, Object> keyMap =  (Map<String, Object>) index.get("key");
					if(keyMap.containsKey("mpiid")){
						indexBuiltFlag = true;
						break;
					}
				}
			}
			if(!indexBuiltFlag){//创建索引
				dbCollection.createIndex(new BasicDBObject("mpiid", 1));
			}
		}
	}
	

	@Override
	public void doAfterJobExecutedAtLastCompleted(ShardingContexts shardingContexts) {
		/**
		 * 测试 (立即触发增量测试)
		 */
		jobOperateAPIUtil.getJobOperateAPI().trigger(Optional.of(Constants.INCREMENT+shardingContexts.getJobName()),  Optional.<String>absent());
		/**
		 * 关闭此次完整调度
		 */
		jobOperateAPIUtil.getJobOperateAPI().shutdown(Optional.of(shardingContexts.getJobName()), Optional.<String>absent());

		System.out.println("原始数据抽取完成");
	}

}
