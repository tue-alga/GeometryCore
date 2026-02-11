package nl.tue.geometrycore.gui.debug;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
class DebugView {

    private final int _index;
    private final String[] _visible;

    DebugView(int index, String[] visible) {
        _index = index;
        _visible = visible;
    }

    int getIndex() {
        return _index;
    }

    String[] getVisible() {
        return _visible;
    }
}
