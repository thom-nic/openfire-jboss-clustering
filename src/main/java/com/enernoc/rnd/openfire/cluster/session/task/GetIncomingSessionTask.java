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
import org.jivesoftware.openfire.session.IncomingServerSession;
import org.jivesoftware.openfire.session.Session;

import com.enernoc.rnd.openfire.cluster.session.ClusterIncomingSession;

public class GetIncomingSessionTask extends GetSessionTask<ClusterIncomingSession> {

	String streamID;
	
	public GetIncomingSessionTask() {}
	public GetIncomingSessionTask( String streamID ) {
		this.streamID = streamID;
	}
	
	@Override
	protected ClusterIncomingSession newSession() {
		return new ClusterIncomingSession( streamID, 
				XMPPServer.getInstance().getNodeID().toByteArray() );
	}
	
	@Override
	protected Session getLocalSession() {
		IncomingServerSession s = XMPPServer.getInstance().getSessionManager().getIncomingServerSession(streamID);
		log.debug( "Getting incoming session for {} : {}", super.jid, s );
		return s;
	}
}
