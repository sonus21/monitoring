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

package com.gitbub.sonus21.monitoring.profiler;

import com.athaydes.javanna.Javanna;
import com.gitbub.sonus21.monitoring.aop.MonitoringTimed;
import com.gitbub.sonus21.monitoring.aop.MonitoringTimedAspect;
import java.util.HashMap;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerProfiler {

  private static Map<String, Object> timedAnnotationData = new HashMap<>();

  static {
    // use percentile data of p90, p95, p99.99
    double[] percentiles = {0.90, 0.95, 0.9999};
    // set histogram to true
    timedAnnotationData.put("histogram", true);
    // set percentile
    timedAnnotationData.put("percentiles", percentiles);
  }

  @Autowired
  private MonitoringTimedAspect timedAspect;

  private static final MonitoringTimed timed =
      Javanna.createAnnotation(MonitoringTimed.class, timedAnnotationData);

  private static final Logger logger = LoggerFactory.getLogger(ControllerProfiler.class);

  @Pointcut("execution(* com.gitbub.sonus21.monitoring.controller..*.*(..))")
  public void controller() {
  }

  @Around("controller()")
  public Object profile(ProceedingJoinPoint pjp) throws Throwable {
    // here add other logic like error happen then log parameters etc
    return timedAspect.timeThisMethod(pjp, timed);
  }
}
