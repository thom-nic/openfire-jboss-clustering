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
import org.jboss.cache.Fqn;
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
		//return new JBossCacheLock( this.cache, getKey( key ) );
		return new JBossNOCacheLock();
	}
	
	
	public void clear() {
		for ( Object node : cache.getChildrenNames( this.baseName ) )
			cache.removeNode( Fqn.fromRelativeElements(baseName, node) );
	}

	
	
	
	public boolean containsKey( Object key ) {
		log.debug( "Cache '{}' contains: {}", getName(), key );
		return cache.getNode( getKey(key) ) != null;
	}

	
	public boolean containsValue(Object val) {
		for ( V v : this.values() ) { // TODO performance 
			if (v.equals( val ) ) 
				return true;
		}
		return false;
	}

	/* TODO this should return a view that lazily retrieves items as they are accessed?
	 * 
	 */
	
	public Set<Entry<K,V>> entrySet() {		
//		if ( true ) throw new UnsupportedOperationException();
		Map<K,V> entries = new HashMap<K,V>();
		for ( Object node : cache.getChildrenNames( this.baseName ) )
			entries.putAll( cache.getNode( Fqn.fromRelativeElements(this.baseName, node) ).getData() );
		return entries.entrySet();
	}
	
	
	public boolean isEmpty() {
		return cache.getChildrenNames( this.baseName ).size() < 1;
	}

	
	public Set<K> keySet() {
//		if ( true ) throw new UnsupportedOperationException();
		Set<K> keys = new HashSet<K>();
		for ( Object node : cache.getChildrenNames( this.baseName ) ) {
			keys.addAll( cache.getKeys( Fqn.fromRelativeElements(this.baseName, node) ) );
		}
		return keys;
	}

	
	public V put(K key, V val) {
		log.debug( "Cache '{}'  put: {}", new Object[] {getName(), key, val} );
		cache.put( getKey(key), key, val );
		return val;
	}

	
	public void putAll( Map<? extends K, ? extends V> map ) {
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

	
	public int size() {
		return cache.getChildrenNames( this.baseName ).size();
	}

	
	public Collection<V> values() {
//		if ( true ) throw new UnsupportedOperationException();
		List<V> vals = new ArrayList<V>();
		for ( Object node : cache.getChildrenNames( this.baseName ) ) {
			vals.addAll( cache.getData( Fqn.fromRelativeElements(this.baseName, node) ).values() );
		}
		return vals;
	}

	
	public V get(Object key) {
		log.debug( "  Cache '{}' get: {}", getName(), key );
		V val = cache.get( getKey(key), (K)key );
		if ( val != null ) this.hits ++ ; else this.misses ++ ; 
		return val; 
	}

	
	public V remove(Object key) {
		log.debug( "  Cache '{}' remove: {}", getName(), key );
		Fqn<String> fqn = getKey(key);
		V val = cache.remove( fqn, (K)key );
		cache.removeNode(fqn);
		return val;
	}

	
	public long getCacheHits() {
		return this.hits;
	}

	
	public long getCacheMisses() {
		return this.misses;
	}

	
	public int getCacheSize() {
		return Integer.MAX_VALUE;
	}

	
	public long getMaxCacheSize() {
		return -1;
	}

	
	public long getMaxLifetime() {
		return this.ttl;
	}

	
	public String getName() {
		return this.baseName.getLastElementAsString();
	}

	
	public void setMaxCacheSize(int arg0) {
		// TODO adjust eviction policy
	}

	
	public void setMaxLifetime( long ttl ) {
		// TODO adjust eviction policy
		this.ttl = ttl;
	}

	
	public void setName( String cacheName ) {
		this.baseName = Fqn.fromElements( cacheName );
	}

}