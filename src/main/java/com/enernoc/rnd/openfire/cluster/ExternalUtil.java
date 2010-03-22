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
package com.enernoc.rnd.openfire.cluster;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.util.cache.ExternalizableUtilStrategy;

public class ExternalUtil implements ExternalizableUtilStrategy {

	public static void write( ObjectOutput out, Externalizable ext ) throws IOException {
		out.writeBoolean( ext != null );
		if ( ext == null ) return;
		ext.writeExternal(out);
	}
	
	public static boolean read( ObjectInput in, Externalizable ext ) throws IOException, ClassNotFoundException {
		if ( ! in.readBoolean() ) return false;
		ext.readExternal(in);
		return true;
	}
	
	public void writeBoolean(DataOutput out, boolean value) throws IOException {
		out.writeBoolean(value);
	}

	public boolean readBoolean(DataInput in) throws IOException {
		return in.readBoolean();
	}

	public void writeByteArray(DataOutput out, byte[] value) throws IOException {
		out.writeInt(value.length);
		for (int i=0;i<value.length;++i)
			out.writeByte(value[i]);

	}

	public byte[] readByteArray(DataInput in) throws IOException {
		int size = in.readInt();
		byte[] result = new byte[size];
		for (int i=0;i<size;++i)
			result[i]=in.readByte();
		return result;
	}

	public void writeInt(DataOutput out, int value) throws IOException {
		out.writeInt(value);
	}

	public int readInt(DataInput in) throws IOException {
		return in.readInt();
	}

	public void writeLong(DataOutput out, long value) throws IOException {
		out.writeLong(value);

	}

	public long readLong(DataInput in) throws IOException {
		return in.readLong();
	}

	public long[] readLongArray(DataInput in) throws IOException {
		int size = in.readInt();
		long[] result = new long[size];
		for (int i = 0; i < size; i++) {
			result[i] = in.readLong();
		}
		return result;
	}

	public void writeLongArray(DataOutput out, long[] array) throws IOException {
		out.write(array.length);
		for (int i = 0; i < array.length; i++) {
			out.writeLong(array[i]);
		}
	}

	public void writeExternalizableMap(DataOutput out,
			Map<String, ? extends Externalizable> map) throws IOException {
		out.writeInt(map.size());
		for( String key : map.keySet() ) {
			out.writeUTF(key);
			out.writeUTF(map.get(key).getClass().getCanonicalName());
			map.get(key).writeExternal((ObjectOutput)out);
		}
	}

	@SuppressWarnings("unchecked")
	public int readExternalizableMap(DataInput in,
			Map<String, ? extends Externalizable> map, ClassLoader loader)
			throws IOException {
		int size = in.readInt();
		int i;
		Map<String, Externalizable> map2 = (Map<String, Externalizable>)map;
		for ( i=0; i<size;i++ ) {
			String key = in.readUTF();
			Externalizable value;
			try {
				value = (Externalizable)Class.forName(in.readUTF(), true, loader).newInstance();
				value.readExternal((ObjectInput)in);
			} catch (Exception e) {
				throw new IOException(e);
			} 
			map2.put(key, value);
		}
		return i;
	}

	public void writeLongIntMap(DataOutput out, Map<Long, Integer> map)
			throws IOException {
		out.writeInt(map.size());
		out.writeUTF(map.getClass().getCanonicalName());
		for(Long key:map.keySet()) {
			out.writeLong(key);
			out.writeInt(map.get(key));
		}
	}

	@SuppressWarnings("unchecked")
	public Map readLongIntMap(DataInput in) throws IOException {
		int size = in.readInt();
		Map<Long, Integer> map;
		try {
			map = (Map<Long, Integer>)Class.forName(in.readUTF(), true, null).newInstance();
		} catch (Exception e1) {
			throw new IOException(e1);
		} 
		for ( int i=0; i<size;i++ ) {
			Long key = in.readLong();
			Integer value = in.readInt();
			map.put(key, value);
		}
		return map;
	}

	public String readSafeUTF(DataInput in) throws IOException {
		if(in.readBoolean()) return in.readUTF();
		else return null;
	}

	public void writeSafeUTF(DataOutput out, String value) throws IOException {
		if(value != null) {
			out.writeBoolean(true);
			out.writeUTF(value);
		}
		else out.writeBoolean(false);
	}

