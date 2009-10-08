package com.enernoc.rnd.openfire.cluster.cache;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

public class JBossCacheTest {
	
	JBossCache<String, Widget> cache;
	
	@Before public void createCache() throws Exception {
		this.cache = new JBossCache<String, Widget>( "testCache" );
		assertEquals( 0, cache.size() );
	}
	
	@After public void destroyCache() throws Exception {
		this.cache.clear();
		assertEquals( 0, cache.size() );
	}

	
	@Test public void testSimpleMapOperations() throws Exception {
		Widget w = new Widget("test", 100);
		String key = "test one";
		this.cache.put( key, w );
		
		assertEquals( 1, cache.size() );
		
		Widget result = cache.get( "test one" );
		assertNotNull( result );
		assertEquals( w, result );
		assertEquals( 100, result.count );
		
		assertTrue( cache.containsKey( key ) );
		assertTrue( cache.containsValue( w ) );
		assertTrue( cache.containsValue( result ) );
		
		cache.remove( key );
		assertEquals( 0,  cache.size() );
		assertFalse( cache.containsKey( key ) );
		assertFalse( cache.containsValue( result ) );
	}
	
	class Widget implements Externalizable {
		String name;
		int count;
		
		public Widget( String n, int c ) {
			this.name = n; this.count = c;
		}

		public void readExternal(ObjectInput in) throws IOException,
				ClassNotFoundException {
			this.name = in.readUTF();
			this.count = in.readInt();
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF( name );
			out.writeInt( count );
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof Widget && this.name.equals( ((Widget)o).name );
		}
	}
}
