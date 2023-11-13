package stellarnear.mystory.BooksLibs;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import stellarnear.mystory.Constants;

public class Book {

    private byte[] imageByte=null;
    private boolean pageDataRecieved = false;


    private List<String> startTimes=new ArrayList<>();

    public List<String> getStartTimes() {
        return startTimes;
    }

    public void addStartTime() {
        setLastRead();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_FORMAT)
                .withZone(ZoneId.systemDefault());
        this.startTimes.add(formatter.format(Instant.now()));
    }

    private List<String> endTimes=new ArrayList<>();

    public List<String> getEndTimes() {
        return endTimes;
    }

    public void addEndTime() {
        setLastRead();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_FORMAT)
                .withZone(ZoneId.systemDefault());
        this.endTimes.add(formatter.format(Instant.now()));
    }
    private String lastRead;
    public String getLastRead() {
        return lastRead;
    }

    public void setLastRead() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_FORMAT)
                .withZone(ZoneId.systemDefault());
        this.lastRead = formatter.format(Instant.now());
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


    private long id;
    private String name;
    private Autor autor;
    private String cover_url;



    public Book(long id, String name, String cover_url, Autor autor) {
        this.id = id;
        this.name = name;
        this.autor = autor;
        this.cover_url = cover_url;
    }

    private Integer currentPercent=0;
    private Integer currentPage= 0;
    private Integer maxPages=null;

    public void setMaxPages(int page) {
        this.maxPages=page;
    }

    public Integer getMaxPages() {
        return maxPages;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPercent(int currentPercent) {
        this.currentPercent = currentPercent;
        if(this.maxPages!=null){
            this.currentPage= (int) ((1.0*currentPercent*maxPages)/100.0);
        }
    }

    public Integer getCurrentPercent() {
        return currentPercent;
    }

    public byte[] getImage() {
        return this.imageByte;
    }


    private OnImageRefreshedEventListener mListenerImageRefreshed;

    public void setOnImageRefreshedEventListener(OnImageRefreshedEventListener eventListener) {
        mListenerImageRefreshed = eventListener;
    }

    private List<Note> notes=new ArrayList<>();

    public List<Note> getNotes() {
        return this.notes;
    }

    public void saveNewStartInstant(String toInstant) {
        this.startTimes=new ArrayList<>();
        this.startTimes.add(toInstant);
    }

    public void saveNewEndInstant(String toInstant) {
        this.endTimes=new ArrayList<>();
        this.endTimes.add(toInstant);
    }

    public void setCurrentPage(int page) {
        this.currentPage=page;
        if(this.maxPages!=null){
            this.currentPercent= (int) ((1.0*currentPage/maxPages)*100.0);
        }
    }


    public interface OnImageRefreshedEventListener {
        void onEvent();
    }


    public void setImageByte(byte[] byteChunk) {
        if(byteChunk!=null && byteChunk.length>1){
            this.imageByte = byteChunk;
            if(mListenerImageRefreshed!=null){
                mListenerImageRefreshed.onEvent();
            }
        }

    }

    //later call pages data
    private SortedSet<Integer> maxPagesFound = new TreeSet<>();

    private OnPageDataRecievedEventListener mListenerPageData;

    public void setOnPageDataRecievedEventListener(OnPageDataRecievedEventListener eventListener) {
        mListenerPageData = eventListener;
    }

    public interface OnPageDataRecievedEventListener {
        void onEvent();
    }


    public Set<Integer> getPagesFounds() {
        return this.maxPagesFound;
    }

    public void addMultipleMaxPagesFound(SortedSet<Integer> maxPagesFounds) {
        this.maxPagesFound = maxPagesFounds;
        if (!pageDataRecieved && mListenerPageData != null) {
            mListenerPageData.onEvent();
        }
        pageDataRecieved =true;
    }

    public String getMeanPagesFoundTxt() {
        if (this.maxPagesFound.size() > 0) {
            int min = Collections.min(this.maxPagesFound);
            int max = Collections.max(this.maxPagesFound);
            if (min == max) {
                return String.valueOf(min) +" pages";
            } else {
                return min + " - " + max+" pages";
            }
        } else {
            return "";
        }
    }

    public void deleteNote(Note note) {
        this.notes.remove(note);
    }

    public void createNote(String title, String note) {
        this.notes.add(new Note(title,note));
    }



    public String getLastStartTime() {
        if(this.startTimes.size()>0){
            return this.startTimes.get(this.startTimes.size()-1);
        }
        return null;
    }

    public String getLastEndTime() {
        if(this.endTimes.size()>0){
            return this.endTimes.get(this.endTimes.size()-1);
        }
        return null;
    }



}
