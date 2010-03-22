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
package com.enernoc.rnd.openfire.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jgroups.Address;
import org.jivesoftware.openfire.cluster.ClusterNodeInfo;
import org.jivesoftware.openfire.cluster.NodeID;

public class JGroupsClusterNodeInfo implements ClusterNodeInfo, Externalizable {

	Address address;
	String hostName;
	long joinedTime;
	NodeID nodeID;
	boolean senior = false;
	
	/**
	 * Constructor used when deserializing from an ObjectInputStream.
	 */
	public JGroupsClusterNodeInfo() {}
	
	/**
	 * This takes a node ID and automatically fills in the host name and 
	 * join time for this server instance; assuming that the server is just
	 * now joining the cluster.
	 * @param nodeID
	 */
	public JGroupsClusterNodeInfo( Address a ) {
		this.address = a;
		this.hostName = a.toString();
		this.nodeID = NodeID.getInstance(this.hostName.getBytes() );
		this.joinedTime = System.currentTimeMillis();
	}
	
	JGroupsClusterNodeInfo( Address a, long time ) {
		this( a );
		this.joinedTime = time;
	}	

	
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.address = (Address)in.readObject();
		this.hostName = in.readUTF();
		this.joinedTime = in.readLong();
		this.nodeID = NodeID.getInstance(this.hostName.getBytes());
	}

	
	public void writeExternal(ObjectOutput out) throws IOException {
		this.address.writeExternal(out); // TODO NPE check
		out.writeUTF(hostName);
		out.writeLong(joinedTime);
	}
	
	
	public boolean equals(Object obj) {
		return obj instanceof ClusterNodeInfo && 
		this.nodeID.equals( ((ClusterNodeInfo)obj).getNodeID() );
	}
	
	
	public int hashCode() {
		return this.nodeID.hashCode();
	}
	
	protected void setSenior( boolean senior ) {
		this.senior = senior;
	}

	
	public String getHostName() {
		return this.hostName;
	}

	
	public long getJoinedTime() {
		return this.joinedTime;
	}

	
	public NodeID getNodeID() {
		return this.nodeID;
	}

	
	public boolean isSeniorMember() {
		return this.senior;
	}
	
	public Address getAddress() {
		return this.address;
	}
}
