/*
 * GeometryCore library   
 * Copyright (C) 2025   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.algorithms.dsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;


/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DepthFirstSearch<TGraph extends SimpleGraph<TGeom, TVertex, TEdge>, TGeom extends OrientedGeometry<TGeom>, TVertex extends SimpleVertex<TGeom, TVertex, TEdge>, TEdge extends SimpleEdge<TGeom, TVertex, TEdge>> {

    private final TGraph _graph;
    private final int[] _distances;
    private final TVertex[] _prev;
    private TVertex _source;

    public DepthFirstSearch(TGraph graph) {
        _graph = graph;
        _distances = new int[_graph.getVertices().size()];
        _prev = (TVertex[]) new SimpleVertex[_graph.getVertices().size()];
    }

    public TVertex getPrevious(TVertex v) {
        return _prev[v.getGraphIndex()];
    }

    /**
     * Returns the path from the source to the specified vertex. The result is
     * null if no path exists or if the source is provided. The source and
     * target vertex are included in the path. Note that this path is not
     * necessarily the shortest path. Use BreadthFirstSearch instead.
     *
     * @param target
     * @return
     */
    public List<TVertex> getPathTo(TVertex target) {
        if (target == _source) {
            List<TVertex> result = new ArrayList();
            result.add(target);
            return result;
        }

        TVertex v = getPrevious(target);
        if (v == null) {
            return null;
        }

        List<TVertex> result = new ArrayList();
        result.add(target);
        result.add(v);
        while (v != _source) {
            v = getPrevious(v);
            result.add(v);
        }
        Collections.reverse(result);
        return result;
    }

    public int run(TVertex source) {
        return run(source, null);
    }

    public int run(TVertex source, TEdge ignore) {
        _source = source;
        Arrays.fill(_distances, -1);
        _distances[source.getGraphIndex()] = 0;
        Arrays.fill(_prev, null);

        Stack<TVertex> Q = new Stack<>();
        int found = 1;
        Q.push(source);
        while (!Q.isEmpty() && found < _graph.getVertices().size()) {
            TVertex v = Q.pop();
            int d = _distances[v.getGraphIndex()];
            for (TVertex nbr : v.getNeighbors()) {
                if (_distances[nbr.getGraphIndex()] < 0 && (ignore == null || !ignore.connects(nbr, v))) {
                    found++;
                    _distances[nbr.getGraphIndex()] = d + 1;
                    _prev[nbr.getGraphIndex()] = v;
                    Q.add(nbr);
                }
            }
        }
        return found;
    }

    public boolean runTo(TVertex source, TVertex target) {
        return runTo(source, target, null);
    }

    public boolean runTo(TVertex source, TVertex target, TEdge ignore) {
        _source = source;
        Arrays.fill(_distances, -1);
        _distances[source.getGraphIndex()] = 0;
        Arrays.fill(_prev, null);

        Queue<TVertex> Q = new LinkedList();
        Q.add(source);
        while (!Q.isEmpty()) {
            TVertex v = Q.remove();
            int d = _distances[v.getGraphIndex()];
            for (TVertex nbr : v.getNeighbors()) {
                if (_distances[nbr.getGraphIndex()] < 0 && (ignore == null || !ignore.connects(nbr, v))) {
                    _distances[nbr.getGraphIndex()] = d + 1;
                    _prev[nbr.getGraphIndex()] = v;
                    if (nbr == target) {
                        return true;
                    }
                    Q.add(nbr);
                }
            }
        }
        return false;
    }
}
