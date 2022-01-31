/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.sidepanel;

import java.awt.event.ItemEvent;
import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 * A specific SideTab which is governed by a dropdown box of items that create
 * their own specific GUI elements below it. Can be provided with a set of
 * common elements that appear between the dropdown box and the specific
 * elements.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 * @param <TItem>
 */
public class ComboTab<TItem extends ComboTabItem> extends SideTab {

    private final TItem[] _items;
    private TItem _selected;
    private final JComboBox _combo;
    private JComponent _last;
    private boolean _commonMode = false;

    public ComboTab(String name, TabbedSidePanel partof, TItem[] items, TItem selected, NewValueListener<ItemEvent, TItem> listener) {
        super(name, partof);
        _items = items;
        _selected = selected == null ? items[0] : selected;

        _last = _combo = addComboBox(_items, _selected, (e, v) -> {
            _selected = v;
            refreshTab();
            if (listener != null) {
                listener.update(e, v);
            }
        });

        refreshTab();
    }

    /**
     * Starts common mode. Use this to add static elements between the dropdown
     * box and the specific elements. Make sure to end common mode. Has no
     * effect if common mode was already started.
     */
    public void startCommonMode() {
        if (!_commonMode) {
            _commonMode = true;
            revertUntil(_last);
        }
    }

    /**
     * Ends common mode. Has no effect if common mode was not started.
     */
    public void endCommonMode() {
        if (_commonMode) {
            _commonMode = false;
            refreshTab();
        }
    }

    @Override
    public void addComponent(JComponent comp, int eltWidth, int eltHeight) {
        super.addComponent(comp, eltWidth, eltHeight);
        if (_commonMode) {
            _last = comp;
        }
    }

    @Override
    public void addComponent(JComponent comp) {
        super.addComponent(comp);
        if (_commonMode) {
            _last = comp;
        }
    }

    @Override
    public void addComponent(JComponent comp, int height) {
        super.addComponent(comp, height);
        if (_commonMode) {
            _last = comp;
        }
    }

    public TItem getSelected() {
        return _selected;
    }

    public void setSelected(TItem selected) {
        _selected = selected;
        _combo.setSelectedItem(selected);
    }

    private void refreshTab() {

        revertUntil(_last);
        addSpace(4);
        _selected.createGUI(this);
    }

}
