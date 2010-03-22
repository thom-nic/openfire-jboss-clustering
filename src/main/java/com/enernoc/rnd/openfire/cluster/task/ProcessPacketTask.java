package com.enernoc.rnd.openfire.cluster.task;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Packet;

public class ProcessPacketTask extends PacketTask<Packet> {

	public ProcessPacketTask() {}
	public ProcessPacketTask( Packet p ) {
		super( p );
	}
	
	public void run() {
		if(packet == null)
			return;
			
		//Retry fetching the session a few times as it may not yet be replicated to this cache
		for(int i = 0; i < 5; i++) {
			Session session = XMPPServer.getInstance().getSessionManager().getSession( packet.getTo() );
			if( session != null ) {
				session.process( packet );
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
	}
}
