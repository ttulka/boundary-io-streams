package cz.net21.ttulka.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Super boundary input stream decorator class.
 *
 * @author ttulka
 */
public class SuperBoundaryInputStream extends BoundaryInputStream {

    protected final int[] superBoundary;

    private final int[] buffer;
    private boolean bufferFilled = false;

    /**
     * Creates the super boundary input stream based on a base input stream.
     * <p>
     * Uses the boundaries from <code>{@link BoundaryStreamConsts}</code>.
     *
     * @param inputStream the base input stream
     */
    public SuperBoundaryInputStream(InputStream inputStream) {
        this(inputStream, BoundaryStreamConsts.BOUNDARY, BoundaryStreamConsts.SUPER_BOUNDARY);
    }

    /**
     * Creates the super boundary input stream based on a base input stream with explicit boundaries.
     *
     * @param inputStream   the base input stream
     * @param boundary      the boundary
     * @param superBoundary the super boundary
     */
    public SuperBoundaryInputStream(InputStream inputStream, byte[] boundary, byte[] superBoundary) {
        super(inputStream, boundary);
        this.superBoundary = new int[superBoundary.length];

        if (new String(boundary).contains(new String(superBoundary)) || new String(superBoundary).contains(new String(boundary))) {
            throw new IllegalArgumentException("The boundary cannot be a substring of the super boundary or vice versa.");
        }

        this.buffer = new int[Math.max(boundary.length, superBoundary.length)];

        for (int i = 0; i < superBoundary.length; i++) {
            this.superBoundary[i] = (int) superBoundary[i];
        }
    }

    /**
     * Returns true if the stream has already reached the superBoundary or EOF.
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
     * no byte is available because the boundary of the current sub-stream has been reached, the superBoundary has been reached, or no byte is available because
     * the end of the base stream has been reached, the value <code>-1</code> is returned. This method blocks until input data is available, the end of the
     * stream is detected, or an exception is thrown.
     *
     * @return the next byte of data, or <code>-1</code> if the boundary of the current sub-stream is reached, the superBoundary is reached, or if the end of
     * the base stream is reached
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

        // fill the buffer
        if (!bufferFilled) {
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = inputStream.read();

                if (buffer[i] == -1) {
                    Arrays.fill(buffer, i + 1, buffer.length, -1);
                    break;
                }
            }
            bufferFilled = true;
        }

        // are we at the superBoundary?
        if (startsWith(buffer, superBoundary)) {
            endOfCurrentStream = true;
            finished = true;

            return -1;
        }
        // are we at the boundary?
        if (startsWith(buffer, boundary)) {
            endOfCurrentStream = true;

            // remove this boundary bytes and read the rest
            for (int i = 0; i < boundary.length; i++) {
                addToBuffer();
            }

            if (buffer[0] == -1 || startsWith(buffer, superBoundary)) {
                finished = true;
            }

            return -1;
        }

        // read always from the top of the buffer
        int currentByte = addToBuffer();

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

    private int addToBuffer() throws IOException {
        int currentByte = buffer[0];

        // shift the buffer to the top (first byte will be returned)
        System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);

        buffer[buffer.length - 1] = inputStream.read();

        return currentByte;
    }
}
