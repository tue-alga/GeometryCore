/*
 * GeometryCore library   
 * Copyright (C) 2022   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.curved;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * A parabolic arc, defined by a focus point and a finite base segment (which
 * the directrix spans). The focus point is assumed to not lie on the directrix.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ParabolicSegment extends ParameterizedCurve<ParabolicSegment> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private LineSegment _base;
    private Vector _focus;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs a parabolic segment for the given base and focus. Note that
     * these objects are stored directly, so changing the given objects will
     * change the parabolic arc.
     *
     * @param base the base line
     * @param focus the focus point
     */
    public ParabolicSegment(LineSegment base, Vector focus) {
        _base = base;
        _focus = focus;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public LineSegment getBase() {
        return _base;
    }

    public void setBase(LineSegment base) {
        _base = base;
    }

    public Vector getFocus() {
        return _focus;
    }

    public void setFocus(Vector focus) {
        _focus = focus;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Constructs the quadratic Bézier curve that precisely describes this
     * parabolic segment
     *
     * @return the matching Bézier curve
     */
    public BezierCurve toQuadraticBezier() {

        Vector start = getStart();
        Vector end = getEnd();

        if (start == null || end == null) {
            return null;
        }

        if (_base.getStart().isApproximately(_base.getEnd())) {
            return null;
        }

        Line bisec_start = Line.bisector(_base.getStart(), _focus);
        Line bisec_end = Line.bisector(_base.getEnd(), _focus);

        List<BaseGeometry> is = bisec_start.intersect(bisec_end);
        if (is.isEmpty()) {
            return null;
        }
        Vector cp = (Vector) is.get(0);

        if (cp == null) {
            return new BezierCurve(start, end);
        } else {
            return new BezierCurve(start, cp, end);
        }
    }

    /**
     * Retrieves the point on the parabola for a point v on the base line. Note
     * that v is not tested to be on the base line. If v does not lie on the
     * line, the result is a point on the parabola defined by the line parallel
     * to the base that contains v.
     *
     * The result is null if v lies on the line parallel to the baseline through
     * the focus point.
     *
     * @param v the base point
     * @return the point on the parabola for the given base point
     */
    public Vector getCurvePointAtBasePoint(Vector v) {
        if (v.isApproximately(_focus)) {
            return null;
        }

        Line perp = Line.perpendicularAt(v, _base.getDirection());
        Line bisec = Line.bisector(v, _focus);

        List<BaseGeometry> is = perp.intersect(bisec);
        if (is.isEmpty()) {
            return null;
        } else {
            return (Vector) is.get(0);
        }
    }

    /**
     * Retrieves the point on the baseline for a point v on curve. Note that v
     * is not tested to be on the curve. Effectively, it projects the point onto
     * the baseline.
     *
     * @param v the base point
     * @return the point on the baseline for the given curve point
     */
    public Vector getBasePointAtCurvePoint(Vector v) {
        return _base.closestPoint(v);
    }

    /**
     * Retrieves the direction of the tangent at the given curve point. Note
     * that v is not tested to be an actual curve point.
     *
     * @param v the curve point
     * @return the tangent direction for curve point
     */
    public Vector getTangentAtCurvePoint(Vector v) {
        return getTangentAtBasePoint(getBasePointAtCurvePoint(v));
    }

    /**
     * Retrieves the direction of the tangent at the curve point defined by the
     * base point. Note that v is not tested to be an actual base point.
     *
     * @param v the base point
     * @return the tangent direction for the implied curve point
     */
    public Vector getTangentAtBasePoint(Vector v) {

        Vector dir = Vector.subtract(_focus, v);
        dir.normalize();
        if (Vector.dotProduct(_base.getDirection(), dir) < 0) {
            dir.invert();
        }
        return dir;
    }

    @Override
    public Vector getStart() {
        return getCurvePointAtBasePoint(_base.getStart());
    }

    @Override
    public Vector getEnd() {
        return getCurvePointAtBasePoint(_base.getEnd());
    }

    @Override
    public Vector getStartTangent() {
        return getTangentAtBasePoint(_base.getStart());
    }

    @Override
    public Vector getEndTangent() {
        return getTangentAtBasePoint(_base.getEnd());
    }

    @Override
    public double areaSigned() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double perimeter() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector getPointAt(double fraction) {
        return getCurvePointAtBasePoint(_base.getPointAt(fraction));
    }

    @Override
    public void reverse() {
        _base.reverse();
    }

    @Override
    public void updateEndpoints(Vector start, Vector end) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void intersect(BaseGeometry other, double prec, List<BaseGeometry> intersections) {

        int presize = intersections.size();
        ParabolicLine.spannedBy(this).intersect(other, prec, intersections);

        for (int i = intersections.size() - 1; i >= presize; i--) {
            BaseGeometry intgeom = intersections.get(i);

            switch (intgeom.getGeometryType()) {
                case VECTOR: {
                    Vector dir = Vector.subtract(_base.getEnd(), _base.getStart());
                    double len = dir.normalize();

                    double dotp = Vector.dotProduct(dir, Vector.subtract((Vector) intgeom, _base.getStart()));
                    if (!DoubleUtil.inClosedInterval(dotp, 0, len)) {
                        intersections.remove(i);
                    }
                    break;
                }
                case PARABOLICLINE: {
                    intersections.set(i, clone());
                    break;
                }
                case PARABOLICHALFLINE: {
                    ParabolicHalfLine intparhalfline = (ParabolicHalfLine) intgeom;

                    boolean startinhalfspace = intparhalfline.getBase().inHalfSpace(_base.getStart());
                    boolean endinhalfspace = intparhalfline.getBase().inHalfSpace(_base.getEnd());

                    if (startinhalfspace && endinhalfspace) {
                        intersections.set(i, clone());
                    } else if (startinhalfspace) {
                        if (_base.getStart().isApproximately(intparhalfline.getBase().getOrigin(), prec)) {
                            intersections.set(i, intparhalfline.getBase().getOrigin());
                        } else {
                            intersections.set(i, new ParabolicSegment(new LineSegment(_base.getStart(), intparhalfline.getBase().getOrigin()), _focus.clone()));
                        }
                    } else if (endinhalfspace) {
                        if (_base.getEnd().isApproximately(intparhalfline.getBase().getOrigin(), prec)) {
                            intersections.set(i, intparhalfline.getBase().getOrigin());
                        } else {
                            intersections.set(i, new ParabolicSegment(new LineSegment(_base.getEnd(), intparhalfline.getBase().getOrigin()), _focus.clone()));
                        }
                    } else {
                        intersections.remove(i);
                    }
                    break;
                }
                case PARABOLICSEGMENT: {

                    ParabolicSegment intsegment = (ParabolicSegment) intgeom;

                    if (Vector.dotProduct(_base.getDirection(), intsegment._base.getDirection()) < 0) {
                        intsegment.reverse();
                    }

                    // five outcomes
                    // 1: this contains intsegment
                    // 2: intsegment contains this
                    // 3: start of this overlaps
                    // 4: end of this overlap
                    // 5: no overlap
                    if (_base.inSlab(intsegment._base.getStart())) {
                        if (_base.inSlab(intsegment._base.getEnd())) {
                            // case 1:
                            // nothing to do
                        } else {
                            // case 4:
                            if (_base.getEnd().isApproximately(intsegment._base.getStart(), prec)) {
                                intersections.set(i, intsegment.getStart());
                            } else {
                                intsegment._base.getEnd().set(_base.getEnd());
                            }
                        }
                    } else if (_base.inSlab(intsegment._base.getEnd())) {
                        // case 3:                    
                        if (_base.getStart().isApproximately(intsegment._base.getEnd(), prec)) {
                            intersections.set(i, intsegment.getEnd());
                        } else {
                            intsegment._base.getStart().set(_base.getStart());
                        }
                    } else if (intsegment._base.inSlab(_base.getStart())) {
                        // case 2:
                        intsegment._base.getStart().set(_base.getStart());
                        intsegment._base.getEnd().set(_base.getEnd());
                    } else {
                        // case 5:
                        intersections.remove(i);
                    }
                    break;
                }
                default:
                    Logger.getLogger(LineSegment.class.getName()).log(Level.SEVERE,
                            "Unexpected geometry type in parabola intersection: {0}",
                            intgeom.getGeometryType());
                    break;
            }
        }
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        if (_focus.isApproximateInverse(point, prec)) {
            return false;
        }
        Vector basept = getBasePointAtCurvePoint(point);
        return _base.onBoundary(basept, prec) && point.isApproximately(getCurvePointAtBasePoint(basept), prec);
    }

    @Override
    public Vector closestPoint(Vector point) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void translate(double deltaX, double deltaY) {
        _base.translate(deltaX, deltaY);
        _focus.translate(deltaX, deltaY);
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        _base.rotate(counterclockwiseangle);
        _focus.rotate(counterclockwiseangle);
    }

    @Override
    public void scale(double factorX, double factorY) {
        _base.scale(factorX, factorY);
        _focus.scale(factorX, factorY);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PARABOLICSEGMENT;
    }

    @Override
    public ParabolicSegment clone() {
        return new ParabolicSegment(_base.clone(), _focus.clone());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _focus + "," + _base + "]";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    /**
     * Constructs the control point for the Bézier curve. The result is null if
     * the start and end point are approximately equal.
     *
     * @return the control point.
     */
    private Vector getBezierControlPoint() {

        if (_base.getStart().isApproximately(_base.getEnd())) {
            return null;
        }

        Line bisec_start = Line.bisector(_base.getStart(), _focus);
        Line bisec_end = Line.bisector(_base.getEnd(), _focus);

        List<BaseGeometry> is = bisec_start.intersect(bisec_end);
        if (is.isEmpty()) {
            return null;
        } else {
            return (Vector) is.get(0);
        }
    }
    //</editor-fold>
}
