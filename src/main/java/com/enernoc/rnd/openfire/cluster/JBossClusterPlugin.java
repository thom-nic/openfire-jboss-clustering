package com.enernoc.rnd.openfire.cluster;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannelFactory;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.ClusterEventListener;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.cache.CacheFactory;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enernoc.rnd.openfire.cluster.session.ClusteredSessionLocator;

public class JBossClusterPlugin implements Plugin, ClusterEventListener {

	protected final Logger log = LoggerFactory.getLogger( getClass() );
	
	public static final String CLUSTER_JGROUPS_CONFIG_PROPERTY = 
		"com.enernoc.clustering.jgroups.config";
	public static final String CLUSTER_CACHE_CONFIG_PROPERTY = 
		"com.enernoc.clustering.cache.config";
	
	JChannelFactory channelFactory;
	Channel jgroups;
	
	ClusterMasterWatcher masterWatcher;
	
	
	public void destroyPlugin() {
		CacheFactory.stopClustering();
		ClusterManager.shutdown();
		// TODO interrupt master listener & node listener?
	}

	
	public void initializePlugin( PluginManager mgr, File pluginDir ) {
		LogManager.getLogManager().getLogger("").setLevel(Level.FINE);
		Enumeration<String> es = LogManager.getLogManager().getLoggerNames();
		while ( es.hasMoreElements() ) log.error( es.nextElement() );
		try {
			String clusterConfig = JiveGlobals.getProperty( CLUSTER_JGROUPS_CONFIG_PROPERTY );
			URL config = clusterConfig != null ? getClass().getResource( clusterConfig ) : 
				getClass().getResource("/udp.xml");
			channelFactory = new JChannelFactory( config );
			this.jgroups = channelFactory.createChannel();
			masterWatcher = new ClusterMasterWatcher( this.jgroups );
			jgroups.connect( "OpenFire-Cluster" ); // TODO make configurable
//			jgroups.getState( null, 20000 ); /// get initial distributed state.
			
			while ( this.getClusterNodes().size() < 1 ) {
				log.info( "Waiting for initial view..." );
				Thread.sleep( 1000 );
			}
			log.info( "Local address: {}", this.getLocalAddress() );
		}
		catch ( Exception ex ) {
			throw new ClusterException( "Unexpected error", ex );
		}
	
		log.info( "Plugin initialized." );
		
		/* this doesn't work because that value is read from a static
		 * initializer, which has already been run by now..  You must 
		 * set a property from the admin UI, then restart the servers.  */
//		JiveProperties.getInstance().put( 
/*		JiveGlobals.setProperty(
				CacheFactory.CLUSTERED_CACHE_PROPERTY_NAME,
        	"com.enernoc.rnd.openfire.cluster.cache.ClusteredCacheFactory");
*/		ExternalizableUtil.getInstance().setStrategy( new ExternalUtil() );
		XMPPServer.getInstance().getRoutingTable().setRemotePacketRouter( new ClusterPacketRouter() );
		XMPPServer.getInstance().setRemoteSessionLocator( new ClusteredSessionLocator() );
		
		XMPPServer.getInstance().setNodeID( NodeID.getInstance( masterWatcher.getLocalAddress().toString().getBytes() ) );		
		
		if ( ! ClusterManager.isClusteringEnabled() )
			ClusterManager.setClusteringEnabled(true); // calls startup() automatically
		else ClusterManager.startup(); // which in turn calls cacheFactory.startClustering() automatically
//		CacheFactory.startClustering();
		masterWatcher.enable();
	}

	/**
	 * Get all of the cluster addresses, including this node.
	 * @return
	 */
	public Map<String,JGroupsClusterNodeInfo> getClusterNodes() {
		return this.masterWatcher.getNodes();
	}

	/**
	 * Get the ID of this node.
	 * @return
	 */
	public Address getLocalAddress() {
		return this.masterWatcher.getLocalAddress();
	}	

	public Channel getChannel() {
		return this.jgroups;
	}
	
	
	public void joinedCluster() {
		log.info( "This node ({}) has JOINED the cluster.", getLocalAddress() );
	}

	
	public void joinedCluster(byte[] nodeID) {
		log.info( "Node {} has JOINED the cluster.", nodeID );
	}

	
	public void leftCluster() {
		log.info( "This node ({}) has LEFT the cluster.", getLocalAddress() );
	}

	
	public void leftCluster(byte[] nodeID) {
		log.info( "Node {} has LEFT the cluster.", nodeID );
	}

	
	public void markedAsSeniorClusterMember() {
		log.info( "This node ({}) has been marked as MASTER.", getLocalAddress() );
	}
}