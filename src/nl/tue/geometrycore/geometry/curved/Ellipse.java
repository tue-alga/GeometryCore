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
import nl.tue.geometrycore.util.Pair;

/**
 * A simple ellipse. Note that, although it inherits from CyclicGeometry, it has
 * no clear orientation. Thus, the computed area will always be positive.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Ellipse extends CyclicGeometry<Ellipse> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Vector _center;
    private Vector _axis;
    private double _width, _height;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Basic constructor of an ellipse, by its two focal points and radius. Note
     * that the order of the focal points does not matter.
     *
     * @param center
     * @param axis Assumed to be normalized
     * @param width
     * @param height
     */
    public Ellipse(Vector center, Vector axis, double width, double height) {
        _center = center;
        _axis = axis;
        _width = width;
        _height = height;
    }

    public static Ellipse fromCircle(Circle c) {
        double r = c.getRadius();
        return new Ellipse(c.getCenter(), Vector.right(), r, r);
    }

    /**
     * Constructs an ellipse given two focal points and a distance d. The
     * ellipse is the locus of points for which the distance to f1 plus the
     * distance to f2 adds up to d.
     *
     * @param f1
     * @param f2
     * @param d
     * @return
     */
    public static Ellipse fromFocalPoints(Vector f1, Vector f2, double d) {
        Vector c = Vector.add(f1,f2);
        c.scale(0.5);
        Vector dir = Vector.subtract(f2,f1);
        double dist = f1.distanceTo(f2);
        dir.scale(1.0/dist);            
        return new Ellipse(c,dir,d, 2 * Math.sqrt(0.25 * (d * d - dist * dist)));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public Vector getCenter() {
        return _center;
    }

    public void setCenter(Vector center) {
        _center = center;
    }

    public Vector getAxis() {
        return _axis;
    }

    /**
     * NB: axis is assumed to be normalized.
     *
     * @param axis
     */
    public void setAxis(Vector axis) {
        _axis = axis;
    }

    public double getWidth() {
        return _width;
    }

    public void setWidth(double width) {
        _width = width;
    }

    public double getHeight() {
        return _height;
    }

    public void setHeight(double height) {
        _height = height;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    @Override
    public double areaSigned() {
        return Math.PI * _width * _height / 4.0;
    }

    @Override
    public double perimeter() {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public double distanceTo(Vector point) {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public boolean contains(Vector point, double prec) {
        throw new UnsupportedOperationException("NYI");
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
        throw new UnsupportedOperationException("Interior intersection not yet implemented for Ellipse");
    }

    public double getMajorLength() {
        return Math.max(_width, _height) / 2.0;
    }

    public double getMinorLength() {
        return Math.min(_width, _height) / 2.0;
    }

    public Vector getMajorAxis() {
        if (_width >= _height) {
            return _axis;
        } else {
            Vector v = _axis.clone();
            v.rotate90DegreesCounterclockwise();
            return v;
        }
    }

    public Vector getMinorAxis() {
        if (_width < _height) {
            return _axis;
        } else {
            Vector v = _axis.clone();
            v.rotate90DegreesCounterclockwise();
            return v;
        }
    }

    public double getFocalDistance() {
        return Math.max(_width, _height);
    }

    public Pair<Vector, Vector> getFocalPoints() {
        double a = getMajorLength();
        double b = getMajorLength();
        double c = Math.sqrt(a * a - b * b);

        Vector axis = getMajorAxis().clone();
        axis.scale(c);
        return new Pair(Vector.add(_center, axis), Vector.subtract(_center, axis));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void translate(double deltaX, double deltaY) {
        _center.translate(deltaX, deltaY);
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        _center.rotate(counterclockwiseangle);
        _axis.rotate(counterclockwiseangle);
    }

    @Override
    public void scale(double factorX, double factorY) {
        assert factorX == factorY : "Cannot perform nonuniform scaling on a circle.";

        _center.scale(factorX, factorX);
        _width *= factorX;
        _width *= factorY;
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
        return new Ellipse(_center.clone(), _axis.clone(), _width, _height);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _center + "," + _axis + "," + _width + "," + _height + "]";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE METHODS">
    //</editor-fold>
}
