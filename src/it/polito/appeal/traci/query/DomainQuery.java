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

import java.io.IOException;

import it.polito.appeal.traci.TraCIException.UnexpectedData;
import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

/**
 * Represents a TraCI domain query.
 * 
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a
 *      href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Environment-related_commands#Command_0x73:_Scenario">Scenario
 *      command</a>
 */
public abstract class DomainQuery extends TraCIQuery {

	protected static final int COMMAND_SCENARIO = 0x73;

	protected static final int DOMAIN_ROADMAP = 0x00;

	protected static final int FLAG_GET = 0x00;

	public DomainQuery(Socket sock) {
		super(sock);
	}

	/**
	 * Builds a Storage containing a Scenario reading.
	 * 
	 * @param domain
	 *            the reference domain
	 * @param domainID
	 *            the ID of the object inside the domain
	 * @param variable
	 *            the variable to retrieve
	 * @param varType
	 *            the expected value type
	 * @return the built Storage
	 */
	protected Storage makeCommand(int domain, int domainID, int variable,
			int varType) {
		Storage scenarioCmd = new Storage();
		scenarioCmd.writeUnsignedByte(10);
		scenarioCmd.writeUnsignedByte(COMMAND_SCENARIO);
		scenarioCmd.writeUnsignedByte(FLAG_GET);
		scenarioCmd.writeUnsignedByte(domain);
		scenarioCmd.writeInt(domainID);
		scenarioCmd.writeByte(variable);
		scenarioCmd.writeByte(varType);

		return scenarioCmd;
	}

	/**
	 * Checks that a Storage contains information about the specified domain,
	 * domain object, variable and variable type. If the check fails, throws an
	 * exception.
	 * 
	 * @param response
	 *            the response to check
	 * @param domain
	 *            the expected domain
	 * @param domainID
	 *            the expected domain object
	 * @param variable
	 *            the expected variable
	 * @param varType
	 *            the expected variable type
	 * @throws UnexpectedData
	 */
	protected void readAndCheckResponse(Storage response, int domain,
			int domainID, int variable, int varType) throws UnexpectedData {
		readResponseLength(response);
		checkResponseByte(response, "command", COMMAND_SCENARIO);
		checkResponseByte(response, "flag", FLAG_GET);
		checkResponseByte(response, "domain", domain);
		checkResponseInt(response, "node ID", domainID);
		checkResponseByte(response, "variable ID", variable);
		checkResponseByte(response, "variable type", varType);
	}

	/**
	 * Sends the Scenario command and receives the result, while checking that
	 * the response is successful and matches the sent command.
	 * 
	 * @param cmd
	 * @return the storage object of the response, whose position is already set
	 *         to the variable's value.
	 * @throws IOException
	 * @see {@link TraCIQuery#queryAndGetResponse(Storage, int)}
	 */
	protected Storage queryAndGetResponse(Storage cmd) throws IOException {
		return super.queryAndGetResponse(cmd, COMMAND_SCENARIO);
	}

}
