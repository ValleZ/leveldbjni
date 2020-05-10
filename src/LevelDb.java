import java.io.Closeable;

public class LevelDb implements Closeable {
    private final long dbRef;

    public LevelDb(String fileName, boolean createIfMissing, boolean errorIfExists) {
        dbRef = open(fileName, createIfMissing, errorIfExists);
        if (dbRef == 0) {
            throw new RuntimeException();
        }
    }

    public Iterator iterator() {
        return new Iterator();
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

    private native long open(String fileName, boolean createIfMissing, boolean errorIfExists);

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
        close(dbRef);
    }

    class Iterator implements Closeable {
        final long ref;

        public Iterator() {
            this.ref = iteratorNew(dbRef);
            if (ref == 0) {
                throw new RuntimeException("cannot create iterator");
            }
        }

        public void close() {
            iteratorClose(ref);
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
    }
}
