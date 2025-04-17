/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * Class to represent a single (typically simple) polygon as a sequence of
 * vertices. Polygon cannot contain holes.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Polygon extends CyclicGeometry<Polygon> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final List<Vector> _vertices;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs an empty polygon.
     */
    public Polygon() {
        _vertices = new ArrayList();
    }

    /**
     * Constructs a polygon from the provided vertices.
     *
     * @param vertices corner points of the polygon
     */
    public Polygon(Vector... vertices) {
        this();
        _vertices.addAll(Arrays.asList(vertices));
    }

    /**
     * Constructs a polygon from the provided vertices.
     *
     * @param vertices corner points of the polygon
     */
    public Polygon(List<Vector> vertices) {
        _vertices = vertices;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">    
    /**
     * Returns the number of vertices defining this polygonal line.
     *
     * @return number of vertices
     */
    public int vertexCount() {
        return _vertices.size();
    }

    /**
     * Returns the vertex at the specified index. Note that the index is treated
     * circularly.
     *
     * @param index position of the desired vertex
     * @return vertex at given index
     */
    public Vector vertex(int index) {
        return _vertices.get(index(index));
    }

    /**
     * Returns the number of edges of this polygon. This is equal to the number
     * of vertices.
     *
     * @return edge count
     */
    public int edgeCount() {
        return _vertices.size();
    }

    /**
     * Constructs the edge between vertex at the specified index and the index
     * after. Value of index must lie in between 0 and the number of edges. Note
     * that the index is treated circularly.
     *
     * @param index edge index
     * @return newly constructed edge
     */
    public LineSegment edge(int index) {
        return new LineSegment(vertex(index), vertex(index + 1));
    }

    @Override
    public Vector closestPoint(Vector point) {
        Vector result = null;
        double distance = Double.POSITIVE_INFINITY;
        for (LineSegment edge : edges()) {
            Vector closest = edge.closestPoint(point);
            double d = closest.squaredDistanceTo(point);
            if (d < distance) {
                result = closest;
                distance = d;
            }
        }
        return result;
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        for (LineSegment edge : edges()) {
            if (edge.onBoundary(point, prec)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double areaSigned() {
        double total = 0;
        Vector prev = vertex(-1);
        for (int i = 0; i < _vertices.size(); i++) {
            Vector curr = _vertices.get(i);

            total += Vector.crossProduct(prev, curr);

            prev = curr;
        }
        return total / 2.0;
    }

    @Override
    public double perimeter() {
        double total = 0;
        final int n = _vertices.size();
        if (n > 1) {
            Vector prev = _vertices.get(_vertices.size() - 1);
            for (int i = 0; i < n; i++) {
                Vector next = _vertices.get(i);
                total += prev.distanceTo(next);

                prev = next;
            }
        }
        return total;
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
        int presize = intersections.size();
        for (LineSegment edge : edges()) {
            edge.intersect(otherGeom, prec, intersections);
        }
        for (int i = intersections.size() - 1; i >= presize; i--) {
            if (intersections.get(i).getGeometryType() == GeometryType.VECTOR) {
                Vector point = (Vector) intersections.get(i);
                for (int j = intersections.size() - 1; j >= presize; j--) {
                    if (i != j && intersections.get(j).onBoundary(point, prec)) {
                        intersections.remove(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns the vertex list of this polygon. Note that changes in this list
     * affect the polygon.
     *
     * @return vertex list
     */
    public List<Vector> vertices() {
        return _vertices;
    }

    /**
     * Allows iteration over the edges of the polygon. Removals are not
     * permitted: modify the vertex list instead.
     *
     * @return iterable over the edges
     */
    public Iterable<LineSegment> edges() {
        return new Iterable<LineSegment>() {

            @Override
            public Iterator<LineSegment> iterator() {
                return new Iterator<LineSegment>() {

                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < edgeCount();
                    }

                    @Override
                    public LineSegment next() {
                        return edge(index++);
                    }

                    @Override
                    public void remove() {
                        Logger.getLogger(Polygon.class.getName()).log(Level.SEVERE, "Cannot remove edges from a Polygon. Remove the relevant vertices instead.");
                    }

                };
            }
        };
    }

    @Override
    public boolean contains(Vector point, double prec) {
        // check if the point is on top of an edge
        double absprec = Math.abs(prec);
        for (LineSegment edge : edges()) {
            if (edge.onBoundary(point, absprec)) {
                return prec >= 0;
            }
        }

        double totangle = 0;

        Vector dirPrev = Vector.subtract(vertex(-1), point);
        dirPrev.normalize();

        for (int i = 0; i < _vertices.size(); i++) {
            Vector dirCurr = Vector.subtract(vertex(i), point);
            dirCurr.normalize();
            totangle += dirPrev.computeSignedAngleTo(dirCurr, false, false);
            dirPrev = dirCurr;
        }

        // its either 0 or a multiple of 2 PI
        // 0  -> outside
        // !0 -> inside
        return !(-0.5 < totangle && totangle < 0.5);
    }

    /**
     * Speed-up of {@link #containsPoint} for convex polygons.
     *
     * @param point location to check containment for
     * @return whether the point lies inside or on the boundary
     */
    public boolean convexContainsPoint(Vector point) {
        return convexContainsPoint(point, DoubleUtil.EPS);
    }

    /**
     * Speed-up of {@link #containsPoint} for convex polygons.
     *
     * @param point location to check containment for
     * @param prec precision
     * @return whether the point lies inside or on the boundary
     */
    public boolean convexContainsPoint(Vector point, double prec) {
        double dirPrevX = vertex(-1).getX() - point.getX();
        double dirPrevY = vertex(-1).getY() - point.getY();

        double dirCurrX = vertex(0).getX() - point.getX();
        double dirCurrY = vertex(0).getY() - point.getY();

        double cp = Vector.crossProduct(dirPrevX, dirPrevY, dirCurrX, dirCurrY);
        double sig = Math.signum(cp);

        double absprec = Math.abs(prec);

        if (DoubleUtil.close(cp, 0, absprec)) {
            // either vertex is on line segment (inside if prec positive) or in parallel with it (outside)
            if (Vector.dotProduct(dirPrevX, dirPrevY, dirCurrX, dirCurrY) < absprec) {
                return prec >= 0;
            } else {
                return false;
            }
        } else {
            for (int i = 1; i < _vertices.size(); i++) {
                dirPrevX = dirCurrX;
                dirPrevY = dirCurrY;

                dirCurrX = vertex(i).getX() - point.getX();
                dirCurrY = vertex(i).getY() - point.getY();

                cp = Vector.crossProduct(dirPrevX, dirPrevY, dirCurrX, dirCurrY);
                double s = Math.signum(cp);
                if (DoubleUtil.close(cp, 0, absprec)) {
                    // either vertex is on line segment (inside if prec positive) or in parallel with it (outside)
                    if (Vector.dotProduct(dirPrevX, dirPrevY, dirCurrX, dirCurrY) < absprec) {
                        return prec >= 0;
                    } else {
                        return false;
                    }
                } else if (DoubleUtil.close(sig, s)) {
                    // continue, same sign
                } else {
                    // opposite signs
                    return false;
                }
            }

            return true;
        }
    }

    public boolean convexContains(BaseGeometry geom) {
        return convexContains(geom, DoubleUtil.EPS);
    }

    /**
     * Tests whether this convex polygon fully contains an object. This
     * operation works only for convex polygons, and is designed for polygons of
     * low complexity (e.g. triangles or quadrilaterals).
     *
     * @param geom The geometry that is to be contained
     * @param precision The desired precision
     * @return true iff the rectangle wholly encompasses the given object
     */
    public boolean convexContains(BaseGeometry geom, double precision) {

        switch (geom.getGeometryType()) {
            case VECTOR:
                return convexContainsPoint((Vector) geom, precision);
            case LINESEGMENT: {
                // we know the start point is inside, so just check other end
                LineSegment L = (LineSegment) geom;
                return convexContainsPoint(L.getStart())
                        && convexContainsPoint(L.getEnd());
            }
            case HALFLINE:
            case LINE: {
                // infinite geometries cannot be contained in a finite polygon
                return false;
            }
            case RECTANGLE: {
                Rectangle R = (Rectangle) geom;
                for (Vector v : R.corners()) {
                    if (!convexContainsPoint(v, precision)) {
                        return false;
                    }
                }
                // all four corners inside
                return true;
            }
            case POLYLINE: {
                for (LineSegment ls : ((PolyLine) geom).edges()) {
                    if (!convexContains(ls, precision)) {
                        return false;
                    }
                }
                return true;
            }
            case POLYGON: {
                for (LineSegment ls : ((Polygon) geom).edges()) {
                    if (!convexContains(ls, precision)) {
                        return false;
                    }
                }
                return true;
            }
            case CIRCULARARC: {
                CircularArc A = (CircularArc) geom;
                if (!convexContainsPoint(A.getStart(), precision)
                        || !convexContainsPoint(A.getEnd(), precision)) {
                    return false;
                }
                // both endpoints inside (or on) boundary
                // just need to make sure it doesn't curve out
                if (A.getCenter() == null) {
                    // linesegment...
                    return true;
                }

                // TODO: perhaps this can be done more efficiently...
                // NB: with the polygon being convex & the arc properly curved, 
                // any and all intersections must be points                
                // effectively, we chop it up into arc pieces, and test each midpoint
                List<BaseGeometry> is = A.intersect(this);
                List<Vector> pts = new ArrayList();
                for (BaseGeometry bg : is) {
                    pts.add((Vector) bg);
                }
                pts.add(A.getStart());
                pts.add(A.getEnd());
                boolean ccw = A.isCounterclockwise();
                Vector center = A.getCenter();
                Vector arm = ccw ? A.getStartArm() : A.getEndArm();
                arm.normalize();
                pts.sort((a, b) -> Double.compare(
                        arm.computeCounterClockwiseAngleTo(Vector.subtract(a, center), false, true),
                        arm.computeCounterClockwiseAngleTo(Vector.subtract(b, center), false, true)));
                for (int i = 0; i < pts.size(); i++) {
                    CircularArc prt = new CircularArc(center, pts.get(i), pts.get(i + 1), true);
                    if (!convexContainsPoint(prt.getPointAt(0.5), precision)) {
                        return false;
                    }
                }
                return true;

            }
            case CIRCLE: {
                Circle C = (Circle) geom;
                return convexContainsPoint(C.getCenter(), precision)
                        && closestPoint(C.getCenter()).distanceTo(C.getCenter()) <= C.getRadius() + precision;
            }
            case GEOMETRYSTRING:
                for (BaseGeometry bg : ((GeometryString<?>) geom).edges()) {
                    if (!convexContains(bg, precision)) {
                        return false;
                    }
                }
                return true;
            case GEOMETRYCYCLE:
                for (BaseGeometry bg : ((GeometryCycle<?>) geom).edges()) {
                    if (!convexContains(bg, precision)) {
                        return false;
                    }
                }
                return true;
            case GEOMETRYGROUP: {
                for (BaseGeometry bg : ((GeometryGroup<?>) geom).getParts()) {
                    if (!convexContains(bg, precision)) {
                        return false;
                    }
                }
                return true;
            }
            default:
                throw new UnsupportedOperationException("Unsupported geometry type: " + geom.getGeometryType());
        }
    }

    public boolean convexOverlaps(BaseGeometry geom) {
        return convexOverlaps(geom, DoubleUtil.EPS);
    }

    /**
     * Tests whether this convex polygon overlaps an object. For cyclic
     * geometries, the interior is to be included in the overlap. For example, a
     * polygon that fully contains the given polygon is also to be returned.
     * This operation works only for convex polygons, and is designed for
     * polygons of low complexity (e.g. triangles or quadrilaterals).
     *
     * @param geom The geometry to overlap with
     * @param precision The desired precision
     * @return true iff the geometric object overlaps the given convex polygon
     */
    public boolean convexOverlaps(BaseGeometry geom, double precision) {
        // Note that, either we get a boundary intersection, or one geometry is contained fully in the other.
        // we catch the one-contained-in-other case by explicitly testing for an arbitrary point
        if (convexContainsPoint(geom.arbitraryPoint(), precision)) {
            return true;
        } else if (geom.getGeometryType() == GeometryType.GEOMETRYGROUP) {
            for (BaseGeometry bg : ((GeometryGroup<?>) geom).getParts()) {
                if (convexOverlaps(bg, precision)) {
                    return true;
                }
            }
            return false;
        } else if (geom.getGeometryType().isCyclic() && ((CyclicGeometry) geom).contains(vertex(0), precision)) {
            return true;
        } else {
            return !intersect(geom, precision).isEmpty();
        }
    }

    /**
     * Computes the centroid of the polygon, assuming a planar polygon. Returns
     * null if the polygon has no vertices.
     *
     * @return new vector representing the centroid
     */
    public Vector centroid() {
        if (vertexCount() == 0) {
            return null;
        } else if (vertexCount() == 1) {
            return vertex(0).clone();
        }

        double A = 0;
        double Cx = 0;
        double Cy = 0;

        Vector p = vertex(-1);
        Vector q;
        for (int i = 0; i < vertexCount(); i++) {
            q = vertex(i);
            double cross = Vector.crossProduct(p, q);
            A += cross;
            Cx += (p.getX() + q.getX()) * cross;
            Cy += (p.getY() + q.getY()) * cross;
            p = q;
        }
        A *= 3;
        Cx = Cx / A;
        Cy = Cy / A;
        return new Vector(Cx, Cy);
    }

    @Override
    public void intersectInterior(BaseGeometry other, double prec, List<BaseGeometry> intersections) {

        double aprec = Math.abs(prec);

        switch (other.getGeometryType()) {
            case LINE: {
                Line line = (Line) other;

                List<BaseGeometry> boundaryIntersections = intersect(other, aprec);

                for (BaseGeometry g : boundaryIntersections) {
                    // order all linesegments along line
                    if (g.getGeometryType() == GeometryType.LINESEGMENT) {
                        LineSegment ls = (LineSegment) g;
                        if (Vector.dotProduct(ls.getDirection(), line.getDirection()) < 0) {
                            ls.reverse();
                        }
                    }
                }

                // sort
                boundaryIntersections.sort((g1, g2) -> {
                    Vector p1;
                    if (g1.getGeometryType() == GeometryType.VECTOR) {
                        p1 = (Vector) g1;
                    } else { // LineSegment
                        p1 = ((LineSegment) g1).getStart();
                    }
                    Vector p2;
                    if (g2.getGeometryType() == GeometryType.VECTOR) {
                        p2 = (Vector) g2;
                    } else { // LineSegment
                        p2 = ((LineSegment) g2).getStart();
                    }
                    double d1 = Vector.dotProduct(line.getDirection(), Vector.subtract(p1, line.getThrough()));
                    double d2 = Vector.dotProduct(line.getDirection(), Vector.subtract(p2, line.getThrough()));
                    return Double.compare(d1, d2);
                });

                // merge
                List<BaseGeometry> merged = new ArrayList();
                Vector vprev = null;
                LineSegment lsprev = null;
                for (BaseGeometry g : boundaryIntersections) {
                    Vector vcurr = null;
                    LineSegment lscurr = null;
                    if (g.getGeometryType() == GeometryType.VECTOR) {
                        vcurr = (Vector) g;
                    } else { // LineSegment
                        lscurr = (LineSegment) g;
                    }

                    if (vprev != null) {
                        if (vcurr != null) {
                            if (vprev.isApproximately(vcurr, aprec)) {
                                // skip over curr
                            } else {
                                // add prev
                                merged.add(vprev);
                                vprev = vcurr;
                            }
                        } else { // lscurr != null
                            // NB: linesegment always replaces a vector
                            if (lscurr.distanceTo(vprev) >= aprec) {
                                // add prev
                                merged.add(vprev);
                            }
                            lsprev = lscurr;
                            vprev = null;
                        }

                    } else if (lsprev != null) {
                        if (vcurr != null) {
                            if (lsprev.distanceTo(vcurr) < aprec) {
                                // skip vprev
                            } else {
                                // add prev
                                merged.add(lsprev);
                                lsprev = null;
                                vprev = vcurr;
                            }
                        } else if (lscurr != null) {
                            if (lsprev.getEnd().isApproximately(lscurr.getStart(), aprec)) {
                                // join them!
                                lsprev.setEnd(lscurr.getEnd());
                            } else {
                                // add prev
                                merged.add(lsprev);
                                lsprev = lscurr;
                            }
                        }
                    } else { // very first...
                        vprev = vcurr;
                        lsprev = lscurr;
                    }
                }

                if (vprev != null) {
                    merged.add(vprev);
                } else if (lsprev != null) {
                    merged.add(lsprev);
                }

                // eliminate nonproper crossings & find interior                
                Vector startAt = null;
                for (BaseGeometry g : merged) {

                    if (g.getGeometryType() == GeometryType.VECTOR) {
                        Vector v = (Vector) g;

                        boolean degenerate = false;

                        for (int i = 0; i < _vertices.size(); i++) {
                            if (_vertices.get(i).isApproximately(v, aprec)) {

                                boolean prevleft = line.isLeftOf(vertex(i - 1), 0);
                                boolean nextleft = line.isLeftOf(vertex(i + 1), 0);

                                degenerate = (prevleft == nextleft);
                                break;
                            }
                        }

                        if (degenerate) {
                            if (startAt == null) {
                                if (prec >= 0) {
                                    intersections.add(v);
                                }
                            } else {
                                if (prec < 0) {
                                    intersections.add(new LineSegment(startAt, v.clone()));
                                    startAt = v;
                                }
                            }
                        } else if (startAt == null) {
                            startAt = v;
                        } else {
                            intersections.add(new LineSegment(startAt, v));
                            startAt = null;
                        }

                    } else {
                        LineSegment ls = (LineSegment) g;

                        boolean prevleft = false;
                        boolean nextleft = true;

                        for (int i = 0; i < _vertices.size(); i++) {
                            if (_vertices.get(i).isApproximately(ls.getStart(), aprec)) {
                                if (ls.onBoundary(vertex(i + 1), aprec)) {
                                    prevleft = line.isLeftOf(vertex(i - 1), 0);
                                } else {
                                    prevleft = line.isLeftOf(vertex(i + 1), 0);
                                }

                                break;
                            }
                        }
                        for (int i = 0; i < _vertices.size(); i++) {
                            if (_vertices.get(i).isApproximately(ls.getEnd(), aprec)) {
                                if (ls.onBoundary(vertex(i + 1), aprec)) {
                                    nextleft = line.isLeftOf(vertex(i - 1), 0);
                                } else {
                                    nextleft = line.isLeftOf(vertex(i + 1), 0);
                                }
                            }
                        }

                        boolean degenerate = (prevleft == nextleft);

                        if (degenerate) {
                            if (startAt == null) {
                                if (prec >= 0) {
                                    intersections.add(ls);
                                }
                            } else {
                                if (prec < 0) {
                                    intersections.add(new LineSegment(startAt, ls.getStart()));
                                    startAt = ls.getEnd();
                                }
                            }
                        } else if (startAt == null) {
                            startAt = prec >= 0 ? ls.getStart() : ls.getEnd();
                        } else {
                            intersections.add(new LineSegment(startAt, prec >= 0 ? ls.getEnd() : ls.getStart()));
                            startAt = null;
                        }
                    }
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Interior intersection not yet implemented for Polygon-" + other.getGeometryType());
        }

    }

    /**
     * Normalizes the index to the range [0,n) where n is the number of vertices
     * (or edges) of the polygon.
     *
     * @param index
     * @return index mod n
     */
    public int index(int index) {
        if (_vertices.isEmpty()) {
            return index;
        }
        while (index < 0) {
            index += _vertices.size();
        }
        return index % _vertices.size();
    }

    /**
     * Returns the next index, in the circular range [0,n) where n is the number
     * of vertices (or edges) of the polygon.
     *
     * @param index
     * @return (index+1) mod n
     */
    public int nextIndex(int index) {
        return index(index + 1);
    }

    /**
     * Returns the previous index, in the circular range [0,n) where n is the
     * number of vertices (or edges) of the polygon.
     *
     * @param index
     * @return (index-1) mod n
     */
    public int previousIndex(int index) {
        return index(index - 1);
    }

    @Override
    public Vector arbitraryPoint() {
        return _vertices.isEmpty() ? null : _vertices.get(0);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void translate(double deltaX, double deltaY) {
        for (Vector v : _vertices) {
            v.translate(deltaX, deltaY);
        }
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        for (Vector v : _vertices) {
            v.rotate(counterclockwiseangle);
        }
    }

    @Override
    public void scale(double factorX, double factorY) {
        for (Vector v : _vertices) {
            v.scale(factorX, factorY);
        }
    }

    /**
     * Appends a vertex to this polygon.
     *
     * @param vertex point to append
     */
    public void addVertex(Vector vertex) {
        _vertices.add(vertex);
    }

    /**
     * Inserts a vertex at the specified index. Vertices at or after this index
     * are shifted. Note that the index is treated circularly.
     *
     * @param index index for the new vertex
     * @param vertex point to insert
     */
    public void addVertex(int index, Vector vertex) {
        _vertices.add(index(index), vertex);
    }

    /**
     * Replaces the vertex at the specified index. Note that the index is
     * treated circularly.
     *
     * @param index index of the vertex to be replaced
     * @param vertex the new position for the vertex
     */
    public void replaceVertex(int index, Vector vertex) {
        _vertices.set(index(index), vertex);
    }

    /**
     * Removes the vertex at the specified index. Note that the index is treated
     * circularly.
     *
     * @param index index of the vertex to be removed
     * @return the removed vertex
     */
    public Vector removeVertex(int index) {
        return _vertices.remove(index(index));
    }

    /**
     * Removes the specified vertex from the polygon.
     *
     * @param vertex point to be removed
     */
    public void removeVertex(Vector vertex) {
        _vertices.remove(vertex);
    }

    @Override
    public void reverse() {
        Collections.reverse(_vertices);
    }

    /**
     * Removes any vertices that do not contribute to the shape of this polygon
     * with precision DoubleUtil.EPS. Both duplicates and collinearities are
     * removed.
     */
    public void minimize() {
        minimize(DoubleUtil.EPS, DoubleUtil.EPS);
    }

    /**
     * Removes any vertices that do not contribute to the shape of this polygon
     * with given precisions. Both duplicates and collinearities are removed.
     *
     * @param distanceprecision precision used for eliminating duplicate
     * vertices
     * @param angleprecision precision used to compare angles of consecutive
     * edges
     */
    public void minimize(double distanceprecision, double angleprecision) {
        // first, remove all zero-length edges
        {
            int i = 0;
            while (i < _vertices.size() && _vertices.size() > 1) {

                if (vertex(i).isApproximately(vertex(i + 1), distanceprecision)) {
                    _vertices.remove(i + 1);
                } else {
                    i++;
                }
            }
        }
        // second, merge all adjacent aligned edges
        {
            Vector curr = vertex(0);
            Vector dprev = Vector.subtract(curr, vertex(-1));
            dprev.normalize();

            int i = 0;
            while (i < _vertices.size() && _vertices.size() > 1) {

                Vector next = vertex(i + 1);

                Vector dnext = Vector.subtract(next, curr);
                dnext.normalize();

                if (Math.abs(dnext.computeSignedAngleTo(dprev, false, false)) < angleprecision) {
                    _vertices.remove(i);
                } else {
                    i++;
                }
                dprev = dnext;
                curr = next;
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POLYGON;
    }

    @Override
    public Polygon clone() {
        List<Vector> cloned = new ArrayList();
        for (Vector vertex : _vertices) {
            cloned.add(vertex.clone());
        }
        return new Polygon(cloned);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _vertices.size() + "]";
    }
    //</editor-fold>
}
