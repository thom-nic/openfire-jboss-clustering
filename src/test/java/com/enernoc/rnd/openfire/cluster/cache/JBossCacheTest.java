package com.enernoc.rnd.openfire.cluster.cache;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JBossCacheTest {
	
	JBossCache<String, Widget> cache1;
	JBossCache<String, Widget> cache2;
	
	@Before public void createCache() throws Exception {
		this.cache1 = new JBossCache<String, Widget>( "testCache", "cache.xml" );
		assertEquals( 0, cache1.size() );
		
		this.cache2 = new JBossCache<String, Widget>( "testCache", "cache2.xml" );
		assertEquals( 0, cache2.size() );
	}
	
	@After public void destroyCache() throws Exception {
		this.cache1.clear();
		assertEquals( 0, cache1.size() );
		this.cache2.clear();
		assertEquals( 0, cache2.size() );
	}

	
	@Test public void testSimpleMapOperations() throws Exception {
		List<Widget> widgets = new ArrayList<Widget>(500);
		for(int i = 0; i < 500; i++) {
			Widget w = new Widget("test", i);
			String key = "test " + i;
			widgets.add(w);
			if( i % 2 == 0 )
				this.cache1.put( key, w );
			else
				this.cache2.put( key, w );
		}
		
		assertEquals( 500, cache1.size() );
		assertEquals( 500, cache2.size() );
		
		Widget result = cache1.get( "test " + 100 );
		assertNotNull( result );
		assertEquals( widgets.get(100), result );
		assertEquals( 100, result.count );
		
		Widget result2 = cache2.get( "test " + 100 );
		assertNotNull( result2 );
		assertEquals( widgets.get(100), result2 );
		assertEquals( 100, result2.count );
		
		assertTrue( cache1.containsKey( widgets.get(301).name + " 301" ) );
		assertTrue( cache1.containsValue( widgets.get(301) ) );
		assertTrue( cache1.containsValue( result2 ) );
		assertTrue( cache2.containsKey( widgets.get(301).name + " 301" ) );
		assertTrue( cache2.containsValue( widgets.get(301) ) );
		assertTrue( cache2.containsValue( result ) );
		
		
		cache2.remove( widgets.get(300).name + " 300" );
		Widget w = widgets.get(300);
		assertEquals( 499,  cache1.size() );
		assertEquals( 499,  cache2.size() );
		assertFalse( cache1.containsKey( widgets.get(300).name + " 300" ) );
		assertFalse( cache2.containsKey( widgets.get(300).name + " 300" ) );
		//TODO Fix this test
		//assertFalse( cache1.containsValue( w ) );
		//assertFalse( cache2.containsValue( w ) );
	}
}
