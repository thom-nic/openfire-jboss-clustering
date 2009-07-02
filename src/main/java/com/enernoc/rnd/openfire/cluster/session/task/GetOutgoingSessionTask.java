package com.enernoc.rnd.openfire.cluster.session.task;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.OutgoingServerSession;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.session.ClusterOutgoingSession;

public class GetOutgoingSessionTask extends GetSessionTask<ClusterOutgoingSession> {
	
	public GetOutgoingSessionTask() {}
	public GetOutgoingSessionTask( JID address ) { super( address ); }
	
	@Override
	protected ClusterOutgoingSession newSession() {
		return new ClusterOutgoingSession( super.jid, 
				XMPPServer.getInstance().getNodeID().toByteArray() );
	}
	@Override
	protected Session getLocalSession() {
		OutgoingServerSession s = XMPPServer.getInstance().getSessionManager().getOutgoingServerSession( super.jid.getDomain() ); 
		log.debug( "Getting outgoing session for {} : {}", super.jid, s );
		return s;
	}
}
