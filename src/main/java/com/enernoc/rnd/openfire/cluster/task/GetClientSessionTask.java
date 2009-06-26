package com.enernoc.rnd.openfire.cluster.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.session.ClusteredClientSession;

public class GetClientSessionTask implements ClusterTask {
	
	JID jid;
	ClusteredClientSession cs;

	public GetClientSessionTask() {}
	public GetClientSessionTask( JID address ) {
		this.jid = address;
	}

	@Override
	public ClusteredClientSession getResult() {
		return this.cs;
	}

	@Override
	public void run() {
		XMPPServer xmpp = XMPPServer.getInstance();
		this.cs = new ClusteredClientSession( jid, xmpp.getNodeID().toByteArray() );
		cs.copy( xmpp.getSessionManager().getSession( jid ) );
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.jid = new JID();
		jid.readExternal(in);
		this.cs = new ClusteredClientSession();
		cs.readExternal(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		jid.writeExternal(out);
		ExternalizableUtil ext = ExternalizableUtil.getInstance();
		ext.writeBoolean( out, cs == null );
	}
}
