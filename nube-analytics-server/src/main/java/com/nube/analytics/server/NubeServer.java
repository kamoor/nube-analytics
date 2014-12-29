package com.nube.analytics.server;

import javax.servlet.MultipartConfigElement;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultiPartConfigFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring boot to start server on the fly and run an app. Parameters: --debug to
 * start in debug mode nube-portal-custom.properties: should be used for custom
 * properties
 * 
 * @author kamoorr
 * 
 */
@Configuration
@EnableAutoConfiguration
@PropertySource(value = {"classpath:application.properties", "classpath:nube-analytics.properties"})
@ImportResource(value = {"classpath:spring/*.xml"})
public class NubeServer {

	@Value("${nube.file.upload.maxsize:15MB}")
	private String fileMaxSize;

	static Logger logger = Logger.getLogger(NubeServer.class);

	public static void main(String[] args) {

		System.out.println("Start Nube cloud application manager");
		ApplicationContext ctx = SpringApplication.run(NubeServer.class, args);
	}

	 
	/**
	 * Create a MultiPart config to upload files. set max file size
	 * @return
	 */
	@Bean
	MultipartConfigElement multipartConfigElement() {

		MultiPartConfigFactory factory = new MultiPartConfigFactory();
		factory.setMaxFileSize(fileMaxSize);
		factory.setMaxRequestSize(fileMaxSize);
		return factory.createMultipartConfig();
	}

}
