/*
 * GeometryCore library   
 * Copyright (C) 2021   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.datastructures.rbtree;

import nl.tue.geometrycore.util.DoubleUtil;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class RedBlackTree<TItem extends RedBlackTreeItem<TItem>> {

    private TItem _root = null;

    // queries
    public TItem getRoot() {
        return _root;
    }

    public void clear() {
        clear(true);
    }

    public void clear(boolean cleanup) {
        if (cleanup && _root != null) {
            _root.postorder((TItem item) -> {

                item._left = null;
                item._parent = null;
                item._right = null;
                item._tree = null;

                return true;
            });
        }
        _root = null;
    }

    public int size() {
        return _root == null ? 0 : _root._subtreeSize;
    }

    public TItem getMinimum() {
        if (_root == null) {
            return null;
        }
        TItem min = _root;
        while (min._left != null) {
            min = min._left;
        }
        return min;
    }

    public TItem getMaximum() {
        if (_root == null) {
            return null;
        }
        TItem max = _root;
        while (max._right != null) {
            max = max._right;
        }
        return max;
    }

    public TItem getItemByRank(int rank) {
        if (rank < 0 || rank >= size()) {
            return null;
        }
        TItem walk = _root;
        int current = walk.getLeftSubtreeSize();
        while (current != rank) {
            if (current > rank) {
                walk = walk._left;
                current--; // node itself
                current -= walk.getRightSubtreeSize();
            } else {
                walk = walk._right;
                current++; // node itself
                current += walk.getLeftSubtreeSize();
            }
        }
        return walk;
    }

    public TItem getItemWithKeyApproximately(double value) {
        return getItemWithKeyApproximately(value, DoubleUtil.EPS);
    }

    public TItem getItemWithKeyApproximately(double value, double eps) {
        TItem walk = _root;
        while (walk != null) {
            if (DoubleUtil.close(value, walk._key, eps)) {
                return walk;
            } else if (value < walk._key) {
                walk = walk._left;
            } else {
                walk = walk._right;
            }
        }
        return null;
    }

    public TItem getItemWithLowestKeyAtLeast(double min) {
        TItem walk = _root;
        TItem best = null;
        while (walk != null) {
            if (walk._key >= min) {
                best = walk;
                walk = walk._left;
            } else {
                walk = walk._right;
            }
        }
        return best;
    }

    public TItem getItemWithLowestKeyAtMost(double max) {
        TItem walk = _root;
        TItem best = null;
        while (walk != null) {
            if (walk._key <= max) {
                best = walk;
                walk = walk._right;
            } else {
                walk = walk._left;
            }
        }
        return best;
    }

    // modifications
    public void insert(TItem item) {
        assert item._tree == null : "Trying to add an item to a tree while it is already part of a tree";
        item._tree = this;

        TItem prev = null;
        TItem walk = _root;
        while (walk != null) {
            prev = walk;
            if (item.getKey() < walk.getKey()) {
                walk = walk.getLeft();
            } else {
                walk = walk.getRight();
            }
        }

        item._parent = prev;
        if (prev == null) {
            _root = item;
        } else if (item.getKey() < prev.getKey()) {
            prev._left = item;
        } else {
            prev._right = item;
        }

        item._left = null;
        item._right = null;
        item._black = false;

        updateAugmentedValues(item, true);

        insertFixUp(item);
    }

    private void updateAugmentedValues(TItem item, boolean force) {

        boolean propagate = true;

        while ((propagate || force) && item != null) {
            propagate = item.updateAugmentedValues();
            item = item._parent;
        }
    }

    private void insertFixUp(TItem item) {
        while (item._parent != null && !item._parent._black) {

            boolean left = item._parent == item._parent._parent._left;
            TItem y = getChild(item._parent._parent, !left);
            if (y != null && !y._black) {
                // case 1: recolor
                item._parent._black = true;
                y._black = true;
                item._parent._parent._black = false;
                item = item._parent._parent;
            } else {
                if (item == getChild(item._parent, !left)) {
                    // case 2
                    item = item._parent;
                    rotate(item, left);
                }
                // case 3
                item._parent._black = true;
                item._parent._parent._black = false;
                rotate(item._parent._parent, !left);
            }
        }
        _root._black = true;
    }

    private void rotate(TItem x, boolean left) {

        TItem y = getChild(x, !left);
        setChild(x, getChild(y, left), !left);
        y._parent = x._parent;
        if (x._parent == null) {
            _root = y;
        } else if (x == getChild(x._parent, left)) {
            setChild(x._parent, y, left);
        } else {
            setChild(x._parent, y, !left);
        }
        setChild(y, x, left);
        x._parent = y;

        x.updateAugmentedValues();
        updateAugmentedValues(y, false);
    }

    private void setChild(TItem parent, TItem child, boolean left) {
        if (left) {
            if (child == null) {
                parent._left = null;
            } else {
                parent._left = child;
                child._parent = parent;
            }
        } else {
            if (child == null) {
                parent._right = null;
            } else {
                parent._right = child;
                child._parent = parent;
            }
        }
    }

    private TItem getChild(TItem parent, boolean left) {
        return left ? parent._left : parent._right;
    }

    public void remove(TItem z) {

        assert z._tree == this : "Trying to remove an item from a tree that it is not contained in";
        z._tree = null;

        // since we're not storing an active nil object, we need to be a bit
        // careful here, and mimic having a nilpointer for x (and knowing what 
        // its parent is)
        TItem x, y, xp;
        boolean xleftofxp;
        boolean oldyblack;
        if (z._left != null && z._right != null) {
            // swap z with its successor
            y = z.getSuccessor();
            oldyblack = y._black;
            y._black = z._black;
            // NB: y cannot have a left child now
            TItem yp = y._parent;
            x = y._right;

            // make y take the place of z
            y._parent = z._parent;
            if (y._parent == null) {
                _root = y;
            } else if (y._parent._left == z) {
                y._parent._left = y;
            } else {
                y._parent._right = y;
            }
            y._left = z._left;
            if (y._left != null) {
                y._left._parent = y;
            }
            if (yp != z) {
                // change right child of y, and update x's parent
                y._right = z._right;
                if (y._right != null) {
                    y._right._parent = y;
                }

                // nb: y must have been left child of its parent   
                yp._left = x;
                if (x != null) {
                    x._parent = yp;
                }
                xp = yp;
                xleftofxp = true;
            } else {
                // just keep x as the right child of y
                xp = y;
                xleftofxp = false;
            }

        } else if (z._left != null || z._right != null) {
            y = z;
            oldyblack = z._black;
            if (z._left != null) {
                x = z._left;
            } else {
                x = z._right;
            }

            // splice out z
            x._parent = z._parent;
            if (z._parent == null) {
                _root = x;
                xleftofxp = true; // doesnt matter
            } else if (z._parent._left == z) {
                x._parent._left = x;
                xleftofxp = true;
            } else {
                x._parent._right = x;
                xleftofxp = false;
            }
            xp = x._parent;

        } else {
            // z has no children
            oldyblack = z._black;
            y = z;
            x = null;
            xp = z._parent;
            if (xp == null) {
                _root = null;
                xleftofxp = true; // doesnt matter
            } else if (xp._left == z) {
                xp._left = null;
                xleftofxp = true;
            } else {
                xp._right = null;
                xleftofxp = false;
            }
        }

        z._left = null;
        z._right = null;
        z._parent = null;

        updateAugmentedValues(xp, true);

        if (oldyblack && _root != null) {
            deleteFixUp(x, xp, xleftofxp);
        }

    }

    private void deleteFixUp(TItem x, TItem xp, boolean xleftofxp) {        
        if (x == null && xp != null) {
            // pretend x is a black nil pointers that is left  
            // (or right, depending on the provided boolean) of its parent
            boolean left = xleftofxp;
            TItem w = getChild(xp, !left);
            if (!w._black) {
                // case 1
                w._black = true;
                xp._black = false;
                rotate(xp, left);
                w = getChild(xp, !left);
            }

            if ((w._left == null || w._left._black) && (w._right == null || w._right._black)) {
                // case 2
                w._black = false;
                x = xp;
            } else {
                if (getChild(w, !left) == null || getChild(w, !left)._black) {
                    // case 3
                    getChild(w, left)._black = true;
                    w._black = false;
                    rotate(w, !left);
                    w = getChild(xp, !left);
                }
                // case 4
                w._black = xp._black;
                xp._black = true;
                if (getChild(w, !left) != null) {
                    getChild(w, !left)._black = true;
                }
                rotate(xp, left);
                x = _root;
            }
        }

        while (x != _root && x._black) {
            boolean left = x == x._parent._left;
            TItem w = getChild(x._parent, !left);
            if (!w._black) {
                // case 1
                w._black = true;
                x._parent._black = false;
                rotate(x._parent, left);
                w = getChild(x._parent, !left);
            }

            if ((w._left == null || w._left._black) && (w._right == null || w._right._black)) {
                // case 2
                w._black = false;
                x = x._parent;
            } else {
                if (getChild(w, !left) == null || getChild(w, !left)._black) {
                    // case 3
                    getChild(w, left)._black = true;
                    w._black = false;
                    rotate(w, !left);
                    w = getChild(x._parent, !left);
                }
                // case 4
                w._black = x._parent._black;
                x._parent._black = true;
                if (getChild(w, !left) != null) {
                    getChild(w, !left)._black = true;
                }
                rotate(x._parent, left);
                x = _root;
            }
        }
        x._black = true;
    }

    void keyChanged(TItem item) {
        remove(item);
        insert(item);
    }

    public boolean inorder(RedBlackTreeItemAction<TItem> action) {
        if (_root != null) {
            if (!_root.inorder(action)) {
                return false;
            }
        }
        return true;
    }

    public boolean preorder(RedBlackTreeItemAction<TItem> action) {
        if (_root != null) {
            if (!_root.preorder(action)) {
                return false;
            }
        }
        return true;
    }

    public boolean postorder(RedBlackTreeItemAction<TItem> action) {
        if (_root != null) {
            if (!_root.preorder(action)) {
                return false;
            }
        }
        return true;
    }
}
