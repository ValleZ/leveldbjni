package com.github.vallez.leveldbjni;

import java.io.*;
import java.util.HashSet;
import java.util.Objects;

public class LevelDb implements Closeable {
    static {
        String osName = System.getProperty("os.name", "");
        int spIndex = osName.indexOf(' ');
        if (spIndex >= 0) {
            osName = osName.substring(0, spIndex);
        }
        if (osName.equalsIgnoreCase("mac")) {
            osName = "Darwin";
        }
        String arch = System.getProperty("os.arch");
        if (arch.equals("amd64")) {
            arch = "x86_64";
        }
        String fileName = System.mapLibraryName("leveldbjni");
        String tmpDir = System.getProperty("java.io.tmpdir");
        System.out.println(osName + "/" + arch);
        String sourceFileName = "META-INF/native/" + osName + "/" + arch + "/" + fileName;
        File targetFile = new File(tmpDir, fileName);
        if (targetFile.exists() && !targetFile.delete()) {
            throw new RuntimeException("Cannot delete " + targetFile);
        }
        ClassLoader loader = LevelDb.class.getClassLoader();
        try (InputStream is = loader.getResourceAsStream(sourceFileName)) {
            if (is == null) {
                throw new RuntimeException("Cannot read " + sourceFileName);
            }
            byte[] buffer = new byte[1024 * 16];
            try (OutputStream os = new FileOutputStream(targetFile)) {
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                targetFile.deleteOnExit();
            }
            System.load(targetFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final long dbRef;
    private final HashSet<Iterator> iterators;

    public LevelDb(String fileName, Options options) {
        dbRef = open(fileName,
                options.createIfMissing,
                options.errorIfExists,
                options.compression,
                options.paranoidChecks,
                options.cacheSizeBytes,
                options.bloomFilterBitsPerKey);
        if (dbRef == 0) {
            throw new RuntimeException();
        }
        iterators = new HashSet<>();
    }

    public Iterator iterator() {
        Iterator iterator = new Iterator();
        iterators.add(iterator);
        return iterator;
    }

    public boolean delete(byte[] key) {
        return delete(dbRef, key);
    }

    public byte[] get(byte[] key) {
        return get(dbRef, key);
    }

    public boolean put(byte[] key, byte[] value) {
        return put(dbRef, key, value);
    }

    private native long open(String fileName,
                             boolean createIfMissing,
                             boolean errorIfExists,
                             boolean compression,
                             boolean paranoidChecks,
                             long cacheSizeBytes,
                             byte bloomFilterBitsPerKey);

    private native boolean put(long dbRef, byte[] key, byte[] value);

    private native byte[] get(long dbRef, byte[] key);

    private native boolean delete(long dbRef, byte[] key);

    private native long iteratorNew(long dbRef);

    private native void iteratorSeekToFirst(long ref);

    private native void iteratorSeekToLast(long ref);

    private native void iteratorSeek(long ref, byte[] key);

    private native boolean iteratorValid(long ref);

    private native void iteratorNext(long ref);

    private native void iteratorPrev(long ref);

    private native byte[] iteratorKey(long ref);

    private native byte[] iteratorValue(long ref);

    private native void iteratorClose(long ref);

    private native void close(long dbRef);

    @Override
    public void close() {
        for (Iterator iterator : iterators) {
            iterator.close();
        }
        close(dbRef);
    }

    public class Iterator implements Closeable {
        private final long ref;

        public Iterator() {
            this.ref = iteratorNew(dbRef);
            if (ref == 0) {
                throw new RuntimeException("cannot create iterator");
            }
        }

        public void close() {
            if (iterators.remove(this)) {
                iteratorClose(ref);
            }
        }

        public void seekToFirst() {
            iteratorSeekToFirst(ref);
        }

        public boolean hasNext() {
            return iteratorValid(ref);
        }

        public void next() {
            iteratorNext(ref);
        }

        public byte[] key() {
            return iteratorKey(ref);
        }

        public byte[] value() {
            return iteratorValue(ref);
        }

        public void seekToLast() {
            iteratorSeekToLast(ref);
        }

        public void prev() {
            iteratorPrev(ref);
        }

        public void seek(byte[] key) {
            iteratorSeek(ref, key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Iterator iterator = (Iterator) o;
            return ref == iterator.ref;
        }

        @Override
        public int hashCode() {
            return Objects.hash(ref);
        }
    }

    public static final class Options {
        boolean createIfMissing = true;
        boolean errorIfExists;
        boolean compression = true;
        boolean paranoidChecks;
        long cacheSizeBytes;
        byte bloomFilterBitsPerKey;

        public Options() {
        }

        public void setCreateIfMissing(boolean createIfMissing) {
            this.createIfMissing = createIfMissing;
        }

        public void setErrorIfExists(boolean errorIfExists) {
            this.errorIfExists = errorIfExists;
        }

        public void setCompression(boolean compression) {
            this.compression = compression;
        }

        public void setParanoidChecks(boolean paranoidChecks) {
            this.paranoidChecks = paranoidChecks;
        }

        public void setCacheSizeBytes(long cacheSizeBytes) {
            this.cacheSizeBytes = cacheSizeBytes;
        }

        public void setBloomFilterBitsPerKey(byte bloomFilterBitsPerKey) {
            this.bloomFilterBitsPerKey = bloomFilterBitsPerKey;
        }
    }
}
