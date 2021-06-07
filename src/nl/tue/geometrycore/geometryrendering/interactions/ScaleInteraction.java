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
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ScaleInteraction<TBase extends BaseGeometry<TBase>> extends Interaction<TBase> {

    private Vector _defaultCenter;
    private double _totalScale;
    private Vector _center;
    private Vector _startDir;

    public ScaleInteraction(Vector defaultCenter, boolean immediate) {
        super(immediate, MouseEvent.BUTTON1, false, true, false);
        _defaultCenter = defaultCenter;
    }

    @Override
    public boolean isTriggeredBy(int button, boolean ctrl, boolean shift, boolean alt) {
        return button == MouseEvent.BUTTON1 && !ctrl && shift && !alt;
    }

    @Override
    public void startInteraction(Vector loc) {
        _totalScale = 1;
        _center = _defaultCenter != null ? _defaultCenter : Rectangle.byBoundingBox(_geometry).center();
        _startDir = Vector.subtract(loc, _center);
    }

    @Override
    public void updateInteraction(Vector loc, Vector prevLoc) {
        if (loc.isApproximately(_center)) {
            // dont...
        } else {
            Vector vec = Vector.subtract(loc, _center);
            double scale = vec.length() / _startDir.length();
            if (_immediate) {
                double delta = scale / _totalScale;
                _geometry.scale(delta, _center);
            }
            _totalScale = scale;
        }
    }

    @Override
    public UndoRedo endInteraction() {
        UndoRedo undoredo = new UndoRedo() {
            Vector _redo_center = _center.clone();
            double _redo_scale = _totalScale;
            TBase _redo_geom = _geometry;

            @Override
            public void undo() {
                _redo_geom.scale(1.0 / _redo_scale, _redo_center);
            }

            @Override
            public void redo() {
                _redo_geom.scale(_redo_scale, _redo_center);
            }
        };
        _totalScale = 0;
        _center = null;
        _startDir = null;
        return undoredo;
    }

    @Override
    public TBase getCurrentInteractedGeometry() {
        if (_immediate) {
            return _geometry;
        } else {
            TBase clone = _geometry.clone();
            clone.scale(_totalScale, _center);
            return clone;
        }
    }

    @Override
    public BaseGeometry[] getCurrentInteractionRepresentation() {
        Vector currDir = _startDir.clone();
        currDir.scale(_totalScale);
        return new BaseGeometry[]{
            new LineSegment(Vector.add(_center, _startDir), Vector.add(_center, currDir)),
            Vector.add(_center, currDir)
        };
    }
}
