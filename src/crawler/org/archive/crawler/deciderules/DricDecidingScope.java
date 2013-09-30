package org.archive.crawler.deciderules;

import org.archive.crawler.datamodel.CandidateURI;

public class DricDecidingScope extends DecidingScope {
	private static final long serialVersionUID = -3521467757512964906L;

	public DricDecidingScope(String name) {
		super(name);
	}
	
	public boolean addSeed(final CandidateURI curi) {
		return true;
	} 
}
