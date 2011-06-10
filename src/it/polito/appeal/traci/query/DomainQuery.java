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

import it.polito.appeal.traci.TraCIException;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.ResponseContainer;

import java.net.Socket;

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
public abstract class DomainQuery extends Query {


	private static final int FLAG_GET = 0;

	public DomainQuery(Socket sock) throws IOException {
		super(sock);
	}

	/**
	 * Builds a Command containing a Scenario reading.
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
	protected Command makeCommand(int domain, int domainID, int variable,
			int varType) {
		Command scenarioCmd = new Command(Constants.CMD_SCENARIO);
		scenarioCmd.content().writeUnsignedByte(FLAG_GET);
		scenarioCmd.content().writeUnsignedByte(domain);
		scenarioCmd.content().writeInt(domainID);
		scenarioCmd.content().writeByte(variable);
		scenarioCmd.content().writeByte(varType);

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
	 * @throws TraCIException 
	 */
	protected void verifyScenarioResponse(Command response, int domain,
			int domainID, int variable, int varType) throws TraCIException {
		
		Storage content = response.content();
		verify("flag", FLAG_GET, (int)content.readUnsignedByte());
		verify("domain", domain, (int)content.readUnsignedByte());
		verify("domain-specific ID", domainID, content.readInt());
		verify("variable ID", variable, (int)content.readUnsignedByte());
		verify("variable type", varType, (int)content.readUnsignedByte());
	}
	
	protected Command queryAndVerifyScenarioCommand(int domain, int domainID,
			int variable, int varType) throws IOException {
		Command req = makeCommand(domain, domainID, variable, varType);
		ResponseContainer respc = queryAndVerifySingle(req);
		Command resp = respc.getResponse();
		verifyScenarioResponse(resp, domain, domainID, variable, varType);
		return resp;
	}
}
