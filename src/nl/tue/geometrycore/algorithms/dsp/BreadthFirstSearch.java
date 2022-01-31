/*
 * GeometryCore library   
 * Copyright (C) 2021   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.algorithms.dsp;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class BreadthFirstSearch<TGraph extends SimpleGraph<TGeom, TVertex, TEdge>, TGeom extends OrientedGeometry<TGeom>, TVertex extends SimpleVertex<TGeom, TVertex, TEdge>, TEdge extends SimpleEdge<TGeom, TVertex, TEdge>> {

    private final TGraph _graph;
    private final int[] _distances;
    private final TVertex[] _prev;

    public BreadthFirstSearch(TGraph graph) {
        _graph = graph;
        _distances = new int[_graph.getVertices().size()];
        _prev = (TVertex[]) new SimpleVertex[_graph.getVertices().size()];
    }

    public int run(TVertex source) {
       return run(source, null);
    }
    
    public int run(TVertex source, TEdge ignore) {
        Arrays.fill(_distances, -1);
        _distances[source.getGraphIndex()] = 0;
        Arrays.fill(_prev, null);
        
        Queue<TVertex> Q = new LinkedList();
        int found = 1;
        Q.add(source);
        while (!Q.isEmpty() && found < _graph.getVertices().size()) {
            TVertex v = Q.remove();
            int d = _distances[v.getGraphIndex()];            
            for (TVertex nbr : v.getNeighbors()) {
                if (_distances[nbr.getGraphIndex()] < 0 && (ignore == null || !ignore.connects(nbr, v))) {
                    found++;
                    _distances[nbr.getGraphIndex()] = d+1;
                    _prev[nbr.getGraphIndex()] = v;
                    Q.add(nbr);
                }
            }
        }
        return found;
    }
    
    public boolean runTo(TVertex source, TVertex target) {
        return runTo(source,target,null);
    }
    
    public boolean runTo(TVertex source, TVertex target, TEdge ignore) {
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
                    _distances[nbr.getGraphIndex()] = d+1;
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
