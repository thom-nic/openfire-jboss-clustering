package com.enernoc.rnd.openfire.cluster;

import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.ComponentSession;
import org.jivesoftware.openfire.session.ConnectionMultiplexerSession;
import org.jivesoftware.openfire.session.IncomingServerSession;
import org.jivesoftware.openfire.session.OutgoingServerSession;
import org.jivesoftware.openfire.session.RemoteSessionLocator;
import org.jivesoftware.util.cache.CacheFactory;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.task.GetSessionTask;

public class ClusteredSessionLocator implements RemoteSessionLocator {

	public ClientSession getClientSession( byte[] nodeID, JID address ) {
		ClientSession cs = (ClientSession)CacheFactory.doSynchronousClusterTask( new GetSessionTask(address), nodeID );
		return new ClusteredClientSession( cs, nodeID );
	}

	public ComponentSession getComponentSession(byte[] nodeID, JID address) {
		// TODO Auto-generated method stub
		return null;
	}

	public ConnectionMultiplexerSession getConnectionMultiplexerSession(
			byte[] nodeID, JID address) {
		// TODO Auto-generated method stub
		return null;
	}

	public IncomingServerSession getIncomingServerSession(byte[] nodeID,
			String streamID) {
		// TODO Auto-generated method stub
		return null;
	}

	public OutgoingServerSession getOutgoingServerSession(byte[] nodeID,
			JID address) {
		// TODO Auto-generated method stub
		return null;
	}

}
