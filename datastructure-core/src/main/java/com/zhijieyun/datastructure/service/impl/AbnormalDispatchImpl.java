package com.zhijieyun.datastructure.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhijieyun.datastructure.constants.Constants;
import com.zhijieyun.datastructure.service.AbnormalDispatchService;
import com.zhijieyun.datastructure.service.HospitalizationService;
import com.zhijieyun.datastructure.service.OutpatientService;
import com.zhijieyun.datastructure.service.PublicHealthService;
import com.zhijieyun.datastructure.util.JobCommonUtil;
import com.zhijieyun.zjylog.domain.entity.JobException;
import com.zhijieyun.zjylog.service.IZjyLogService;

@Service("abnormalDispatchService")
@SuppressWarnings("unchecked")
public class AbnormalDispatchImpl implements AbnormalDispatchService {
	private static Logger logger = Logger.getLogger(AbnormalDispatchImpl.class);
	@Autowired
	private OutpatientService outpatientService;
	
	@Autowired
	private HospitalizationService hospitalizationService;
	
	@Autowired
	private PublicHealthService publicHealthService;
	
	@Autowired
	private IZjyLogService zjyLogService;
	@Override
	public Map<String, Object> abnormalDataProcessing(String ids) {
		List<JobException> abnormalDataMaps= zjyLogService.queryJobAbnormalExtraction(ids);
		abnormalDataProcessing(abnormalDataMaps);
		return null;
	}
	
	@Override
	public Map<String, Object> abnormalDataProcessing(List<JobException> abnormalDataMaps) {
		for(JobException abnormalData: abnormalDataMaps){
			if(Constants.HOSPITALIZATION.equals(abnormalData.getIoType())){//住院处理
				try {
					abnormalData.setState("1");
					Map<String, Object> param =JobCommonUtil.getSerializedMaps(abnormalData.getData());
					List<Map<String, Object>> outpatientParam= (List<Map<String, Object>>) param.get("data");
					hospitalizationService.singleInhosRecordBuild(outpatientParam, param);
				} catch (Exception e) {
					logger.error("住院异常数据处理失败", e);
					abnormalData.setState("2");
					continue;
				}
			}else if(Constants.OUTPATIENT.equals(abnormalData.getIoType())){//门诊处理
				try {
					abnormalData.setState("1");
					Map<String, Object> param =JobCommonUtil.getSerializedMaps(abnormalData.getData());
					List<Map<String, Object>> outpatientParam= (List<Map<String, Object>>) param.get("data");
					outpatientService.singleOutpatientRecordBuild(outpatientParam, param);
				} catch (Exception e) {
					logger.error("门诊异常数据处理失败", e);
					abnormalData.setState("2");
					continue;
				}
			}else if(Constants.PUBLICHEALTH.equals(abnormalData.getIoType())){//公卫和健康树处理
				try {
					abnormalData.setState("1");
				    Map<String, Object> param =JobCommonUtil.getSerializedMaps(abnormalData.getData());
					publicHealthService.personalPublicHealthBuild(param);
				} catch (Exception e) {
					logger.error("公卫和健康树异常数据处理失败", e);
					abnormalData.setState("2");
					continue;
				}
			}else if(Constants.PERSONALINFO.equals(abnormalData.getIoType())){//个人基本信息处理
				try {
					abnormalData.setState("1");
				    List<Map<String, Object>> list =JobCommonUtil.getSerializedLists(abnormalData.getData());
					publicHealthService.personalInfoBuild(list);
				} catch (Exception e) {
					logger.error("个人基本信息异常数据处理失败", e);
					abnormalData.setState("2");
					continue;
				}
			}
		}
		// 修改状态
		zjyLogService.updateJobException(abnormalDataMaps);
		return null;
	}
}
