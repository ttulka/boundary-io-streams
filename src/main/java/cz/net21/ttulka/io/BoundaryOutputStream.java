package cz.net21.ttulka.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Boundary output stream decorator class.
 *
 * @author ttulka
 */
public class BoundaryOutputStream extends OutputStream {

    protected final OutputStream outputStream;
    protected final byte[] boundary;

    /**
     * Creates the boundary output stream based on a base output stream.
     * <p>
     * Uses the boundary from <code>{@link BoundaryStreamConsts}</code>.
     *
     * @param outputStream the base output stream
     */
    public BoundaryOutputStream(OutputStream outputStream) {
        this(outputStream, BoundaryStreamConsts.BOUNDARY);
    }

    /**
     * Creates the boundary output stream based on a base output stream with an explicit boundary.
     *
     * @param outputStream the base output stream
     * @param boundary     the boundary
     */
    public BoundaryOutputStream(OutputStream outputStream, byte[] boundary) {
        super();
        this.outputStream = outputStream;
        this.boundary = boundary;
    }

    /**
     * Writes the boundary into the base stream.
     *
     * @throws IOException if an I/O error occurs
     */
    public void boundary() throws IOException {
        outputStream.write(boundary);
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void close() throws IOException {
        super.close();
        outputStream.close();
    }
}
