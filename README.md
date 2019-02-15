GeometryCore is a java library to quickly develop prototypes for geometric algorithms 
 and tools. It has five primary foci
 
(1) Standalone, quick to set up and portable
    This library does not depend on other libraries beyond the standard java 
    libraries. Being native java, it should readily work on most platforms.

(2) Providing some basic functionality for working with geometric objects in 2D
    See the nl.tue.geometrycore.geometry package for the implementation

(3) Ease of setting up a GUI with some default interaction
    See the nl.tue.geometrycore.gui / geometryrendering packages. Most important
     is to extend from GeometryPanel (geometryrendering package). You can 
    quickly create a JFrame using the function in GUIUtils (gui package). 
    Optionally, you can also provide a TabbedSidePanel (gui package) for quickly 
     creating a basic GUI in code.
     
(4) Ease of importing and exporting to IPE and IPE files
    IPEReader and IPEWriter are the main classes for this functionality 
    (nl.tue.geometrycore.io.ipe package). Note that the constructors are static 
    functions.
    
(5) Abstraction of rendering code from the target (GUI, IPE, SVG, PNG)
    The same BaseWriter (nl.tue.geometrycore.io package) is implemented for 
    different targets (Raster images, Swing, IPE and SVG formats). See the 
    respective subpackages. Also note that the GeometryPanel (focus 3) uses such 
     a base writer. You can re-use the drawScene() method for other targets by 
    creating the appropriate writer and calling render() on the GeometryPanel.
    
Beyond this core functionality, the library offers some basic implementations of 
 graphs, data structures and algorithms. 

