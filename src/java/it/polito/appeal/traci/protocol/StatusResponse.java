/*   
    Copyright (C) 2013 ApPeAL Group, Politecnico di Torino

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

import java.io.IOException;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Representation of a status response, that describes the outcome of a command.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class StatusResponse {
	private final int id;
	private final int result;
	private final String description;
	
	/**
	 * Constructor for a successful outcome without a description.
	 * 
	 * @param id
	 */
	public StatusResponse(int id) {
		this(id, Constants.RTYPE_OK, "");
	}
	
	/**
	 * Constructor for an outcome with a description.
	 * 
	 * @param id
	 * @param result
	 * @param description
	 */
	public StatusResponse(int id, int result, String description) {
		this.id = id;
		this.result = result;
		this.description = description;
	}
	
	/**
	 * Constructor from de-serialization.
	 * 
	 * @param packet
	 * @throws IOException
	 */
	public StatusResponse(Storage packet) throws IOException {
		int len = packet.readByte();
		if (len == 0)
			packet.readInt(); // length is ignored; we can derive it
		
		id = packet.readUnsignedByte();
		result = packet.readUnsignedByte();
		description = packet.readStringASCII();
	}

	/**
	 * 
	 * @return the command ID that this response refers to
	 */
	public int id() {
		return id;
	}
	
	/**
	 * @return the outcome description
	 */
	public int result() {
		return result;
	}

	/**
	 * @return the description
	 */
	public String description() {
		return description;
	}
	
	/**
	 * Serializes this object to the given {@link Storage}.
	 * @param out
	 */
	public void writeTo(Storage out) {
		out.writeByte(0);
		out.writeInt(5+1+1+4+description.length());
		out.writeByte(id);
		out.writeByte(result);
		out.writeStringASCII(description);
	}
}
