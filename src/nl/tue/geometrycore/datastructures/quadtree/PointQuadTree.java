/*
 * GeometryCore library   
 * Copyright (C) 2024   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.quadtree;

import java.util.ArrayList;
import java.util.function.Consumer;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 * Provides a simple quad-tree implementation for finding points. It is based on
 * a container rectangle, that is recursively subdivided into four equal parts,
 * up to a maximum depth. These nodes are created on demand, as elements are
 * inserted. Behavior for points outside of the initially specified rectangle is
 * undefined.
 *
 * @param <T> the class of objects stored in the priority queue
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class PointQuadTree<T extends Vector> extends GeometryStore<T> {

    private final PointQuadNode<T> _root;
    private final int _maxDepth;

    /**
     * Constructs a quad-tree for the given bounding box and maximum depth.
     *
     * @param box bounding box encompassing all insertions.
     * @param maxDepth maximum depth of the tree
     */
    public PointQuadTree(Rectangle box, int maxDepth) {
        _root = new PointQuadNode();
        _root._rect = box.clone();
        _maxDepth = maxDepth;
    }

    /**
     * Traverses all nodes in this quad tree, invoking the handler on each node.
     * This method is meant for inspecting the tree only, not for making
     * modifications. The nodes are visited a pre-order traversal.
     *
     * @param handler
     */
    public void traverse(QuadNodeHandler handler) {
        traverse(_root, 0, handler);
    }

    private void traverse(PointQuadNode<T> node, int depth, QuadNodeHandler handler) {
        if (node != null) {
            handler.handle(node, depth);

            depth++;
            traverse(node._LB, depth, handler);
            traverse(node._RB, depth, handler);
            traverse(node._RT, depth, handler);
            traverse(node._LT, depth, handler);
        }
    }

    @Override
    public void insert(T elt) {
        PointQuadNode<T> n = _root;
        int d = 0;
        while (n._elts == null) {
            // have not reached a leaf 
            Vector c = n._rect.center();
            d++;
            if (elt.getX() <= c.getX()) {
                // left
                if (elt.getY() <= c.getY()) {
                    // bottom     
                    if (n._LB == null) {
                        n._LB = new PointQuadNode();
                        n._LB._rect = Rectangle.byCorners(n._rect.leftBottom(), c);
                        n._LB._parent = n;
                        if (d >= _maxDepth) {
                            n._LB._elts = new ArrayList();
                        }
                    }
                    n = n._LB;
                } else {
                    // top                    
                    if (n._LT == null) {
                        n._LT = new PointQuadNode();
                        n._LT._rect = Rectangle.byCorners(n._rect.leftTop(), c);
                        n._LT._parent = n;
                        if (d >= _maxDepth) {
                            n._LT._elts = new ArrayList();
                        }
                    }
                    n = n._LT;
                }
            } else {
                // right
                if (elt.getY() <= c.getY()) {
                    // bottom     
                    if (n._RB == null) {
                        n._RB = new PointQuadNode();
                        n._RB._rect = Rectangle.byCorners(n._rect.rightBottom(), c);
                        n._RB._parent = n;
                        if (d >= _maxDepth) {
                            n._RB._elts = new ArrayList();
                        }
                    }
                    n = n._RB;
                } else {
                    // top                    
                    if (n._RT == null) {
                        n._RT = new PointQuadNode();
                        n._RT._rect = Rectangle.byCorners(n._rect.rightTop(), c);
                        n._RT._parent = n;
                        if (d >= _maxDepth) {
                            n._RT._elts = new ArrayList();
                        }
                    }
                    n = n._RT;
                }
            }
        }
        n._elts.add(elt);
    }

    @Override
    public boolean remove(T elt) {
        PointQuadNode<T> n = _root;
        int d = 0;
        while (n._elts == null) {
            // have not reached a leaf 
            Vector c = n._rect.center();
            d++;
            if (elt.getX() <= c.getX()) {
                // left
                if (elt.getY() <= c.getY()) {
                    // bottom     
                    if (n._LB == null) {
                        return false;
                    }
                    n = n._LB;
                } else {
                    // top                    
                    if (n._LT == null) {
                        return false;
                    }
                    n = n._LT;
                }
            } else {
                // right
                if (elt.getY() <= c.getY()) {
                    // bottom     
                    if (n._RB == null) {
                        return false;
                    }
                    n = n._RB;
                } else {
                    // top                    
                    if (n._RT == null) {
                        return false;
                    }
                    n = n._RT;
                }
            }
        }
        if (n._elts.remove(elt)) {

            do {
                PointQuadNode<T> p = n._parent;
                if (p._LB == n) {
                    p._LB = null;
                } else if (p._LT == n) {
                    p._LT = null;
                } else if (p._RB == n) {
                    p._RB = null;
                } else if (p._RT == n) {
                    p._RT = null;
                }
                n = p;
            } while (n != _root && n._LT == null && n._LB == null && n._RB == null && n._RT == null);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void findContained(Rectangle rect, double precision, Consumer<? super T> action) {
        findContainedRecursive(_root, rect, precision, action);
    }

    private void findContainedRecursive(PointQuadNode<T> n, Rectangle rect, double precision, Consumer<? super T> action) {
        if (n == null || !n._rect.overlaps(rect, precision)) {
            // no node
            // or disjoint
            return;
        }

        if (n._elts == null) {
            // interior node, that overlaps R
            findContainedRecursive(n._LB, rect, precision, action);
            findContainedRecursive(n._RB, rect, precision, action);
            findContainedRecursive(n._LT, rect, precision, action);
            findContainedRecursive(n._RT, rect, precision, action);
        } else {
            // leaf node with elements            
            if (rect.containsCompletely(n._rect, precision)) {
                n._elts.forEach(action);
            } else {
                for (T elt : n._elts) {
                    if (rect.contains(elt, precision)) {
                        action.accept(elt);
                    }
                }
            }
        }
    }

    @Override
    public void findContained(Polygon convex, double precision, Consumer<? super T> action) {
        findContainedRecursive(_root, convex, precision, action);
    }

    private void findContainedRecursive(PointQuadNode<T> n, Polygon convex, double precision, Consumer<? super T> action) {
        if (n == null || !convex.convexOverlaps(n._rect, precision)) {
            // no node
            // or disjoint
            return;
        }

        if (n._elts == null) {
            // interior node, that overlaps R
            findContainedRecursive(n._LB, convex, precision, action);
            findContainedRecursive(n._RB, convex, precision, action);
            findContainedRecursive(n._LT, convex, precision, action);
            findContainedRecursive(n._RT, convex, precision, action);
        } else {
            // leaf node with elements            
            if (convex.convexContains(n._rect, precision)) {
                n._elts.forEach(action);
            } else {
                for (T elt : n._elts) {
                    if (convex.convexContainsPoint(elt, precision)) {
                        action.accept(elt);
                    }
                }
            }
        }
    }

    /**
     * Finds an element that has approximately the same position as specified.
     * Take O(maxdepth) time, plus the number of elements in the leaf node(s),
     * but may traverse up to four paths, if the given position is close to a
     * corner or boundary of a quad. If multiple elements would match the given
     * position, an arbitrary one is returned.
     *
     * @param point position of the element to search for
     * @param precision desired precision
     * @return element with approximately the given position, or null if no such
     * element exists
     */
    public T find(Vector point, double precision) {
        return findRecursive(_root, point, precision);
    }

    private T findRecursive(PointQuadNode<T> n, Vector point, double precision) {
        if (n == null || !n._rect.contains(point, precision)) {
            return null;
        } else if (n._elts == null) {
            T elt = findRecursive(n._LB, point, precision);
            if (elt != null) {
                return elt;
            }
            elt = findRecursive(n._LT, point, precision);
            if (elt != null) {
                return elt;
            }
            elt = findRecursive(n._RT, point, precision);
            if (elt != null) {
                return elt;
            }
            elt = findRecursive(n._RB, point, precision);
            return elt;
        } else {
            for (T elt : n._elts) {
                if (elt.isApproximately(point, precision)) {
                    return elt;
                }
            }
            return null;
        }
    }   

    @Override
    public void findStabbed(Vector point, double precision, Consumer<? super T> action) {
        findStabbedRecursive(_root, point, precision, action);
    }

    private void findStabbedRecursive(PointQuadNode<T> n, Vector point, double precision, Consumer<? super T> action) {
        if (n == null || !n._rect.contains(point, precision)) {
            // no node
            // or disjoint
            return;
        }

        if (n._elts == null) {
            findStabbedRecursive(n._LB, point, precision, action);
            findStabbedRecursive(n._LT, point, precision, action);
            findStabbedRecursive(n._RT, point, precision, action);
            findStabbedRecursive(n._RB, point, precision, action);
        } else {
            for (T elt : n._elts) {
                if (elt.isApproximately(point, precision)) {
                    action.accept(elt);
                }
            }
        }
    }

    @Override
    public void findOverlapping(Rectangle rect, double precision, Consumer<? super T> action) {
        // dealing with points only, so overlapping == contained
        findContained(rect, precision, action);
    }

    @Override
    public void findOverlapping(Polygon convex, double precision, Consumer<? super T> action) {
        // dealing with points only, so overlapping == contained
        findContained(convex, precision, action);
    }

    public static class PointQuadNode<T extends Vector> {

        PointQuadNode<T> _parent;
        Rectangle _rect;
        PointQuadNode<T> _LT, _LB, _RT, _RB;
        ArrayList<T> _elts;

        public Rectangle getRect() {
            return _rect;
        }

        public ArrayList<T> getElts() {
            return _elts;
        }
    }

    public interface QuadNodeHandler<T extends Vector> {

        void handle(PointQuadNode<T> quadnode, int depth);
    }
}
