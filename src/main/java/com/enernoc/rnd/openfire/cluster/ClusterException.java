package com.enernoc.rnd.openfire.cluster;

/**
 * @author tnichols
 *
 */
public class ClusterException extends RuntimeException {

	public ClusterException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ClusterException(String arg0) {
		super(arg0);
	}

	public ClusterException(Throwable arg0) {
		super(arg0);
	}

	private static final long serialVersionUID = 6637935977107661212L;

	public ClusterException() {
		super();
	}
}