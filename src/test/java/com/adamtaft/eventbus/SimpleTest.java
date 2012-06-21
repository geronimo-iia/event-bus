package com.adamtaft.eventbus;

import java.awt.event.ActionEvent;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
 * 
 */
public class SimpleTest extends TestCase {

	private int	handleStringCount;
	private int	handleActionEventCount;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
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

	public void testSubscribeUnsubscribe() throws InterruptedException {

		// subscribe it to the EventBus
		DefaultEventBus.subscribe(this);

		// publish some events to the bus.
		DefaultEventBus.publish("Some String Event");
		DefaultEventBus.publish(new ActionEvent("Fake Action Event Source", -1, "Fake Command"));

		// this shouldn't be seen, since no handler is interested in Object
		DefaultEventBus.publish(new Object());

		// wait here to ensure all events (above) have been pushed out before
		// unsubscribing. Unsubscribe may happen before the event is delivered.
		while (DefaultEventBus.hasPendingEvents()) {
			Thread.sleep(50);
		}

		Assert.assertTrue(handleActionEventCount == 1);
		Assert.assertTrue(handleStringCount == 1);

		// don't forget to unsubscribe if you're done.
		// not required in this case, since the program ends here anyway.
		DefaultEventBus.unsubscribe(this);

		// Future messages shouldn't be seen by the SimpleExample handler after
		// being unsubscribed.
		DefaultEventBus.publish("This event should not be seen after the unsubscribe call.");

		while (DefaultEventBus.hasPendingEvents()) {
			Thread.sleep(50);
		}
		Assert.assertTrue(handleActionEventCount == 1);
		Assert.assertTrue(handleStringCount == 1);
	}

}
