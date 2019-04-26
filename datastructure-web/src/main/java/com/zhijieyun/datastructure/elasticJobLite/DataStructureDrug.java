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

public class DataStructureDrug implements DataflowJob<Map<String, Object>>{
	
	private Logger logger = Logger.getLogger(DataStructureDrug.class);
	
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
			mapList = templateStructureService.queryBuildingDrugs(map);
		} catch (Exception e) {
			logger.error("查询药品信息至mongodb失败", e);
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
			long drugsSize =mongoTemplate.count(new Query(), "drugs");
			if(drugsSize<=0){
				mongoTemplate.insert(data, "drugs");
			}else{
				System.out.println("已存在药品信息,将不在构建药品信息!");
			}
			logger.info("插入药品信息至mongodb成功");
		} catch (Exception e) {
			logger.error("插入药品信息至mongodb失败", e);
		}
	
	}
	
}
