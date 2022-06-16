/*
 * GeometryCore library   
 * Copyright (C) 2022   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry;

/**
 * Simple interface to allow easier processing of objects with different
 * geometric representations.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface GeometryExtractor<TObject, TGeom extends GeometryConvertable> {

    public TGeom extract(TObject object);
}
