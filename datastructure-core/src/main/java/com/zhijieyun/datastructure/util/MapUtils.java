/**
 * Copyright (c) 2018. ZhiJieYun Corporation. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of ZhiJieYun
 * Corporation ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ZhiJieYun Corporation or a ZhiJieYun
 * authorized reseller (the "License Agreement"). ZhiJieYun may make changes to the
 * Confidential Information from time to time. Such Confidential Information may
 * contain errors.
 *
 * EXCEPT AS EXPLICITLY SET FORTH IN THE LICENSE AGREEMENT, ZhiJieYun DISCLAIMS ALL
 * WARRANTIES, COVENANTS, REPRESENTATIONS, INDEMNITIES, AND GUARANTEES WITH
 * RESPECT TO SOFTWARE AND DOCUMENTATION, WHETHER EXPRESS OR IMPLIED, WRITTEN OR
 * ORAL, STATUTORY OR OTHERWISE INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, TITLE, NON-INFRINGEMENT AND FITNESS FOR A
 * PARTICULAR PURPOSE. ZhiJieYun DOES NOT WARRANT THAT END USER'S USE OF THE
 * SOFTWARE WILL BE UNINTERRUPTED, ERROR FREE OR SECURE.
 *
 * ZhiJieYun SHALL NOT BE LIABLE TO END USER, OR ANY OTHER PERSON, CORPORATION OR
 * ENTITY FOR INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY OR CONSEQUENTIAL
 * DAMAGES, OR DAMAGES FOR LOSS OF PROFITS, REVENUE, DATA OR USE, WHETHER IN AN
 * ACTION IN CONTRACT, TORT OR OTHERWISE, EVEN IF ZhiJieYun HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. ZhiJieYun' TOTAL LIABILITY TO END USER SHALL NOT
 * EXCEED THE AMOUNTS PAID FOR THE ZhiJieYun SOFTWARE BY END USER DURING THE PRIOR
 * TWELVE (12) MONTHS FROM THE DATE IN WHICH THE CLAIM AROSE.  BECAUSE SOME
 * STATES OR JURISDICTIONS DO NOT ALLOW LIMITATION OR EXCLUSION OF CONSEQUENTIAL
 * OR INCIDENTAL DAMAGES, THE ABOVE LIMITATION MAY NOT APPLY TO END USER.
 *
 * Copyright version 1.0
 */
package com.zhijieyun.datastructure.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * MapUtil.java Description:
 * 
 * @author：ls @version： V1.0.0
 * @date：2018年6月27日 下午6:04:16
 */
@SuppressWarnings("unchecked")
public class MapUtils {

	public static void mapExcludeNull(List<Map<String, Object>> list) {
		for (Map<String, Object> map : list) {
			Iterator<Entry<String, Object>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				String key = entry.getKey();
				Object val = entry.getValue();
				if (val == null || "null".equals(val)) {
					val = "";
					map.put(key, val);
				}
			}
		}
	}
	
	public static void mapExcludeNull(Map<String, Object> map) {
		Iterator<Entry<String, Object>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			Object val = entry.getValue();
			if (val == null || "null".equals(val)) {
				val = "";
				map.put(key, val);
			}
		}
	}
	/**
	 * 递归遍历一个集合里面所有key val
	 * @param list
	 */
	public static void mapExcludeNullRecursion(List<Map<String, Object>> list) {
		//需要移出某些id为空的字段 做潜规则处理   索引从大到小移出
		if(list!=null){
			for (int n=list.size()-1 ;n>=0;n--) {//倒序遍历排除remove
				Map<String, Object> map=list.get(n);
				if(map.get("id")==null||"".equals("id")){
					if(map.get("ID")==null||"".equals("ID")){
						list.remove(n);
						continue;
					}
				}
				Iterator<Entry<String, Object>> it = map.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Object> entry = it.next();
					String key = entry.getKey();
					Object val = entry.getValue();
					if (val == null || "null".equals(val)&&!(val instanceof List)&&!(val instanceof Map)) {
						val = "";
						map.put(key, val);
					}else if(val instanceof List){
						mapExcludeNullRecursion((List<Map<String, Object>>)val);
					}else if(val instanceof Map){
						mapExcludeNullRecursion((Map<String, Object>)val);
					}
				}
			}
		}

	}
	
	public static void mapExcludeNullRecursion(Map<String, Object> map) {
		Iterator<Entry<String, Object>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			Object val = entry.getValue();
			if (val == null || "null".equals(val)&&!(val instanceof List)&&!(val instanceof Map)) {
				val = "";
				map.put(key, val);
			}else if(val instanceof List){
				mapExcludeNullRecursion((List<Map<String, Object>>)val);
			}else if(val instanceof Map){
				mapExcludeNullRecursion((Map<String, Object>)val);
			}
		}
	}
}
