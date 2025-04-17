/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.doublylinkedlist;

/**
 * Provides a simple wrapper around objects that cannot inherit from
 * DoublyLinkedListItem or that need to be in multiple lists.
 *
 * @param <TSelf> Class of the linked-list item
 * @param <TObj> Class of object to be stored with the list item
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ReferencedDLLItem<TSelf extends ReferencedDLLItem<TSelf,TObj>,TObj> extends DoublyLinkedListItem<TSelf> {

    private final TObj _object;

    public ReferencedDLLItem(TObj object) {
        _object = object;
    }

    public TObj getObject() {
        return _object;
    }
}
