/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.core.Range;
import com.prosysopc.ua.stack.core.StatusCodes;
import com.prosysopc.ua.stack.utils.NumericRange;
import com.prosysopc.ua.types.opcua.AnalogItemType;

/**
 * Example how to implement custom behavior by extending a
 * {@link com.prosysopc.ua.server.io.UaTypeIoListener} template.
 */
public class MyAnalogItemListener extends AnalogItemListenerTemplate {
  @Override
  protected boolean onWriteValue(ServiceContext serviceContext, AnalogItemType node, NumericRange indexRange,
      DataValue dataValue) throws StatusException {
    double value = (Double) dataValue.getValue().getValue();
    Range euRangeValue = node.getEURange();
    if ((euRangeValue != null) && ((value < euRangeValue.getLow()) || (value > euRangeValue.getHigh()))) {
      throw new StatusException(StatusCodes.Bad_OutOfRange);
    }
    return false;
  }
}
