package com.nube.analytics.collector.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nube.analytics.collector.service.TrackerService;
import com.nube.core.dao.analytics.TrackerDao;
import com.nube.core.exception.NubeException;
import com.nube.core.util.date.DateUtil;
import com.nube.core.util.string.StringUtil;
import com.nube.core.vo.analytics.Tracker;

@Service
public class TrackerServiceImpl implements TrackerService {

	static Logger logger = Logger.getLogger(TrackerServiceImpl.class);

	@Autowired
	TrackerDao trackerDao;

	public void insert(Tracker tracker) throws NubeException {
		if(this.validate(tracker)){
			trackerDao.insert(tracker);
		}else{
			logger.debug("nothing to save");
		}
	}
	
	private boolean validate(Tracker t){
		/**
		 * Nothing valuable to save
		 */
		if(StringUtil.isEmpty(t.getAid()) ||
		   StringUtil.isEmpty(t.getSid())){
			return false;
		}else{
			t.setDt(DateUtil.getSysDateString());
		}
		
		return true;
	}

	

}
