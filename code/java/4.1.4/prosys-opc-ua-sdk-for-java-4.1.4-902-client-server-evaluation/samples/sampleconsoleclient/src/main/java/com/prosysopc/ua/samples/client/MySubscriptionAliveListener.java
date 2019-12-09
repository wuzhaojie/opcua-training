/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.client;

import java.util.Calendar;

import com.prosysopc.ua.client.Subscription;
import com.prosysopc.ua.client.SubscriptionAliveListener;

/**
 * A sampler listener for subscription alive events.
 */
public class MySubscriptionAliveListener implements SubscriptionAliveListener {

  @Override
  public void onAfterCreate(Subscription subscription) {
    // the subscription was (re)created to the server
    // this happens if the subscription was timed out during
    // a communication break and had to be recreated after reconnection
    SampleConsoleClient.println(String.format("%tc Subscription created: ID=%d lastAlive=%tc", Calendar.getInstance(),
        subscription.getSubscriptionId().getValue(), subscription.getLastAlive()));
  }

  @Override
  public void onAlive(Subscription subscription) {
    // the server acknowledged that the connection is alive,
    // although there were no changes to send
    SampleConsoleClient.println(String.format("%tc Subscription alive: ID=%d lastAlive=%tc", Calendar.getInstance(),
        subscription.getSubscriptionId().getValue(), subscription.getLastAlive()));
  }

  @Override
  public void onLifetimeTimeout(Subscription subscription) {
    SampleConsoleClient.println(String.format("%tc Subscription lifetime ended: ID=%d lastAlive=%tc",
        Calendar.getInstance(), subscription.getSubscriptionId().getValue(), subscription.getLastAlive()));

  }

  @Override
  public void onTimeout(Subscription subscription) {
    // the server did not acknowledge that the connection is alive, and the
    // maxKeepAliveCount has been exceeded
    SampleConsoleClient.println(String.format("%tc Subscription timeout: ID=%d lastAlive=%tc", Calendar.getInstance(),
        subscription.getSubscriptionId().getValue(), subscription.getLastAlive()));

  }

}
