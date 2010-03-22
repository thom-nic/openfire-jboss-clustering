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
package com.enernoc.rnd.openfire.cluster.session.task;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ConnectionMultiplexerSession;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.session.ClusterConnectionMultiplexerSession;


public class GetMultiplexerSessionTask extends GetSessionTask<ClusterConnectionMultiplexerSession> {

	public GetMultiplexerSessionTask() { super(); }
	public GetMultiplexerSessionTask( JID address ) { super( address ); }
	
	@Override
	protected ConnectionMultiplexerSession getLocalSession() {
		ConnectionMultiplexerSession s = XMPPServer.getInstance().getSessionManager().getConnectionMultiplexerSession(super.jid);
		log.debug( "Getting multiplexer session for {} : {}", super.jid, s );
		return s;
	}

	@Override
	protected ClusterConnectionMultiplexerSession newSession() {
		return new ClusterConnectionMultiplexerSession( super.jid, 
				XMPPServer.getInstance().getNodeID().toByteArray() );
	}
}
