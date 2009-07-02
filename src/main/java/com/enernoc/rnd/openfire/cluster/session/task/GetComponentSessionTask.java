package com.enernoc.rnd.openfire.cluster.session.task;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ComponentSession;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.session.ClusterComponentSession;

public class GetComponentSessionTask extends GetSessionTask<ClusterComponentSession> {

	public GetComponentSessionTask() { super(); }
	public GetComponentSessionTask( JID address ) { super( address ); }

	@Override
	protected ClusterComponentSession newSession() {
		return new ClusterComponentSession( super.jid,
				XMPPServer.getInstance().getNodeID().toByteArray() );
	}
	@Override
	protected Session getLocalSession() {
		ComponentSession s = XMPPServer.getInstance().getSessionManager().getComponentSession( super.jid.getDomain() );
		log.debug( "Getting incoming session for {} : {}", super.jid, s );
		return s;
	}
}
