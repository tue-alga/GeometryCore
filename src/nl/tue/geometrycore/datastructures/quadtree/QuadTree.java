/*
 * GeometryCore library   
 * Copyright (C) 2024   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.quadtree;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.Vector;
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
public class QuadTree<T extends Vector> {

    private final QuadNode _root;
    private final int _maxDepth;

    /**
     * Constructs a quad-tree for the given bounding box and maximum depth.
     *
     * @param box bounding box encompassing all insertions.
     * @param maxDepth maximum depth of the tree
     */
    public QuadTree(Rectangle box, int maxDepth) {
        _root = new QuadNode();
        _root._rect = box.clone();
        _maxDepth = maxDepth;
    }

    /**
     * Inserts the given element into the quad-tree.
     *
     * @param elt element to insert
     */
    public void insert(T elt) {
        QuadNode n = _root;
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
                        n._LB = new QuadNode();
                        n._LB._rect = Rectangle.byCorners(n._rect.leftBottom(), c);
                        if (d >= _maxDepth) {
                            n._LB._elts = new ArrayList();
                        }
                    }
                    n = n._LB;
                } else {
                    // top                    
                    if (n._LT == null) {
                        n._LT = new QuadNode();
                        n._LT._rect = Rectangle.byCorners(n._rect.leftTop(), c);
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
                        n._RB = new QuadNode();
                        n._RB._rect = Rectangle.byCorners(n._rect.rightBottom(), c);
                        if (d >= _maxDepth) {
                            n._RB._elts = new ArrayList();
                        }
                    }
                    n = n._RB;
                } else {
                    // top                    
                    if (n._RT == null) {
                        n._RT = new QuadNode();
                        n._RT._rect = Rectangle.byCorners(n._rect.rightTop(), c);
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

    /**
     * Removes the specified element from the quad-tree.
     *
     * @param elt the element to be removed
     * @return true iff the element was found
     */
    public boolean remove(T elt) {
        QuadNode n = _root;
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
        return n._elts.remove(elt);
    }

    /**
     * Finds all elements that are contained within the specified rectangle.
     * Time is proportional to the number of leaves that intersect the
     * rectangle, and the elements contained therein.
     *
     * @param R rectangle in which to search for elements
     * @param prec desired precision; use negative values for excluding the
     * rectangle boundary
     * @return a list of the contained elements
     */
    public List<T> find(Rectangle R, double prec) {
        List<T> result = new ArrayList();
        findRecursive(result, _root, R, prec);
        return result;
    }

    private void findRecursive(List<T> result, QuadNode n, Rectangle R, double prec) {
        if (n == null || !n._rect.overlaps(R, prec)) {
            // no node
            // or disjoint
            return;
        }

        if (n._elts == null) {
            // interior node, that overlaps R
            findRecursive(result, n._LB, R, prec);
            findRecursive(result, n._RB, R, prec);
            findRecursive(result, n._LT, R, prec);
            findRecursive(result, n._RT, R, prec);
        } else {
            // leaf node with elements            
            if (R.containsCompletely(n._rect, prec)) {
                result.addAll(n._elts);
            } else {
                for (T elt : n._elts) {
                    if (R.contains(elt, prec)) {
                        result.add(elt);
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
     * @param pos position of the element to search for
     * @param prec desired precision
     * @return element with approximately the given position, or null if no such
     * element exists
     */
    public T find(Vector pos, double prec) {
        return findRecursive(_root, pos, prec);
    }

    private T findRecursive(QuadNode n, Vector pos, double prec) {
        if (n == null || !n._rect.contains(pos, prec)) {
            return null;
        } else if (n._elts == null) {
            T elt = findRecursive(n._LB, pos, prec);
            if (elt != null) {
                return elt;
            }
            elt = findRecursive(n._LT, pos, prec);
            if (elt != null) {
                return elt;
            }
            elt = findRecursive(n._RT, pos, prec);
            if (elt != null) {
                return elt;
            }
            elt = findRecursive(n._RB, pos, prec);
            return elt;
        } else {
            for (T elt : n._elts) {
                if (elt.isApproximately(pos, prec)) {
                    return elt;
                }
            }
            return null;
        }
    }

    private class QuadNode {

        Rectangle _rect;
        QuadNode _LT, _LB, _RT, _RB;
        ArrayList<T> _elts;

    }
}
