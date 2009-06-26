package com.enernoc.rnd.openfire.cluster.task;

import org.jivesoftware.openfire.XMPPServer;
import org.xmpp.packet.Packet;

public class PacketRouterTask extends PacketTask<Packet> {

	public PacketRouterTask() {}
	public PacketRouterTask( Packet packet ) {
		super( packet );
	}

	public void run() {
		XMPPServer.getInstance().getPacketRouter().route( packet );
	}
}
