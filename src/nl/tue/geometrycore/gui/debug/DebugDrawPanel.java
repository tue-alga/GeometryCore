package nl.tue.geometrycore.gui.debug;

import java.awt.event.KeyEvent;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.geometrycore.util.Pair;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
final class DebugDrawPanel extends GeometryPanel {

    private final DebugWindow _window;
    private boolean _firstActualDraw = true;

    DebugDrawPanel(DebugWindow window) {
        _window = window;
    }

    @Override
    protected void drawScene() {

        Pair<DebugPage, DebugView> pv = _window.getShown();
        if (pv == null) {
            return;
        }
        
        if (_firstActualDraw) {
            zoomToFit();
            _firstActualDraw = false;
        }

        DebugPage p = pv.getFirst();
        DebugView v = pv.getSecond();

        String[] layers;
        if (v != null) {
            layers = v.getVisible();
            p.renderViewTo(this, layers);
        } else {
            layers = new String[0];
            p.renderTo(this);
        }

        {
            double ts = 12;
            double th = 14;
            double m = 4;
            double tw = 125;
            int rows = layers.length + 1;

            setSizeMode(SizeMode.VIEW);
            Rectangle R = Rectangle.byCorners(
                    convertViewToWorld(new Vector(0, getHeight())),
                    convertViewToWorld(new Vector(tw, getHeight() - (rows + 1) * th))
            );
            setStroke(ExtendedColors.black, 1, Dashing.SOLID);
            setFill(ExtendedColors.white, Hashures.SOLID);
            setTextStyle(TextAnchor.LEFT, ts);
            draw(R);
            pushClipping(R);
            draw(convertViewToWorld(new Vector(m, getHeight() - th)), p.getName());
            for (int i = 0; i < layers.length; i++) {
                draw(convertViewToWorld(new Vector(m, getHeight() - th * (i + 2))), "- " + layers[i]);
            }
            popClipping();
        }

        if (_window.getRenderer().isWaiting()) {
            Rectangle R = Rectangle.byCorners(
                    convertViewToWorld(new Vector(0, 0)),
                    convertViewToWorld(new Vector(getWidth(), getHeight())));
            setStroke(ExtendedColors.darkRed, 3, Dashing.SOLID);
            setFill(null, Hashures.SOLID);
            draw(R);
        }
    }

    @Override
    public Rectangle getBoundingRectangle() {

        DebugPage p = _window.getShownPage();
        if (p == null) {
            return null;
        }
        return p.determineBoundingBox();
    }

    @Override
    protected void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {

    }

    @Override
    protected void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
        switch (keycode) {
            case KeyEvent.VK_RIGHT: {
                _window.nextView();
                break;
            }
            case KeyEvent.VK_LEFT: {
                _window.previousView();
                break;
            }
            case KeyEvent.VK_DOWN: {
                _window.nextPage(shift);
                break;
            }
            case KeyEvent.VK_UP: {
                _window.previousPage(shift);
                break;
            }
            case KeyEvent.VK_PAGE_UP: {
                if (ctrl) {
                    _window.previousPage(shift);
                } else {
                    _window.previousView();
                }
                break;
            }
            case KeyEvent.VK_PAGE_DOWN: {
                if (ctrl) {
                    _window.nextPage(shift);
                } else {
                    _window.nextView();
                }
                break;
            }
            case KeyEvent.VK_C: {
                if (_window.getRenderer().isWaiting()) {
                    _window.confirmContinue();
                }
                break;
            }

        }
    }
}
