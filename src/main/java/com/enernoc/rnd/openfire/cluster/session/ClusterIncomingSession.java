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
import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.openfire.session.IncomingServerSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.Packet;

import com.enernoc.rnd.openfire.cluster.session.task.GetIncomingSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.RemoteSessionTask;
import com.enernoc.rnd.openfire.cluster.session.task.RemoteSessionTask.Operation;

public class ClusterIncomingSession extends ClusterSession implements
		IncomingServerSession {

	String localDomain;
	Collection<String> validDomains = new ArrayList<String>();
	
	public ClusterIncomingSession( String streamID, byte[] nodeID ) {
		super( null, nodeID );
		this.streamID = new BasicStreamID(streamID);
	}
	
	@Override
	void doCopy(Session s) {
		IncomingServerSession inS = (IncomingServerSession)s;
		this.localDomain =  inS.getLocalDomain();
		this.validDomains = inS.getValidatedDomains();

	}

	@Override
	void doReadExternal(ExternalizableUtil ext, ObjectInput in)
			throws IOException, ClassNotFoundException {
		this.streamID = new BasicStreamID(ext.readSafeUTF(in));
		this.localDomain = ext.readSafeUTF(in);
		ext.readStrings(in, this.validDomains);
	}

	@Override
	void doWriteExternal(ExternalizableUtil ext, ObjectOutput out)
			throws IOException {
		ext.writeSafeUTF( out, streamID.getID() );
		ext.writeSafeUTF( out, localDomain );
		ext.writeStrings( out, validDomains );
	}

	ClusterTask getSessionUpdateTask() {
		return new GetIncomingSessionTask( this.streamID.getID() );
	}

	public String getLocalDomain() {
		return this.localDomain;
	}

	public Collection<String> getValidatedDomains() {
		super.checkUpdate();
		return this.validDomains;
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
