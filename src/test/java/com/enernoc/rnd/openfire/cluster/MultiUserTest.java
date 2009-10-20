package com.enernoc.rnd.openfire.cluster;

import static org.junit.Assert.assertEquals;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class MultiUserTest {
	static ConnectionConfiguration c1 = new ConnectionConfiguration( "localhost", 5222 );
	static ConnectionConfiguration c2 = new ConnectionConfiguration( "bbeggs-server", 5222 );
	
	static final String DOMAIN = "@tnichols-server";
	static final String PASSWD = "pass"; 

	@BeforeClass
	public static void setUp() throws Exception {
		XMPPConnection xmpp = new XMPPConnection( c1 );
		xmpp.connect();
//		xmpp.getAccountManager().createAccount( "c11", PASSWD );
//		xmpp.getAccountManager().createAccount( "c12", PASSWD );
		xmpp.disconnect();
		
		xmpp = new XMPPConnection( c2 );
		xmpp.connect();
//		xmpp.getAccountManager().createAccount( "c21", PASSWD );
//		xmpp.getAccountManager().createAccount( "c22", PASSWD );
//		xmpp.login( "c21", PASSWD );
//		xmpp.disconnect();
//		xmpp.connect();
//		xmpp.login( "c22", PASSWD );
		xmpp.disconnect();
	}
	
//	@AfterClass
	public static void tearDown() throws Exception {
		XMPPConnection xmpp = new XMPPConnection( c1 );
		xmpp.connect();
		xmpp.login( "c11", PASSWD );
		xmpp.getAccountManager().deleteAccount();
		xmpp.disconnect();
		xmpp.connect();
		xmpp.login( "c12", PASSWD );
		xmpp.getAccountManager().deleteAccount();
		xmpp.disconnect();
		
		xmpp = new XMPPConnection( c2 );
		xmpp.connect();
		xmpp.login( "c21", PASSWD );
		xmpp.getAccountManager().deleteAccount();
		xmpp.disconnect();
		xmpp.connect();
		xmpp.login( "c22", PASSWD );
		xmpp.getAccountManager().deleteAccount();		
		xmpp.disconnect();
	}
	
	@Test public void testSameServerMessage() throws Exception {
		SmackClient c11 = new SmackClient( c1, "c11", "c12" );
		SmackClient c12 = new SmackClient( c1, "c12", "c11" );
		try {
			c11.sendMessage( "Test 1-1" );
			c11.sendMessage( "Test 1-2" );
			c12.sendMessage( "Test 2-1" );
			c12.sendMessage( "Test 2-2" );
			
			Thread.sleep( 5000 );
			assertEquals( 2, c11.getMessages().size() );
			assertEquals( 2, c12.getMessages().size() );
		}
		finally {
			c11.shutdown();
			c12.shutdown();
		}
	}

	@Test public void testClusteredMessage() throws Exception {
		SmackClient c11 = new SmackClient( c1, "c11", "c21" );
		SmackClient c21 = new SmackClient( c2, "c21", "c11" );
		try {
			c11.sendMessage( "Cluster Test 1-1" );
			c11.sendMessage( "Cluster Test 1-2" );
			c21.sendMessage( "Cluster Test 2-1" );
			c21.sendMessage( "Cluster Test 2-2" );
			
			Thread.sleep( 5000 );
			assertEquals( 2, c21.getMessages().size() );
			assertEquals( 2, c11.getMessages().size() );
		}
		finally {
			c11.shutdown();
			c21.shutdown();
		}
	}
	
	class SmackClient implements PacketListener, PacketFilter {

		String sendToUser;
		final XMPPConnection xmpp;
		Queue<String> messageQueue = new LinkedBlockingQueue<String>();
		
		public SmackClient( ConnectionConfiguration c, String user, String toUser ) 
				throws XMPPException, InterruptedException {
			xmpp = new XMPPConnection( c );
			xmpp.connect();
			//xmpp.getAccountManager().createAccount( user, PASSWD );
			xmpp.addPacketListener(this, this);
			xmpp.login( user, PASSWD );
			this.sendToUser = toUser + DOMAIN; // must be qualified by @host
			Thread.sleep(1000);
			messageQueue.clear(); // clear any msgs that might have been queued on the server.
		}
		
		public void shutdown() throws XMPPException {
			xmpp.disconnect();
		}
				
		public void sendMessage( String msg ) throws XMPPException {
			Message m = new Message( sendToUser );
			m.setBody( msg );
//			System.out.println( m.getBody() );
			xmpp.sendPacket( m );
		}
		
		public Queue<String> getMessages() { return this.messageQueue; }

		public void processPacket( Packet p ) {
			String body = ((Message)p).getBody();
			System.out.println( "Got message! : " + body );
			this.messageQueue.add( body );
		}

		public boolean accept( Packet p ) {
			return p instanceof Message;
		}
	}
}
