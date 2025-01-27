/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.geometrycore.datastructures.quadtree;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 *
 * @author wmeulema
 */
public class QuadNode<T extends GeometryConvertable> {

    final QuadNode<T> _parent;
    final Rectangle _rect;
    final Rectangle _fullRect;
    final ArrayList<T> _elts = new ArrayList();
    final boolean _infLeft, _infRight, _infBottom, _infTop;
    QuadNode<T> _LT, _LB, _RT, _RB;

    QuadNode(QuadNode parent, Rectangle rect, boolean infLeft, boolean infRight, boolean infBottom, boolean infTop) {
        _parent = parent;
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
}
