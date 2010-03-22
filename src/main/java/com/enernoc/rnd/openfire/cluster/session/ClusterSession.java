/**
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
**/
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
import com.enernoc.rnd.openfire.cluster.session.task.RemoteSessionTask;
import com.enernoc.rnd.openfire.cluster.task.ProcessPacketTask;


/**
 * Session proxy for use across clusters
 * @author tnichols
 */
public abstract class ClusterSession implements Session, Externalizable {

	protected static final Logger log = LoggerFactory.getLogger( ClusterSession.class ); 
	private static final long refreshInterval = 20000;
	
	protected byte[] nodeID;
	protected JID address = new JID();
	
	//Cache content that never changes
	protected StreamID streamID;
	private Date created;
	private String hostAddr;
	private String hostName;
	private String serverName; 
	
	public ClusterSession() {}
	public ClusterSession( JID jid, byte[] nodeID ) {
		this.nodeID = nodeID;
		this.address = jid;
	}
	
	abstract void doCopy( Session s );
	
	abstract void doWriteExternal( ExternalizableUtil ext, ObjectOutput out ) throws IOException;
	abstract void doReadExternal( ExternalizableUtil ext, ObjectInput in ) throws IOException, ClassNotFoundException;
	
	abstract RemoteSessionTask getRemoteSessionTask(RemoteSessionTask.Operation operation);
    abstract ClusterTask getDeliverRawTextTask(String text);
    abstract ClusterTask getProcessPacketTask(Packet packet);
	
    /**
     * Invokes a task on the remote cluster member synchronously and returns the result of
     * the remote operation.
     *
     * @param task        the ClusterTask object to be invoked on a given cluster member.
     * @return result of remote operation.
     * @throws IllegalStateException if requested node was not found or not running in a cluster.
     */
    protected Object doSynchronousClusterTask(ClusterTask task) {
        return CacheFactory.doSynchronousClusterTask(task, nodeID);
    }
    
    /**
     * Invokes a task on the remote cluster member in an asynchronous fashion.
     *
     * @param task the task to be invoked on the specified cluster member.
     * @throws IllegalStateException if requested node was not found or not running in a cluster. 
     */
    protected void doClusterTask(ClusterTask task) {
        CacheFactory.doClusterTask(task, nodeID);
    }
    
	/** 
	 * Will update from the remote server if it hasn't updated in a given interval.
	 * 
	 */
    @Deprecated
	protected void checkUpdate() {
		// only update from remote server every so often.
		/*
    	if ( ( System.currentTimeMillis() - lastUpdated ) > refreshInterval ) return;
		
		ClusterTask task = getSessionUpdateTask();
		Object o = CacheFactory.doSynchronousClusterTask( task, this.nodeID );
		this.copy( (Session) o );
		
		lastUpdated = System.currentTimeMillis();
		*/
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
		this.streamID = s.getStreamID();
		doCopy(s);
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
		ClusterTask task = getRemoteSessionTask(RemoteSessionTask.Operation.getLastActiveDate);
        return (Date) doSynchronousClusterTask(task);
	}

	
	public long getNumClientPackets() {
		ClusterTask task = getRemoteSessionTask(RemoteSessionTask.Operation.getNumClientPackets);
        return (Long) doSynchronousClusterTask(task);
	}

	
	public long getNumServerPackets() {
		ClusterTask task = getRemoteSessionTask(RemoteSessionTask.Operation.getNumServerPackets);
        return (Long) doSynchronousClusterTask(task);
	}

	
	public String getServerName() {
		return this.serverName;
	}

	/**
     * Remote sessions are always authenticated. Otherwise, they won't be visibile to other
     * cluster nodes. When the session is closed it will no longer be visible to other nodes
     * so {@link #STATUS_CLOSED} is never returned. 
     *
     * @return the authenticated status.
     */
	public int getStatus() {
		return STATUS_AUTHENTICATED;
	}

	
	public StreamID getStreamID() {
		// Get it once and cache it since it never changes
        if (streamID == null) {
            ClusterTask task = getRemoteSessionTask(RemoteSessionTask.Operation.getStreamID);
            String id = (String) doSynchronousClusterTask(task);
            streamID = new BasicStreamID(id);
        }
        return streamID;
	}

	public boolean isClosed() {
		ClusterTask task = getRemoteSessionTask(RemoteSessionTask.Operation.isClosed);
		return (Boolean) doSynchronousClusterTask(task);
	}

	
	public boolean isSecure() {
		ClusterTask task = getRemoteSessionTask(RemoteSessionTask.Operation.isSecure);
        return (Boolean) doSynchronousClusterTask(task);
	}
	
	

	public void process( Packet packet ) {
		packet.setTo(this.getAddress());
		CacheFactory.doClusterTask( new ProcessPacketTask( packet ), nodeID );
	}

	
	public void deliverRawText( String txt ) {
		throw new UnsupportedOperationException();
	}

	
	public boolean validate() {
		log.warn( "NO-OP : validate not implemented!" );
		return true;
	}

	
	public void close() {
		doSynchronousClusterTask(getRemoteSessionTask(RemoteSessionTask.Operation.close));
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance();
		this.address.writeExternal(out);
		ext.writeByteArray(out, nodeID);
		ext.writeLong(out, created.getTime());
		ext.writeSafeUTF(out, hostAddr);
		ext.writeSafeUTF(out, hostName);
		ext.writeSafeUTF(out, serverName);
		//FIXME need to write out the stream info or remove external implementation
		ext.writeSafeUTF(out, streamID.getID());
		doWriteExternal( ext, out );
	}
	
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance();
		this.address.readExternal(in);
		this.nodeID = ext.readByteArray(in);
		this.created = new Date( ext.readLong(in ) );
		this.hostAddr = ext.readSafeUTF(in);
		this.hostName = ext.readSafeUTF(in);
		this.serverName = ext.readSafeUTF(in);
		//FIXME need to write out the stream info or remove external implementation
		this.streamID = new BasicStreamID(ext.readSafeUTF(in));
		doReadExternal( ext, in );
	}
    
	/**
     * Simple implementation of the StreamID interface to hold the stream ID of
     * the surrogated session.
     */
    protected static class BasicStreamID implements StreamID {
        String id;

        public BasicStreamID(String id) {
            this.id = id;
        }

        public String getID() {
            return id;
        }

        public String toString() {
            return id;
        }

        public int hashCode() {
            return id.hashCode();
        }
    }
}