/*
 * GeometryCore library   
 * Copyright (C) 2024   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.quadtree;

import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 * Extends the simple QuadTree implementation, by slightly enlarging each of the
 * children of a node.
 *
 * Specifically, the width and height are extended by a "fuzziness" factor, to
 * overlap with its siblings. A fuzziness factor of 0 effectively gives a
 * regular QuadTree. A fuzziness factor of 0.1 extends the nodes by 10% of their
 * width and height.
 *
 * Elements can now be contained in multiple nodes that are not in a descendant
 * relation: an arbitrary such node will eventually contain the object.
 *
 * @param <T> the class of objects stored in the priority queue
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class FuzzyQuadTree<T extends GeometryConvertable> extends QuadTree<T> {

    private final double _fuzziness;

    /**
     * Constructs a fuzzy quad-tree for the given bounding box and maximum
     * depth.
     *
     * @param box bounding box encompassing all insertions.
     * @param maxDepth maximum depth of the tree
     * @param fuzziness fuzziness factor for overlapping siblings
     */
    public FuzzyQuadTree(Rectangle box, int maxDepth, double fuzziness) {
        super(box, maxDepth);
        _fuzziness = fuzziness;
    }

    @Override
    protected QuadNode<T> find(QuadNode<T> n, T elt, boolean extend) {
        Rectangle b = Rectangle.byBoundingBox(elt);
        // sift up
        while (n._depth > 0 && !n._fullRect.containsCompletely(b)) {
            n = n._parent;
        }
        // sift down
        while (n._depth < _maxDepth) {

            Vector c = n._rect.center();
            double fx = n._rect.width() / 2.0 * _fuzziness;
            double fy = n._rect.height() / 2.0 * _fuzziness;
            if (b.getRight() <= c.getX() + fx) {
                // left
                if (b.getTop() <= c.getY() + fy) {
                    // bottom
                    if (n._LB == null) {
                        if (extend) {
                            n._LB = new QuadNode(n, Rectangle.byCorners(n._rect.leftBottom(), Vector.add(c, new Vector(fx, fy))), n._infLeft, false, n._infBottom, false);
                        } else {
                            return null;
                        }
                    }
                    n = n._LB;
                } else if (b.getBottom() >= c.getY() - fy) {
                    // top
                    if (n._LT == null) {
                        if (extend) {
                            n._LT = new QuadNode(n, Rectangle.byCorners(n._rect.leftTop(), Vector.add(c, new Vector(fx, -fy))), n._infLeft, false, false, n._infTop);
                        } else {
                            return null;
                        }
                    }
                    n = n._LT;
                } else {
                    return n;
                }
            } else if (b.getLeft() >= c.getX() - fx) {
                // right
                if (b.getTop() <= c.getY() + fy) {
                    //  bottom
                    if (n._RB == null) {
                        if (extend) {
                            n._RB = new QuadNode(n, Rectangle.byCorners(n._rect.rightBottom(), Vector.add(c, new Vector(-fx, fy))), false, n._infRight, n._infBottom, false);
                        } else {
                            return null;
                        }
                    }
                    n = n._RB;
                } else if (b.getBottom() >= c.getY() - fy) {
                    //  top
                    if (n._RT == null) {
                        if (extend) {
                            n._RT = new QuadNode(n, Rectangle.byCorners(n._rect.rightTop(), Vector.add(c, new Vector(-fx, -fy))), false, n._infRight, false, n._infTop);
                        } else {
                            return null;
                        }
                    }
                    n = n._RT;
                } else {
                    return n;
                }
            } else {
                return n;
            }
        }

        return n;
    }

}
