package com.github.vallez.leveldbjni;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LevelDbTest {

    private LevelDb db;
    private File dbDir;

    @BeforeEach
    void setUp() {
        dbDir = new File("testDb");
        db = new LevelDb(dbDir, null);
        assertNotNull(db);
    }

    @AfterEach
    void tearDown() {
        if (db != null) {
            db.close();
            assertTrue(dbDir.exists());
            String[] files = dbDir.list();
            if (files != null) {
                for (String fileName : files) {
                    assertTrue(new File(dbDir, fileName).delete());
                }
            }
            assertTrue(dbDir.delete());
        }
    }

    @Test
    void openWithBloomFilter() {
        db.close();
        LevelDb.Options options = new LevelDb.Options();
        options.bloomFilterBitsPerKey = 10;
        db = new LevelDb(dbDir, options);
    }

    @Test
    void openWithCache() {
        db.close();
        LevelDb.Options options = new LevelDb.Options();
        options.cacheSizeBytes = 10_000_000;
        db = new LevelDb(dbDir, options);
    }

    @Test
    void openWithNoCompression() {
        db.close();
        LevelDb.Options options = new LevelDb.Options();
        options.compression = false;
        db = new LevelDb(dbDir, options);
    }

    @Test
    void iterator_iterateForward() {
        db.put(b("a"), b("av"));
        db.put(b("b"), b("bv"));
        db.put(b("c"), b("cv"));
        StringBuilder sb = new StringBuilder();
        try (LevelDb.Iterator it = db.iterator()) {
            for (it.seekToFirst(); it.hasNext(); it.next()) {
                sb.append(new String(it.key(), StandardCharsets.UTF_8));
                sb.append('=');
                sb.append(new String(it.value(), StandardCharsets.UTF_8));
                sb.append(';');
            }
        }
        assertEquals("a=av;b=bv;c=cv;", sb.toString());
    }

    @Test
    void iterator_noDefault() {
        db.put(b("a"), b("av"));
        db.put(b("b"), b("bv"));
        db.put(b("c"), b("cv"));
        StringBuilder sb = new StringBuilder();
        try (LevelDb.Iterator it = db.iterator()) {
            for (; it.hasNext(); it.next()) {
                sb.append(new String(it.key(), StandardCharsets.UTF_8));
                sb.append('=');
                sb.append(new String(it.value(), StandardCharsets.UTF_8));
                sb.append(';');
            }
        }
        assertEquals("", sb.toString());
    }

    @Test
    void iterator_iterateBackward() {
        db.put(b("a"), b("av"));
        db.put(b("b"), b("bv"));
        db.put(b("c"), b("cv"));
        StringBuilder sb = new StringBuilder();
        try (LevelDb.Iterator it = db.iterator()) {
            for (it.seekToLast(); it.hasNext(); it.prev()) {
                sb.append(new String(it.key(), StandardCharsets.UTF_8));
                sb.append('=');
                sb.append(new String(it.value(), StandardCharsets.UTF_8));
                sb.append(';');
            }
        }
        assertEquals("c=cv;b=bv;a=av;", sb.toString());
    }

    @Test
    void iterator_iterateFromMid() {
        db.put(b("a"), b("av"));
        db.put(b("b"), b("bv"));
        db.put(b("c"), b("cv"));
        StringBuilder sb = new StringBuilder();
        try (LevelDb.Iterator it = db.iterator()) {
            for (it.seek(b("b")); it.hasNext(); it.next()) {
                sb.append(new String(it.key(), StandardCharsets.UTF_8));
                sb.append('=');
                sb.append(new String(it.value(), StandardCharsets.UTF_8));
                sb.append(';');
            }
        }
        assertEquals("b=bv;c=cv;", sb.toString());
    }

    @Test
    void createWriteBatch() {
        byte[] key1 = b("key1");
        byte[] key2 = b("key2");
        byte[] key3 = b("key3");
        db.put(key1, b("a"));
        db.put(key2, b("b"));
        try (LevelDb.WriteBatch batch = db.createWriteBatch()) {
            batch.put(key1, b("aaa"));
            batch.delete(key2);
            batch.put(key3, b("ccc"));
        }
        assertArrayEquals(b("aaa"), db.get(key1));
        assertNull(db.get(key2));
        assertArrayEquals(b("ccc"), db.get(key3));
    }

    @Test
    void createWriteBatch_distinctWrite() {
        byte[] key1 = b("key1");
        byte[] key2 = b("key2");
        byte[] key3 = b("key3");
        db.put(key1, b("a"));
        db.put(key2, b("b"));
        LevelDb.WriteBatch batch = db.createWriteBatch();
        batch.put(key1, b("aaa"));
        batch.delete(key2);
        batch.put(key3, b("ccc"));
        assertTrue(batch.write());
        assertFalse(batch.write());
        assertArrayEquals(b("aaa"), db.get(key1));
        assertNull(db.get(key2));
        assertArrayEquals(b("ccc"), db.get(key3));
    }

    @Test
    void delete() {
        byte[] key = b("key");
        db.put(key, b("value"));
        assertNotNull(db.get(key));
        assertTrue(db.delete(key));
        assertNull(db.get(key));
    }

    @Test
    void delete_nonExistent() {
        byte[] key = b("key");
        assertTrue(db.delete(key));
    }

    @Test
    void get_nonExistent() {
        assertNull(db.get(new byte[0]));
    }

    @Test
    void getAndPut_empty() {
        db.put(new byte[0], new byte[0]);
        assertArrayEquals(new byte[0], db.get(new byte[0]));
    }

    @Test
    void getAndPut_notEmptyValue() {
        db.put(new byte[0], new byte[1]);
        assertArrayEquals(new byte[1], db.get(new byte[0]));
    }

    @Test
    void getAndPut_largeValue() {
        byte[] key = b("efweio efuhdioef  epf oieifoj");
        byte[] value = new byte[10000000];
        Arrays.fill(value, (byte) 0xff);
        assertTrue(db.put(key, value));
        assertArrayEquals(value, db.get(key));
    }

    @Test
    void put_update() {
        assertTrue(db.put(b("a"), b("1")));
        assertTrue(db.put(b("a"), b("2")));
        assertArrayEquals(b("2"), db.get(b("a")));
    }

    @Test
    void close() {
        db.close();
        db.close();
    }

    private static byte[] b(String s) {
        return s == null ? null : s.getBytes(StandardCharsets.UTF_8);
    }
}