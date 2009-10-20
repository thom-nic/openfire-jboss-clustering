package com.enernoc.rnd.openfire.cluster.cache;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public class Widget implements Externalizable {
	String name;
	int count;
	
	public Widget( String n, int c ) {
		this.name = n; this.count = c;
	}
	
	public Widget() {
		// TODO Auto-generated constructor stub
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.name = in.readUTF();
		this.count = in.readInt();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF( name );
		out.writeInt( count );
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Widget && this.name.equals( ((Widget)o).name );
	}
}