package cz.net21.ttulka.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Stop boundary output stream decorator class.
 *
 * @author ttulka
 */
public class StopBoundaryOutputStream extends BoundaryOutputStream {

    protected final byte[] stopBoundary;

    /**
     * Creates the stop boundary output stream based on a base output stream.
     * <p>
     * Uses the boundaries from <code>{@link BoundaryStreamConsts}</code>.
     *
     * @param outputStream the base output stream
     */
    public StopBoundaryOutputStream(OutputStream outputStream) {
        this(outputStream, BoundaryStreamConsts.BOUNDARY, BoundaryStreamConsts.STOP_BOUNDARY);
    }

    /**
     * Creates the stop boundary output stream based on a base output stream with explicit boundaries.
     *
     * @param outputStream the base output stream
     * @param boundary     the boundary
     * @param stopBoundary the stop boundary
     * @throws IllegalArgumentException when the boundary is a substring of the stop boundary or vice versa
     */
    public StopBoundaryOutputStream(OutputStream outputStream, byte[] boundary, byte[] stopBoundary) {
        super(outputStream, boundary);
        this.stopBoundary = stopBoundary;

        if (new String(boundary).contains(new String(stopBoundary)) || new String(stopBoundary).contains(new String(boundary))) {
            throw new IllegalArgumentException("The boundary cannot be a substring of the stop boundary or vice versa.");
        }
    }

    /**
     * Writes the super boundary into the base stream.
     *
     * @throws IOException if an I/O error occurs
     */
    public void stopBoundary() throws IOException {
        outputStream.write(stopBoundary);
    }
}
