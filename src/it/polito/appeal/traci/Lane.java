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

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * Represents a lane in the SUMO simulation. It has a string
 * identifier and a shape, i.e. a set of connected segments.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
public class Lane implements Serializable {
	private static final long serialVersionUID = 2584580269405598020L;
	
	public final String externalID;
	public final Path2D shape;
	
	public Lane(String id, Path2D shape) {
		this.externalID = id;
		this.shape = shape;
	}

	public Rectangle2D getBoundingBox() {
		return shape.getBounds2D();
	}

	public int hashCode() {
		return externalID.hashCode();
	}

	public String toString() {
		return externalID + shape;
	}

	public double getLength() {
		PathIterator iterator = shape.getPathIterator(null);
		
		/* TODO cache length */
		double sum = 0;
		Point2D prevPoint = null;
		while (!iterator.isDone()) {
			float[] coords = new float[6];
			int type = iterator.currentSegment(coords);
			switch(type) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				Point2D point = new Point2D.Float(coords[0], coords[1]);
				if (prevPoint != null) {
					sum += prevPoint.distance(point);
				}
				prevPoint = point;
				break;
			default:
				throw new IllegalStateException("can't handle curves in path");
			}
				
			iterator.next();
		}
		
		return sum;
	}
}