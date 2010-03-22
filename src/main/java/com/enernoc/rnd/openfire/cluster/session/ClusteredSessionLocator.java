package com.enernoc.rnd.openfire.cluster.session;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ConnectionMultiplexerSession;
import org.jivesoftware.openfire.session.IncomingServerSession;
import org.jivesoftware.openfire.session.RemoteSessionLocator;
import org.jivesoftware.util.cache.CacheFactory;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.session.task.ClientSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.GetClientSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.GetComponentSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.GetIncomingSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.GetMultiplexerSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.GetOutgoingSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.RemoteSessionTask.Operation;

/**
 * Executed to get session proxies for sessions located on other nodes in this 
 * cluster.
 * @author tnichols
 */
public class ClusteredSessionLocator implements RemoteSessionLocator {

	public ClusteredClientSession getClientSession( byte[] nodeID, JID address ) {
		return new ClusteredClientSession(address, nodeID);
	}

	public ClusterComponentSession getComponentSession(byte[] nodeID, JID address) {
		return (ClusterComponentSession) CacheFactory.doSynchronousClusterTask(
				new GetComponentSessionTask( address ), nodeID);
	}

	public ConnectionMultiplexerSession getConnectionMultiplexerSession(
			byte[] nodeID, JID address) {
		return (ConnectionMultiplexerSession) CacheFactory.doSynchronousClusterTask(
				new GetMultiplexerSessionTask( address ), nodeID);
	}

	public IncomingServerSession getIncomingServerSession(byte[] nodeID,
			String streamID) {
		XMPPServer xmpp = XMPPServer.getInstance();
		IncomingServerSession s = xmpp.getSessionManager().getIncomingServerSession(streamID);
		if ( s == null ) {
//			xmpp.getSessionManager().createClientSession(conn)
		}
		return s;
//		return (ClusterIncomingSession) CacheFactory.doSynchronousClusterTask(
//				new GetIncomingSessionTask( streamID ), nodeID);
	}

	public ClusterOutgoingSession getOutgoingServerSession(byte[] nodeID,
			JID address) {
		return null;
//		return (ClusterOutgoingSession) CacheFactory.doSynchronousClusterTask(
//				new GetOutgoingSessionTask( address ), nodeID);
	}

}
