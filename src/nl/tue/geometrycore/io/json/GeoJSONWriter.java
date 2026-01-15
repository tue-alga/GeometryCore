/*
 * GeometryCore library   
 * Copyright (C) 2025   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io.json;

import java.awt.geom.AffineTransform;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.io.BaseWriter;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class GeoJSONWriter extends BaseWriter<Void, BufferedWriter> {

    private final File _file;
    private final boolean _dense;
    private BufferedWriter _out;
    private boolean _first;

    private GeoJSONWriter(File file, boolean dense) {
        _file = file;
        _dense = dense;
    }

    public static GeoJSONWriter fileWriter(File file, boolean dense) {
        return new GeoJSONWriter(file, dense);
    }

    @Override
    public void initialize() throws IOException {
        _out = new BufferedWriter(new FileWriter(_file));
        _out.append("{" + newline());
        _out.append(indent(1) + "\"type\":" + space() + "\"FeatureCollection\"," + newline());
        _out.append(indent(1) + "\"features\":" + space() + "[" + newline());
        _first = true;
    }

    @Override
    public Void closeWithResult() throws IOException {
        _out.append(indent(2) + "}" + newline()); // closes the last feature without a comma
        _out.append(indent(1) + "]" + newline());
        _out.append("}" + newline());

        _out.close();
        return null;
    }

    @Override
    public void pushMatrix(AffineTransform transform) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void popMatrix() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pushClipping(GeometryConvertable geometry) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void popClipping() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pushGroup() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void popGroup() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void draw(Collection<? extends GeometryConvertable> geometries) {
        for (GeometryConvertable gc : geometries) {
            BaseGeometry g = gc.toGeometry();
            switch (g.getGeometryType()) {
                case POLYGON:
                    break;
                case GEOMETRYGROUP:
                    break;
            }
        }
    }

    @Override
    public void draw(Vector location, String text) {
        try {
            write(location, new String[][]{{"label", "\"" + text + "\""}});
        } catch (IOException ex) {
            Logger.getLogger(GeoJSONWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public BufferedWriter getRenderObject() {
        return _out;
    }

    public void write(Polygon polygon, String[][] properties) throws IOException {
        write(new Polygon[]{polygon}, properties);
    }

    public void write(Polygon[] polygonWithHoles, String[][] properties) throws IOException {
        startFeature("Polygon");
        _out.append(indent(4) + "\"coordinates\":" + space() + "[" + newline());

        for (Polygon p : polygonWithHoles) {
            _out.append(indent(5) + "[" + newline());
            for (Vector v : p.vertices()) {
                _out.append(indent(6) + toCoordinate(v) + "," + newline());
            }
            _out.append(indent(6) + toCoordinate(p.vertex(0)) + newline());
            if (p != polygonWithHoles[polygonWithHoles.length - 1]) {
                _out.append(indent(5) + "]," + newline());
            } else {
                _out.append(indent(5) + "]" + newline());
            }
        }

        _out.append(indent(4) + "]" + newline());
        endFeature(properties);
    }

    public void write(Polygon[][] multiPolygon, String[][] properties) throws IOException {
        startFeature("MultiPolygon");
        _out.append(indent(4) + "\"coordinates\":" + space() + "[" + newline());
        for (Polygon[] polygonWithHoles : multiPolygon) {

            _out.append(indent(5) + "[" + newline());

            for (Polygon p : polygonWithHoles) {
                _out.append(indent(6) + "[" + newline());
                for (Vector v : p.vertices()) {
                    _out.append(indent(7) + toCoordinate(v) + "," + newline());
                }
                _out.append(indent(7) + toCoordinate(p.vertex(0)) + newline());
                if (p != polygonWithHoles[polygonWithHoles.length - 1]) {
                    _out.append(indent(6) + "]," + newline());
                } else {
                    _out.append(indent(6) + "]" + newline());
                }
            }

            if (polygonWithHoles != multiPolygon[multiPolygon.length - 1]) {
                _out.append(indent(5) + "]," + newline());
            } else {
                _out.append(indent(5) + "]" + newline());
            }

        }
        _out.append(indent(4) + "]" + newline());
        endFeature(properties);
    }

    public void write(Vector point, String[][] properties) throws IOException {
        startFeature("Point");
        _out.append(indent(4) + "\"coordinates\":" + space() + "" + toCoordinate(point) + newline());
        endFeature(properties);
    }

    private void startFeature(String geomtype) throws IOException {
        if (!_first) {
            // close the previous one with a comma
            _out.append(indent(2) + "}," + newline());
        } else {
            _first = false;
        }
        _out.append(indent(2) + "{" + newline());
        _out.append(indent(3) + "\"type\":" + space() + "\"Feature\"," + newline());
        _out.append(indent(3) + "\"geometry\":" + space() + "{" + newline());
        _out.append(indent(4) + "\"type\":" + space() + "\"" + geomtype + "\"," + newline());
    }

    private void endFeature(String[][] properties) throws IOException {
        _out.append(indent(3) + "}," + newline());
        _out.append(indent(3) + "\"properties\":" + space() + "{" + newline());
        if (properties != null) {
            for (String[] keyvalue : properties) {
                _out.append(indent(3) + "\"" + keyvalue[0] + "\":" + space() + keyvalue[1] + (keyvalue == properties[properties.length-1] ? "" : ",") + newline());
            }
        }
        _out.append(indent(3) + "}" + newline());
        // NB: closing the feature only happens at the start of the next
    }

    private String toCoordinate(Vector v) {
        return "[" + v.getX() + "," + space() + v.getY() + "]";
    }

    private String space() {
        return styling(" ");
    }

    private String newline() {
        return styling("\n");
    }

    private String indent(int k) {
        String s = "  ";
        return styling(s.repeat(k));
    }

    private String styling(String s) {
        if (_dense) {
            return "";
        } else {
            return s;
        }
    }
}
