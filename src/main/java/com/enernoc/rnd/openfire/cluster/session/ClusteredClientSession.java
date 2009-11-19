package com.enernoc.rnd.openfire.cluster.session;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.privacy.PrivacyList;
import org.jivesoftware.openfire.privacy.PrivacyListManager;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.ClientSessionInfo;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.enernoc.rnd.openfire.cluster.session.task.ClientSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.DeliverRawTextTask;
import com.enernoc.rnd.openfire.cluster.session.task.RemoteSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.RemoteSessionTask.Operation;
import com.enernoc.rnd.openfire.cluster.task.ProcessPacketTask;

/**
 * This class acts as a remote proxy to send requests to a remote client session. 
 * @author tnichols
 */
public class ClusteredClientSession extends ClusterSession implements ClientSession {

	private long initialized = -1;
	
	public ClusteredClientSession() {}
	public ClusteredClientSession( JID jid, byte[] nodeID ) { 
		super( jid, nodeID ); 
		}
	
	@Override
	void doCopy( Session s ) {
		ClientSession cs = (ClientSession)s;
		this.initialized = cs.isInitialized() ? 1 : 0;
	}

	@Override
	void doReadExternal( ExternalizableUtil ext, ObjectInput in ) throws IOException, ClassNotFoundException {
		this.initialized = ext.readLong(in);
		
	}

	@Override
	void doWriteExternal( ExternalizableUtil ext, ObjectOutput out ) throws IOException {
		ext.writeLong(out, this.initialized );
	}

	
	public boolean canFloodOfflineMessages() {
		// Code copied from LocalClientSession to avoid remote calls
        if(isOfflineFloodStopped()) {
            return false;
        }
        String username = getAddress().getNode();
        for (ClientSession session : SessionManager.getInstance().getSessions(username)) {
            if (session.isOfflineFloodStopped()) {
                return false;
            }
        }
        return true;
	}

	
	public PrivacyList getActiveList() {
		Cache<String, ClientSessionInfo> cache = SessionManager.getInstance().getSessionInfoCache();
        ClientSessionInfo sessionInfo = cache.get(getAddress().toString());
        if (sessionInfo != null && sessionInfo.getActiveList() != null) {
            return PrivacyListManager.getInstance().getPrivacyList(address.getNode(), sessionInfo.getActiveList());
        }
        return null;
	}

	
	public PrivacyList getDefaultList() {
		Cache<String, ClientSessionInfo> cache = SessionManager.getInstance().getSessionInfoCache();
        ClientSessionInfo sessionInfo = cache.get(getAddress().toString());
        if (sessionInfo != null && sessionInfo.getDefaultList() != null) {
            return PrivacyListManager.getInstance().getPrivacyList(address.getNode(), sessionInfo.getDefaultList());
        }
        return null;
	}

	
	public Presence getPresence() {
		Cache<String,ClientSessionInfo> cache = SessionManager.getInstance().getSessionInfoCache();
        ClientSessionInfo sessionInfo = cache.get(getAddress().toString());
        if (sessionInfo != null) {
            return sessionInfo.getPresence();
            }
        return null;
	}

	
	public String getUsername() throws UserNotFoundException {
		return address.getNode();
	}

	
	public boolean isAnonymousUser() {
		return SessionManager.getInstance().isAnonymousRoute(getAddress());
	}

	
	public boolean isInitialized() {
        if (initialized == -1) {
            Presence presence = getPresence();
            if (presence != null && presence.isAvailable()) {
                // Optimization to avoid making a remote call
                initialized = 1;
            }
            else {
                ClusterTask task = getRemoteSessionTask(RemoteSessionTask.Operation.isInitialized);
                initialized = (Boolean) doSynchronousClusterTask(task) ? 1 : 0;
            }
        }
        return initialized == 1;
    }

	
	public boolean isOfflineFloodStopped() {
		Cache<String, ClientSessionInfo> cache = SessionManager.getInstance().getSessionInfoCache();
        ClientSessionInfo sessionInfo = cache.get(getAddress().toString());
        return sessionInfo != null && sessionInfo.isOfflineFloodStopped();
	}

	
	public int incrementConflictCount() {
		ClusterTask task = getRemoteSessionTask(RemoteSessionTask.Operation.incrementConflictCount);
        return (Integer) doSynchronousClusterTask(task);
	}

	
	public void setActiveList( PrivacyList aList ) {
		// Highly unlikely that a list is change to a remote session but still possible
        doClusterTask(new SetPrivacyListTask(address, true, aList));
	}

	
	public void setDefaultList( PrivacyList dList ) {
		// Highly unlikely that a list is change to a remote session but still possible
        doClusterTask(new SetPrivacyListTask(address, false, dList));
	}

	
	public void setInitialized( boolean init ) {
		doClusterTask(new SetInitializedTask(address, init));
	}

	
	public void setPresence( Presence p ) {
		try {
			doClusterTask(new SetPresenceTask(address, p));
        } catch (IllegalStateException e) {
            // Remote node is down
            if (p.getType() == Presence.Type.unavailable) {
                // Ignore unavailable presence (since session is already unavailable - at least to us)
                return;
            }
            throw e;
        }
	}

