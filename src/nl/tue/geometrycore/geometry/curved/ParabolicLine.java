/*
 * GeometryCore library   
 * Copyright (C) 2022   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.curved;

import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.InfiniteGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.HalfLine;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * A parabolic arc, defined by a focus point and a base line (the
 * directrix). The focus point is assumed to not lie on the directrix.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ParabolicLine extends BaseGeometry<ParabolicLine> implements InfiniteGeometry<ParabolicSegment> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Line _base;
    private Vector _focus;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs a parabolic line for the given base and focus. Note that
     * these objects are stored directly, so changing the given objects will
     * change the parabolic arc.
     *
     * @param base the base line
     * @param focus the focus point
     */
    public ParabolicLine(Line base, Vector focus) {
        _base = base;
        _focus = focus;
    }

    /**
     * Constructs the parabolic line that extends the given parabolic
     * halfline in the implied direction of its base.
     *
     * @param phl the halfline to be extended
     * @return the extended parabolic line
     */
    public static ParabolicLine spannedBy(ParabolicHalfLine phl) {
        return new ParabolicLine(Line.spannedBy(phl.getBase()), phl.getFocus().clone());
    }

    /**
     * Constructs the parabolic line that extends the given parabolic
     * segment in the implied direction of its base.
     *
     * @param ps the segment to be extended
     * @return the extended parabolic line
     */
    public static ParabolicLine spannedBy(ParabolicSegment ps) {
        return new ParabolicLine(Line.spannedBy(ps.getBase()), ps.getFocus().clone());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public Line getBase() {
        return _base;
    }

    public void setBase(Line base) {
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

    @Override
    public void intersect(BaseGeometry other, double prec, List<BaseGeometry> intersections) {

        int presize = intersections.size();

        switch (other.getGeometryType()) {
            case HALFLINE: {
                HalfLine hl = (HalfLine) other;
                intersect(Line.spannedBy(hl), prec, intersections);
                for (int i = intersections.size() - 1; i >= presize; i--) {
                    // intersection must be a vector
                    Vector v = (Vector) intersections.get(i);
                    if (!hl.onBoundary(v, prec)) {
                        intersections.remove(i);
                    }
                }
                return;
            }
            case LINESEGMENT: {
                LineSegment ls = (LineSegment) other;
                intersect(Line.spannedBy(ls), prec, intersections);
                for (int i = intersections.size() - 1; i >= presize; i--) {
                    // intersection must be a vector
                    Vector v = (Vector) intersections.get(i);
                    if (!ls.onBoundary(v, prec)) {
                        intersections.remove(i);
                    }
                }
                return;
            }
            case PARABOLICHALFLINE:
            case PARABOLICSEGMENT: {
                other.intersect(this, prec, intersections);
                return;
            }
            case LINE: {
                // handled below
                break;
            }
            case PARABOLICLINE: {
                // these we handle below, unless it is exactly the same parabola
                ParabolicLine pl = (ParabolicLine) other;
                boolean samefocus = _focus.isApproximately(pl._focus, prec);
                boolean sameorientation = _base.getDirection().isApproximately(pl._base.getDirection(), prec) || _base.getDirection().isApproximateInverse(pl._base.getDirection(), prec);
                boolean sameintercept = _base.onBoundary(pl._base.getThrough(), prec);
                if (samefocus && sameorientation && sameintercept) {
                    // the same 
                    intersections.add(this.clone());
                    return;
                } else {
                    // not the same
                    break;
                }
            }
            default: {
                // otherwise, flip and handle there (and stop)
                other.intersect(this, prec, intersections);
                return;
            }
        }

        // rotate all this parabola such that its base is directed to the right
        boolean rotate = !_base.getDirection().isApproximately(Vector.right());
        double angle;
        ParabolicLine rot_this;
        BaseGeometry rot_other;
        if (rotate) {
            angle = _base.getDirection().computeClockwiseAngleTo(Vector.right(), false, false);

            rot_this = this.clone();
            rot_this.rotate(-angle);

            rot_other = other.clone();
            rot_other.rotate(-angle);
        } else {
            rot_this = this;
            rot_other = other;
            angle = 0;
        }

        // express this parabola as a quadratic equation
        // p : y = pa x^2 + pb x + pc
        Vector pt = rot_this._focus;
        Vector ptdir = Vector.subtract(pt, rot_this._base.getThrough());
        double f = 0.5 * Vector.dotProduct(ptdir, Vector.up());
        Vector m = Vector.add(pt, Vector.down(f));

        double pa = 1.0 / (4.0 * f);
        double pb = -m.getX() / (2.0 * f);
        double pc = m.getX() * m.getX() / (4.0 * f) + m.getY();

        switch (other.getGeometryType()) {
            case LINE: {
                Line l = (Line) rot_other;
                Vector l_dir = l.getDirection();
                if (DoubleUtil.close(l_dir.getX(), 0, prec)) {
                    double x = l.getThrough().getX();
                    intersections.add(new Vector(x, pa * x * x + pb * x + pc));
                } else {
                    // express the line as a linear equation
                    // q : y = s x + i
                    double s = l_dir.getY() / l_dir.getX();
                    // ty = s tx + i
                    // i = ty - s tx
                    double i = l.getThrough().getY() - s * l.getThrough().getX();
                    // so we solve s x + i = pa x^2 + pb x + pc
                    // which is finding the roots of pa x^2 + (pb - s) x + (pc - i)
                    double[] xs = DoubleUtil.solveQuadraticEquation(pa, pb - s, pc - i);
                    for (double x : xs) {
                        intersections.add(new Vector(x, pa * x * x + pb * x + pc));
                    }
                }
                break;
            }
            case PARABOLICLINE: {
                ParabolicLine pl = (ParabolicLine) rot_other;

                if (DoubleUtil.close(pl._base.getDirection().getY(), 0, prec)) {
                    // easy case of same rotation

                    // q : y = qa x^2 + qb x + qc
                    Vector qt = pl._focus;
                    Vector qtdir = Vector.subtract(qt, pl._base.getThrough());
                    double qf = 0.5 * Vector.dotProduct(qtdir, Vector.up());
                    Vector qm = Vector.add(qt, Vector.down(qf));

                    double qa = 1.0 / (4.0 * qf);
                    double qb = -qm.getX() / (2.0 * qf);
                    double qc = qm.getX() * qm.getX() / (4.0 * qf) + qm.getY();

                    // solve p = q
                    //   pa x^2 + pb x + pc = qa x^2 + qb x + qc        
                    // so find roots of (pa-qa) x^2 + (pb-qb) x + (pc-qc)
                    double[] xs = DoubleUtil.solveQuadraticEquation(pa - qa, pb - qb, pc - qc);
                    for (double x : xs) {
                        intersections.add(new Vector(x, pa * x * x + pb * x + pc));
                    }

                } else {
                    // complex case of different rotations

                    // q : (qa * x + qb * y + qc)^2/(qa^2 + qb^2) == (x - qfx)^2 + (y - qfy)^2 
                    Vector q_dir = pl._base.getDirection().clone();
                    q_dir.rotate90DegreesCounterclockwise();
                    double qa = q_dir.getX();
                    double qb = q_dir.getY();
                    double qc = -Vector.dotProduct(q_dir, pl._base.getThrough());
                    double qfx = pl._focus.getX();
                    double qfy = pl._focus.getY();

                    // substitute p into q and find roots of
                    // (qa * x + qb * (pa x^2 + pb x + pc) + qc)^2/(qa^2 + qb^2) - (x - qfx)^2 + ((pa x^2 + pb x + pc) - qfy)^2 
                    // substitute
                    double PA = Math.pow(pa, 2);
                    double PB = Math.pow(pb, 2);
                    double PC = Math.pow(pc, 2);
                    double QA = Math.pow(qa, 2);
                    double QB = Math.pow(qb, 2);
                    double QC = Math.pow(qc, 2);
                    double QFX = Math.pow(qfx, 2);
                    double QFY = Math.pow(qfy, 2);

                    double e = -PC + (PC * QB) / (QA + QB)
                            + (2 * pc * qb * qc) / (QA + QB) + QC / (QA + QB) - QFX
                            + 2 * pc * qfy - QFY;

                    double d = -2 * pb * pc + (2 * pc * qa * qb) / (QA + QB)
                            + (2 * pb * pc * QB) / (QA + QB) + (2 * qa * qc) / (QA + QB)
                            + (2 * pb * qb * qc) / (QA + QB) + 2 * qfx + 2 * pb * qfy;

                    double c = -1 - PB - 2 * pa * pc + QA / (QA + QB)
                            + (2 * pb * qa * qb) / (QA + QB) + (PB * QB) / (QA + QB)
                            + (2 * pa * pc * QB) / (QA + QB) + (2 * pa * qb * qc) / (QA + QB)
                            + 2 * pa * qfy;

                    double b = -2 * pa * pb + (2 * pa * qa * qb) / (QA + QB)
                            + (2 * pa * pb * QB) / (QA + QB);

                    double a = -PA + (PA * QB) / (QA + QB);

                    double[] res = DoubleUtil.solveQuarticEquationReal(a, b, c, d, e);
                    for (double x : res) {
                        intersections.add(new Vector(x, pa * x * x + pb * x + pc));
                    }
                }
                break;
            }
        }

        // rotate back for all new things found
        if (rotate) {
            for (int i = presize; i < intersections.size(); i++) {
                intersections.get(i).rotate(angle);
            }
        }
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        if (_focus.isApproximateInverse(point, prec)) {
            return false;
        }
        Vector basept = getBasePointAtCurvePoint(point);
        return point.isApproximately(getCurvePointAtBasePoint(basept), prec);
    }

    @Override
    public Vector closestPoint(Vector point) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ParabolicSegment clip(Rectangle clipbox) {

        List<BaseGeometry> vs = clipbox.intersect(this);

        if (vs.size() <= 1) {
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

        return new ParabolicSegment(new LineSegment(getBasePointAtCurvePoint(first), getBasePointAtCurvePoint(last)), _focus);
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
        return GeometryType.PARABOLICLINE;
    }

    @Override
    public ParabolicLine clone() {
        return new ParabolicLine(_base.clone(), _focus.clone());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _focus + "," + _base + "]";
    }
    //</editor-fold>

}
