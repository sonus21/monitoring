package com.gitbub.sonus21.monitoring.config;

import com.gitbub.sonus21.monitoring.config.MonitoringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(MonitoringApplication.class);
	}

}
