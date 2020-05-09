import java.io.File;

public class Main {

    public static void main(String[] args) {
        String ldpath = System.getProperty("java.library.path", "");
        System.out.println(ldpath + "   " + new File(ldpath).getAbsolutePath() +
                " " + new File(ldpath, "libex.dylib").exists());
        System.loadLibrary("ex");
        Main main = new Main();
        boolean result = main.open("testdb", true, false);
        System.out.println(result);
        if (result) {
            if (main.put("key".getBytes(), "kgyfekuewncr".getBytes())) {
                System.out.println("PUT SUCCESS");
                byte[] value = main.get("key".getBytes());
                if (value == null) {
                    System.out.println("Get failed");
                } else {
                    System.out.println("READ " + new String(value));
                }
            }
            main.close();
        }
    }

    private native boolean open(String fileName, boolean createIfMissing, boolean errorIfExists);

    private native boolean put(byte[] key, byte[] value);

    private native byte[] get(byte[] key);

    private native void close();
}
