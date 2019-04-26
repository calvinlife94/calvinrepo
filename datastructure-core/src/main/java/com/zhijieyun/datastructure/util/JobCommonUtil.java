package com.zhijieyun.datastructure.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.zhijieyun.datastructure.constants.Constants;

/**
 * 数据特殊处理工具
 * @author zwj
 *
 */
@SuppressWarnings({"unchecked","serial"})
public class JobCommonUtil {
	private static Logger logger = Logger.getLogger(JobCommonUtil.class);
	
	/**
	 * 对批量数据进行归总处理
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static Map<String, List<Integer>> dataBuild(List<Map<String, Object>> data) throws Exception{
        //数据去null 处理
		MapUtils.mapExcludeNull(data);
		Map<String, List<Integer>> searchMap=new HashMap<>();
		for(Map<String, Object> map:data){
			String temstr=map.get("temstr").toString();
			Map<String, List<Integer>> strings=JSONObject.parseObject("{"+temstr+"}",Map.class);
	        for(Entry<String, List<Integer>> entry: strings.entrySet()){
	        	if(searchMap.containsKey(entry.getKey())){//当存在此数据时进行累计处理
	        		//对个人信息特殊处理 (当一个人信息中存在多个信息时(大于1),保留第一个信息)
	        		if(Constants.PERSONALINFO.equals(entry.getKey())&&!entry.getValue().isEmpty()&&entry.getValue().size()>0){
	        			searchMap.get(entry.getKey()).add(entry.getValue().get(0));
	        		}else if(!Constants.PERSONALINFO.equals(entry.getKey())){
	        		    //其他
	        			searchMap.get(entry.getKey()).addAll(entry.getValue());
	        		}
	        	}else{
	        		if(Constants.PERSONALINFO.equals(entry.getKey())){
	        			List<Integer> show=new ArrayList<Integer>();
	        			show.add(entry.getValue().get(0));
	        			searchMap.put(entry.getKey(),show);
	        		}else if(Constants.OUTPATIENT.equals(entry.getKey())||Constants.HOSPITALIZATION.equals(entry.getKey())){
	        			searchMap.put(Constants.HEALTHTREE, null);//此标记为有健康树需要处理
	        			searchMap.put(entry.getKey(),entry.getValue());
	        		}else{
	        			searchMap.put(Constants.HEALTHTREE, null);//此标记为有健康树需要处理
	        			searchMap.put(Constants.PUBLICHEALTH, null);//此标记为有公卫信息
	        			searchMap.put(entry.getKey(),entry.getValue());
	        		}
	        	}
	        }
		}
		return searchMap;
	}
	
	/**
	 * 构建参数  如果参数没有实例化   则默认参数
	 * @param jobParameter
	 * 例如:extractSize=100,iotype=all,tempTableName=tempTableName
	 * @return
	 */
	public	static Map<String, Object> paramBuild(String jobParameter){
		String[] paramArr = jobParameter.split(",");
		//初始化map 并给出默认值
		Map<String, Object> paramMap=new HashMap<String, Object>(){
		{put("extractSize",500); put("iotype","all");put("tempTableName","tempTableName");put("reExtract",false);}};
		for(String string:paramArr){
			String[] paramString =string.split("=");
			paramMap.put(paramString[0], paramString[1]);
		}
		return paramMap;
	}
	/**
	 * 根据数据ID构建展示树
	 * @param data 
	 * @param constants住院或门诊
	 * @return
	 */
	public static Map<String, Object> extractTreeInfoById(List<Map<String, Object>> data,String constants){
		Map<String, Object> dataMap=new HashMap<>();
		if(data!=null){
			for(Map<String, Object> map: data){
				if(map.get("id")!=null){
					Map<String, Object> dataDetailsMap=new HashMap<>();
					dataDetailsMap.put("deptName",map.getOrDefault("doctorName",""));
					dataDetailsMap.put("sexCode",map.getOrDefault("sexCode",""));
					dataDetailsMap.put("doctorName",map.getOrDefault("doctorName",""));
					dataDetailsMap.put("orgName",map.getOrDefault("orgName",""));
					dataDetailsMap.put("code",map.getOrDefault("metaSetCode",""));
					dataDetailsMap.put("pname",map.getOrDefault("pname",""));
					dataDetailsMap.put("orgCode",map.getOrDefault("orgCode",""));
					dataDetailsMap.put("idcard",map.getOrDefault("idcard",""));
					if(Constants.OUTPATIENT.equals(constants)){
						dataDetailsMap.put("mzId",map.get("mzId"));
						dataDetailsMap.put("serviceName","门诊记录");
					}
					if(Constants.HOSPITALIZATION.equals(constants)){
						dataDetailsMap.put("zyId",map.get("zyId"));
						dataDetailsMap.put("serviceName","住院记录");
					}
					dataDetailsMap.put("serviceTime",map.getOrDefault("serviceTime",""));
					dataDetailsMap.put("deptCode",map.getOrDefault("deptCode",""));
					dataMap.put(map.getOrDefault("id", "").toString(), dataDetailsMap);
				}
			}
		}
		return dataMap;
	}
	
