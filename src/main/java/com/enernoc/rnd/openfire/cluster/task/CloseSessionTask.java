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
import org.jivesoftware.util.cache.ClusterTask;
import org.xmpp.packet.JID;

public class CloseSessionTask implements ClusterTask {

	JID address;
	public CloseSessionTask() {}
	public CloseSessionTask( JID jid ) {
		this.address = jid;
	}
	
	
	public Void getResult() {
		return null;
	}

	
	public void run() {
		XMPPServer.getInstance().getSessionManager().getSession(
				this.address ).close();
	}

	
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		address.readExternal(in);
	}

	
	public void writeExternal(ObjectOutput out) throws IOException {
		address.writeExternal(out);
	}
}
