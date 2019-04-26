package com.zhijieyun.datastructure.dubbo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.zhijieyun.mongo.service.Service4PHR;
import com.zhijieyun.zjylog.service.IZjyLogService;
import com.zhijieyun.zjylog.service.IZjyLogUtilsService;


public class DubboServiceConsumer implements ApplicationContextAware{

	private static ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		DubboServiceConsumer.applicationContext = applicationContext;
		
	}
	
	public DubboServiceConsumer(){
		
	}
	
	public static Service4PHR getService4PHR(){
		return (Service4PHR) applicationContext.getBean("service4PHRConsumer");
	}
		
	public static IZjyLogService getZjyLogService(){
		return (IZjyLogService) applicationContext.getBean("zjyLogServiceConsumer");
	}
	
	public static IZjyLogUtilsService getZjyLogUtilsService(){
		return (IZjyLogUtilsService) applicationContext.getBean("zjyLogUtilsServiceConsumer");
	}
	
}
