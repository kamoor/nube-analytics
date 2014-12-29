package com.nube.analytics.distributor.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.nube.analytics.distributor.service.AnalyticsService;
import com.nube.core.dao.analytics.ActivitySummaryDao;
import com.nube.core.exception.NubeException;
import com.nube.core.vo.analytics.ActivitySummary;

@Service
public class AnalyticsServiceImpl implements AnalyticsService{

	
	@Autowired
	@Qualifier("activitySummaryDaily")
	ActivitySummaryDao activitySummaryDaoDaily;
	
	
	@Autowired
	@Qualifier("activitySummaryMonthly")
	ActivitySummaryDao activitySummaryDaoMonthly;
	
	
	
	
	/**
	 * Read daily summary for an app.
	 */
	public List<ActivitySummary> getDailySummary(String appId)
			throws NubeException {
		return activitySummaryDaoDaily.read(appId);
	}

	/**
	 * Read daily summary for an app.
	 */
	public List<ActivitySummary> getMonthlySummary(String appId)
			throws NubeException {
		return activitySummaryDaoMonthly.read(appId);
	}

	
}
