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

package it.polito.appeal.traci.protocol;

import it.polito.appeal.traci.TraCIException;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.uniluebeck.itm.tcpip.Storage;

public class BoundingBox extends Rectangle2D {

	private final Rectangle2D.Double rect;
	
	public BoundingBox(Storage storage, boolean verifyType) throws TraCIException {
		if (verifyType) {
			if (storage.readByte() != Constants.TYPE_BOUNDINGBOX)
				throw new TraCIException("bounding box expected");
		}

		double llX = storage.readDouble();
		double llY = storage.readDouble();
		double urX = storage.readDouble();
		double urY = storage.readDouble();
		
		rect = new Rectangle2D.Double(llX, llY, urX - llX, urY - llY);
	}
	
	public void writeTo(Storage out) {
		out.writeByte(Constants.TYPE_BOUNDINGBOX);
		out.writeDouble(rect.x);
		out.writeDouble(rect.y);
		out.writeDouble(rect.x + rect.width);
		out.writeDouble(rect.y + rect.height);
	}

	
	/**
	 * @param newx
	 * @param newy
	 * @see java.awt.geom.Rectangle2D#add(double, double)
	 */
	public void add(double newx, double newy) {
		rect.add(newx, newy);
	}

	/**
	 * @param pt
	 * @see java.awt.geom.Rectangle2D#add(java.awt.geom.Point2D)
	 */
	public void add(Point2D pt) {
		rect.add(pt);
	}

	/**
	 * @param r
	 * @see java.awt.geom.Rectangle2D#add(java.awt.geom.Rectangle2D)
	 */
	public void add(Rectangle2D r) {
		rect.add(r);
	}

	/**

	 * @see java.awt.geom.RectangularShape#clone()
	 */
	public Object clone() {
		return rect.clone();
	}

	/**
	 * @param x
	 * @param y
	 * @param w
	 * @param h

	 * @see java.awt.geom.Rectangle2D#contains(double, double, double, double)
	 */
	public boolean contains(double x, double y, double w, double h) {
		return rect.contains(x, y, w, h);
	}

	/**
	 * @param x
	 * @param y

	 * @see java.awt.geom.Rectangle2D#contains(double, double)
	 */
	public boolean contains(double x, double y) {
		return rect.contains(x, y);
	}

	/**
	 * @param arg0

	 * @see java.awt.geom.RectangularShape#contains(java.awt.geom.Point2D)
	 */
	public boolean contains(Point2D arg0) {
		return rect.contains(arg0);
	}

	/**
	 * @param arg0

	 * @see java.awt.geom.RectangularShape#contains(java.awt.geom.Rectangle2D)
	 */
	public boolean contains(Rectangle2D arg0) {
		return rect.contains(arg0);
	}

	/**
	 * @param r

	 * @see java.awt.geom.Rectangle2D#createIntersection(java.awt.geom.Rectangle2D)
	 */
	public Rectangle2D createIntersection(Rectangle2D r) {
		return rect.createIntersection(r);
	}

	/**
	 * @param r

	 * @see java.awt.geom.Rectangle2D#createUnion(java.awt.geom.Rectangle2D)
	 */
	public Rectangle2D createUnion(Rectangle2D r) {
		return rect.createUnion(r);
	}

	/**
	 * @param obj

	 * @see java.awt.geom.Rectangle2D#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return rect.equals(obj);
	}

	/**

	 * @see java.awt.geom.RectangularShape#getBounds()
	 */
	public Rectangle getBounds() {
		return rect.getBounds();
	}

	/**

	 * @see java.awt.geom.Rectangle2D#getBounds2D()
	 */
	public Rectangle2D getBounds2D() {
		return rect.getBounds2D();
	}

	/**

	 * @see java.awt.geom.RectangularShape#getCenterX()
	 */
	public double getCenterX() {
		return rect.getCenterX();
	}

	/**

	 * @see java.awt.geom.RectangularShape#getCenterY()
	 */
	public double getCenterY() {
		return rect.getCenterY();
	}

	/**

	 * @see java.awt.geom.RectangularShape#getFrame()
	 */
	public Rectangle2D getFrame() {
		return rect.getFrame();
	}

	/**

	 * @see java.awt.geom.RectangularShape#getHeight()
	 */
	public double getHeight() {
		return rect.getHeight();
	}

	/**

	 * @see java.awt.geom.RectangularShape#getMaxX()
	 */
	public double getMaxX() {
		return rect.getMaxX();
	}

	/**

	 * @see java.awt.geom.RectangularShape#getMaxY()
	 */
	public double getMaxY() {
		return rect.getMaxY();
	}

	/**

	 * @see java.awt.geom.RectangularShape#getMinX()
	 */
	public double getMinX() {
		return rect.getMinX();
	}

	/**

	 * @see java.awt.geom.RectangularShape#getMinY()
	 */
	public double getMinY() {
		return rect.getMinY();
	}

