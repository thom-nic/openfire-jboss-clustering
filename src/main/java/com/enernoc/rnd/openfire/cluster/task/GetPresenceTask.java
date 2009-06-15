package com.enernoc.rnd.openfire.cluster.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.Log;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.Presence;

public class GetPresenceTask implements ClusterTask {

	String jid;
	Presence p;
	public GetPresenceTask(String jid){
		this.jid=jid;
	}
	
	public Object getResult() {
		return this.p;
	}

	public void run() {
		try {
			XMPPServer server = XMPPServer.getInstance(); 
			User u = server.getUserManager().getUser(jid);
			p = server.getPresenceManager().getPresence(u);
		} catch ( UserNotFoundException e ) {
			Log.debug("Presence not found for " + jid, e);
		}
	}

	 public void writeExternal(ObjectOutput out) throws IOException {
		 	ExternalizableUtil ext = ExternalizableUtil.getInstance();
		 	ext.writeSafeUTF(out, jid);
	        ext.writeSerializable(out, (Serializable)p.getElement());
	    }

	    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		 	ExternalizableUtil ext = ExternalizableUtil.getInstance();
	    	this.jid= ext.readSafeUTF(in);
	        Element packetElement = (Element) ext.readSerializable(in);
	        p = new Presence(packetElement, true);
	    }

}
