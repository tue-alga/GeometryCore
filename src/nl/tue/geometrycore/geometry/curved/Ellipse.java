/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.curved;

import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * A simple ellipse. Note that, although it inherits from CyclicGeometry, it has
 * no clear orientation. Thus, the computed area will always be positive.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Ellipse extends CyclicGeometry<Ellipse> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Vector _f1, _f2;
    private double _d;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs an ellipse given two focal points and a distance d. The
     * ellipse is the locus of points for which the distance to f1 plus the
     * distance to f2 adds up to d.
     *
     * @param f1 first focal point
     * @param f2 second focal point
     * @param d total distance to both focal points along the boundary
     */
    public Ellipse(Vector f1, Vector f2, double d) {
        _f1 = f1;
        _f2 = f2;
        _d = d;
    }

    /**
     * Constructs an ellipse that is the inscribed ellipse of the given
     * rectangle. That is, the ellipse touches each of the four sizes of the
     * rectangle. Returns null if the rectangle is empty.
     *
     * @param rect The rectangle to be inscribed
     * @return The inscribed ellipse
     */
    public static Ellipse fromRectangle(Rectangle rect) {
        if (rect.isEmpty()) {
            return null;
        }

        double w = rect.width();
        double h = rect.height();

        if (w >= h) {
            return Ellipse.fromCenterAxisAndSize(rect.center(), Vector.right(), w, h);
        } else {
            return Ellipse.fromCenterAxisAndSize(rect.center(), Vector.up(), h, w);
        }
    }

    /**
     * Constructs an ellipse, from its center point, major axis, and extend
     * along the major axis and minor axis.
     *
     * @param center Center point of the ellipse
     * @param majorAxis The major axis, assumed to be normalized
     * @param majorWidth The length of the ellipse in the axis direction
     * @param minorWidth The length of the ellipse in the direction orthogonal
     * to the axis
     * @return The specified ellipse
     */
    public static Ellipse fromCenterAxisAndSize(Vector center, Vector majorAxis, double majorWidth, double minorWidth) {
        assert DoubleUtil.close(majorAxis.length(), 1);

        double a = majorWidth / 2.0;
        double b = minorWidth / 2.0;
        double c = Math.sqrt(a * a - b * b);

        Vector dir = Vector.multiply(c, majorAxis);
        return new Ellipse(Vector.add(center, dir), Vector.subtract(center, dir), majorWidth);
    }

    /**
     * Constructs an ellipse that matches the given circle. This ellipse is an
     * independent copy of the circle: the defining vectors are independent.
     *
     * @param c The circle to be copied
     * @return An ellipse equal to c
     */
    public static Ellipse fromCircle(Circle c) {
        Vector ctr = c.getCenter().clone();
        return new Ellipse(ctr, ctr.clone(), 2 * c.getRadius());
    }

    /**
     * Constructs an ellipse given two focal points and a distance d. The
     * ellipse is the locus of points for which the distance to f1 plus the
     * distance to f2 adds up to d.
     *
     * @param f1 first focal point
     * @param f2 second focal point
     * @param d total distance to both focal points along the boundary
     * @return the described ellipse
     */
    public static Ellipse fromFocalPoints(Vector f1, Vector f2, double d) {
        return new Ellipse(f1, f2, d);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public Vector getFirstFocalPoint() {
        return _f1;
    }

    public void setFirstFocalPoint(Vector f1) {
        _f1 = f1;
    }

    public Vector getSecondFocalPoint() {
        return _f2;
    }

    public void setSecondFocalPoint(Vector f2) {
        _f2 = f2;
    }

    public double getDistance() {
        return _d;
    }

    public void setDistance(double d) {
        _d = d;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * The ellipse is considered "empty", if the focal points are further apart
     * than the distance. That is, the locus of points with a total distance to
     * both focal points being exactly the specified distance is empty.
     *
     * @return
     */
    public boolean isEmpty() {
        return _d * _d < _f1.squaredDistanceTo(_f2);
    }

    /**
     * The ellipse is considered a circle when its two focal points are
     * approximately equal.
     *
     * @return true iff the ellipse is a circle
     */
    public boolean isCircle() {
        return _f1.isApproximately(_f2);
    }

    /**
     * Computes the center of the ellipse.
     *
     * @return The point halfway the two focal points
     */
    public Vector getCenter() {
        return Vector.interpolate(_f1, _f2, 0.5);
    }

    /**
     * Computes a normalized vector that represents the major axis. Returns null
     * for empty ellipses, and a vector that is orthogonal to getMinorAxis() for
     * circles.
     *
     * @return Normalized vector
     */
    public Vector getMajorAxis() {
        if (isEmpty()) {
            return null;
        } else if (isCircle()) {
            return Vector.right();
        } else {
            Vector v = Vector.subtract(_f2, _f1);
            v.normalize();
            return v;
        }
    }

    /**
     * Computes a normalized vector that represents the minor axis. Returns null
     * for empty ellipses, and a vector that is orthogonal to getMajorAxis() for
     * circles.
     *
     * @return Normalized vector
     */
    public Vector getMinorAxis() {
        if (isEmpty()) {
            return null;
        } else if (isCircle()) {
            return Vector.up();
        } else {
            Vector v = Vector.subtract(_f2, _f1);
            v.normalize();
            v.rotate90DegreesCounterclockwise();
            return v;
        }
    }

    /**
     * Computes the length of the major semi-axis. That is, it is half of the
     * longest extent of the ellipse.
     *
     * @return Length of the major semi-axis
     */
    public double getMajorAxisLength() {
        return _d / 2.0;
    }

    /**
     * Computes the length of the minor semi-axis. That is, it is half of the
     * smallest extent of the ellipse.
     *
     * @return Length of the minor semi-axis
     */
    public double getMinorAxisLength() {
        double hfd = _f1.distanceTo(_f2) / 2.0;
        double hd = _d / 2.0;
        return Math.sqrt(hd * hd - hfd * hfd);
    }

    @Override
    public double areaSigned() {
        return Math.PI * getMajorAxisLength() * getMinorAxisLength();
    }

    @Override
    public double perimeter() {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        return DoubleUtil.close(point.distanceTo(_f1) + point.distanceTo(_f2), _d, prec);
    }

    @Override
    public double distanceTo(Vector point) {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public boolean contains(Vector point, double prec) {
        return point.distanceTo(_f1) + point.distanceTo(_f2) <= _d + prec;
    }

    @Override
    public Vector closestPoint(Vector point) {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public void intersectInterior(BaseGeometry other, double prec, List<BaseGeometry> intersections) {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public Vector arbitraryPoint() {
        throw new UnsupportedOperationException("NYI");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void translate(double deltaX, double deltaY) {
        _f1.translate(deltaX, deltaY);
        _f2.translate(deltaX, deltaY);
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        _f1.rotate(counterclockwiseangle);
        _f2.rotate(counterclockwiseangle);
    }

    @Override
    public void scale(double factorX, double factorY) {
        assert factorX == factorY : "Cannot perform nonuniform scaling on an ellipse.";

        _f1.scale(factorX);
        _f2.scale(factorX);
        _d *= factorX;
    }

    @Override
    public void reverse() {
        // no effect
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.ELLIPSE;
    }

    @Override
    public Ellipse clone() {
        return new Ellipse(_f1.clone(), _f2.clone(), _d);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _f1 + "," + _f2 + "," + _d + "]";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE METHODS">
    //</editor-fold>
}
