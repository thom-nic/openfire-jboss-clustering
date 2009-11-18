/**
 * 
 */
package com.enernoc.rnd.openfire.cluster.cache;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author macdiesel
 *
 */
public class JBossNOCacheLock implements Lock {

	
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.locks.Lock#lock()
	 */
	public void lock() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.locks.Lock#lockInterruptibly()
	 */
	public void lockInterruptibly() throws InterruptedException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.locks.Lock#newCondition()
	 */
	public Condition newCondition() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.locks.Lock#tryLock()
	 */
	public boolean tryLock() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.locks.Lock#tryLock(long, java.util.concurrent.TimeUnit)
	 */
	public boolean tryLock(long time, TimeUnit unit)
			throws InterruptedException {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.locks.Lock#unlock()
	 */
	public void unlock() {
		// TODO Auto-generated method stub
		
	}

}
