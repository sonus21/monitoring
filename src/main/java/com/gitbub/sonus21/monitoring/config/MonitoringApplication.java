/*
 * Copyright (c) Sonu Kumar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
