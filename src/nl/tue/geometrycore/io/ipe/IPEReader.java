/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io.ipe;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.BezierCurve;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.geometrycore.io.BaseReader;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.util.ClipboardUtil;

/**
 * This class provides a reader for the IPE XML format. It can handle both files
 * as well as selection code, allowing easy copy-pasting from IPE into Java. The
 * applicable type is detected automatically.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class IPEReader extends BaseReader {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Map<String, Color> _namedColors;
    private Map<String, Double> _namedStrokewidths;
    private Map<String, Double> _namedSymbolsizes;
    private Map<String, Double> _namedTransparencies;
    private Map<String, Dashing> _namedDashing;
    private Rectangle _pagebounds = IPEWriter.getA4Size();
    private final BufferedReader _source;
    private String _currentLayer;
    private int _bezierSampling = -1;
    private Stack<double[][]> matrixstack = new Stack();
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    private IPEReader(BufferedReader source) {
        _source = source;
    }

    /**
     * Constructs the reader of a file containing the IPE XML code.
     *
     * @param file the ipe file to be read
     * @return new reader for the provided file
     * @throws FileNotFoundException
     */
    public static IPEReader fileReader(File file) throws FileNotFoundException {
        return new IPEReader(new BufferedReader(new FileReader(file)));
    }

    /**
     * Constructs a reader for the provided string containing IPE XML code.
     *
     * @param string IPE XML code
     * @return new reader for the code
     */
    public static IPEReader stringReader(String string) {
        return new IPEReader(new BufferedReader(new StringReader(string)));
    }

    /**
     * Constructs a custom reader using some buffered reader that provides the
     * IPE XML code line by line.
     *
     * @param reader buffered reader providing the IPE XML code
     * @return new reader for the code
     */
    public static IPEReader customReader(BufferedReader reader) {
        return new IPEReader(reader);
    }

    /**
     * Constructs a reader based on the contents of the clipboard.
     *
     * @return new reader for the clipboard string
     */
    public static IPEReader clipboardReader() {
        return stringReader(ClipboardUtil.getClipboardContents());
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="STATIC">    
    /**
     * Reads the first geometry found in the file. 
     *
     * @param file the ipe file to be read
     * @return the first geometry found, or null of no geometries were found
     * @throws IOException
     */
    public static BaseGeometry readSingleGeometry(File file) throws IOException {
        IPEReader read = IPEReader.fileReader(file);
        List<ReadItem> items = read.read();
        read.close();        
        return items.isEmpty() ? null : items.get(0).toGeometry();
    }

   /**
     * Reads the first geometry found in the string. 
     *
     * @param string the ipe string to be read
     * @return the first geometry found, or null of no geometries were found
     * @throws IOException
     */
    public static BaseGeometry readSingleGeometry(String string) throws IOException {
        IPEReader read = IPEReader.stringReader(string);
        List<ReadItem> items = read.read();
        read.close();        
        return items.isEmpty() ? null : items.get(0).toGeometry();
    }
    
    /**
     * Reads the first geometry found on the clipboard. 
     *
     * @return the first geometry found, or null of no geometries were found
     * @throws IOException
     */
    public static BaseGeometry readSingleGeometry() throws IOException {
        IPEReader read = IPEReader.clipboardReader();
        List<ReadItem> items = read.read();
        read.close();        
        return items.isEmpty() ? null : items.get(0).toGeometry();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void close() throws IOException {
        _source.close();
    }

    /**
     * Reads only the items from a specific page. First page is numbered 1. Any
     * value below 1 will result in all pages being read.
     *
     * @param page
     * @return
     * @throws IOException
     */
    public List<ReadItem> read(int page) throws IOException {
        List<ReadItem> items = new ArrayList();
        read(items, page);
        return items;
    }

    @Override
    public void read(List<ReadItem> items) throws IOException {
        read(items, -1);
    }

    /**
     * Reads all items, each list representing a single page in the file.
     *
     * @return
     * @throws IOException
     */
    public List<List<ReadItem>> readPages() throws IOException {
        List<List<ReadItem>> pages = new ArrayList();
        readPages(pages);
        return pages;
    }

    /**
     * Reads all items, each list representing a single page in the file. If the
     * provided list is not empty, each page is appended after the current lists
     *
     * @param pages
     * @throws IOException
     */
    public void readPages(List<List<ReadItem>> pages) throws IOException {
        readInternal(pages, -1);
    }

    /**
     * Reads only the items from a specific page. First page is numbered 1. Any
     * value below 1 will result in all pages being read.
     *
     * @param items
     * @param page
     * @throws IOException
     */
    public void read(List<ReadItem> items, int page) throws IOException {
        List<List<ReadItem>> pages = new ArrayList();
        pages.add(items);
        readInternal(pages, page >= 1 ? page : 0);
    }

    /**
     *
     * @param pages list of items per page to fill
     * @param page positive number: reads only a single page (list is already in
     * pages list); 0: read all pages into a single list (which is already in
     * the pages list); -1: read all pages into separate lists be appended to
     */
    private void readInternal(List<List<ReadItem>> pages, int page) throws IOException {

        _currentLayer = "default";

        String line = _source.readLine();

        boolean onpage = line.startsWith("<ipeselection");
        boolean instyle = false;

        int pageNumber = 0;

        if (onpage) {
            // selection
            _namedStrokewidths = null;
            _namedSymbolsizes = null;
            _namedTransparencies = null;
            _namedColors = null;
            _namedDashing = null;
            pageNumber++;
        } else {
            _namedStrokewidths = new HashMap();
            _namedSymbolsizes = new HashMap();
            _namedTransparencies = new HashMap();
            _namedColors = new HashMap();
            _namedDashing = new HashMap();

            // IPE doesnt store some default values...
            _namedStrokewidths.put("normal", 0.4);
            _namedSymbolsizes.put("normal", 3.0);
            _namedColors.put("black", Color.black);
            _namedColors.put("white", Color.white);
        }

        List<ReadItem> items = page >= 0 ? pages.get(0) : null;

        while (line != null) {

            if (line.startsWith("<page")) {
                pageNumber++;
                if (page == -1) {
                    items = new ArrayList();
                    pages.add(items);
                }
                onpage = true;
            } else if (line.startsWith("</page")) {
                onpage = false;
            } else if (line.startsWith("<ipestyle")) {
                instyle = true;
            } else if (line.startsWith("</ipestyle")) {
                instyle = false;
            } else if (onpage && (page < 1 || pageNumber == page)) {

                if (line.startsWith("<path")) {
                    ReadItem item = readPath(line);
                    item.setPageNumber(pageNumber);
                    items.add(item);
                } else if (line.startsWith("<use") && line.contains("name=\"mark")) {
                    ReadItem item = readMark(line);
                    item.setPageNumber(pageNumber);
                    items.add(item);
                } else if (line.startsWith("<group")) {
                    ReadItem item = readGroup(line);
                    item.setPageNumber(pageNumber);
                    items.add(item);
                } else if (line.startsWith("<text")) {
                    ReadItem item = readText(line);
                    item.setPageNumber(pageNumber);
                    items.add(item);
                }
            } else if (instyle) {

                if (line.startsWith("<dashstyle")) {
                    _namedDashing.put(readAttribute(line, "name="), interpretDash(readAttribute(line, "value=")));
                } else if (line.startsWith("<pen")) {
                    _namedStrokewidths.put(readAttribute(line, "name="), interpretPen(readAttribute(line, "value=")));
                } else if (line.startsWith("<color")) {
                    _namedColors.put(readAttribute(line, "name="), interpretColor(readAttribute(line, "value=")));
                } else if (line.startsWith("<symbolsize")) {
                    _namedSymbolsizes.put(readAttribute(line, "name="), interpretSymbolSize(readAttribute(line, "value=")));
                } else if (line.startsWith("<opacity")) {
                    _namedTransparencies.put(readAttribute(line, "name="), interpretTransparency(readAttribute(line, "value=")));
                } else if (line.startsWith("<layout ")) {
                    _pagebounds = interpretPageBounds(readAttribute(line, "paper="));
                }
            }

            line = _source.readLine();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    public Map<String, Color> getNamedColors() {
        return _namedColors;
    }

    public Map<String, Double> getNamedStrokeWidths() {
        return _namedStrokewidths;
    }

    public Map<String, Double> getNamedSymbolSizes() {
        return _namedSymbolsizes;
    }

    public Map<String, Double> getNamedTransparencies() {
        return _namedTransparencies;
    }

    public Map<String, Dashing> getNamedDashing() {
        return _namedDashing;
    }

    public Rectangle getPageBounds() {
        return _pagebounds;
    }

    public int getBezierSampling() {
        return _bezierSampling;
    }

    /**
     * Set to -1 (default) to simply read Bezier curves. To sample, Bezier
     * curves into polylines, set this to a nonnegative value, indicating the
     * number of intermediate samples. E.g., setting it to zero turns each
     * Bezier Curve into a line segment between its first and last control
     * point.
     *
     * @param bezierSampling
     */
    public void setBezierSampling(int bezierSampling) {
        _bezierSampling = bezierSampling;
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="PRIVATE">    
    /**
     * Reads the value of a given XML attribute in a given line.
     *
     * @param line Full line to search in
     * @param attributename Attribute name, include "=" sign
     * @return Attribute value (without quotes) or null if attribute is not
     * found.
     */
    private String readAttribute(String line, String attributename) {
        int index = line.indexOf(attributename);
        if (index < 0) {
            return null;
        } else {
            String value = line.substring(index);
            value = value.substring(value.indexOf("\"") + 1);
            value = value.substring(0, value.indexOf("\""));
            return value;
        }
    }

    /**
     * Read a <path> element in the XML format.
     *
     * NB: will read up to and including the closing <path> tag.
     *
     * @param line first line containing the path-tag
     * @return ReadItem containing the geometry and style of the path
     * @throws IOException
     */
    private ReadItem readPath(String line) throws IOException {
        if (line.contains("layer=")) {
            _currentLayer = readAttribute(line, "layer=");
        }

        Color stroke = interpretColor(readAttribute(line, "stroke="));
        Color fill = interpretColor(readAttribute(line, "fill="));
        double strokewidth = interpretPen(readAttribute(line, "pen="));
        double[][] m = interpretMatrix(readAttribute(line, "matrix="));
        if (m != null) {
            matrixstack.push(m);
        }

        Dashing dash = interpretDash(readAttribute(line, "dash="));
        double alpha = interpretTransparency(readAttribute(line, "opacity="));

        // start reading geometries
        List<BaseGeometry> finishedgeometries = new ArrayList();

        PathReader current = null;

        line = _source.readLine();

        while (!line.startsWith("</path>")) {
            if (line.endsWith("h")) {
                // return to first
                finishedgeometries.add(current.close());
                current = null;
            } else if (line.endsWith(" m")) {
                if (current != null) {
                    finishedgeometries.add(current.end());
                }
                current = new PathReader();
                current.move(interpretPosition(line));
            } else if (line.endsWith(" l")) {
                current.lineTo(interpretPosition(line));
            } else if (line.endsWith(" e")) {
                if (current != null) {
                    finishedgeometries.add(current.end());
                    current = null;
                }
                // circle (NB: closed)
                finishedgeometries.add(interpretCircle(line));
            } else if (line.endsWith(" a")) {
                current.arcTo(interpretCircularArc(line, current._end));
            } else if (line.endsWith(" c")) {
                current.curveTo(interpretPosition(line));

            } else if (line.indexOf(' ', line.indexOf(' ') + 1) < 0) {
                // point for curve (just one whitespace on line)
                current.blank(interpretPosition(line));
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected command: \"{0}\"", line);
            }

            line = _source.readLine();
        }

        if (current != null) {
            finishedgeometries.add(current.end());
            current = null;
        }

        BaseGeometry newgeom;
        if (finishedgeometries.size() == 1) {
            newgeom = finishedgeometries.get(0);
        } else {
            newgeom = new GeometryGroup(finishedgeometries);
        }

        ReadItem item = new ReadItem();

        item.setGeometry(newgeom);
        item.setAlpha(alpha);
        item.setDash(dash);
        item.setFill(fill);
        item.setLayer(_currentLayer);
        item.setStroke(stroke);
        item.setStrokewidth(strokewidth);

        if (m != null) {
            matrixstack.pop();
        }
        return item;
    }

    /**
     * Read a <mark> elements in the XML format.
     *
     * @param line first line containing the mark-tag
     * @return ReadItem containing the geometry and style of the mark
     */
    private ReadItem readMark(String line) {

        if (line.contains("layer")) {
            _currentLayer = readAttribute(line, "layer");
        }

        Color stroke = interpretColor(readAttribute(line, "stroke="));
        Color fill = interpretColor(readAttribute(line, "fill="));

        double size = interpretSymbolSize(readAttribute(line, "size="));
        double[][] m = interpretMatrix(readAttribute(line, "matrix="));
        if (m != null) {
            matrixstack.push(m);
        }

        Dashing dash = interpretDash(readAttribute(line, "dash="));
        double alpha = interpretTransparency(readAttribute(line, "opacity="));

        Vector v = interpretPosition(readAttribute(line, "pos="));

        ReadItem item = new ReadItem();

        item.setGeometry(v);
        item.setAlpha(alpha);
        item.setDash(dash);
        item.setFill(fill);
        item.setLayer(_currentLayer);
        item.setStroke(stroke);
        item.setStrokewidth(0.4 * size);
        item.setSymbolsize(size);

        if (m != null) {
            matrixstack.pop();
        }
        return item;
    }

    /**
     * Read a <group> elements in the XML format.
     *
     * NB: will read up to and including the closing <group> tag.
     *
     * @param line first line containing the group-tag
     * @return ReadItem containing the geometry and last encountered style of
     * the group
     * @throws IOException
     */
    private ReadItem readGroup(String line) throws IOException {
        if (line.contains("layer")) {
            _currentLayer = readAttribute(line, "layer");
        }

        double[][] m = interpretMatrix(readAttribute(line, "matrix"));
        if (m != null) {
            matrixstack.push(m);
        }

        List<BaseGeometry> parts = new ArrayList();
        String string = null;

        Color stroke = null;
        Color fill = null;
        double strokewidth = -1;
        double size = -1;
        Dashing dash = null;
        double alpha = -1;

        line = _source.readLine();

        while (!line.startsWith("</group")) {
            ReadItem item = null;
            if (line.startsWith("<path")) {
                item = readPath(line);
            } else if (line.startsWith("<use") && line.contains("name=\"mark")) {
                item = readMark(line);
            } else if (line.startsWith("<group")) {
                item = readGroup(line);
            } else if (line.startsWith("<text")) {
                item = readText(line);
            }

            if (item != null) {
                BaseGeometry geo = item.getGeometry();
                parts.add(geo);

                stroke = item.getStroke();
                fill = item.getFill();
                strokewidth = item.getStrokewidth();
                size = item.getSymbolsize();
                dash = item.getDash();
                alpha = item.getAlpha();

                if (item.getString() != null) {
                    if (string == null) {
                        string = item.getString();
                    } else {
                        string += "\n" + item.getString();
                    }
                }
            }

            line = _source.readLine();
        }

        ReadItem item = new ReadItem();

        item.setString(string);
        item.setLayer(_currentLayer);
        item.setGeometry(new GeometryGroup(parts));

        if (alpha >= 0) {
            item.setAlpha(alpha);
        }
        if (dash != null) {
            item.setDash(dash);
        }
        if (fill != null) {
            item.setFill(fill);
        }
        if (stroke != null) {
            item.setStroke(stroke);
        }
        if (strokewidth >= 0) {
            item.setStrokewidth(strokewidth);
        }
        if (size >= 0) {
            item.setSymbolsize(size);
        }

        if (m != null) {
            matrixstack.pop();
        }
        return item;
    }

    private ReadItem readText(String line) throws IOException {
        if (line.contains("layer=")) {
            _currentLayer = readAttribute(line, "layer=");
        }

        Color stroke = interpretColor(readAttribute(line, "stroke="));
        double[][] m = interpretMatrix(readAttribute(line, "matrix="));
        if (m != null) {
            matrixstack.push(m);
        }
        Vector pos = interpretPosition(readAttribute(line, "pos="));

        double size = interpretTextSize(readAttribute(line, "size="));
        double alpha = interpretTransparency(readAttribute(line, "opacity="));
        TextAnchor anchor = interpretTextAnchor(readAttribute(line, "halign="), readAttribute(line, "valign="));

        // start reading geometries
        String string = line.substring(line.indexOf(">") + 1);

        while (!string.endsWith("</text>")) {
            string += "\n" + _source.readLine();
        }

        string = string.substring(0, string.length() - 7);

        ReadItem item = new ReadItem();

        item.setString(string);
        item.setGeometry(pos);
        item.setAlpha(alpha);
        item.setLayer(_currentLayer);
        item.setStroke(stroke);
        item.setSymbolsize(size * (m == null ? 1 : m[0][0]));
        item.setAnchor(anchor);

        if (m != null) {
            matrixstack.pop();
        }
        return item;
    }

    /**
     * Interpret a color value, possibly looking it up in the named values.
     *
     * @param attr Attribute value
     */
    private Color interpretColor(String attr) {
        if (attr == null) {
            return null;
        }

        try {
            String[] split = attr.split(" ");
            if (split.length == 1) {

                double v = Double.parseDouble(attr);
                return new Color((int) (255 * v),
                        (int) (255 * v),
                        (int) (255 * v));

            } else {
                double r, g, b;
                r = Double.parseDouble(split[0]);
                g = Double.parseDouble(split[1]);
                b = Double.parseDouble(split[2]);

                return new Color((int) (255 * r),
                        (int) (255 * g),
                        (int) (255 * b));
            }
        } catch (NumberFormatException ex) {
            if (_namedColors == null) {
                _namedColors = IPEDefaults.getColors();
            }

            if (_namedColors.containsKey(attr)) {
                return _namedColors.get(attr);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected color name: {0}", attr);
                return Color.red;
            }
        }
    }

    /**
     * Interpret a text-size value, possibly looking it up in the named values.
     *
     * @param attr Attribute value
     */
    private double interpretTextSize(String attr) {
        if (attr == null) {
            return 1;
        }

        try {
            return Double.parseDouble(attr);
        } catch (NumberFormatException ex) {
            Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Not (yet) supporting named text-sizes", attr);
            return 1;
        }
    }

    /**
     * Interpret a point-size value, possibly looking it up in the named values.
     *
     * @param attr Attribute value
     */
    private double interpretSymbolSize(String attr) {
        if (attr == null) {
            return 1;
        }

        try {
            return Double.parseDouble(attr);
        } catch (NumberFormatException ex) {
            if (_namedSymbolsizes == null) {
                _namedSymbolsizes = IPEDefaults.getSymbolSizes();
            }

            if (_namedSymbolsizes.containsKey(attr)) {
                return _namedSymbolsizes.get(attr);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected symbol-size name: {0}", attr);
                return 1;
            }
        }
    }

    private TextAnchor interpretTextAnchor(String hattr, String vattr) {
        TextAnchor anchor = TextAnchor.BASELINE;
        if (hattr != null) {
            if (hattr.equals("center")) {
                anchor = TextAnchor.BASELINE_CENTER;
            } else if (hattr.equals("right")) {
                anchor = TextAnchor.BASELINE_RIGHT;
            }
        }

        if (vattr != null) {
            if (vattr.equals("center")) {
                switch (anchor) {
                    case BASELINE:
                        anchor = TextAnchor.LEFT;
                        break;
                    case BASELINE_CENTER:
                        anchor = TextAnchor.CENTER;
                        break;
                    case BASELINE_RIGHT:
                        anchor = TextAnchor.RIGHT;
                        break;
                }
            } else if (vattr.equals("top")) {
                switch (anchor) {
                    case BASELINE:
                        anchor = TextAnchor.TOP_LEFT;
                        break;
                    case BASELINE_CENTER:
                        anchor = TextAnchor.TOP;
                        break;
                    case BASELINE_RIGHT:
                        anchor = TextAnchor.TOP_RIGHT;
                        break;
                }
            } else if (vattr.equals("bottom")) {
                switch (anchor) {
                    case BASELINE:
                        anchor = TextAnchor.BOTTOM_LEFT;
                        break;
                    case BASELINE_CENTER:
                        anchor = TextAnchor.BOTTOM;
                        break;
                    case BASELINE_RIGHT:
                        anchor = TextAnchor.BOTTOM_RIGHT;
                        break;
                }
            }
        }

        return anchor;
    }

    /**
     * Interpret a page size from the IPE style specification.
     *
     * @param attr two space-separated dimensions (width followed by height)
     * @return a rectangle matching the page boundaries
     */
    private Rectangle interpretPageBounds(String attr) {
        if (attr == null) {
            return null;
        }
        String[] split = attr.split(" ");
        double width = Double.parseDouble(split[0]);
        double height = Double.parseDouble(split[1]);
        return new Rectangle(0, width, 0, height);
    }

    /**
     * Interpret a transparency value, possibly looking it up in the named
     * values. Although IPE allows only named values, we'll also allow any
     * numeric value in between 0 and 1.
     *
     * @param attr Attribute value
     */
    private double interpretTransparency(String attr) {
        if (attr == null) {
            return 1;
        }
        try {
            return Double.parseDouble(attr);
        } catch (NumberFormatException ex) {
            if (_namedTransparencies == null) {
                _namedTransparencies = IPEDefaults.getTransparencies();
            }

            if (_namedTransparencies.containsKey(attr)) {
                return _namedTransparencies.get(attr);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected transparency name: {0}", attr);
                return 1;
            }
        }
    }

    /**
     * Interpret a dash-style value, possibly looking it up in the named values.
     *
     * @param attr Attribute value
     */
    private Dashing interpretDash(String attr) {
        if (attr == null) {
            return Dashing.SOLID;
        }

        int index = attr.indexOf("[");
        if (index >= 0) {
            String[] split = attr.substring(index + 1, attr.indexOf("]")).split(" ");
            double[] dash = new double[split.length];
            for (int i = 0; i < dash.length; i++) {
                dash[i] = Double.parseDouble(split[i]);
            }
            return new Dashing(dash);
        } else {
            if (_namedDashing == null) {
                _namedDashing = IPEDefaults.getDashStyles();
            }

            if (_namedDashing.containsKey(attr)) {
                return _namedDashing.get(attr);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected dash name: {0}", attr);
                return Dashing.SOLID;
            }
        }
    }

    /**
     * Interpret a position (x,y) value, applying the transformation matrix, if
     * any.
     *
     * @param attr Attribute value
     * @param matrix Matrix for transforming the (x,y) value
     */
    private Vector interpretPosition(String attr) {
        double x, y;
        if (attr == null) {
            x = 0;
            y = 0;
        } else {
            String[] split = attr.split(" ");
            x = Double.parseDouble(split[0]);
            y = Double.parseDouble(split[1]);
        }

        Vector v = new Vector(x, y);
        applyMatrixToPosition(v);
        return v;
    }

    /**
     * Interpret a position (x,y) value, applying the transformation matrix, if
     * any.
     *
     * @param strX X-coordinate as string
     * @param strY Y-coordinate as string
     * @param matrix Matrix for transforming the (x,y) value
     */
    private Vector interpretPosition(String strX, String strY) {
        double x, y;

        x = Double.parseDouble(strX);
        y = Double.parseDouble(strY);

        Vector v = new Vector(x, y);
        applyMatrixToPosition(v);
        return v;
    }

    /**
     * Applies all matrix transformations on the stack to the given position. 
     *
     * @param position Position to which the matrices must be applied
     */
    private void applyMatrixToPosition(Vector position) {      
        for (int i = matrixstack.size() - 1; i >= 0; i--) {
            double[][] matrix = matrixstack.get(i);
            position.set(
                    matrix[0][0] * position.getX() + matrix[0][1] * position.getY() + matrix[0][2],
                    matrix[1][0] * position.getX() + matrix[1][1] * position.getY() + matrix[1][2]
            );
        }
    }

    /**
     * Interpret a matrix value.
     *
     * @param attr Attribute value
     */
    private double[][] interpretMatrix(String attr) {
        if (attr == null) {
            return null;
        }

        String[] split = attr.split(" ");

        return new double[][]{
            {Double.parseDouble(split[0]), Double.parseDouble(split[2]), Double.parseDouble(split[4])},
            {Double.parseDouble(split[1]), Double.parseDouble(split[3]), Double.parseDouble(split[5])}
        };
    }

    /**
     * Interpret a stroke-width value, possibly looking it up in the named
     * values.
     *
     * @param attr Attribute value
     */
    private double interpretPen(String attr) {
        if (attr == null) {
            attr = "normal"; // standard normal...
        }

        try {
            return Double.parseDouble(attr);
        } catch (NumberFormatException e) {
            if (_namedStrokewidths == null) {
                _namedStrokewidths = IPEDefaults.getStrokeWidths();
            }

            if (_namedStrokewidths.containsKey(attr)) {
                return _namedStrokewidths.get(attr);
            } else {
                Logger.getLogger(IPEReader.class.getName()).log(Level.WARNING, "Unexpected pen name: {0}", attr);
                return 1;
            }
        }
    }

    /**
     * Interpret a specified Circle from the line, applying the given matrix, if
     * any.
     *
     * @param line Line containing Circle description
     * @param matrix Matrix to be applied (if any)
     */
    private Circle interpretCircle(String line) {
        String[] parts = line.split(" ");
        double r = Vector.subtract(
                interpretPosition(parts[0], parts[1]),
                interpretPosition("0", "0")).length();
        Vector c = interpretPosition(parts[4], parts[5]);
        return new Circle(c, r);
    }

    /**
     * Interpret a specified CircularArc from the line, applying the given
     * matrix, if any.
     *
     * @param line Line containing CircularArc description
     * @param matrix Matrix to be applied (if any)
     */
    private CircularArc interpretCircularArc(String line, Vector prev) {
        String[] parts = line.split(" ");
        Vector origin = interpretPosition("0", "0");
        Vector arm1 = Vector.subtract(
                interpretPosition(parts[0], parts[1]),
                origin);
        Vector arm2 = Vector.subtract(
                interpretPosition(parts[2], parts[3]),
                origin);
        Vector center = interpretPosition(parts[4], parts[5]);
        Vector end = interpretPosition(parts[6], parts[7]);

        CircularArc arc;
        if (Vector.crossProduct(arm1, arm2) > 0) {
            // counter clockwise
            arc = new CircularArc(center, prev, end, true);
        } else {
            arc = new CircularArc(center, prev, end, false);
            // clockwise by default
        }
        return arc;
    }

    /**
     * Subclass to handle reading one curve of mixed polygonal and curved parts
     */
    private class PathReader {

        Vector _start;
        Vector _end;
        List<OrientedGeometry> _edges = new ArrayList();
        List<Vector> _polyline = new ArrayList();
        List<Vector> _controlpoints = null;

        void move(Vector point) {
            _polyline.add(point);
            _start = _end = point;
        }

        void lineTo(Vector point) {
            _polyline.add(point);
            _end = point;
        }

        /**
         * Shifts the polygonal representation to the edge list
         */
        void consolidate() {
            switch (_polyline.size()) {
                case 0:
                case 1:
                    // nothing to do
                    break;
                case 2:
                    _edges.add(new LineSegment(_polyline.get(0), _polyline.get(1)));
                    break;
                default:
                    _edges.add(new PolyLine(_polyline));
                    break;
            }
            _polyline = new ArrayList();
        }

        void arcTo(CircularArc arc) {
            consolidate();
            _edges.add(arc);

            _end = arc.getEnd();
            _polyline.add(_end);
        }

        void blank(Vector point) {
            if (_controlpoints == null) {
                _controlpoints = new ArrayList();
                _controlpoints.add(_end);
            }
            _controlpoints.add(point);
        }

        void curveTo(Vector point) {
            _controlpoints.add(point);
            _end = point;

            BezierCurve curve = new BezierCurve(_controlpoints);
            _controlpoints = null;

            if (_bezierSampling < 0) {
                consolidate();
                _edges.add(curve);
            } else {
                for (int i = 1; i <= _bezierSampling; i++) {
                    double t = i / (double) (_bezierSampling + 1);
                    _polyline.add(curve.getPointAt(t));
                }
            }
            _polyline.add(_end);
        }

        OrientedGeometry end() {    
            // shift the last polygonal bit into the edge list        
            consolidate();
            if (_edges.size() == 1) {
                // only one type of curve (single arc, single bezier, or single polyline)
                return _edges.get(0);
            } else {
                return new GeometryString(_edges);
            }
        }

        CyclicGeometry close() {
            if (_edges.isEmpty()) {
                // fully polygonal
                if (_start.isApproximately(_end)) {
                    // closed explicitly, trim
                    _polyline.remove(_polyline.size() - 1);
                }
                return new Polygon(_polyline);
            } else {
                // mixed, create line back to start if necessary
                if (!_start.isApproximately(_end)) {
                    lineTo(_start);
                }
                // shift the last polygonal bit into the edge list
                consolidate();
                return new GeometryCycle(_edges);
            }
        }
    }
    //</editor-fold>
}
