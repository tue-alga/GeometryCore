package nl.tue.geometrycore.io;

/**
 * This interface can be used to make it explicit that writers can add new
 * pages, possibly specified via a number of layers. The support for the latter
 * may be writer dependent.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public interface PagesWriter {

    public void newPage(String... layers);
}
