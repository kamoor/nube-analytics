package com.nube.analytics.server;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class NubeWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter{
	
	Logger logger = Logger.getLogger(NubeWebSecurityConfigurerAdapter.class);
	/**
	 * Disabling basic auth for now
	 */
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		logger.info("Configuring security");
        http.httpBasic().disable();
    }
}
