package com.zhijieyun.datastructure.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.CollectionUtils;


/**
 * Redis Util
 */
public class RedisManager {
	private static Logger logger = Logger.getLogger(RedisManager.class);

	private RedisTemplate<String, Object> redisTemplate_healthTree;   			//1号库，构建过健康树的mpiid
	private RedisTemplate<String, Object> redisTemplate_publicHealth; 			//2号库，构建过公卫的mpiid
	private RedisTemplate<String, Object> redisTemplate_healthTreeRecord;   	//3号库，需要构建的健康树的mpiid、id
	private RedisTemplate<String, Object> redisTemplate_inhos;   	  			//4号库，需要构建的住院记录的id、sourceid
	private RedisTemplate<String, Object> redisTemplate_outpatient;   			//5号库，需要构建的门诊记录的id、sourceid
	private RedisTemplate<String, Object> redisTemplate_publicHealthRecord;   	//6号库，需要构建的公卫的mpiid、id
	private RedisTemplate<String, Object> redisTemplate_personalInfo;   	    //7号库，需要构建的个人基本信息的mpiid、id
	
	public void setRedisTemplate_healthTree(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate_healthTree = redisTemplate;
	}
	
	public void setRedisTemplate_publicHealth(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate_publicHealth = redisTemplate;
	}
	
	public void setRedisTemplate_healthTreeRecord(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate_healthTreeRecord = redisTemplate;
	}
	
	public void setRedisTemplate_inhos(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate_inhos = redisTemplate;
	}
	
	public void setRedisTemplate_outpatient(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate_outpatient = redisTemplate;
	}
	
	public void setRedisTemplate_publicHealthRecord(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate_publicHealthRecord = redisTemplate;
	}
	
	public void setRedisTemplate_personalInfo(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate_personalInfo = redisTemplate;
	}

