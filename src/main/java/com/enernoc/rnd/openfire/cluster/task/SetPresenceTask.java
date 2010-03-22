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
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.Presence;

public class SetPresenceTask extends PacketTask<Presence> {

	public SetPresenceTask() {}
	public SetPresenceTask( Presence p ) {
		super( p );
	}
	
	public void run() {
		XMPPServer.getInstance().getSessionManager()
			.getSession( packet.getFrom() ).setPresence( packet );
	}

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
 		this.packet = new Presence( super.readXML(in), true );
    }

    @Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ExternalizableUtil.getInstance().writeSafeUTF( out, packet.toXML() );
	}
}
