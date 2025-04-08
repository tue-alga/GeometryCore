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
import nl.tue.geometrycore.geometry.InfiniteGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.HalfLine;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 * A parabolic arc, defined by a focus point and a base halfline (which the
 * directrix spans). base), The focus point is assumed to not lie on the
 * directrix.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ParabolicHalfLine extends BaseGeometry<ParabolicHalfLine> implements InfiniteGeometry<ParabolicSegment> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private HalfLine _base;
    private Vector _focus;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs a parabolic halfline for the given base and focus. Note that
     * these objects are stored directly, so changing the given objects will
     * change the parabolic arc.
     *
     * @param base the base line
     * @param focus the focus point
     */
    public ParabolicHalfLine(HalfLine base, Vector focus) {
        _base = base;
        _focus = focus;
    }

    /**
     * Constructs the parabolic halfline that extends the given parabolic
     * segment in the implied direction of its base.
     *
     * @param ps the segment to be extended
     * @return the extended parabolic halfline
     */
    public static ParabolicHalfLine spannedBy(ParabolicSegment ps) {
        return new ParabolicHalfLine(HalfLine.spannedBy(ps.getBase()), ps.getFocus());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public HalfLine getBase() {
        return _base;
    }

    public void setBase(HalfLine base) {
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

    /**
     * Gets the curve point where the parabolic arc starts
     *
     * @return the starting point
     */
    public Vector getStart() {
        return getCurvePointAtBasePoint(_base.getOrigin());
    }

    /**
     * Gets the target at the starting point
     *
     * @return the tangent direction
     */
    public Vector getStartTangent() {
        return getTangentAtBasePoint(_base.getOrigin());
    }

    @Override
    public void intersect(BaseGeometry other, double prec, List<BaseGeometry> intersections) {

        int presize = intersections.size();
        ParabolicLine.spannedBy(this).intersect(other, prec, intersections);

        for (int i = intersections.size() - 1; i >= presize; i--) {
            BaseGeometry intgeom = intersections.get(i);

            switch (intgeom.getGeometryType()) {
                case VECTOR: {
                    boolean inhalfspace = _base.inHalfSpace((Vector) intgeom, prec);
                    if (!inhalfspace) {
                        intersections.remove(i);
                    }
                    break;
                }
                case PARABOLICLINE: {
                    // NB: can only occur on similar bases
                    intersections.set(i, clone());
                    break;
                }
                case PARABOLICHALFLINE: {
                    // NB: can only occur on similar bases
                    ParabolicHalfLine intparhalfline = (ParabolicHalfLine) intgeom;

                    boolean inhalfspace = _base.inHalfSpace(intparhalfline._base.getOrigin(), prec);

                    // NB: is either -1 or 1
                    if (Vector.dotProduct(_base.getDirection(), intparhalfline._base.getDirection()) < 0) {
                        // oppositely directed
                        if (inhalfspace) {
                            if (_base.getOrigin().isApproximately(intparhalfline._base.getOrigin(), prec)) {
                                intersections.set(i, getStart());
                            } else {
                                intersections.set(i, new ParabolicSegment(new LineSegment(intparhalfline._base.getOrigin(), _base.getOrigin().clone()), _focus.clone()));
                            }
                        } else {
                            intersections.remove(i);
                        }
                    } else {
                        // same direction
                        if (inhalfspace) {
                            // nothing needs to be done
                        } else {
                            intparhalfline._base.setOrigin(_base.getOrigin().clone());
                        }
                    }
                    break;
                }
                case PARABOLICSEGMENT: {
                    // NB: can only occur on similar bases
                    ParabolicSegment intsegment = (ParabolicSegment) intgeom;

                    boolean startinhalfspace = _base.inHalfSpace(intsegment.getStart());
                    boolean endinhalfspace = _base.inHalfSpace(intsegment.getEnd());

                    if (startinhalfspace && endinhalfspace) {
                        // nothing needs to be done
                    } else if (startinhalfspace) {
                        if (intsegment.getStart().isApproximately(_base.getOrigin(), prec)) {
                            intersections.set(i, intsegment.getStart());
                        } else {
                            intsegment.getEnd().set(_base.getOrigin());
                        }
                    } else if (endinhalfspace) {
                        if (intsegment.getEnd().isApproximately(_base.getOrigin(), prec)) {
                            intersections.set(i, intsegment.getEnd());
                        } else {
                            intsegment.getStart().set(_base.getOrigin());
                        }
                    } else {
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

    @Override
    public ParabolicSegment clip(Rectangle clipbox) {
        List<BaseGeometry> vs = clipbox.intersect(this);

        if (vs.size() < 1) {
            return null;
        }

        // find the first and last
        Vector dir = _base.getDirection();
        Vector first = null;
        double first_pos = Double.POSITIVE_INFINITY;
        Vector last = null;
        double last_pos = Double.NEGATIVE_INFINITY;
        for (BaseGeometry vg : vs) {
            // must be vector
            Vector v = (Vector) vg;

            double pos = Vector.dotProduct(dir, v);

            if (pos < first_pos) {
                first = v;
                first_pos = pos;
            }

            if (pos > last_pos) {
                last = v;
                last_pos = pos;
            }
        }

        if (clipbox.contains(getStart())) {
            return new ParabolicSegment(new LineSegment(_base.getOrigin(), getBasePointAtCurvePoint(last)), _focus);

        } else {
            return new ParabolicSegment(new LineSegment(getBasePointAtCurvePoint(first), getBasePointAtCurvePoint(last)), _focus);
        }
    }

    @Override
    public Vector arbitraryPoint() {
        throw new UnsupportedOperationException("NYI");
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
        return GeometryType.PARABOLICHALFLINE;
    }

    @Override
    public ParabolicHalfLine clone() {
        return new ParabolicHalfLine(_base.clone(), _focus.clone());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _focus + "," + _base + "]";
    }
    //</editor-fold>

}
