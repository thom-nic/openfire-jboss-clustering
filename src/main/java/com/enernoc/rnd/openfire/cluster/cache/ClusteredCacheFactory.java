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
import org.jgroups.Address;
import org.jgroups.ChannelException;
import org.jgroups.Message;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.cluster.ClusterNodeInfo;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactoryStrategy;
import org.jivesoftware.util.cache.CacheWrapper;
import org.jivesoftware.util.cache.ClusterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enernoc.rnd.openfire.cluster.ClusterException;
import com.enernoc.rnd.openfire.cluster.JBossClusterPlugin;
import com.enernoc.rnd.openfire.cluster.JGroupsClusterNodeInfo;

public class ClusteredCacheFactory implements CacheFactoryStrategy {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	// TODO replace with CacheManager?
	CacheFactory factory = new DefaultCacheFactory();
	org.jboss.cache.Cache cache;
	
	protected URL cacheConfigURL;
	
	JBossClusterPlugin cluster; 
	MessageDispatcher dispatcher;
	TaskExecutor taskHandler;
		
	/**
	 * This is called by the {@link ClusterManager}, when 
	 * {@link JBossClusterPlugin}.initializePlugin() is executed.
	 */
	public boolean startCluster() {
		log.info( "Cluster starting..." );
		for ( Plugin p : XMPPServer.getInstance().getPluginManager().getPlugins() ) {
			if ( p.getClass().equals(JBossClusterPlugin.class) ) {
				this.cluster = (JBossClusterPlugin)p;
				break;
			}
		}
		
		try {
			String cacheConfig = JiveGlobals.getProperty( JBossClusterPlugin.CLUSTER_CACHE_CONFIG_PROPERTY );
			this.cacheConfigURL = cacheConfig != null ? new URL( cacheConfig ) : 
				JBossClusterPlugin.class.getResource( "/cache.xml" );

			
			InputStream cfgStream = cacheConfigURL.openStream();
			try { this.cache = factory.createCache(cfgStream, true); }
			finally { try { cfgStream.close(); } catch ( IOException ex ) {} }		

			
			dispatcher = new MessageDispatcher( cluster.getChannel(), null, null, true );
			taskHandler = new TaskExecutor( dispatcher );
			log.info( "Cache factory started." );
			return true;
		}
		catch ( Exception ex ) {
			//throw new ClusterException( "FATAL error initializing task queue services", ex );
			log.error( "FATAL error initializing task queue services", ex );
			return false;
		}
	}

	public void stopCluster() {
		log.info( "Cluster stopping..." );
		// TODO should this tell the clusterPlugin to shutdown other services?
	}

	@SuppressWarnings("unchecked")
	public Cache createCache(String name) {
		log.info( "Creating cache '{}'", name );
		try {
			return new JBossCache( name, this.cache );			
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
		Collection<JGroupsClusterNodeInfo> nodes = cluster.getClusterNodes().values();
		try {
			Address local = cluster.getLocalAddress();
			byte[] data = marshal( task );
			final Message base = new Message(null, local, data );
			for ( JGroupsClusterNodeInfo node : nodes ) {
				if ( node.getAddress().equals( local ) ) continue;
				
				Message msg = base.copy();
				msg.setDest( node.getAddress() );
				try {
					dispatcher.getChannel().send(msg);
				}
				catch ( ChannelException ex ) {
					log.error( "Error sending task {} to node {}", 
							new Object[] {task, node.getAddress()}, ex );
				}
			}
		}
		catch ( IOException ex ) {
			log.error( "Error serializing task {}", task, ex );
			return;
		}
	}

	public boolean doClusterTask( ClusterTask task, byte[] nodeID ) {
		log.debug( "Cluster task {}", task );
		Message msg = new Message();
		Map<String,JGroupsClusterNodeInfo> allNodes = cluster.getClusterNodes(); 
		msg.setDest( allNodes.get( new String(nodeID) ).getAddress() );
		msg.setSrc( cluster.getLocalAddress() );
		try {
			msg.setBuffer( marshal( task ) );
			dispatcher.getChannel().send(msg);
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
		
		Address local = cluster.getLocalAddress();
		Vector<Address> recipients = new Vector<Address>();
		for ( JGroupsClusterNodeInfo node : cluster.getClusterNodes().values() ) {
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
		Map<String,JGroupsClusterNodeInfo> allNodes = cluster.getClusterNodes(); 
		msg.setDest( allNodes.get( new String(nodeID) ).getAddress() );
		msg.setSrc( cluster.getLocalAddress() );
		try {
			msg.setBuffer( marshal( task ) );
			return dispatcher.sendMessage(msg, GroupRequest.GET_FIRST, 20000);
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
		return (Collection)this.cluster.getClusterNodes().values();
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
		ClusterNodeInfo localNode = this.cluster.getClusterNodes().get( 
				this.cluster.getLocalAddress().toString() ); 
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
