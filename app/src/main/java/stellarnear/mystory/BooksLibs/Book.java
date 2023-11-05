package stellarnear.mystory.BooksLibs;

import java.time.Instant;

public class Book {

   public Instant getStartTime() {
      return startTime;
   }

   public void setStartTime(Instant startTime) {
      this.startTime = startTime;
   }

   public Instant getEndTime() {
      return endTime;
   }

   public void setEndTime(Instant endTime) {
      this.endTime = endTime;
   }

   public Instant getLastRead() {
      return lastRead;
   }

   public void setLastRead(Instant lastRead) {
      this.lastRead = lastRead;
   }

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Autor getAutor() {
      return autor;
   }

   public void setAutor(Autor autor) {
      this.autor = autor;
   }

   public String getCover_url() {
      return cover_url;
   }

   public void setCover_url(String cover_url) {
      this.cover_url = cover_url;
   }

   private Instant startTime;
   private Instant endTime;
   private  Instant lastRead;
   private long id;
   private String name;
   private Autor autor;
   private String cover_url;

   public Book( long id, String name, String cover_url,Autor autor) {
      this.id = id;
      this.name = name;
      this.autor = autor;
      this.cover_url = cover_url;
   }
}
