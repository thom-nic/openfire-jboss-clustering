package com.enernoc.rnd.openfire.cluster.cache;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jivesoftware.util.cache.ClusterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskExecutor implements RequestHandler {

	protected final Logger log = LoggerFactory.getLogger( getClass() );
	MessageDispatcher dispatcher;
	
	public TaskExecutor( MessageDispatcher dispatcher ) {
		this.dispatcher = dispatcher;
		this.dispatcher.setRequestHandler( this );
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
			log.error( "Error executing task from msg {}", msg, ex );
			return null;
		}
	}
}
