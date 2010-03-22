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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.NodeRemoved;
import org.jboss.cache.notifications.event.NodeRemovedEvent;

/**
 * TODO track thread owner to ensure only a single thread calls these methods?
 * @author tnichols
 */
@CacheListener
public class JBossCacheLock implements Lock {

	final static String LOCK_NODE = "_LOCK";
	final static String lockVal = "locked";
	Fqn<String> node;
	Cache<String,String> cache;
	volatile boolean locked = true;
	
	public JBossCacheLock( Cache c, Fqn<String> parent ) {
		this.node = Fqn.fromRelativeElements(parent, LOCK_NODE);
		this.cache = c;
		
		cache.addCacheListener( this );
	}
	
	@NodeRemoved public synchronized void nodeRemoved( NodeRemovedEvent evt ) {
		if ( evt.getFqn().equals( this.node) ) notify();
	}
	
	
	protected void finalize() throws Throwable {
		cache.removeCacheListener( this ); // hopefully this is a weak hashMap...
		super.finalize();
	}
	
	
	public synchronized void lock() {
		while ( cache.put( node, LOCK_NODE, lockVal ) != null ) {
			try { wait(); }
			catch ( InterruptedException ex ) {}
		}
		locked = true;
	}

	
	public synchronized void lockInterruptibly() throws InterruptedException {
		while ( cache.put( node, LOCK_NODE, lockVal ) != null )
			wait();
		locked = true;
	}

	
	public Condition newCondition() {
		return new CacheLockCondition();
	}
	
	
	public boolean tryLock() {
		locked = cache.put( node, LOCK_NODE, lockVal ) == null;
		return locked;
	}

	
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		long duration = System.currentTimeMillis();
		if ( cache.put( node, LOCK_NODE, lockVal ) == null ) { 
			locked = true ; return true;
		}
		try { unit.timedWait( this, time ); }
		catch ( InterruptedException ex ) {} // TODO
		// time we slept for this iteration
		duration= System.currentTimeMillis() - duration;
		long timeLeft = unit.toMillis( time ) - duration;
		if ( timeLeft < 1 ) return false;
		return tryLock( timeLeft, MILLISECONDS );
	}

	
	public void unlock() {
		//if ( ! locked ) throw new IllegalMonitorStateException( "Not locked" );
		cache.removeNode( node );
		locked = false;
	}

	class CacheLockCondition implements Condition {

		volatile boolean notified = false;
		
		
		public synchronized void await() throws InterruptedException {
			unlock();
			if ( ! notified ) this.wait();
			lockInterruptibly();
			notified = false;
		}

		
		public synchronized boolean await(long time, TimeUnit unit)
				throws InterruptedException {
			return awaitNanos( unit.toNanos(time) ) > 0;
		}

		
		public synchronized long awaitNanos(long nanosTimeout) throws InterruptedException {
			long duration = System.nanoTime();
			unlock();// FIXME this isn't really atomic...........
			if ( ! notified ) this.wait( MILLISECONDS.convert(nanosTimeout, TimeUnit.NANOSECONDS),
					(int)(nanosTimeout % 1000000) );
			duration = System.nanoTime() - duration;
			lockInterruptibly();
			notified = false;
			return duration - nanosTimeout;
		}

		
		public void awaitUninterruptibly() {
			
			unlock();
			boolean interrupt = false;
			while ( ! notified ) {
				try { 
					this.wait();
					notified = true;
				}
				catch ( InterruptedException ex ) { interrupt = true; }
			}
			lock();
			if ( interrupt ) Thread.currentThread().interrupt(); //reset the interrupted flag...
			notified = false;
		}

		
		public boolean awaitUntil(Date deadline) throws InterruptedException {
			try {
				unlock();
				long timeLeft = deadline.getTime() - System.currentTimeMillis();
				while ( timeLeft > 0  && ! notified ) this.wait( timeLeft );
				lock();
				return notified;
			} finally { notified = false; }
		}

		
		public synchronized void signal() {
			this.notified = true;
			this.notify();
		}

		
		public synchronized void signalAll() {
			this.notified = true;
			this.notifyAll();
		}
		
	}
}
