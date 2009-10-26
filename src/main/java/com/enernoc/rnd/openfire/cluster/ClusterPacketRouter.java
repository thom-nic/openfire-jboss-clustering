package com.enernoc.rnd.openfire.cluster;

import org.jivesoftware.openfire.RemotePacketRouter;
import org.jivesoftware.util.cache.CacheFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import com.enernoc.rnd.openfire.cluster.task.BroadcastTask;
import com.enernoc.rnd.openfire.cluster.task.PacketRouterTask;

public class ClusterPacketRouter implements RemotePacketRouter {

	public void broadcastPacket( Message packet ) {
		CacheFactory.doClusterTask( new BroadcastTask(packet) );
	}

	public boolean routePacket( byte[] nodeID, JID receipient, Packet packet ) {
		packet.setTo(receipient);
		CacheFactory.doClusterTask( new PacketRouterTask(packet), nodeID );
		return true;
	}	
}