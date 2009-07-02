package com.enernoc.rnd.openfire.cluster.session;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.session.ConnectionMultiplexerSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.session.task.GetMultiplexerSessionTask;

public class ClusterConnectionMultiplexerSession extends ClusterSession
		implements ConnectionMultiplexerSession {

	public ClusterConnectionMultiplexerSession() { super(); }
	public ClusterConnectionMultiplexerSession( JID address, byte[] nodeID ) {
		super(address, nodeID);
	}
	
	@Override
	void doCopy(Session s) { /* no-op */ }

	@Override
	void doReadExternal(ExternalizableUtil ext, ObjectInput in)
			throws IOException, ClassNotFoundException {
		/* no-op */
	}

	@Override
	void doWriteExternal(ExternalizableUtil ext, ObjectOutput out)
			throws IOException {
		/* no-op */
	}

	@Override
	ClusterTask getSessionUpdateTask() {
		// TODO Auto-generated method stub
		return new GetMultiplexerSessionTask();
	}
}
