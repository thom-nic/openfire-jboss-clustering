package com.enernoc.rnd.openfire.cluster.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.util.Log;
import org.jivesoftware.util.cache.ClusterTask;
import org.xmpp.packet.JID;

public class GetSessionTask implements ClusterTask {
	JID jid;
	ClientSession cs;

	public GetSessionTask( JID address ) {
		this.jid = address;
	}

	@Override
	public Object getResult() {
		return this.cs;
	}

	@Override
	public void run() {
		this.cs = XMPPServer.getInstance().getSessionManager().getSession( jid );
		Log.debug("GetSessionTask : " + cs);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		jid.readExternal(in);		
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		jid.writeExternal(out);		
	}
}
