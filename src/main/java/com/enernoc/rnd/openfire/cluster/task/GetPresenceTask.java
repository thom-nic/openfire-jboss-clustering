package com.enernoc.rnd.openfire.cluster.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.Presence;

public class GetPresenceTask extends PacketTask<Presence> {

	String jid;
	
	public GetPresenceTask() {}
	public GetPresenceTask( String jid ) {
		this.jid=jid;
	}
	
	public Presence getResult() { return super.packet; }

	public void run() {
		log.debug( " TASK - GetPresence : {}", jid );
		try {
			XMPPServer server = XMPPServer.getInstance(); 
			User u = server.getUserManager().getUser(jid);
			super.packet = server.getPresenceManager().getPresence(u);
		} catch ( UserNotFoundException e ) {
			log.warn( "Presence not found for {}", jid, e );
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance();
		ext.writeSafeUTF(out, jid);
		ext.writeBoolean(out, packet == null );
		if ( packet != null ) ext.writeSafeUTF(out, packet.toXML() );
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance();
		this.jid= ext.readSafeUTF(in);
		if ( ext.readBoolean(in) ) packet = new Presence( super.readXML(in) );
	}
}
