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

package it.polito.appeal.traci;

import java.util.HashSet;
import java.util.Set;

import it.polito.appeal.traci.TraCIException.UnexpectedData;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.StatusResponse;
import de.uniluebeck.itm.tcpip.Storage;

public class Utils {
	static void checkType(Storage content, int typeID) throws TraCIException.UnexpectedDatatype {
		int b = content.readUnsignedByte();
		if (b != typeID)
			throw new TraCIException.UnexpectedDatatype(typeID, b);
	}

	static void checkByte(Storage content, int expectedByte) throws UnexpectedData {
		int b = content.readUnsignedByte();
		if (b != expectedByte)
			throw new TraCIException.UnexpectedData("byte value" , expectedByte, b);
	}
	
	static void checkObjectID(Storage content, String objectID) throws TraCIException.UnexpectedData {
		String s = content.readStringASCII();
		if (!s.equals(objectID))
			throw new TraCIException.UnexpectedData("object ID", objectID, s);
	}
	
	static void checkStatusResponse(StatusResponse statusResponse, int commandID) throws TraCIException {
		if (statusResponse.id() != commandID)
			throw new TraCIException.UnexpectedData("command/status ID", statusResponse.id(), commandID);
		
		if (statusResponse.result() != Constants.RTYPE_OK)
			throw new TraCIException("SUMO error for command "
					+ statusResponse.id() + ": " + statusResponse.description());
	}

	/**
	 * Given two sets "before" and "after", returns which element were added to
	 * the first one to get to the second one.
	 * Please note that adding the returned items to the "before" set does not
	 * result in a set that is equal to the "after" set.
	 * @param <T>
	 * @param before
	 * @param after
	 * @return
	 */
	public static <T> Set<T> getAddedItems(Set<T> before, Set<T> after) {
		Set<T> out = new HashSet<T>(after);
		out.removeAll(before);
		return out;
	}
	
	/**
	 * Given two sets "before" and "after", returns which elements were removed to
	 * the first one to get to the second one.
	 * Please note that removing the returned items to the "before" set does not
	 * result in a set that is equal to the "after" set.
	 * @param <T>
	 * @param before
	 * @param after
	 * @return
	 */
	public static <T> Set<T> getRemovedItems(Set<T> before, Set<T> after) {
		return getAddedItems(after, before);
	}
}
