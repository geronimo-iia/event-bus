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
public class ExceptionTest extends TestCase {

	private int throwRuntimeExceptionCount;
	private int throwExceptionCount;
	private int handleExceptionsCount;

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
