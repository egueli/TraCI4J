/*   
    Copyright (C) 2017 ApPeAL Group, Politecnico di Torino

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

import java.io.DataInputStream;
import java.io.DataOutputStream;

import de.uniluebeck.itm.tcpip.Storage;
import it.polito.appeal.traci.LastStepVehicleData.InformationPacket;
import it.polito.appeal.traci.protocol.Command;

/**
 * Query for obtaining a set of {@link LastStepVehicleData}s 
 * 
 * @author Maximiliano Bottazzi &lt;maximiliano.bottazzi@dlr.de&gt;
 *
 */
public class LastStepDataQuery extends ReadObjectVarQuery<LastStepVehicleData> {

	LastStepDataQuery(DataInputStream dis, DataOutputStream dos, int commandID,
			String objectID, int varID)	{
		super(dis, dos, commandID, objectID, varID);
	}
	
	@Override
	protected LastStepVehicleData readValue(Command resp) throws TraCIException
	{
		Storage content = resp.content();

		content.readUnsignedByte();
		content.readInt();
		content.readUnsignedByte();
		
		int packsCount = content.readInt();
		
		LastStepVehicleData lsvd = new LastStepVehicleData();
		
		for (int i = 0; i < packsCount; i++)
		{
			int tmp = content.readUnsignedByte();			
			String vehicleId = content.readStringASCII();
			tmp = content.readUnsignedByte();
			double vehicleLength = content.readDouble();
			tmp = content.readUnsignedByte();
			double entryTime = content.readDouble();
			tmp = content.readUnsignedByte();
			double leaveTime = content.readDouble();
			tmp = content.readUnsignedByte();
			String vehicleTypeId = content.readStringASCII();
			
			InformationPacket ip = new InformationPacket();
			ip.setVehicleId(vehicleId);
			ip.setVehicleLength(vehicleLength);
			ip.setEntryTime(entryTime);
			ip.setLeaveTime(leaveTime);
			ip.setVehicleTypeId(vehicleTypeId);
			
			lsvd.addInformationPacket(ip);						
		}
		
		return lsvd;
	}	
	
}
