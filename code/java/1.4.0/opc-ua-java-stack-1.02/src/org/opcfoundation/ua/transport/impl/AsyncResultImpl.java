/* ========================================================================
 * Copyright (c) 2005-2013 The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Reciprocal Community License ("RCL") Version 1.00
 * 
 * Unless explicitly acquired and licensed from Licensor under another 
 * license, the contents of this file are subject to the Reciprocal 
 * Community License ("RCL") Version 1.00, or subsequent versions as 
 * allowed by the RCL, and You may not copy or use this file in either 
 * source code or executable form, except in compliance with the terms and 
 * conditions of the RCL.
 * 
 * All software distributed under the RCL is provided strictly on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, 
 * AND LICENSOR HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
 * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RCL for specific 
 * language governing rights and limitations under the RCL.
 *
 * The complete license agreement can be found here:
 * http://opcfoundation.org/License/RCL/1.00/
 * ======================================================================*/

package org.opcfoundation.ua.transport.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.StatusCodes;
import org.opcfoundation.ua.transport.AsyncResult;
import org.opcfoundation.ua.transport.ResultListener;
import org.opcfoundation.ua.utils.StackUtils;

public class AsyncResultImpl<T> implements AsyncResult<T> {
	
	/** Logger */
	static Logger logger = LoggerFactory.getLogger(AsyncResultImpl.class);
	static Logger listenerLogger = LoggerFactory.getLogger(ResultListener.class);
	
	static Executor executor = StackUtils.BLOCKING_EXECUTOR;
	
	/** Container for an error */
	ServiceResultException error;
	/** Container for a result */
	T result;
	/** Listener */
	ResultListener<T> listener;
	/** Semaphore that is released once there is a result or error */
	Semaphore s = new Semaphore(0);

	public AsyncResultImpl() {
	}	
		
	@Override
	public ServiceResultException getError() {
		return error;
	}

	/**
	 * Set error, invokes any listener in executor thread.
	 * 
	 * If result or error has already been set, this method does nothing.
	 *  
	 * @param error
	 */
	public void setError(final ServiceResultException error)
	{
		synchronized(this) {
			if (isFinished()) return;
			this.error = error;
			final ResultListener<T> l = listener;
			if (l != null) {
				executor.execute(new Runnable() {
					public void run() {
						try {
							l.onError(error);
						} catch (RuntimeException rte) {
							listenerLogger.error("Unexpected RuntimeException in ResultListener#onError(", rte);
						} finally {
							finish();
						}
					}});	
			}
		}
		s.release(Integer.MAX_VALUE);		
	}

	/**
	 * @return
	 */
	private boolean isFinished() {
		return this.result!=null || this.error!=null;
	}

	/**
	 * Set error, invokes any listener in excutor thread
	 * 
	 * If result or error has already been set, this method does nothing.
	 * 
	 * @param result
	 */
	public void setResult(T result) {		
		synchronized(this) {
			if (isFinished()) return;
			this.result = result;
			final ResultListener<T> l = listener;
			if (l != null) {
				executor.execute(new Runnable() {
					public void run() {
						try {
							l.onCompleted(AsyncResultImpl.this.result);
						} catch (RuntimeException rte) {
							listenerLogger.error("Unexpected RuntimeException in ResultListener#onCompleted", rte);
						} finally {
							finish();
						}
					}});	
			}
		}
		s.release(Integer.MAX_VALUE);	
	}

	/**
	 * Set error, invokes any listener here and now.
	 * Note the listener may throw unexpected {@link RuntimeException}
	 *  
	 * @param error
	 */
	public void setErrorSync(final ServiceResultException error)
	throws RuntimeException
	{
		synchronized(this) {
			if (isFinished()) return;
			this.error = error;
			final ResultListener<T> l = listener;
			if (l != null) {
				try {
					l.onError(error);
				} catch (RuntimeException rte) {
					listenerLogger.error("Unexpected RuntimeException in ResultListener#onError(", rte);
				} finally {
					finish();
				}
				
			}
		}
		s.release(Integer.MAX_VALUE);		
	}

	private void finish() {
//		logger.debug("finish");
		setListener(null);
	}

	public void setResultSync(T result) 		
	throws RuntimeException
	{
		synchronized(this) {
			if (isFinished()) return;
			this.result = result;
			final ResultListener<T> l = listener;
			if (l != null) {
				try {
					l.onCompleted(result);
				} catch (RuntimeException rte) {
					listenerLogger.error("Unexpected RuntimeException in ResultListener#onCompleted(", rte);
				} finally {
					finish();
				}
			}
		}
		s.release(Integer.MAX_VALUE);	
	}
	
	@Override
	public T getResult() {
		return result;
	}

	@Override
	public AsyncResultStatus getStatus() {
		if (error!=null)
			return AsyncResultStatus.Failed;
		if (result!=null)
			return AsyncResultStatus.Succeed;
		return AsyncResultStatus.Waiting;
	}

	@Override
	public void setListener(ResultListener<T> listener) {
		ServiceResultException _error;
		T _result;
		synchronized(this) {
			this.listener = listener;
			_error = error;
			_result = result;
		}
		if (listener!=null) {
			if (_result!=null) listener.onCompleted(_result);
			if (_error!=null) listener.onError(_error);
		}
	}

	@Override
	public T waitForResult() 
	throws ServiceResultException 
	{
		try {
			s.acquire();
		} catch (InterruptedException e) {
			logger.debug("timeout: ", error);
			throw new ServiceResultException(StatusCodes.Bad_Timeout, e);
		}
		if (result!=null) return result;
		if (error!=null) {
			logger.debug("error: ", error);
			// The error thread has originated from TcpConnection / Read - Switch the thread in the stack trace to the current thread
			if (error instanceof ServiceFaultException)
				throw new ServiceFaultException(((ServiceFaultException) error).getServiceFault());
			else
				throw error;
		}
		throw new ServiceResultException(StatusCodes.Bad_UnexpectedError);
	}
	
	@Override
	public T waitForResult(long timeout, TimeUnit unit)
			throws ServiceResultException {
		try {
			s.tryAcquire(timeout, unit);
		} catch (InterruptedException e) {
			throw new ServiceResultException(StatusCodes.Bad_Timeout, e);
		}
//		logger.debug("result:"+ result);
		if (result!=null) return result;
		logger.debug("error:", error);
		if (error!=null) {
			// The error thread has originated from TcpConnection / Read - Switch the thread in the stack trace to the current thread
			if (error instanceof ServiceFaultException)
				throw new ServiceFaultException(((ServiceFaultException) error).getServiceFault());
			else
				throw error;			
		}
		throw new ServiceResultException(StatusCodes.Bad_Timeout);
	}

	/**
	 * Links another result as a source of this result object
	 *  
	 * @param source
	 */
	public void setSource(AsyncResult<T> source)
	{
		source.setListener(new ResultListener<T>() {
			@Override
			public void onError(ServiceResultException error) {
				setError(error);
			}
			@Override
			public void onCompleted(T result) {
				setResult(result);
			}
		});
	}

}
