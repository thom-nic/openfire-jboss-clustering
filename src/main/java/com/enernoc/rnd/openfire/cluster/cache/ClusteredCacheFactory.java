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
package com.enernoc.rnd.openfire.cluster.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;

import org.jboss.cache.CacheFactory;
import org.jboss.cache.DefaultCacheFactory;
import org.jboss.cache.config.ConfigurationException;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannelFactory;
import org.jgroups.Message;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.cluster.ClusterNodeInfo;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactoryStrategy;
import org.jivesoftware.util.cache.CacheWrapper;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enernoc.rnd.openfire.cluster.ClusterException;
import com.enernoc.rnd.openfire.cluster.ClusterMasterWatcher;
import com.enernoc.rnd.openfire.cluster.ExternalUtil;
import com.enernoc.rnd.openfire.cluster.JBossClusterPlugin;
import com.enernoc.rnd.openfire.cluster.JGroupsClusterNodeInfo;

public class ClusteredCacheFactory implements CacheFactoryStrategy {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	// TODO replace with CacheManager?
	@SuppressWarnings("unchecked")
	private CacheFactory factory = new DefaultCacheFactory();
	@SuppressWarnings("unchecked")
	private org.jboss.cache.Cache cache;
	
	private MessageDispatcher dispatcher;
	private ClusterMasterWatcher masterWatcher;
	private TaskExecutor taskHandler;
	private Channel channel;

	/**
	 * This is called by the {@link ClusterManager}, when 
	 * {@link JBossClusterPlugin}.initializePlugin() is executed.
	 */
	@SuppressWarnings("unchecked")
	public boolean startCluster() {
		log.info( "Cluster starting..." );
		try {
			//Initilize the caches
			String cacheConfig = JiveGlobals.getProperty( JBossClusterPlugin.CLUSTER_CACHE_CONFIG_PROPERTY );
			URL cacheConfigURL = cacheConfig != null ? new URL( cacheConfig ) : 
				JBossClusterPlugin.class.getResource( "/cache.xml" );
			InputStream cfgStream = cacheConfigURL.openStream();
			try {
				this.cache = factory.createCache(cfgStream, true);
			} catch (ConfigurationException e) {
				log.error("Exception creating the cache, clustering not started", e);
				throw e;
			} finally {
				try {
					cfgStream.close();
				} catch (IOException ex) {}
			}
			
			String clusterConfig = JiveGlobals.getProperty( JBossClusterPlugin.CLUSTER_JGROUPS_CONFIG_PROPERTY );
			URL config = clusterConfig != null ? getClass().getResource( clusterConfig ) : 
				getClass().getResource("/tcp.xml");
			
			//Channel Setup
			JChannelFactory channelFactory = new JChannelFactory( config );
			this.channel = channelFactory.createChannel();
			
			//Watcher setup
			this.masterWatcher = new ClusterMasterWatcher(this.channel.getLocalAddress());
			this.taskHandler = new TaskExecutor();
			ClusterManager.addListener(masterWatcher);
			this.dispatcher = new MessageDispatcher( channel, taskHandler, masterWatcher, taskHandler, true );
			
			//Connect to the replication
			this.channel.connect( "OpenFire-Cluster" ); // TODO make configurable
			XMPPServer.getInstance().setNodeID( NodeID.getInstance( channel.getLocalAddress().toString().getBytes() ) );
			log.info( "Local address: {}", channel.getLocalAddress() ); 
			log.info( "NodeId: {}", new String(XMPPServer.getInstance().getNodeID().toByteArray()) );
			
			
			//TODO add some code here so that when a certain number of retries
			//or a time limit is hit we throw an exeception and stop waiting
			while(ClusterManager.getNodesInfo().size() < 1) {
				Thread.sleep(500);
			}
			
        	masterWatcher.enable();
			ExternalizableUtil.getInstance().setStrategy( new ExternalUtil() );
			//setup the caches to use the clusteredcache factory
			ClusterManager.fireJoinedCluster(false);
			if(isSeniorClusterMember()) {
				ClusterManager.fireMarkedAsSeniorClusterMember();
			}
			
			log.info( "Cache factory started." );
			log.info( "Plugin initialized." );
			return true;
		}
		catch ( Exception ex ) {
			log.error( "FATAL error initializing cluster", ex );
			this.stopCluster();
			return false;
		}
	}

	public void stopCluster() {
		log.info( "Cluster stopping..." );
		masterWatcher.disable();
		try {
			channel.close();
		} catch (Exception e) {
			log.error("Error closing channel {}", e.getMessage());
		}
		
		ClusterManager.fireLeftCluster();
		
		masterWatcher = null;
		channel = null;
		dispatcher = null;
		taskHandler = null;
	}

	@SuppressWarnings("unchecked")
	public Cache createCache(String name) {
		log.info( "Creating cache '{}'", name );
		try {
			return new JBossCache( name, cache );			
		}
		catch ( IOException ex ) {
			throw new ClusterException( ex );
		}
	}

	@SuppressWarnings("unchecked")
	public void destroyCache(Cache cache) {
		log.info( "Destroying cache '{}'", cache.getName() );
		if ( cache instanceof CacheWrapper )
			cache = ((CacheWrapper)cache).getWrappedCache();
		if ( ! ( cache instanceof JBossCache ) ) return;
		((JBossCache)cache).shutdown();
	}

