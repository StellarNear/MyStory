package stellarnear.mystory.BooksLibs;

import java.time.Instant;

import stellarnear.mystory.Constants;

public class Note {
    private final Instant creation;
    private final String title;
    private final String note;

    public Note(String title, String note) {
        this.title = title;
        this.note = note;
        this.creation = Instant.now();
    }

    public Instant getCreation() {
        return creation;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public String getCreationDate() {
        return Constants.DATE_FORMATTER.format(creation);
    }
}
