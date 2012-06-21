package com.adamtaft.eventbus;

import java.util.EventObject;

/**
 * A VetoEvent is sent out of the event bus when a veto has
 * been made by the subscriber. The subscriber will have
 * indicated a veto by throwing a {@link VetoException} in
 * the {@link EventHandler} annotated method.
 * 
 * @author Adam Taft
 * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
 */
public class VetoEvent extends EventObject {

	private static final long	serialVersionUID	= 55729908996948134L;

	public VetoEvent(final Object event) {
		super(event);
	}

}
