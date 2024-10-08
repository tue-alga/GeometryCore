/*
 * GeometryCore library   
 * Copyright (C) 2023   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.io.ipe;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to run commands through the commandline via IPE's native auxiliary
 * commands. This requires IPE to be installed and is therefore platform and
 * installation specific. As such, it is not very well suited to general-use
 * programs, but can be useful in code of experiments to e.g. convert output
 * automatically to PDFs and PNGs.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class IPECommands {

    private final String _ipeDir;

    /**
     * Creates an IPECommands using the given directory of IPE.
     *
     * @param ipeDir Full or relative path to the \bin\ folder of IPE.
     * @return a new IPECommands instance, or null if the folder does not exist
     * or does not contain ipetoipe.
     */
    public static IPECommands create(String ipeDir) {

        File dir = new File(ipeDir);
        if (!dir.exists()) {

            return null;
        }

        
        if (// windows
                !new File(dir, "ipetoipe.exe").exists()
                // linux/mac?
                && !new File(dir, "ipetoipe").exists()
                && !new File(dir, "ipetoipe.bin").exists()) {
            return null;
        }

        return new IPECommands(ipeDir);
    }

    private IPECommands(String ipeDir) {
        _ipeDir = ipeDir;
    }

    /**
     * Converts the given IPE file to a PDF file. This command uses the same
     * location and name, replacing the ipe-extension with a pdf-extension.
     *
     * @param ipe The source IPE file
     */
    public void convertIPEtoPDF(File ipe) {
        if (ipe.getName().endsWith(".ipe")) {
            convertIPEtoPDF(ipe, new File(ipe.getParentFile(), ipe.getName().replace(".ipe", ".pdf")));
        } else {
            Logger.getLogger(IPECommands.class.getName()).log(Level.WARNING, null, "No .ipe extension to replace; aborting conversion.");
        }
    }

    /**
     * Converts the given IPE file to a PDF file, at the given file.
     *
     * @param ipe The source IPE file
     * @param pdf The target PDF file (does not have to exist yet)
     */
    public void convertIPEtoPDF(File ipe, File pdf) {
        try {
            Runtime.getRuntime().exec(_ipeDir + "ipetoipe -pdf \"" + ipe.getAbsolutePath() + "\" \"" + pdf.getCanonicalPath() + "\"");
        } catch (IOException ex) {
            Logger.getLogger(IPECommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Converts the given IPE file to a PNG file.This command uses the same
     * location and name, replacing the ipe-extension with a png-extension.
     *
     * @param ipe The source IPE file
     * @param resolution The target resolution in pixels/inch
     * @param crop Set to true to crop the PNG file to the bounding box of its
     * contents; otherwise, the full page is rendered.
     */
    public void convertIPEtoPNG(File ipe, int resolution, boolean crop) {

        if (ipe.getName().endsWith(".ipe")) {
            convertIPEtoPNG(ipe, new File(ipe.getParentFile(), ipe.getName().replace(".ipe", ".png")), resolution, crop);
        } else {
            Logger.getLogger(IPECommands.class.getName()).log(Level.WARNING, null, "No .ipe extension to replace; aborting conversion.");
        }
    }

    
    /**
     * Converts the given IPE file to a PNG file.
     *
     * @param ipe The source IPE file
     * @param png The target PNG file.
     * @param resolution The target resolution in pixels/inch
     * @param crop Set to true to crop the PNG file to the bounding box of its
     * contents; otherwise, the full page is rendered.
     */
    public void convertIPEtoPNG(File ipe, File png, int resolution, boolean crop) {
        try {
            Runtime.getRuntime().exec(_ipeDir + "iperender -png -resolution " + resolution + (crop ? " " : " -nocrop") + " -transparent \"" + ipe.getAbsolutePath() + "\" \"" + png.getCanonicalPath() + "\"");
        } catch (IOException ex) {
            Logger.getLogger(IPECommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
