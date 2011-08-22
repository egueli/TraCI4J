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

package it.polito.appeal.traci.newquery;

import it.polito.appeal.traci.TraCIException;
import it.polito.appeal.traci.TraCIException.UnexpectedData;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.StatusResponse;
import de.uniluebeck.itm.tcpip.Storage;

public class Utils {
	public static void checkType(Storage content, int typeID) throws TraCIException.UnexpectedDatatype {
		int b = content.readUnsignedByte();
		if (b != typeID)
			throw new TraCIException.UnexpectedDatatype(typeID, b);
	}

	public static void checkByte(Storage content, int expectedByte) throws UnexpectedData {
		int b = content.readUnsignedByte();
		if (b != expectedByte)
			throw new TraCIException.UnexpectedData("byte value" , expectedByte, b);
	}
	
	public static void checkObjectID(Storage content, String objectID) throws TraCIException.UnexpectedData {
		String s = content.readStringASCII();
		if (!s.equals(objectID))
			throw new TraCIException.UnexpectedData("object ID", objectID, s);
	}
	
	public static void checkStatusResponse(StatusResponse statusResponse, int commandID) throws TraCIException {
		if (statusResponse.id() != commandID)
			throw new TraCIException.UnexpectedData("command/status ID", statusResponse.id(), commandID);
		
		if (statusResponse.result() != Constants.RTYPE_OK)
			throw new TraCIException("SUMO error for command "
					+ statusResponse.id() + ": " + statusResponse.description());
	}
}
