package com.enernoc.rnd.openfire.cluster.session.task;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ConnectionMultiplexerSession;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.session.ClusterConnectionMultiplexerSession;


public class GetMultiplexerSessionTask extends GetSessionTask<ClusterConnectionMultiplexerSession> {

	public GetMultiplexerSessionTask() { super(); }
	public GetMultiplexerSessionTask( JID address ) { super( address ); }
	
	@Override
	protected ConnectionMultiplexerSession getLocalSession() {
		ConnectionMultiplexerSession s = XMPPServer.getInstance().getSessionManager().getConnectionMultiplexerSession(super.jid);
		log.debug( "Getting multiplexer session for {} : {}", super.jid, s );
		return s;
	}

	@Override
	protected ClusterConnectionMultiplexerSession newSession() {
		return new ClusterConnectionMultiplexerSession( super.jid, 
				XMPPServer.getInstance().getNodeID().toByteArray() );
	}
}
