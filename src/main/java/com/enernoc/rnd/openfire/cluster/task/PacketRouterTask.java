package com.enernoc.rnd.openfire.cluster.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.Log;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

public class PacketRouterTask implements ClusterTask {

	JID recipient;
	Packet packet;
	public PacketRouterTask(JID receipient, Packet packet) {
		recipient=receipient;
		this.packet=packet;
	}

	public Object getResult() {
		return null;
	}

	public void run() {
		XMPPServer.getInstance().getPacketRouter().route(packet);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance(); 
		try{
			recipient = (JID)ext.readSerializable(in);
			String className = ext.readSafeUTF(in);
			String xmlPacket = ext.readSafeUTF(in);
			Constructor<?> ctr = Class.forName(className).getConstructor(new Class [] {Element.class});
			Element e = DocumentHelper.parseText(xmlPacket).getRootElement();
			packet = (Packet)ctr.newInstance(new Object [] {e});
			}
			catch( Exception e ) {
				Log.debug("readExternal = unable to load");
				throw new IOException();
			}
	}

	

	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance(); 
		ext.writeSerializable(out, recipient);
		ext.writeSafeUTF(out, packet.getClass().getCanonicalName());
		ext.writeSafeUTF(out, packet.getElement().asXML());
	}
}
