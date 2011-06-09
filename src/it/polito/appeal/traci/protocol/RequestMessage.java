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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Contains utility functions for handling a TraCI message, i.e. a packet
 * composed by the length header and
 * the payload, as specified in the TraCI wiki. 
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a
 *      href="https://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Messages">https://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Messages</a>
 */
public class RequestMessage {

	private final List<Command> commands = new ArrayList<Command>();
	
	public void append(Command c) {
		commands.add(c);
	}
	
	public void writeTo(DataOutputStream dos) throws IOException {
		int totalLen = Integer.SIZE/8; // the length header
		
		for (Command cmd : commands) {
			totalLen += cmd.rawSize();
		}
		
		dos.writeInt(totalLen);
		
		for (Command cmd : commands) {
			Storage s = new Storage();
			cmd.writeRawTo(s);
			storageToDOS(s, dos);
		}
	}
	
	private void storageToDOS(Storage storage, DataOutputStream dos) throws IOException {
		for (Byte b : storage.getStorageList())
			dos.writeByte(b);
	}
	
	public List<Command> commands() {
		return Collections.unmodifiableList(commands);
	}
}
