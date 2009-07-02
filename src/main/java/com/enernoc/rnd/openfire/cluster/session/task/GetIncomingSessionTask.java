package com.enernoc.rnd.openfire.cluster.session.task;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.IncomingServerSession;
import org.jivesoftware.openfire.session.Session;

import com.enernoc.rnd.openfire.cluster.session.ClusterIncomingSession;

public class GetIncomingSessionTask extends GetSessionTask<ClusterIncomingSession> {

	String streamID;
	
	public GetIncomingSessionTask() {}
	public GetIncomingSessionTask( String streamID ) {
		this.streamID = streamID;
	}
	
	@Override
	protected ClusterIncomingSession newSession() {
		return new ClusterIncomingSession( streamID, 
				XMPPServer.getInstance().getNodeID().toByteArray() );
	}
	
	@Override
	protected Session getLocalSession() {
		IncomingServerSession s = XMPPServer.getInstance().getSessionManager().getIncomingServerSession(streamID);
		log.debug( "Getting incoming session for {} : {}", super.jid, s );
		return s;
	}
}
