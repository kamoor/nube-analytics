package com.nube.analytics.refinery.steps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
import org.springframework.stereotype.Component;

import com.mongodb.util.Hash;
import com.nube.analytics.common.dao.impl.AcitivitySummaryDaoImpl;
import com.nube.core.dao.analytics.ActivitySummaryDao;
import com.nube.core.dao.analytics.TrackerDao;
import com.nube.core.exception.NubeException;
import com.nube.core.util.collections.CollectionsUtil;
import com.nube.core.util.http.HttpUtil;
import com.nube.core.vo.analytics.ActivitySummary;
import com.nube.core.vo.analytics.Tracker;

import javax.annotation.*;

/**
 * This class required a major rewrite with following features
 * -- move to spring batch later,
 * -- Ability to create yearly or weekly with out code change, most of the code can be reused 
 * -- need option to turn on/off each summary,
 * -- Configuration based purge. (rows older than 2 years etc)
 * @author kamoorr
 *
 */
@Component
public class RefineDailyDataStep {

	static Logger logger = Logger.getLogger(RefineDailyDataStep.class);

	@Autowired
	TrackerDao trackerDao;

	@Autowired()
	@Qualifier("activitySummaryDaily")
	ActivitySummaryDao activitySummaryDailyDao;

	@Autowired()
	@Qualifier("activitySummaryMonthly")
	ActivitySummaryDao activitySummaryMonthlyDao;

	/**
	 * Extract tracking data to DAILY report
	 */
	public void refine() {
		try {
			this.startProcessing();
		} catch (NubeException e) {
			logger.error("Error while refinement - " + e.getMessage());
		}
	}

	private void startProcessing() throws NubeException {

		// Get random records
		List<Tracker> someRecords = trackerDao.readFew();
		// Store all unique sessions and mark as unprocessed
		Map<String, Boolean> sessions = new HashMap<String, Boolean>();
		for (Tracker tracker : someRecords) {
			// logger.info("sid: "+ tracker.getSid());
			sessions.put(tracker.getSid(), false);
		}
		// Now process each sessions convert to meaningful data;
		for (String sessionId : sessions.keySet()) {
			this.refineSession(sessionId);
			sessions.put(sessionId, true);
		}

		// Now delete sessions from tracker
		this.deleteSession(sessions);
		logger.info(sessions.size() + " sessions processed.");
	}

	/**
	 * Refine a single session
	 * 
	 * @param sessionId
	 */
	private void refineSession(String sessionId) throws NubeException {

		List<Tracker> sessionRecords = trackerDao.readSession(sessionId);
		Tracker oneEntry = sessionRecords.get(0);
		String userAgent = oneEntry.getUa();
		String appId = oneEntry.getAid();
		// This needs revisit to adjust to timezone set by app owner. Right now
		// it default to UTC
		String day = new SimpleDateFormat("yyyy-MM-dd").format(oneEntry
				.getDate());

		// start with most recent date
		Date sessionBeginTime = new Date();
		// start with really old date
		Date sessionEndTime = new Date(100000);
		// initialize as bounced.
		boolean isBounced = false;

		String os = HttpUtil.getOsInfo(userAgent);
		String device = HttpUtil.getDeviceInfo(userAgent);
		String browser = HttpUtil.getBrowserName(userAgent);
		String refer = "Not Available";

		boolean isNewVisitor = Tracker.UB_NEW.equals(oneEntry.getUb()) ? true
				: false;

		for (Tracker entry : sessionRecords) {

			// Get start time
			if (entry.getDate().before(sessionBeginTime)) {
				sessionBeginTime = entry.getDate();
			}
			// Get End time.
			if (entry.getDate().after(sessionEndTime)) {
				sessionEndTime = entry.getDate();
			}

		}
		int sessionDuration = (int) ((sessionEndTime.getTime() - sessionBeginTime
				.getTime()) / 1000L);
		if (sessionRecords.size() == 1 || sessionDuration < 3) {
			isBounced = true;
		}

		//daily summary 
		this.adjustDailySummary(day, appId, sessionDuration, isNewVisitor,
				isBounced, browser, device, os, refer);
		logger.info("Daily summary updated.");
		
		//Monthly summary
		this.adjustMonhtlySummary(day, appId, sessionDuration, isNewVisitor,
				isBounced, browser, device, os, refer);
		logger.info("Monthly summary updated.");
	
	}

