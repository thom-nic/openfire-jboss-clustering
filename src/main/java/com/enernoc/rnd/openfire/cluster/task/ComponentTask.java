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
