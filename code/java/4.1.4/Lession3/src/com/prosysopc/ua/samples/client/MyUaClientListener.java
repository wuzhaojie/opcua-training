package com.prosysopc.ua.samples.client;

import com.prosysopc.ua.client.ConnectException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.client.UaClientListener;
import com.prosysopc.ua.stack.application.Session;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.core.PublishRequest;
import com.prosysopc.ua.stack.core.PublishResponse;
import com.prosysopc.ua.stack.core.RepublishResponse;

public class MyUaClientListener implements UaClientListener {

  // One hour in milliseconds
  private static final long ALLOWED_PUBLISHTIME_DIFFERENCE = 3600000;

  // Set to true to accept PublishResponses from the future/past of more than ALLOWED_PUBLISHTIME_DIFFERENCE.
  private static boolean publishTimeOverride = false;

  @Override
  public void onAfterCreateSessionChannel(UaClient client, Session session) throws ConnectException {
  }

  @Override
  public void onBeforePublishRequest(UaClient client, PublishRequest publishRequest) {
    /*
     * Do nothing for now. Saving the request could be implemented here in case a comparison to
     * response from validatePublishResponse is wanted
     */
  }

  @Override
  public boolean validatePublishResponse(UaClient client, PublishResponse response) {
    return validatePublishTime(response.getNotificationMessage().getPublishTime());
  }

  @Override
  public boolean validateRepublishResponse(UaClient client, RepublishResponse response) {
    return validatePublishTime(response.getNotificationMessage().getPublishTime());
  }

  private boolean validatePublishTime(DateTime publishTime) {
    if (publishTimeOverride) {
      return true;
    }

    //If publishTime is too much into past or future, discard the data
    long diff = Math.abs(DateTime.currentTime().getTimeInMillis() - publishTime.getTimeInMillis());
    if ((diff > ALLOWED_PUBLISHTIME_DIFFERENCE) && !publishTime.equals(DateTime.MIN_VALUE)
        && !publishTime.equals(DateTime.MAX_VALUE)) {
      System.out.println(String.format(
          "PublishResponse PublishTime difference to "
              + "current time more than allowed,  discarding data, (%sms vs %sms)",
          diff, ALLOWED_PUBLISHTIME_DIFFERENCE));
      return false;
    }

    //CTT, should check if PublishResponse.getResults contains bad statuscodes
    return true;
  }
}
