package com.enernoc.rnd.openfire.cluster.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheFactory;
import org.jboss.cache.DefaultCacheFactory;
import org.jboss.cache.Fqn;
import org.jboss.cache.interceptors.base.CommandInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JBossCache<K,V> implements org.jivesoftware.util.cache.Cache<K,V> {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	volatile long hits = 0;
	volatile long misses = 0;
	long ttl;
	final Cache<K,V> cache;
	static final String KEY = "key";
	static final String VAL = "value";
	
	protected Fqn<String> baseName;
	
	
	public JBossCache( String cacheName, Cache<K,V> cache ) throws IOException {
		this.cache = cache;
		this.baseName = Fqn.fromElements( cacheName );
	}
	
	public JBossCache( String cacheName ) throws IOException {
		this.baseName = Fqn.fromElements( cacheName );
		CacheFactory<K,V> fac = new DefaultCacheFactory<K,V>();
		this.cache = fac.createCache(JBossCache.class.getResourceAsStream("/cache.xml"));
	}
	
	
	public void shutdown() {
		try {
			this.clear();
//			this.cache.stop();
//			this.cache.destroy();
		}
		catch ( Exception ex ) {
			log.error( "Error while destroying this cache.", ex );
		}
	}

	protected Fqn<String> getKey( Object key ) {
		return Fqn.fromRelativeElements(this.baseName, 
				key.getClass().getCanonicalName() + "@" + key.hashCode() );
	}
	
	public Lock getLock( Object key ) {
		return new JBossCacheLock( this.cache, getKey( key ) );
	}
	
	@Override
	public void clear() {
		for ( Object node : cache.getChildrenNames( this.baseName ) )
			cache.removeNode( Fqn.fromRelativeElements(baseName, node) );
	}

	
	
	@Override
	public boolean containsKey( Object key ) {
		return cache.getNode( getKey(key) ) != null;
	}

	@Override
	public boolean containsValue(Object val) {
		for ( V v : this.values() ) // TODO performance 
			if (v.equals( val ) ) return true;
		return false;
	}

	/* TODO this should return a view that lazily retrieves items as they are accessed?
	 * 
	 */
	@Override
	public Set<Entry<K,V>> entrySet() {		
//		if ( true ) throw new UnsupportedOperationException();
		Map<K,V> entries = new HashMap<K,V>();
		for ( Object node : cache.getChildrenNames( this.baseName ) )
			entries.putAll( cache.getNode( Fqn.fromRelativeElements(this.baseName, node) ).getData() );
		return entries.entrySet();
	}
	
	@Override
	public boolean isEmpty() {
		return cache.getChildrenNames( this.baseName ).size() < 1;
	}

	@Override
	public Set<K> keySet() {
//		if ( true ) throw new UnsupportedOperationException();
		Set<K> keys = new HashSet<K>();
		for ( Object node : cache.getChildrenNames( this.baseName ) ) {
			keys.addAll( cache.getKeys( Fqn.fromRelativeElements(this.baseName, node) ) );
		}
		return keys;
	}

	@Override
	public V put(K key, V val) {
		cache.put( getKey(key), key, val );
		return val;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		cache.startBatch();
		try {
			for ( K key : map.keySet() ) put( key, map.get(key) );
			cache.endBatch(true);
		}
		catch ( RuntimeException ex ) {
			cache.endBatch(false);
			throw ex;
		}
	}

	@Override
	public int size() {
		return cache.getChildrenNames( this.baseName ).size();
	}

	@Override
	public Collection<V> values() {
//		if ( true ) throw new UnsupportedOperationException();
		List<V> vals = new ArrayList<V>();
		for ( Object node : cache.getChildrenNames( this.baseName ) ) {
			vals.addAll( cache.getData( Fqn.fromRelativeElements(this.baseName, node) ).values() );
		}
		return vals;
	}

	@Override
	public V get(Object key) {
		V val = cache.get( getKey(key), (K)key );
		if ( val != null ) this.hits ++ ; else this.misses ++ ; 
		return val; 
	}

	@Override
	public V remove(Object key) {
		Fqn<String> fqn = getKey(key);
		V val = cache.remove( fqn, (K)key );
		cache.removeNode(fqn);
		return val;
	}

	@Override
	public long getCacheHits() {
		return this.hits;
	}

	@Override
	public long getCacheMisses() {
		return this.misses;
	}

	@Override
	public int getCacheSize() {
		return Integer.MAX_VALUE;
	}

	@Override
	public long getMaxCacheSize() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getMaxLifetime() {
		return this.ttl;
	}

	@Override
	public String getName() {
		return this.baseName.getLastElementAsString();
	}

	@Override
	public void setMaxCacheSize(int arg0) {
		// TODO adjust eviction policy
	}

	@Override
	public void setMaxLifetime( long ttl ) {
		// TODO adjust eviction policy
		this.ttl = ttl;
	}

	@Override
	public void setName( String cacheName ) {
		this.baseName = Fqn.fromElements( cacheName );
	}

}