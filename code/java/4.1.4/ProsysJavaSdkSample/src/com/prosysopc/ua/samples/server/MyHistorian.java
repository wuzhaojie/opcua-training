/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.AggregateCalculator;
import com.prosysopc.ua.AggregateCalculator.AggregateCalculatorContinuationPoint;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.HistoryContinuationPoint;
import com.prosysopc.ua.server.HistoryManagerListener;
import com.prosysopc.ua.server.HistoryResult;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.server.nodes.UaObjectNode;
import com.prosysopc.ua.server.nodes.UaVariableNode;
import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.AggregateConfiguration;
import com.prosysopc.ua.stack.core.EventFilter;
import com.prosysopc.ua.stack.core.EventNotifierType;
import com.prosysopc.ua.stack.core.HistoryData;
import com.prosysopc.ua.stack.core.HistoryEvent;
import com.prosysopc.ua.stack.core.HistoryEventFieldList;
import com.prosysopc.ua.stack.core.HistoryModifiedData;
import com.prosysopc.ua.stack.core.HistoryReadDetails;
import com.prosysopc.ua.stack.core.HistoryReadValueId;
import com.prosysopc.ua.stack.core.HistoryUpdateDetails;
import com.prosysopc.ua.stack.core.HistoryUpdateResult;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.PerformUpdateType;
import com.prosysopc.ua.stack.core.StatusCodes;
import com.prosysopc.ua.stack.core.TimestampsToReturn;
import com.prosysopc.ua.stack.utils.NumericRange;
import com.prosysopc.ua.types.opcua.HistoricalDataConfigurationType;

/**
 * A sample implementation of a data historian.
 * <p>
 * It is implemented as a HistoryManagerListener. It could as well be a HistoryManager, instead.
 */
public class MyHistorian implements HistoryManagerListener {
  private static Logger logger = LoggerFactory.getLogger(MyHistorian.class);
  private final Map<UaObjectNode, EventHistory> eventHistories = new HashMap<UaObjectNode, EventHistory>();

  // The variable histories
  private final Map<UaVariableNode, ValueHistory> variableHistories = new HashMap<UaVariableNode, ValueHistory>();
  private final AggregateCalculator aggregateCalculator;

  public MyHistorian(AggregateCalculator aggregateCalculator) {
    this.aggregateCalculator = aggregateCalculator;
  }

  /**
   * Add the object to the historian for event history.
   * <p>
   * The historian will mark it to contain history (in EventNotifier attribute) and it will start
   * monitoring events for it.
   *
   * @param node the object to initialize
   */
  public void addEventHistory(UaObjectNode node) {
    EventHistory history = new EventHistory(node);
    // History can be read
    Set<EventNotifierType.Fields> eventNotifier = node.getEventNotifier().toSet();
    eventNotifier.add(EventNotifierType.Fields.HistoryRead);
    node.setEventNotifier(EventNotifierType.of(eventNotifier));
    eventHistories.put(node, history);
  }

  /**
   * Add the variable to the historian.
   * <p>
   * The historian will mark it to be historized and it will start monitoring value changes for it.
   *
   * @param variable the variable to initialize
   */
  public void addVariableHistory(UaVariableNode variable) {
    ValueHistory history = new ValueHistory(variable);
    // History is being collected
    variable.setHistorizing(true);
    // History can be read
    variable.setAccessLevel(
        AccessLevelType.of(AccessLevelType.CurrentRead, AccessLevelType.CurrentWrite, AccessLevelType.HistoryRead));
    NodeId nodeId =
        new NodeId(variable.getNodeId().getNamespaceIndex(), variable.getNodeId().getValue() + "HAConfiguration");
    HistoricalDataConfigurationType historicalDataConf =
        variable.getNodeManager().createInstance(HistoricalDataConfigurationType.class, "HA Configuration", nodeId);
    try {
      historicalDataConf.setStepped(history.isStepped());
    } catch (StatusException e) {
      throw new RuntimeException(e);
    }
    variable.addReference(historicalDataConf, Identifiers.HasHistoricalConfiguration, false);

    variableHistories.put(variable, history);
  }

  @Override
  public Object onBeginHistoryRead(ServiceContext serviceContext, HistoryReadDetails details,
      TimestampsToReturn timestampsToReturn, HistoryReadValueId[] nodesToRead,
      HistoryContinuationPoint[] continuationPoints, HistoryResult[] results) throws ServiceException {
    return null;
  }

