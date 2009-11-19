package com.enernoc.rnd.openfire.cluster.session;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.session.ConnectionMultiplexerSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import com.enernoc.rnd.openfire.cluster.session.task.GetMultiplexerSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.RemoteSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.RemoteSessionTask.Operation;

public class ClusterConnectionMultiplexerSession extends ClusterSession
		implements ConnectionMultiplexerSession {

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

	ClusterTask getSessionUpdateTask() {
		// TODO Auto-generated method stub
		return new GetMultiplexerSessionTask();
	}

	@Override
	ClusterTask getDeliverRawTextTask(String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	ClusterTask getProcessPacketTask(Packet packet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	RemoteSessionTask getRemoteSessionTask(Operation operation) {
		// TODO Auto-generated method stub
		return null;
	}
}
