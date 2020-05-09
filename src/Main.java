import java.io.File;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        String ldpath = System.getProperty("java.library.path", "");
        System.out.println(ldpath + "   " + new File(ldpath).getAbsolutePath() +
                " " + new File(ldpath, "libex.dylib").exists());
        System.loadLibrary("ex");
        Main db = new Main();
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
                    if(db.delete("key".getBytes())){
                        System.out.println("DEL SUCCESS");
                    };
                }
            }
            db.close();
        }
    }

    private native boolean open(String fileName, boolean createIfMissing, boolean errorIfExists);

    private native boolean put(byte[] key, byte[] value);

    private native byte[] get(byte[] key);

    private native boolean delete(byte[] key);

    private native void close();
}
