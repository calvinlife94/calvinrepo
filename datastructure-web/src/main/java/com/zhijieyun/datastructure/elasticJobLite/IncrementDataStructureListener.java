package com.zhijieyun.datastructure.elasticJobLite;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;
import com.zhijieyun.datastructure.constants.Constants;
import com.zhijieyun.datastructure.service.TemplateStructureService;
import com.zhijieyun.datastructure.util.JobCommonUtil;
import com.zhijieyun.datastructure.util.JobOperateAPIUtil;

import io.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import io.elasticjob.lite.executor.ShardingContexts;
/**
 * 增量更新监听器
 * @author 湖里一条鱼
 *
 */
public class IncrementDataStructureListener extends AbstractDistributeOnceElasticJobListener {
	private static Logger logger = Logger.getLogger(IncrementDataStructureListener.class);
    
	@Autowired
	private TemplateStructureService templateStructureService;
	
	@Autowired
	private JobOperateAPIUtil jobOperateAPIUtil;
	

	public IncrementDataStructureListener(long startedTimeoutMilliseconds, long completedTimeoutMilliseconds) {
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
		map.put("jobName", "increment"+shardingContexts.getJobName()+jobParameter.get("iotype"));
		try {
			System.out.println("提示:开始创建增量临时表中,系统将启动增量数据抽取,请勿再次此触发!");
			Long startTime=System.currentTimeMillis();
			templateStructureService.createIncrementTemplateStructure(map);
			Long endTime=System.currentTimeMillis();
			System.out.println("耗时(ms):"+(endTime-startTime)+",临时增量表创建成功!");
		} catch (Exception e) {
			logger.error("创建临时表失败:"+e);
		}
	}

	@Override
	public void doAfterJobExecutedAtLastCompleted(ShardingContexts shardingContexts) {
		/**
		 * 临时表两个数据进行同步
		 */
		Map<String, Object> map=new HashMap<>();
		
		Map<String, Object> jobParameter=JobCommonUtil.paramBuild(shardingContexts.getJobParameter());
		map.put("tempTableName", jobParameter.get("tempTableName"));
		try {
			templateStructureService.mergeIncrementData(map);
		} catch (Exception e) {
			logger.error("数据同步失败",e);
		}
		try {
			jobOperateAPIUtil.getJobOperateAPI().trigger(Optional.of(Constants.INCREMENT+shardingContexts.getJobName()),  Optional.<String>absent());
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println("数据抽取完成");
	}

}
