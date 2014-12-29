package com.nube.analytics.collector.controller;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nube.analytics.collector.service.TrackerService;
import com.nube.core.exception.NubeException;
import com.nube.core.util.http.HttpUtil;
import com.nube.core.util.string.StringUtil;
import com.nube.core.vo.analytics.Tracker;
import com.nube.core.vo.system.Property;

/**
 * All it does is take bunch of tracking parameter and save it in to mongodb
 * ..No validation happens at this level
 * 
 * @author kamoorr
 *
 */
@Controller
@RequestMapping("/v1/ana")
public class TrackerController {

	static Logger logger = Logger.getLogger(TrackerController.class);
	
	//5 years 
	static int cookieAge = 5*365*24*60*60;
	
	byte[] returnBytes;
	

	@Autowired
	TrackerService trackerService;

	@PostConstruct
	public void postConstruct() throws IOException{
		logger.info("traffic tracker api initialized");
		returnBytes = get1x1PixelImage();
	}

	/**
	 * Create a new key val pair in a context, Content-Type should be
	 * application/json. Request Body should map to Property.java Request
	 * Method: POST
	 * 
	 * @see Property
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/nube.png", method = RequestMethod.GET,  produces = org.springframework.http.MediaType.IMAGE_PNG_VALUE)
	public @ResponseBody byte[]  create(
			@ModelAttribute Tracker prop, HttpServletRequest request, HttpServletResponse response) {
		logger.info("track ");
		try {
			//TODO: This needs to be async
			this.extractRequestParam(prop, request, response);
			trackerService.insert(prop);
			
		} catch (NubeException nbe) {
			logger.error("error tracking", nbe);
		}
		
		return returnBytes;

	}
	
	private void extractRequestParam(Tracker prop, HttpServletRequest request, HttpServletResponse res){
		//Device Id, this is to track if user is returning or new 
		String did = HttpUtil.getCookie(request, Tracker.KEY_DID);
		
		if(did ==  null){
				//create new id
				did = StringUtil.random(8);
				HttpUtil.setCookie(res, Tracker.KEY_DID, did, cookieAge);
				prop.setUb(Tracker.UB_NEW);
		}else{
				prop.setUb(Tracker.UB_RETURNING);
		}
		//Session Id
		String sessId = HttpUtil.getCookie(request, Tracker.KEY_SID);
		if(sessId ==  null){
			sessId = StringUtil.random(8);
			HttpUtil.setCookie(res, Tracker.KEY_SID, sessId, -1);
			prop.setSessBegin(true);
		}
		
		prop.setDid(did);
		prop.setSid(sessId);
		prop.setIp(request.getLocalAddr());
		prop.setUa(HttpUtil.getUserAgent(request));
		prop.setLoc(HttpUtil.getLocale(request));
	}
	
	
	
	public static byte[] get1x1PixelImage() throws IOException{

	    // The following code was used to generate the tracking pixel.
	    BufferedImage singlePixelImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
	    Color transparent = new Color(0, 0, 0, 0);
	    singlePixelImage.setRGB(0, 0, transparent.getRGB());
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(singlePixelImage, "png", baos);
	    byte[] imageInBytes = baos.toByteArray();
	    baos.close();
	    return imageInBytes;
	}
	
	
	


}
