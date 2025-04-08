/*
 * GeometryCore library   
 * Copyright (C) 2024   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.quadtree;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 * Provides a simple quad-tree implementation for finding geometric objects. It
 * is based on a container rectangle, that is recursively subdivided into four
 * equal parts, up to a maximum depth. These nodes are created on demand as
 * elements are inserted, and removed when possible as elements are removed.
 * Elements are stored smallest node that contains it still. Boxes that are
 * adjacent to the exterior of the specified rectangle are treated as infinite
 * towards that side.
 *
 * Note that the implementation is efficient predominantly for working with many
 * small objects over a large domain. Large objects are stored high up in the
 * tree and are thus not very efficient.
 *
 * @param <T> the class of objects stored in the priority queue
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class QuadTree<T extends GeometryConvertable> implements GeometryStore<T> {

    protected final QuadNode<T> _root;
    protected final int _maxDepth;

    /**
     * Constructs a quad-tree for the given bounding box and maximum depth.
     *
     * @param box bounding box encompassing all insertions.
     * @param maxDepth maximum depth of the tree
     */
    public QuadTree(Rectangle box, int maxDepth) {
        _root = new QuadNode(null, box.clone(), true, true, true, true);
        _maxDepth = maxDepth;
    }

    public QuadNode<T> getRoot() {
        return _root;
    }
    
    public int getMaxDepth() {
        return _maxDepth;
    }

    public QuadNode<T> find(T elt) {
        return find(elt, false);
    }

    protected QuadNode<T> find(T elt, boolean extend) {
        Rectangle b = Rectangle.byBoundingBox(elt);
        QuadNode<T> n = _root;
        int d = 0;
        while (d <= _maxDepth) {

            Vector c = n._rect.center();
            if (b.getRight() <= c.getX()) {
                // left
                if (b.getTop() <= c.getY()) {
                    // bottom
                    if (n._LB == null) {
                        if (extend) {
                            n._LB = new QuadNode(n, Rectangle.byCorners(n._rect.leftBottom(), c), n._infLeft, false, n._infBottom, false);
                        } else {
                            return null;
                        }
                    }
                    n = n._LB;
                    d++;
                } else if (b.getBottom() >= c.getY()) {
                    // top
                    if (n._LT == null) {
                        if (extend) {
                            n._LT = new QuadNode(n, Rectangle.byCorners(n._rect.leftTop(), c), n._infLeft, false, false, n._infTop);
                        } else {
                            return null;
                        }
                    }
                    n = n._LT;
                    d++;
                } else {
                    return n;
                }
            } else if (b.getLeft() >= c.getX()) {
                // right
                if (b.getTop() <= c.getY()) {
                    //  bottom
                    if (n._RB == null) {
                        if (extend) {
                            n._RB = new QuadNode(n, Rectangle.byCorners(n._rect.rightBottom(), c), false, n._infRight, n._infBottom, false);
                        } else {
                            return null;
                        }
                    }
                    n = n._RB;
                    d++;
                } else if (b.getBottom() >= c.getY()) {
                    //  top
                    if (n._RT == null) {
                        if (extend) {
                            n._RT = new QuadNode(n, Rectangle.byCorners(n._rect.rightTop(), c), false, n._infRight, false, n._infTop);
                        } else {
                            return null;
                        }
                    }
                    n = n._RT;
                    d++;
                } else {
                    return n;
                }
            } else {
                return n;
            }
        }

        return n;
    }

    /**
     * Determines whether the given element is contained in the quad-tree.
     *
     * @param elt the element to be sought
     * @return true iff the element contained in the tree
     */
    public boolean contains(T elt) {
        QuadNode<T> n = find(elt, false);
        if (n == null) {
            return false;
        }
        return n._elts.contains(elt);
    }

    @Override
    public void insert(T elt) {
        insertElement(elt);
    }

    /**
     * Inserts the given element into the quad-tree.
     *
     * @param elt element to insert
     * @return The node of the quadtree storing the element
     */
    public QuadNode<T> insertElement(T elt) {
        QuadNode<T> n = find(elt, true);
        n._elts.add(elt);
        return n;
    }

    @Override
    public boolean remove(T elt) {
        QuadNode<T> n = find(elt, false);
        if (n == null) {
            return false;
        }
        return removeElement(elt, n);
    }

    public boolean removeElement(T elt, QuadNode<T> n) {
        boolean result = n._elts.remove(elt);

        // trim the tree
        while (n._parent != null && n._elts.isEmpty() && n._LB == null && n._LT == null && n._RB == null && n._RT == null) {
            // cull it
            QuadNode p = n._parent;
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
        }

        return result;
    }

    @Override
    public List<T> findContained(Rectangle rect, double precision) {
        List<T> result = new ArrayList();
        findContained(result, _root, rect, precision);
        return result;
    }

    private void findContained(List<T> result, QuadNode<T> n, Rectangle rect, double precision) {

        if (n == null || !n._fullRect.overlaps(rect, precision)) {
            // no node
            // or disjoint
            return;
        }

        // elements of this node
        if (rect.containsCompletely(n._fullRect, precision)) {
            result.addAll(n._elts);
        } else {
            for (T elt : n._elts) {
                if (rect.containsCompletely(elt, precision)) {
                    result.add(elt);
                }
            }
        }

        // elements of children
        findContained(result, n._LB, rect, precision);
        findContained(result, n._RB, rect, precision);
        findContained(result, n._LT, rect, precision);
        findContained(result, n._RT, rect, precision);
    }

    @Override
    public List<T> findContained(Polygon convex, double precision) {
        List<T> result = new ArrayList();
        findContained(result, _root, convex, precision);
        return result;
    }

    private void findContained(List<T> result, QuadNode<T> n, Polygon convex, double precision) {

        if (n == null || !convex.convexOverlaps(n._fullRect, precision)) {
            // no node
            // or disjoint
            return;
        }

        // elements of this node
        if (convex.convexContains(n._fullRect, precision)) {
            result.addAll(n._elts);
        } else {
            for (T elt : n._elts) {
                if (convex.convexContains(elt.toGeometry(), precision)) {
                    result.add(elt);
                }
            }
        }

        // elements of children
        findContained(result, n._LB, convex, precision);
        findContained(result, n._RB, convex, precision);
        findContained(result, n._LT, convex, precision);
        findContained(result, n._RT, convex, precision);
    }

    @Override
    public List<T> findOverlapping(Rectangle rect, double precision) {
        List<T> result = new ArrayList();
        findOverlappingRecursive(result, _root, rect, precision);
        return result;
    }

    private void findOverlappingRecursive(List<T> result, QuadNode<T> n, Rectangle rect, double precision) {

        if (n == null || !n._fullRect.overlaps(rect, precision)) {
            // no node
            // or disjoint
            return;
        }

        // elements of this node
        if (rect.containsCompletely(n._fullRect, precision)) {
            result.addAll(n._elts);
        } else {
            for (T elt : n._elts) {
                if (rect.overlaps(elt.toGeometry(), precision)) {
                    result.add(elt);
                }
            }
        }

        // elements of children
        findOverlappingRecursive(result, n._LB, rect, precision);
        findOverlappingRecursive(result, n._RB, rect, precision);
        findOverlappingRecursive(result, n._LT, rect, precision);
        findOverlappingRecursive(result, n._RT, rect, precision);
    }
    
    @Override
    public List<T> findOverlapping(Polygon convex, double precision) {
        List<T> result = new ArrayList();
        findOverlappingRecursive(result, _root, convex, precision);
        return result;
    }

    private void findOverlappingRecursive(List<T> result, QuadNode<T> n, Polygon convex, double precision) {

        if (n == null || !n._fullRect.overlaps(convex, precision)) {
            // no node
            // or disjoint
            return;
        }

        // elements of this node
        if (convex.convexContains(n._fullRect, precision)) {
            result.addAll(n._elts);
        } else {
            for (T elt : n._elts) {
                if (convex.convexOverlaps(elt.toGeometry(), precision)) {
                    result.add(elt);
                }
            }
        }

        // elements of children
        findOverlappingRecursive(result, n._LB, convex, precision);
        findOverlappingRecursive(result, n._RB, convex, precision);
        findOverlappingRecursive(result, n._LT, convex, precision);
        findOverlappingRecursive(result, n._RT, convex, precision);
    }

    @Override
    public List<T> findStabbed(Vector point, double precision) {
        List<T> result = new ArrayList();
        findStabbedRecursive(result, _root, point, precision);
        return result;
    }

    private void findStabbedRecursive(List<T> result, QuadNode<T> n, Vector point, double precision) {

        if (n == null || !n._fullRect.contains(point, precision)) {
            // no node
            // or disjoint
            return;
        }

        // elements of this node
        for (T elt : n._elts) {
            if (point.stabs(elt.toGeometry(), precision)) {
                result.add(elt);
            }
        }

        // elements of children
        findStabbedRecursive(result, n._LB, point, precision);
        findStabbedRecursive(result, n._RB, point, precision);
        findStabbedRecursive(result, n._LT, point, precision);
        findStabbedRecursive(result, n._RT, point, precision);
    }
}
