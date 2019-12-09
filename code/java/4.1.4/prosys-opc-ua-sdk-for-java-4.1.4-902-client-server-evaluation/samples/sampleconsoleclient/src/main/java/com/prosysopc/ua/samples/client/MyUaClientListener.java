/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.client;

import com.prosysopc.ua.client.ConnectException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.client.UaClientListener;
import com.prosysopc.ua.stack.application.Session;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.core.MessageSecurityMode;
import com.prosysopc.ua.stack.core.PublishRequest;
import com.prosysopc.ua.stack.core.PublishResponse;
import com.prosysopc.ua.stack.core.RepublishResponse;

public class MyUaClientListener implements UaClientListener {

  // One hour in milliseconds
  private static final long ALLOWED_PUBLISHTIME_DIFFERENCE = 3600000;

  /**
   * Set to true to accept PublishResponses from the future/past of more than
   * ALLOWED_PUBLISHTIME_DIFFERENCE.
   */
  private static boolean publishTimeOverride = false;

  /**
   * Unrealistically long session timeout, e.g. one day, in milliseconds.
   */
  private static final double UNREALISTIC_LONG_TIMEOUT = 86400000;

  /**
   * Unrealistically short session timeout, 10 seconds, in milliseconds.
   */
  private static final double UNREALISTIC_SHORT_TIMEOUT = 10000;

  /*
   * (non-Javadoc)
   *
   * @see com.prosysopc.ua.client.UaClientListener#onAfterCreateSessionChannel(
   * com.prosysopc.ua.client.UaClient)
   */
  @Override
  public void onAfterCreateSessionChannel(UaClient client, Session session) throws ConnectException {
    Session s = client.getSession();
    if (s.getSessionTimeout() <= UNREALISTIC_SHORT_TIMEOUT) {
      pl("The RevisedSessionTimeout is unrealistically short: " + s.getSessionTimeout()
          + " ms. Do you still want to connect? y=Yes, anything else is No");
      String input = SampleConsoleClient.readInput(false).toLowerCase();
      if (!input.equals("y")) {
        throw new ConnectException("Canceled by user", "", null);
      }
    }

    if (s.getSessionTimeout() >= UNREALISTIC_LONG_TIMEOUT) {
      pl("The RevisedSessionTimeout is unrealistically long: " + s.getSessionTimeout()
          + " ms. Do you still want to connect? y=Yes, anything else is No");
      String input = SampleConsoleClient.readInput(false).toLowerCase();
      if (!input.equals("y")) {
        throw new ConnectException("Canceled by user", "", null);
      }
    }

    if ((s.getServerNonce() == null) || (s.getServerNonce().length < 32)) {
      if (MessageSecurityMode.None != client.getSecurityMode().getMessageSecurityMode()) {
        pl("The serverNonce is less than 32 bytes, Do you still want to connect? y=Yes, anything else is No");
        String input = SampleConsoleClient.readInput(false).toLowerCase();
        if (!input.equals("y")) {
          throw new ConnectException("Canceled by user", "", null);
        }
      }
    }

  }

  /*
   * (non-Javadoc)
   *
   * @see com.prosysopc.ua.client.UaClientListener#onBeforePublishRequest(org.
   * opcfoundation.ua.core.PublishRequest)
   */
  @Override
  public void onBeforePublishRequest(UaClient client, PublishRequest publishRequest) {
    /*
     * Do nothing for now. Saving the request could be implemented here in case a comparison to
     * response from validatePublishResponse is wanted
     */
  }

  /*
   * (non-Javadoc)
   *
   * @see com.prosysopc.ua.client.UaClientListener#validatePublishResponse(org.
   * opcfoundation.ua.core.PublishResponse)
   */
  @Override
  public boolean validatePublishResponse(UaClient client, PublishResponse response) {
    return validatePublishTime(response.getNotificationMessage().getPublishTime());
  }

  @Override
  public boolean validateRepublishResponse(UaClient client, RepublishResponse response) {
    return validatePublishTime(response.getNotificationMessage().getPublishTime());
  }

  /*
   * (non-Javadoc)
   *
   * @see com.prosysopc.ua.client.UaClientListener#validateRePublishResponse(org
   * .opcfoundation.ua.core.RepublishResponse)
   */
  private void pl(String line) {
    SampleConsoleClient.println(line);
  }

  private boolean validatePublishTime(DateTime publishTime) {
    if (publishTimeOverride) {
      return true;
    }

    /*
     * If publishTime is too much into past or future, discard the data
     */
    long diff = Math.abs(DateTime.currentTime().getTimeInMillis() - publishTime.getTimeInMillis());
    if ((diff > ALLOWED_PUBLISHTIME_DIFFERENCE) && !publishTime.equals(DateTime.MIN_VALUE)
        && !publishTime.equals(DateTime.MAX_VALUE)) {
      pl(String.format(
          "PublishResponse PublishTime difference to "
              + "current time more than allowed,  discarding data, (%sms vs %sms)",
          diff, ALLOWED_PUBLISHTIME_DIFFERENCE));
      return false;
    }

    /*
     * CTT, should check if PublishResponse.getResults contains bad statuscodes
     */
    return true;
  }
}
