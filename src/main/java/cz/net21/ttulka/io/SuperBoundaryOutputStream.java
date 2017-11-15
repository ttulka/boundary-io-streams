package cz.net21.ttulka.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Super Boundary output stream decorator class.
 *
 * @author ttulka
 */
public class SuperBoundaryOutputStream extends BoundaryOutputStream {

    protected final byte[] superBoundary;

    /**
     * Creates the superBoundary output stream based on a base output stream.
     * <p>
     * Uses the superBoundary from <code>{@link BoundaryStreamConsts}</code>.
     *
     * @param outputStream the base output stream
     */
    public SuperBoundaryOutputStream(OutputStream outputStream) {
        this(outputStream, BoundaryStreamConsts.BOUNDARY, BoundaryStreamConsts.SUPER_BOUNDARY);
    }

    /**
     * Creates the superBoundary output stream based on a base output stream with an explicit superBoundary.
     *
     * @param outputStream  the base output stream
     * @param boundary      the superBoundary
     * @param superBoundary the super superBoundary
     */
    public SuperBoundaryOutputStream(OutputStream outputStream, byte[] boundary, byte[] superBoundary) {
        super(outputStream, boundary);
        this.superBoundary = superBoundary;
    }

    /**
     * Writes the super superBoundary into the base stream.
     *
     * @throws IOException if an I/O error occurs
     */
    public void superBoundary() throws IOException {
        outputStream.write(superBoundary);
    }
}
