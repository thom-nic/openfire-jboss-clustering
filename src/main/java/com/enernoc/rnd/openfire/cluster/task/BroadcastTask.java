package com.enernoc.rnd.openfire.cluster.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.Message;

public class BroadcastTask implements ClusterTask {
	Message packet;
	public BroadcastTask(Message packet2) {
		packet = packet2;
	}

	public Object getResult() {
		return null;
	}

	public void run() {
		XMPPServer.getInstance().getRoutingTable().broadcastPacket(packet, true);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		Element packetElement = (Element) ExternalizableUtil.getInstance().readSerializable(in);
		packet = new Message(packetElement, true);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil.getInstance().writeSerializable(out, (DefaultElement) packet.getElement());
	}
}
