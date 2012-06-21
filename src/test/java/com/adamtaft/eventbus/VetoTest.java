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
public class VetoTest extends TestCase {

	int	vetoOccur			= 0;
	int	handleEventWithVeto	= 0;

	@EventHandler
	public void handleStringEventWithoutVeto(final String evt) {
		Assert.fail("This shouldn't have happened, it should have been vetoed.");
		throw new AssertionError("This shouldn't have happened, it should have been vetoed.");
	}

	@EventHandler(canVeto = true)
	public void handleStringEventWithVeto(final String evt) {
		// System.out.println("event message was: " + evt);
		handleEventWithVeto++;
		throw new VetoException();
	}

	@EventHandler
	public void handleVeto(final VetoEvent vetoEvent) {
		vetoOccur++;
		// System.out.println("Veto has occured on bus: " + vetoEvent);
	}

	public void testVeto() throws InterruptedException {

		// subscribe it to the bus
		DefaultEventBus.subscribe(this);

		// publish an event that will get vetoed
		DefaultEventBus.publish("String Event (should be vetoed)");

		// wait here to ensure all events (above) have been pushed out.
		while (DefaultEventBus.hasPendingEvents()) {
			Thread.sleep(50);
		}

		Assert.assertTrue(vetoOccur == 1);
		Assert.assertTrue(handleEventWithVeto == 1);
	}

}
