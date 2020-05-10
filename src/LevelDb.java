import java.io.Closeable;
import java.io.File;

public class LevelDb {

    public static void main(String[] args) {
        String ldpath = System.getProperty("java.library.path", "");
        System.out.println(ldpath + "   " + new File(ldpath).getAbsolutePath() +
                " " + new File(ldpath, "libex.dylib").exists());
        System.loadLibrary("leveldbjni");
        LevelDb db = new LevelDb();
        boolean result = db.open("testdb", true, false);
        System.out.println(result);
        if (result) {
            if (db.put("key".getBytes(), "kgyfekuewncr".getBytes())) {
                System.out.println("PUT SUCCESS");
                byte[] value = db.get("key".getBytes());
                if (value == null) {
                    System.out.println("Get failed");
                } else {
                    System.out.println("READ " + new String(value));
                    if (db.delete("key".getBytes())) {
                        System.out.println("DEL SUCCESS");
                    }
                }
            }
            db.put("a".getBytes(), "a value".getBytes());
            db.put("b".getBytes(), "b value".getBytes());
            db.put("c".getBytes(), "c value".getBytes());

            Iterator it = db.new Iterator();
            for (it.seekToFirst(); it.valid(); it.next()) {
                byte[] key = it.key();
                byte[] value = it.value();
                System.out.println(new String(key) + " -> " + new String(value));
            }
            System.out.println("backwards...");
            for (it.seekToLast(); it.valid(); it.prev()) {
                byte[] key = it.key();
                byte[] value = it.value();
                System.out.println(new String(key) + " -> " + new String(value));
            }

            System.out.println("mid...");
            for (it.seek("b".getBytes()); it.valid(); it.next()) {
                byte[] key = it.key();
                byte[] value = it.value();
                System.out.println(new String(key) + " -> " + new String(value));
            }
            it.close();

            db.close();
        }
    }

    private native boolean open(String fileName, boolean createIfMissing, boolean errorIfExists);

    private native boolean put(byte[] key, byte[] value);

    private native byte[] get(byte[] key);

    private native boolean delete(byte[] key);

    private native long iteratorNew();

    private native void iteratorSeekToFirst(long ref);

    private native void iteratorSeekToLast(long ref);

    private native void iteratorSeek(long ref, byte[] key);

    private native boolean iteratorValid(long ref);

    private native void iteratorNext(long ref);

    private native void iteratorPrev(long ref);

    private native byte[] iteratorKey(long ref);

    private native byte[] iteratorValue(long ref);

    private native void iteratorClose(long ref);

    private native void close();

    class Iterator implements Closeable {
        final long ref;

        public Iterator() {
            this.ref = iteratorNew();
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

        public boolean valid() {
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
