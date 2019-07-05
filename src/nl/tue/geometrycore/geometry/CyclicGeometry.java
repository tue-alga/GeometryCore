/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * Base class for a non-point geometry without a start and end point.
 *
 * @param <TActual> the actual type of this geometry
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class CyclicGeometry<TActual extends CyclicGeometry> extends BaseGeometry<TActual> {

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Computes the total boundary length of the given geometry.
     *
     * @return perimeter length
     */
    public abstract double perimeter();

    /**
     * Computes the total area enclosed by the geometry. Should always be
     * nonnegative.
     *
     * @return area
     */
    public double areaUnsigned() {
        return Math.abs(areaSigned());
    }

    /**
     * Computes the total signed area enclosed by the geometry. Should be
     * positive for counterclockwise shapes and negative for clockwise shapes.
     *
     * @return signed area
     */
    public abstract double areaSigned();

    /**
     * Computes whether the provided point lies inside or on the boundary, with
     * a precision of DoubleUtil.EPS.
     *
     * @param point location to check containment for
     * @return whether the point lies inside or on the boundary
     */
    public boolean contains(Vector point) {
        return contains(point, DoubleUtil.EPS);
    }

    /**
     * Computes whether the provided point lies inside or on the boundary, with
     * a given precision. A negative precision allows to check whether the point
     * lies strictly inside, that is, not on the boundary.
     *
     * @param point location to check containment for
     * @param prec precision
     * @return whether the point lies inside or on the boundary
     */
    public abstract boolean contains(Vector point, double prec);
    
    /**
     * Compute the intersections between this and the given geometry, using the
     * standard precision of DoubleUtil.EPS. Note that the result must be
     * independent from the input geometry.
     *
     * @param other geometry with which intersection should be computed
     * @return list of geometries describing the intersection
     */
    public List<BaseGeometry> intersectInterior(BaseGeometry other) {
        return intersectInterior(other, DoubleUtil.EPS);
    }

    /**
     * Compute the intersections between this and the given geometry, using a
     * specified precision. Note that the result must be independent from the
     * input geometry.
     *
     * @param other geometry with which intersection should be computed
     * @param prec precision of the computation. A negative precision excludes the boundary.
     * @return list of geometries describing the intersection
     */
    public List<BaseGeometry> intersectInterior(BaseGeometry other, double prec) {
        List<BaseGeometry> list = new ArrayList();
        intersectInterior(other, prec, list);
        return list;
    }

    /**
     * Compute the intersections between this and the given geometry, using the
     * standard precision of DoubleUtil.EPS. Note that the result must be
     * independent from the input geometry.
     *
     * @param other geometry with which intersection should be computed
     * @param intersections list in which to store the intersection
     */
    public void intersectInterior(BaseGeometry other, List<BaseGeometry> intersections) {
        intersectInterior(other, DoubleUtil.EPS, intersections);
    }

    /**
     * Compute the intersections between this and the given geometry, using a
     * specified precision. Note that the result must be independent from the
     * input geometry.
     *
     * @param other geometry with which intersection should be computed
     * @param prec precision of the computation. A negative precision excludes the boundary.
     * @param intersections list in which to store the intersection
     */
    public abstract void intersectInterior(BaseGeometry other, double prec, List<BaseGeometry> intersections);
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    /**
     * Reverse this geometry from start to end, without changing its appearance.
     */
    public abstract void reverse();
    //</editor-fold>
}
