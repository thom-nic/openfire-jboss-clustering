package com.enernoc.rnd.openfire.cluster.session.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.cache.ClusterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.session.ClusterSession;

public abstract class GetSessionTask<S extends ClusterSession> implements ClusterTask {

	protected static final Logger log = LoggerFactory.getLogger( GetSessionTask.class ); 
	protected JID jid;
	protected S session;
	
	public GetSessionTask() {}
	public GetSessionTask( JID address ) { this.jid = address; }

	public S getResult() {
		return this.session;
	}

	protected abstract S newSession();
	
	protected abstract Session getLocalSession();
	
	public void run() {
		this.session = newSession();
		session.copy( getLocalSession() );
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.jid = new JID();
		jid.readExternal(in);
		if ( in.readBoolean() ) return;
		this.session = newSession();
		session.readExternal(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		jid.writeExternal(out);
		out.writeBoolean( session == null );
		if ( session != null ) session.writeExternal( out );
	}
}
