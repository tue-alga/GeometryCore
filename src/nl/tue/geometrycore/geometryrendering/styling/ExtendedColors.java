/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.styling;

import java.awt.Color;

/**
 * Provided a (new) set of standard colors as well as some simple constructors
 * for colors from double values.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ExtendedColors {

    //<editor-fold defaultstate="collapsed" desc="STANDARD COLORS">
    // for sake of inclusion
    public static final Color black = Color.black;
    public static final Color white = Color.white;

    // IPE Grays
    public static final Color lightGray = fromUnitGray(0.827);
    public static final Color gray = fromUnitGray(0.745);
    public static final Color darkGray = fromUnitGray(0.663);

    // Color Brewer scheme https://colorbrewer2.org/#type=qualitative&scheme=Paired&n=10)
    public static final Color lightBlue = fromUnitRGB(0.651, 0.8078, 0.8902);
    public static final Color darkBlue = fromUnitRGB(0.1216, 0.4706, 0.7059);
    public static final Color lightGreen = fromUnitRGB(0.698, 0.8745, 0.5412);
    public static final Color darkGreen = fromUnitRGB(0.2, 0.6275, 0.1725);
    public static final Color lightRed = fromUnitRGB(0.9843, 0.6039, 0.6);
    public static final Color darkRed = fromUnitRGB(0.8902, 0.102, 0.1098);
    public static final Color lightOrange = fromUnitRGB(0.9922, 0.749, 0.4353);
    public static final Color darkOrange = fromUnitRGB(1, 0.498, 0);
    public static final Color lightPurple = fromUnitRGB(0.7922, 0.698, 0.8392);
    public static final Color darkPurple = fromUnitRGB(0.4157, 0.2392, 0.6039);
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="COLOR SCHEMES"> 
    /**
     * The 10-color paired ColorBrewer scheme. See https://colorbrewer2.org/#type=qualitative&scheme=Paired&n=10
     */
    public static final Color[] paired = {
        lightBlue, darkBlue,
        lightGreen, darkGreen,
        lightRed, darkRed,
        lightOrange, darkOrange,
        lightPurple, darkPurple};
    
    /**
     * The 5 dark colors from "paired"
     */
    public static final Color[] dark = {
        darkBlue,
        darkGreen,
        darkRed,
        darkOrange,
        darkPurple};
    
    /**
     * The 5 light colors from "paired"
     */
    public static final Color[] light = {
        lightBlue,
        lightGreen,
        lightRed,
        lightOrange,
        lightPurple};
    
    /** 
     * A set of colors that is useful for coloring regions in maps.
     */
    public static final Color[] cartographic = {
        ExtendedColors.fromUnitRGB(0.145, 0.737, 0.612),
        ExtendedColors.fromUnitRGB(0.533, 0.78, 0.396),
        ExtendedColors.fromUnitRGB(0.561, 0.737, 0.757),
        ExtendedColors.fromUnitRGB(0.604, 0.839, 0.741),
        ExtendedColors.fromUnitRGB(0.706, 0.592, 0.506),
        ExtendedColors.fromUnitRGB(0.733, 0.718, 0.349),
        ExtendedColors.fromUnitRGB(0.831, 0.878, 0.353),
        ExtendedColors.fromUnitRGB(0.835, 0.725, 0.541),
        ExtendedColors.fromUnitRGB(0.867, 0.529, 0.475),
        ExtendedColors.fromUnitRGB(0.996, 0.965, 0.608),
        ExtendedColors.fromUnitRGB(0.996, 0.859, 0.706),
        ExtendedColors.fromUnitRGB(0.980, 0.714, 0.58),
        ExtendedColors.fromUnitRGB(1.000, 0.8, 0.302)};
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="COLOR CONSTRUCTORS">
    public static Color fromUnitRGB(double r, double g, double b) {
        return new Color((float) r, (float) g, (float) b);
    }

    public static Color fromUnitGray(double gray) {
        return fromUnitRGB(gray, gray, gray);
    }
    //</editor-fold>
}
