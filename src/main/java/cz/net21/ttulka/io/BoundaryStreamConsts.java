package cz.net21.ttulka.io;

/**
 * Constants class.
 *
 * @author ttulka
 */
public final class BoundaryStreamConsts {

    public static final byte[] BOUNDARY = "-----StreamBoundary-----".getBytes();

    private BoundaryStreamConsts() {
        throw new IllegalStateException("Cannot create an instance of this class.");
    }
}