	public void writeSerializable(DataOutput out, Serializable value)
			throws IOException {
		if(value != null) {
			out.writeBoolean(true);
			((ObjectOutput)out).writeObject(value);
		}
		else out.writeBoolean(false);
	}

	public Serializable readSerializable(DataInput in) throws IOException {
		if(in.readBoolean()){
			try {
				return (Serializable)((ObjectInput)in).readObject();
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void writeStringList(DataOutput out, List stringList)
			throws IOException {
		out.writeInt(stringList.size());
		out.writeUTF(stringList.getClass().getCanonicalName());
		for(Object value:stringList) {
			out.writeUTF((String)value);
		}

	}

	@SuppressWarnings("unchecked")
	public List<String> readStringList(DataInput in) throws IOException {
		int size = in.readInt();
		List<String>  list;
		try {
			list = (List<String>)Class.forName(in.readUTF(), true, null).newInstance();
		} catch (Exception e1) {
			throw new IOException(e1);
		}
		for(int i=0; i<size;i++){
			list.add(in.readUTF());
		}
		return list;
	}

	public void writeStringMap(DataOutput out, Map<String, String> stringMap)
			throws IOException {
		out.writeInt(stringMap.size());
		out.writeUTF(stringMap.getClass().getCanonicalName());
		for(String key:stringMap.keySet()) {
			out.writeUTF(key);
			out.writeUTF(stringMap.get(key));
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> readStringMap(DataInput in) throws IOException {
		int size = in.readInt();
		Map<String, String>  list;
		try {
			list = (Map<String, String>)Class.forName(in.readUTF(), true, null).newInstance();
		} catch (Exception e1) {
			throw new IOException(e1);
		}
		for(int i=0; i<size;i++) {
			list.put(in.readUTF(),in.readUTF());
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public int readStrings(DataInput in, Collection<String> collection)
			throws IOException {
		int size = in.readInt();
		try {
			collection = (Collection<String>)Class.forName(in.readUTF(), true, null).newInstance();
		} catch (Exception e1) {
			throw new IOException(e1);
		}
		for(int i=0; i<size;i++) {
			collection.add(in.readUTF());
		}
		return size;
	}

	public void writeStrings(DataOutput out, Collection<String> collection)
			throws IOException {
		out.writeInt(collection.size());
		out.writeUTF(collection.getClass().getCanonicalName());
		for(Object value:collection) {
			out.writeUTF((String)value);
		}
	}

	@SuppressWarnings("unchecked")
	public int readStringsMap(DataInput in, Map<String, Set<String>> map)
			throws IOException {
		int size = in.readInt();
		try {
			map = (Map<String, Set<String>>)Class.forName(in.readUTF(), true, null).newInstance();
		} catch (Exception e1) {
			throw new IOException(e1);
		}
		for(int i=0; i<size;i++){
			Set<String> collection=null;
			readStrings(in, collection);
			map.put(in.readUTF(),collection );
		}
		return size;
	}

	public void writeStringsMap(DataOutput out, Map<String, Set<String>> map)
			throws IOException {
		out.writeInt(map.size());
		out.writeUTF(map.getClass().getCanonicalName());
		for(String value:map.keySet()) {
			writeStrings(out, map.get(value));
		}
	}

	public void writeExternalizableCollection(DataOutput out,
			Collection<? extends Externalizable> value) throws IOException {
		out.writeInt(value.size());
		for(Externalizable key:value) {
			out.writeUTF(key.getClass().getCanonicalName());
			key.writeExternal((ObjectOutput)out);
		}
	}

	@SuppressWarnings("unchecked")
	public int readExternalizableCollection(DataInput in,
			Collection<? extends Externalizable> value, ClassLoader loader)
			throws IOException {
		int size = in.readInt();
		int i;
		Collection<Externalizable> value2 = (Collection<Externalizable>)value;
		for(i=0; i<size;i++) {
			Externalizable item;
			try {
				item = (Externalizable)Class.forName(in.readUTF(), true, loader).newInstance();
				item.readExternal((ObjectInput)in);
			} catch (Exception e) {
				throw new IOException(e);
			} 
			value2.add(item);
		}
		return i;
	}

}
