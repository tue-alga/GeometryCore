package nl.tue.geometrycore.geometryrendering;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.glyphs.PointStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.FontStyle;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.geometrycore.io.LayeredWriter;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class CachedRenderer implements GeometryRenderer<Object>, LayeredWriter {

    private final List<Command> commands = new ArrayList();

    public void renderTo(GeometryRenderer target) {
        for (Command c : commands) {
            c.perform(target);
        }
    }

    public void renderLayersTo(GeometryRenderer target, String... layers) {
        boolean in_layer = false;
        for (Command c : commands) {
            switch (c.getType()) {
                case STYLING:
                    c.perform(target);
                    break;
                case PAGING:
                    String L = ((CmdSetLayer) c).layer;
                    in_layer = false;
                    for (String active : layers) {
                        if (active.equals(L)) {
                            in_layer = true;
                            break;
                        }
                    }
                    // NB: roll into next case
                case RENDERING:
                    if (in_layer) {
                        c.perform(target);
                    }
                    break;
            }
        }
    }

    public Rectangle determineBoundingBox() {
        Rectangle rect = new Rectangle();
        for (Command c : commands) {
            if (c instanceof CmdDraw) {
                rect.includeGeometry(((CmdDraw) c).geometries);
            } else if (c instanceof CmdDrawText) {
                rect.include(((CmdDrawText) c).location);
            }
        }
        return rect;
    }

    public List<String> collectLayers() {
        List<String> result = new ArrayList();
        cmdloop:
        for (Command c : commands) {
            if (c instanceof CmdSetLayer) {
                String L = ((CmdSetLayer) c).layer;
                for (String r : result) {
                    if (r.equals(L)) {
                        continue cmdloop;
                    }
                }
                result.add(L);
            }
        }
        return result;
    }

    @Override
    public void setAlpha(double alpha) {
        commands.add(new CmdSetAlpha(alpha));
    }

    @Override
    public void setTextStyle(TextAnchor anchor, double textsize) {
        commands.add(new CmdSetTextStyle(anchor, textsize, null));
    }

    @Override
    public void setTextStyle(TextAnchor anchor, double textsize, FontStyle fontstyle) {
        commands.add(new CmdSetTextStyle(anchor, textsize, fontstyle));
    }

    @Override
    public void setSizeMode(SizeMode sizeMode) {
        commands.add(new CmdSetSizeMode(sizeMode));
    }

    @Override
    public void setStroke(Color color, double strokewidth, Dashing dash) {
        commands.add(new CmdSetStroke(color, strokewidth, dash));
    }

    @Override
    public void setFill(Color color, Hashures hash) {
        commands.add(new CmdSetFill(color, hash));
    }

    @Override
    public void setPointStyle(PointStyle pointstyle, double pointsize) {
        commands.add(new CmdSetPointStyle(pointstyle, pointsize));
    }

    @Override
    public void setBackwardArrowStyle(ArrowStyle arrow, double arrowsize) {
        commands.add(new CmdSetBackwardArrowStyle(arrow, arrowsize));
    }

    @Override
    public void setForwardArrowStyle(ArrowStyle arrow, double arrowsize) {
        commands.add(new CmdSetForwardArrowStyle(arrow, arrowsize));
    }

    @Override
    public void pushMatrix(AffineTransform transform) {
        commands.add(new CmdPushMatrix(transform));
    }

    @Override
    public void popMatrix() {
        commands.add(new CmdPopMatrix());
    }

    @Override
    public void pushClipping(GeometryConvertable geometry) {
        commands.add(new CmdPushClipping(geometry));
    }

    @Override
    public void popClipping() {
        commands.add(new CmdPopClipping());
    }

    @Override
    public void pushGroup() {
        commands.add(new CmdPushGroup());
    }

    @Override
    public void popGroup() {
        commands.add(new CmdPopGroup());
    }

    @Override
    public void draw(Collection<? extends GeometryConvertable> geometries) {
        commands.add(new CmdDraw(geometries));
    }

    @Override
    public void draw(GeometryConvertable... geometries) {
        commands.add(new CmdDraw(geometries));
    }

    @Override
    public void draw(Vector location, String text) {
        commands.add(new CmdDrawText(location, text));
    }

    @Override
    public void setLayer(String layer) {
        commands.add(new CmdSetLayer(layer));
    }

    @Override
    public Object getRenderObject() {
        return null;
    }

    private static enum CommandType {
        STYLING,
        PAGING,
        RENDERING
    }

    private static abstract class Command {

        abstract void perform(GeometryRenderer target);

        abstract CommandType getType();
    }

    private static class CmdSetAlpha extends Command {

        final double alpha;

        CmdSetAlpha(double alpha) {
            this.alpha = alpha;
        }

        @Override
        void perform(GeometryRenderer target) {
            target.setAlpha(alpha);
        }

        @Override
        CommandType getType() {
            return CommandType.STYLING;
        }
    }

    private static class CmdSetTextStyle extends Command {

        final TextAnchor anchor;
        final double textsize;
        final FontStyle fontstyle;

        CmdSetTextStyle(TextAnchor anchor, double textsize, FontStyle fontstyle) {
            this.anchor = anchor;
            this.textsize = textsize;
            this.fontstyle = fontstyle;
        }

        @Override
        void perform(GeometryRenderer target) {
            if (fontstyle == null) {
                target.setTextStyle(anchor, textsize);
            } else {
                target.setTextStyle(anchor, textsize, fontstyle);
            }
        }

        @Override
        CommandType getType() {
            return CommandType.STYLING;
        }
    }

    private static class CmdSetSizeMode extends Command {

        final SizeMode sizeMode;

        CmdSetSizeMode(SizeMode sizeMode) {
            this.sizeMode = sizeMode;
        }

        @Override
        void perform(GeometryRenderer target) {
            target.setSizeMode(sizeMode);
        }

        @Override
        CommandType getType() {
            return CommandType.STYLING;
        }
    }

    private static class CmdSetStroke extends Command {

        final Color color;
        final double strokewidth;
        final Dashing dash;

        CmdSetStroke(Color color, double strokewidth, Dashing dash) {
            this.color = color;
            this.strokewidth = strokewidth;
            this.dash = dash;
        }

        @Override
        void perform(GeometryRenderer target) {
            target.setStroke(color, strokewidth, dash);
        }

        @Override
        CommandType getType() {
            return CommandType.STYLING;
        }
    }

    private static class CmdSetFill extends Command {

        final Color color;
        final Hashures hash;

        CmdSetFill(Color color, Hashures hash) {
            this.color = color;
            this.hash = hash;
        }

        @Override
        void perform(GeometryRenderer target) {
            target.setFill(color, hash);
        }

        @Override
        CommandType getType() {
            return CommandType.STYLING;
        }
    }

    private static class CmdSetPointStyle extends Command {

        final PointStyle pointstyle;
        final double pointsize;

        CmdSetPointStyle(PointStyle pointstyle, double pointsize) {
            this.pointstyle = pointstyle;
            this.pointsize = pointsize;
        }

        @Override
        void perform(GeometryRenderer target) {
            target.setPointStyle(pointstyle, pointsize);
        }

        @Override
        CommandType getType() {
            return CommandType.STYLING;
        }
    }

    private static class CmdSetBackwardArrowStyle extends Command {

        final ArrowStyle arrow;
        final double arrowsize;

        CmdSetBackwardArrowStyle(ArrowStyle arrow, double arrowsize) {
            this.arrow = arrow;
            this.arrowsize = arrowsize;
        }

        @Override
        void perform(GeometryRenderer target) {
            target.setBackwardArrowStyle(arrow, arrowsize);
        }

        @Override
        CommandType getType() {
            return CommandType.STYLING;
        }
    }

    private static class CmdSetForwardArrowStyle extends Command {

        final ArrowStyle arrow;
        final double arrowsize;

        CmdSetForwardArrowStyle(ArrowStyle arrow, double arrowsize) {
            this.arrow = arrow;
            this.arrowsize = arrowsize;
        }

        @Override
        void perform(GeometryRenderer target) {
            target.setForwardArrowStyle(arrow, arrowsize);
        }

        @Override
        CommandType getType() {
            return CommandType.STYLING;
        }
    }

    private static class CmdPushMatrix extends Command {

        final AffineTransform transform;

        CmdPushMatrix(AffineTransform transform) {
            this.transform = new AffineTransform(transform);
        }

        @Override
        void perform(GeometryRenderer target) {
            target.pushMatrix(transform);
        }

        @Override
        CommandType getType() {
            return CommandType.RENDERING;
        }
    }

    private static class CmdPopMatrix extends Command {

        CmdPopMatrix() {
        }

        @Override
        void perform(GeometryRenderer target) {
            target.popMatrix();
        }

        @Override
        CommandType getType() {
            return CommandType.RENDERING;
        }
    }

    private static class CmdPushClipping extends Command {

        final BaseGeometry bg;

        CmdPushClipping(GeometryConvertable geometry) {
            bg = geometry.toGeometry().clone();
        }

        @Override
        void perform(GeometryRenderer target) {
            target.pushClipping(bg);
        }

        @Override
        CommandType getType() {
            return CommandType.RENDERING;
        }
    }

    private static class CmdPopClipping extends Command {

        CmdPopClipping() {
        }

        @Override
        void perform(GeometryRenderer target) {
            target.popClipping();
        }

        @Override
        CommandType getType() {
            return CommandType.RENDERING;
        }
    }

    private static class CmdPushGroup extends Command {

        CmdPushGroup() {
        }

        @Override
        void perform(GeometryRenderer target) {
            target.pushGroup();
        }

        @Override
        CommandType getType() {
            return CommandType.RENDERING;
        }
    }

    private static class CmdPopGroup extends Command {

        CmdPopGroup() {
        }

        @Override
        void perform(GeometryRenderer target) {
            target.popGroup();
        }

        @Override
        CommandType getType() {
            return CommandType.RENDERING;
        }
    }

    private static class CmdDraw extends Command {

        final BaseGeometry[] geometries;

        CmdDraw(Collection<? extends GeometryConvertable> geometries) {
            this.geometries = new BaseGeometry[geometries.size()];
            int i = 0;
            for (GeometryConvertable gc : geometries) {
                this.geometries[i] = gc.toGeometry().clone();
                i++;
            }
        }

        CmdDraw(GeometryConvertable... geometries) {
            this.geometries = new BaseGeometry[geometries.length];
            int i = 0;
            for (GeometryConvertable gc : geometries) {
                this.geometries[i] = gc.toGeometry().clone();
                i++;
            }
        }

        @Override
        void perform(GeometryRenderer target) {
            target.draw(geometries);
        }

        @Override
        CommandType getType() {
            return CommandType.RENDERING;
        }

    }

    private static class CmdDrawText extends Command {

        final Vector location;
        final String text;

        CmdDrawText(Vector location, String text) {
            this.location = location.clone();
            this.text = text;
        }

        @Override
        void perform(GeometryRenderer target) {
            target.draw(location, text);
        }

        @Override
        CommandType getType() {
            return CommandType.RENDERING;
        }
    }

    private static class CmdSetLayer extends Command {

        final String layer;

        CmdSetLayer(String layer) {
            this.layer = layer;
        }

        @Override
        void perform(GeometryRenderer target) {
            if (target instanceof LayeredWriter) {
                ((LayeredWriter) target).setLayer(layer);
            }
        }

        @Override
        CommandType getType() {
            return CommandType.PAGING;
        }
    }
}
