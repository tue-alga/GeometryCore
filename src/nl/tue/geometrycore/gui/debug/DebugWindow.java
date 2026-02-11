package nl.tue.geometrycore.gui.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import nl.tue.geometrycore.gui.FrameLogo;
import nl.tue.geometrycore.util.Pair;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
final class DebugWindow extends JFrame {

    private final DebugRenderer _renderer;
    private final DebugSidePanel _side;
    private final DebugDrawPanel _draw;

    private int _showPage = 0, _showView = 0;
    private boolean _autoLastPage = false;
    private boolean _autoContinue = false;

    DebugWindow(DebugRenderer renderer) {
        super(renderer.getName());

        _renderer = renderer;
        _side = new DebugSidePanel(this);
        _draw = new DebugDrawPanel(this);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(300, 300));
        try {
            setIconImages(FrameLogo.GEOMETRYCORE.getLogos());
        } catch (IOException ex) {
            Logger.getLogger(DebugRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }

        add(_side, BorderLayout.WEST);
        add(_draw, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                renderer.hide();
                renderer.confirmContinue();
            }
        });
    }

    DebugRenderer getRenderer() {
        return _renderer;
    }

    int getPageCount() {
        if (_renderer.hasDrawPage()) {
            return _renderer.getPageCount() - 1;
        } else {
            return _renderer.getPageCount();
        }
    }

    DebugPage getShownPage() {
        if (_showPage >= getPageCount()) {
            return null;
        } else {
            return _renderer.getPage(_showPage);
        }
    }

    Pair<DebugPage, DebugView> getShown() {
        if (_showPage >= getPageCount()) {
            return null;
        }

        DebugPage p = _renderer.getPage(_showPage);
        if (p.hasViews()) {
            return new Pair(p, p.getViews().get(_showView));
        } else {
            return new Pair(p, null);
        }
    }

    void nextView() {
        if (_showPage >= getPageCount()) {
            return;
        }

        DebugPage p = _renderer.getPage(_showPage);
        if (!p.hasViews() || _showView >= p.getViews().size() - 1) {
            nextPage(false);
        } else {
            _showView++;
        }
        _draw.repaint();
        _side.updatePageViewNumbers(_showPage, _showView);
    }

    void previousView() {
        if (_showPage >= getPageCount()) {
            return;
        }

        DebugPage p = _renderer.getPage(_showPage);
        if (!p.hasViews() || _showView <= 0) {
            previousPage(false);
        } else {
            _showView--;
        }
        _draw.repaint();
        _side.updatePageViewNumbers(_showPage, _showView);
    }

    void nextPage(boolean keepView) {
        if (_showPage >= getPageCount() - 1) {
            return;
        }

        _showPage++;
        DebugPage p = _renderer.getPage(_showPage);
        if (!keepView || !p.hasViews() || _showView >= p.getViews().size()) {
            _showView = 0;
        }
        _draw.repaint();
        _side.updatePageViewNumbers(_showPage, _showView);
    }

    void previousPage(boolean keepView) {
        if (_showPage == 0) {
            return;
        }

        _showPage--;
        DebugPage p = _renderer.getPage(_showPage);
        if (!keepView || !p.hasViews() || _showView >= p.getViews().size()) {
            _showView = 0;
        }
        _draw.repaint();
        _side.updatePageViewNumbers(_showPage, _showView);
    }

    void setShownView(int index) {
        _showView = index;
        _draw.repaint();
        _side.updatePageViewNumbers(_showPage, _showView);
    }

    void setShownPage(int index) {
        _showPage = index;
        _showView = 0;
        _draw.repaint();
        _side.updatePageViewNumbers(_showPage, _showView);
    }

    void pageDoneNotice() {
        _side.refreshPageTab();
        if (_autoLastPage && _showPage < getPageCount() - 1) {
            _showPage = getPageCount() - 1;
            _draw.repaint();
            _side.updatePageViewNumbers(_showPage, _showView);
        }
    }

    void waitingNotice() {
        if (_autoContinue) {
            _renderer.confirmContinue();
        } else {
            _side.refreshFlowTab();
            _draw.repaint();
        }
    }

    void newViewNotice(DebugPage page) {
        _side.refreshPageTab();
    }

    void newPageNotice(DebugPage page) {
        _side.refreshPageTab();
        if (_autoLastPage && _showPage < getPageCount() - 1) {
            _showPage = getPageCount() - 1;
            _draw.repaint();
            _side.updatePageViewNumbers(_showPage, _showView);
        }
    }

    void pageChangeNotice(DebugPage page) {
        if (page.getIndex() == _showPage) {
            _draw.repaint();
        }
    }

    void saveShownPage() {
        DebugPage p = getShownPage();
        if (p != null) {
            _renderer.savePage(p);
        }
    }

    void saveShownView() {
        Pair<DebugPage, DebugView> pv = getShown();
        if (pv != null) {
            if (pv.getSecond() == null) {
                _renderer.savePage(pv.getFirst());
            } else {
                _renderer.saveView(pv.getFirst(), pv.getSecond());
            }
        }
    }

    void confirmContinue() {
        _renderer.confirmContinue();
        _side.refreshFlowTab();
        _draw.repaint();
    }

    boolean isAutoContinue() {
        return _autoContinue;
    }

    void setAutoContinue(boolean enable) {
        _autoContinue = enable;
    }

    boolean isAutoLastPage() {
        return _autoLastPage;
    }

    void setAutoLastPage(boolean enable) {
        _autoLastPage = enable;
    }

}