  @Override
  public Object onBeginHistoryUpdate(ServiceContext serviceContext, HistoryUpdateDetails[] details,
      HistoryUpdateResult[] results, DiagnosticInfo[] diagnosticInfos) throws ServiceException {
    return null;
  }

  @Override
  public void onDeleteAtTimes(ServiceContext serviceContext, Object operationContext, NodeId nodeId, UaNode node,
      DateTime[] reqTimes, StatusCode[] operationResults, DiagnosticInfo[] operationDiagnostics)
      throws StatusException {
    ValueHistory history = variableHistories.get(node);
    if (history != null) {
      history.deleteAtTimes(reqTimes, operationResults, operationDiagnostics);
    } else {
      throw new StatusException(StatusCodes.Bad_NoData);
    }
  }

  @Override
  public void onDeleteEvents(ServiceContext serviceContext, Object operationContext, NodeId nodeId, UaNode node,
      ByteString[] eventIds, StatusCode[] operationResults, DiagnosticInfo[] operationDiagnostics)
      throws StatusException {
    EventHistory history = eventHistories.get(node);
    if (history != null) {
      history.deleteEvents(eventIds, operationResults, operationDiagnostics);
    } else {
      throw new StatusException(StatusCodes.Bad_NoData);
    }
  }

  @Override
  public void onDeleteModified(ServiceContext serviceContext, Object operationContext, NodeId nodeId, UaNode node,
      DateTime startTime, DateTime endTime) throws StatusException {
    throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
  }

  @Override
  public void onDeleteRaw(ServiceContext serviceContext, Object operationContext, NodeId nodeId, UaNode node,
      DateTime startTime, DateTime endTime) throws StatusException {
    ValueHistory history = variableHistories.get(node);
    if (history != null) {
      history.deleteRaw(startTime, endTime);
    } else {
      throw new StatusException(StatusCodes.Bad_NoData);
    }
  }

  @Override
  public void onEndHistoryRead(ServiceContext serviceContext, Object operationContext, HistoryReadDetails details,
      TimestampsToReturn timestampsToReturn, HistoryReadValueId[] nodesToRead,
      HistoryContinuationPoint[] continuationPoints, HistoryResult[] results) throws ServiceException {}

  @Override
  public void onEndHistoryUpdate(ServiceContext serviceContext, Object operationContext, HistoryUpdateDetails[] details,
      HistoryUpdateResult[] results, DiagnosticInfo[] diagnosticInfos) throws ServiceException {}

  @Override
  public Object onReadAtTimes(ServiceContext serviceContext, Object operationContext,
      TimestampsToReturn timestampsToReturn, NodeId nodeId, UaNode node, Object continuationPoint, DateTime[] reqTimes,
      Boolean useSimpleBounds, NumericRange indexRange, HistoryData historyData) throws StatusException {
    if (logger.isDebugEnabled()) {
      logger.debug("onReadAtTimes: reqTimes=[" + reqTimes.length + "] "
          + ((reqTimes.length < 20) ? Arrays.toString(reqTimes) : ""));
    }
    ValueHistory history = variableHistories.get(node);
    if (history != null) {
      historyData.setDataValues(history.readAtTimes(reqTimes));
    } else {
      throw new StatusException(StatusCodes.Bad_NoData);
    }
    return null;
  }

  @Override
  public Object onReadEvents(ServiceContext serviceContext, Object operationContext, NodeId nodeId, UaNode node,
      Object continuationPoint, DateTime startTime, DateTime endTime, UnsignedInteger numValuesPerNode,
      EventFilter filter, HistoryEvent historyEvent) throws StatusException {
    EventHistory history = eventHistories.get(node);
    if (history != null) {
      List<HistoryEventFieldList> events = new ArrayList<HistoryEventFieldList>();
      int firstIndex = continuationPoint == null ? 0 : (Integer) continuationPoint;
      Integer newContinuationPoint =
          history.readEvents(startTime, endTime, numValuesPerNode.intValue(), filter, events, firstIndex);
      historyEvent.setEvents(events.toArray(new HistoryEventFieldList[events.size()]));
      return newContinuationPoint;
    } else {
      throw new StatusException(StatusCodes.Bad_NoData);
    }
  }

  @Override
  public Object onReadModified(ServiceContext serviceContext, Object operationContext,
      TimestampsToReturn timestampsToReturn, NodeId nodeId, UaNode node, Object continuationPoint, DateTime startTime,
      DateTime endTime, UnsignedInteger numValuesPerNode, NumericRange indexRange, HistoryModifiedData historyData)
      throws StatusException {
    throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
  }

