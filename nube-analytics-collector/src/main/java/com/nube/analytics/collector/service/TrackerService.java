package com.nube.analytics.collector.service;

import com.nube.core.exception.NubeException;
import com.nube.core.vo.analytics.Tracker;

public interface TrackerService {

	/**
	 * Insert tracker
	 * 
	 * @param tracker
	 * @throws NubeException
	 */
	public void insert(Tracker tracker) throws NubeException;
	
	
	
	
	
	
}
