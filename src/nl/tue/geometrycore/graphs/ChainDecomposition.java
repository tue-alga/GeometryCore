package nl.tue.geometrycore.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;

/**
 * /**
 * Maximally concatenates edges into "chains", such that each vertex has degree
 * 2, except possibly the first and last.
 *
 * A closed chain represents a full cycle of degree-2 vertices (a "polygon").
 *
 * An open chain represents a chain starting and ending at a vertex of degree
 * not equal to 2 (a "polyline"). Note that an open cycle may start and end at
 * the same vertex.
 *
 * Degree-0 vertices are not part of any chain.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ChainDecomposition<TGraph extends SimpleGraph<TGeom, TVertex, TEdge>, TVertex extends SimpleVertex<TGeom, TVertex, TEdge>, TEdge extends SimpleEdge<TGeom, TVertex, TEdge>, TGeom extends OrientedGeometry<TGeom>> {

    private final TGraph _graph;
    private final List<Chain> _chains;

    public ChainDecomposition(TGraph graph) {
        _graph = graph;
        _chains = new ArrayList();
        initialize();
    }

    public List<Chain> getChains() {
        return _chains;
    }

    private void initialize() {

        boolean[] treated = new boolean[_graph.edgeCount()];
        Arrays.fill(treated, false);

        for (TEdge edge : _graph.getEdges()) {
            if (treated[edge.getGraphIndex()]) {
                continue;
            }

            treated[edge.getGraphIndex()] = true;

            Chain chain = new Chain();
            _chains.add(chain);

            // walk from start backwards
            chain._edges.addLast(edge);

            TEdge walk = edge;
            TVertex hop = walk.getStart();
            while (hop.getDegree() == 2) {
                walk = hop.getEdges().get(0) == walk ? hop.getEdges().get(1) : hop.getEdges().get(0);
                hop = walk.getOtherVertex(hop);

                if (walk == edge) {
                    chain._closed = true;
                    break;
                } else {
                    treated[walk.getGraphIndex()] = true;
                    chain._edges.addFirst(walk);
                }
            }

            if (!chain._closed) {

                walk = edge;
                hop = walk.getEnd();
                while (hop.getDegree() == 2) {
                    walk = hop.getEdges().get(0) == walk ? hop.getEdges().get(1) : hop.getEdges().get(0);
                    hop = walk.getOtherVertex(hop);

                    assert !treated[walk.getGraphIndex()];

                    treated[walk.getGraphIndex()] = true;
                    chain._edges.add(walk);
                }
            }
        }
    }

    public class Chain implements GeometryConvertable<BaseGeometry> {

        boolean _closed = false;
        LinkedList<TEdge> _edges = new LinkedList();

        private TVertex firstVertex() {
            TEdge first = _edges.getFirst();
            if (_edges.size() >= 2) {
                return first.getOtherVertex(first.getCommonVertex(_edges.get(1)));
            } else {
                return first.getStart();
            }
        }

        public boolean isClosed() {
            return _closed;
        }

        public Iterable<TVertex> vertices() {

            return () -> new Iterator<TVertex>() {

                Iterator<TEdge> it = _edges.iterator();
                TVertex prev = firstVertex();
                boolean first = !_closed;

                @Override
                public boolean hasNext() {
                    return first || it.hasNext();
                }

                @Override
                public TVertex next() {
                    if (first) {
                        first = false;
                        return prev;
                    } else {
                        TEdge edge = it.next();
                        prev = edge.getOtherVertex(prev);
                        return prev;
                    }
                }

            };
        }

        public Iterable<TEdge> edges() {
            return _edges;
        }

        @Override
        public BaseGeometry toGeometry() {
            return _closed ? toPolygon() : toPolyLine();
        }

        public List<TGeom> getDirectedGeometries() {
            List<TGeom> geoms = new ArrayList();

            TVertex prev = firstVertex();
            Iterator<TEdge> it = _edges.iterator();
            while (it.hasNext()) {
                TEdge e = it.next();

                TGeom geom = e.toGeometry().clone();
                if (prev != e.getStart()) {
                    geom.reverse();
                }
                geoms.add(geom);

                prev = e.getOtherVertex(prev);
            }

            return geoms;
        }

        /**
         * Uses the actual edge geometry stored in the graph. If the geometry of
         * an edge is a LineSegment, using toPolyLine() is more efficient.
         */
        public GeometryString<TGeom> toGeometryString() {
            return new GeometryString<>(getDirectedGeometries());

        }

        /**
         * Uses the actual edge geometry stored in the graph. If the geometry of
         * an edge is a LineSegment, using toPolygon() is more efficient.
         */
        public GeometryCycle<TGeom> toGeometryCycle() {
            return new GeometryCycle<>(getDirectedGeometries());
        }

        public BaseGeometry toPolygonalGeometry() {
            return _closed ? toPolygon() : toPolyLine();
        }

        /**
         * Uses straight-line edges between consecutive vertices, regardless of
         * actual edge geometry.
         */
        public Polygon toPolygon() {
            if (!_closed) {
                return null;
            }

            Polygon p = new Polygon();
            for (TVertex v : vertices()) {
                p.addVertex(v);
            }
            return p;
        }

        /**
         * Uses straight-line edges between consecutive vertices, regardless of
         * actual edge geometry.
         */
        public PolyLine toPolyLine() {
            if (_closed) {
                return null;
            }

            PolyLine p = new PolyLine();
            for (TVertex v : vertices()) {
                p.addVertex(v);
            }
            return p;
        }
    }

}
