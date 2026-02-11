package nl.tue.geometrycore.gui.debug;

import nl.tue.geometrycore.geometryrendering.CachedRenderer;
import java.util.ArrayList;
import java.util.List;
import nl.tue.geometrycore.io.LayeredWriter;
import nl.tue.geometrycore.io.ViewsWriter;
import nl.tue.geometrycore.io.ipe.IPEWriter;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DebugPage extends CachedRenderer implements ViewsWriter, LayeredWriter {

    private final DebugRenderer _renderer;
    private final int _index;
    private final String _name;
    private final List<DebugView> _views = new ArrayList();

    DebugPage(DebugRenderer renderer, int index, String name) {
        _renderer = renderer;
        _index = index;
        _name = name;
    }

    public DebugRenderer getRenderer() {
        return _renderer;
    }

    public int getIndex() {
        return _index;
    }

    public String getName() {
        return _name;
    }

    public List<DebugView> getViews() {
        return _views;
    }

    public boolean hasViews() {
        return !_views.isEmpty();
    }

    public void notifyChange() {
        _renderer.notifyPageChange(this);
    }

    @Override
    public void newView(String... visible) {
        DebugView v = new DebugView(_views.size(), visible);
        _views.add(v);
        _renderer.notifyNewView(this);
    }

    public void renderToIPE(IPEWriter write) {
        write.newPage(collectLayersArray());
        for (DebugView view : _views) {
            write.newView(view.getVisible());
        }
        renderTo(write);
    }

    void renderToIPE(IPEWriter write, DebugView view) {
        write.newPage(collectLayersArray());
        write.newView(view.getVisible());
        renderViewTo(write, view.getVisible());
    }

}
