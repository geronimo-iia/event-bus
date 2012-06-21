package com.adamtaft.eventbus;

/**
 * An {@link EventBus} factory that will return a singleton implementation. By
 * default, a {@link BasicEventBus} implementation will be returned via the
 * factory methods.
 * 
 * 
 * 
 * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
 */
public final class DefaultEventBus {

	private static class EventBusFactory {
		private static final EventBus	INSTANCE	= new BasicEventBus();
	}

	private DefaultEventBus() {
		if (EventBusFactory.INSTANCE != null) {
			throw new IllegalStateException("Already instantiated");
		}
	}

	public static EventBus getInstance() {
		return EventBusFactory.INSTANCE;
	}

	public static void subscribe(final Object subscriber) {
		EventBusFactory.INSTANCE.subscribe(subscriber);
	}

	public static void unsubscribe(final Object subscriber) {
		EventBusFactory.INSTANCE.unsubscribe(subscriber);
	}

	public static void publish(final Object event) {
		EventBusFactory.INSTANCE.publish(event);
	}

	public static boolean hasPendingEvents() {
		return EventBusFactory.INSTANCE.hasPendingEvents();
	}

}
