package cz.net21.ttulka.io.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cz.net21.ttulka.io.BoundaryInputStream;
import cz.net21.ttulka.io.BoundaryStreamConsts;
import cz.net21.ttulka.io.SuperBoundaryInputStream;
import cz.net21.ttulka.io.SuperBoundaryOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author ttulka
 */
@RunWith(MockitoJUnitRunner.class)
public class SuperBoundaryStreamsTest {

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
        SuperBoundaryOutputStream sbos = new SuperBoundaryOutputStream(os);

        sbos.close();

        // Should close the base stream
        verify(os).close();
    }

    @Test
    public void basicOutputStreamTest() throws IOException {
        String boundaryString = new String(BoundaryStreamConsts.BOUNDARY);
        String superBoundaryString = new String(BoundaryStreamConsts.SUPER_BOUNDARY);
        String longString = generateLongString();

        writeStringStreams(tmpFile, "a", "bc", "def", longString);

        String content = readFileContent(tmpFile);

        assertThat(content, is("a" + boundaryString
                               + "bc" + boundaryString
                               + "def" + boundaryString
                               + longString + boundaryString
                               + superBoundaryString));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentOutputStream1Test() {
        OutputStream os = mock(OutputStream.class);
        new SuperBoundaryOutputStream(os, "xxx".getBytes(), "xx".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentOutputStream2Test() {
        OutputStream os = mock(OutputStream.class);
        new SuperBoundaryOutputStream(os, "xx".getBytes(), "xxx".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentOutputStream3Test() {
        OutputStream os = mock(OutputStream.class);
        new SuperBoundaryOutputStream(os, "xxx".getBytes(), "xxx".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentInputStream1Test() {
        InputStream is = mock(InputStream.class);
        new SuperBoundaryInputStream(is, "xxx".getBytes(), "xx".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentInputStream2Test() {
        InputStream is = mock(InputStream.class);
        new SuperBoundaryInputStream(is, "xx".getBytes(), "xxx".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentInputStream3Test() {
        InputStream is = mock(InputStream.class);
        new SuperBoundaryInputStream(is, "xxx".getBytes(), "xxx".getBytes());
    }

    @Test
    public void explicitBoundaryTest() throws IOException {
        String boundary = "--TEST_BOUNDARY--";
        String superBoundary = "--TEST_SUPER_BOUNDARY--";

        SuperBoundaryOutputStream sbos = null;
        try {
            sbos = new SuperBoundaryOutputStream(new FileOutputStream(tmpFile), boundary.getBytes(), superBoundary.getBytes());

            sbos.write("abc".getBytes());
            sbos.boundary();

            sbos.write("def".getBytes());
            sbos.boundary();

            sbos.write("ghi".getBytes());
            sbos.superBoundary();

        } finally {
            sbos.close();
        }

        String content = readFileContent(tmpFile);

        assertThat(content, is("abc" + boundary + "def" + boundary + "ghi" + superBoundary));
    }

    @Test
    public void oneCharacterBoundaryTest() throws IOException {
        String boundary = String.valueOf('\1');
        String superBoundary = String.valueOf('\2');

        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        writeStringStreams(tmpFile, boundary.getBytes(), superBoundary.getBytes(), strings);

        List<String> results = new ArrayList<String>();

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile), boundary.getBytes(), superBoundary.getBytes());

            for (InputStream is : sbis) {
                String res = readStream(is);
                results.add(res);
            }
        } finally {
            sbis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void superBoundaryLongerThanBoundaryTest() throws IOException {
        String boundary = "xx";
        String superBoundary = "xxx";

        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        writeStringStreams(tmpFile, boundary.getBytes(), superBoundary.getBytes(), strings);

        List<String> results = new ArrayList<String>();

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile), boundary.getBytes(), superBoundary.getBytes());

            for (InputStream is : sbis) {
                String res = readStream(is);
                results.add(res);
            }
        } finally {
            sbis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void superBoundaryShorterThanBoundaryTest() throws IOException {
        String boundary = "xxx";
        String superBoundary = "xx";

        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        writeStringStreams(tmpFile, boundary.getBytes(), superBoundary.getBytes(), strings);

        List<String> results = new ArrayList<String>();

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile), boundary.getBytes(), superBoundary.getBytes());

            for (InputStream is : sbis) {
                String res = readStream(is);
                results.add(res);
            }
        } finally {
            sbis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void superBoundaryWithoutPreviousBoundaryTest() throws IOException {
        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        List<String> toWrite = new ArrayList<String>();

        boolean first = true;
        for (String s : strings) {
            if (!first) {
                toWrite.add(new String(BoundaryStreamConsts.BOUNDARY));
            }
            first = false;

            toWrite.add(s);
        }
        toWrite.add(new String(BoundaryStreamConsts.SUPER_BOUNDARY));

        writeStrings(tmpFile, toWrite.toArray(new String[0]));

        List<String> results = new ArrayList<String>();

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile));

            for (InputStream is : sbis) {
                String res = readStream(is);
                results.add(res);
            }
        } finally {
            sbis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void superBoundaryWithPreviousBoundaryTest() throws IOException {
        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        List<String> toWrite = new ArrayList<String>();

        for (String s : strings) {
            toWrite.add(s);
            toWrite.add(new String(BoundaryStreamConsts.BOUNDARY));
        }
        toWrite.add(new String(BoundaryStreamConsts.SUPER_BOUNDARY));

        writeStrings(tmpFile, toWrite.toArray(new String[0]));

        List<String> results = new ArrayList<String>();

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile));

            for (InputStream is : sbis) {
                String res = readStream(is);
                results.add(res);
            }
        } finally {
            sbis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void oneCharacterSuperBoundaryWithoutPreviousBoundaryTest() throws IOException {
        String boundary = String.valueOf('\1');
        String superBoundary = String.valueOf('\2');

        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        List<String> toWrite = new ArrayList<String>();

        boolean first = true;
        for (String s : strings) {
            if (!first) {
                toWrite.add(boundary);
            }
            first = false;

            toWrite.add(s);
        }
        toWrite.add(superBoundary);

        writeStrings(tmpFile, toWrite.toArray(new String[0]));

        List<String> results = new ArrayList<String>();

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile), boundary.getBytes(), superBoundary.getBytes());

            for (InputStream is : sbis) {
                String res = readStream(is);
                results.add(res);
            }
        } finally {
            sbis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void oneCharacterSuperBoundaryWithPreviousBoundaryTest() throws IOException {
        String boundary = String.valueOf('\1');
        String superBoundary = String.valueOf('\2');

        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        List<String> toWrite = new ArrayList<String>();

        for (String s : strings) {
            toWrite.add(s);
            toWrite.add(boundary);
        }
        toWrite.add(superBoundary);

        writeStrings(tmpFile, toWrite.toArray(new String[0]));

        List<String> results = new ArrayList<String>();

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile), boundary.getBytes(), superBoundary.getBytes());

            for (InputStream is : sbis) {
                String res = readStream(is);
                results.add(res);
            }
        } finally {
            sbis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void noBoundaryShortTest() throws IOException {
        String strings[] = {
                "a"
        };
        writeStringStreams(tmpFile, new byte[0], new byte[0], strings);

        List<String> results = new ArrayList<String>();

        BoundaryInputStream bis = null;
        try {
            bis = new BoundaryInputStream(new FileInputStream(tmpFile));

            for (InputStream is : bis) {
                String res = readStream(is);
                results.add(res);
            }
        } finally {
            bis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void noBoundaryLongTest() throws IOException {
        String strings[] = {
                generateLongString() + generateLongString()
        };
        writeStringStreams(tmpFile, new byte[0], new byte[0], strings);

        List<String> results = new ArrayList<String>();

        BoundaryInputStream bis = null;
        try {
            bis = new BoundaryInputStream(new FileInputStream(tmpFile));

            for (InputStream is : bis) {
                String res = readStream(is);
                results.add(res);
            }
        } finally {
            bis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void microStreamTest() throws IOException {
        String strings[] = {
                "a"
        };
        checkResults(tmpFile, strings);
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
                "a", "bc", "def"//, generateLongString(), generateLongString() + generateLongString()
        };
        writeStringStreams(tmpFile, strings);

        List<String> results = new ArrayList<String>();

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile));

            for (InputStream is : sbis) {
                String res = readStream(is);
                results.add(res);
            }
        } finally {
            sbis.close();
        }

        compareResults(strings, results.toArray(new String[0]));
    }

    @Test
    public void subStreamShouldReturnMinusOneAlwaysWhenIsFinishedTest() throws IOException {
        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        writeStringStreams(tmpFile, strings);

        String[] results = new String[strings.length];
        int streamIndex = 0;

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile));

            StringBuilder sb = new StringBuilder();
            int read;
            while (!sbis.hasFinished()) {
                sbis.next();
                while ((read = sbis.read()) != -1) {
                    sb.append((char) read);
                }
                assertThat(sbis.read(), is(-1));
                assertThat(sbis.read(), is(-1));
                assertThat(sbis.read(), is(-1));
                assertThat(sbis.read(), is(-1));
                assertThat(sbis.read(), is(-1));

                results[streamIndex] = sb.toString();
                sb = new StringBuilder();
                streamIndex++;

                assertThat(sbis.read(), is(-1));
                assertThat(sbis.read(), is(-1));
                assertThat(sbis.read(), is(-1));
                assertThat(sbis.read(), is(-1));
                assertThat(sbis.read(), is(-1));
            }
        } finally {
            sbis.close();
        }
    }

    @Test
    public void hasNextMustBeIdempotentTest() throws IOException {
        String strings[] = {
                "a", "bc", "def", generateLongString(), generateLongString() + generateLongString()
        };
        writeStringStreams(tmpFile, strings);

        List<String> results = new ArrayList<String>();

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile));
            Iterator<InputStream> it = sbis.iterator();

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
            sbis.close();
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

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile));
            Iterator<InputStream> it = sbis.iterator();

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
            sbis.close();
        }
    }

    @Test
    public void imageDataTest() throws IOException {
        SuperBoundaryOutputStream sbos = null;
        try {
            sbos = new SuperBoundaryOutputStream(new FileOutputStream(tmpFile));

            IOUtils.copy(SuperBoundaryStreamsTest.class.getResourceAsStream("/image1.jpeg"), sbos);
            sbos.boundary();
            IOUtils.copy(SuperBoundaryStreamsTest.class.getResourceAsStream("/image2.jpeg"), sbos);
            sbos.boundary();
            IOUtils.copy(SuperBoundaryStreamsTest.class.getResourceAsStream("/image3.jpeg"), sbos);
            sbos.boundary();

        } finally {
            sbos.close();
        }

        checkImagesStream();
    }

    @Test
    public void imageDataNoBoundaryAtEndTest() throws IOException {
        SuperBoundaryOutputStream sbos = null;
        try {
            sbos = new SuperBoundaryOutputStream(new FileOutputStream(tmpFile));

            IOUtils.copy(SuperBoundaryStreamsTest.class.getResourceAsStream("/image1.jpeg"), sbos);
            sbos.boundary();
            IOUtils.copy(SuperBoundaryStreamsTest.class.getResourceAsStream("/image2.jpeg"), sbos);
            sbos.boundary();
            IOUtils.copy(SuperBoundaryStreamsTest.class.getResourceAsStream("/image3.jpeg"), sbos);
            // bos.superBoundary(); // no superBoundary at the end

        } finally {
            sbos.close();
        }

        checkImagesStream();
    }

    private void checkImagesStream() throws IOException {
        File[] imageFiles = {tmpFolder.newFile(), tmpFolder.newFile(), tmpFolder.newFile()};

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(tmpFile));

            int streamIndex = 0;

            for (InputStream is : sbis) {
                FileOutputStream fos = new FileOutputStream(imageFiles[streamIndex]);
                IOUtils.copy(is, fos);
                fos.close();

                streamIndex++;
            }

        } finally {
            sbis.close();
        }

        long[] imageSizes = {34948L, 80846L, 29260L};

        for (int i = 0; i < imageFiles.length; i++) {
            assertThat(imageFiles[i].length(), is(imageSizes[i]));
        }
    }

    private String readStream(InputStream is) throws IOException {
        return IOUtils.toString(is, Charset.defaultCharset());
    }

    private void checkResults(File file, String strings[]) throws IOException {
        writeStringStreams(file, strings);

        String[] results = new String[strings.length];
        int streamIndex = 0;

        SuperBoundaryInputStream sbis = null;
        try {
            sbis = new SuperBoundaryInputStream(new FileInputStream(file));

            StringBuilder sb = new StringBuilder();
            int read;
            while (!sbis.hasFinished()) {
                sbis.next();
                while ((read = sbis.read()) != -1) {
                    sb.append((char) read);
                }
                results[streamIndex] = sb.toString();
                sb = new StringBuilder();
                streamIndex++;
            }
        } finally {
            sbis.close();
        }

        assertThat(streamIndex, is(strings.length));

        compareResults(strings, results);
    }

    private void compareResults(String strings[], String results[]) {
        assertThat(results.length, is(strings.length));

        for (int i = 0; i < strings.length; i++) {
            assertThat(results[i], is(strings[i]));
        }
    }

    // three times longer than the superBoundary
    private String generateLongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < BoundaryStreamConsts.BOUNDARY.length * 3; i++) {
            sb.append((char) ('0' + i));
        }
        return sb.toString();
    }

    private void writeStrings(File file, String... strings) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);

            for (String str : strings) {
                os.write(str.getBytes());
            }

        } finally {
            os.close();
        }
    }

    private void writeStringStreams(File file, String... strings) throws IOException {
        SuperBoundaryOutputStream sbos = null;
        try {
            sbos = new SuperBoundaryOutputStream(new FileOutputStream(file));

            for (String str : strings) {
                sbos.write(str.getBytes());
                sbos.boundary();
            }

            sbos.superBoundary();

        } finally {
            sbos.close();
        }
    }

    private void writeStringStreams(File file, byte[] boundary, byte[] superBoundary, String... strings) throws IOException {
        SuperBoundaryOutputStream sbos = null;
        try {
            sbos = new SuperBoundaryOutputStream(new FileOutputStream(file), boundary, superBoundary);

            for (String str : strings) {
                sbos.write(str.getBytes());
                sbos.boundary();
            }

            sbos.superBoundary();

        } finally {
            sbos.close();
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
