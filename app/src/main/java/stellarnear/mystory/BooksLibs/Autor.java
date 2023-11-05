package stellarnear.mystory.BooksLibs;

public class Autor {
      private long id;
      private String fullName;

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getFullName() {
      return fullName;
   }

   public void setFullName(String fullName) {
      this.fullName = fullName;
   }

   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   private String lastName;
      private String firstName;

   public Autor(long id ,String firstName, String lastName, String fullName) {
      this.id = id;
      this.firstName = firstName;
      this.lastName = lastName;
      this.fullName = fullName;
   }
}
