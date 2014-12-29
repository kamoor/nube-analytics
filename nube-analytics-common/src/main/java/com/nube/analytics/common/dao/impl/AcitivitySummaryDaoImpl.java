package com.nube.analytics.common.dao.impl;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.nube.core.dao.analytics.ActivitySummaryDao;
import com.nube.core.dao.mongo.AbstractMongoDao;
import com.nube.core.exception.NubeException;
import com.nube.core.util.string.StringUtil;
import com.nube.core.vo.analytics.ActivitySummary;
import com.nube.core.vo.analytics.Usage;

/**
 * Manage activitiy summary , monthly, daily , yearly basis This is abstract
 * class
 * 
 * @author kamoorr
 *
 */
public abstract class AcitivitySummaryDaoImpl extends
		AbstractMongoDao<BasicDBObject, ActivitySummary> implements
		ActivitySummaryDao {

	static Logger logger = Logger.getLogger(AcitivitySummaryDaoImpl.class);

	/**
	 * See daily and monthly implementations
	 */
	public abstract DBCollection getCollection();
	
	
	public abstract String getDateFormat();

	
	public abstract String getTimeFrame();

	public void validateDate(String date) throws NubeException {
		if (StringUtil.isEmpty(date) || !date.matches(getDateFormat())) {
			logger.error("Date format for "+ date + " is no good. Expected "+ this.getDateFormat());
			throw new NubeException("wrong_date_format");
		}
	}

	
	/**
	 * Insert summary
	 */
	public void insert(ActivitySummary summary) throws NubeException {

		this.validateDate(summary.getDate());
		getCollection().save(this.serialize(summary));

	}

	/**
	 * update summary
	 */
	public void update(ActivitySummary summary) throws NubeException {

		this.validateDate(summary.getDate());
		getCollection().update(
				new BasicDBObject().append("dt", summary.getDate()).append(
						"appId", summary.getAppId()), this.serialize(summary));

	}

	/**
	 * Read by day or multiple days
	 */
	public List<ActivitySummary> read(String date, String appId)
			throws NubeException {

		this.validateDate(date);
		DBCursor cursor = getCollection()
				.find(new BasicDBObject().append("dt", date).append("appId",
						appId));
		return super.extract(cursor);
	}
	
	/**
	 * Read by app id
	 */
	public List<ActivitySummary> read(String appId)
			throws NubeException {

		DBCursor cursor = getCollection()
				.find(new BasicDBObject().append("appId",
						appId));
		return super.extract(cursor);
	}

	public void delete(String date) throws NubeException {
		getCollection().remove(new BasicDBObject().append("dt", date));

	}

	public ActivitySummary parse(BasicDBObject dbO) {
		ActivitySummary summary = new ActivitySummary(
				this.getTimeFrame(), dbO.getString("dt"),
				dbO.getString("appId"));
		summary.setTotalUsers(dbO.getInt("totalUsrs"));
		summary.setNewUsers(dbO.getInt("newUsrs"));
		summary.setNewUsersPct(dbO.getInt("newUsrsPct"));
		summary.setRetUsers(dbO.getInt("retUsrs"));
		summary.setRetUsersPct(dbO.getDouble("retUsrsPct"));
		summary.setBouncedUsers(dbO.getInt("bounceUsrs"));
		summary.setBouncedUsersPct(dbO.getInt("bounceUsrsPct"));
		summary.setAvgTimeSpent(dbO.getInt("avgTime"));
		BasicDBList osUsageObj = (BasicDBList) dbO.get("osUsg");
		BasicDBList deviceUsageObj = (BasicDBList) dbO.get("deviceUsg");
		BasicDBList refererObj = (BasicDBList) dbO.get("refs");
		BasicDBList browsersObj = (BasicDBList) dbO.get("browsers");
		if (osUsageObj != null) {
			for (Object o : osUsageObj) {
				summary.addOsUsage(((BasicDBObject) o).getString("os"),
						((BasicDBObject) o).getInt("count"),
						((BasicDBObject) o).getDouble("pct"));
			}
		}
		if (deviceUsageObj != null) {
			for (Object o : deviceUsageObj) {
				summary.addDeviceUsage(((BasicDBObject) o).getString("device"),
						((BasicDBObject) o).getInt("count"),
						((BasicDBObject) o).getDouble("pct"));
			}
		}
		if (refererObj != null) {
			for (Object o : refererObj) {
				summary.addReferer(((BasicDBObject) o).getString("ref"),
						((BasicDBObject) o).getInt("count"),
						((BasicDBObject) o).getDouble("pct"));
			}
		}
		if (browsersObj != null) {
			for (Object o : browsersObj) {
				summary.addBrowser(((BasicDBObject) o).getString("browser"),
						((BasicDBObject) o).getInt("count"),
						((BasicDBObject) o).getDouble("pct"));
			}
		}
		return summary;
	}

	public BasicDBObject serialize(ActivitySummary pojo) {
		
		BasicDBObject dbObject = new BasicDBObject()
				.append("dt", pojo.getDate())
				.append("appId", pojo.getAppId())
				.append("totalUsrs", pojo.getTotalUsers())
				.append("newUsrs", pojo.getNewUsers())
				.append("newUsrsPct", pojo.getNewUsersPct())
				.append("retUsrs", pojo.getRetUsers())
				.append("retUsrsPct", pojo.getRetUsersPct())
				.append("bounceUsrs", pojo.getBouncedUsers())
				.append("bounceUsrsPct", pojo.getBouncedUsersPct())
				.append("avgTime", pojo.getAvgTimeSpent())
				.append("updatedDt", new Date());
		
		BasicDBList osUsageList = new BasicDBList();
		
		for (Usage u : pojo.getOs()) {
			osUsageList.add(new BasicDBObject().append("os", u.getType())
					.append("count", u.getCount()).append("pct", u.getPct()));
		}

		BasicDBList deviceUsageList = new BasicDBList();
		
		for (Usage u : pojo.getDevices()) {
			deviceUsageList.add(new BasicDBObject()
					.append("device", u.getType())
					.append("count", u.getCount()).append("pct", u.getPct()));
		}

		BasicDBList refList = new BasicDBList();
		
		for (Usage u : pojo.getReferer()) {
			refList.add(new BasicDBObject().append("ref", u.getType())
					.append("count", u.getCount()).append("pct", u.getPct()));
		}
		
		BasicDBList browserList = new BasicDBList();
		
		for (Usage u : pojo.getBrowsers()) {
			browserList.add(new BasicDBObject().append("browser", u.getType())
					.append("count", u.getCount()).append("pct", u.getPct()));
		}

		dbObject.append("osUsg", osUsageList);
		dbObject.append("deviceUsg", deviceUsageList);
		dbObject.append("refs", refList);
		dbObject.append("browsers", browserList);

		return dbObject;
	}

}
