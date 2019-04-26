package com.zhijieyun.datastructure.util;

import org.apache.log4j.Logger;

import com.google.common.base.Optional;

import io.elasticjob.lite.lifecycle.api.JobAPIFactory;
import io.elasticjob.lite.lifecycle.api.JobOperateAPI;

public class JobOperateAPIUtil {
	static Logger logger = Logger.getLogger(JobOperateAPIUtil.class);
	public String serverLists;
	public String namespace;

	public  String getServerLists() {
		return serverLists;
	}
	public  void setServerLists(String serverLists) {
		this.serverLists = serverLists;
	}
	public  String getNamespace() {
		return namespace;
	}
	public  void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public  JobOperateAPI getJobOperateAPI() {
		JobOperateAPI jobAPIService = JobAPIFactory.createJobOperateAPI(serverLists, namespace,Optional.fromNullable(null));
		return jobAPIService;
	}
}
