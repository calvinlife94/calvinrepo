package com.zhijieyun.datastructure.elasticJobLite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.zhijieyun.datastructure.service.TemplateStructureService;
import com.zhijieyun.datastructure.util.MapUtils;

import io.elasticjob.lite.api.ShardingContext;
import io.elasticjob.lite.api.dataflow.DataflowJob;

public class DataStructureItem implements DataflowJob<Map<String, Object>>{
	
	private Logger logger = Logger.getLogger(DataStructureItem.class);
	
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private TemplateStructureService templateStructureService;
	
	/*
	 * 在数据抓取时，每个片只抓取自己需要处理的数据，不用每个片都抓取全部数据
	 */
	@Override
	public List<Map<String, Object>> fetchData(ShardingContext shardingContext) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("shardingItem", shardingContext.getShardingItem());
		map.put("shardingTotalCount", shardingContext.getShardingTotalCount());
		List<Map<String, Object>> mapList = null;
		try {
			mapList = templateStructureService.queryBuildingItems(map);
		} catch (Exception e) {
			logger.error("查询服务信息失败", e);
		}
		return mapList;
	}

	/*
	 * 直接处理抓取的数据，不用再做逻辑判断
	 */
	@Override
	public void processData(ShardingContext shardingContext, List<Map<String, Object>> data) {
		MapUtils.mapExcludeNull(data);
		try {
			long time1 = System.currentTimeMillis();
			long drugsSize =mongoTemplate.count(new Query(), "items");
			if(drugsSize<=0){
				mongoTemplate.insert(data, "items");
			}else{
				System.out.println("已存在服务信息,将不在构建服务信息!");
			}
			long time2 = System.currentTimeMillis();
			logger.info("mongodb插入服务信息成功:耗时:"+(time2-time1));
		} catch (Exception e) {
			logger.error("mongodb插入服务信息失败", e);
		}
	
	}
	
}
