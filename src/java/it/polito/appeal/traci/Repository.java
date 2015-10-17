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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.polito.appeal.traci.ReadObjectVarQuery.StringListQ;

/**
 * Represents a collection of TraCI objects, that may or may not be complete
 * w.r.t. its counterpart in SUMO.
 * <p>
 * It requires a {@link StringListQ} query that will be used to retrieve the
 * complete list of IDs to be able to check for correctness of requested IDs.
 * <p>
 * In order for a repository to be efficient, it should be the only class that
 * instantiates new items.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class Repository<V extends TraciObject<?>> {

	// log4j Logger
	private static final Logger log = LogManager.getLogger();

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
	 * 
	 * @param factory
	 *            the {@link ObjectFactory} that will be used to make new
	 *            objects when requested the first time
	 * @param idListQuery
	 *            a reference to a query of list of IDs.
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
	 *         not exist
	 * @throws IOException
	 *             if, in the first call of this method, a query is sent to SUMO
	 *             but something bad happened
	 */
	public V getByID(String id) throws IOException {
		getIDs(); // used only for its collateral effects
		return objectCache.get(id);
	}

	/**
	 * @return a {@link Set} made of all the string IDs of the objects
	 *         represented by this repository.
	 * @throws IOException
	 */
	public Set<String> getIDs() throws IOException {
		/*
		 * If the ID list query wasn't made obsolete, just get the key set from
		 * the object cache (i.e. the previously made object set).
		 */
		// if (idListQuery.hasValue()) {
		// List<String> set1 = idListQuery.get();
		// Set<String> set2 = objectCache.keySet();
		// log.info(set1);
		// log.info(set2);
		// return set2;
		// }
		// boolean test = idListQuery.hasValue();
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
	 * {@link StepAdvanceListener} and therefore need to be updated at every
	 * simulation step.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 * @param <V>
	 */
	static class UpdatableRepository<V extends TraciObject<?> & StepAdvanceListener> extends Repository<V>
			implements StepAdvanceListener {

		public UpdatableRepository(ObjectFactory<V> factory, StringListQ idListQuery) {
			super(factory, idListQuery);
		}

		public void nextStep(double step) {
			for (V item : getCached().values()) {

				if (item == null) {
					log.error("Item is null! ");
				} else
					item.nextStep(step);
			}
		}
	}

	static class Edges extends UpdatableRepository<Edge> {
		Edges(final DataInputStream dis, final DataOutputStream dos, StringListQ idListQuery) {
			super(new ObjectFactory<Edge>() {
				public Edge newObject(String objectID) {
					return new Edge(dis, dos, objectID);
				}
			}, idListQuery);
		}
	}

	static class Lanes extends Repository<Lane> {
		Lanes(final DataInputStream dis, final DataOutputStream dos, final Repository<Edge> edges,
				StringListQ idListQuery) {
			super(null, idListQuery);

			setObjectFactory(new ObjectFactory<Lane>() {
				public Lane newObject(String objectID) {
					return new Lane(dis, dos, objectID, edges, Lanes.this);
				}
			});
		}
	}

	static class Vehicles extends UpdatableRepository<Vehicle> {

		Vehicles(final DataInputStream dis, final DataOutputStream dos, final Repository<Edge> edges,
				final Repository<Lane> lanes, final Map<String, Vehicle> vehicles, final StringListQ idListQuery) {
			super(new ObjectFactory<Vehicle>() {
				/**
				 * This implementation does not make a new object; instead it
				 * looks for a vehicle in the specified map and returns that.
				 */
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
		public POIs(final DataInputStream dis, final DataOutputStream dos, StringListQ idListQuery) {
			super(new ObjectFactory<POI>() {
				public POI newObject(String objectID) {
					return new POI(dis, dos, objectID);
				}
			}, idListQuery);
		}
	}

	static class InductionLoops extends UpdatableRepository<InductionLoop> {
		public InductionLoops(final DataInputStream dis, final DataOutputStream dos, final Repository<Lane> lanes,
				final Repository<Vehicle> vehicles, StringListQ idListQuery) {
			super(new ObjectFactory<InductionLoop>() {
				public InductionLoop newObject(String objectID) {
					return new InductionLoop(dis, dos, objectID, lanes, vehicles);
				}
			}, idListQuery);
		}
	}

	static class TrafficLights extends UpdatableRepository<TrafficLight> {

		public TrafficLights(final DataInputStream dis, final DataOutputStream dos, final Repository<Lane> lanes,
				StringListQ idListQuery) {
			super(new ObjectFactory<TrafficLight>() {
				public TrafficLight newObject(String objectID) {
					return new TrafficLight(dis, dos, objectID, lanes);
				}
			}, idListQuery);
		}
	}

	static class VehicleTypes extends Repository<VehicleType> {
		public VehicleTypes(final DataInputStream dis, final DataOutputStream dos, StringListQ idListQuery) {
			super(new ObjectFactory<VehicleType>() {
				public VehicleType newObject(String objectID) {
					return new VehicleType(dis, dos, objectID);
				}
			}, idListQuery);
		}
	}

	static class MeMeDetectors extends UpdatableRepository<MeMeDetector> {
		public MeMeDetectors(final DataInputStream dis, final DataOutputStream dos, final Repository<Vehicle> vehicles,
				StringListQ idListQuery) {
			super(new ObjectFactory<MeMeDetector>() {
				public MeMeDetector newObject(String objectID) {
					return new MeMeDetector(dis, dos, objectID, vehicles);
				}
			}, idListQuery);
		}
	}

	static class LaArDetectors extends UpdatableRepository<LaArDetector> {
		public LaArDetectors(final DataInputStream dis, final DataOutputStream dos, final Repository<Lane> lanes,
				final Repository<Vehicle> vehicles, StringListQ idListQuery) {
			super(new ObjectFactory<LaArDetector>() {
				public LaArDetector newObject(String objectID) {
					return new LaArDetector(dis, dos, objectID, lanes, vehicles);
				}
			}, idListQuery);
		}
	}

	static class Routes extends UpdatableRepository<Route> {
		public Routes(final DataInputStream dis, final DataOutputStream dos, final Repository<Edge> edges,
				StringListQ idListQuery) {
			super(new ObjectFactory<Route>() {
				public Route newObject(String objectID) {
					return new Route(dis, dos, objectID, edges);
				}
			}, idListQuery);
		}
	}

	/*
	 * TODO add repository definitions for other SUMO object classes
	 */

}
