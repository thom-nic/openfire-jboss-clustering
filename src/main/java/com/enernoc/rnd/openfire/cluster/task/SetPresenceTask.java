package com.enernoc.rnd.openfire.cluster.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.Presence;

public class SetPresenceTask extends PacketTask<Presence> {

	public SetPresenceTask() {}
	public SetPresenceTask( Presence p ) {
		super( p );
	}
	
	public void run() {
		XMPPServer.getInstance().getSessionManager()
			.getSession( packet.getFrom() ).setPresence( packet );
	}

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
 		this.packet = new Presence( super.readXML(in), true );
    }

    @Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil.getInstance().writeSafeUTF( out, packet.toXML() );
	}
}
