/*
 * GeometryCore library   
 * Copyright (C) 2021   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.interactions;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface UndoRedo {

    public void undo();

    public void redo();
}
