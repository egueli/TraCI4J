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

package it.polito.appeal.traci;

import it.polito.appeal.traci.protocol.Constants;

import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class SimulationData extends TraciObject<SimulationData.Variable> implements StepAdvanceListener {

	enum Variable {
		CURRENT_SIM_TIME (Constants.VAR_TIME_STEP),
		NET_BOUNDARIES (Constants.VAR_NET_BOUNDING_BOX),
		POSITION_CONVERSION (Constants.POSITION_CONVERSION)
		;
		public final int id;
		private Variable(int id) {
			this.id = id;
		}
	}
	
	SimulationData(DataInputStream dis, DataOutputStream dos) {
		super("", Variable.class);
		
		addReadQuery(Variable.CURRENT_SIM_TIME,
				new ReadObjectVarQuery.IntegerQ(dis, dos,
						Constants.CMD_GET_SIM_VARIABLE, "",
						Variable.CURRENT_SIM_TIME.id) {
			
		});
		
		addReadQuery(Variable.NET_BOUNDARIES,
				new ReadObjectVarQuery.BoundingBoxQ(dis, dos,
						Constants.CMD_GET_SIM_VARIABLE, "",
						Variable.NET_BOUNDARIES.id));
		
		addReadQuery(Variable.POSITION_CONVERSION,
				new PositionConversionQuery(dis, dos,
						Constants.CMD_GET_SIM_VARIABLE, "",
						Variable.POSITION_CONVERSION.id));

	}

	/**
	 * 
	 * @return a query for obtaining the current simulation time
	 */
	public ReadObjectVarQuery<Integer> queryCurrentSimTime() {
		return (ReadObjectVarQuery.IntegerQ) getReadQuery(Variable.CURRENT_SIM_TIME);
	}
	
	/**
	 * Note: the data returned from this query will return the network
	 * boundaries regardless of the lane width and number. Actual borders of
	 * lanes, as well as position of vehicles, may lie outside these boundaries.
	 * To read absolute borders, iterate over all lanes and calculate the
	 * common bounding box.
	 * 
	 * @return the bounding box of the road network
	 */
	public ReadObjectVarQuery<Rectangle2D> queryNetBoundaries() {
		return (ReadObjectVarQuery.BoundingBoxQ) getReadQuery(Variable.NET_BOUNDARIES);
	}

	/**
	 * 
	 * @return a query to do position conversion
	 */
	public PositionConversionQuery queryPositionConversion() {
		return (PositionConversionQuery) getReadQuery(Variable.POSITION_CONVERSION);
	}
	
	@Override
	public void nextStep(double step) {
		queryCurrentSimTime().setObsolete();
	}
}
