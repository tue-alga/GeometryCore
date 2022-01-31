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
public abstract class RedBlackTreeItem<TItem extends RedBlackTreeItem<TItem>> {

    // three structure
    RedBlackTree<TItem> _tree;
    TItem _parent, _left, _right;
    // this node
    boolean _black;
    double _key;
    // standard augmentations
    int _subtreeSize;

    public RedBlackTreeItem(double key) {
        _key = key;
    }

    public double getKey() {
        return _key;
    }

    public void setKey(double key) {
        _key = key;
        if (_tree != null) {
            _tree.keyChanged((TItem) this);
        }
    }

    public int getRank() {
        int r = getLeftSubtreeSize();
        RedBlackTreeItem<TItem> prev = this;
        TItem walk = _parent;
        while (walk != null) {
            if (prev == walk._right) {
                r += walk.getLeftSubtreeSize() + 1;
            }
            prev = walk;
            walk = walk._parent;
        }
        return r;
    }

    public TItem getSuccessor() {
        if (_right != null) {
            // walk downwards
            TItem walk = _right;
            while (walk._left != null) {
                walk = walk._left;
            }
            return walk;
        } else {
            // walk upwards
            RedBlackTreeItem<TItem> walk = this;
            while (walk._parent != null && walk._parent._right == walk) {
                walk = walk._parent;
            }
            return walk._parent;
        }
    }

    public TItem getPredecessor() {
        if (_left != null) {
            // walk downwards
            TItem walk = _left;
            while (walk._right != null) {
                walk = walk._right;
            }
            return walk;
        } else {
            // walk upwards
            RedBlackTreeItem<TItem> walk = this;
            while (walk._parent != null && walk._parent._left == walk) {
                walk = walk._parent;
            }
            return walk._parent;
        }
    }

    /**
     * If not sure whether to return true or false, return true. (False gives
     * only an efficiency gain.)
     *
     * @return true if it requires further upward propagation after a rotation
     */
    public boolean updateAugmentedValues() {
        _subtreeSize = 1;
        if (_left != null) {
            _subtreeSize += _left._subtreeSize;
        }
        if (_right != null) {
            _subtreeSize += _right._subtreeSize;
        }
        return false;
    }

    public int getSubtreeSize() {
        return _subtreeSize;
    }

    public int getLeftSubtreeSize() {
        return _left == null ? 0 : _left._subtreeSize;
    }

    public int getRightSubtreeSize() {
        return _right == null ? 0 : _right._subtreeSize;
    }

    public TItem getLeft() {
        return _left;
    }

    public TItem getRight() {
        return _right;
    }

    public TItem getParent() {
        return _parent;
    }

    public boolean isBlack() {
        return _black;
    }

    public boolean inorder(RedBlackTreeItemAction<TItem> action) {
        if (_left != null) {
            if (!_left.inorder(action)) {
                return false;
            }
        }
        if (!action.process((TItem) this)) {
            return false;
        }
        if (_right != null) {
            if (!_right.inorder(action)) {
                return false;
            }
        }
        return true;
    }

    public boolean postorder(RedBlackTreeItemAction<TItem> action) {
        if (_left != null) {
            if (!_left.postorder(action)) {
                return false;
            }
        }
        if (_right != null) {
            if (!_right.postorder(action)) {
                return false;
            }
        }
        if (!action.process((TItem) this)) {
            return false;
        }
        return true;
    }

    public boolean preorder(RedBlackTreeItemAction<TItem> action) {

        if (!action.process((TItem) this)) {
            return false;
        }
        if (_left != null) {
            if (!_left.preorder(action)) {
                return false;
            }
        }
        if (_right != null) {
            if (!_right.preorder(action)) {
                return false;
            }
        }
        return true;
    }

}
