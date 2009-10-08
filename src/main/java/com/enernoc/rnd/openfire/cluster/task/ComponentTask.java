package com.enernoc.rnd.openfire.cluster.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.XMPPServer;
import org.xmpp.packet.Packet;

public class ComponentTask extends PacketTask<Packet> {

	private Operation op;
	
	public ComponentTask( Operation op ) {
		this.op = op;
	}
	public ComponentTask( Packet p ) {
		super( p );
		this.op = Operation.PROCESS;
	}

	@Override
	public Object getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	public void run() {
		switch ( this.op ) {
		case PROCESS :
			XMPPServer.getInstance().getSessionManager().getComponentSession( 
					super.packet.getTo().getDomain() ).process( super.packet );
			break;
		case STARTUP :
			break;
		case INIT :
			break;
		case SHUTDOWN :
			break;
		}
	}

	@Override
	public void readExternal(ObjectInput arg0) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeExternal(ObjectOutput arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	enum Operation {
		STARTUP, SHUTDOWN, INIT, PROCESS
	}
}
