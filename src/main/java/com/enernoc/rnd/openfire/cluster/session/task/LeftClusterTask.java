/**
 * 
 */
package com.enernoc.rnd.openfire.cluster.session.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.util.cache.CacheFactory;
import org.jivesoftware.util.cache.ClusterTask;

import com.enernoc.rnd.openfire.cluster.cache.ClusteredCacheFactory;

/**
 * @author macdiesel
 *
 */
public class LeftClusterTask implements ClusterTask {

	NodeID nodeId;
	
	/**
	 * @param nodeId
	 */
	public LeftClusterTask(NodeID nodeId) {
		super();
		this.nodeId = nodeId;
	}

	/* (non-Javadoc)
	 * @see org.jivesoftware.util.cache.ClusterTask#getResult()
	 */
	public Object getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if(XMPPServer.getInstance().getNodeID().equals(nodeId)) {
			ClusterManager.fireLeftCluster();
		} else {
			ClusterManager.fireLeftCluster(nodeId.toByteArray());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub

	}

}
