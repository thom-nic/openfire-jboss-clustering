package com.enernoc.rnd.openfire.cluster.cache;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RequestHandler;
import org.jivesoftware.util.cache.ClusterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskExecutor implements RequestHandler, MessageListener {

	protected final Logger log = LoggerFactory.getLogger( getClass() );
	
	public TaskExecutor() {
	}
	
	public Object handle( Message msg ) {
		try { 
			ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream( msg.getBuffer() ) );
			// TODO execute incoming tasks from a threadpool? 
			ClusterTask task = (ClusterTask)in.readObject();
			in.close(); 
			
			task.run(); // run synchronously.
			return task.getResult();
		}
		catch ( Exception ex ) {
			log.error( "Error executing task from msg {}", msg );
			log.error( " Stack Trace: ", ex);
			return null;
		}
	}

	public void receive(Message msg) {
		this.handle(msg);
	}
	
	public byte[] getState() {
		// TODO Auto-generated method stub
		log.info("Called Unimplemented getState()");
		return new byte[] {};
	}

	public void setState(byte[] state) {
		// TODO Auto-generated method stub
		log.info("setState(byte[] state)");
	}
}
