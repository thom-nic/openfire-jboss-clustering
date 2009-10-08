package com.enernoc.rnd.openfire.cluster.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.Message;

public class BroadcastTask extends PacketTask<Message> {

	public BroadcastTask() {}
	public BroadcastTask( Message msg ) { super(msg); }

	public void run() {
		XMPPServer.getInstance().getRoutingTable().broadcastPacket( packet, true );
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		packet = new Message( super.readXML(in), true );
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil.getInstance().writeSafeUTF( out, packet.toXML() );
	}
}
