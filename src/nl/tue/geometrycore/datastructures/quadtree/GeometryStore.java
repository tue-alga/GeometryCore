/*
 * GeometryCore library   
 * Copyright (C) 2025   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.quadtree;

import java.util.List;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 * A simple interface for a data structure maintaining a set of geometric
 * objects that can be searched for in various ways. The reference to the
 * objects are maintained, instead of making copies, and returned in the various
 * queries. Note that these data structures may assume that the objects do not
 * change their geometry. The user of the data structure is required to remove
 * objects before changes are made, and insert them again afterwards.
 *
 * The interface provides static functions for the tests used for the queries as
 * to allow consistent behavior between implementations.
 *
 * @param <T> The type of geometric objects to be stored.
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface GeometryStore<T extends GeometryConvertable> {

    /**
     * Inserts the given geometric object into the data structure.
     *
     * @param elt The geometry to add
     */
    public void insert(T elt);

    /**
     * Removes the given geometric object from the data structure.
     *
     * @param elt The geometry to add
     * @return true iff the element was found
     */
    public boolean remove(T elt);

    /**
     * Finds all objects that are "stabbed" by the given point. A geometric
     * object is stabbed, if the point is (approximately) equal for point
     * objects, if the point is (approximately) on the boundary for any nonpoint
     * object, or if the point is in the interior (for cyclic geometries).
     *
     * @param point The point that is to stab the intended objects
     * @param precision The precision of the geometric tests
     * @return List of stabbed geometries
     */
    public List<T> findStabbed(Vector point, double precision);

    /**
     * Finds all objects that are fully contained in the given rectangle. Note
     * that a negative precision can be used to exclude the boundary.
     *
     * @param rect The rectangle that is to contain the intended objects
     * @param precision The precision of the geometric tests
     * @return List of contained geometries
     */
    public List<T> findContained(Rectangle rect, double precision);

    /**
     * Finds all objects that are fully contained in the given convex polygon.
     *
     * @param convex The convex polygon that is to contain the intended objects
     * @param precision The precision of the geometric tests
     * @return List of contained geometries
     */
    public List<T> findContained(Polygon convex, double precision);

    /**
     * Finds all objects that overlap in the given rectangle. For cyclic
     * geometries, the interior is to be included in the overlap. For example, a
     * polygon that fully contains the given rectangle is also to be returned.
     *
     * @param rect The rectangle that is to overlap the intended objects
     * @param precision The precision of the geometric tests
     * @return List of overlapping geometries
     */
    public List<T> findOverlapping(Rectangle rect, double precision);

    /**
     * Finds all objects that overlap in the given convex polygon. For cyclic
     * geometries, the interior is to be included in the overlap. For example, a
     * polygon that fully contains the given convex polygon is also to be
     * returned.
     *
     * @param convex The convex polygon that is to contain the intended objects
     * @param precision The precision of the geometric tests
     * @return List of overlapping geometries
     */
    public List<T> findOverlapping(Polygon convex, double precision);

}
