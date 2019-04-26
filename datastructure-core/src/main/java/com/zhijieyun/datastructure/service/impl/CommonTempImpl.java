package com.zhijieyun.datastructure.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhijieyun.datastructure.dao.mapper.CommonTempMapper;
import com.zhijieyun.datastructure.service.CommonTempService;

@SuppressWarnings("rawtypes")
@Service("commonTempService")
public class CommonTempImpl implements CommonTempService{
	
	@Autowired
	private CommonTempMapper commonTempMapper;


	@Override
	public int createInsertIOTempTableName(Map map) throws Exception {
		return commonTempMapper.createInsertIOTempTableName(map);
	}

	@Override
	public void queryAndSaveMaxId(Map map) throws Exception{
		commonTempMapper.queryAndSaveMaxId(map);
	}

	@Override
	public Long queryLastBatchMaxIdByJobName(Map<String, Object> param) throws Exception{
		Long lastBatchMax=commonTempMapper.queryLastBatchMaxIdByJobName(param);
		return lastBatchMax==null?0L:lastBatchMax;
	}

}
