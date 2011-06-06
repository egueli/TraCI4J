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

package it.polito.appeal.traci.query;

import it.polito.appeal.traci.TraCIException;
import it.polito.appeal.traci.TraCIException.UnexpectedData;

import java.io.IOException;

import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

/**
 * A TraCI query is a combination of a command and its response. This abstract
 * class provides some basic functions and constants that help implementing 
 * specific queries.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public abstract class TraCIQuery {

	/**
	 * Reference to the SUMO socket. 
	 */
	protected final Socket sock;
	
	/**
	 * Status returned for a successful command.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Status_Response">Status Response</a>.
	 */
	private static final int STATUS_OK = 0x00;

	/**
	 * Identifier of the 3DPosition datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#3DPosition_.28ubyte_identifier:_0x03.29">3DPosition</a>
	 */
	public static final int DATATYPE_3DPOSITION = 0x03;

	/**
	 * Identifier of the Roadmap Position datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Road_Map_Position_.28ubyte_identifier:_0x04.29">Roadmap Position</a>
	 */
	public static final int DATATYPE_ROADMAP = 0x04;
	
	/**
	 * Identifier of the Boundary Box datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Boundary_Box_.28ubyte_identifier:_0x05.29">Boundary Box</a>
	 */
	public static final int DATATYPE_BOUNDARYBOX = 0x05;
	
	/**
	 * Identifier of the Polygon datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Polygon_.28ubyte_identifier:_0x06.29">Polygon</a>
	 */
	public static final int DATATYPE_POLYGON = 0x06;

	/**
	 * Identifier of ubyte datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Atomar_Types">Atomar Types</a>
	 */
	public static final int DATATYPE_UBYTE = 0x07;
	
	/**
	 * Identifier of byte datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Atomar_Types">Atomar Types</a>
	 */
	public static final int DATATYPE_BYTE = 0x08;
	
	/**
	 * Identifier of integer datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Atomar_Types">Atomar Types</a>
	 */
	public static final int DATATYPE_INTEGER = 0x09;
	
	/**
	 * Identifier of float datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Atomar_Types">Atomar Types</a>
	 */
	public static final int DATATYPE_FLOAT = 0x0A;
	
	/**
	 * Identifier of double datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Atomar_Types">Atomar Types</a>
	 */
	public static final int DATATYPE_DOUBLE = 0x0B;
	
	/**
	 * Identifier of string datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Atomar_Types">Atomar Types</a>
	 */
	public static final int DATATYPE_STRING = 0x0C;
	
	/**
	 * Identifier of stringList datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Atomar_Types">Atomar Types</a>
	 */
	public static final int DATATYPE_STRINGLIST = 0x0E;
	
	/**
	 * Identifier of compound object datatype.
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Atomar_Types">Atomar Types</a>
	 */
	public static final int DATATYPE_COMPOUND = 0x0F;
	
	/**
	 * Constructor of a TraCI query. Requires an established socket to a SUMO
	 * instance.
	 * @param sock
	 */
	public TraCIQuery(Socket sock) {
		this.sock = sock;
	}

	/**
	 * Sends a command and receives the result; reads the message's length field
	 * and the first byte (i.e. the command in reply to) in order to verify that
	 * the response is successful and matches the expected command.
	 * 
	 * @param cmd
	 * @param expectedCommand
	 * @return the storage object of the response, whose position is already set
	 *         to the first non-status command.
	 * @throws IOException
	 * @throws TraCIException
	 * @see <a
	 *      href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Status_Response">Status
	 *      Response</a>
	 */
	protected Storage queryAndGetResponse(Storage cmd, int expectedCommand) throws IOException {
		sock.sendExact(cmd);
		
		Storage response = sock.receiveExact();
		
		checkStatusResponse(response, expectedCommand);
		
		return response;
	}
	

	/**
	 * Reads the message's length field and the first byte (i.e. the command in
	 * reply to) in order to verify that the response is successful and matches
	 * the expected command
	 * 
	 * @param response
	 * @param expectedCommand
	 * @throws TraCIException.UnexpectedResponse
	 *             if the response's command and the specified command don't
	 *             match
	 * @throws TraCIException
	 *             if the command didn't succeed and SUMO retuned a string
	 *             description; such description is contained in the exception's
	 *             message
	 * @see <a
	 *      href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Status_Response">Status
	 *      Response</a>
	 */
	protected static void checkStatusResponse(Storage response, int expectedCommand) throws TraCIException {
		readResponseLength(response);
		
		short cmd = response.readUnsignedByte();
		if(cmd != expectedCommand)
			throw new TraCIException.UnexpectedResponse(expectedCommand, cmd);
		
		
		short status = response.readUnsignedByte();
		String descr = response.readStringASCII();
		if(status != STATUS_OK)
			throw new TraCIException(descr);
	}
	
	/**
	 * Returns the length of a response.
	 * @param response
	 * @return
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Messages">Messages</a>
	 */

	protected static int readResponseLength(Storage response) {
		int len = response.readUnsignedByte();
		if (len == 0)
			return response.readInt();
		else
			return len;
	}
	

	/**
	 * Checks that the following byte in the response matches the expected
	 * value. If not, throws an {@link UnexpectedData} exception with a custom
	 * message.
	 * @param response the response to check
	 * @param what a string description of the check to be done
	 * @param expected the expected value
	 * @throws UnexpectedData if the read value is not what was expected.
	 */
	protected static void checkResponseByte(Storage response, String what, int expected)
			throws UnexpectedData {
		int got = response.readUnsignedByte();
		checkResponse(what, expected, got);
	}
	

	/**
	 * Checks that the following integer in the response matches the expected
	 * value. If not, throws an {@link UnexpectedData} exception with a custom
	 * message.
	 * @param response the response to check
	 * @param what a string description of the check to be done
	 * @param expected the expected value
	 * @throws UnexpectedData if the read value is not what was expected.
	 */
	protected static void checkResponseInt(Storage response, String what, int expected)
			throws UnexpectedData {
		int got = response.readInt();
		checkResponse(what, expected, got);
	}
	
	/**
	 * Compares two values and throws an {@link UnexpectedData} if they don't
	 * match.
	 * @param what a string description of the comparison
	 * @param expected
	 * @param got
	 * @throws UnexpectedData
	 */

	private static void checkResponse(String what, int expected, int got)
			throws UnexpectedData {
		if (got != expected) {
			throw new TraCIException.UnexpectedData(what, expected, got);
		}
	}
}
