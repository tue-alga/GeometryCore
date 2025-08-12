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
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 * Node class used in the quad tree implementations.
 *
 * @param <T> the class of objects stored in the quad tree
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class QuadNode<T extends GeometryConvertable> {

    final QuadNode<T> _parent;
    final Rectangle _rect;
    final Rectangle _fullRect;
    final ArrayList<T> _elts = new ArrayList();
    final boolean _infLeft, _infRight, _infBottom, _infTop;
    final int _depth;
    QuadNode<T> _LT, _LB, _RT, _RB;

    QuadNode(QuadNode parent, Rectangle rect, boolean infLeft, boolean infRight, boolean infBottom, boolean infTop) {
        _parent = parent;
        _depth = parent == null ? 0 : parent._depth + 1;
        _rect = rect;
        _infLeft = infLeft;
        _infRight = infRight;
        _infBottom = infBottom;
        _infTop = infTop;
        if (_infLeft || _infRight || _infBottom || _infTop) {
            _fullRect = rect.clone();
            if (_infLeft) {
                _fullRect.setLeft(Double.NEGATIVE_INFINITY);
            }
            if (_infRight) {
                _fullRect.setRight(Double.POSITIVE_INFINITY);
            }
            if (_infBottom) {
                _fullRect.setBottom(Double.NEGATIVE_INFINITY);
            }
            if (_infTop) {
                _fullRect.setTop(Double.POSITIVE_INFINITY);
            }
        } else {
            _fullRect = rect;
        }
    }

    public Rectangle getRectangle() {
        return _rect;
    }

    public Rectangle getFullRectangle() {
        return _fullRect;
    }

    public List<T> getElements() {
        return _elts;
    }
    
    public int getDepth() {
        return _depth;
    }
}
