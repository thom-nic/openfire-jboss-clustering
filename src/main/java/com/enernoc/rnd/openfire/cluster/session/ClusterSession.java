package com.enernoc.rnd.openfire.cluster.session;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.UnknownHostException;
import java.util.Date;

import org.jivesoftware.openfire.StreamID;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.cache.CacheFactory;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import com.enernoc.rnd.openfire.cluster.ClusterException;
import com.enernoc.rnd.openfire.cluster.task.CloseSessionTask;
import com.enernoc.rnd.openfire.cluster.task.ProcessPacketTask;

/**
 * Session proxy for use across clusters
 * @author tnichols
 */
public abstract class ClusterSession implements Session, Externalizable {

	protected final Logger log = LoggerFactory.getLogger( getClass() ); 
	long refreshInterval = 20000;
	
	byte[] nodeId;
	JID address = new JID();
	long lastUpdated = 0;
	Date created;
	String hostAddr;
	String hostName;
	String serverName;
	Date lastActive;
	long clientPackets;
	long serverPackets;
	int status;
	String streamID; 
	boolean closed = false;
	boolean secure = false;
	
	public ClusterSession() {}
	
	public ClusterSession( JID jid, byte[] nodeID ) {
		this.nodeId = nodeID;
		this.address = jid;
	}

	
	public void process( Packet packet ) {
		CacheFactory.doClusterTask( new ProcessPacketTask( packet ), nodeId );
	}

	
	public void deliverRawText( String txt ) {
		throw new UnsupportedOperationException();
	}

	
	public boolean validate() {
		log.warn( "NO-OP : validate not implemented!" );
		return true;
	}

	
	public void close() {
		CacheFactory.doClusterTask( new CloseSessionTask(address), nodeId );
		this.closed = true; // TODO assume for now; maybe the task should return a value.
	}
	
	/**
	 * Subclasses should override this to indicate which task executes the 
	 * correct method for retrieving their remote session type. i.e. a 
	 * GetClientSessionTask would be returned by a ClusteredClientSession
	 * implementation.  The task should return a Session implementation.
	 * @return
	 */
	abstract ClusterTask getSessionUpdateTask();
	
	abstract void doCopy( Session s );
	
	abstract void doWriteExternal( ExternalizableUtil ext, ObjectOutput out ) throws IOException;
	abstract void doReadExternal( ExternalizableUtil ext, ObjectInput in ) throws IOException, ClassNotFoundException;
	
	/** 
	 * Will update from the remote server if it hasn't updated in a given interval.
	 * 
	 */
	protected void checkUpdate() {
		// only update from remote server every so often.
		if ( ( System.currentTimeMillis() - lastUpdated ) < refreshInterval ) return;
		
		ClusterTask task = getSessionUpdateTask();
		CacheFactory.doSynchronousClusterTask( task, this.nodeId );
		this.copy( (Session)task.getResult() );
		
		lastUpdated = System.currentTimeMillis();
	}
	
	/**
	 * Unfortunately Session implementations aren't serializable, so we need 
	 * to copy the info to send it across the network. 
	 * Override to get additional properties from session subclasses.
	 * @param s
	 */
	public void copy( Session s ) {
		this.address = s.getAddress();
		this.created = s.getCreationDate();
		try {
			this.hostAddr = s.getHostAddress();
			this.hostName = s.getHostName();
		}
		catch ( UnknownHostException ex ) {
			throw new ClusterException( ex );
		}
		this.serverName = s.getServerName();
		this.lastActive = s.getLastActiveDate();
		this.clientPackets = s.getNumClientPackets();
		this.serverPackets = s.getNumServerPackets();
		this.status = s.getStatus();
		this.closed = s.isClosed();
		this.secure = s.isSecure();
		this.streamID = s.getStreamID().getID();
		doCopy(s);
	}
	
	
	
	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance();
		this.address.writeExternal(out);
		ext.writeByteArray(out, nodeId);
		ext.writeLong(out, created.getTime());
		ext.writeSafeUTF(out, hostAddr);
		ext.writeSafeUTF(out, hostName);
		ext.writeSafeUTF(out, serverName);
		ext.writeLong(out, lastActive.getTime());
		ext.writeLong(out, clientPackets);
		ext.writeLong(out, serverPackets);
		ext.writeInt(out, status);
		ext.writeBoolean(out, closed);
		ext.writeBoolean(out, secure);
		ext.writeSafeUTF(out, streamID);
		doWriteExternal( ext, out );
	}
	
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance();
		this.address.readExternal(in);
		this.nodeId = ext.readByteArray(in);
		this.created = new Date( ext.readLong(in ) );
		this.hostAddr = ext.readSafeUTF(in);
		this.hostName = ext.readSafeUTF(in);
		this.serverName = ext.readSafeUTF(in);
		this.lastActive = new Date(ext.readLong(in));
		this.clientPackets = ext.readLong(in);
		this.serverPackets = ext.readLong(in);
		this.status = ext.readInt(in);
		this.closed = ext.readBoolean(in);
		this.secure = ext.readBoolean(in);
		this.streamID = ext.readSafeUTF(in);
		doReadExternal( ext, in );
	}
	
	
	public JID getAddress() {
		return this.address;
	}
	
	
	public Date getCreationDate() {
		return created;
	}

	
	public String getHostAddress() throws UnknownHostException {
		return this.hostAddr;
	}

	
	public String getHostName() throws UnknownHostException {
		return this.hostName;
	}

	
	public Date getLastActiveDate() {
		this.checkUpdate();
		return this.lastActive;
	}

	
	public long getNumClientPackets() {
		this.checkUpdate();
		return this.clientPackets;
	}

	
	public long getNumServerPackets() {
		this.checkUpdate();
		return this.serverPackets;
	}

	
	public String getServerName() {
		return this.serverName;
	}

	
	public int getStatus() {
		this.checkUpdate();
		return this.status;
	}

	
	public StreamID getStreamID() {
		return new StreamID() {
			 public String getID() {
				return streamID;
			}
		};
	}

	
	public boolean isClosed() {
		this.checkUpdate();
		return this.closed;
	}

	
	public boolean isSecure() {
		this.checkUpdate();
		return this.secure;
	}
}