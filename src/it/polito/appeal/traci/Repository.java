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

import it.polito.appeal.traci.ReadObjectVarQuery.StringListQ;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a collection of TraCI objects, that may or may not be complete
 * w.r.t. its counterpart in SUMO.
 * <p>
 * It requires a {@link StringListQ} query that will be used to retrieve the
 * complete list of IDs to be able to check for correctness of requested IDs.
 * <p>
 * In order for a repository to be efficient, it should be the only class that
 * instantiates new items. 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class Repository<V extends TraciObject<?>> {
	private final Map<String, V> objectCache = new HashMap<String, V>();
	/*
	 * the factory is not final: there's a setter for those cases when the
	 * factory is the subclass or points to the subclass, that doesn't exist
	 * yet.
	 */
	private ObjectFactory<V> factory;
	private final StringListQ idListQuery;
	
	/**
	 * Constructor for the repository.
	 * @param factory the {@link ObjectFactory} that will be used to make new
	 * objects when requested the first time
	 * @param idListQuery a reference to a query of list of IDs.
	 */
	public Repository(ObjectFactory<V> factory, StringListQ idListQuery) {
		this.factory = factory;
		this.idListQuery = idListQuery;
	}
	
	protected void setObjectFactory(ObjectFactory<V> factory) {
		this.factory = factory;
	}

	protected Map<String, V> getCached() {
		return Collections.unmodifiableMap(objectCache);
	}
	
	/**
	 * Returns the TraCI object associated to the given ID.
	 * <p>
	 * First, a query is made to ensure that the ID is valid: if doesn't exist,
	 * <code>null</code> is returned and the corresponding cached object, if
	 * present, is deleted. Then, checks if the repository already
	 * holds an object with the given ID; if so, it returns
	 * that one, otherwise, it asks the {@link ObjectFactory} passed to the
	 * constructor to build a fresh one. That object is cached for future
	 * requests.
	 * 
	 * @param id
	 * @return the requested object, or <code>null</code> if such object does
	 * not exist
	 * @throws IOException if, in the first call of this method, a query is sent
	 * to SUMO but something bad happened
	 */
	public V getByID(String id) throws IOException {
		if (!getIDs().contains(id)) {
			objectCache.remove(id);
			return null;
		}
		
		if (!objectCache.containsKey(id)) {
			objectCache.put(id, factory.newObject(id));
		}
		
		return objectCache.get(id);
	}
	
	public Set<String> getIDs() throws IOException {
		Set<String> idSet = new HashSet<String>(idListQuery.get());
		return Collections.unmodifiableSet(idSet);
	}
	
	public Map<String, V> getAll() throws IOException {
		for (String id : getIDs()) {
			getByID(id); // used only for its collateral effects
		}
		return Collections.unmodifiableMap(objectCache);
	}
	
	/**
	 * Represents a {@link Repository} whose objects need to clear their cache
	 * at every simulation step.
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 * @param <V>
	 */
	static class RefreshableRepository<V extends TraciObject<?>> extends Repository<V> implements StepAdvanceListener {

		public RefreshableRepository(ObjectFactory<V> factory,
				StringListQ idListQuery) {
			super(factory, idListQuery);
		}

		@Override
		public void nextStep(double step) {
			for (V value : getCached().values()) {
				value.clearCache();
			}
		}
	}
	
	static class Edges extends RefreshableRepository<Edge> {
		Edges(final DataInputStream dis, final DataOutputStream dos, StringListQ idListQuery) {
			super(new ObjectFactory<Edge>() {
				@Override
				public Edge newObject(String objectID) {
					return new Edge(dis, dos, objectID);
				}
			}, idListQuery);
		}
	}
	
	static class Lanes extends Repository<Lane> {
		Lanes(final DataInputStream dis, final DataOutputStream dos, final Repository<Edge> edges, StringListQ idListQuery) {
			super(null, idListQuery);
			
			setObjectFactory(new ObjectFactory<Lane>() {
				@Override
				public Lane newObject(String objectID) {
					return new Lane(dis, dos, objectID, edges, Lanes.this);
				}
			});
		}
	}
	
	static class Vehicles extends RefreshableRepository<Vehicle> {

		Vehicles(
				final DataInputStream dis, 
				final DataOutputStream dos, 
				final Repository<Edge> edges, 
				final Repository<Lane> lanes, 
				final Map<String, Vehicle> vehicles,
				StringListQ idListQuery) {
			super(new ObjectFactory<Vehicle>() {
				/**
				 * This implementation does not make a new object; instead it
				 * looks for a vehicle in the specified map and returns that.
				 */
				@Override
				public Vehicle newObject(String objectID) {
					assert vehicles.containsKey(objectID);
					return vehicles.get(objectID);
				}
			}, idListQuery);
		}
		
	}
	
	static class POIs extends Repository<POI> {
		public POIs(final DataInputStream dis, final DataOutputStream dos, StringListQ idListQuery) {
			super(new ObjectFactory<POI>() {
				@Override
				public POI newObject(String objectID) {
					return new POI(objectID, dis, dos);
				}
			}, idListQuery);
		}
	}
	
	static class InductionLoops extends RefreshableRepository<InductionLoop> {
		public InductionLoops(
				final DataInputStream dis, 
				final DataOutputStream dos, 
				final Repository<Lane> lanes, 
				final Repository<Vehicle> vehicles,
				StringListQ idListQuery) {
			super(new ObjectFactory<InductionLoop>() {
				@Override
				public InductionLoop newObject(String objectID) {
					return new InductionLoop(objectID, lanes, vehicles, dis, dos);
				}
			}, idListQuery);
		}
	}
	
	static class TrafficLights extends RefreshableRepository<TrafficLight> {

		public TrafficLights(
				final DataInputStream dis, 
				final DataOutputStream dos, 
				final Repository<Lane> lanes, 
				StringListQ idListQuery) {
			super(new ObjectFactory<TrafficLight>() {
				@Override
				public TrafficLight newObject(String objectID) {
					return new TrafficLight(objectID, dis, dos, lanes);
				}
			}, idListQuery);
		}
	}
	/*
	 * TODO add repository definitions for other SUMO object classes 
	 */
}
