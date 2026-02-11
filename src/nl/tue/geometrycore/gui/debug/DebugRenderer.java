package nl.tue.geometrycore.gui.debug;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometryrendering.GeometryRenderer;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.glyphs.PointStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.FontStyle;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.geometrycore.io.LayeredWriter;
import nl.tue.geometrycore.io.PagesWriter;
import nl.tue.geometrycore.io.ViewsWriter;
import nl.tue.geometrycore.io.ipe.IPEWriter;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DebugRenderer implements GeometryRenderer<DebugPage>, LayeredWriter, PagesWriter, ViewsWriter {

    private final String _name;
    private final List<DebugPage> _pages = new ArrayList();
    private DebugWindow _window;
    private JFileChooser _chooser;
    private Object _waitlock = null;
    private boolean _waiting = false;
    private DebugPage _drawPage;

    public DebugRenderer() {
        this("Debugger");
    }

    public DebugRenderer(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public DebugPage lastPage() {
        if (_pages.isEmpty()) {
            return addPage();
        } else {
            return _pages.get(_pages.size() - 1);
        }
    }

    public DebugPage drawPage() {
        if (_drawPage == null) {
            _drawPage = addPage();
        }
        return _drawPage;
    }

    public int getPageCount() {
        return _pages.size();
    }

    public DebugPage getPage(int page) {
        return _pages.get(page);
    }

    @Override
    public void newPage(String... layers) {
        addPage();
    }

    public DebugPage addPage() {
        return addPage("Page " + (_pages.size() + 1));
    }

    public DebugPage addPage(String name) {
        if (_drawPage != null) {
            notifyPageDone();
        }
        _drawPage = new DebugPage(this, _pages.size(), name);
        _pages.add(_drawPage);
        notifyNewPage(_drawPage);
        return _drawPage;
    }
    
    boolean hasDrawPage() {
        return _drawPage != null;
    }

    public void notifyPageDone() {
        _drawPage = null;
        if (_window != null) {
            _window.pageDoneNotice();
        }
    }

    private void ensureWindow() {
        if (_window != null) {
            return;
        }
        _window = new DebugWindow(this);
    }

    void notifyPageChange(DebugPage page) {
        if (_window != null) {
            _window.pageChangeNotice(page);
        }
    }

    void notifyNewPage(DebugPage page) {
        if (_window != null) {
            _window.newPageNotice(page);
        }
    }

    void notifyNewView(DebugPage page) {
        if (_window != null) {
            _window.newViewNotice(page);
        }
    }

    void notifyWaiting() {
        if (_window != null) {
            _window.waitingNotice();
        }
    }

    public void show() {
        ensureWindow();
        _window.setVisible(true);
    }

    public void hide() {
        if (_window != null) {
            _window.setVisible(false);
        }
    }

    boolean isWaiting() {
        return _waiting;
    }

    public void confirmContinue() {
        if (_waiting && _waitlock != null) {
            synchronized (_waitlock) {
                _waiting = false;
                _waitlock.notify();
            }
        }
    }

    public void waitForContinue() {
        show();

        _waitlock = new Object();
        _waiting = true;

        notifyWaiting();

        synchronized (_waitlock) {
            while (_waiting) {
                try {
                    _waitlock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(DebugRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        _waitlock = null;
        _waiting = false;
    }

    private File chooseFile() {
        if (_chooser == null) {
            _chooser = new JFileChooser(".");
            FileFilter filter = new FileNameExtensionFilter("IPE files", "ipe");
            _chooser.addChoosableFileFilter(filter);
            _chooser.setFileFilter(filter);
        }

        if (_chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            return _chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    public void savePage(DebugPage page) {
        File f = chooseFile();
        if (f != null) {
            savePage(page, f);
        }
    }

    public void savePage(DebugPage page, File f) {
        try (IPEWriter write = IPEWriter.fileWriter(f)) {
            write.initialize();
            page.renderToIPE(write);
        } catch (IOException ex) {
            Logger.getLogger(DebugRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void saveView(DebugPage page, DebugView view) {
        File f = chooseFile();
        if (f != null) {
            savePage(page, f);
        }
    }

    void saveView(DebugPage page, DebugView view, File f) {
        try (IPEWriter write = IPEWriter.fileWriter(f)) {
            write.initialize();
            page.renderToIPE(write);
        } catch (IOException ex) {
            Logger.getLogger(DebugRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveAllPages() {
        File f = chooseFile();
        if (f != null) {
            saveAllPages(f);
        }
    }

    public void saveAllPages(File f) {
        try (IPEWriter write = IPEWriter.fileWriter(f)) {
            write.initialize();
            for (DebugPage p : _pages) {
                p.renderToIPE(write);
            }
        } catch (IOException ex) {
            Logger.getLogger(DebugRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveLastPage() {
        if (getPageCount() == 0) {
            return;
        }

        savePage(lastPage());
    }

    public void saveLastPage(File f) {
        if (getPageCount() == 0) {
            return;
        }

        savePage(lastPage(), f);
    }

    @Override
    public void newView(String... layers) {
        drawPage().newView(layers);
    }

    @Override
    public void setAlpha(double alpha) {
        drawPage().setAlpha(alpha);
    }

    @Override
    public void setTextStyle(TextAnchor anchor, double textsize) {
        drawPage().setTextStyle(anchor, textsize);
    }

    @Override
    public void setTextStyle(TextAnchor anchor, double textsize, FontStyle fontstyle) {
        drawPage().setTextStyle(anchor, textsize, fontstyle);
    }

    @Override
    public void setSizeMode(SizeMode sizeMode) {
        drawPage().setSizeMode(sizeMode);
    }

    @Override
    public void setStroke(Color color, double strokewidth, Dashing dash) {
        drawPage().setStroke(color, strokewidth, dash);
    }

    @Override
    public void setFill(Color color, Hashures hash) {
        drawPage().setFill(color, hash);
    }

    @Override
    public void setPointStyle(PointStyle pointstyle, double pointsize) {
        drawPage().setPointStyle(pointstyle, pointsize);
    }

    @Override
    public void setBackwardArrowStyle(ArrowStyle arrow, double arrowsize) {
        drawPage().setBackwardArrowStyle(arrow, arrowsize);
    }

    @Override
    public void setForwardArrowStyle(ArrowStyle arrow, double arrowsize) {
        drawPage().setForwardArrowStyle(arrow, arrowsize);
    }

    @Override
    public void pushMatrix(AffineTransform transform) {
        drawPage().pushMatrix(transform);
    }

    @Override
    public void popMatrix() {
        drawPage().popMatrix();
    }

    @Override
    public void pushClipping(GeometryConvertable geometry) {
        drawPage().pushClipping(geometry);
    }

    @Override
    public void popClipping() {
        drawPage().popClipping();
    }

    @Override
    public void pushGroup() {
        drawPage().pushGroup();
    }

    @Override
    public void popGroup() {
        drawPage().popGroup();
    }

    @Override
    public void draw(Collection<? extends GeometryConvertable> geometries) {
        drawPage().draw(geometries);
    }

    @Override
    public void draw(GeometryConvertable... geometries) {
        drawPage().draw(geometries);
    }

    @Override
    public void draw(Vector location, String text) {
        drawPage().draw(location, text);
    }

    @Override
    public void setLayer(String layer) {
        drawPage().setLayer(layer);
    }

    @Override
    public DebugPage getRenderObject() {
        return drawPage();
    }
}
