package com.github.vallez.leveldbjni.example;


import com.github.vallez.leveldbjni.LevelDb;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        try (LevelDb db = new LevelDb(new File("testdb"), new LevelDb.Options())) {
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
            System.out.println("write batch");
            try (LevelDb.WriteBatch writeBatch = db.createWriteBatch()) {
                writeBatch.delete("c".getBytes());
                writeBatch.put("b".getBytes(), "b value updated".getBytes());
            }
            try (LevelDb.Iterator it = db.iterator()) {
                for (it.seekToFirst(); it.hasNext(); it.next()) {
                    byte[] key = it.key();
                    byte[] value = it.value();
                    System.out.println(new String(key) + " -> " + new String(value));
                }
            }
        }
    }
}
