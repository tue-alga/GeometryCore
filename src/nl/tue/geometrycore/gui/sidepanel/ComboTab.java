/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.gui.sidepanel;

import java.awt.event.ItemEvent;
import javax.swing.JComboBox;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 * @param <TItem>
 */
public class ComboTab<TItem extends ComboTabItem> extends SideTab {

    private TItem[] _items;
    private TItem _selected;
    private JComboBox _combo;

    public ComboTab(String name, TabbedSidePanel partof, TItem[] items, TItem selected, NewValueListener<ItemEvent,TItem> listener) {
        super(name, partof);
        _items = items;
        _selected = selected == null ? items[0] : selected;

        _combo = addComboBox(_items, _selected, (e, v) -> {
            _selected = v;
            refreshTab();
            if (listener != null) {
                listener.update(e, v);
            }
        });
                
        refreshTab();
    }

    public TItem getSelected() {
        return _selected;
    }

    public void setSelected(TItem selected) {
        _selected = selected;
        _combo.setSelectedItem(selected);
    }
    

    private void refreshTab() {

        revertUntil(_combo);
        addSpace(4);
        _selected.createGUI(this);
    }

}
