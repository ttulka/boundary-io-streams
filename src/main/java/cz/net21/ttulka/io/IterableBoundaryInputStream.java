package cz.net21.ttulka.io;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Iterable wrapper class for the input stream.
 *
 * @author ttulka
 */
public class IterableBoundaryInputStream implements Iterable<InputStream> {

    protected final BoundaryInputStream boundaryInputStream;

    /**
     * Creates the iterable boundary input stream based on a boundary input stream.
     *
     * @param boundaryInputStream the boundary input stream
     */
    public IterableBoundaryInputStream(BoundaryInputStream boundaryInputStream) {
        super();
        this.boundaryInputStream = boundaryInputStream;
    }

    @Override
    public Iterator<InputStream> iterator() {
        return new Iterator<InputStream>() {
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
        };
    }
}
