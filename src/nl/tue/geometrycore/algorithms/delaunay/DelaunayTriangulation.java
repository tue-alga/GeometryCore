/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.algorithms.delaunay;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.GeometryCloner;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;
import nl.tue.geometrycore.util.LexicographicOrder;

/**
 * Implementation of the Divide-and-Conquer path algorithm to compute the
 * Delaunay triangulation, running in O(V log V) time. Note that calling run()
 * again, will recompute the Delaunay triangulation.
 *
 * @param <TGraph> Class of graph to be used for shortest-path computations
 * @param <TGeom> Class of edge geometry used by TGraph
 * @param <TVertex> Class of vertex used by TGraph
 * @param <TEdge> Class of edge used by TGraph
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DelaunayTriangulation<TGraph extends SimpleGraph<TGeom, TVertex, TEdge>, TGeom extends OrientedGeometry<TGeom>, TVertex extends SimpleVertex<TGeom, TVertex, TEdge>, TEdge extends SimpleEdge<TGeom, TVertex, TEdge>> {

    private final TGraph _input;
    private final GeometryCloner<LineSegment, TGeom> _cloner;
    private final List<TVertex> _vertices;
    private int[] _graph_to_sorted;

    private enum Side {
        LEFT, RIGHT;
    }

    public DelaunayTriangulation(TGraph input, GeometryCloner<LineSegment, TGeom> cloner) {
        _input = input;
        _cloner = cloner;
        _vertices = new ArrayList();
        _graph_to_sorted = null;
    }

    public boolean run() {
        _input.clearEdges();

        _vertices.addAll(_input.getVertices());
        _vertices.sort(new LexicographicOrder());

        _graph_to_sorted = new int[_vertices.size()];
        for (int i = 0; i < _vertices.size(); i++) {
            _graph_to_sorted[_vertices.get(i).getGraphIndex()] = i;
        }

        boolean success = null != compute(0, _vertices.size());

        _graph_to_sorted = null;
        _vertices.clear();

        return success;
    }

    /**
     * Computes the DT on the vertices [low, high) interval
     *
     * @param low index of lowest vertex
     * @param high index of highest vertex + 1
     */
    private TVertex compute(int low, int high) {

        switch (high - low) {
            case 0:
                System.err.println("Should not occur");
                return null;
            case 1: {
                // one vertex               
//                System.err.println("CALL " + low + "-" + (high - 1) + " :: " + _vertices.get(low) + "-" + _vertices.get(high - 1));
                TVertex u = _vertices.get(low);
//                System.err.println("RET " + u + " << " + low + "-" + (high - 1));
                return u;
            }
            case 2: {
                // one line segment
//                System.err.println("CALL " + low + "-" + (high - 1) + " :: " + _vertices.get(low) + "-" + _vertices.get(high - 1));
                TVertex u = _vertices.get(low);
                TVertex v = _vertices.get(low + 1);
                addEdge(u, v);
                TVertex min = u.getY() <= v.getY() ? u : v;
//                System.err.println("RET " + min + " << " + low + "-" + (high - 1));
                return min;
            }
            case 3: {
                // one triangle

//                System.err.println("CALL " + low + "-" + (high - 1) + " :: " + _vertices.get(low) + "-" + _vertices.get(high - 1));
                TVertex u = _vertices.get(low);
                TVertex v = _vertices.get(low + 1);
                TVertex w = _vertices.get(low + 2);
                addEdge(u, v);
                addEdge(v, w);
                addEdge(w, u);
                TVertex min = u.getY() <= v.getY() && u.getY() <= w.getY()
                        ? u : (v.getY() <= w.getY() ? v : w);
//                System.err.println("RET " + min + " << " + low + "-" + (high - 1));
                return min;
            }
            default: {
                int mid = (low + high) / 2;
                TVertex u = compute(low, mid);
                TVertex v = compute(mid, high);

//                System.err.println("CALL " + low + "-" + (high - 1) + " :: " + _vertices.get(low) + "-" + _vertices.get(high - 1));

//                System.err.println("  mid: " + mid);
                if (u == null || v == null) {
                    System.err.println("  nulls?  << " + low + "-" + (high - 1));
                    return null;
                }

//                System.err.println("  u " + u);
//                System.err.println("  v " + v);

                TVertex min = u.getY() <= v.getY() ? u : v;
                // walk up to find edge on lower convex hull

                TVertex prevu;
                TVertex prevv;
                do {
                    prevu = u;
                    prevv = v;
                    u = shiftHull(u, v, mid, Side.LEFT);
                    v = shiftHull(v, u, mid, Side.RIGHT);
                } while (u != prevu || v != prevv);

                addEdge(u, v);

                // start merge process
                TVertex u_cand = candidate(u, v, mid, Side.LEFT);
                TVertex v_cand = candidate(v, u, mid, Side.RIGHT);

//                System.err.println("  u " + u);
//                System.err.println("  v " + v);
//                System.err.println("  u_cand " + u_cand);
//                System.err.println("  v_cand " + v_cand);

                while (u_cand != null || v_cand != null) {
                    if (u_cand != null && v_cand != null) {
                        Circle c = Circle.byThreePoints(u, v, u_cand);
//                        System.err.println("  c "+c);
                        if (c != null && c.contains(v_cand)) {
//                            System.err.println("  discarding u_cand");
                            u_cand = null;
                        } else {
//                            System.err.println("  discarding v_cand");
                            v_cand = null;
                        }
                    }

                    if (u_cand != null) {
                        if (addEdge(u_cand, v)) {
                            System.err.println("DUPLICATE EDGE -- aborting" + " << " + low + "-" + (high - 1));
                            return null;
                        }
                        u = u_cand;
                    } else {  // v_cand != null   
                        if (addEdge(u, v_cand)) {
                            System.err.println("DUPLICATE EDGE -- aborting" + " << " + low + "-" + (high - 1));
                            return null;
                        }
                        v = v_cand;
                    }

                    u_cand = candidate(u, v, mid, Side.LEFT);
                    v_cand = candidate(v, u, mid, Side.RIGHT);

//                    System.err.println("  u " + u);
//                    System.err.println("  v " + v);
//                    System.err.println("  u_cand " + u_cand);
//                    System.err.println("  v_cand " + v_cand);
                }

//                System.err.println("RET " + min + " << " + low + "-" + (high - 1));
                return min;
            }
        }
    }

    private boolean addEdge(TVertex u, TVertex v) {
//        System.err.println("  adding " + u + "-" + v);
        if (u.isNeighborOf(v)) {
            return true;
        }
        _input.addEdge(u, v, _cloner.clone(new LineSegment(u.clone(), v.clone())));
        return false;
    }

    private TVertex shiftHull(TVertex vertex, TVertex other, int mid, Side side) {
        Vector down = Vector.down();

        Vector dir = Vector.subtract(other, vertex);

        switch (side) {
            case LEFT: {
                TVertex next = findFirst(vertex, down, mid, Side.LEFT);
                while (next != null && Vector.crossProduct(dir, Vector.subtract(next, vertex)) <= 0) {
                    vertex = next;
                    next = findFirst(vertex, down, mid, Side.LEFT);
                    dir = Vector.subtract(other, vertex);

                }

                TVertex prev = findLast(vertex, down, mid, Side.LEFT);
                while (prev != null && Vector.crossProduct(dir, Vector.subtract(prev, vertex)) < 0) {
                    vertex = prev;
                    prev = findLast(vertex, down, mid, Side.LEFT);
                    dir = Vector.subtract(other, vertex);
                }
                break;
            }
            case RIGHT:
                TVertex next = findFirst(vertex, down, mid, Side.RIGHT);
                while (next != null && Vector.crossProduct(dir, Vector.subtract(next, vertex)) >= 0) {
                    vertex = next;
                    next = findFirst(vertex, down, mid, Side.RIGHT);
                    dir = Vector.subtract(other, vertex);
                }
                TVertex prev = findLast(vertex, down, mid, Side.RIGHT);
                while (prev != null && Vector.crossProduct(dir, Vector.subtract(prev, vertex)) > 0) {
                    vertex = prev;
                    prev = findLast(vertex, down, mid, Side.RIGHT);
                    dir = Vector.subtract(other, vertex);

                }
                break;
        }
        return vertex;
    }

    private TVertex candidate(TVertex vertex, TVertex other, int mid, Side side) {

        Vector dir = Vector.subtract(other, vertex);
        dir.normalize();

//        System.err.println("  candidate? "+vertex+" "+other);
        
        TVertex first = findFirst(vertex, dir, mid, side);
//        System.err.println("    first "+first);
        if (first == null) {
            return null;
        }
        TVertex second = findSecond(vertex, dir, mid, side);
//        System.err.println("    second "+second);
        Circle c = Circle.byThreePoints(vertex, other, first);
//        System.err.println("    c "+c);
        while (second != null && c != null && c.contains(second)) {
//            System.err.println("  removing " + vertex + "-" + first);
            _input.removeEdge(vertex.getEdgeTo(first));
            first = second;
            second = findSecond(vertex, dir, mid, side);
            c = Circle.byThreePoints(vertex, other, first);
        }
        return first;
    }

    private TVertex findFirst(TVertex vertex, Vector dir, int mid, Side side) {

        TVertex min = null;
        double opt = Double.POSITIVE_INFINITY;
        for (TEdge e : vertex.getEdges()) {
            TVertex nbr = e.getOtherVertex(vertex);
            if (side == Side.LEFT && _graph_to_sorted[nbr.getGraphIndex()] >= mid) {
                continue;
            } else if (side == Side.RIGHT && _graph_to_sorted[nbr.getGraphIndex()] < mid) {
                continue;
            }
            Vector edir = Vector.subtract(nbr, vertex);
            edir.normalize();
            double a = side == Side.LEFT
                    ? dir.computeCounterClockwiseAngleTo(edir, false, false)
                    : dir.computeClockwiseAngleTo(edir, false, false);
//            if (a > Math.PI * 2 - DoubleUtil.EPS) {
//                a = 0;
//            }
            if (a < opt) {
                min = nbr;
                opt = a;
            }
        }

        if (min == null) {
            return null;
        }

        if (opt >= Math.PI) {
            return null;
        }

        return min;
    }

    private TVertex findSecond(TVertex vertex, Vector dir, int mid, Side side) {

        TVertex min = null;
        double opt = Double.POSITIVE_INFINITY;
        TVertex sec = null;
        double sec_opt = Double.POSITIVE_INFINITY;
        for (TEdge e : vertex.getEdges()) {

            TVertex nbr = e.getOtherVertex(vertex);
            if (side == Side.LEFT && _graph_to_sorted[nbr.getGraphIndex()] >= mid) {
                continue;
            } else if (side == Side.RIGHT && _graph_to_sorted[nbr.getGraphIndex()] < mid) {
                continue;
            }
            Vector edir = Vector.subtract(nbr, vertex);
            edir.normalize();
            double a = side == Side.LEFT
                    ? dir.computeCounterClockwiseAngleTo(edir, false, false)
                    : dir.computeClockwiseAngleTo(edir, false, false);
            
//            System.err.println("      "+nbr+" :: "+a);
//            if (a > Math.PI * 2 - DoubleUtil.EPS) {
//                a = 0;
//            }
            if (a < opt) {
                sec = min;
                sec_opt = opt;
                min = nbr;
                opt = a;
            } else if (a < sec_opt) {
                sec = nbr;
                sec_opt = a;
            }
        }

        if (sec == null) {
            return null;
        }

        if (sec_opt >= Math.PI) {
            return null;
        }

        return sec;
    }

    private TVertex findLast(TVertex vertex, Vector dir, int mid, Side side) {

        TVertex min = null;
        double opt = Double.POSITIVE_INFINITY;
        for (TEdge e : vertex.getEdges()) {
            TVertex nbr = e.getOtherVertex(vertex);
            if (side == Side.LEFT && _graph_to_sorted[nbr.getGraphIndex()] >= mid) {
                continue;
            } else if (side == Side.RIGHT && _graph_to_sorted[nbr.getGraphIndex()] < mid) {
                continue;
            }
            Vector edir = Vector.subtract(nbr, vertex);
            edir.normalize();
            // SWAPPED WRT find first/second
            double a = side == Side.RIGHT
                    ? dir.computeCounterClockwiseAngleTo(edir, false, false)
                    : dir.computeClockwiseAngleTo(edir, false, false);
//            if (a > Math.PI * 2 - DoubleUtil.EPS) {
//                a = 0;
//            }
            if (a < opt) {
                min = nbr;
                opt = a;
            }
        }

        if (min == null) {
            return null;
        }

        if (opt >= Math.PI) {
            return null;
        }

        return min;
    }
}
