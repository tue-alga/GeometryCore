package nl.tue.geometrycore.gui.debug;

import javax.swing.JSpinner;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;
import nl.tue.geometrycore.util.Pair;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
final class DebugSidePanel extends TabbedSidePanel {

    private final DebugRenderer _renderer;
    private final DebugWindow _window;
    private final SideTab _pagetab;
    private final SideTab _flowtab;

    DebugSidePanel(DebugWindow window) {
        _window = window;
        _renderer = window.getRenderer();

        _pagetab = addTab("Pages");
        refreshPageTab();

        _flowtab = addTab("Control");
        refreshFlowTab();
    }

    void refreshPageTab() {
        _pagetab.clearTab();

        int pc = _window.getPageCount();
        Pair<DebugPage, DebugView> pv = _window.getShown();

        if (pc == 1) {
            _pagetab.addLabel("1 page available");
        } else {
            _pagetab.addLabel(pc + " pages available");
        }

        _pagetab.makeSplit(2, 2);
        _pagetab.addLabel("Page:");
        if (pc == 0) {
            _pagetab.addLabel("---");
        } else {
            _pagetab.addIntegerSpinner(pv.getFirst().getIndex() + 1, 1, pc, 1, (e, v) -> {
                _window.setShownPage(v - 1);
            });
        }

        _pagetab.makeSplit(2, 2);
        _pagetab.addLabel("View:");
        if (pv == null || !pv.getFirst().hasViews()) {
            _pagetab.addLabel("---");
        } else {
            _pagetab.addIntegerSpinner(pv.getSecond().getIndex() + 1, 1, pv.getFirst().getViews().size(), 1, (e, v) -> {
                _window.setShownView(v - 1);
            });
        }

        _pagetab.addCheckbox("Automatically go to latest page", _window.isAutoLastPage(), (e, v) -> _window.setAutoLastPage(v));

        _pagetab.addButton("Save current view", (e) -> _window.saveShownView());
        _pagetab.addButton("Save current page", (e) -> _window.saveShownPage());
        _pagetab.addButton("Save all pages", (e) -> _renderer.saveAllPages());

    }

    void refreshFlowTab() {

        _flowtab.clearTab();

        _flowtab.addCheckbox("Auto-continue", _window.isAutoContinue(), (e, v) -> _window.setAutoContinue(v));

        _flowtab.addButton("Continue", (e) -> _window.confirmContinue())
                .setEnabled(_renderer.isWaiting());

        _flowtab.addButton("Abort", (e) -> {
            System.out.println("Aborting");
            System.exit(1);
        });

        _flowtab.invalidate();
    }

    void updatePageViewNumbers(int showPage, int showView) {
        refreshPageTab(); // TODO: update GUI more nicely...
    }
}
