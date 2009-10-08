package com.enernoc.rnd.openfire.cluster.session;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ComponentSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

public class ClusterComponentSession extends ClusterSession implements ComponentSession {

	ClusterExternalComponent component;
	
	public ClusterComponentSession() { super(); }
	public ClusterComponentSession( JID jid, byte[] nodeID ) { super( jid, nodeID ); }
	
	@Override
	void doCopy(Session s) {
		// TODO Auto-generated method stub

	}

	@Override
	void doReadExternal(ExternalizableUtil ext, ObjectInput in)
			throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	void doWriteExternal(ExternalizableUtil ext, ObjectOutput out)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	ClusterTask getSessionUpdateTask() {
		// TODO Auto-generated method stub
		return null;
	}

	public ExternalComponent getExternalComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	class ClusterExternalComponent implements ExternalComponent {

		private String category;
		private String initialSubdomain;
		private Collection<String> subdomains;
		private String type;
		private String name;
		private String description;
		
		public String getCategory() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getInitialSubdomain() {
			// TODO Auto-generated method stub
			return null;
		}

		public Collection<String> getSubdomains() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getType() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setCategory(String arg0) {
			// TODO Auto-generated method stub
			
		}

		public void setName(String arg0) {
			// TODO Auto-generated method stub
			
		}

		public void setType(String arg0) {
			// TODO Auto-generated method stub
			
		}

		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		public void initialize( JID address, ComponentManager cMgr ) throws ComponentException {
			// TODO Auto-generated method stub	
		}

		public void processPacket( Packet p ) {
			XMPPServer.getInstance().getSessionManager().getComponentSession( null ).process( p );
		}

		public void shutdown() {
			// TODO Auto-generated method stub
			
		}

		public void start() {
			// TODO Auto-generated method stub
			
		}
	}
}
