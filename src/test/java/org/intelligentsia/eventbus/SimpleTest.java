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
package org.intelligentsia.eventbus;

import java.awt.event.ActionEvent;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
 * 
 */
public class SimpleTest {

	private int handleStringCount;
	private int handleActionEventCount;

	@Before
	public void setUp() throws Exception {
		handleStringCount = 0;
		handleActionEventCount = 0;
	}

	@EventHandler
	public void handleString(final String evt) {
		System.out.println("handleString called: " + evt);
		handleStringCount++;
	}

	@EventHandler
	public void handleActionEvent(final ActionEvent evt) {
		System.out.println("handleActionEvent called: " + evt);
		handleActionEventCount++;
	}

	@Test
	public void testSubscribeUnsubscribe() throws InterruptedException {
		final EventBus eventBus = new BasicEventBus();
		// subscribe it to the EventBus
		eventBus.subscribe(this);

		// publish some events to the bus.
		eventBus.publish("Some String Event");
		eventBus.publish(new ActionEvent("Fake Action Event Source", -1, "Fake Command"));

		// this shouldn't be seen, since no handler is interested in Object
		eventBus.publish(new Object());

		// wait here to ensure all events (above) have been pushed out before
		// unsubscribing. Unsubscribe may happen before the event is delivered.
		while (eventBus.hasPendingEvents()) {
			Thread.sleep(50);
		}

		Assert.assertTrue(handleActionEventCount == 1);
		Assert.assertTrue(handleStringCount == 1);

		// don't forget to unsubscribe if you're done.
		// not required in this case, since the program ends here anyway.
		eventBus.unsubscribe(this);

		// Future messages shouldn't be seen by the SimpleExample handler after
		// being unsubscribed.
		eventBus.publish("This event should not be seen after the unsubscribe call.");

		while (eventBus.hasPendingEvents()) {
			Thread.sleep(50);
		}
		Assert.assertTrue(handleActionEventCount == 1);
		Assert.assertTrue(handleStringCount == 1);
	}

}
