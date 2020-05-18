/*
  Copyright (C) 2020 Valentin Konovalov <valle.ketsujin@gmail.com>
  <p>
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  <p>
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
  <p>
  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.vallez.leveldbjni;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Java JNI wrapper for LevelDB.
 * It loads native JNI wrapper library for appropriate OS and platform and exposes methods
 * to create, iterate over, add into, delete from a LevelDB database.
 * <p>
 * Database should be closed after use to flush data.
 * <p>
 * Example:
 * <p>
 * try (LevelDb db = new LevelDb(new File("testdb"), new LevelDb.Options())) {
 * if (db.put("key".getBytes(), "value".getBytes())) {
 * System.out.println("Read " + new String(db.get("key".getBytes())));
 * db.delete("key".getBytes());
 * }
 * }
 */
public class LevelDb implements Closeable {

    private static final String LIBNAME = "leveldbjni";

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
        String fileName = System.mapLibraryName(LIBNAME);
        String tmpDir = System.getProperty("java.io.tmpdir");
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
        } catch (Exception e) {
            try {
                System.loadLibrary(LIBNAME);
            } catch (Throwable ignored) {
                throw new RuntimeException(e);
            }
        }
    }

    private final long dbRef;
    private final AtomicBoolean dbClosed = new AtomicBoolean();
    private final ConcurrentHashMap<Iterator, Boolean> iterators = new ConcurrentHashMap<>();

    /**
     * Opens or creates LevelDB
     *
     * @param file    Directory for the DB
     * @param options db open options
     */
    public LevelDb(File file, Options options) {
        if (options == null) {
            options = new Options();
        }
        dbRef = open(file.getAbsolutePath(), options);
    }

    protected long open(String fileName, Options options) {
        long dbRef;
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
        return dbRef;
    }

    /**
     *
     * @return DB iterator
     */
    public Iterator iterator() {
        Iterator iterator = new Iterator();
        iterators.put(iterator, true);
        return iterator;
    }

    /**
     *
     * @return WriteBatch to perform batched writes (commits)
     */
    public WriteBatch createWriteBatch() {
        return new WriteBatch();
    }

    /**
     * Delete given key
     * @param key the key to delete
     * @return true if successful, false for error
     */
    public boolean delete(byte[] key) {
        return delete(dbRef, key);
    }

    /**
     * Get value for given key
     * @param key the key
     * @return value
     */
    public byte[] get(byte[] key) {
        return get(dbRef, key);
    }

    /**
     * Put key-value pair into the database
     * @param key key
     * @param value value
     * @return true if successful
     */
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

    private native long writeBatchNew(long dbRef);

    private native void writeBatchPut(long ref, byte[] key, byte[] value);

    private native void writeBatchDelete(long ref, byte[] key);

    private native boolean writeBatchWriteAndClose(long dbRef, long ref);

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

    /**
     * Flush and close database
     */
    @Override
    public void close() {
        if (dbClosed.compareAndSet(false, true)) {
            for (Iterator iterator : iterators.keySet()) {
                iterator.close();
            }
            close(dbRef);
        }
    }

    /**
     * Use WriteBatch to perform atomic writes
     */
    public class WriteBatch implements Closeable {
        private final long ref;
        private final AtomicBoolean writeBatchClosed = new AtomicBoolean(false);

        private WriteBatch() {
            this.ref = writeBatchNew(dbRef);
            if (ref == 0) {
                throw new RuntimeException("cannot create WriteBatch");
            }
        }

        /**
         * Put operation
         *
         * @param key   key
         * @param value value
         */
        public void put(byte[] key, byte[] value) {
            writeBatchPut(ref, key, value);
        }

        /**
         * Delete operation
         *
         * @param key key
         */
        public void delete(byte[] key) {
            writeBatchDelete(ref, key);
        }

        /**
         * Commits this patch
         *
         * @return true if successful
         */
        public boolean write() {
            if (writeBatchClosed.compareAndSet(false, true)) {
                return writeBatchWriteAndClose(dbRef, ref);
            }
            return false;
        }

        /**
         * Commits this batch
         */
        public void close() {
            write();
        }
    }

    /**
     * Iterator over database. Use close() or write() to commit changes.
     */
    public class Iterator implements Closeable {
        private final long ref;

        /**
         * Create an iterator. At first it is not pointed to anything.
         * EX:
         * try (LevelDb.Iterator it = db.iterator()) {
         * for (it.seekToFirst(); it.hasNext(); it.next()) {
         * System.out.println(new String(it.key(), StandardCharsets.UTF_8) +
         * "=" + new String(it.value(), StandardCharsets.UTF_8));
         * }
         * }
         */
        public Iterator() {
            this.ref = iteratorNew(dbRef);
            if (ref == 0) {
                throw new RuntimeException("cannot create Iterator");
            }
        }

        /**
         * Close and free the iterator.
         */
        public void close() {
            if (iterators.remove(this)) {
                iteratorClose(ref);
            }
        }

        /**
         * Seeks to first element
         */
        public void seekToFirst() {
            iteratorSeekToFirst(ref);
        }

        /**
         * Is current element valid
         *
         * @return true if you can retrieve current element, false if you are out of borders
         */
        public boolean isValid() {
            return iteratorValid(ref);
        }

        /**
         * Proceed forward
         */
        public void next() {
            iteratorNext(ref);
        }

        /**
         * @return key of the current element
         */
        public byte[] key() {
            return iteratorKey(ref);
        }

        /**
         * @return value of the current element
         */
        public byte[] value() {
            return iteratorValue(ref);
        }

        /**
         * Seeks to last element
         */
        public void seekToLast() {
            iteratorSeekToLast(ref);
        }

        /**
         * Proceed backward
         */
        public void prev() {
            iteratorPrev(ref);
        }

        /**
         * Seeks to the provided key
         */
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

    /**
     * Options for opening existing or creating new database
     */
    public static final class Options {
        boolean createIfMissing = true;
        boolean errorIfExists;
        boolean compression = true;
        boolean paranoidChecks;
        long cacheSizeBytes;
        byte bloomFilterBitsPerKey;

        public Options() {
        }

        /**
         * Create database on open if it is not created yet.
         * Default value is true.
         *
         * @param createIfMissing true to create database, false to throw an error when it is missed
         */
        public void setCreateIfMissing(boolean createIfMissing) {
            this.createIfMissing = createIfMissing;
        }

        /**
         * Throw error on open if database already exists.
         * Default value is false.
         *
         * @param errorIfExists true to throw error, false to proceed when database already exists
         */
        public void setErrorIfExists(boolean errorIfExists) {
            this.errorIfExists = errorIfExists;
        }

        /**
         * Use Snappy compression when necessary. Default is true.
         *
         * @param compression true to use Snappy compression when possible, false to disable it
         */
        public void setCompression(boolean compression) {
            this.compression = compression;
        }

        /**
         * Verify integrity on go. Disabled by default.
         *
         * @param paranoidChecks true to enable paranoid checks
         */
        public void setParanoidChecks(boolean paranoidChecks) {
            this.paranoidChecks = paranoidChecks;
        }

        /**
         * Add cache for DB blocks.
         *
         * @param cacheSizeBytes cache size in bytes
         */
        public void setCacheSizeBytes(long cacheSizeBytes) {
            this.cacheSizeBytes = cacheSizeBytes;
        }

        /**
         * Use bloom filtering.
         *
         * @param bloomFilterBitsPerKey bits per key
         */
        public void setBloomFilterBitsPerKey(byte bloomFilterBitsPerKey) {
            this.bloomFilterBitsPerKey = bloomFilterBitsPerKey;
        }
    }
}
