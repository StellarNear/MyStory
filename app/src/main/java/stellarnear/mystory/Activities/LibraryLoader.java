package stellarnear.mystory.Activities;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.BooksLibs.Library;
import stellarnear.mystory.Log.CustomLog;
import stellarnear.mystory.TinyDB;


public class LibraryLoader {

    private LibraryLoader() {

    }

    private static Library library = null;
    private static TinyDB tinyDB = null;
    private static SharedPreferences prefs;
    private static File internalStorageDir = null;


    private static final CustomLog log = new CustomLog(LibraryLoader.class);

    public static void loadLibrary(Context mC) {
        if (tinyDB == null || library == null) {
            tinyDB = new TinyDB(mC);
            prefs = PreferenceManager.getDefaultSharedPreferences(mC);
            internalStorageDir = mC.getFilesDir();
            try {
                loadCurrentFromSave();
                loadAccessStats();
            } catch (Exception e) {
                log.err("Could not load the library", e);
                library = new Library();
                saveLibrary();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        loadAllListFromSave();
                    } catch (Exception e) {
                        log.err("Could not load the library lists", e);
                    }
                }
            }).start();
        }
    }

    private static void loadAccessStats() {
        if (library == null) {
            library = new Library();
        }
        try {

            Library.AccessStats accessStats = tinyDB.getAccessStats("library_access_stats");
            if (accessStats != null) {
                library.setAccessStats(accessStats);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.err("Error while loading access stats", e);
        }
    }

    public static void loadCurrentFromSave() {
        if (library == null) {
            library = new Library();
        }
        if (tinyDB.getString("library_current").equalsIgnoreCase("")) {
            library.setCurrentBook(null);
        } else {
            library.setCurrentBook(tinyDB.getBook(tinyDB.getString("library_current")));
        }

    }


    public static File getInternalStorageDir() {
        return internalStorageDir;
    }

    public static void loadAllListFromSave() {
        if (library == null) {
            library = new Library();
        }
        library.loadShelf(toListBook(tinyDB.getListString("library_shelf")));
        library.loadWish(toListBook(tinyDB.getListString("library_wish")));
        library.loadDownload(toListBook(tinyDB.getListString("library_download")));
    }


    private static List<Book> toListBook(ArrayList<String> listUuid) {
        List<Book> books = new ArrayList<>();
        for (String uuid : listUuid) {
            books.add(tinyDB.getBook(uuid));
        }
        return books;
    }

    private static void saveLibrary() {
        tinyDB.putListString("library_shelf", toUUID(library.getShelfList()));
        tinyDB.putListString("library_wish", toUUID(library.getWishList()));
        tinyDB.putListString("library_download", toUUID(library.getDownloadList()));
        saveCurrent();
    }

    public static void saveCurrent() {
        if (library.getCurrentBook() != null) {
            tinyDB.putString("library_current", library.getCurrentBook().getUuid().toString());
        } else {
            tinyDB.putString("library_current", "");
        }
    }

    private static ArrayList<String> toUUID(List<Book> list) {
        ArrayList<String> result = new ArrayList<>();
        for (Book book : list) {
            result.add(book.getUuid().toString());
        }
        return result;
    }

    public static void saveShelf() {
        tinyDB.putListString("library_shelf", toUUID(library.getShelfList()));
    }

    public static void saveWish() {
        tinyDB.putListString("library_wish", toUUID(library.getWishList()));
    }

    public static void saveDownload() {
        tinyDB.putListString("library_download", toUUID(library.getDownloadList()));
    }


    public static void saveBook(Book book) {
        tinyDB.saveBook(book);
    }

    public static void saveAccessStats() {
        tinyDB.saveAccessStats("library_access_stats", library.getAccessStats());
    }


    public static void deleteBook(Book selectedBook) {
        if (selectedBook != null) {
            prefs.edit().remove(selectedBook.getUuid().toString()).apply();
        }
    }


    public static Book getCurrentBook() {
        return library.getCurrentBook();
    }

    public static List<Book> getWishList() {
        return library.getWishList();
    }


    public static void addStartTime(Book selectedBook) {
        if (selectedBook != null) {
            selectedBook.addStartTime();
            saveBook(selectedBook);
        }
    }

    public static void addEndTime(Book selectedBook) {
        if (selectedBook != null) {
            selectedBook.addEndTime();
            saveBook(selectedBook);
        }
    }

    public static void setCurrentBook(Book selectedBook) {
        if (selectedBook != null) {
            library.setCurrentBook(selectedBook);
            saveCurrent();
        }
    }


    public static void putCurrentToShelf() {
        if (getCurrentBook() != null) {
            library.putCurrentToShelf();
            saveShelf();
            saveCurrent();
        }
    }

    public static void endBookAndPutToShelf() {
        Book book = library.getCurrentBook();
        if (book != null) {
            addEndTime(book);
            putCurrentToShelf();
        }

    }

    public static void deleteCurrent() {
        if (library.getCurrentBook() != null) {
            deleteBook(library.getCurrentBook());
        }
        library.deleteCurrent();
        saveCurrent();
    }

    public static void removeBookFromWishList(Book selectedBook) {
        if (selectedBook != null) {
            library.removeFromWishList(selectedBook);
            saveWish();
        }
    }

    public static void addBookToShelf(Book selectedBook) {
        if (selectedBook != null) {
            library.addToShelf(selectedBook);
            saveShelf();
        }
    }

    public static void removeBookFromShelf(Book selectedBook) {
        if (selectedBook != null) {
            library.removeFromShelf(selectedBook);
            saveShelf();
        }
    }


    public static void addToWishList(Book selectedBook) {
        if (selectedBook != null) {
            library.addToWishList(selectedBook);
            saveWish();
        }
    }

    public static List<Book> getShelf() {
        return library.getShelfList();
    }


    public static List<Book> getDownloadList() {
        return library.getDownloadList();
    }

    public static void removeBookFromDownloadList(Book selectedBook) {
        if (selectedBook != null) {
            library.removeBookFromDownloadList(selectedBook);
            saveDownload();
        }
    }

    public static void addBookToDownload(Book selectedBook) {
        if (selectedBook != null) {
            library.addToDownloadList(selectedBook);
            saveDownload();
        }
    }


    public static int checkStreak() {
        library.getAccessStats().storeLogin();
        int nStreak = library.getAccessStats().getnStreak();
        if (nStreak >= 1) {
            library.getAccessStats().setDisplayBreakStreakAnim(true);
        }
        saveAccessStats();
        return nStreak;
    }

    public static Library.AccessStats getAccessStats() {
        return library.getAccessStats();
    }

    public static Library getLibrary() {
        return library;
    }

    public static void resetLibrary() {
        library = new Library();
    }

    public static boolean shouldDisplayBreakStreakAnim() {
        return library.getAccessStats().shouldDisplayBreakStreakAnim();
    }

    public static void setDisplayBreakStreakAnim(boolean b) {
        library.getAccessStats().setDisplayBreakStreakAnim(b);
        saveAccessStats();
    }
}
