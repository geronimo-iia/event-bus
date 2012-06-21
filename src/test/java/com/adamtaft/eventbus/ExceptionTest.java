package com.adamtaft.eventbus;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
 * 
 */
public class ExceptionTest extends TestCase {

	private int	throwRuntimeExceptionCount;
	private int	throwExceptionCount;
	private int	handleExceptionsCount;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		throwRuntimeExceptionCount = 0;
		throwExceptionCount = 0;
		handleExceptionsCount = 0;
	}

	@EventHandler
	public void throwRuntimeException(final String event) {
		throwRuntimeExceptionCount++;
		throw new RuntimeException("Some RuntimeException");
	}

	@EventHandler
	public void throwException(final String event) throws Exception {
		throwExceptionCount++;
		throw new Exception("Some Exception");
	}

	@EventHandler
	public void handleExceptions(final BusExceptionEvent event) {
		handleExceptionsCount++;
		System.out.println("Exception Handled was: " + event.getCause());
	}

	public void testException() throws InterruptedException {
		DefaultEventBus.subscribe(this);
		DefaultEventBus.publish("Some String Event");
		while (DefaultEventBus.hasPendingEvents()) {
			Thread.sleep(50);
		}
		// we launch one exception by throwException
		Assert.assertTrue(throwExceptionCount == 1);
		// one runtime launch by throwRuntimeException
		Assert.assertTrue(throwRuntimeExceptionCount == 1);
		// two : throwException + throwRuntimeException
		Assert.assertTrue(handleExceptionsCount == 2);
	}

}
