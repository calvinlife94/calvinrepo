package com.zhijieyun.datastructure.elasticJobLite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhijieyun.datastructure.service.AbnormalDispatchService;
import com.zhijieyun.datastructure.util.JobCommonUtil;
import com.zhijieyun.zjylog.domain.entity.JobException;
import com.zhijieyun.zjylog.service.IZjyLogService;

import io.elasticjob.lite.api.ShardingContext;
import io.elasticjob.lite.api.dataflow.DataflowJob;

/**
 * 异常处理(自动化)
 * 抽出完毕(原始数据,增量数据)后进行调度
 * @author 湖里一条鱼
 *
 */
public class AbnormalExtractionHandle implements DataflowJob<JobException>{
	private static Logger logger = Logger.getLogger(AbnormalExtractionHandle.class);
	
	@Autowired
	private AbnormalDispatchService abnormalDispatchService;
	
	@Autowired
	private IZjyLogService zjyLogService;

	@Override
	public List<JobException> fetchData(ShardingContext shardingContext) {
		Map<String, Object> map=new HashMap<String, Object>();
		Map<String, Object> paramBuildMap=JobCommonUtil.paramBuild(shardingContext.getJobParameter());
		map.put("jobFillcount", paramBuildMap.get("handleNum"));//根据count查询失败次数 当大于3(默认)次 表示此数据需要人工处理
		map.put("jobState", "1");//jobState只要有值就会进行数据筛选失败的
		map.put("start", 0);//开始
		map.put("end", paramBuildMap.get("fillCount"));//处理几条数据默认10
		List<JobException> exceptions=zjyLogService.queryJobException(map);
		return exceptions;
	}

	@Override
	public void processData(ShardingContext shardingContext, List<JobException> abnormalDataMaps) {
		try {
			abnormalDispatchService.abnormalDataProcessing(abnormalDataMaps);
		} catch (Exception e) {
			logger.error("数据处理失败!",e);
		}
	}
}
