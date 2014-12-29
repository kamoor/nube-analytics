package com.nube.analytics.common.dao.impl;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.nube.core.dao.analytics.TrackerDao;
import com.nube.core.dao.mongo.AbstractMongoDao;
import com.nube.core.dao.mongo.MongoConnection;
import com.nube.core.exception.NubeException;
import com.nube.core.vo.analytics.Tracker;

/**
 * Manage tracking info
 * 
 * @author kamoorr
 *
 */
@Repository
@Profile("default")
public class TrackerDaoImpl extends AbstractMongoDao<BasicDBObject, Tracker> implements TrackerDao{

	@Autowired
	MongoConnection mongoConnection;

	private static final String COLLECTION = "nana_collector";
	
	private static long timeDelay = AGE_TO_REFINE_MINS*60*1000;

	static Logger logger = Logger.getLogger(TrackerDaoImpl.class);

	public DBCollection getCollection() {
		return mongoConnection.getCollection(COLLECTION);
	}

	/**
	 * Insert a tracker info
	 */
	public void insert(Tracker tracker) throws NubeException {
		getCollection().save(this.serialize(tracker));

	}

	/**
	 * Update tracker info
	 */
	public void update(Tracker tracker) throws NubeException {
		throw new NubeException("unimplemented");

	}

	
	public List<Tracker> readAll(String appId) throws NubeException{
		DBCursor cursor = getCollection().find(new BasicDBObject().append("aid", appId));
		cursor.batchSize(MAX_READ_COUNT);
		return super.extract(cursor);
	}
	
	

	public List<Tracker> readFew() throws NubeException {
		DBCursor cursor = getCollection().find(
					new BasicDBObject().append("dt", new BasicDBObject("$lt", new Date(System.currentTimeMillis() - timeDelay))));
		cursor.batchSize(MAX_READ_COUNT);
		return super.extract(cursor);
	}

	public List<Tracker> readSession(String sessionId) throws NubeException {
		return super.extract(getCollection().find(new BasicDBObject().append("sid", sessionId)));
	}

	public void deleteSession(String sessionId) throws NubeException {
		getCollection().remove(new BasicDBObject().append("sid", sessionId));
		
	}

	public void delete(String tid) throws NubeException {
		getCollection().remove(new BasicDBObject().append("tid", tid));
	}
	
	public Tracker parse(BasicDBObject dbObject) {
		Tracker track = new Tracker();
		track.setTid(dbObject.getString("tid"));
		track.setAid(dbObject.getString("aid"));
		track.setSid(dbObject.getString("sid"));
		track.setDid(dbObject.getString("did"));
		track.setIp(dbObject.getString("ip"));
		track.setUa(dbObject.getString("ua"));
		track.setRef(dbObject.getString("ref"));
		track.setLt(dbObject.getString("lt"));
		track.setLg(dbObject.getString("lg"));
		track.setPg(dbObject.getString("pg"));
		track.setDt(dbObject.getString("dt"));
		track.setDate(dbObject.getDate("dt"));
		track.setEv(dbObject.getString("ev"));
		track.setEvsc(dbObject.getString("evsc"));
		track.setIn(dbObject.getString("in"));
		track.setUb(dbObject.getString("ub"));
		track.setSessBegin(dbObject.getBoolean("sessBgn"));
		return track;
	}

	public BasicDBObject serialize(Tracker pojo) {
		BasicDBObject dbObject = new BasicDBObject()
				.append("tid", pojo.getTid()).append("aid", pojo.getAid())
				.append("sid", pojo.getSid()).append("did", pojo.getDid())
				.append("ip", pojo.getIp()).append("ua", pojo.getUa())
				.append("ref", pojo.getRef()).append("loc", pojo.getLoc())
				.append("lt", pojo.getLt()).append("lg", pojo.getLg())
				.append("pg", pojo.getPg()).append("dt", new Date())
				.append("ev", pojo.getEv()).append("evsc", pojo.getEvsc())
				.append("in", pojo.getIn()).append("ub", pojo.getUb())
				.append("sessBgn", pojo.isSessBegin());
		return dbObject;
	}


}
