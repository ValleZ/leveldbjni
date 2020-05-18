# LevelDb JNI

This library gives you a java interface to the [LevelDB](http://code.google.com/p/leveldb/) C++ library - extremely fast for writing, compact key-value database.

## How to use 

Start by adding compiled/leveldbjni-1.122.0.jar to your project.

Then add required imports:
```
import com.github.vallez.leveldbjni.LevelDb;
import java.io.File;
```

Open database, don't forget to close it:

```
LevelDb.Options options = new LevelDb.Options();
options.setCreateIfMissing(true);
options.setErrorIfExists(false);
try (LevelDb db = new LevelDb(new File("testdb"), options)) {
    ... use it here
}
```  

## Reads And Writes

```       
byte[] key = "a".getBytes();
byte[] value = "a value".getBytes();
db.put(key, value);
value = db.get(key);
db.delete(key);
```            

## Atomic Updates

``` 
if (db.get(key1) != null) {
    try (LevelDb.WriteBatch writeBatch = db.createWriteBatch()) {
        writeBatch.delete(key1);
        writeBatch.put(key2, value);
    } //you can either call writeBatch.write() or close it to commit
}
``` 

## Iteration
It doesn't provide Java's Iterator interface to keep it faster.

### Forward iteration
``` 
try (LevelDb.Iterator it = db.iterator()) {
    for (it.seekToFirst(); it.isValid(); it.next()) {
        byte[] key = it.key();
        byte[] value = it.value();
    }
}
``` 

### Backward iteration
``` 
try (LevelDb.Iterator it = db.iterator()) {
    for (it.seekToLast(); it.isValid(); it.prev()) {
        byte[] key = it.key();
        byte[] value = it.value();
    }
}
``` 

### Iteration for a key

``` 
try (LevelDb.Iterator it = db.iterator()) {
    for (it.seek(key); it.isValid(); it.prev()) {
        byte[] key = it.key();
        byte[] value = it.value();
    }
}
``` 

  