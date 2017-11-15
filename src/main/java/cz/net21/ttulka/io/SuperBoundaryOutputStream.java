package cz.net21.ttulka.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Super boundary output stream decorator class.
 *
 * @author ttulka
 */
public class SuperBoundaryOutputStream extends BoundaryOutputStream {

    protected final byte[] superBoundary;

    /**
     * Creates the super boundary output stream based on a base output stream.
     * <p>
     * Uses the boundaries from <code>{@link BoundaryStreamConsts}</code>.
     *
     * @param outputStream the base output stream
     */
    public SuperBoundaryOutputStream(OutputStream outputStream) {
        this(outputStream, BoundaryStreamConsts.BOUNDARY, BoundaryStreamConsts.SUPER_BOUNDARY);
    }

    /**
     * Creates the super boundary output stream based on a base output stream with explicit boundaries.
     *
     * @param outputStream  the base output stream
     * @param boundary      the boundary
     * @param superBoundary the super boundary
     * @throws IllegalArgumentException when the boundary is a substring of the super boundary or vice versa
     */
    public SuperBoundaryOutputStream(OutputStream outputStream, byte[] boundary, byte[] superBoundary) {
        super(outputStream, boundary);
        this.superBoundary = superBoundary;

        if (new String(boundary).contains(new String(superBoundary)) || new String(superBoundary).contains(new String(boundary))) {
            throw new IllegalArgumentException("The boundary cannot be a substring of the super boundary or vice versa.");
        }
    }

    /**
     * Writes the super boundary into the base stream.
     *
     * @throws IOException if an I/O error occurs
     */
    public void superBoundary() throws IOException {
        outputStream.write(superBoundary);
    }
}
