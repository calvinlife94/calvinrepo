package com.zhijieyun.datastructure.service;

import java.util.List;
import java.util.Map;


public interface PublicHealthService {
	/**
	 * 根据ids 查询    此批次所有人得个案信息
	 * 抛出异常 调用点统一处理
	 */
	List<Map<String, Object>> queryBatchPersonals(List<Integer> ids) throws Exception;
	/**
	 * 构建公卫信息
	 * @param list
	 * @throws Exception
	 */
	void personalPublicHealthBuild(Map<String, Object> param) throws Exception;
    /**
     * 构建个人基本信息
     *  @param list
     *  @throws Exception
     */
	void personalInfoBuild(List<Map<String, Object>> list) throws Exception;
}
