package com.nube.analytics.distributor.service;

import java.util.List;

import com.nube.core.exception.NubeException;
import com.nube.core.vo.analytics.ActivitySummary;

public interface AnalyticsService {

	
	public List<ActivitySummary> getDailySummary(String appId) throws NubeException;
	
	public List<ActivitySummary> getMonthlySummary(String appId) throws NubeException;

	
	
}
