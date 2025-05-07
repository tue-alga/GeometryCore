/*
 * GeometryCore library   
 * Copyright (C) 2025   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.quadtree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * A simple base class for a data structure maintaining a set of geometric
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
public abstract class GeometryStore<T extends GeometryConvertable> {

    /**
     * Inserts the given geometric object into the data structure.
     *
     * @param elt The geometry to add
     */
    public abstract void insert(T elt);

    /**
     * Removes the given geometric object from the data structure.
     *
     * @param elt The geometry to remove
     * @return true iff the element was found
     */
    public abstract boolean remove(T elt);

    /**
     * Finds all objects that are "stabbed" by the given point. A geometric
     * object is stabbed, if the point is (approximately) equal for point
     * objects, if the point is (approximately) on the boundary for any nonpoint
     * object, or if the point is in the interior (for cyclic geometries).
     *
     * @param point The point that is to stab the intended objects
     * @return List of stabbed geometries
     */
    public List<T> findStabbed(Vector point) {
        return findStabbed(point, DoubleUtil.EPS);
    }

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
    public List<T> findStabbed(Vector point, double precision) {
        List<T> result = new ArrayList();
        findStabbed(point, precision, (e) -> result.add(e));
        return result;
    }

    /**
     * Finds all objects that are "stabbed" by the given point.A geometric
     * object is stabbed, if the point is (approximately) equal for point
     * objects, if the point is (approximately) on the boundary for any nonpoint
     * object, or if the point is in the interior (for cyclic geometries).
     *
     * @param point The point that is to stab the intended objects
     * @param action The action to be taken for each object found
     */
    public void findStabbed(Vector point, Consumer<? super T> action) {
        findStabbed(point, DoubleUtil.EPS, action);
    }

    /**
     * Finds all objects that are "stabbed" by the given point. A geometric
     * object is stabbed, if the point is (approximately) equal for point
     * objects, if the point is (approximately) on the boundary for any nonpoint
     * object, or if the point is in the interior (for cyclic geometries).
     *
     * @param point The point that is to stab the intended objects
     * @param precision The precision of the geometric tests
     * @param action The action to be taken for each object found
     */
    public abstract void findStabbed(Vector point, double precision, Consumer<? super T> action);

    /**
     * Finds all objects that are fully contained in the given rectangle. Note
     * that a negative precision can be used to exclude the boundary.
     *
     * @param rect The rectangle that is to contain the intended objects
     * @return List of contained geometries
     */
    public List<T> findContained(Rectangle rect) {
        return findContained(rect, DoubleUtil.EPS);
    }

    /**
     * Finds all objects that are fully contained in the given rectangle. Note
     * that a negative precision can be used to exclude the boundary.
     *
     * @param rect The rectangle that is to contain the intended objects
     * @param precision The precision of the geometric tests
     * @return List of contained geometries
     */
    public List<T> findContained(Rectangle rect, double precision) {
        List<T> result = new ArrayList();
        findContained(rect, precision, (e) -> result.add(e));
        return result;
    }

    /**
     * Finds all objects that are fully contained in the given rectangle. Note
     * that a negative precision can be used to exclude the boundary.
     *
     * @param rect The rectangle that is to contain the intended objects
     * @param action The action to be taken for each object found
     */
    public void findContained(Rectangle rect, Consumer<? super T> action) {
        findContained(rect, DoubleUtil.EPS, action);
    }

    /**
     * Finds all objects that are fully contained in the given rectangle. Note
     * that a negative precision can be used to exclude the boundary.
     *
     * @param rect The rectangle that is to contain the intended objects
     * @param precision The precision of the geometric tests
     * @param action The action to be taken for each object found
     */
    public abstract void findContained(Rectangle rect, double precision, Consumer<? super T> action);

    /**
     * Finds all objects that are fully contained in the given convex polygon.
     *
     * @param convex The convex polygon that is to contain the intended objects
     * @return List of contained geometries
     */
    public List<T> findContained(Polygon convex) {
        return findContained(convex, DoubleUtil.EPS);
    }

    /**
     * Finds all objects that are fully contained in the given convex polygon.
     *
     * @param convex The convex polygon that is to contain the intended objects
     * @param precision The precision of the geometric tests
     * @return List of contained geometries
     */
    public List<T> findContained(Polygon convex, double precision) {
        List<T> result = new ArrayList();
        findContained(convex, precision, (e) -> result.add(e));
        return result;
    }

    /**
     * Finds all objects that are fully contained in the given convex polygon.
     *
     * @param convex The convex polygon that is to contain the intended objects
     * @param action The action to be taken for each object found
     */
    public void findContained(Polygon convex, Consumer<? super T> action) {
        findContained(convex, DoubleUtil.EPS, action);
    }

    /**
     * Finds all objects that are fully contained in the given convex polygon.
     *
     * @param convex The convex polygon that is to contain the intended objects
     * @param precision The precision of the geometric tests
     * @param action The action to be taken for each object found
     */
    public abstract void findContained(Polygon convex, double precision, Consumer<? super T> action);

    /**
     * Finds all objects that overlap in the given rectangle. For cyclic
     * geometries, the interior is to be included in the overlap. For example, a
     * polygon that fully contains the given rectangle is also to be returned.
     *
     * @param rect The rectangle that is to overlap the intended objects
     * @return List of overlapping geometries
     */
    public List<T> findOverlapping(Rectangle rect) {
        return findOverlapping(rect, DoubleUtil.EPS);
    }

    /**
     * Finds all objects that overlap in the given rectangle. For cyclic
     * geometries, the interior is to be included in the overlap. For example, a
     * polygon that fully contains the given rectangle is also to be returned.
     *
     * @param rect The rectangle that is to overlap the intended objects
     * @param precision The precision of the geometric tests
     * @return List of overlapping geometries
     */
    public List<T> findOverlapping(Rectangle rect, double precision) {
        List<T> result = new ArrayList();
        findOverlapping(rect, precision, (e) -> result.add(e));
        return result;
    }

    /**
     * Finds all objects that overlap in the given rectangle. For cyclic
     * geometries, the interior is to be included in the overlap. For example, a
     * polygon that fully contains the given rectangle is also to be returned.
     *
     * @param rect The rectangle that is to overlap the intended objects
     * @param action The action to be taken for each object found
     */
    public void findOverlapping(Rectangle rect, Consumer<? super T> action) {
        findOverlapping(rect, DoubleUtil.EPS, action);
    }

    /**
     * Finds all objects that overlap in the given rectangle. For cyclic
     * geometries, the interior is to be included in the overlap. For example, a
     * polygon that fully contains the given rectangle is also to be returned.
     *
     * @param rect The rectangle that is to overlap the intended objects
     * @param precision The precision of the geometric tests
     * @param action The action to be taken for each object found
     */
    public abstract void findOverlapping(Rectangle rect, double precision, Consumer<? super T> action);

    /**
     * Finds all objects that overlap in the given convex polygon. For cyclic
     * geometries, the interior is to be included in the overlap. For example, a
     * polygon that fully contains the given convex polygon is also to be
     * returned.
     *
     * @param convex The convex polygon that is to contain the intended objects
     * @return List of overlapping geometries
     */
    public List<T> findOverlapping(Polygon convex) {
        return findOverlapping(convex, DoubleUtil.EPS);
    }

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
    public List<T> findOverlapping(Polygon convex, double precision) {
        List<T> result = new ArrayList();
        findOverlapping(convex, precision, (e) -> result.add(e));
        return result;
    }

    /**
     * Finds all objects that overlap in the given convex polygon. For cyclic
     * geometries, the interior is to be included in the overlap. For example, a
     * polygon that fully contains the given convex polygon is also to be
     * returned.
     *
     * @param convex The convex polygon that is to contain the intended objects
     * @param action The action to be taken for each object found
     */
    public void findOverlapping(Polygon convex, Consumer<? super T> action) {
        findOverlapping(convex, DoubleUtil.EPS, action);
    }

    /**
     * Finds all objects that overlap in the given convex polygon. For cyclic
     * geometries, the interior is to be included in the overlap. For example, a
     * polygon that fully contains the given convex polygon is also to be
     * returned.
     *
     * @param convex The convex polygon that is to contain the intended objects
     * @param precision The precision of the geometric tests
     * @param action The action to be taken for each object found
     */
    public abstract void findOverlapping(Polygon convex, double precision, Consumer<? super T> action);

}
