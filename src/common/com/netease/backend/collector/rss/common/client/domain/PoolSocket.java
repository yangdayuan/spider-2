package com.netease.backend.collector.rss.common.client.domain;

import org.apache.thrift.transport.TSocket;

public class PoolSocket {
	
	private int cur;
	
	private TSocket tSocket;

	public int getCur() {
		return cur;
	}

	public void setCur(int cur) {
		this.cur = cur;
	}

	public TSocket gettSocket() {
		return tSocket;
	}

	public void settSocket(TSocket tSocket) {
		this.tSocket = tSocket;
	}
	
	

}
