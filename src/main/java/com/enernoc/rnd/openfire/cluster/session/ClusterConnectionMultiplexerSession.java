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
package com.enernoc.rnd.openfire.cluster.session;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jivesoftware.openfire.session.ConnectionMultiplexerSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import com.enernoc.rnd.openfire.cluster.session.task.GetMultiplexerSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.RemoteSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.RemoteSessionTask.Operation;

public class ClusterConnectionMultiplexerSession extends ClusterSession
		implements ConnectionMultiplexerSession {

	public ClusterConnectionMultiplexerSession( JID address, byte[] nodeID ) {
		super(address, nodeID);
	}
	
	@Override
	void doCopy(Session s) { /* no-op */ }

	@Override
	void doReadExternal(ExternalizableUtil ext, ObjectInput in)
			throws IOException, ClassNotFoundException {
		/* no-op */
	}

	@Override
	void doWriteExternal(ExternalizableUtil ext, ObjectOutput out)
			throws IOException {
		/* no-op */
	}

	ClusterTask getSessionUpdateTask() {
		// TODO Auto-generated method stub
		return new GetMultiplexerSessionTask();
	}

	@Override
	ClusterTask getDeliverRawTextTask(String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	ClusterTask getProcessPacketTask(Packet packet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	RemoteSessionTask getRemoteSessionTask(Operation operation) {
		// TODO Auto-generated method stub
		return null;
	}
}
