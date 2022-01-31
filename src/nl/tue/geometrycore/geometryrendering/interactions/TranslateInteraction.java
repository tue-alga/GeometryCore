/*
 * GeometryCore library   
 * Copyright (C) 2021   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.interactions;

import java.awt.event.MouseEvent;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class TranslateInteraction<TBase extends BaseGeometry<TBase>> extends Interaction<TBase> {

    private Vector _start;
    private Vector _totalDelta;

    public TranslateInteraction(boolean immediate) {
        super(immediate, MouseEvent.BUTTON1, false, false, false);
    }

    @Override
    public void startInteraction(Vector loc) {
        _totalDelta = Vector.origin();
        _start = loc;
    }

    @Override
    public void updateInteraction(Vector loc, Vector prevLoc) {
        Vector delta = Vector.subtract(loc, _start);
        if (_immediate) {
            Vector d = Vector.subtract(delta, _totalDelta);
            _geometry.translate(d);
        }
        _totalDelta = delta;
    }

    @Override
    public UndoRedo endInteraction() {
        UndoRedo undoredo = new UndoRedo() {
            Vector _redo_delta = _totalDelta.clone();
            TBase _redo_geom = _geometry;

            @Override
            public void undo() {
                _redo_geom.translate(-_redo_delta.getX(), -_redo_delta.getY());
            }

            @Override
            public void redo() {
                _redo_geom.translate(_redo_delta.getX(), _redo_delta.getY());
            }
        };
        _totalDelta = null;
        _start = null;
        return undoredo;
    }

    @Override
    public TBase getCurrentInteractedGeometry() {
        if (_immediate) {
            return _geometry;
        } else {
            TBase clone = _geometry.clone();
            clone.translate(_totalDelta);
            return clone;
        }
    }

    @Override
    public BaseGeometry[] getCurrentInteractionRepresentation() {
        return new BaseGeometry[]{
            LineSegment.byStartAndOffset(_start, _totalDelta),
            Vector.add(_start, _totalDelta)
        };
    }

}
