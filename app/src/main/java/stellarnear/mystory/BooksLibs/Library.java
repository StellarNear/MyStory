package stellarnear.mystory.BooksLibs;

import java.util.ArrayList;
import java.util.List;

public class Library {
    private Book currentBook = null;


    public Book getCurrentBook() {
        return currentBook;
    }

    public void setCurrentBook(Book currentBook) {
        this.currentBook = currentBook;
    }

    private List<Book> wishList = new ArrayList<>();

    public List<Book> getWishList() {
        return wishList;
    }

    private List<Book> shelfList = new ArrayList<>();

    public List<Book> getShelfList() {
        return shelfList;
    }

    public void putCurrentToShelf() {
        this.shelfList.add(this.currentBook);
        this.currentBook = null;
    }

    public void deleteCurrent() {
        this.currentBook = null;
    }

    public void removeFromWishList(Book selectedBook) {
        this.wishList.remove(selectedBook);
    }

    public void addToWishList(Book selectedBook) {
        this.wishList.add(selectedBook);
    }

    public void removeFromShelf(Book selectedBook) {
        this.shelfList.remove(selectedBook);
    }

    public void addToShelf(Book selectedBook) {
        this.shelfList.add(selectedBook);
    }

    private List<Book> downloadList = new ArrayList<>();

    public List<Book> getDownloadList() {
        return this.downloadList;
    }

    public void removeBookFromDownloadList(Book selectedBook) {
        this.downloadList.remove(selectedBook);
    }

    public void addToDownloadList(Book selectedBook) {
        this.downloadList.add(selectedBook);
    }

    public void loadShelf(List<Book> library_shelf) {
        this.shelfList = library_shelf;
    }

    public void loadWish(List<Book> library_wish) {
        this.wishList = library_wish;
    }

    public void loadDownload(List<Book> library_download) {
        this.downloadList = library_download;
    }
}
