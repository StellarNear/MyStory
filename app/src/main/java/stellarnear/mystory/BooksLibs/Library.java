package stellarnear.mystory.BooksLibs;

import java.util.ArrayList;
import java.util.List;

public class Library {
   private Book currentBook=null;

   public Book getCurrentBook() {
      return currentBook;
   }

   public void setCurrentBook(Book currentBook) {
      this.currentBook = currentBook;
   }


   private List<Book> wishList=new ArrayList<>();

   public List<Book> getWishList() {
      return wishList;
   }

   private List<Book> shelfList=new ArrayList<>();

   public List<Book> getShelfList() {
      return shelfList;
   }

}
