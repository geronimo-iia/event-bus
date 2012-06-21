/**
 * Copyright (c) 2010, Adam Taft
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     * Neither the name of the project owner nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.adamtaft.eventbus;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple Event Bus implementation which receives events or messages from
 * various sources and distributes them to all subscribers of the event type.
 * This is highly useful for programs which are event driven. Swing applications
 * in particular can benefit from an event bus architecture, as opposed to the
 * traditional event listener architecture it employs.
 * <p>
 * The BasicEventBus class is thread safe and uses a background thread to notify
 * the subscribers of the event. The subscribers are notified in a serial
 * fashion, and only one event will be published at a time. Though, the
 * {@link #publish(Object)} method is done in a non-blocking way.
 * <p>
 * Subscribers subscribe to the EventBus using the {@link #subscribe(Object)}
 * method. A specific subscriber type is not required, but the subscriber will
 * be reflected to find all methods annotated with the {@link EventHandler}
 * annotations. These methods will be invoked as needed by the event bus based
 * on the type of the first parameter to the annotated method.
 * <p>
 * An event handler can indicate that it can veto events by setting the
 * {@link EventHandler#canVeto()} value to true. This will inform the EventBus
 * of the subscriber's desire to veto the event. A vetoed event will not be sent
 * to the regular subscribers.
 * <p>
 * During publication of an event, all veto EventHandler methods will be
 * notified first and allowed to throw a {@link VetoException} indicating that
 * the event has been vetoed and should not be published to the remaining event
 * handlers. If no vetoes have been made, the regular subscriber handlers will
 * be notified of the event.
 * <p>
 * Subscribers are stored using a {@link WeakReference} such that a memory leak
 * can be avoided if the client fails to unsubscribe at the end of the use.
 * However, calling the {@link #unsubscribe(Object)} method is highly
 * recommended none-the-less.
 * 
 * @author Adam Taft
 * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
 */
public final class BasicEventBus implements EventBus {
	/**
	 * Logger 'BasicEventBus.class' user to warn some exception when they occurs
	 * on event handler.
	 */
	private final Logger logger = LoggerFactory.getLogger(BasicEventBus.class);

	private final List<HandlerInfo> handlers = new CopyOnWriteArrayList<HandlerInfo>();
	private final BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	private final BlockingQueue<HandlerInfo> killQueue = new LinkedBlockingQueue<HandlerInfo>();

	/**
	 * The ExecutorService used to handle event delivery to the event handlers.
	 */
	private final ExecutorService executorService;

	/**
	 * Should the event bus wait for the regular handlers to finish processing
	 * the event messages before continuing to the next event. Defaults to
	 * 'false' which is sensible for most use cases.
	 */
	private final boolean waitForHandlers;

	/**
	 * Default constructor sets up the executorService property to use the
	 * {@link Executors#newCachedThreadPool()} implementation. The configured
	 * ExecutorService will have a custom ThreadFactory such that the threads
	 * returned will be daemon threads (and thus not block the application from
	 * shutting down).
	 */
	public BasicEventBus() {
		this(Executors.newCachedThreadPool(new ThreadFactory() {
			private final ThreadFactory delegate = Executors.defaultThreadFactory();

			@Override
			public Thread newThread(final Runnable runnable) {
				final Thread thread = delegate.newThread(runnable);
				thread.setDaemon(true);
				return thread;
			}
		}), false);
	}

	public BasicEventBus(final ExecutorService executorService, final boolean waitForHandlers) {
		// start the background daemon consumer thread.
		final Thread eventQueueThread = new Thread(new EventQueueRunner(), "EventQueue Consumer Thread");
		eventQueueThread.setDaemon(true);
		eventQueueThread.start();

		final Thread killQueueThread = new Thread(new KillQueueRunner(), "KillQueue Consumer Thread");
		killQueueThread.setDaemon(true);
		killQueueThread.start();

		this.executorService = executorService;
		this.waitForHandlers = waitForHandlers;
	}

