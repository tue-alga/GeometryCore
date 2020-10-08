/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.sidepanel;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface ComboTabItem {

    public String toString();

    public void createGUI(SideTab tab);
}
