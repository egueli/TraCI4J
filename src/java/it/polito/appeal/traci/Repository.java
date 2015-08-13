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

import it.polito.appeal.traci.ReadObjectVarQuery.StringListQ;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;


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
	
	// log4j Logger
	private static final Logger log = Logger.getLogger(Repository.class);
	
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
	Repository(ObjectFactory<V> factory, StringListQ idListQuery) {
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
	 * Synchronizes the local set of objects with SUMO's counterparts and
	 * returns the TraCI object associated to the given ID.
	 * 
	 * @param id
	 * @return the requested object, or <code>null</code> if such object does
	 * not exist
	 * @throws IOException if, in the first call of this method, a query is sent
	 * to SUMO but something bad happened
	 */
	public V getByID(String id) throws IOException {
		getIDs(); // used only for its collateral effects
		return objectCache.get(id);
	}
	
	/**
	 * @return a {@link Set} made of all the string IDs of the objects
	 * represented by this repository.
	 * @throws IOException
	 */
	public Set<String> getIDs() throws IOException {
		/*
		 * If the ID list query wasn't made obsolete, just get the key set
		 * from the object cache (i.e. the previously made object set).
		 */
//		if (idListQuery.hasValue()) {
//			List<String> set1 = idListQuery.get();
//			Set<String> set2 = objectCache.keySet();
//			log.info(set1);
//			log.info(set2);
//			return set2;
//		}
//		boolean test = idListQuery.hasValue();
		/*
		 * Here we also update the cache.
		 */
		Set<String> idSet = new HashSet<String>(idListQuery.get());
		
		final Set<String> cachedSet = objectCache.keySet();
		
		if (!cachedSet.equals(idSet)) {
			Set<String> added = Utils.getAddedItems(cachedSet, idSet);
			for (String newID : added) {
				V newObject = factory.newObject(newID);
				if (newObject == null)
					throw new IllegalStateException("newObject == null");
				objectCache.put(newID, newObject);
			}
			
			Set<String> removed = Utils.getRemovedItems(cachedSet, idSet);
			for (String oldID : removed) {
				objectCache.remove(oldID);
			}
		}
		
		return Collections.unmodifiableSet(idSet);
	}
	
	/**
	 * @return a mapping between the known SUMO object IDs and the corresponding
	 *         repository objects
	 * @throws IOException
	 */
	public Map<String, V> getAll() throws IOException {
		getIDs(); // used only for its collateral effects
		return Collections.unmodifiableMap(objectCache);
	}

	/**
	 * 
	 * @return the {@link StringListQ} query associated to this repository.
	 */
	public StringListQ getQuery() {
		return idListQuery;
	}
	
	/**
	 * Represents a {@link Repository} whose objects implement
	 * {@link StepAdvanceListener} and therefore need to be updated at
	 * every simulation step.
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 * @param <V>
	 */
	static class UpdatableRepository<V extends TraciObject<?> & StepAdvanceListener> extends Repository<V> implements StepAdvanceListener {

		public UpdatableRepository(ObjectFactory<V> factory,
				StringListQ idListQuery) {
			super(factory, idListQuery);
		}

		@Override
		public void nextStep(double step) {
			for (V item : getCached().values()) {
				
				if(item == null){
					log.error("Item is null! ");
				}
				else
					item.nextStep(step);
			}
		}
	}
	
	static class Edges extends UpdatableRepository<Edge> {
		Edges(final TraciChannel traciChannel, StringListQ idListQuery) {
			super(new ObjectFactory<Edge>() {
				@Override
				public Edge newObject(String objectID) {
					return new Edge(traciChannel, objectID);
				}
			}, idListQuery);
		}
	}
	
	static class Lanes extends Repository<Lane> {
		Lanes(final TraciChannel traciChannel, final Repository<Edge> edges, StringListQ idListQuery) {
			super(null, idListQuery);
			
			setObjectFactory(new ObjectFactory<Lane>() {
				@Override
				public Lane newObject(String objectID) {
					return new Lane(traciChannel, objectID, edges, Lanes.this);
				}
			});
		}
	}
	
	static class Vehicles extends UpdatableRepository<Vehicle> {

		Vehicles(
				final TraciChannel traciChannel, 
				final Repository<Edge> edges, 
				final Repository<Lane> lanes, 
				final Map<String, Vehicle> vehicles,
				final StringListQ idListQuery) {
			super(new ObjectFactory<Vehicle>() {
				/**
				 * This implementation does not make a new object; instead it
				 * looks for a vehicle in the specified map and returns that.
				 */
				@Override
				public Vehicle newObject(String objectID) {
					if (!vehicles.containsKey(objectID))
						throw new IllegalArgumentException("vehicleID '" + objectID + "' not found in vehicles map");
					else
						log.debug(" vehicleID " + objectID + " found in vehicles map");
					return vehicles.get(objectID);
				}
			}, idListQuery);
			
		}
	}
	
	static class POIs extends Repository<POI> {
		public POIs(final TraciChannel traciChannel, StringListQ idListQuery) {
			super(new ObjectFactory<POI>() {
				@Override
				public POI newObject(String objectID) {
					return new POI(traciChannel, objectID);
				}
			}, idListQuery);
		}
	}
	
	static class InductionLoops extends UpdatableRepository<InductionLoop> {
		public InductionLoops(
				final TraciChannel traciChannel, 
				final Repository<Lane> lanes, 
				final Repository<Vehicle> vehicles,
				StringListQ idListQuery) {
			super(new ObjectFactory<InductionLoop>() {
				@Override
				public InductionLoop newObject(String objectID) {
					return new InductionLoop(traciChannel, objectID, lanes, vehicles);
				}
			}, idListQuery);
		}
	}
	
	static class TrafficLights extends UpdatableRepository<TrafficLight> {

		public TrafficLights(
				final TraciChannel traciChannel, 
				final Repository<Lane> lanes, 
				StringListQ idListQuery) {
			super(new ObjectFactory<TrafficLight>() {
				@Override
				public TrafficLight newObject(String objectID) {
					return new TrafficLight(traciChannel, objectID, lanes);
				}
			}, idListQuery);
		}
	}
	
	static class VehicleTypes extends Repository<VehicleType> {
		public VehicleTypes(final TraciChannel traciChannel, StringListQ idListQuery) {
			super(new ObjectFactory<VehicleType>() {
				@Override
				public VehicleType newObject(String objectID) {
					return new VehicleType(traciChannel, objectID);
				}
			}, idListQuery);
		}
	}
	
	static class MeMeDetectors extends UpdatableRepository<MeMeDetector> {
		public MeMeDetectors(
				final TraciChannel traciChannel, 
				final Repository<Vehicle> vehicles, 
				StringListQ idListQuery) {
			super(new ObjectFactory<MeMeDetector>() {
				@Override
				public MeMeDetector newObject(String objectID) {
					return new MeMeDetector(traciChannel, objectID, vehicles);
				}
			}, idListQuery);
		}
	}
	
	static class Routes extends UpdatableRepository<Route> {
		public Routes(
				final TraciChannel traciChannel, 
				final Repository<Edge> edges, 
				StringListQ idListQuery) {
			super(new ObjectFactory<Route>() {
				@Override
				public Route newObject(String objectID) {
					return new Route(traciChannel, objectID, edges);
				}
			}, idListQuery);
		}
	}	
	
	/*
	 * TODO add repository definitions for other SUMO object classes 
	 */

}
