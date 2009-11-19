package com.enernoc.rnd.openfire.cluster.session.task;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.session.ClusteredClientSession;

@Deprecated
/**
 * @Deprecated use ClientSessionTask instead
 */
public class GetClientSessionTask extends GetSessionTask<ClusteredClientSession> {
	
	public GetClientSessionTask() {}
	public GetClientSessionTask( JID address ) { super( address ); }
	
	@Override
	protected ClusteredClientSession newSession() {
		return new ClusteredClientSession( super.jid, 
				XMPPServer.getInstance().getNodeID().toByteArray() );
	}
	
	@Override
	protected Session getLocalSession() {
		ClientSession s = XMPPServer.getInstance().getSessionManager().getSession( jid );
		log.debug( "Getting client session for {} : {}", super.jid, s );
		return s;
	}
}
