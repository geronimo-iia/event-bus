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
