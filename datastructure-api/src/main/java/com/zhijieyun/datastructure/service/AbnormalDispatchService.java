package com.zhijieyun.datastructure.service;

import java.util.List;
import java.util.Map;

import com.zhijieyun.zjylog.domain.entity.JobException;


/**
 * 
 * @author 湖里一条鱼
 * 异常处理重传
 */
public interface AbnormalDispatchService {
     /**
      * 处理异常数据
      * @param  constants 处理数据常量
      * @param  ids 需要处理的数据ids
      * @return Map{}处理成功多少,处理失败多少
      */
	
	Map<String, Object> abnormalDataProcessing(String ids);
	/**
	 * 处理异常数据
	 * @param abnormalDataMaps
	 * @return
	 */
    Map<String, Object> abnormalDataProcessing(List<JobException> abnormalDataMaps);
}
