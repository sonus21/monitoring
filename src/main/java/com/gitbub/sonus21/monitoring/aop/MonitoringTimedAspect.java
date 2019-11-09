/*
 * Copyright (c)  Sonu Kumar
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

package com.gitbub.sonus21.monitoring.aop;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.LongTaskTimer.Sample;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
@Slf4j
public class MonitoringTimedAspect {
  /** Taken from {@link io.micrometer.core.aop.TimedAspect} */
  public static final String DEFAULT_METRIC_NAME = "method.timed";

  public static final String EXCEPTION_TAG = "exception";
  private final MeterRegistry registry;
  private final Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinPoint;

  public MonitoringTimedAspect() {
    this(Metrics.globalRegistry);
  }

  public MonitoringTimedAspect(MeterRegistry registry) {
    this(
        registry,
        pjp ->
            Tags.of(
                "class",
                pjp.getStaticPart().getSignature().getDeclaringTypeName(),
                "method",
                pjp.getStaticPart().getSignature().getName()));
  }

  public MonitoringTimedAspect(
      MeterRegistry registry, Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinPoint) {
    this.registry = registry;
    this.tagsBasedOnJoinPoint = tagsBasedOnJoinPoint;
  }

  private Object processWithTimer(ProceedingJoinPoint pjp, MonitoringTimed timed, String metricName)
      throws Throwable {
    Timer.Sample sample = Timer.start(registry);
    String exceptionClass = "none";

    try {
      return pjp.proceed();
    } catch (Exception ex) {
      exceptionClass = ex.getClass().getSimpleName();
      throw ex;
    } finally {
      try {
        sample.stop(
            Timer.builder(metricName)
                .description(timed.description().isEmpty() ? null : timed.description())
                .tags(timed.extraTags())
                .tags(EXCEPTION_TAG, exceptionClass)
                .tags(tagsBasedOnJoinPoint.apply(pjp))
                .publishPercentileHistogram(timed.histogram())
                .publishPercentiles(timed.percentiles().length == 0 ? null : timed.percentiles())
                .register(registry));
      } catch (Exception e) {
        // ignoring on purpose
      }
    }
  }

  private Object processWithLongTaskTimer(
      ProceedingJoinPoint pjp, MonitoringTimed timed, String metricName) throws Throwable {

    Optional<Sample> sample = buildLongTaskTimer(pjp, timed, metricName).map(LongTaskTimer::start);

    try {
      return pjp.proceed();
    } finally {
      try {
        sample.ifPresent(LongTaskTimer.Sample::stop);
      } catch (Exception e) {
        // ignoring on purpose
      }
    }
  }

  /**
   * Secure long task timer creation - it should not disrupt the application flow in case of
   * exception
   */
  private Optional<LongTaskTimer> buildLongTaskTimer(
      ProceedingJoinPoint pjp, MonitoringTimed timed, String metricName) {
    try {
      return Optional.of(
          LongTaskTimer.builder(metricName)
              .description(timed.description().isEmpty() ? null : timed.description())
              .tags(timed.extraTags())
              .tags(tagsBasedOnJoinPoint.apply(pjp))
              .register(registry));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  // ============================= TimedAspect code ends here =======================
  private Object timeThisMethod(ProceedingJoinPoint pjp, MonitoringTimed timed, String metricName)
      throws Throwable {
    Object response;
    if (!timed.longTask()) {
      response = processWithTimer(pjp, timed, metricName);
    } else {
      response = processWithLongTaskTimer(pjp, timed, metricName);
    }
    if (timed.loggingEnabled()) {
      log.info("{} {}", response, pjp.getArgs());
    }
    return response;
  }

  private String generateMetricName(ProceedingJoinPoint pjp, MonitoringTimed timed) {
    if (!timed.value().isEmpty()) {
      return timed.value();
    }
    return DEFAULT_METRIC_NAME;
  }

  @Around("execution (@com.gitbub.sonus21.monitoring.aop.MonitoringTimed * *.*(..))")
  public Object timedMethod(ProceedingJoinPoint pjp) throws Throwable {
    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
    MonitoringTimed timed = method.getAnnotation(MonitoringTimed.class);
    if (timed == null) {
      method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
      timed = method.getAnnotation(MonitoringTimed.class);
    }
    final String metricName = generateMetricName(pjp, timed);
    return timeThisMethod(pjp, timed, metricName);
  }

  public Object timeThisMethod(ProceedingJoinPoint pjp, MonitoringTimed timed) throws Throwable {
    final String metricName = generateMetricName(pjp, timed);
    return timeThisMethod(pjp, timed, metricName);
  }
}
