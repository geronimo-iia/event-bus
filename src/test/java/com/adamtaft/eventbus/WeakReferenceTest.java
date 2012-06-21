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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
 * 
 */
public class WeakReferenceTest extends TestCase {
	private static int	handleStringCount	= 0;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		WeakReferenceTest.handleStringCount = 0;
	}

	@EventHandler
	public void handleString(final String evt) {
		WeakReferenceTest.handleStringCount++;
	}

	public void testWeakReference() throws InterruptedException {
		// create an event handler for this example
		WeakReferenceTest wre = new WeakReferenceTest();

		// subscribe the handler to the event bus
		DefaultEventBus.subscribe(wre);

		// send an event to the buss
		DefaultEventBus.publish("First String Event");

		// wait here to ensure all events (above) have been pushed out.
		while (DefaultEventBus.hasPendingEvents()) {
			Thread.sleep(50);
		}

		Assert.assertTrue(WeakReferenceTest.handleStringCount == 1);

		// set the reference to null
		// IT'S STILL BETTER TO UNSUBSCRIBE. THIS SHOULD ONLY BE CONSIDERED A
		// FALLBACK.
		wre = null;

		// Pretty please, run the garbage collection.
		System.gc();

		// Ideally, this second event won't show up. YMMV
		// You might see this second event if the garbage collector didn't run
		// This is system and JVM specific. This example does work for me.
		DefaultEventBus.publish("Second String Event");

		// wait here to ensure all events (above) have been pushed out.
		while (DefaultEventBus.hasPendingEvents()) {
			Thread.sleep(50);
		}

		Assert.assertTrue(WeakReferenceTest.handleStringCount == 1);
	}

}
