package com.netease.backend.collector.rss.common.exit;

import com.netease.backend.collector.rss.common.exception.DricException;

public class ExitObserver implements IExitObserver {
	private static final long serialVersionUID = 7660914960053392937L;
	private boolean exit = false;
	
	@Override
	public void notifyExit(ExitObservable o, Object args) throws DricException {
		this.exit = true;
	}
	
	public boolean isExit() {
		return exit;
	}

}
