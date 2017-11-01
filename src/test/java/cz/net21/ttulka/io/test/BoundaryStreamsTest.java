package cz.net21.ttulka.io.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cz.net21.ttulka.io.BoundaryInputStream;
import cz.net21.ttulka.io.BoundaryOutputStream;
import cz.net21.ttulka.io.BoundaryStreamConsts;
import cz.net21.ttulka.io.IterableBoundaryInputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author ttulka
 */
@RunWith(MockitoJUnitRunner.class)
public class BoundaryStreamsTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private File tmpFile;

    @Before
    public void setUp() throws IOException {
        this.tmpFile = tmpFolder.newFile();
    }

    @Test
    public void closeInputStreamTest() throws IOException {
        InputStream is = mock(InputStream.class);
        BoundaryInputStream bis = new BoundaryInputStream(is);

        bis.close();

        // Should close the base stream
        verify(is).close();
    }

    @Test
    public void closeOutputStreamTest() throws IOException {
        OutputStream os = mock(OutputStream.class);
        BoundaryOutputStream bos = new BoundaryOutputStream(os);

        bos.close();

        // Should close the base stream
        verify(os).close();
    }

    @Test
    public void basicOutputStreamTest() throws IOException {
        String boundaryString = new String(BoundaryStreamConsts.BOUNDARY);
        String longString = generateLongString();

        writeStringStreams(tmpFile, "a", "bc", "def", longString);

        String content = readFileContent(tmpFile);

        assertThat(content, is("a" + boundaryString
                               + "bc" + boundaryString
                               + "def" + boundaryString
                               + longString + boundaryString));
    }

    @Test
    public void explicitBoundaryTest() throws IOException {
        String boundary = "--TEST_BOUNDARY--";

        BoundaryOutputStream bos = null;
        try {
            bos = new BoundaryOutputStream(new FileOutputStream(tmpFile), boundary.getBytes());

            bos.write("abc".getBytes());
            bos.boundary();

            bos.write("def".getBytes());
            bos.boundary();

        } finally {
            bos.close();
        }

        String content = readFileContent(tmpFile);

        assertThat(content, is("abc" + boundary + "def" + boundary));
    }

    @Test
    public void shortStreamsTest() throws IOException {
        String strings[] = {
                "a", "bc", "def", generateLongString()
        };
        checkResults(tmpFile, strings);
    }

    @Test
    public void longStreamsTest() throws IOException {
        String strings[] = {
                generateLongString(), "a", generateLongString(), generateLongString() + generateLongString()
        };
        checkResults(tmpFile, strings);
    }

    @Test
    public void iterableTest() throws IOException {
        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        writeStringStreams(tmpFile, strings);

        List<String> results = new ArrayList<String>();

        BoundaryInputStream bis = null;
        try {
            IterableBoundaryInputStream ibis = new IterableBoundaryInputStream(bis);
            for (InputStream is : ibis) {
                results.add(readStream(is));
            }
        } finally {
            bis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void subStreamShouldReturnMinusOneAlwaysWhenIsFinishedTest() throws IOException {
        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        writeStringStreams(tmpFile, strings);

        String[] results = new String[4];
        int streamIndex = 0;

        BoundaryInputStream bis = null;
        try {
            StringBuilder sb = new StringBuilder();
            int read;
            while ((read = bis.read()) != -2) {
                if (read != -1) {
                    sb.append((char) read);
                } else {
                    assertThat(bis.read(), is(-1));
                    assertThat(bis.read(), is(-1));
                    assertThat(bis.read(), is(-1));
                    assertThat(bis.read(), is(-1));
                    assertThat(bis.read(), is(-1));

                    results[streamIndex] = sb.toString();
                    sb = new StringBuilder();
                    streamIndex++;

                    assertThat(bis.read(), is(-1));
                    assertThat(bis.read(), is(-1));
                    assertThat(bis.read(), is(-1));
                    assertThat(bis.read(), is(-1));
                    assertThat(bis.read(), is(-1));

                    bis.next();
                }
            }
        } finally {
            bis.close();
        }
    }

    @Test
    public void hasNextMustBeIdempotentTest() throws IOException {
        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        writeStringStreams(tmpFile, strings);

        List<String> results = new ArrayList<String>();

        BoundaryInputStream bis = null;
        try {
            Iterator<InputStream> it = new IterableBoundaryInputStream(bis).iterator();
            while (it.hasNext()) {
                it.hasNext();
                it.hasNext();
                it.hasNext();
                it.hasNext();
                it.hasNext();

                results.add(readStream(it.next()));

                it.hasNext();
                it.hasNext();
                it.hasNext();
                it.hasNext();
                it.hasNext();
            }
        } finally {
            bis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test(expected = NoSuchElementException.class)
    public void nextMustSkipCurrentSubStreamTest() throws IOException {
        String str4 = generateLongString();
        String str5 = generateLongString() + generateLongString();
        String strings[] = {
                "a", "bc", "def", str4, str5
        };
        writeStringStreams(tmpFile, strings);

        BoundaryInputStream bis = null;
        try {
            Iterator<InputStream> it = new IterableBoundaryInputStream(bis).iterator();

            assertThat(it.hasNext(), is(true));
            // skip the first stream
            it.next();

            assertThat(it.hasNext(), is(true));
            assertThat(readStream(it.next()), is("bc"));

            // skip the next two streams
            it.next();
            it.next();

            assertThat(it.hasNext(), is(true));
            assertThat(readStream(it.next()), is(str5));

            assertThat(it.hasNext(), is(false));

            it.next();  // NoSuchElementException

        } finally {
            bis.close();
        }
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        int read;
        while ((read = is.read()) != -1) {
            sb.append((char) read);
        }
        return sb.toString();
    }

    private void checkResults(File file, String strings[]) throws IOException {
        writeStringStreams(file, strings);

        String[] results = new String[4];
        int streamIndex = 0;

        BoundaryInputStream bis = null;
        try {
            StringBuilder sb = new StringBuilder();
            int read;
            while ((read = bis.read()) != -2) {
                if (read != -1) {
                    sb.append((char) read);
                } else {
                    results[streamIndex] = sb.toString();
                    sb = new StringBuilder();
                    streamIndex++;

                    bis.next();
                }
            }
        } finally {
            bis.close();
        }

        assertThat(streamIndex, is(strings.length));

        compareResults(strings, results);
    }

    private void compareResults(String strings[], String results[]) {
        for (int i = 0; i < strings.length; i++) {
            assertThat(strings[i], is(results[i]));
        }
    }

    // three times longer than the boundary
    private String generateLongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < BoundaryStreamConsts.BOUNDARY.length * 3; i++) {
            sb.append((char) ('a' + i));
        }
        return sb.toString();
    }

    private void writeStringStreams(File file, String... strings) throws IOException {
        BoundaryOutputStream bos = null;
        try {
            bos = new BoundaryOutputStream(new FileOutputStream(file));

            for (String str : strings) {
                bos.write(str.getBytes());
                bos.boundary();
            }

        } finally {
            bos.close();
        }
    }

    private String readFileContent(File file) throws FileNotFoundException {
        Scanner scanner = null;
        try {
            scanner = new Scanner(file).useDelimiter("\\Z");
            return scanner.next();

        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}