	/**
	 * Subscribe the specified instance as a potential event subscriber. The
	 * subscriber must annotate a method (or two) with the {@link EventHandler}
	 * annotation if it expects to receive notifications.
	 * <p>
	 * Note that the EventBus maintains a {@link WeakReference} to the
	 * subscriber, but it is still adviced to call the
	 * {@link #unsubscribe(Object)} method if the subscriber does not wish to
	 * receive events any longer.
	 * 
	 * @param subscriber
	 *            The subscriber object which will receive notifications on
	 *            {@link EventHandler} annotated methods.
	 */
	@Override
	public void subscribe(final Object subscriber) {
		// lookup to see if we have any subscriber instances already
		boolean subscribedAlready = false;
		for (final HandlerInfo info : handlers) {
			final Object otherSubscriber = info.getSubscriber();
			if (otherSubscriber == null) {
				try {
					killQueue.put(info);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			if (subscriber == otherSubscriber) {
				subscribedAlready = true;
			}
		}
		if (subscribedAlready) {
			return;
		}

		final Method[] methods = subscriber.getClass().getDeclaredMethods();
		for (final Method method : methods) {
			// look for the EventHandler annotation on the method, if it exists
			// if not, this returns null, and go to the next method
			final EventHandler eh = method.getAnnotation(EventHandler.class);
			if (eh == null) {
				continue;
			}

			// evaluate the parameters of the method.
			// only a single parameter of the Object type is allowed for the
			// handler method.
			final Class<?>[] parameters = method.getParameterTypes();
			if (parameters.length != 1) {
				throw new IllegalArgumentException("EventHandler methods must specify a single Object paramter.");
			}

			// add the subscriber to the list
			final HandlerInfo info = new HandlerInfo(parameters[0], method, subscriber, eh.canVeto());
			handlers.add(info);
		}
	}

	/***
	 * Unsubscribe the specified subscriber from receiving future published
	 * events.
	 * 
	 * @param subscriber
	 *            The object to unsubcribe from future events.
	 */
	@Override
	public void unsubscribe(final Object subscriber) {
		final List<HandlerInfo> killList = new ArrayList<HandlerInfo>();
		for (final HandlerInfo info : handlers) {
			final Object obj = info.getSubscriber();
			if ((obj == null) || (obj == subscriber)) {
				killList.add(info);
			}
		}
		for (final HandlerInfo kill : killList) {
			handlers.remove(kill);
		}
	}

	/**
	 * Publish the specified event to the event bus. Based on the type of the
	 * event, the EventBus will publish the event to the subscribing objects.
	 * 
	 * @param event
	 *            The event to publish on the event bus.
	 */
	@Override
	public void publish(final Object event) {
		try {
			queue.put(event);

		} catch (final InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns if the event bus has pending events.
	 * 
	 * @return Returns true if the event bus has pending events to publish.
	 */
	@Override
	public boolean hasPendingEvents() {
		return queue.size() > 0;
	}

	/**
	 * called on the background thread.
	 */
	private void notifySubscribers(final Object evt) {
		// roll through the subscribers
		// we find the veto handlers, regular handlers
		final List<Callable<Boolean>> vetoList = new ArrayList<Callable<Boolean>>();
		final List<Callable<Boolean>> reguList = new ArrayList<Callable<Boolean>>();

		for (final HandlerInfo info : handlers) {
			if (!info.matchesEvent(evt)) {
				continue;
			}
			final HandlerInfoCallable hc = new HandlerInfoCallable(info, evt);
			if (info.isVetoHandler()) {
				vetoList.add(hc);
			} else {
				reguList.add(hc);
			}
		}

		// used to keep track if a veto was called.
		// if so, the regular list won't be processed.
		boolean vetoCalled = false;

		// submit the veto calls to the executor service
		try {
			final List<Future<Boolean>> result = executorService.invokeAll(vetoList);
			for (final Future<Boolean> f : result) {
				if (f.get().booleanValue()) {
					vetoCalled = true;
				}
			}
		} catch (final Exception e) {
			// this only happens if the executorService is interrupted,
			// and by default, that shouldn't really ever happen.
			// or, if the callable sneaks out an exception, which again
			// shouldn't happen.
			vetoCalled = true;
			e.printStackTrace();
		}

		// VetoEvents cannot be vetoed, sorry. :)
		if (vetoCalled && (evt instanceof VetoEvent)) {
			vetoCalled = false;
		}

		// simply return if a veto has occured
		if (vetoCalled) {
			return;
		}

		// ExecutorService.invokeAll() blocks until all the results are
		// computed.
		// for the regular handlers, we need to check if the waitForHandlers
		// property is true. Otherwise (by default) we don't want invokeAll()
		// to block. We don't care about the results, because no vetoes are
		// accounted for here and exceptions really shouldn't be thrown.
		if (waitForHandlers) {
			try {
				executorService.invokeAll(reguList);
			} catch (final Exception e) {
				e.printStackTrace();
			}

		} else {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						executorService.invokeAll(reguList);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

	}

	/**
	 * the background thread consumer, simply extracts any events from the queue
	 * and publishes them.
	 */
	private class EventQueueRunner implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					notifySubscribers(queue.take());
				}

			} catch (final InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * KillQueueRunner consumer runnable to remove handler information from the
	 * subscription list if they are null. this is if the GC has collected them.
	 */
	private class KillQueueRunner implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					final HandlerInfo info = killQueue.take();
					if (info.getSubscriber() == null) {
						handlers.remove(info);
					}
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * HandlerInfo is used to hold the subscriber details.
	 */
	private static class HandlerInfo {
		private final Class<?> eventClass;
		private final Method method;
		private final WeakReference<?> subscriber;
		private final boolean vetoHandler;

		public HandlerInfo(final Class<?> eventClass, final Method method, final Object subscriber, final boolean vetoHandler) {
			this.eventClass = eventClass;
			this.method = method;
			this.subscriber = new WeakReference<Object>(subscriber);
			this.vetoHandler = vetoHandler;
		}

		public boolean matchesEvent(final Object event) {
			return event.getClass().equals(eventClass);
		}

		public Method getMethod() {
			return method;
		}

		public Object getSubscriber() {
			return subscriber.get();
		}

		public boolean isVetoHandler() {
			return vetoHandler;
		}

	}

	/**
	 * HandlerInfoCallable used to actually invoke the task it eats any
	 * exception thrown and publishes an event back onto the bus.
	 */
	private class HandlerInfoCallable implements Callable<Boolean> {
		private final HandlerInfo handlerInfo;
		private final Object event;

		public HandlerInfoCallable(final HandlerInfo handlerInfo, final Object event) {
			this.handlerInfo = handlerInfo;
			this.event = event;
		}

		/**
		 * Invokes the HandlerInfo's callback handler method. If any exeptions
		 * are thrown, besides a VetoException, a {@link BusExceptionEvent} will
		 * be published to the bus with the root cause of the problem. If a
		 * {@link VetoException} is thrown from the invoked method, a
		 * {@link VetoEvent} will be published to the bus and the call will
		 * return true.
		 * <p>
		 * The call has been modified to not throw any Exceptions. It will not,
		 * unlike the interface defintion, throw an exception. All exceptions
		 * are handled locally.
		 * 
		 * @return True if the invoke was vetoed, false otherwise.
		 */
		@Override
		public Boolean call() {
			try {
				final Object subscriber = handlerInfo.getSubscriber();
				if (subscriber == null) {
					killQueue.put(handlerInfo);
					return false;
				}

				handlerInfo.getMethod().invoke(subscriber, event);
				return false;

			} catch (final Exception e) {
				Throwable cause = e;

				// find the root cause
				while (cause.getCause() != null) {
					cause = cause.getCause();
				}

				if (cause instanceof VetoException) {
					publish(new VetoEvent(event));
					return true;
				}

				publish(new BusExceptionEvent(handlerInfo, cause));

				logger.warn("An exception occur when callback event handler method was invoked", e);
				return false;
			}
		}
	}

}
