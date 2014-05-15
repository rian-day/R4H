/*
 ** Copyright (C) 2014 Mellanox Technologies
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at:
 **
 ** http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 ** either express or implied. See the License for the specific language
 ** governing permissions and  limitations under the License.
 **
 */

package com.mellanox.r4h;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mellanox.jxio.EventQueueHandler;

public class R4HEventHandler extends EventQueueHandler {
	Runnable onBreakEqh;
	/**
	 * @param callbacks
	 */
	public R4HEventHandler(Callbacks callbacks, Runnable onBreakEqhFunc ) {
		super(callbacks);
		this.onBreakEqh=onBreakEqhFunc;
	}

	public R4HEventHandler(Callbacks callbacks, boolean toIgnore) {
		super(callbacks);
		setIgnoreBreak(toIgnore);
	}

	private final static Log LOG = LogFactory.getLog(R4HEventHandler.class.getName());
	public static Object o = new Object();
	private boolean ignoreBreak = false;

	@Override
	public void run() {
		try {
			while (true) { //TODO: while !this.stopLoop?
				runEventLoop(-1 /* Infinite events */, -1 /* Infinite duration */);
				onBreakEqh();
			}
		} catch (Throwable t) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			t.printStackTrace(ps);
			LOG.fatal("A R4H event handler worker was crashed. " + new String(baos.toByteArray()));
		}
	}
	
	private void onBreakEqh() {
		if (onBreakEqh != null) {
			onBreakEqh.run();
		}
	}

	public synchronized boolean isBreakIgnored() {
		return ignoreBreak;
	}

	public synchronized void setIgnoreBreak(boolean ignoreBreak) {
		this.ignoreBreak = ignoreBreak;
	}

	@Override
	public synchronized void breakEventLoop() {
		if (!isBreakIgnored()) {
			super.breakEventLoop();
		}
	}
}