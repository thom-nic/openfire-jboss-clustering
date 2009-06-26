package com.enernoc.rnd.openfire.cluster.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.cache.ClusterTask;
import org.xmpp.packet.JID;

public class CloseSessionTask implements ClusterTask {

	JID address;
	public CloseSessionTask() {}
	public CloseSessionTask( JID jid ) {
		this.address = jid;
	}
	
	@Override
	public Void getResult() {
		return null;
	}

	@Override
	public void run() {
		XMPPServer.getInstance().getSessionManager().getSession(
				this.address ).close();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		address.readExternal(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		address.writeExternal(out);
	}
}
