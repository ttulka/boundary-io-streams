package cz.net21.ttulka.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Boundary input stream decorator class.
 *
 * @author ttulka
 */
public class BoundaryInputStream extends InputStream implements Iterable<InputStream> {

    protected final InputStream inputStream;
    protected final int[] boundary;

    private final BoundaryInputStreamIterator iterator;

    private final int[] buffer;

    private boolean finished = false;
    private boolean started = false;

    private int bufferIndex = 0;
    private boolean endOfCurrentStream = false;

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
        this.buffer = new int[boundary.length];
        this.boundary = new int[boundary.length];

        for (int i = 0; i < boundary.length; i++) {
            this.boundary[i] = (int) boundary[i];
        }

        this.iterator = new BoundaryInputStreamIterator(this);
    }

    /**
     * Returns true if the stream has already reached EOF.
     *
     * @return true if the steam finished, otherwise false
     */
    public boolean hasFinished() {
        return finished;
    }

    /**
     * Moves to the next sub-stream. This method must be called always when a current stream reaches <code>-1</code>.
     *
     * @throws NoSuchElementException when no more sub-streams
     */
    public void next() {
        if (finished) {
            throw new NoSuchElementException("Stream already finished.");
        }
        if (started && !endOfCurrentStream) {
            consumeCurrentStream();
        }
        started = true;
        endOfCurrentStream = false;
    }

    private void consumeCurrentStream() {
        try {
            while ((this.read()) != -1) {
                ;
            }
        } catch (IOException e) {
            endOfCurrentStream = true;
            finished = true;
        }
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an <code>int</code> in the range <code>0</code> to <code>255</code>. If
     * no byte is available because the boundary of the current sub-stream has been reached, or no byte is available because the end of the base stream has been
     * reached, the value <code>-1</code> is returned. This method blocks until input data is available, the end of the stream is detected, or an exception is
     * thrown.
     *
     * @return the next byte of data, or <code>-1</code> if the boundary of the current sub-stream is reached, or if the end of the base stream is reached
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
        for (int i = bufferIndex; i < buffer.length; i++) {
            buffer[i] = inputStream.read();

            if (buffer[i] == -1) {
                break;
            }
            bufferIndex++;
            if (bufferIndex >= buffer.length) {
                bufferIndex = buffer.length - 1;
            }
        }

        // are we at the boundary?
        if (Arrays.equals(buffer, boundary)) {
            endOfCurrentStream = true;

            // read one next byte to check if not EOF
            buffer[0] = inputStream.read();
            if (buffer[0] == -1) {
                finished = true;
            }
            bufferIndex = 1;    // next write as the second byte

            return -1;
        }

        // after the buffer is filled, write always on the end
        bufferIndex = buffer.length - 1;

        // read always from the top of the buffer
        int currentByte = buffer[0];

        // finish the reading
        if (currentByte == -1) {
            endOfCurrentStream = true;
            finished = true;
            return -1;
        }

        // shift the buffer to the top (first byte will be returned)
        System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);

        return currentByte;
    }

    @Override
    public BoundaryInputStreamIterator iterator() {
        return this.iterator;
    }

    @Override
    public void close() throws IOException {
        endOfCurrentStream = true;
        finished = true;

        super.close();
        inputStream.close();
    }
}
