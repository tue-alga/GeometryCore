package nl.tue.geometrycore.gui.debug;

import nl.tue.geometrycore.geometryrendering.CachedRenderer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DebugPage extends CachedRenderer {

    private String name;
    private List<DebugView> views = null;

    public DebugPage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public List<DebugView> getViews() {
        return views;
    }
    
    public boolean hasViews() {
        return views != null;
    }

    public DebugView addView(String... layers) {
        if (views == null) {
            views = new ArrayList();
        }
        DebugView v = new DebugView(layers);
        views.add(v);
        return v;
    }

    public void removeView(DebugView v) {
        views.remove(v);
    }

    public void clearViews() {
        views = null;
    }

    public class DebugView {

        private String[] layers;

        public DebugView(String[] layers) {
            this.layers = layers;
        }

        public String[] getLayers() {
            return layers;
        }

    }
}
