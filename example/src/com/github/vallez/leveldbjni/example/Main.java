package com.github.vallez.leveldbjni.example;

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

import com.github.vallez.leveldbjni.LevelDb;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println(System.getProperty("os.arch"));
        try (LevelDb db = new LevelDb(new File("testdb"),
                new LevelDb.Options().setCreateIfMissing(true).setErrorIfExists(false))) {
            if (db.put("key".getBytes(), "value".getBytes())) {
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
                for (it.seekToFirst(); it.isValid(); it.next()) {
                    byte[] key = it.key();
                    byte[] value = it.value();
                    System.out.println(new String(key) + " -> " + new String(value));
                }
                System.out.println("backwards...");
                for (it.seekToLast(); it.isValid(); it.prev()) {
                    byte[] key = it.key();
                    byte[] value = it.value();
                    System.out.println(new String(key) + " -> " + new String(value));
                }

                System.out.println("mid...");
                for (it.seek("b".getBytes()); it.isValid(); it.next()) {
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
                for (it.seekToFirst(); it.isValid(); it.next()) {
                    byte[] key = it.key();
                    byte[] value = it.value();
                    System.out.println(new String(key) + " -> " + new String(value));
                }
            }
        }
    }
}
