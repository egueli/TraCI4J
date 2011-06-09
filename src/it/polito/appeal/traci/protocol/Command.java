/*   
    Copyright (C) 2011 ApPeAL Group, Politecnico di Torino

    This file is part of TraCI4J.

    TraCI4J is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TraCI4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TraCI4J.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.polito.appeal.traci.protocol;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Represents a single TraCI command, with its identifier and content. The
 * content itself is represented as a byte array and an offset/length relative
 * to that array.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class Command {
	private static final int HEADER_SIZE = 
		Byte.SIZE/8     // length 0
	  + Integer.SIZE/8  // integer length
	  + Byte.SIZE/8;     // command id
	
	private final int id;
	private final Storage content;
	
	
	public Command(Storage rawStorage) {
		int contentLen = rawStorage.readUnsignedByte();
		if (contentLen == 0)
			contentLen = rawStorage.readInt() - 6;
		else
			contentLen = contentLen - 2;

		id = rawStorage.readUnsignedByte();
		
		short[] buf = new short[contentLen];
		for (int i=0; i<contentLen; i++) {
			buf[i] = (byte)rawStorage.readUnsignedByte();
		}
		
		content = new Storage(buf);
	}
	
	public Command(int id) {
		if (id > 255)
			throw new IllegalArgumentException("id should fit in a byte");
		content = new Storage();
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int id() {
		return id;
	}

	/**
	 * @return the content
	 */
	public Storage content() {
		return content;
	}

	public void writeRawTo(Storage out) {
		/*
		 * use only the long form (length 0 + length as integer)
		 */
		out.writeByte(0);
		out.writeInt(HEADER_SIZE + content.size());
		
		out.writeUnsignedByte(id);
		
		for (Byte b : content.getStorageList()) {
			out.writeByte(b);
		}
	}

	public int rawSize() {
		return HEADER_SIZE + content.size();
	}
}
