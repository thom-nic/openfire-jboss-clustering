package com.enernoc.rnd.openfire.cluster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jivesoftware.openfire.cluster.ClusterEventListener;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterMasterWatcher implements Receiver, ClusterEventListener, MessageListener, MembershipListener {
	protected Logger log = LoggerFactory.getLogger(getClass());

	private Address myAddr;
	private boolean master, enabled = false;

	private Map<String,JGroupsClusterNodeInfo> clusterNodes = new HashMap<String, JGroupsClusterNodeInfo>();
	
	public ClusterMasterWatcher( Address myAddr ) {
		this.myAddr = myAddr;
	}
	
	public void enable() {
		if ( master ) ClusterManager.fireMarkedAsSeniorClusterMember();
		else ClusterManager.fireJoinedCluster(true);
		enabled = true;
	}
	
	public void disable() {
		log.info("disabling ClusterMasterWatcher");
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
			if ( ! peerNodes.contains( node ) && ! node.equals(myAddr) ) {
				JGroupsClusterNodeInfo n = new JGroupsClusterNodeInfo( node );
				if ( node.equals(masterNode) ) n.setSenior(true);
				nodeMap.put( node.toString(), n );
				if ( enabled ) ClusterManager.fireJoinedCluster( node.toString().getBytes(), true );
			}
		}
		
		this.clusterNodes = nodeMap;
		
		if ( ! master && masterNode.equals(this.myAddr) ) {
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

	public Address getLocalAddress() {
		return this.myAddr;
	}

	public byte[] getState() {
		log.debug("getState() called");
		if ( true ) return new byte[] {};
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(data);
			ExternalizableUtil.getInstance().writeExternalizableMap(out, this.clusterNodes);
			out.flush();
			return data.toByteArray();
		}
		catch ( IOException ex ) {
			log.error( "Couldn't serialize state", ex );
			return null;
		}
		finally { try { out.close(); } catch ( Exception ex ) {} }
	}

	
	public void receive(Message message) {
		log.debug("recieved message {}", message.toStringAsObject());
	}

	
	public void setState(byte[] st) {
		log.debug("Cluster state changed: {}", new String(st) );
		if ( true ) return;
		ByteArrayInputStream data = new ByteArrayInputStream(st);
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(data);
			ExternalizableUtil.getInstance().readExternalizableMap(in, this.clusterNodes, getClass().getClassLoader());
		}
		catch ( IOException ex ) {
			log.error( "Couldn't deserialize state", ex );
		}
		finally { try { in.close(); } catch ( Exception ex ) {} }
	}

	
	public void block() {}

	
	public void suspect(Address arg0) {}
	
	//ClusterEventListener interface
	public void joinedCluster() {
		log.info( "This node ({}) has JOINED the cluster.", getLocalAddress() );
	}

	
	public void joinedCluster(byte[] nodeID) {
		log.info( "Node {} has JOINED the cluster.", new String(nodeID) );
	}

	
	public void leftCluster() {
		log.info( "This node ({}) has LEFT the cluster.", getLocalAddress() );
	}

	
	public void leftCluster(byte[] nodeID) {
		log.info( "Node {} has LEFT the cluster.", new String(nodeID) );
	}

	
	public void markedAsSeniorClusterMember() {
		log.info( "This node ({}) has been marked as MASTER.", getLocalAddress() );
	}
}
