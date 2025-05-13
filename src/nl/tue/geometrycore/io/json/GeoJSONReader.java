
/*
 * GeometryCore library   
 * Copyright (C) 2025   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.io.BaseReader;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.util.ClipboardUtil;

/**
 * This class provides a reader for the GeoJSON format.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class GeoJSONReader extends BaseReader {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final BufferedReader _source;
    private boolean _eof;
    private String[] _propsToKeep;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    private GeoJSONReader(BufferedReader source) {
        _source = source;
        _eof = false;
        _propsToKeep = null;
    }

    /**
     * Constructs the reader of a file containing the IPE XML code.
     *
     * @param file the ipe file to be read
     * @return new reader for the provided file
     * @throws FileNotFoundException
     */
    public static GeoJSONReader fileReader(File file) throws FileNotFoundException {
        return new GeoJSONReader(new BufferedReader(new FileReader(file)));
    }

    /**
     * Constructs a reader for the provided string containing IPE XML code.
     *
     * @param string IPE XML code
     * @return new reader for the code
     */
    public static GeoJSONReader stringReader(String string) {
        return new GeoJSONReader(new BufferedReader(new StringReader(string)));
    }

    /**
     * Constructs a custom reader using some buffered reader that provides the
     * IPE XML code line by line.
     *
     * @param reader buffered reader providing the IPE XML code
     * @return new reader for the code
     */
    public static GeoJSONReader customReader(BufferedReader reader) {
        return new GeoJSONReader(reader);
    }

    /**
     * Constructs a reader based on the contents of the clipboard.
     *
     * @return new reader for the clipboard string
     */
    public static GeoJSONReader clipboardReader() {
        return stringReader(ClipboardUtil.getClipboardContents());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void close() throws IOException {
        _source.close();
    }

    @Override
    public void read(List<ReadItem> items) throws IOException {

        if (!skipToQuotedString("features")) {
            return;
        }
        if (!skipToCharacter('[')) {
            return;
        }

        ReadItem ri;
        while ((ri = readFeature()) != null) {
            items.add(ri);
        }
    }

    public void setPropertiesOfInterest(String... props) {
        _propsToKeep = props;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private ReadItem readFeature() throws IOException {
        if (!skipToCharacter('{')) {
            return null;
        }

        ReadItem ri = new ReadItem();
        String s;
        while ((s = readNextQuotedString('}')) != null) {
            switch (s) {
                case "type":
                    readNextQuotedString();
                    break;
                case "properties":
                    readProperties(ri);
                    break;
                case "geometry":
                    readGeometry(ri);
                    break;
            }
        }
        return ri;

    }

    private void readProperties(ReadItem ri) throws IOException {
        Map<String, String> props = new HashMap();
        ri.setAuxiliary(props);

        boolean done = false;
        while (!done) {
            String name = readNextQuotedString('}');
            if (name == null) {
                break;
            }
            skipToCharacter(':');
            StringBuilder value = new StringBuilder();
            boolean instring = false;
            int accolades = 0;
            int brackets = 0;

            int next = _source.read();
            charloop:
            while (next != -1) {
                char ch = (char) next;
                switch (ch) {
                    case '{':
                        if (!instring) {
                            accolades++;
                        }
                        value.append(ch);
                        break;
                    case '}':
                        if (!instring) {
                            accolades--;
                        }
                        if (accolades < 0) {
                            // the end } of the properties
                            done = true;
                            break charloop;
                        } else {
                            value.append(ch);
                        }
                        break;
                    case '[':
                        if (!instring) {
                            brackets++;
                        }
                        value.append(ch);
                        break;
                    case ']':
                        if (!instring) {
                            brackets--;
                        }
                        value.append(ch);
                        break;
                    case '"':
                        instring = !instring;
                        value.append(ch);
                        break;
                    case ',':
                        if (!instring && brackets == 0 && accolades == 0) {
                            // end of the property
                            break charloop;
                        } else {
                            value.append(ch);
                        }
                        break;
                    default:
                        value.append(ch);
                        break;
                }
                next = _source.read();
            }

            boolean interest;
            if (_propsToKeep == null) {
                interest = true;
            } else {
                interest = false;
                for (String p : _propsToKeep) {
                    interest = interest || p.equals(name);
                }
            }         
            
            if (interest) {
                String val = value.toString().trim();
                if (val.charAt(0) == '"') {
                    val = val.substring(1, val.length() - 1);
                }
                props.put(name, val);
            } 
        }
    }

    private void readGeometry(ReadItem ri) throws IOException {
        skipToQuotedString("type");
        String type = readNextQuotedString();
        switch (type) {
            case "Point":
                skipToQuotedString("coordinates");
                ri.setGeometry(readPoint());
                break;
            case "MultiPoint":
                skipToQuotedString("coordinates");
                ri.setGeometry(readMultiPoint());
                break;
            case "LineString":
                skipToQuotedString("coordinates");
                ri.setGeometry(readLineString());
                break;
            case "MultiLineString":
                skipToQuotedString("coordinates");
                ri.setGeometry(readMultiLineString());
                break;
            case "Polygon": {
                skipToQuotedString("coordinates");
                GeometryGroup<Polygon> grp = readPolygon();
                if (grp.getParts().size() == 1) {
                    ri.setGeometry(grp.getParts().get(0));
                } else {
                    ri.setGeometry(grp);
                }
                break;
            }
            case "MultiPolygon": {
                skipToQuotedString("coordinates");
                GeometryGroup<Polygon> grp = readMultiPolygon();
                if (grp.getParts().size() == 1) {
                    ri.setGeometry(grp.getParts().get(0));
                } else {
                    ri.setGeometry(grp);
                }
                break;
            }
            case "GeometryCollection":
                skipToQuotedString("geometries");
                // TODO
                break;
            default:
                System.err.println("Unsupported geometry type: " + type);
                break;
        }
        skipToCharacter('}');
    }

    private GeometryGroup<Polygon> readMultiPolygon() throws IOException {
        skipToCharacter('[');
        GeometryGroup<Polygon> grp = new GeometryGroup<>();

        GeometryGroup<Polygon> poly;
        while ((poly = readPolygon()) != null) {
            grp.getParts().addAll(poly.getParts());
        }

        return grp;
    }

    private GeometryGroup<Polygon> readPolygon() throws IOException {
        if (!skipToCharacter('[', ']')) {
            return null;
        }
        GeometryGroup<Polygon> grp = new GeometryGroup<>();

        Polygon p;
        while ((p = readSinglePolygon()) != null) {
            grp.getParts().add(p);
        }

        return grp;
    }

    private Polygon readSinglePolygon() throws IOException {
        if (!skipToCharacter('[', ']')) {
            return null;
        }
        Polygon P = new Polygon();
        Vector v;
        while ((v = readPoint()) != null) {
            P.addVertex(v);
        }
        if (P.vertex(0).isApproximately(P.vertex(-1))) {
            P.removeVertex(-1);
        }
        return P;
    }

    private GeometryGroup<PolyLine> readMultiLineString() throws IOException {
        skipToCharacter('[');
        GeometryGroup<PolyLine> grp = new GeometryGroup<>();

        PolyLine line;
        while ((line = readLineString()) != null) {
            grp.getParts().add(line);
        }

        return grp;
    }

    private PolyLine readLineString() throws IOException {
        if (!skipToCharacter('[', ']')) {
            return null;
        }
        PolyLine P = new PolyLine();
        Vector v;
        while ((v = readPoint()) != null) {
            P.addVertex(v);
        }
        return P;
    }

    private GeometryGroup<Vector> readMultiPoint() throws IOException {
        skipToCharacter('[');
        GeometryGroup<Vector> grp = new GeometryGroup<>();

        Vector v;
        while ((v = readPoint()) != null) {
            grp.getParts().add(v);
        }

        return grp;
    }

    private Vector readPoint() throws IOException {
        if (!skipToCharacter('[', ']')) {
            return null;
        }
        String x = readUntil(',');
        String y = readUntil(']');
        return new Vector(Double.parseDouble(x), Double.parseDouble(y));
    }

    private boolean skipToCharacter(char c) throws IOException {
        return readUntil(c) != null;
    }

    private boolean skipToCharacter(char c, char esc) throws IOException {
        return readUntil(c, esc) != null;
    }

    private boolean skipToQuotedString(String s) throws IOException {
        while (!_eof && !s.equals(readNextQuotedString())) {
            // skip
        }
        return !_eof;
    }

    private String readNextQuotedString() throws IOException {
        readUntil('"');
        return readUntil('"');
    }

    private String readNextQuotedString(char esc) throws IOException {
        if (readUntil('"', esc) == null) {
            return null;
        }
        return readUntil('"');
    }

    private String readUntil(char c) throws IOException {
        StringBuilder result = new StringBuilder();
        int next = _source.read();
        while (next != -1 && next != c) {
            result.append((char) next);
            next = _source.read();
        }
        if (next == -1) {
            _eof = true;
            return null;
        } else {
            return result.toString();
        }
    }

    private String readUntil(char c, char esc) throws IOException {
        StringBuilder result = new StringBuilder();
        int next = _source.read();
        while (next != -1 && next != c && next != esc) {
            result.append((char) next);
            next = _source.read();
        }
        if (next == -1) {
            _eof = true;
            return null;
        } else if (next == esc) {
            return null;
        } else {
            return result.toString();
        }
    }
    //</editor-fold>
}
