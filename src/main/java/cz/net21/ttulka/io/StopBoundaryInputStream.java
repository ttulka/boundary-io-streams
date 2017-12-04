package cz.net21.ttulka.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

/**
 * Stop boundary input stream decorator class.
 *
 * @author ttulka
 */
public class StopBoundaryInputStream extends BoundaryInputStream {

    protected final int[] stopBoundary;

    /**
     * Creates the stop boundary input stream based on a base input stream.
     * <p>
     * Uses the boundaries from <code>{@link BoundaryStreamConsts}</code>.
     *
     * @param inputStream the base input stream
     */
    public StopBoundaryInputStream(InputStream inputStream) {
        this(inputStream, BoundaryStreamConsts.BOUNDARY, BoundaryStreamConsts.STOP_BOUNDARY);
    }

    /**
     * Creates the stop boundary input stream based on a base input stream with explicit boundaries.
     *
     * @param inputStream  the base input stream
     * @param boundary     the boundary
     * @param stopBoundary the stop boundary
     */
    public StopBoundaryInputStream(InputStream inputStream, byte[] boundary, byte[] stopBoundary) {
        super(inputStream, boundary, Math.max(boundary.length, stopBoundary.length));
        this.stopBoundary = copyBoundary(stopBoundary);

        if (new String(boundary).contains(new String(stopBoundary)) || new String(stopBoundary).contains(new String(boundary))) {
            throw new IllegalArgumentException("The boundary cannot be a substring of the stop boundary or vice versa.");
        }
    }

    /**
     * Returns true if the stream has already reached the stopBoundary or EOF.
     *
     * @return true if the stream finished, otherwise false
     */
    @Override
    public boolean hasFinished() {
        return super.hasFinished();
    }

    /**
     * Moves to the next sub-stream. This method must be called always when a current stream reaches <code>-1</code>.
     *
     * @throws NoSuchElementException when no more sub-streams
     */
    @Override
    public void next() {
        super.next();
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an <code>int</code> in the range <code>0</code> to <code>255</code>. If
     * no byte is available because the boundary of the current sub-stream has been reached, the stopBoundary has been reached, or no byte is available because
     * the end of the base stream has been reached, the value <code>-1</code> is returned. This method blocks until input data is available, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return the next byte of data, or <code>-1</code> if the boundary of the current sub-stream is reached, the stopBoundary is reached, or if the end of the
     * base stream is reached
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read() throws IOException {
        if (finished) {
            return -1;
        }
        if (endOfCurrentStream) {
            return -1;
        }

        initBuffer();

        // are we at the stopBoundary?
        if (startsWith(buffer, stopBoundary)) {
            endOfCurrentStream = true;
            finished = true;

            return -1;
        }
        // are we at the boundary?
        if (startsWith(buffer, boundary)) {
            endOfCurrentStream = true;

            // remove this boundary bytes and read the rest
            for (int i = 0; i < boundary.length; i++) {
                readByteToBufferAndGet();
            }

            if (buffer[0] == -1 || startsWith(buffer, stopBoundary)) {
                finished = true;
            }

            return -1;
        }

        // read always from the top of the buffer
        int currentByte = readByteToBufferAndGet();

        // finish the reading
        if (currentByte == -1) {
            endOfCurrentStream = true;
            finished = true;

            return -1;
        }

        return currentByte;
    }

    private boolean startsWith(int[] buffer, int[] prefix) {
        for (int i = 0; i < prefix.length; i++) {
            if (buffer[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
