package stellarnear.mystory.BooksLibs;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.Activities.LibraryLoader;
import stellarnear.mystory.Constants;

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

    private AccessStats accessStats = new AccessStats();

    public AccessStats getAccessStats() {
        return this.accessStats;
    }

    public void setAccessStats(AccessStats libraryAccessStats) {
        this.accessStats = libraryAccessStats;
    }

    public class AccessStats {
        private int nTotal = 0;
        private int nStreak = 0;
        private int bestStreak = 0;
        private int lastClaimedStreakReward=0;

        private String firstLog = "";
        private String lastLog = "";
        private String currentLog = "";


        public void storeLogin() {
            if (firstLog == null) {
                firstLog = "";
            }
            if (lastLog == null) {
                firstLog = "";
            }
            if (currentLog == null) {
                firstLog = "";
            }

            nTotal++;
            // Storing the previous currentLog into lastLog
            lastLog = currentLog;

            // Creating a formatter for your Constants.TIME_PATTERN_FORMAT
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_FORMAT)
                    .withZone(ZoneId.systemDefault());

            // Getting the current log time
            currentLog = formatter.format(Instant.now());


            if (firstLog.equalsIgnoreCase("")) {
                firstLog = formatter.format(Instant.now());
            }

            if (!lastLog.equalsIgnoreCase("")) {
                // Parse the lastLog and currentLog to LocalDate
                LocalDate lastLogDate = LocalDate.parse(lastLog, formatter);
                LocalDate currentLogDate = LocalDate.parse(currentLog, formatter);

                // If lastLog is empty, it means this is the first log, so we start the streak
                if (lastLog.isEmpty()) {
                    nStreak = 0;
                } else {
                    // Calculate the difference in days between the last log and the current log
                    long daysBetween = ChronoUnit.DAYS.between(lastLogDate, currentLogDate);

                    // If the difference is exactly 1, it means the logs are consecutive days
                    if (daysBetween == 1) {
                        nStreak++;
                    } else if (daysBetween > 1) {
                        // If the difference is more than 1, reset the streak
                        nStreak = 0;
                        lastClaimedStreakReward=0;
                    }
                    // If the days between is 0, it means the user logged in more than once on the same day,
                    // which should not affect the streak
                }

                if (bestStreak < nStreak) {
                    bestStreak = nStreak;
                }
            }

        }

        public int getnStreak() {
            return nStreak;
        }

        public String getFirstLog() {
            return firstLog;
        }

        public String getLastLog() {
            return lastLog;
        }

        public int getnTotal() {
            return nTotal;
        }

        public int getBestStreak() {
            return bestStreak;
        }

        public long getNdaysBetweenFirstAndCurrent() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_FORMAT)
                    .withZone(ZoneId.systemDefault());
            LocalDate firstLogDate = LocalDate.parse(firstLog, formatter);
            LocalDate currentLogDate = LocalDate.parse(currentLog, formatter);
            long daysBetween = ChronoUnit.DAYS.between(firstLogDate, currentLogDate);
            return daysBetween;
        }


        //TODO REMOVE APRES FAKE BACK
        public void forceFirstCo(String startFake, float nConnexionDay) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_FORMAT)
                    .withZone(ZoneId.systemDefault());
            LocalDate firstLogDate = LocalDate.parse(firstLog, formatter);
            LocalDate fakeLogDate = LocalDate.parse(startFake, formatter);
            long daysToCatchBack = ChronoUnit.DAYS.between(fakeLogDate, firstLogDate);

            nTotal += nConnexionDay * daysToCatchBack;

            firstLog = startFake;
            LibraryLoader.saveAccessStats();
        }

        public void setLastClaimedStreakReward(int streak) {
            this.lastClaimedStreakReward=streak;
        }
        public int getLastClaimedStreakReward() {
            return this.lastClaimedStreakReward;
        }
    }
}
