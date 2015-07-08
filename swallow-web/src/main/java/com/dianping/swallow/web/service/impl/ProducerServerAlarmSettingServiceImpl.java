package com.dianping.swallow.web.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.swallow.web.dao.ProducerServerAlarmSettingDao;
import com.dianping.swallow.web.model.alarm.ProducerServerAlarmSetting;
import com.dianping.swallow.web.service.ProducerServerAlarmSettingService;

@Service("producerServerAlarmSettingService")
public class ProducerServerAlarmSettingServiceImpl implements ProducerServerAlarmSettingService {

	@Autowired
	private ProducerServerAlarmSettingDao producerServerAlarmSettingDao;
	
	@Override
	public boolean insert(ProducerServerAlarmSetting setting) {
		return producerServerAlarmSettingDao.insert(setting);
	}

	@Override
	public boolean update(ProducerServerAlarmSetting setting) {
		return producerServerAlarmSettingDao.update(setting);
	}

	@Override
	public int deleteById(String id) {
		return producerServerAlarmSettingDao.deleteById(id);
	}

	@Override
	public ProducerServerAlarmSetting findById(String id) {
		return producerServerAlarmSettingDao.findById(id);
	}

	@Override
	public List<ProducerServerAlarmSetting> findAll() {
		return producerServerAlarmSettingDao.findAll();
	}

}