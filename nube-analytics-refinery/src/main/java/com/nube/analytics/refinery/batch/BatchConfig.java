package com.nube.analytics.refinery.batch;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.nube.analytics.refinery.steps.RefineDailyDataStep;

/**
 * This needs to move to spring batch
 * @author kamoorr
 *
 */
@Configuration
public class BatchConfig {

	static Logger logger = Logger.getLogger(BatchConfig.class);
	
	@Autowired
	RefineDailyDataStep refineDailyDataStep;
	
	
	/**
	 * Now running every minute
	 */
	@Scheduled(cron = "0 * * * * ?")
	public void refineTrafficData(){
		logger.info("Starting analytics data refinement.");
		refineDailyDataStep.refine();
	}
}