	@Override
	ClusterTask getDeliverRawTextTask(String text) {
		return new DeliverRawTextTask(this, address, text);
	}

	@Override
	ClusterTask getProcessPacketTask(Packet packet) {
        //return new ProcessPacketTask(this, address, packet);
		return new ProcessPacketTask(packet);
	}

	@Override
	RemoteSessionTask getRemoteSessionTask(Operation operation) {
		 return new ClientSessionTask(address, operation);
	}
	
	private static class SetPresenceTask extends ClientSessionTask {
        private Presence presence;

        public SetPresenceTask() {
            super();
        }

        protected SetPresenceTask(JID address, Presence presence) {
            super(address, null);
            this.presence = presence;
        }

        public void run() {
            ((ClientSession)getSession()).setPresence(presence);
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            ExternalizableUtil.getInstance().writeSerializable(out, (DefaultElement) presence.getElement());
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            Element packetElement = (Element) ExternalizableUtil.getInstance().readSerializable(in);
            presence = new Presence(packetElement, true);
        }
    }
	
	private static class SetPrivacyListTask extends ClientSessionTask {
        private boolean activeList;
        private String listName;

        public SetPrivacyListTask() {
            super();
        }

        protected SetPrivacyListTask(final JID address, final boolean activeList, final PrivacyList list) {
            super(address, null);
            this.activeList = activeList;
            this.listName = list != null ? list.getName() : null;
        }

        public void run() {
            final ClientSession session = ((ClientSession) getSession());
            PrivacyList list = null;
            // Get the privacy list to set
            if (listName != null) {
                try {
                    final String username = session.getUsername();
                    list = PrivacyListManager.getInstance().getPrivacyList(username, listName);
                } catch (final UserNotFoundException e) {
                    // Should never happen
                }
            }
            // Set the privacy list to the session
            if (activeList) {
                session.setActiveList(list);
            }
            else {
                session.setDefaultList(list);
            }
        }

        public void writeExternal(final ObjectOutput out) throws IOException {
            super.writeExternal(out);
            ExternalizableUtil.getInstance().writeBoolean(out, activeList);
            ExternalizableUtil.getInstance().writeBoolean(out, listName != null);
            if (listName != null) {
                ExternalizableUtil.getInstance().writeSafeUTF(out, listName);
            }
        }

        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            activeList = ExternalizableUtil.getInstance().readBoolean(in);
            if (ExternalizableUtil.getInstance().readBoolean(in)) {
                listName = ExternalizableUtil.getInstance().readSafeUTF(in);

            }
        }
    }

    private static class SetInitializedTask extends ClientSessionTask {
        private boolean initialized;

        public SetInitializedTask() {
            super();
        }

        protected SetInitializedTask(final JID address, final boolean initialized) {
            super(address, null);
            this.initialized = initialized;
        }

        public void run() {
            ((ClientSession) getSession()).setInitialized(initialized);
        }

        public void writeExternal(final ObjectOutput out) throws IOException {
            super.writeExternal(out);
            ExternalizableUtil.getInstance().writeBoolean(out, initialized);
        }

        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialized = ExternalizableUtil.getInstance().readBoolean(in);
        }
    }
}