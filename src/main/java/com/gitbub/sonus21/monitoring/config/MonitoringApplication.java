package com.gitbub.sonus21.monitoring.config;

import com.gitbub.sonus21.monitoring.aop.MonitoringTimedAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(
    scanBasePackages = {
      "com.gitbub.sonus21.monitoring.controller",
      "com.gitbub.sonus21.monitoring.profiler",
      "com.gitbub.sonus21.monitoring.service",
    })
@Slf4j
public class MonitoringApplication {

  public static void main(String[] args) {
    SpringApplication.run(MonitoringApplication.class, args);
  }

  @Bean
  public MonitoringTimedAspect timedAspect() {
    return new MonitoringTimedAspect();
  }
}
