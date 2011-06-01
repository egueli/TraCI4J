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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Represents a road (i.e. a lane) in the SUMO simulation. A road has two
 * identifiers (an "external" string identifier, used by SUMO itself, and an
 * "internal" integer identifier, used by the TraCI protocol) and a shape,
 * i.e. a set of connected segments.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
public class Road implements Serializable {
	public final int internalID;
	public final String externalID;
	public final List<Point2D> shape;
	
	private transient Rectangle2D boundingBoxCache;

	public Road(int internalID, String externalID, List<Point2D> shape) {
		this.internalID = internalID;
		this.externalID = externalID;
		this.shape = Collections.unmodifiableList(shape);
	}

	public Rectangle2D getBoundingBox() {
		if(boundingBoxCache == null) {

			double left = Double.POSITIVE_INFINITY;
			double right = Double.NEGATIVE_INFINITY;
			double top = Double.POSITIVE_INFINITY;
			double bottom = Double.NEGATIVE_INFINITY;
			
			for(Point2D p : shape) {
				left =   Math.min(left,   p.getX());
				right =  Math.max(right,  p.getX());
				top =    Math.min(top,    p.getY());
				bottom = Math.max(bottom, p.getY());
			}
			
			boundingBoxCache = new Rectangle2D.Double(left, top, right - left, bottom - top);
		}
		
		return boundingBoxCache;
	}

	public String getEdgeID() {
		return externalID.substring(0, externalID.lastIndexOf('_'));
	}

	public int hashCode() {
		return externalID.hashCode();
	}

	public String toString() {
		return externalID + " (internal " + internalID + ") " + shape;
	}

	public double getLength() {
		/* TODO cache length */
		double sum = 0;
		Point2D prevPoint = null;
		for (Point2D point : shape) {
			if (prevPoint != null) {
				sum += prevPoint.distance(point);
			}
			prevPoint = point;
		}
		return sum;
	}
}