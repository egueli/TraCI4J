package it.polito.appeal.traci;

import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.ChangeObjectVarQuery.ChangeIntegerQ;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author thomas.plathe@tu-clausthal.de
 */
public class ChangeLaneChangeModeQuery extends ChangeIntegerQ
{
    ChangeLaneChangeModeQuery(DataInputStream dis, DataOutputStream dos, String objectID ) {
        super(dis, dos, Constants.CMD_SET_VEHICLE_VARIABLE, objectID, Constants.CMD_CHANGE_LANE_CHANGE_MODE);
    }
}
