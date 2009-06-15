package com.enernoc.rnd.openfire.cluster;

import java.net.UnknownHostException;
import java.util.Date;

import org.jivesoftware.openfire.StreamID;
import org.jivesoftware.openfire.privacy.PrivacyList;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.cache.CacheFactory;
import org.jivesoftware.util.cache.ClusterTask;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.enernoc.rnd.openfire.cluster.task.GetPresenceTask;

/**
 * This class acts as a remote proxy to send requests to a remote client session. 
 * @author tnichols
 */
public class ClusteredClientSession implements ClientSession {

	ClientSession session;
	byte[] nodeId;
	public ClusteredClientSession( ClientSession cs, byte[] memberId ) {
		this.nodeId=memberId;
		session=cs;
	}
	
	public Presence getPresence() {
		String node = session.getAddress().getNode();
		ClusterTask ct = new GetPresenceTask(node); 
		return (Presence)CacheFactory.doSynchronousClusterTask(ct, nodeId);
	}

	public void setPresence(Presence presence) {
		
	}

	public boolean canFloodOfflineMessages() {
		return session.canFloodOfflineMessages();
	}

	public PrivacyList getActiveList() {
		return session.getActiveList();
	}

	public PrivacyList getDefaultList() {
		return session.getDefaultList();
	}

	public String getUsername() throws UserNotFoundException {
		return session.getUsername();
	}

	public boolean isAnonymousUser() {
		return session.isAnonymousUser();
	}

	public boolean isInitialized() {
		return session.isInitialized();
	}

	public boolean isOfflineFloodStopped() {
		return session.isOfflineFloodStopped();
	}

	public int incrementConflictCount() {
		return session.incrementConflictCount();
	}

	public void setActiveList(PrivacyList activeList) {
		session.setActiveList(activeList);
	}

	public void setDefaultList(PrivacyList defaultList) {
		session.setDefaultList(defaultList);
	}

	public void setInitialized(boolean isInit) {
		session.setInitialized(isInit);
	}

	public void close() {
		session.close();
	}

	public JID getAddress() {
		return session.getAddress();
	}

	public void deliverRawText(String text) {
		// TODO Auto-generated method stub
	}

	public Date getCreationDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHostAddress() throws UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHostName() throws UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getLastActiveDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getNumClientPackets() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getNumServerPackets() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	public StreamID getStreamID() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isClosed() {
		return session.isClosed();
	}

	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	public void process(Packet packet) {
		// TODO Auto-generated method stub

	}

	public boolean validate() {
		// TODO Auto-generated method stub
		return false;
	}
}
