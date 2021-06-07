/*
 * GeometryCore library   
 * Copyright (C) 2021   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.interactions;

import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;

/**
 * Abstract interaction class. It has four settings to detect its trigger (which
 * mouse button and which modifiers) and can be configured to act "immediately",
 * that is, the actual geometry is already updated during the interaction, or
 * not. However, it is up to the implemented interactions to actually make this
 * distinction.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 * @param <TBase> The type of geometric objects contained in this layer
 */
public abstract class Interaction<TBase extends BaseGeometry<TBase>> {

    protected TBase _geometry = null;
    protected boolean _immediate;
    protected int _button;
    protected boolean _ctrl, _shift, _alt;

    /**
     * Constructs a new interaction with the given settings.
     *
     * @param immediate Should the interaction update the actual geometry while
     * it is occurring
     * @param button Which button (from MouseEvent) should be used to trigger
     * this interaction?
     * @param ctrl Should the Control key be pressed to trigger this
     * interaction?
     * @param shift Should the Shift key be pressed to trigger this interaction?
     * @param alt Should the Alt key be pressed to trigger this interaction?
     */
    public Interaction(boolean immediate, int button, boolean ctrl, boolean shift, boolean alt) {
        _immediate = immediate;
        _button = button;
        _ctrl = ctrl;
        _shift = shift;
        _alt = alt;
    }

    public boolean isImmediate() {
        return _immediate;
    }

    public void setImmediate(boolean immediate) {
        _immediate = immediate;
    }

    public int getButton() {
        return _button;
    }

    public void setButton(int button) {
        _button = button;
    }

    public boolean isCtrl() {
        return _ctrl;
    }

    public void setCtrl(boolean ctrl) {
        _ctrl = ctrl;
    }

    public boolean isShift() {
        return _shift;
    }

    public void setShift(boolean shift) {
        _shift = shift;
    }

    public boolean isAlt() {
        return _alt;
    }

    public void setAlt(boolean alt) {
        _alt = alt;
    }

    /**
     * Returns the geometric object that is currently being interacted with.
     * Returns null if this interaction is currently not being performed.
     *
     * @return pointer to the current geometry
     */
    public TBase getCurrentGeometry() {
        return _geometry;
    }

    /**
     * For immediate interactions, this just returns the current geometry.
     * Otherwise, it returns a clone of this geometry upon which the interaction
     * is already performed.
     *
     * @return
     */
    public abstract TBase getCurrentInteractedGeometry();

    /**
     * Returns some representation from the current interaction for rendering
     * the interaction.
     *
     * @return
     */
    public abstract BaseGeometry[] getCurrentInteractionRepresentation();

    /**
     * Sets the geometry upon which this interaction is currently being
     * performed. Mostly for internal use by the InteractiveGeometryPanel.
     *
     * @param geometry
     */
    public void setCurrentGeometry(TBase geometry) {
        assert _geometry == null : "Should not start an interaction before ending the previous one";
        _geometry = geometry;
    }

    /**
     * Tests whether this interaction is triggered by the given combination of
     * mouse button and modifiers. Mostly for internal use by the
     * InteractiveGeometryPanel.
     *
     * @param button
     * @param ctrl
     * @param shift
     * @param alt
     * @return
     */
    public boolean isTriggeredBy(int button, boolean ctrl, boolean shift, boolean alt) {
        return button == _button && ctrl == _ctrl && shift == _shift && alt == _alt;
    }

    /**
     * Starts the interaction. Note that the current geometry should be set to a
     * geometric object beforehand.
     *
     * @param loc the location of the mouse event
     */
    public abstract void startInteraction(Vector loc);

    /**
     * Updates the interaction. Note that the current geometry should be set to
     * a geometric object beforehand and startInteraction should be called
     * beforehand.
     *
     * @param loc the location of the mouse event
     * @param prevLoc the location of the previous mouse event
     */
    public abstract void updateInteraction(Vector loc, Vector prevLoc);

    /**
     * Terminates the interaction, creating an UndoRedo object that will be used
     * to either keep the new geometry or revert any changes made, depending on
     * what triggers the interaction.
     *
     * @return an object for the undo/redo stacks
     */
    public abstract UndoRedo endInteraction();

}
