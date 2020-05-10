package com.github.vallez.leveldbjni.example;


import com.github.vallez.leveldbjni.LevelDb;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        String ldpath = System.getProperty("java.library.path", "");
        System.out.println(new File(ldpath).getAbsolutePath());
        System.loadLibrary("leveldbjni");
        try (LevelDb db = new LevelDb("testdb", new LevelDb.Options())) {
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

            try (LevelDb.Iterator it = db.iterator()) {
                for (it.seekToFirst(); it.hasNext(); it.next()) {
                    byte[] key = it.key();
                    byte[] value = it.value();
                    System.out.println(new String(key) + " -> " + new String(value));
                }
                System.out.println("backwards...");
                for (it.seekToLast(); it.hasNext(); it.prev()) {
                    byte[] key = it.key();
                    byte[] value = it.value();
                    System.out.println(new String(key) + " -> " + new String(value));
                }

                System.out.println("mid...");
                for (it.seek("b".getBytes()); it.hasNext(); it.next()) {
                    byte[] key = it.key();
                    byte[] value = it.value();
                    System.out.println(new String(key) + " -> " + new String(value));
                }
            }
        }
    }
}
