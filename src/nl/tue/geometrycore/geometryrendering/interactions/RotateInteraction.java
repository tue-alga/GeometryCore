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
public class RotateInteraction<TBase extends BaseGeometry<TBase>> extends Interaction<TBase> {

    private Vector _defaultCenter;
    private double _totalAngle;
    private Vector _center;
    private Vector _startDir;

    public RotateInteraction(Vector defaultCenter, boolean immediate) {
        super(immediate, MouseEvent.BUTTON1, true, false, false);
        _defaultCenter = defaultCenter;
    }

    @Override
    public void startInteraction(Vector loc) {
        _totalAngle = 0;
        _center = _defaultCenter != null ? _defaultCenter : Rectangle.byBoundingBox(_geometry).center();
        _startDir = Vector.subtract(loc, _center);
    }

    @Override
    public void updateInteraction(Vector loc, Vector prevLoc) {
        if (loc.isApproximately(_center)) {
            // dont...
        } else {
            Vector vec = Vector.subtract(loc, _center);
            double angle = _startDir.computeSignedAngleTo(vec);
            if (_immediate) {
                double delta = angle - _totalAngle;
                _geometry.rotate(delta, _center);
            }
            _totalAngle = angle;
        }
    }

    @Override
    public UndoRedo endInteraction() {
        UndoRedo undoredo = new UndoRedo() {
            Vector _redo_center = _center.clone();
            double _redo_angle = _totalAngle;
            TBase _redo_geom = _geometry;
            
            @Override
            public void undo() {
                _redo_geom.rotate(-_redo_angle, _redo_center);
            }

            @Override
            public void redo() {
                _redo_geom.rotate(_redo_angle, _redo_center);
            }
        };
        _totalAngle = 0;
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
            clone.rotate(_totalAngle, _center);
            return clone;
        }
    }

    @Override
    public BaseGeometry[] getCurrentInteractionRepresentation() {
        Vector currDir = _startDir.clone();
        currDir.rotate(_totalAngle);
        return new BaseGeometry[]{
            LineSegment.byStartAndOffset(_center, _startDir),
            LineSegment.byStartAndOffset(_center, currDir),
            Vector.add(_center, currDir)
        };
    }

}
