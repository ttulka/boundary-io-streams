# Boundary I/O Streams

**Java library for boundary I/O streams.**

- Separate sub-stream from one stream with a boundary.
- Read multiple sub-stream from one stream based on a boundary.

A stream
```
+-------------------+----------+-----+-------------------+----------+----+
| sub-stream 1 data | boundary | ... | sub-stream N data | boundary | -1 |  
+-------------------+----------+-----+-------------------+----------+----+
```
will be read as
```
+-------------------+----+-----+-------------------+----+
| sub-stream 1 data | -1 | ... | sub-stream N data | -1 | finished 
+-------------------+----+-----+-------------------+----+
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
    <version>1.1.0</version>
</dependency>
```

### Ream from a Boundary Stream

#### Create a boundary input stream:
```
BoundaryInputStream bis = null;
try {
    // create the bis from a file input stream
    FileInputStream fis = new FileInputStream("test.dat");
    bis = new BoundaryInputStream(fis);  // or new BoundaryInputStream(fis, boundary) with an explicit boundary
    // ...

} finally {
    // closes the base 'fis' automatically
    bis.close();
}
```

#### Iterate through multiple streams:
```
for (InputStream is : bis) {
    int b;
    while ((b = is.read()) != 1) {
        // ...
    }
}
```

#### Use the multiple stream iterator:
```
Iterator<InputStream> it = bis.iterator();
if (it.hasNext()) {
    InputStream is = it.next();
    
    int b;
    while ((b = is.read()) != 1) {
        // ...
    }
}
```

#### Alternatively you can work with the boundary input stream directly:
```
while (!bis.hasFinished()) {
    bis.next();
    
    int b;
    while ((b = bis.read()) != -1) {
        // ...
    }
}
```

### Write into a Boundary Stream

#### Create a boundary output stream:
```
BoundaryOutputStream bos = null;
try {
    // create the bos from a file output stream 
    FileOutputStream fos = new FileOutputStream("test.dat");
    bos = new BoundaryOutputStream(fos);  // or 'new BoundaryOutputStream(fos, boundary)' with an explicit boundary  
    // ...

} finally {
    // closes the base 'fos' automatically
    bos.close();
}
```

#### Write multiple streams into a boundary output stream:
```
byte[] subStream1 = ...
bos.write(subStream1);
bos.boundary(); // write the boundary after the first sub-stream

byte[] subStream2 = ...
bos.write(subStream2);
bos.boundary(); // write the boundary after the second sub-stream
```

Method `boundary()` is only convenient and identical to the following code:
```
byte[] boundary = ...
os.write(boundary);
```
So it's not necessary to create the stream via `BoundaryOutputStream` for reading it via `BoundaryInputStream`.

## Release Changes

### 1.1.0
- `BoundaryInputStream` implements `Iterable<InputStream>`.
- `IterableBoundaryInputStream` class removed as obsolete.
- Bugfix: `hasFinished() == true` after calling `close()`.

### 1.0.0
Initial version

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)