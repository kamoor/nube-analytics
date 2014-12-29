package com.nube.analytics.distributor.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nube.analytics.distributor.service.AnalyticsService;
import com.nube.core.exception.NubeException;
import com.nube.core.vo.analytics.ActivitySummary;
import com.nube.core.vo.response.Response;
import com.nube.core.vo.response.ValidResponse;

import javax.annotation.*;

/**
 * Return all the analytics summary to for charts 
 * @author kamoorr
 *
 */
@Controller
@RequestMapping("/v1/analytics")
public class AnalyticsController {

	@Autowired
	AnalyticsService analyticsService;

	static Logger logger = Logger.getLogger(AnalyticsController.class);
	
	@PostConstruct
	public void init(){
		logger.info("analytics api initialized");
	}

	/**
	 * Provide daily summary of site usage, Content-Type should be application/json
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/summary/daily", method = RequestMethod.GET, consumes = "application/json")
	public @ResponseBody Response<List<ActivitySummary>> getDailySummary(
			@RequestParam(value = "appId", required = false) String appId,
			final HttpServletRequest request, final HttpServletResponse response) {

		try {

			logger.info(String.format("Daily summary for ", appId));
			List<ActivitySummary> summary = analyticsService
					.getDailySummary(appId);
			return new ValidResponse<List<ActivitySummary>>(summary);

		} catch (NubeException nException) {
			logger.error("Error", nException);
			return new ValidResponse<List<ActivitySummary>>(nException);
		}

	}
	
	
	/**
	 * Provide monthly summary of site usage, Content-Type should be application/json
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/summary/monthly", method = RequestMethod.GET, consumes = "application/json")
	public @ResponseBody Response<List<ActivitySummary>> getMonthlySummary(
			@RequestParam(value = "appId", required = false) String appId,
			final HttpServletRequest request, final HttpServletResponse response) {

		try {

			logger.info(String.format("Mothly summary for ", appId));
			List<ActivitySummary> summary = analyticsService
					.getMonthlySummary(appId);
			return new ValidResponse<List<ActivitySummary>>(summary);

		} catch (NubeException nException) {
			logger.error("Error", nException);
			return new ValidResponse<List<ActivitySummary>>(nException);
		}

	}

}
