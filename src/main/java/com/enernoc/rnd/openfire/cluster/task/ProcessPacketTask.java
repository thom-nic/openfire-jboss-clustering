package com.enernoc.rnd.openfire.cluster.task;

import org.jivesoftware.openfire.XMPPServer;
import org.xmpp.packet.Packet;

public class ProcessPacketTask extends PacketTask<Packet> {

	public ProcessPacketTask() {}
	public ProcessPacketTask( Packet p ) {
		super( p );
	}
	
	public void run() {
		log.debug( " TASK - ProcessPacket : {}", packet );
		XMPPServer.getInstance().getSessionManager()
			.getSession( packet.getTo() ).process( packet );
	}
}
