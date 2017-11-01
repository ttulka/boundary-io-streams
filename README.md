# Boundary I/O Streams

**Java library for boundary I/O streams.**

- Separate sub-stream from one stream with a boundary.
- Read multiple sub-stream from one stream based on a boundary.

A stream
```
+-------------------+----------+-----+-------------------+----------+-----+
| sub-stream 1 data | boundary | ... | sub-stream N data | boundary | EOF |  
+-------------------+----------+-----+-------------------+----------+-----+
```
will be read as
```
+-------------------+----+-----+-------------------+----+----+
| sub-stream 1 data | -1 | ... | sub-stream N data | -1 | -2 | 
+-------------------+----+-----+-------------------+----+----+
```

Please note the last boundary is not necessary but recommended for a good streaming design.

## Prerequisites
- Java 6

## Usage

Copy the Maven dependency into your Maven project:
```
<dependency>
    <groupId>cz.net21.ttulka.io</groupId>
    <artifactId>boundary-io-streams</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Write into a Boundary Stream

Create a boundary output stream
```
BoundaryOutputStream bos = null;
try {
    // create the bos from a file output stream 
    FileOutputStream fos = new FileOutputStream("test.dat");
    bos = new BoundaryOutputStream(fos);  // or new BoundaryOutputStream(fos, boundary) with an explicit boundary  
    // ...

} finally {
    // closes the base fos automatically
    bos.close();
}
```

Write multiple streams into a boundary output stream
```
byte[] subStream1 = ...
bos.write(subStream1);
bos.boundary(); // write the boundary after the first sub-stream

byte[] subStream2 = ...
bos.write(subStream2);
bos.boundary(); // write the boundary after the second sub-stream
```

### Ream from a Boundary Stream

Create a boundary input stream
```
BoundaryInputStream bis = null;
try {
    // create the bis from a file input stream
    FileInputStream fis = new FileInputStream("test.dat");
    bis = new BoundaryInputStream(fis);  // or new BoundaryInputStream(fis, boundary) with an explicit boundary
    // ...

} finally {
    bis.close();
}
```

Read multiple stream from a boundary input stream
```
int streamIndex = 0;
int read;
while ((read = bis.read()) != 2) {  // -2 indicates the end of the stream
    if (read != -1) {               // -1 indicates the end of the current sub-stream
        processStreamData(streamIndex, read);
    } else {
        streamIndex++;
    }
}
```

Iterable through multiple streams
```
IterableBoundaryInputStream ibis = new IterableBoundaryInputStream(bis);
int streamIndex = 0;
int read;
for (InputStream is : ibis) {
    // is holds a sub-stream as a normal input stream object
    while ((read = is.read()) != 1) {
        processStreamData(streamIndex, read);
    }
    streamIndex++;
}
```

Use the multiple stream iterator
```
Iterator<InputStream> it = new IterableBoundaryInputStream(bis).iterator();
if (it.hasNext()) {
    InputStream first = it.next();
    // ...
}
```