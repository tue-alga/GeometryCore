/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import java.util.logging.Level;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.InfiniteGeometry;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.BezierCurve;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.curved.Ellipse;
import nl.tue.geometrycore.geometry.curved.ParabolicSegment;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometry.mix.GeometryString;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class AWTGeometryConversion {

    public static Shape toAWTShape(GeometryConvertable geomconvertable, Rectangle infiniteClip) {
        if (geomconvertable == null) {
            return null;
        }

        BaseGeometry geom = geomconvertable.toGeometry();
        if (geom.getGeometryType().isInfinite()) {
            geom = ((InfiniteGeometry) geom).clip(infiniteClip);
        }

        if (geom == null) {
            return null;
        }

        switch (geom.getGeometryType()) {
            case LINESEGMENT:
                return toShape((LineSegment) geom);
            case CIRCULARARC:
                return toShape((CircularArc) geom);
            case CIRCLE:
                return toShape((Circle) geom);
            case ELLIPSE:
                return toShape((Ellipse) geom);
            case POLYLINE:
                return toShape((PolyLine) geom);
            case POLYGON:
                return toShape((Polygon) geom);
            case RECTANGLE:
                return toShape((Rectangle) geom);
            case GEOMETRYSTRING:
                return toShape((GeometryString) geom);
            case GEOMETRYCYCLE:
                return toShape((GeometryCycle) geom);
            case GEOMETRYGROUP:
                return toShape((GeometryGroup) geom, infiniteClip);
            case LINE:
            case HALFLINE:
                Logger.getLogger(AWTGeometryConversion.class.getClass().getName()).log(Level.WARNING,
                        "Still an infinite geometry, despite of clipping?");
                return null;
            case VECTOR:
                Logger.getLogger(AWTGeometryConversion.class.getClass().getName()).log(Level.WARNING,
                        "Trying to convert a point to a shape. This must be handled individually for rendering, to apply the desired point style");
                return null;
            case BEZIERCURVE:
                return toShape((BezierCurve) geom);
            case PARABOLICSEGMENT:
                return toShape(((ParabolicSegment) geom).toQuadraticBezier());
            case PARABOLICHALFLINE:
                Logger.getLogger(AWTGeometryConversion.class.getClass().getName()).log(Level.WARNING,
                        "Still an infinite geometry, despite of clipping?");
                return null;
            case PARABOLICLINE:
                Logger.getLogger(AWTGeometryConversion.class.getClass().getName()).log(Level.WARNING,
                        "Still an infinite geometry, despite of clipping?");
                return null;
            default:
                Logger.getLogger(AWTGeometryConversion.class.getClass().getName()).log(Level.WARNING,
                        "Unexpected geometry type: {0}", geom.getGeometryType());
                return null;
        }
    }

    private static Shape toShape(GeometryGroup<? extends BaseGeometry> group, Rectangle infiniteClip) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        for (BaseGeometry part : group.getParts()) {
            Shape shp = toAWTShape(part, infiniteClip);
            if (shp != null) {
                path.append(shp, false);
            }
        }
        return path;
    }

    private static Shape toShape(GeometryString<? extends OrientedGeometry> string) {
        GeneralPath path = new GeneralPath();
        for (OrientedGeometry part : string.edges()) {
            path.append(toAWTShape(part, null), true);
        }
        return path;
    }

    private static Shape toShape(GeometryCycle<? extends OrientedGeometry> cycle) {
        GeneralPath path = new GeneralPath();
        for (OrientedGeometry part : cycle.edges()) {
            path.append(toAWTShape(part, null), true);
        }
        path.closePath();
        return path;
    }

    private static Shape toShape(Circle circle) {
        double radius = circle.getRadius();
        Vector center = circle.getCenter();

        double x = center.getX() - radius;
        double y = center.getY() - radius;
        double width = 2 * radius;

        return new Ellipse2D.Double(x, y, width, width);
    }

    private static Shape toShape(Ellipse ellipse) {

        if (ellipse.isEmpty()) {
            return null;
        }

        Vector axis = ellipse.getMajorAxis();
        double angle = Vector.right().computeCounterClockwiseAngleTo(axis);
        double a = ellipse.getMajorAxisLength();
        double b = ellipse.getMinorAxisLength();
        Vector center = ellipse.getCenter();

        Shape E = new Ellipse2D.Double(-a, -b, 2 * a, 2 * b);
        E = AffineTransform.getRotateInstance(angle).createTransformedShape(E);
        E = AffineTransform.getTranslateInstance(center.getX(), center.getY()).createTransformedShape(E);
        return E;
    }

    private static Shape toShape(CircularArc arc) {
        if (arc.getCenter() == null) {
            // No center so a line segment
            Vector start = arc.getStart();
            Vector end = arc.getEnd();

            return new Line2D.Double(
                    start.getX(), start.getY(),
                    end.getX(), end.getY());
        } else {
            double radius = arc.radius();
            double centralAngle = Math.toDegrees(-arc.centralAngle(radius));
            Vector center = arc.getCenter();

            double x = center.getX() - radius;
            double y = center.getY() - radius;
            double width = 2 * radius;

            Vector startdir = Vector.subtract(arc.getStart(), arc.getCenter());
            startdir.scale(1.0 / radius);

            double startAngle = Math.toDegrees(Vector.right().computeClockwiseAngleTo(startdir, false, false));

            return new Arc2D.Double(x, y, width, width, startAngle, centralAngle, Arc2D.OPEN);
        }
    }

    private static Shape toShape(LineSegment segment) {
        Vector start = segment.getStart();
        Vector end = segment.getEnd();

        return new Line2D.Double(
                start.getX(), start.getY(),
                end.getX(), end.getY());
    }

    private static Shape toShape(PolyLine polyline) {
        Path2D path = new Path2D.Double();
        Vector v = polyline.vertex(0);
        path.moveTo(v.getX(), v.getY());
        for (int i = 1; i < polyline.vertexCount(); i++) {
            v = polyline.vertex(i);
            path.lineTo(v.getX(), v.getY());
        }

        return path;
    }

    private static Shape toShape(Polygon polygon) {
        Path2D path = new Path2D.Double();
        Vector v = polygon.vertex(0);
        path.moveTo(v.getX(), v.getY());
        for (int i = 1; i < polygon.vertexCount(); i++) {
            v = polygon.vertex(i);
            path.lineTo(v.getX(), v.getY());
        }
        path.closePath();

        return path;
    }

    private static Shape toShape(Rectangle rectangle) {

        if (rectangle.isEmpty()) {
            return null;
        }

        double width = rectangle.width();
        double height = rectangle.height();

        return new Rectangle2D.Double(rectangle.getLeft(), rectangle.getBottom(), width, height);
    }

    private static Shape toShape(BezierCurve bezierCurve) {
        Vector u = bezierCurve.getStart();
        Vector v = bezierCurve.getEnd();

        Path2D path = new Path2D.Double();
        path.moveTo(u.getX(), u.getY());

        int d = bezierCurve.getControlpoints().size();
        switch (d) {
            case 2: {
                // line segment
                path.lineTo(v.getX(), v.getY());
                break;
            }
            case 3: {
                // quadratic bezier
                Vector cp1 = bezierCurve.getControlpoints().get(1);
                path.quadTo(cp1.getX(), cp1.getY(), v.getX(), v.getY());
                break;
            }
            default: {
                Logger.getLogger(AWTGeometryConversion.class.getClass().getName()).log(Level.WARNING,
                        "Unsupported number of control points for conversion: {0}; Bézier curves must have 2 to 4 control points. Using arbitrary control points instead.", bezierCurve.getControlpoints().size());
                if (d < 2) {
                    // nothing to do
                    break;
                }
                // else: fall through to cubic Bezier
            }
            case 4: {
                // cubic bezier
                Vector cp1 = bezierCurve.getControlpoints().get(1);
                Vector cp2 = bezierCurve.getControlpoints().get(d - 2);
                path.curveTo(cp1.getX(), cp1.getY(), cp2.getX(), cp2.getY(), v.getX(), v.getY());
                break;
            }
        }

        return path;
    }

}
