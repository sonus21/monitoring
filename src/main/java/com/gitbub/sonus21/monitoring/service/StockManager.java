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

package com.gitbub.sonus21.monitoring.service;

import com.gitbub.sonus21.monitoring.aop.MonitoringTimed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StockManager {
  @Autowired private MeterRegistry meterRegistry;
  private List<String> orders = new Vector<>();
  private Counter counter;
  private Gauge gauge;

  @PostConstruct
  public void init() {
    counter =
        Counter.builder("order_created")
            .description("number of orders created")
            .register(meterRegistry);
    gauge =
        Gauge.builder("stock.size", this, StockManager::getNumberOfItems)
            .description("Number of items in stocks")
            .register(meterRegistry);
  }

  public int getNumberOfItems() {
    return orders.size();
  }

  @MonitoringTimed
  public void addItems(List<String> items) {
    orders.addAll(items);
    // measure gauge
    gauge.measure();
  }

  public List<String> getItem(int count) {
    List<String> items = new ArrayList<>(count);
    while (count > 0) {
      try {
        items.add(orders.remove(0));
      } catch (ArrayIndexOutOfBoundsException e) {
        break;
      }
      count -= 1;
    }
    // increase counter
    counter.increment();
    //  measure gauge
    gauge.measure();
    return items;
  }
}