	@SuppressWarnings("unchecked")
	public Lock getLock(Object key, Cache cache) {
		if ( cache instanceof CacheWrapper )
			cache = ((CacheWrapper)cache).getWrappedCache();
		if ( ! ( cache instanceof JBossCache ) ) 
			cache = this.createCache( cache.getName() );
		
		log.debug( "Creating lock for {} on cache {}", key, cache.getName() );
		return ((JBossCache)cache).getLock(key);
	}

	@SuppressWarnings("unchecked")
	public void updateCacheStats( Map<String,Cache> caches ) {
		// TODO Auto-generated method stub	
	}
	
	
	public void doClusterTask( ClusterTask task ) {
		log.debug( "Cluster task {}", task );
		try {
			Address local = channel.getLocalAddress();
			byte[] data = marshal( task );
			final Message base = new Message(null, local, data );
			for ( JGroupsClusterNodeInfo node : masterWatcher.getNodes().values() ) {
				if ( node.getAddress().equals( local ) ) 
					continue;
				Message msg = base.copy();
				msg.setDest( node.getAddress() );
				//FIXME not sure these messages are being sent
				dispatcher.send(msg);
				//dispatcher.sendMessage(msg, GroupRequest.GET_FIRST, 10000);
			}
		}
		catch ( Exception ex ) {
			log.error( "Error sending task {}", task, ex );
			return;
		}
	}

	public boolean doClusterTask( ClusterTask task, byte[] nodeID ) {
		log.debug( "Cluster task {}", task );
		Message msg = new Message(); 
		msg.setDest( masterWatcher.getNodes().get( new String(nodeID) ).getAddress() );
		msg.setSrc( channel.getLocalAddress() );
		try {
			msg.setBuffer( marshal( task ) );
			//FIXME not sure these messages are being sent.
			dispatcher.send(msg);
			//dispatcher.sendMessage(msg, GroupRequest.GET_FIRST, 10000);
			return true;
		}
		catch ( Exception ex ) {
			log.error( "Error sending task", ex );
			return false;
		}
	}

	public Collection<Object> doSynchronousClusterTask( ClusterTask task,
			boolean includeLocal ) {
		log.debug( "Sync Cluster task {}", task );
		
		Address local = channel.getLocalAddress();
		Vector<Address> recipients = new Vector<Address>();
		for ( JGroupsClusterNodeInfo node : masterWatcher.getNodes().values() ) {
			Address a = node.getAddress();
			if ( ! includeLocal && a.equals( local ) ) continue;
			recipients.add( a );
		}
		Message msg = null;
		try {
			msg = new Message( null, local, marshal( task) );
			RspList responses = dispatcher.castMessage(recipients, msg, GroupRequest.GET_ALL, 20000 );
			List<Object> responseData = new ArrayList<Object>(responses.size());
			for ( Rsp response : responses.values() ) responseData.add( response.getValue() );
			return responseData;
		}
		catch ( Exception ex ) {
			log.warn( "Exception sending message {}", msg, ex );
			//throw new ClusterException( "Exception sending message " + msg, ex );
			return null;
		}
	}

	
	public Object doSynchronousClusterTask( ClusterTask task, byte[] nodeID ) {
		log.debug( "Sync Cluster task {}", task );
		Message msg = new Message();
		Map<String,JGroupsClusterNodeInfo> allNodes = masterWatcher.getNodes(); 
		msg.setDest( allNodes.get( new String(nodeID) ).getAddress() );
		msg.setSrc( channel.getLocalAddress() );
		try {
			msg.setBuffer( marshal( task ) );
			Object o = dispatcher.sendMessage(msg, GroupRequest.GET_FIRST, 20000);
			return o;
		}
		catch ( Exception ex ) {
			log.warn( "Exception sending message {}", msg, ex );
			//throw new ClusterException( "Exception sending message " + msg, ex );
			return null;
		}
	}

	
	public byte[] getClusterMemberID() {
		return XMPPServer.getInstance().getNodeID().toByteArray();
	}

	 @SuppressWarnings("unchecked")
	public Collection<ClusterNodeInfo> getClusterNodesInfo() {
		return (Collection)this.masterWatcher.getNodes().values();
	}

	
	public int getMaxClusterNodes() {
		// TODO Auto-generated method stub
		return 100;
	}

	
	public byte[] getSeniorClusterMemberID() {
		for ( ClusterNodeInfo node : getClusterNodesInfo() ) {
			if ( node.isSeniorMember() ) return node.getNodeID().toByteArray();
		}
		throw new ClusterException( "Couldn't find master node in cluster info list!" );
	}

	
	public boolean isSeniorClusterMember() {
		ClusterNodeInfo localNode = masterWatcher.getNodes().get( 
				channel.getLocalAddress().toString() ); 
		return localNode != null && localNode.isSeniorMember();
	}

	public static byte[] marshal( Serializable obj ) throws IOException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream( data );
		try {
			out.writeObject(obj);
//			obj.writeExternal(out);
			return data.toByteArray();
		}
		finally { out.close(); }
	}
}
