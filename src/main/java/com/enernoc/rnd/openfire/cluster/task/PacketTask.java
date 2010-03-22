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

import java.io.DataInput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.util.cache.ClusterTask;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

/**
 * @author tnichols
 *
 */
public abstract class PacketTask<P extends Packet> implements ClusterTask {

	static final Logger log = LoggerFactory.getLogger( PacketTask.class ); 
	protected P packet;
	
	public PacketTask() {}
	
	protected PacketTask( P packet ) {
		this.packet = packet;
	}
	
	public Object getResult() {
		return null;
	}
		
	public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance(); 
		this.packet = deserializePacket( 
				(Class<P>)Class.forName( ext.readSafeUTF(in) ), in ); 
	}

	public void writeExternal( ObjectOutput out ) throws IOException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance(); 
		ext.writeSafeUTF( out, packet.getClass().getCanonicalName() );
		ext.writeSafeUTF( out, packet.getElement().asXML() );
	}

	protected Element readXML( ObjectInput in ) throws IOException {
		try {
			return DocumentHelper.parseText( 
					ExternalizableUtil.getInstance().readSafeUTF(in) )
						.getRootElement();
		} 
		catch (DocumentException e) { throw new IOException( e ); }
	}
	
	/**
	 * Get a packet instance given a generic type.  Clients who already know the
	 * type of packet they are manipulating can skip this since they can directly
	 * instantiate the packet subclass they are working with.
	 * @param <P>
	 * @param packetType
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected P deserializePacket( Class<P> packetType, DataInput in ) throws IOException {
		ExternalizableUtil ext = ExternalizableUtil.getInstance(); 
		try{
			String xmlPacket = ext.readSafeUTF(in);
			Constructor<P> ctr = packetType.getConstructor( 
					new Class [] { Element.class } );
			Element e = DocumentHelper.parseText( xmlPacket ).getRootElement();
			return ctr.newInstance( e );
		}
	 	catch ( NoSuchMethodException ex ) { throw new IOException( ex ); }
	 	catch ( InvocationTargetException ex ) { throw new IOException( ex ); }
	 	catch ( IllegalAccessException ex ) { throw new IOException( ex ); }
	 	catch ( IllegalArgumentException ex ) { throw new IOException( ex ); }
	 	catch ( InstantiationException ex ) { throw new IOException( ex ); }
	 	catch ( DocumentException ex ) { throw new IOException( ex ); }
	}
}