  @Override
  public Object onReadProcessed(ServiceContext serviceContext, Object operationContext,
      TimestampsToReturn timestampsToReturn, NodeId nodeId, UaNode node, Object continuationPoint, DateTime startTime,
      DateTime endTime, Double processingInterval, NodeId aggregateType, AggregateConfiguration aggregateConfiguration,
      NumericRange indexRange, HistoryData aggregateData) throws StatusException {
    logger.debug("onReadProcessed: nodeId={}, startTime={}, endtime={}, processingInterval={}", nodeId, startTime,
        endTime, processingInterval);
    if (continuationPoint != null) {
      AggregateCalculator.AggregateCalculatorContinuationPoint aggregateContinuationPoint =
          (AggregateCalculator.AggregateCalculatorContinuationPoint) continuationPoint;
      logger.debug("continuationPoint: processingIntervalIndex={}, lastValue={}",
          aggregateContinuationPoint.getProcessingIntervalIndex(),
          aggregateContinuationPoint.getCachedOutsideRawValue());
    }
    if (!timestampsToReturn.equals(TimestampsToReturn.Source)) {
      throw new StatusException(StatusCodes.Bad_TimestampsToReturnInvalid);
    } else if (!AggregateCalculator.percentValuesAreValid(aggregateConfiguration)) {
      throw new StatusException(StatusCodes.Bad_AggregateInvalidInputs);
    }
    HistoricalDataConfigurationType historicalDataConf = (HistoricalDataConfigurationType) node
        .getReference(Identifiers.HasHistoricalConfiguration, false).getTargetNode();
    // Assume stepped interpolation by default
    Boolean useSteppedInterpolation = historicalDataConf == null ? true : historicalDataConf.isStepped();
    ValueHistory history = variableHistories.get(node);
    if (history != null) {
      AggregateCalculatorContinuationPoint newContinuationPoint = aggregateCalculator.calculateAggregates(startTime,
          endTime, processingInterval, aggregateType, aggregateConfiguration, useSteppedInterpolation, history,
          (AggregateCalculatorContinuationPoint) continuationPoint, aggregateData);
      for (DataValue aggregateValue : aggregateData.getDataValues()) {
        logger.debug("aggregateData: DataValue={}", aggregateValue);
      }
      return newContinuationPoint;
    }
    return null;
  }

  @Override
  public Object onReadRaw(ServiceContext serviceContext, Object operationContext, TimestampsToReturn timestampsToReturn,
      NodeId nodeId, UaNode node, Object continuationPoint, DateTime startTime, DateTime endTime,
      UnsignedInteger numValuesPerNode, Boolean returnBounds, NumericRange indexRange, HistoryData historyData)
      throws StatusException {
    logger.debug("onReadRaw: startTime={} endTime={} numValuesPerNode={}", startTime, endTime, numValuesPerNode);
    ValueHistory history = variableHistories.get(node);
    if (history != null) {
      List<DataValue> values = new ArrayList<DataValue>();
      int firstIndex = continuationPoint == null ? 0 : (Integer) continuationPoint;
      Integer newContinuationPoint =
          history.readRaw(startTime, endTime, numValuesPerNode.intValue(), returnBounds, firstIndex, values);
      historyData.setDataValues(values.toArray(new DataValue[values.size()]));

      return newContinuationPoint;

    }
    return null;
  }

  @Override
  public void onUpdateData(ServiceContext serviceContext, Object operationContext, NodeId nodeId, UaNode node,
      DataValue[] updateValues, PerformUpdateType performInsertReplace, StatusCode[] operationResults,
      DiagnosticInfo[] operationDiagnostics) throws StatusException {
    throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
  }

  @Override
  public void onUpdateEvent(ServiceContext serviceContext, Object operationContext, NodeId nodeId, UaNode node,
      Variant[] eventFields, EventFilter filter, PerformUpdateType performInsertReplace, StatusCode[] operationResults,
      DiagnosticInfo[] operationDiagnostics) throws StatusException {
    throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
  }

  @Override
  public void onUpdateStructureData(ServiceContext serviceContext, Object operationContext, NodeId nodeId, UaNode node,
      DataValue[] updateValues, PerformUpdateType performUpdateType, StatusCode[] operationResults,
      DiagnosticInfo[] operationDiagnostics) throws StatusException {
    throw new StatusException(StatusCodes.Bad_HistoryOperationUnsupported);
  }

}
