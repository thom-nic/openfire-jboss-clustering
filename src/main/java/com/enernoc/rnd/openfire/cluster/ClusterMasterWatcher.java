package com.enernoc.rnd.openfire.cluster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jivesoftware.openfire.cluster.ClusterEventListener;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterMasterWatcher implements ClusterEventListener, Receiver {
	protected Logger log = LoggerFactory.getLogger(getClass());

	final Channel channel;
	Address myAddr;

	private Map<String,JGroupsClusterNodeInfo> clusterNodes = new HashMap<String, JGroupsClusterNodeInfo>();
	
	public ClusterMasterWatcher( Channel ch ) {
		try {
			this.channel = ch;
			channel.setReceiver(this);
			ClusterManager.fireJoinedCluster(true);
		} catch ( Exception ex ) {
			throw new ClusterException( "Error during channel connect.", ex );
		}
	}
	
	@Override
	public void viewAccepted(View v) {
		if ( this.myAddr == null ) myAddr = channel.getLocalAddress();
		Vector<Address> newNodes = v.getMembers();
		Map<String,JGroupsClusterNodeInfo> nodeMap = new HashMap<String,JGroupsClusterNodeInfo>(newNodes.size());
		nodeMap.putAll( this.clusterNodes );
		
		Address masterNode = newNodes.get(0); 
		
		List<Address> peerNodes = new ArrayList<Address>( this.clusterNodes.size() );
		for ( JGroupsClusterNodeInfo n : this.clusterNodes.values() ) 
			peerNodes.add( n.getAddress() );

		for ( Address node : peerNodes )  // look for missing nodes
			if ( ! newNodes.contains( node ) ) {
				ClusterManager.fireLeftCluster( node.toString().getBytes() );
				if ( masterNode.equals(this.myAddr) ) nodeMap.remove( node.toString() );
			}

		for ( Address node : newNodes ) { // look for new nodes 
			if ( ! peerNodes.contains( node ) && ! node.equals(myAddr) ) {
				ClusterManager.fireJoinedCluster( node.toString().getBytes(), true );
				if ( masterNode.equals(this.myAddr) ) {
					JGroupsClusterNodeInfo n = new JGroupsClusterNodeInfo( node );
					if ( node.equals(masterNode) ) n.setSenior(true);
					nodeMap.put( node.toString(), n );
				}
			}
		}
		
		this.clusterNodes = nodeMap;
		
		if ( masterNode.equals(this.myAddr) ) // TODO don't fire if we're already senior.
			ClusterManager.fireMarkedAsSeniorClusterMember();
		
		else  try {
			if ( channel.isConnected() ) channel.getState(null, 20000);
		}
		catch ( ChannelException ex ) {
			log.warn( "Error getting distributed state", ex );
		}
	}
	
	public Map<String,JGroupsClusterNodeInfo> getNodes() {
		return Collections.unmodifiableMap(this.clusterNodes);
	}

	public Address getLocalAddress() {
		return this.myAddr;
	}

	/**
	 * Notify other nodes in the cluster that we have left.
	 */
	@Override
	public void leftCluster() {
		this.channel.close();
	}
			
	// don't care about these events:
	
	@Override public void markedAsSeniorClusterMember() {}
	@Override public void joinedCluster() {}
	@Override public void joinedCluster(byte[] arg0) {}
	@Override public void leftCluster(byte[] arg0) {}

	@Override
	public byte[] getState() {
		log.debug("getState() called");
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(data);
		try {
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

	@Override
	public void receive(Message arg0) {}

	@Override
	public void setState(byte[] st) {
		log.debug("Cluster state changed: {}", new String(st) );
		ByteArrayInputStream data = new ByteArrayInputStream(st);
		DataInputStream in = new DataInputStream(data);
		try {
			ExternalizableUtil.getInstance().readExternalizableMap(in, this.clusterNodes, getClass().getClassLoader());
		}
		catch ( IOException ex ) {
			log.error( "Couldn't deserialize state", ex );
		}
		finally { try { in.close(); } catch ( Exception ex ) {} }
	}

	@Override
	public void block() {}

	@Override
	public void suspect(Address arg0) {}
}
