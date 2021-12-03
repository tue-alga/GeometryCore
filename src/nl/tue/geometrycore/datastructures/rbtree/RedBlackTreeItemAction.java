/*
 * GeometryCore library   
 * Copyright (C) 2021   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.rbtree;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface RedBlackTreeItemAction<TItem extends RedBlackTreeItem<TItem>> {
    
    public boolean process(TItem item);
}