	/**
	 * 指定缓存失效时间
	 * 
	 * @param key
	 *            键
	 * @param time
	 *            时间(秒)
	 * @return
	 */
	public boolean expire(String key, long time) {
		try {
			if (time > 0) {
				redisTemplate_healthTree.expire(key, time, TimeUnit.SECONDS);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

	/**
	 * 根据key 获取过期时间
	 * 
	 * @param key
	 *            键 不能为null
	 * @return 时间(秒) 返回0代表为永久有效
	 */
	public long getExpire(String key) {
		return redisTemplate_healthTree.getExpire(key, TimeUnit.SECONDS);
	}

	/**
	 * 判断key是否存在
	 * 
	 * @param key
	 *            键
	 * @return true 存在 false不存在
	 */
	public boolean hasKey(String key) {
		try {
			return redisTemplate_healthTree.hasKey(key);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除缓存
	 * 
	 * @param key
	 *            可以传一个值 或多个
	 */
	@SuppressWarnings("unchecked")
	public void del(String... key) {
		if (key != null && key.length > 0) {
			if (key.length == 1) {
				redisTemplate_healthTree.delete(key[0]);
			} else {
				redisTemplate_healthTree.delete(CollectionUtils.arrayToList(key));
			}
		}
	}

	// ============================String=============================
	/**
	 * 普通缓存获取
	 * 
	 * @param key
	 *            键
	 * @return 值
	 */
	public Object getHealthTree(String key) {
		return key == null ? null : redisTemplate_healthTree.opsForValue().get(key);
	}
	
	public Object getPublicHealth(String key) {
		return key == null ? null : redisTemplate_publicHealth.opsForValue().get(key);
	}

	/**
	 * 普通缓存放入
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return true成功 false失败
	 */
	public boolean set(String key, Object value) {
		try {
			redisTemplate_healthTree.opsForValue().set(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 普通缓存放入并设置时间
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param time
	 *            时间(秒) time要大于0 如果time小于等于0 将设置无限期
	 * @return true成功 false 失败
	 */
	public boolean set(String key, Object value, long time) {
		try {
			if (time > 0) {
				redisTemplate_healthTree.opsForValue().set(key, value, time, TimeUnit.SECONDS);
			} else {
				set(key, value);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 递增
	 * 
	 * @param key
	 *            键
	 * @param by
	 *            要增加几(大于0)
	 * @return
	 */
	public long incr(String key, long delta) {
		if (delta < 0) {
			throw new RuntimeException("递增因子必须大于0");
		}
		return redisTemplate_healthTree.opsForValue().increment(key, delta);
	}

	/**
	 * 递减
	 * 
	 * @param key
	 *            键
	 * @param by
	 *            要减少几(小于0)
	 * @return
	 */
	public long decr(String key, long delta) {
		if (delta < 0) {
			throw new RuntimeException("递减因子必须大于0");
		}
		return redisTemplate_healthTree.opsForValue().increment(key, -delta);
	}

	// ================================Map=================================
	/**
	 * HashGet
	 * 
	 * @param key
	 *            键 不能为null
	 * @param item
	 *            项 不能为null
	 * @return 值
	 */
	public Object hget(String key, String item) {
		return redisTemplate_healthTree.opsForHash().get(key, item);
	}

	/**
	 * 获取hashKey对应的所有键值对
	 * 
	 * @param key
	 *            键
	 * @return 对应的多个键值对
	 */
	public Map<Object, Object> hmget(String key) {
		return redisTemplate_healthTree.opsForHash().entries(key);
	}

	/**
	 * HashSet
	 * 
	 * @param key
	 *            键
	 * @param map
	 *            对应多个键值对
	 * @return true 成功 false 失败
	 */
	public boolean hmset(String key, Map<String, Object> map) {
		try {
			redisTemplate_healthTree.opsForHash().putAll(key, map);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * HashSet 并设置时间
	 * 
	 * @param key
	 *            键
	 * @param map
	 *            对应多个键值对
	 * @param time
	 *            时间(秒)
	 * @return true成功 false失败
	 */
	public boolean hmset(String key, Map<String, Object> map, long time) {
		try {
			redisTemplate_healthTree.opsForHash().putAll(key, map);
			if (time > 0) {
				expire(key, time);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 向一张hash表中放入数据,如果不存在将创建
	 * 
	 * @param key
	 *            键
	 * @param item
	 *            项
	 * @param value
	 *            值
	 * @return true 成功 false失败
	 */
	public boolean hset(String key, String item, Object value) {
		try {
			redisTemplate_healthTree.opsForHash().put(key, item, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 向一张hash表中放入数据,如果不存在将创建
	 * 
	 * @param key
	 *            键
	 * @param item
	 *            项
	 * @param value
	 *            值
	 * @param time
	 *            时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
	 * @return true 成功 false失败
	 */
	public boolean hset(String key, String item, Object value, long time) {
		try {
			redisTemplate_healthTree.opsForHash().put(key, item, value);
			if (time > 0) {
				expire(key, time);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除hash表中的值
	 * 
	 * @param key
	 *            键 不能为null
	 * @param item
	 *            项 可以使多个 不能为null
	 */
	public void hdel(String key, Object... item) {
		redisTemplate_healthTree.opsForHash().delete(key, item);
	}

	/**
	 * 判断hash表中是否有该项的值
	 * 
	 * @param key
	 *            键 不能为null
	 * @param item
	 *            项 不能为null
	 * @return true 存在 false不存在
	 */
	public boolean hHasKey(String key, String item) {
		return redisTemplate_healthTree.opsForHash().hasKey(key, item);
	}

	/**
	 * hash递增 如果不存在,就会创建一个 并把新增后的值返回
	 * 
	 * @param key
	 *            键
	 * @param item
	 *            项
	 * @param by
	 *            要增加几(大于0)
	 * @return
	 */
	public double hincr(String key, String item, double by) {
		return redisTemplate_healthTree.opsForHash().increment(key, item, by);
	}

	/**
	 * hash递减
	 * 
	 * @param key
	 *            键
	 * @param item
	 *            项
	 * @param by
	 *            要减少记(小于0)
	 * @return
	 */
	public double hdecr(String key, String item, double by) {
		return redisTemplate_healthTree.opsForHash().increment(key, item, -by);
	}

	// ============================set=============================
	/**
	 * 根据key获取Set中的所有值
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public Set<Object> sGet(String key) {
		try {
			return redisTemplate_healthTree.opsForSet().members(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据value从一个set中查询,是否存在
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @return true 存在 false不存在
	 */
	public boolean sHasKey(String key, Object value) {
		try {
			return redisTemplate_healthTree.opsForSet().isMember(key, value);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 将数据放入set缓存
	 * 
	 * @param key
	 *            键
	 * @param values
	 *            值 可以是多个
	 * @return 成功个数
	 */
	public long sSet(String key, Object... values) {
		try {
			return redisTemplate_healthTree.opsForSet().add(key, values);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 将set数据放入缓存
	 * 
	 * @param key
	 *            键
	 * @param time
	 *            时间(秒)
	 * @param values
	 *            值 可以是多个
	 * @return 成功个数
	 */
	public long sSetAndTime(String key, long time, Object... values) {
		try {
			Long count = redisTemplate_healthTree.opsForSet().add(key, values);
			if (time > 0)
				expire(key, time);
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 获取set缓存的长度
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public long sGetSetSize(String key) {
		try {
			return redisTemplate_healthTree.opsForSet().size(key);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 移除值为value的
	 * 
	 * @param key
	 *            键
	 * @param values
	 *            值 可以是多个
	 * @return 移除的个数
	 */
	public long setRemove(String key, Object... values) {
		try {
			Long count = redisTemplate_healthTree.opsForSet().remove(key, values);
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	// ===============================list=================================

	/**
	 * 获取list缓存的内容
	 * 
	 * @param key
	 *            键
	 * @param start
	 *            开始
	 * @param end
	 *            结束 0 到 -1代表所有值
	 * @return
	 */
	public List<Object> lGet(String key, long start, long end) {
		try {
			return redisTemplate_healthTreeRecord.opsForList().range(key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取list缓存的长度
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public long lGetListSize(String key) {
		try {
			return redisTemplate_healthTree.opsForList().size(key);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 通过索引 获取list中的值
	 * 
	 * @param key
	 *            键
	 * @param index
	 *            索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
	 * @return
	 */
	public Object lGetIndex(String key, long index) {
		try {
			return redisTemplate_healthTree.opsForList().index(key, index);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 将list放入缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param time
	 *            时间(秒)
	 * @return
	 */
	public boolean lSet(String key, Object value) {
		try {
			redisTemplate_healthTreeRecord.opsForList().rightPush(key, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 将list放入缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param time
	 *            时间(秒)
	 * @return
	 */
	public boolean lSet(String key, Object value, long time) {
		try {
			redisTemplate_healthTree.opsForList().rightPush(key, value);
			if (time > 0)
				expire(key, time);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 将list放入缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param time
	 *            时间(秒)
	 * @return
	 */
	public boolean lSet(String key, List<Object> value) {
		try {
			redisTemplate_healthTreeRecord.opsForList().rightPushAll(key, value);
			return true; 
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 将list放入缓存
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param time
	 *            时间(秒)
	 * @return
	 */
	public boolean lSet(String key, List<Object> value, long time) {
		try {
			redisTemplate_healthTree.opsForList().rightPushAll(key, value);
			if (time > 0)
				expire(key, time);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 根据索引修改list中的某条数据
	 * 
	 * @param key
	 *            键
	 * @param index
	 *            索引
	 * @param value
	 *            值
	 * @return
	 */
	public boolean lUpdateIndex(String key, long index, Object value) {
		try {
			redisTemplate_healthTree.opsForList().set(key, index, value);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 移除N个值为value
	 * 
	 * @param key
	 *            键
	 * @param count
	 *            移除多少个
	 * @param value
	 *            值
	 * @return 移除的个数
	 */
	public long lRemove(String key, long count, Object value) {
		try {
			Long remove = redisTemplate_healthTree.opsForList().remove(key, count, value);
			return remove;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public void batchHealthTreeInsert(Map<String, Object> map) {
        //使用pipeline方式
		redisTemplate_healthTree.executePipelined(new RedisCallback<List<Object>>() {
            @SuppressWarnings("unchecked")
			@Override
            public List<Object> doInRedis(RedisConnection connection) throws DataAccessException {
            	for(String key : map.keySet()){
                    RedisSerializer<String> serializer_key = (RedisSerializer<String>) redisTemplate_healthTree.getKeySerializer();
                    byte[] rawKey = serializer_key.serialize(key);
                    RedisSerializer<String> serializer_value = (RedisSerializer<String>) redisTemplate_healthTree.getValueSerializer();
                    byte[] rawValue = serializer_value.serialize(map.get(key).toString());
                    connection.setEx(rawKey, Integer.MAX_VALUE, rawValue);
            	}
                    
                return null;
            }
        });
 
    }
	
	public void batchPublicHealthInsert(Map<String, Object> map) {
        //使用pipeline方式
		redisTemplate_publicHealth.executePipelined(new RedisCallback<List<Object>>() {
            @SuppressWarnings("unchecked")
			@Override
            public List<Object> doInRedis(RedisConnection connection) throws DataAccessException {
            	for(String key : map.keySet()){
                    RedisSerializer<String> serializer_key = (RedisSerializer<String>) redisTemplate_publicHealth.getKeySerializer();
                    byte[] rawKey = serializer_key.serialize(key);
                    RedisSerializer<String> serializer_value = (RedisSerializer<String>) redisTemplate_publicHealth.getValueSerializer();
                    byte[] rawValue = serializer_value.serialize(map.get(key).toString());
                    connection.setEx(rawKey, Integer.MAX_VALUE, rawValue);
            	}
                    
                return null;
            }
        });
 
    }

	public boolean lSet4HealthTreeRecord(String key, List<Object> value) {
		try {
			redisTemplate_healthTreeRecord.opsForList().rightPushAll(key, value);
			return true;
		} catch (Exception e) {
			logger.error("插入健康树信息至redis失败", e);
			return false;
		}
	}
	
	public List<Object> lGet4HealthTreeRecord(String key, long start, long end) {
		try {
			return redisTemplate_healthTreeRecord.opsForList().range(key, start, end);
		} catch (Exception e) {
			logger.error("从redis中分页查询健康树信息失败", e);
			return null;
		}
	}

	public boolean lSet4PersonalInfoRecord(String key, List<Object> value) {
		try {
			redisTemplate_personalInfo.opsForList().rightPushAll(key, value);
			return true;
		} catch (Exception e) {
			logger.error("插入个人基本信息至redis失败", e);
			return false;
		}
	}

	public List<Object> lGet4PersonalInfoRecord(String key, int start, int end) {
		try {
			return redisTemplate_personalInfo.opsForList().range(key, start, end);
		} catch (Exception e) {
			logger.error("从redis中分页查询个人基本信息失败", e);
			return null;
		}
	}

	public boolean lSet4InhosRecord(String key, List<Object> value) {
		try {
			redisTemplate_inhos.opsForList().rightPushAll(key, value);
			return true;
		} catch (Exception e) {
			logger.error("插入住院记录信息至redis失败", e);
			return false;
		}
	}
	
	public List<Object> lGet4InhosRecord(String key, int start, int end) {
		try {
			return redisTemplate_inhos.opsForList().range(key, start, end);
		} catch (Exception e) {
			logger.error("从redis中分页查询住院记录失败", e);
			return null;
		}
	}

	public boolean lSet4OutpatientRecord(String key, List<Object> value) {
		try {
			redisTemplate_outpatient.opsForList().rightPushAll(key, value);
			return true;
		} catch (Exception e) {
			logger.error("插入门诊记录信息至redis失败", e);
			return false;
		}
	}

	public List<Object> lGet4OutpatientRecord(String key, int start, int end) {
		try {
			return redisTemplate_outpatient.opsForList().range(key, start, end);
		} catch (Exception e) {
			logger.error("从redis中分页查询门诊记录失败", e);
			return null;
		}
	}

	public boolean lSet4PublicHealthRecord(String key, List<Object> value) {
		try {
			redisTemplate_publicHealthRecord.opsForList().rightPushAll(key, value);
			return true;
		} catch (Exception e) {
			logger.error("插入公卫信息至redis失败", e);
			return false;
		}
	}

	public List<Object> lGet4PublicHealthRecord(String key, int start, int end) {
		try {
			return redisTemplate_publicHealthRecord.opsForList().range(key, start, end);
		} catch (Exception e) {
			logger.error("从redis中分页查询公卫信息失败", e);
			return null;
		}
	}

}