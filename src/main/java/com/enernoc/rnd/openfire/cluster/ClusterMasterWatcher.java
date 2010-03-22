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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.MembershipListener;
import org.jgroups.View;
import org.jivesoftware.openfire.cluster.ClusterEventListener;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterMasterWatcher implements ClusterEventListener, MembershipListener {
	protected Logger log = LoggerFactory.getLogger(getClass());

	private Address address;
	private boolean master, enabled = false;

	private Map<String,JGroupsClusterNodeInfo> clusterNodes = new HashMap<String, JGroupsClusterNodeInfo>();
	
	public ClusterMasterWatcher( Address address ) {
		this.address = address;
	}
	
	public void enable() {
		if ( master ) ClusterManager.fireMarkedAsSeniorClusterMember();
		else ClusterManager.fireJoinedCluster(true);
		enabled = true;
	}
	
	public void disable() {
		log.info("disabling ClusterMasterWatcher");
		ClusterManager.fireLeftCluster();
		this.enabled = false;
	}
	
	public void viewAccepted(View v) {
		log.info( "View accepted: {}", v );

		Vector<Address> newNodes = v.getMembers();
		Map<String,JGroupsClusterNodeInfo> nodeMap = new HashMap<String,JGroupsClusterNodeInfo>(newNodes.size());
		nodeMap.putAll( this.clusterNodes );
		
		Address masterNode = newNodes.get(0); 
		
		List<Address> peerNodes = new ArrayList<Address>( this.clusterNodes.size() );
		for ( JGroupsClusterNodeInfo n : this.clusterNodes.values() ) 
			peerNodes.add( n.getAddress() );

		for ( Address node : peerNodes )  // look for missing nodes
			if ( ! newNodes.contains( node ) ) {
				nodeMap.remove( node.toString() );
				if ( enabled ) ClusterManager.fireLeftCluster( node.toString().getBytes() );
			}

		for ( Address node : newNodes ) { // look for new nodes 
			if ( ! peerNodes.contains( node ) ) {
				JGroupsClusterNodeInfo n = new JGroupsClusterNodeInfo( node );
				if ( node.equals(masterNode) ) n.setSenior(true);
				nodeMap.put( node.toString(), n );
				if ( enabled ) ClusterManager.fireJoinedCluster( node.toString().getBytes(), true );
			}
		}
		
		this.clusterNodes = nodeMap;
		
		if ( ! master && masterNode.equals(address) ) {
			if ( enabled ) ClusterManager.fireMarkedAsSeniorClusterMember();
			master = true;
		}
			
		
//		else  try {
//			if ( channel.isConnected() ) channel.getState(null, 20000);
//		}
//		catch ( ChannelException ex ) {
//			log.warn( "Error getting distributed state", ex );
//		}
	}
	
	public Map<String,JGroupsClusterNodeInfo> getNodes() {
		return Collections.unmodifiableMap(this.clusterNodes);
	}

	public void block() {}

	
	public void suspect(Address arg0) {}
	
	//ClusterEventListener interface
	public void joinedCluster() {
		log.info( "This node ({}) has JOINED the cluster.", address );
	}

	
	public void joinedCluster(byte[] nodeID) {
		log.info( "Node {} has JOINED the cluster.", new String(nodeID) );
	}

	
	public void leftCluster() {
		ClusterManager.fireLeftCluster();
		log.info( "This node ({}) has LEFT the cluster.", address );
	}

	
	public void leftCluster(byte[] nodeID) {
		log.info( "Node {} has LEFT the cluster.", new String(nodeID) );
	}

	
	public void markedAsSeniorClusterMember() {
		log.info( "This node ({}) has been marked as MASTER.", address );
	}
}
