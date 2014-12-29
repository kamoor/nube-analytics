package com.nube.analytics.common.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.DBCollection;
import com.nube.core.dao.mongo.MongoConnection;
import com.nube.core.exception.NubeException;
import com.nube.core.util.string.StringUtil;
import com.nube.core.vo.analytics.ActivitySummary;
/**
 * Save summary by month.
 * @author kamoorr
 *
 */
@Repository("activitySummaryMonthly")
public class ActivitySummaryDaoMonthlyImpl extends AcitivitySummaryDaoImpl{

	@Autowired
	MongoConnection mongoConnection;

	private static final String COLLECTION = "nana_activity_summary_monthly";
	
	private static final String DATE_FORMAT = "\\d{4}-\\d{2}";

	@Override
	public DBCollection getCollection() {
		return mongoConnection.getCollection(COLLECTION);
	}

	@Override
	public String getDateFormat() {
		return DATE_FORMAT;
	}

	@Override
	public String getTimeFrame() {
		return ActivitySummary.TIME_FRAME_MONTHLY;
	}

}
