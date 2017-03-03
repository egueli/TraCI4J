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

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the last step's vehicle data return type  
 * described in &lt;http://sumo.dlr.de/wiki/TraCI/Induction_Loop_Value_Retrieval&gt;
 * 
 * @author Maximiliano Bottazzi &lt;maximiliano.bottazzi@dlr.de&gt;
 *
 */
public class LastStepVehicleData
{
	private Set<InformationPacket> informationPackets = new HashSet<InformationPacket>();
	
	/**
	 * 
	 * @param ip
	 */
	void addInformationPacket(InformationPacket ip)
	{
		informationPackets.add(ip);
	}
	
	/**
	 * 
	 * @return
	 */
	public Set<InformationPacket> getInformationPackets()
	{
		return informationPackets;
	}
	
	/**
	 * This class represents the information packet return type described 
	 * in &lt;http://sumo.dlr.de/wiki/TraCI/Induction_Loop_Value_Retrieval&gt;
	 * 
	 * @author Maximiliano Bottazzi &lt;maximiliano.bottazzi@dlr.de&gt;
	 *
	 */
	public static class InformationPacket
	{
		private String vehicleId;
		private double vehicleLength;
		
		private double entryTime;
		private double leaveTime;
		
		private String vehicleTypeId;
		
		
		public void setVehicleId(String vehicleId)
		{
			this.vehicleId = vehicleId;
		}
		
		public void setVehicleLength(double vehicleLength)
		{
			this.vehicleLength = vehicleLength;
		}
		
		public void setEntryTime(double entryTime)
		{
			this.entryTime = entryTime;
		}
		
		public void setLeaveTime(double leaveTime)
		{
			this.leaveTime = leaveTime;
		}
		
		public void setVehicleTypeId(String vehicleTypeId)
		{
			this.vehicleTypeId = vehicleTypeId;
		}
		
		/**
		 * 
		 * @return
		 */
		public double getEntryTime()
		{
			return entryTime;
		}
		
		/**
		 * 
		 * @return
		 */
		public double getLeaveTime()
		{
			return leaveTime;
		}
		
		/**
		 * 
		 * @return
		 */
		public String getVehicleId()
		{
			return vehicleId;
		}
		
		/**
		 * 
		 * @return
		 */
		public double getVehicleLength()
		{
			return vehicleLength;
		}
		
		/**
		 * 
		 * @return
		 */
		public String getVehicleTypeId()
		{
			return vehicleTypeId;
		}
	}
}
