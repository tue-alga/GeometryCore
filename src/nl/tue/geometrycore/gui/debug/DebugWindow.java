package nl.tue.geometrycore.gui.debug;

import nl.tue.geometrycore.geometryrendering.CachedRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.geometryrendering.GeometryRenderer;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.glyphs.PointStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.FontStyle;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.geometrycore.gui.FrameLogo;
import nl.tue.geometrycore.gui.debug.DebugPage.DebugView;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;
import nl.tue.geometrycore.io.ipe.IPEWriter;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DebugWindow implements GeometryRenderer<CachedRenderer> {

    private final String name;
    private final List<DebugPage> pages = new ArrayList();
    private JFrame frame;
    private JFileChooser chooser;
    private DebugTabs side;
    private DebugDraw draw;
    private int show_page = 0, show_view = 0;
    private boolean auto_last_page = true;
    private Object waitlock = null;
    private boolean waiting = false;

    public DebugWindow() {
        this("Debugger");
    }

    public DebugWindow(String name) {
        this.name = name;
    }

    public void clearPages() {
        pages.clear();
        show_page = 0;
    }

    public DebugPage lastPage() {
        if (pages.isEmpty()) {
            return addPage();
        } else {
            return pages.get(pages.size() - 1);
        }
    }

    public int getPageCount() {
        return pages.size();
    }

    public DebugPage getPage(int page) {
        return pages.get(page);
    }

    public DebugPage addPage() {
        return addPage("Page " + (pages.size() + 1));
    }

    public DebugPage addPage(String name) {
        if (auto_last_page) {
            show_page = pages.size();
        }

        DebugPage p = new DebugPage(name);
        pages.add(p);
        updateGUI();
        return p;
    }

    public DebugView addView(String... layers) {
        return lastPage().addView(layers);
    }

    private void ensureFrame() {
        if (frame != null) {
            return;
        }

        frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());
        frame.setMinimumSize(new Dimension(300, 300));
        try {
            frame.setIconImages(FrameLogo.GEOMETRYCORE.getLogos());
        } catch (IOException ex) {
            Logger.getLogger(DebugWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        side = new DebugTabs();
        draw = new DebugDraw();
        frame.add(side, BorderLayout.WEST);
        frame.add(draw, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                hide();
                confirmContinue();
            }
        });
    }

    private void updateGUI() {
        if (frame != null && frame.isVisible()) {
            side.refreshTabs();
            draw.repaint();
        }
    }

    public void show() {
        ensureFrame();

        frame.setVisible(true);
    }

    public void hide() {
        if (frame != null) {
            frame.setVisible(false);
        }
    }

    public void confirmContinue() {
        if (waiting && waitlock != null) {
            synchronized (waitlock) {
                waiting = false;
                waitlock.notify();
            }
        }
    }

    public void waitForContinue() {
        show();

        waitlock = new Object();
        waiting = true;

        side.refreshTabs();

        synchronized (waitlock) {
            while (waiting) {
                try {
                    waitlock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(DebugWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        waitlock = null;
        waiting = false;
    }

    public void saveLastPage() {
        if (getPageCount() == 0) {
            return;
        }

        if (chooser == null) {
            chooser = new JFileChooser(".");
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("IPE files", "ipe"));
        }

        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            saveLastPage(chooser.getSelectedFile());
        }
    }

    public void saveLastPage(File f) {
        if (getPageCount() == 0) {
            return;
        }

        try (IPEWriter write = IPEWriter.fileWriter(f)) {
            write.initialize();

            DebugPage page = lastPage();
            writePage(write, page);
        } catch (IOException ex) {
            Logger.getLogger(DebugWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveShownPage() {
        if (show_page >= getPageCount()) {
            return;
        }

        if (chooser == null) {
            chooser = new JFileChooser(".");
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("IPE files", "ipe"));
        }

        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            saveShownPage(chooser.getSelectedFile());
        }
    }

    public void saveShownPage(File f) {
        if (show_page >= getPageCount()) {
            return;
        }

        try (IPEWriter write = IPEWriter.fileWriter(f)) {
            write.initialize();

            DebugPage page = getPage(show_page);
            writePage(write, page);
        } catch (IOException ex) {
            Logger.getLogger(DebugWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveAllPages() {
        if (getPageCount() == 0) {
            return;
        }

        if (chooser == null) {
            chooser = new JFileChooser(".");
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("IPE files", "ipe"));
        }

        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            saveAllPages(chooser.getSelectedFile());
        }
    }

    public void saveAllPages(File f) {
        if (getPageCount() == 0) {
            return;
        }

        try (IPEWriter write = IPEWriter.fileWriter(f)) {
            write.initialize();

            for (DebugPage page : pages) {
                writePage(write, page);
            }
        } catch (IOException ex) {
            Logger.getLogger(DebugWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writePage(IPEWriter write, DebugPage page) {
        String[] layers = page.collectLayers().toArray(new String[0]);
        if (layers.length > 0) {
            write.newPage(layers);

            if (page.hasViews()) {
                for (DebugView v : page.getViews()) {
                    write.newView(v.getLayers());
                }
            }
        } else {
            write.newPage("default");
            write.setLayer("default");
        }

        page.renderTo(write);
    }

    private class DebugTabs extends TabbedSidePanel {

        private final SideTab pagetab;

        public DebugTabs() {
            pagetab = addTab("Pages");
            refreshTabs();
        }

        void refreshTabs() {
            refreshPageTab();
        }

        void refreshPageTab() {
            pagetab.clearTab();

            int pc = getPageCount();
            DebugPage p = show_page < pc ? pages.get(show_page) : null;

            if (pc == 1) {
                pagetab.addLabel("1 page available");
            } else {
                pagetab.addLabel(pc + " pages available");
            }

            pagetab.makeSplit(2, 2);
            pagetab.addLabel("Page:");
            if (pc == 0) {
                pagetab.addLabel("---");
            } else {
                pagetab.addIntegerSpinner(show_page + 1, 1, pc, 1, (e, v) -> {
                    show_page = v - 1;
                    draw.repaint();
                });
            }

            pagetab.makeSplit(2, 2);
            pagetab.addLabel("View:");
            if (p == null || !p.hasViews()) {
                pagetab.addLabel("---");
            } else {
                pagetab.addIntegerSpinner(show_view+1, 1, p.getViews().size(), 1, (e, v) -> {
                    show_view = v - 1;
                    draw.repaint();
                });
            }

            pagetab.addCheckbox("Automatically go to latest page", auto_last_page, (e, v) -> auto_last_page = v);

            pagetab.addButton("Save current page", (e) -> saveShownPage());
            pagetab.addButton("Save all pages", (e) -> saveAllPages());

            if (waiting) {
                pagetab.addButton("Continue", (e) -> confirmContinue());
            }
            pagetab.addButton("Abort", (e) -> {
                System.out.println("Aborting");
                System.exit(1);
            });

            pagetab.invalidate();
        }
    }

    private class DebugDraw extends GeometryPanel {

        @Override
        protected void drawScene() {
            if (show_page >= getPageCount()) {
                return;
            }

            DebugPage p = getPage(show_page);
            String[] layers;
            if (p.hasViews()) {
                layers = p.getViews().get(show_view).getLayers();
                p.renderLayersTo(this, layers);
            } else {
                layers = null;
                p.renderTo(this);
            }

            double ts = 12;
            double th = 14;
            double m = 4;
            double tw = 125;
            int rows = layers == null ? 1 : (layers.length + 1);

            setSizeMode(SizeMode.VIEW);
            Rectangle R = Rectangle.byCorners(
                    convertViewToWorld(new Vector(0, getHeight())),
                    convertViewToWorld(new Vector(tw, getHeight() - (rows +1) * th))
            );
            setStroke(Color.black, 1, Dashing.SOLID);
            setFill(Color.white, Hashures.SOLID);
            setTextStyle(TextAnchor.LEFT, ts);
            draw(R);
            pushClipping(R);
            draw(convertViewToWorld(new Vector(m, getHeight() - th)), p.getName());
            if (layers != null) {
                for (int i = 0; i < layers.length; i++) {
                    draw(convertViewToWorld(new Vector(m, getHeight() - th * (i + 2))), "- "+layers[i]);
                }
            }
            popClipping();
        }

        @Override
        public Rectangle getBoundingRectangle() {
            return getPage(show_page).determineBoundingBox();
        }

        @Override
        protected void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {

        }

        @Override
        protected void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {
            switch (keycode) {
                case KeyEvent.VK_RIGHT: {
                    if (show_page < pages.size()) {
                        DebugPage p = pages.get(show_page);
                        if (p.hasViews() && show_view < p.getViews().size() - 1) {
                            show_view++;
                            updateGUI();
                        }
                    }
                    // else, try to increase page (VK_UP rollover)
                }
                case KeyEvent.VK_UP: {
                    if (show_page < pages.size() - 1) {
                        show_page++;
                        if (!shift) {
                            show_view = 0;
                        } else {
                            DebugPage p = pages.get(show_page);
                            if (p.hasViews() && show_view >= p.getViews().size()) {
                                show_view = p.getViews().size() - 1;
                            } // else: keep the same view
                        }
                        updateGUI();
                    }
                    break;
                }
                case KeyEvent.VK_LEFT: {
                    if (show_view > 0) {
                        show_view--;
                        updateGUI();
                        break;
                    }
                    // else, try to reduce page (VK_DOWN rollover)
                }
                case KeyEvent.VK_DOWN: {
                    if (show_page > 0) {
                        show_page--;
                        DebugPage p = pages.get(show_page);
                        if (!shift) {
                            show_view = p.hasViews() ? p.getViews().size() - 1 : 0;
                        } else {
                            if (p.hasViews() && show_view >= p.getViews().size()) {
                                show_view = p.getViews().size() - 1;
                            } // else: keep the same view
                        }
                        updateGUI();
                    }
                    break;
                }

            }
        }
    }

    @Override
    public void setAlpha(double alpha) {
        lastPage().setAlpha(alpha);
    }

    @Override
    public void setTextStyle(TextAnchor anchor, double textsize) {
        lastPage().setTextStyle(anchor, textsize);
    }

    @Override
    public void setTextStyle(TextAnchor anchor, double textsize, FontStyle fontstyle) {
        lastPage().setTextStyle(anchor, textsize, fontstyle);
    }

    @Override
    public void setSizeMode(SizeMode sizeMode) {
        lastPage().setSizeMode(sizeMode);
    }

    @Override
    public void setStroke(Color color, double strokewidth, Dashing dash) {
        lastPage().setStroke(color, strokewidth, dash);
    }

    @Override
    public void setFill(Color color, Hashures hash) {
        lastPage().setFill(color, hash);
    }

    @Override
    public void setPointStyle(PointStyle pointstyle, double pointsize) {
        lastPage().setPointStyle(pointstyle, pointsize);
    }

    @Override
    public void setBackwardArrowStyle(ArrowStyle arrow, double arrowsize) {
        lastPage().setBackwardArrowStyle(arrow, arrowsize);
    }

    @Override
    public void setForwardArrowStyle(ArrowStyle arrow, double arrowsize) {
        lastPage().setForwardArrowStyle(arrow, arrowsize);
    }

    @Override
    public void pushMatrix(AffineTransform transform) {
        lastPage().pushMatrix(transform);
    }

    @Override
    public void popMatrix() {
        lastPage().popMatrix();
    }

    @Override
    public void pushClipping(GeometryConvertable geometry) {
        lastPage().pushClipping(geometry);
    }

    @Override
    public void popClipping() {
        lastPage().popClipping();
    }

    @Override
    public void pushGroup() {
        lastPage().pushGroup();
    }

    @Override
    public void popGroup() {
        lastPage().popGroup();
    }

    @Override
    public void draw(Collection<? extends GeometryConvertable> geometries) {
        lastPage().draw(geometries);
    }

    @Override
    public void draw(GeometryConvertable... geometries) {
        lastPage().draw(geometries);
    }

    @Override
    public void draw(Vector location, String text) {
        lastPage().draw(location, text);
    }

    @Override
    public CachedRenderer getRenderObject() {
        return lastPage();
    }
}
