package com.zhijieyun.datastructure.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhijieyun.zjylog.service.IZjyLogService;
import com.zhijieyun.zjylog.service.IZjyLogUtilsService;
/**
 * 记录日志的工具类
 * @author yl
 */
public class ZjyLogUtil {

	private static Logger logger = Logger.getLogger(ZjyLogUtil.class);
	
	@Autowired
	private IZjyLogUtilsService zjyLogUtilsService;
	@Autowired
	private IZjyLogService zjyLogService;
	
	private static ZjyLogUtil zjyLogUtil;
	
	private static InetAddress addr;
	private static String ip = "";
	static Properties zjylogProp;
	static InputStream zjylogIs;
	static String timeConsumingFlag = "";

	static{
		try {
			addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress().toString(); 
		} catch (UnknownHostException e) {
			String msg = "无法获取当前客户端的ip";
			logger.error(msg, e);
		} 
		
		try {
			zjylogProp = new Properties();
			zjylogIs = ZjyLogUtil.class.getClassLoader().getResourceAsStream("conf/zjylog.properties");
		} catch (Exception e) {
			logger.error("读取properties文件失败");
		}
		try {
			zjylogProp.load(zjylogIs);
			timeConsumingFlag = zjylogProp.getProperty("timeConsumingFlag");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("加载文件流异常");
		} finally{
			try {
	            if(null != zjylogIs) {
	            	zjylogIs.close();
	            }
	        } catch (IOException e) {
	            logger.error("文件流关闭异常");
	        }
		}
		
	}
		
	@PostConstruct 
	public void init() {       
		zjyLogUtil= this; 
		zjyLogUtil.zjyLogUtilsService= this.zjyLogUtilsService; 
		zjyLogUtil.zjyLogService= this.zjyLogService; 
	} 

	/**
	 * 记录各步骤的耗时
	 * 只有当开关为true时，才记录该日志
	 * @param jobParam
	 */
	public static void saveJobTimeConsuming(Map<String, Object> jobParam){
		if("true".equals(timeConsumingFlag)){
			jobParam.put("ip", ip);
			try {
				zjyLogUtil.zjyLogUtilsService.saveJobTimeConsuming(jobParam);
			} catch (Exception e) {
				String msg = "记录job耗时失败, " + jobParam;
				logger.error(msg, e);
			}
		}
	}
	
	/**
	 * 记录各分片插入MongoDB的记录数
	 * @param jobParam
	 */
	public static void saveJobRecordNumShardingItem(Map<String, Object> jobParam){
		jobParam.put("ip", ip);
		try {
			zjyLogUtil.zjyLogUtilsService.saveJobRecordNumShardingItem(jobParam);
		} catch (Exception e) {
			String msg = "记录插入纪录数失败, " + jobParam;
			logger.error(msg, e);
		}
	}
	
	/**
	 * 记录平台新上传的记录数以及有效记录数
	 * @param jobParam
	 */
	public static void saveJobRecordNum(Map<String, Object> jobParam){
		try {
			zjyLogUtil.zjyLogUtilsService.saveJobRecordNum(jobParam);
		} catch (Exception e) {
			String msg = "记录中心网关新上传纪录数失败, " + jobParam;
			logger.error(msg, e);
		}
	}
	
	/**
	 * 记录job作业的次数及日期
	 * @param jobParam
	 */
	public static void saveJobFrequency(Map<String, Object> jobParam){
		try {
			zjyLogUtil.zjyLogUtilsService.saveJobFrequency(jobParam);
		} catch (Exception e) {
			String msg = "记录作业次数失败, " + jobParam;
			logger.error(msg, e);
		}
	}
	
	/**
	 * 记录job作业的次数及日期
	 * @param jobParam
	 */
	public static void saveJobAbnormalExtraction(Map<String, Object> jobParam){
		try {
			zjyLogUtil.zjyLogService.saveJobAbnormalExtraction(jobParam);
		} catch (Exception e) {
			String msg = "记录作业次数失败, " + jobParam;
			logger.error(msg, e);
		}
	}
	
}
