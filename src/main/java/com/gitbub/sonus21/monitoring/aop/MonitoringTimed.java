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

package com.gitbub.sonus21.monitoring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitoringTimed {

  /**
   * All fields are same as in {@link io.micrometer.core.annotation.Timed}
   */
  String value() default "";

  String[] extraTags() default {};

  boolean longTask() default false;

  double[] percentiles() default {};

  boolean histogram() default false;

  String description() default "";

  // NEW fields starts here
  boolean loggingEnabled() default false;
}
