/*
 * GeometryCore library   
 * Copyright (C) 2024   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.quadtree;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;

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
public class QuadTree<T extends GeometryConvertable> {

    private final QuadNode<T> _root;
    private final int _maxDepth;

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

    public QuadNode<T> find(T elt) {
        return find(elt, false);
    }

    private QuadNode<T> find(T elt, boolean extend) {
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

    /**
     * Inserts the given element into the quad-tree.
     *
     * @param elt element to insert
     */
    public QuadNode<T> insert(T elt) {
        QuadNode<T> n = find(elt, true);
        n._elts.add(elt);
        return n;
    }

    /**
     * Removes the specified element from the quad-tree.
     *
     * @param elt the element to be removed
     * @return true iff the element was found
     */
    public boolean remove(T elt) {
        QuadNode<T> n = find(elt, false);
        if (n == null) {
            return false;
        }
        return remove(elt, n);
    }

    public boolean remove(T elt, QuadNode<T> n) {
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

    /**
     * Finds all elements that are fully contained within the specified
     * rectangle. Time is proportional to the number of leaves that intersect
     * the rectangle, the number of elements on the path to these leaves, and
     * their geometric complexity.
     *
     * @param R rectangle in which to search for elements
     * @param prec desired precision; use negative values for excluding the
     * rectangle boundary
     * @return a list of the contained elements
     */
    public List<T> findContainment(Rectangle R, double prec) {
        List<T> result = new ArrayList();
        findContainmentRecursive(result, _root, R, prec);
        return result;
    }

    private void findContainmentRecursive(List<T> result, QuadNode<T> n, Rectangle R, double prec) {

        if (n == null || !n._fullRect.overlaps(R, prec)) {
            // no node
            // or disjoint
            return;
        }

        // elements of this node
        if (R.containsCompletely(n._fullRect, prec)) {
            result.addAll(n._elts);
        } else {
            for (T elt : n._elts) {
                if (R.containsCompletely(elt, prec)) {
                    result.add(elt);
                }
            }
        }

        // elements of children
        findContainmentRecursive(result, n._LB, R, prec);
        findContainmentRecursive(result, n._RB, R, prec);
        findContainmentRecursive(result, n._LT, R, prec);
        findContainmentRecursive(result, n._RT, R, prec);
    }

    /**
     * Finds all elements that overlap the specified rectangle. Note that for
     * cyclic geometries (polygons and such), overlap with the interior is also
     * considered. Time is proportional to the number of leaves that intersect
     * the rectangle, the number of elements on the path to these leaves, and
     * their geometric complexity.
     *
     * @param R rectangle with which to search for elements
     * @param prec desired precision; use negative values for excluding the
     * rectangle boundary
     * @return a list of the contained elements
     */
    public List<T> findOverlap(Rectangle R, double prec) {
        List<T> result = new ArrayList();
        findOverlapRecursive(result, _root, R, prec);
        return result;
    }

    private void findOverlapRecursive(List<T> result, QuadNode<T> n, Rectangle R, double prec) {

        if (n == null || !n._fullRect.overlaps(R, prec)) {
            // no node
            // or disjoint
            return;
        }

        // elements of this node
        if (R.containsCompletely(n._fullRect, prec)) {
            result.addAll(n._elts);
        } else {
            for (T elt : n._elts) {
                if (overlaps(R, elt.toGeometry(), prec)) {
                    result.add(elt);
                }
            }
        }

        // elements of children
        findOverlapRecursive(result, n._LB, R, prec);
        findOverlapRecursive(result, n._RB, R, prec);
        findOverlapRecursive(result, n._LT, R, prec);
        findOverlapRecursive(result, n._RT, R, prec);
    }

    private boolean overlaps(Rectangle R, BaseGeometry geom, double prec) {
        if (R.containsCompletely(geom, prec)) {
            return true;
        } else if (geom.getGeometryType() == GeometryType.GEOMETRYGROUP) {
            for (BaseGeometry bg : ((GeometryGroup<?>) geom).getParts()) {
                if (overlaps(R, bg, prec)) {
                    return true;
                }
            }
            return false;
        } else if (geom.getGeometryType().isCyclic() && ((CyclicGeometry) geom).contains(R.center(), prec)) {
            // element has surface area and contains a point of the query (so, may completely contain)
            return true;
        } else {
            // there is a boundary intersection between query and the element
            // NB: for cyclic geometries, full containment has been excluded, 
            // so if there is overlap there must be a boundary intersection
            return !R.intersect(geom, prec).isEmpty();
        }
    }

    /**
     * Finds all elements that overlap the specified point. Note that for cyclic
     * geometries (polygons and such), overlap with the interior is also
     * considered. Time is proportional to the number of leaves that intersect
     * the rectangle, the number of elements on the path to these leaves, and
     * their geometric complexity.
     *
     * @param pt point with which to search for elements
     * @param prec desired precision
     * @return a list of the contained elements
     */
    public List<T> findStabbed(Vector pt, double prec) {
        List<T> result = new ArrayList();
        findStabbedRecursive(result, _root, pt, prec);
        return result;
    }

    private void findStabbedRecursive(List<T> result, QuadNode<T> n, Vector pt, double prec) {

        if (n == null || !n._fullRect.contains(pt, prec)) {
            // no node
            // or disjoint
            return;
        }

        // elements of this node
        for (T elt : n._elts) {
            if (isStabbed(pt, elt.toGeometry(), prec)) {
                result.add(elt);
            }
        }

        // elements of children
        findStabbedRecursive(result, n._LB, pt, prec);
        findStabbedRecursive(result, n._RB, pt, prec);
        findStabbedRecursive(result, n._LT, pt, prec);
        findStabbedRecursive(result, n._RT, pt, prec);
    }

    private boolean isStabbed(Vector pt, BaseGeometry geom, double prec) {
        if (geom.getGeometryType() == GeometryType.GEOMETRYGROUP) {
            for (BaseGeometry bg : ((GeometryGroup<?>) geom).getParts()) {
                if (isStabbed(pt, bg, prec)) {
                    return true;
                }
            }
            return false;
        } else if (geom.getGeometryType().isCyclic()) {
            return ((CyclicGeometry) geom).contains(pt, prec);
        } else {
            return geom.onBoundary(pt, prec);
        }
    }
}
