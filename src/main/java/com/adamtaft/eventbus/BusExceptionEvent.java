package com.adamtaft.eventbus;

import java.util.EventObject;

/**
 * For any exceptions that occur on the bus during handler execution, this event
 * will be published.
 * 
 * @author Adam Taft
 * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
 */
public class BusExceptionEvent extends EventObject {
	private static final long	serialVersionUID	= 1L;

	private final Throwable		cause;

	public BusExceptionEvent(final Object subscriber, final Throwable cause) {
		super(subscriber);
		this.cause = cause;
	}

	public Object getSubscriber() {
		return getSource();
	}

	public Throwable getCause() {
		return cause;
	}

}
