package stellarnear.mystory.BooksLibs;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        private int lastGrantedStreakReward = 0;

        private String firstLog = "";
        private String lastLog = "";
        private String currentLog = "";

        private List<String> giftsUnclaimed = new ArrayList<>();
        private boolean displayBreakStreakAnim = true;

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

            // Getting the current log time
            currentLog = Constants.DATE_FORMATTER.format(Instant.now());


            if (firstLog.equalsIgnoreCase("")) {
                firstLog = Constants.DATE_FORMATTER.format(Instant.now());
            }

            if (!lastLog.equalsIgnoreCase("")) {
                // Parse the lastLog and currentLog to LocalDate
                LocalDate lastLogDate = LocalDate.parse(lastLog, Constants.DATE_FORMATTER);
                LocalDate currentLogDate = LocalDate.parse(currentLog, Constants.DATE_FORMATTER);

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
                        lastGrantedStreakReward = 0;
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
            LocalDate firstLogDate = LocalDate.parse(firstLog, Constants.DATE_FORMATTER);
            LocalDate currentLogDate = LocalDate.parse(currentLog, Constants.DATE_FORMATTER);
            long daysBetween = ChronoUnit.DAYS.between(firstLogDate, currentLogDate);
            return daysBetween;
        }

        public void setLastGrantedStreakReward(int streak) {
            this.lastGrantedStreakReward = streak;
        }

        public int getLastGrantedStreakReward() {
            return this.lastGrantedStreakReward;
        }

        public void addGiftToclaim(String gift) {
            if (giftsUnclaimed == null) {
                this.giftsUnclaimed = new ArrayList<>();
            }
            this.giftsUnclaimed.add(gift);
        }

        public List<String> getGiftsUnclaimed() {
            if (giftsUnclaimed == null) {
                this.giftsUnclaimed = new ArrayList<>();
            }
            return giftsUnclaimed;
        }

        public void claimGift(String gift) {
            // Get the iterator for the list
            if (giftsUnclaimed == null) {
                this.giftsUnclaimed = new ArrayList<>();
                return;
            }
            Iterator<String> iterator = giftsUnclaimed.iterator();
            // Iterate through the list
            while (iterator.hasNext()) {
                // Check if the current element matches the target string
                if (iterator.next().equalsIgnoreCase(gift)) {
                    // Remove the element using iterator's remove method
                    iterator.remove();
                    // Exit the method
                    return;
                }
            }
        }

        public boolean shouldDisplayBreakStreakAnim() {
            return displayBreakStreakAnim;
        }

        public void setDisplayBreakStreakAnim(boolean displayBreakStreakAnim) {
            this.displayBreakStreakAnim = displayBreakStreakAnim;
        }
    }
}