	/**
	 * @param at
	 * @param flatness

	 * @see java.awt.geom.Rectangle2D#getPathIterator(java.awt.geom.AffineTransform, double)
	 */
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return rect.getPathIterator(at, flatness);
	}

	/**
	 * @param at

	 * @see java.awt.geom.Rectangle2D#getPathIterator(java.awt.geom.AffineTransform)
	 */
	public PathIterator getPathIterator(AffineTransform at) {
		return rect.getPathIterator(at);
	}

	/**

	 * @see java.awt.geom.RectangularShape#getWidth()
	 */
	public double getWidth() {
		return rect.getWidth();
	}

	/**

	 * @see java.awt.geom.RectangularShape#getX()
	 */
	public double getX() {
		return rect.getX();
	}

	/**

	 * @see java.awt.geom.RectangularShape#getY()
	 */
	public double getY() {
		return rect.getY();
	}

	/**

	 * @see java.awt.geom.Rectangle2D#hashCode()
	 */
	public int hashCode() {
		return rect.hashCode();
	}

	/**
	 * @param x
	 * @param y
	 * @param w
	 * @param h

	 * @see java.awt.geom.Rectangle2D#intersects(double, double, double, double)
	 */
	public boolean intersects(double x, double y, double w, double h) {
		return rect.intersects(x, y, w, h);
	}

	/**
	 * @param arg0

	 * @see java.awt.geom.RectangularShape#intersects(java.awt.geom.Rectangle2D)
	 */
	public boolean intersects(Rectangle2D arg0) {
		return rect.intersects(arg0);
	}

	/**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2

	 * @see java.awt.geom.Rectangle2D#intersectsLine(double, double, double, double)
	 */
	public boolean intersectsLine(double x1, double y1, double x2, double y2) {
		return rect.intersectsLine(x1, y1, x2, y2);
	}

	/**
	 * @param l

	 * @see java.awt.geom.Rectangle2D#intersectsLine(java.awt.geom.Line2D)
	 */
	public boolean intersectsLine(Line2D l) {
		return rect.intersectsLine(l);
	}

	/**

	 * @see java.awt.geom.RectangularShape#isEmpty()
	 */
	public boolean isEmpty() {
		return rect.isEmpty();
	}

	/**
	 * @param x
	 * @param y

	 * @see java.awt.geom.Rectangle2D#outcode(double, double)
	 */
	public int outcode(double x, double y) {
		return rect.outcode(x, y);
	}

	/**
	 * @param p

	 * @see java.awt.geom.Rectangle2D#outcode(java.awt.geom.Point2D)
	 */
	public int outcode(Point2D p) {
		return rect.outcode(p);
	}

	/**
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @see java.awt.geom.Rectangle2D#setFrame(double, double, double, double)
	 */
	public void setFrame(double x, double y, double w, double h) {
		rect.setFrame(x, y, w, h);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see java.awt.geom.RectangularShape#setFrame(java.awt.geom.Point2D, java.awt.geom.Dimension2D)
	 */
	public void setFrame(Point2D arg0, Dimension2D arg1) {
		rect.setFrame(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see java.awt.geom.RectangularShape#setFrame(java.awt.geom.Rectangle2D)
	 */
	public void setFrame(Rectangle2D arg0) {
		rect.setFrame(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @see java.awt.geom.RectangularShape#setFrameFromCenter(double, double, double, double)
	 */
	public void setFrameFromCenter(double arg0, double arg1, double arg2,
			double arg3) {
		rect.setFrameFromCenter(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see java.awt.geom.RectangularShape#setFrameFromCenter(java.awt.geom.Point2D, java.awt.geom.Point2D)
	 */
	public void setFrameFromCenter(Point2D arg0, Point2D arg1) {
		rect.setFrameFromCenter(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @see java.awt.geom.RectangularShape#setFrameFromDiagonal(double, double, double, double)
	 */
	public void setFrameFromDiagonal(double arg0, double arg1, double arg2,
			double arg3) {
		rect.setFrameFromDiagonal(arg0, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see java.awt.geom.RectangularShape#setFrameFromDiagonal(java.awt.geom.Point2D, java.awt.geom.Point2D)
	 */
	public void setFrameFromDiagonal(Point2D arg0, Point2D arg1) {
		rect.setFrameFromDiagonal(arg0, arg1);
	}

	/**
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @see java.awt.geom.Rectangle2D#setRect(double, double, double, double)
	 */
	public void setRect(double x, double y, double w, double h) {
		rect.setRect(x, y, w, h);
	}

	/**
	 * @param r
	 * @see java.awt.geom.Rectangle2D#setRect(java.awt.geom.Rectangle2D)
	 */
	public void setRect(Rectangle2D r) {
		rect.setRect(r);
	}

	/**

	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return rect.toString();
	}

}
