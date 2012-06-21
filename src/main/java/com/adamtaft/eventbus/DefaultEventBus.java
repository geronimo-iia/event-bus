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
