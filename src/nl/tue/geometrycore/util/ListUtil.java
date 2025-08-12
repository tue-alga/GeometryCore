/*
 * GeometryCore library   
 * Copyright (C) 2025   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

import java.util.List;
import nl.tue.geometrycore.datastructures.priorityqueue.Indexable;

/**
 * Utility class with convenience methods for dealing with lists.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ListUtil {

    /**
     * Removes the element at the specified index from the list, by replacing it
     * with the last element. The last element is returned, now newly placed at
     * the given index. If the specified index is the last element, null is
     * returned instead. Note that any indices are not updated automatically.
     *
     * @param <T> Type of objects in the list
     * @param index The index of the object to be removed
     * @param list The list to remove the object from
     * @return The element that replaced the specified index, or null
     */
    public static <T> T swapRemove(int index, List<T> list) {
        int lastindex = list.size() - 1;
        T last = list.remove(lastindex);
        if (index == lastindex) {
            return null;
        }
        list.set(index, last);
        return last;
    }

    /**
     * Removes the indexed element from the list, by replacing it with the last
     * element. The last element is returned, now newly placed at the given
     * index. If the specified index is the last element, null is returned
     * instead. The index of the specified element is set to -1, and the index
     * of the last element is updated to reflect its new position.
     *
     * @param <T> Type of objects in the list, should implement the Indexable
     * interface
     * @param elt The element to be removed
     * @param list The list to remove the element from
     * @return The element that replaced the specified element, or null
     */
    public static <T extends Indexable> T swapRemove(T elt, List<T> list) {
        final int index = elt.getIndex();

        assert list.get(index) == elt;

        T last = swapRemove(index, list);
        if (last != null) {
            last.setIndex(index);
        }
        elt.setIndex(-1);
        return last;
    }

    /**
     * Inserts an element at the end of the list and sets its index accordingly.
     *
     * @param <T> Type of objects in the list, should implement the Indexable
     * interface
     * @param elt The element to be inserted
     * @param list The list to insert the element into
     */
    public static <T extends Indexable> void insert(T elt, List<T> list) {
        assert elt.getIndex() < 0;

        elt.setIndex(list.size());
        list.add(elt);
    }
}
