/*
 * GeometryCore library   
 * Copyright (C) 2021   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.interactions;

import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.Vector;

/**
 * Interactions are handled through layers: groups of objects that allow the
 * same interaction. An object can be interacted with, based on configuring a
 * distance towards one of its object (either in view distance / pixels or in
 * world distance). For cyclic geometry, it can also be configured to allow
 * interactions based on clicking inside such geometric objects.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 * @param <TBase> The type of geometry upon which this layer interacts
 */
public class InteractiveLayer<TBase extends BaseGeometry<TBase>> {

    protected final List<TBase> _geometries = new ArrayList();
    protected final List<Interaction> _actions = new ArrayList();
    protected double _clickDistance;
    protected boolean _viewDistance;
    protected boolean _allowInterior;

    /**
     *
     * @param clickDistance
     * @param viewDistance
     * @param allowInterior
     * @param actions
     */
    public InteractiveLayer(double clickDistance, boolean viewDistance, boolean allowInterior, Interaction... actions) {
        _clickDistance = clickDistance;
        _viewDistance = viewDistance;
        _allowInterior = allowInterior;
        for (Interaction action : actions) {
            _actions.add(action);
        }
    }

    public List<TBase> getGeometries() {
        return _geometries;
    }

    public List<Interaction> getActions() {
        return _actions;
    }

    public double getClickDistance() {
        return _clickDistance;
    }

    public void setClickDistance(double clickDistance) {
        _clickDistance = clickDistance;
    }

    public boolean isViewDistance() {
        return _viewDistance;
    }

    public void setViewDistance(boolean viewDistance) {
        _viewDistance = viewDistance;
    }

    public boolean isAllowInterior() {
        return _allowInterior;
    }

    public void setAllowInterior(boolean _allowInterior) {
        this._allowInterior = _allowInterior;
    }

    public Interaction findAction(int button, boolean ctrl, boolean shift, boolean alt) {
        for (Interaction action : _actions) {
            if (action.isTriggeredBy(button, ctrl, shift, alt)) {
                return action;
            }
        }
        return null;
    }

    public TBase findGeometry(Vector loc, double viewToWorld) {

        double threshold = (_viewDistance ? viewToWorld : 1) * _clickDistance;
        for (TBase geom : _geometries) {
            if (geom.distanceTo(loc) <= threshold
                    || (_allowInterior && geom.getGeometryType().isCyclic()
                    && ((CyclicGeometry) geom).contains(loc))) {
                return geom;
            }
        }
        return null;
    }

    public void addGeometries(Iterable<TBase> geometries) {
        for (TBase geom : geometries) {
            _geometries.add(geom);
        }
    }

    public void addGeometries(TBase... geometries) {
        for (TBase geom : geometries) {
            _geometries.add(geom);
        }
    }

    public static <TBase extends BaseGeometry<TBase>> InteractiveLayer<TBase> createTranslateLayer(boolean immediate, double clickDistance, boolean screenDistance, boolean allowInterior) {
        return new InteractiveLayer(clickDistance, screenDistance, allowInterior,
                new TranslateInteraction(immediate));
    }

    public static <TBase extends BaseGeometry<TBase>> InteractiveLayer<TBase> createRotateLayer(Vector center, boolean immediate, double clickDistance, boolean screenDistance, boolean allowInterior) {
        return new InteractiveLayer(clickDistance, screenDistance, allowInterior,
                new RotateInteraction(center, immediate));
    }

    public static <TBase extends BaseGeometry<TBase>> InteractiveLayer<TBase> createScaleLayer(Vector center, boolean immediate, double clickDistance, boolean screenDistance, boolean allowInterior) {
        return new InteractiveLayer(clickDistance, screenDistance, allowInterior,
                new ScaleInteraction(center, immediate));
    }

    public static <TBase extends BaseGeometry<TBase>> InteractiveLayer<TBase> createRigidLayer(Vector center, boolean immediate, double clickDistance, boolean screenDistance, boolean allowInterior) {
        return new InteractiveLayer(clickDistance, screenDistance, allowInterior,
                new TranslateInteraction(immediate),
                new RotateInteraction(center, immediate));
    }

    public static <TBase extends BaseGeometry<TBase>> InteractiveLayer<TBase> createSimilarityLayer(Vector center, boolean immediate, double clickDistance, boolean screenDistance, boolean allowInterior) {
        return new InteractiveLayer(clickDistance, screenDistance, allowInterior,
                new TranslateInteraction(immediate),
                new RotateInteraction(center, immediate),
                new ScaleInteraction(center, immediate));
    }
}