	/**
	 * 根据数据ID构建展示树
	 * @param data 
	 * @param constants住院或门诊
	 * @return
	 */
	public static Map<String, Object> extractTreeInfoById(Object mpiid,String metasetCode,Map<String, Object> diabetesManagementDataMap){
		Map<String, Object> nodeMap=new HashMap<>();
		nodeMap.put("docNum", 1);
		nodeMap.put("metaSetCode",metasetCode);
		nodeMap.put("mpiid", mpiid);
		if(diabetesManagementDataMap!=null){
			nodeMap.put("name",diabetesManagementDataMap.getOrDefault("name", ""));
			nodeMap.put("sourceID",diabetesManagementDataMap.getOrDefault("ID",""));
			nodeMap.put("hrn", diabetesManagementDataMap.getOrDefault("hrn", ""));
			nodeMap.put("pname",diabetesManagementDataMap.getOrDefault("pname", ""));
			nodeMap.put("sex",diabetesManagementDataMap.getOrDefault("sex", ""));
			nodeMap.put("serviceTime",diabetesManagementDataMap.getOrDefault("serviceTime", ""));
			nodeMap.put("serviceOrgName",diabetesManagementDataMap.getOrDefault("orgName", ""));
			nodeMap.put("displayType",diabetesManagementDataMap.getOrDefault("Other", ""));
			nodeMap.put("dob", diabetesManagementDataMap.getOrDefault("dob", ""));
		}
		return nodeMap;
	}
	
	/**
	 * Map转数组
	 * @param list
	 * @return
	 */
	public static byte[] getSerializedBytes(Object data) {
		if (null == data) return null;
		byte[]  byteData=null;
		try {
			 ByteArrayOutputStream bo= new ByteArrayOutputStream();
			 ObjectOutputStream os = new ObjectOutputStream(bo);
			 os.writeObject(data);
			 byteData=bo.toByteArray();
			 os.close();
			 bo.close();
		} catch (IOException e) {
			logger.error("map转byte[]异常",e);
			return null;
		}
		return byteData;
	}

	public static Map<String, Object> getSerializedMaps(byte[] data) {
		Map<String, Object> map = null;
		ByteArrayInputStream byteInt = new ByteArrayInputStream(data);
		try {
			ObjectInputStream objInt = new ObjectInputStream(byteInt);
			map = (Map<String, Object>) objInt.readObject();
		} catch (IOException | ClassNotFoundException e) {
			logger.error("byte[]转map异常", e);
		}
		return map;
	}
	
	public static List<Map<String, Object>> getSerializedLists(byte[] data) {
		List<Map<String, Object>> list= null;
		ByteArrayInputStream byteInt = new ByteArrayInputStream(data);
		try {
			ObjectInputStream objInt = new ObjectInputStream(byteInt);
			list = (List<Map<String, Object>>) objInt.readObject();
		} catch (IOException | ClassNotFoundException e) {
			logger.error("byte[]转map异常", e);
		}
		return list;
	}
}