	/**
	 * Adjust daily report for the day given 
	 * @param day
	 * @param appId
	 * @param sessionDuration
	 * @param isNewVisitor
	 * @param isBounced
	 * @param browser
	 * @param device
	 * @param os
	 * @param refer
	 * @throws NubeException
	 */
	private void adjustDailySummary(String day, String appId,
			int sessionDuration, boolean isNewVisitor, boolean isBounced,
			String browser, String device, String os, String refer)
			throws NubeException {
		// Create pojo.
		ActivitySummary summary = this.getExistingSummary(ActivitySummary.TIME_FRAME_DAILY, day, appId);

		this.adjustSummaryObject(summary, day, appId, sessionDuration, isNewVisitor, isBounced, browser, device, os, refer);

		// logger.info("Done refinement: "+ summary.toString());

		this.insertOrUpdateDaily(summary);

	}
	
	/**
	 * Adjust daily report for the day given 
	 * @param day
	 * @param appId
	 * @param sessionDuration
	 * @param isNewVisitor
	 * @param isBounced
	 * @param browser
	 * @param device
	 * @param os
	 * @param refer
	 * @throws NubeException
	 */
	private void adjustMonhtlySummary(String day, String appId,
			int sessionDuration, boolean isNewVisitor, boolean isBounced,
			String browser, String device, String os, String refer)
			throws NubeException {
		//YYYY-MM
		String month = day.substring(0, 7);
		// Create pojo.
		ActivitySummary summary = this.getExistingSummary(ActivitySummary.TIME_FRAME_MONTHLY, month, appId);

		this.adjustSummaryObject(summary, month, appId, sessionDuration, isNewVisitor, isBounced, browser, device, os, refer);

		// logger.info("Done refinement: "+ summary.toString());

		this.insertOrUpdateMonthly(summary);

	}
	
	/**
	 * Add all numbers/info to object
	 * @param summary
	 * @param day
	 * @param appId
	 * @param sessionDuration
	 * @param isNewVisitor
	 * @param isBounced
	 * @param browser
	 * @param device
	 * @param os
	 * @param refer
	 * @return
	 */
	private ActivitySummary adjustSummaryObject(ActivitySummary summary, String day, String appId,
			int sessionDuration, boolean isNewVisitor, boolean isBounced,
			String browser, String device, String os, String refer){
		// Now manipulate it
		summary.adjustAvgTimeSpent(sessionDuration);

		summary.incrementTotalUsers();

		if (isNewVisitor == true) {
			summary.incrementNewUsers();
		} else {
			summary.incrementRetUsers();
		}

		if (isBounced == true) {
			summary.incrementBouncedUsers();
		}
		// Find all percentage
		summary.adjustPctForAll();
		summary.addBrowser(browser).addDeviceUsage(device).addOsUsage(os)
				.addReferer(refer);
		return summary;
	}

	/**
	 * Create new pojo if not existing, read if existing
	 * 
	 * @param day
	 * @param appId
	 * @return
	 * @throws NubeException
	 */
	private ActivitySummary getExistingSummary(String summaryType, String date, String appId)
			throws NubeException {

		List<ActivitySummary> summary = null;
		
		if(summaryType.equals(ActivitySummary.TIME_FRAME_DAILY)){
			summary = activitySummaryDailyDao.read(date, appId);
		}else{
			summary = activitySummaryMonthlyDao.read(date, appId);
		}
		
		if (CollectionsUtil.isEmpty(summary)) {
			return new ActivitySummary(summaryType, date,
					appId);
		} else {
			return summary.get(0);
		}
	}
	

	private void insertOrUpdateDaily(ActivitySummary summary)
			throws NubeException {
		// First entry
		if (summary.getTotalUsers() == 1) {
			activitySummaryDailyDao.insert(summary);
		} else {
			activitySummaryDailyDao.update(summary);
		}
	}

	private void insertOrUpdateMonthly(ActivitySummary summary)
			throws NubeException {
		// First entry
		if (summary.getTotalUsers() == 1) {
			activitySummaryMonthlyDao.insert(summary);
		} else {
			activitySummaryMonthlyDao.update(summary);
		}
	}
	
	/**
	 * Delete all records for a session
	 * 
	 * @param sessions
	 * @throws NubeException
	 */
	private void deleteSession(Map<String, Boolean> sessions)
			throws NubeException {
		for (String sessionId : sessions.keySet()) {
			trackerDao.deleteSession(sessionId);
		}
	}

}
