package de.opti4apps.timelytest.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Random;

import io.objectbox.Box;
import io.objectbox.BoxStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UserTest {
    protected File boxStoreDir;
    protected BoxStore store;
    protected Box<User> box;
    protected Random random = new Random();
    protected boolean runExtensiveTests;

    @Before
    public void setUp() throws Exception {
        // This works with Android without needing any context
        File tempFile = File.createTempFile("object-store-test", "");
        tempFile.delete();
        boxStoreDir = tempFile;
        store = createBoxStore();
        box = store.boxFor(User.class);
        runExtensiveTests = System.getProperty("extensive") != null;
    }

    protected BoxStore createBoxStore() {
        return MyObjectBox.builder().directory(boxStoreDir).build();
    }

    @After
    public void tearDown() throws Exception {
        if (store != null) {
            try {
                store.close();
                store.deleteAllFiles();

                File[] files = boxStoreDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        logError("File was not deleted: " + file.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                logError("Could not clean up test", e);
            }
        }
        if (boxStoreDir != null && boxStoreDir.exists()) {
            File[] files = boxStoreDir.listFiles();
            for (File file : files) {
                delete(file);
            }
            delete(boxStoreDir);
        }
    }

    @Test
    public void testBasics() {
        User object = new User();
        long id = box.put(object);
        assertTrue(id > 0);
        assertEquals(id, object.getId());
        User objectRead = box.get(id);
        assertNotNull(objectRead);
        assertEquals(id, objectRead.getId());
        assertEquals(1, box.count());

        box.remove(id);
        assertEquals(0, box.count());
        assertNull(box.get(id));
    }

    private boolean delete(File file) {
        boolean deleted = file.delete();
        if (!deleted) {
            file.deleteOnExit();
            logError("Could not delete " + file.getAbsolutePath());
        }
        return deleted;
    }

    protected void log(String text) {
        System.out.println(text);
    }

    protected void logError(String text) {
        System.err.println(text);
    }

    protected void logError(String text, Exception ex) {
        if (text != null) {
            System.err.println(text);
        }
        ex.printStackTrace();
    }

    protected long time() {
        return System.currentTimeMillis();
    }

}