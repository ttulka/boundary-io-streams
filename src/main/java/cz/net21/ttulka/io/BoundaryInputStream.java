package cz.net21.ttulka.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Boundary input stream decorator class.
 *
 * @author ttulka
 */
public class BoundaryInputStream extends InputStream {

    protected final InputStream inputStream;
    protected final byte[] boundary;

    /**
     * Creates the boundary input stream based on a base input stream.
     * <p>
     * Uses the boundary from <code>{@link BoundaryStreamConsts}</code>.
     *
     * @param inputStream the base input stream
     */
    public BoundaryInputStream(InputStream inputStream) {
        this(inputStream, BoundaryStreamConsts.BOUNDARY);
    }

    /**
     * Creates the boundary input stream based on a base input stream with an explicit boundary.
     *
     * @param inputStream the base input stream
     * @param boundary    the boundary
     */
    public BoundaryInputStream(InputStream inputStream, byte[] boundary) {
        this.inputStream = inputStream;
        this.boundary = boundary;
    }

    /**
     * Moves to the next sub-stream.
     */
    public void next() {
        // TODO
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an <code>int</code> in the range <code>0</code> to <code>255</code>. If
     * no byte is available because the boundary of the current sub-stream has been reached, the value <code>-1</code> is returned. If no byte is available
     * because the end of the base stream has been reached, the value <code>-2</code> is returned. This method blocks until input data is available, the end of
     * the stream is detected, or an exception is thrown.
     *
     * @return the next byte of data, or <code>-1</code> if the boundary of the current sub-stream is reached, or <code>-2</code> if the end of the base stream
     * is reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        return 0;   // TODO
    }

    @Override
    public void close() throws IOException {
        super.close();
        inputStream.close();
    }
}
