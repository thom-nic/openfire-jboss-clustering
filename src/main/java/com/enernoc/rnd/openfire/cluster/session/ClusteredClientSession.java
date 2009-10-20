package com.enernoc.rnd.openfire.cluster.session;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.jivesoftware.openfire.privacy.PrivacyList;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

import com.enernoc.rnd.openfire.cluster.ClusterException;
import com.enernoc.rnd.openfire.cluster.session.task.GetClientSessionTask;

/**
 * This class acts as a remote proxy to send requests to a remote client session. 
 * @author tnichols
 */
public class ClusteredClientSession extends ClusterSession implements ClientSession {

	boolean canFloodOffline;
	boolean stoppedFloodOffline;
	PrivacyList activeList = new PrivacyList();
	PrivacyList defaultList = new PrivacyList();
	Presence presence = new Presence();
	String username;
	boolean anonymous;
	boolean initialized;
	
	public ClusteredClientSession() {}
	
	public ClusteredClientSession( JID jid, byte[] nodeID ) { super( jid, nodeID ); }
	
	@Override
	void doCopy( Session s ) {
		ClientSession cs = (ClientSession)s;
		
		this.canFloodOffline = cs.canFloodOfflineMessages();
		this.stoppedFloodOffline = cs.isOfflineFloodStopped();
		this.activeList = cs.getActiveList();
		this.defaultList = cs.getDefaultList();
		this.presence = cs.getPresence();
		this.anonymous = cs.isAnonymousUser();
		this.initialized = cs.isInitialized();
		try {
			this.username = cs.getUsername();
		}
		catch ( UserNotFoundException ex ) { throw new ClusterException( ex ); }
	}

	@Override
	void doReadExternal( ExternalizableUtil ext, ObjectInput in ) throws IOException, ClassNotFoundException {
		this.canFloodOffline = ext.readBoolean(in);
		this.stoppedFloodOffline = ext.readBoolean(in);
		//FIXME Peak ahead in the buffer to see if the privacy lists are attached?
		//this.activeList.readExternal(in);
		//this.defaultList.readExternal(in);
		try {
			this.presence = new Presence( DocumentHelper.parseText( ext.readSafeUTF(in) ).getRootElement() );
		} catch ( DocumentException ex ) { throw new ClusterException( ex ); }
		this.anonymous = ext.readBoolean(in);
		this.initialized = ext.readBoolean(in);
		this.username = ext.readSafeUTF(in);
	}

	@Override
	void doWriteExternal( ExternalizableUtil ext, ObjectOutput out ) throws IOException {
		ext.writeBoolean(out, this.canFloodOffline );
		ext.writeBoolean(out, this.stoppedFloodOffline );
		if( this.activeList != null ) {
			this.activeList.writeExternal(out);
		}
		if( this.defaultList != null ) {
			this.defaultList.writeExternal(out);
		}
		ext.writeSafeUTF(out, this.presence.toXML() );
		ext.writeBoolean(out, this.anonymous );
		ext.writeBoolean(out, this.initialized );
		ext.writeSafeUTF(out, this.username );
	}

	@Override
	ClusterTask getSessionUpdateTask() {
		return new GetClientSessionTask( super.address );
	}

	
	public boolean canFloodOfflineMessages() {
		super.checkUpdate();
		return this.canFloodOffline;
	}

	
	public PrivacyList getActiveList() {
		super.checkUpdate();
		return this.activeList;
	}

	
	public PrivacyList getDefaultList() {
		super.checkUpdate();
		return this.defaultList;
	}

	
	public Presence getPresence() {
		super.checkUpdate();
		return this.presence;
	}

	
	public String getUsername() throws UserNotFoundException {
		super.checkUpdate();
		return this.username;
	}

	
	public boolean isAnonymousUser() {
		super.checkUpdate();
		return this.anonymous;
	}

	
	public boolean isInitialized() {
		super.checkUpdate();
		return this.initialized;
	}

	
	public boolean isOfflineFloodStopped() {
		super.checkUpdate();
		return this.stoppedFloodOffline;
	}

	
	public int incrementConflictCount() {
		// TODO update server side.
		return 0;
	}

	
	public void setActiveList( PrivacyList aList ) {
		// TODO Auto-generated method stub
		
	}

	
	public void setDefaultList( PrivacyList dList ) {
		// TODO Auto-generated method stub
		
	}

	
	public void setInitialized( boolean init ) {
		// TODO Auto-generated method stub
		
	}

	
	public void setPresence( Presence p ) {
		// TODO Auto-generated method stub
		
	}
}