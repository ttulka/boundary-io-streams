package cz.net21.ttulka.io;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Iterator wrapper class for the boundary input stream.
 *
 * @author ttulka
 */
class BoundaryInputStreamIterator implements Iterator<InputStream> {

    protected final BoundaryInputStream boundaryInputStream;

    /**
     * Creates the boundary input stream iterator based on a boundary input stream.
     *
     * @param boundaryInputStream the boundary input stream
     */
    public BoundaryInputStreamIterator(BoundaryInputStream boundaryInputStream) {
        super();
        this.boundaryInputStream = boundaryInputStream;
    }

    @Override
    public boolean hasNext() {
        return !boundaryInputStream.hasFinished();
    }

    @Override
    public InputStream next() {
        boundaryInputStream.next();
        return boundaryInputStream;
    }

    @Override
    public void remove() {
        throw new IllegalStateException("Cannot remove from a stream.");
    }
}
