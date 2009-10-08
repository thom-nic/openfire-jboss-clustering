package com.enernoc.rnd.openfire.cluster.session;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.openfire.session.OutgoingServerSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.xmpp.packet.JID;

import com.enernoc.rnd.openfire.cluster.session.task.GetOutgoingSessionTask;

public class ClusterOutgoingSession extends ClusterSession implements OutgoingServerSession {

	protected Collection<String> authenticatedDomains = new ArrayList<String>();
	protected Collection<String> hostnames = new ArrayList<String>();
	protected boolean usingDialback;
	
	public ClusterOutgoingSession() { super(); }
	public ClusterOutgoingSession( JID jid, byte[] nodeID ) { super( jid, nodeID ); }
	
	@Override
	void doCopy(Session s) {
		OutgoingServerSession oss = (OutgoingServerSession)s;
		this.authenticatedDomains = oss.getAuthenticatedDomains();
		this.hostnames = oss.getHostnames();
		this.usingDialback = oss.isUsingServerDialback();
	}

	@Override
	void doReadExternal(ExternalizableUtil ext, ObjectInput in)
			throws IOException, ClassNotFoundException {
		ext.readStrings(in, this.authenticatedDomains);
		ext.readStrings(in, this.hostnames);
		this.usingDialback = in.readBoolean();
	}

	@Override
	void doWriteExternal(ExternalizableUtil ext, ObjectOutput out)
			throws IOException {
		ext.writeStrings(out, authenticatedDomains);
		ext.writeStrings(out, hostnames);
		out.writeBoolean(usingDialback);
	}

	@Override
	ClusterTask getSessionUpdateTask() {
		return new GetOutgoingSessionTask(this.address);
	}

	
	public void addAuthenticatedDomain(String domain) {
		// TODO Auto-generated method stub
		
	}

	
	public void addHostname(String host) {
		// TODO Auto-generated method stub
		
	}

	
	public boolean authenticateSubdomain(String domain, String host) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public Collection<String> getAuthenticatedDomains() {
		super.checkUpdate();
		return this.authenticatedDomains;
	}

	
	public Collection<String> getHostnames() {
		super.checkUpdate();
		return this.hostnames;
	}

	
	public boolean isUsingServerDialback() {
		return this.usingDialback;
	}
}
