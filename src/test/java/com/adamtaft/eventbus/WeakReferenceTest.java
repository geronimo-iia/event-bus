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
