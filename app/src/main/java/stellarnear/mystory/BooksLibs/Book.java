package stellarnear.mystory.BooksLibs;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Book {

    private byte[] imageByte=null;
    private boolean pageDataRecieved = false;

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
    private Instant lastRead;
    private long id;
    private String name;
    private Autor autor;
    private String cover_url;

    private int currentPercent=0;
    private int currentPage= 0;
    private int maxPage=-1;

    public Book(long id, String name, String cover_url, Autor autor) {
        this.id = id;
        this.name = name;
        this.autor = autor;
        this.cover_url = cover_url;
    }


    public void setMaxPage(int page) {
        this.maxPage=page;
    }


    public byte[] getImage() {
        return this.imageByte;
    }


    private OnImageRefreshedEventListener mListenerImageRefreshed;

    public void setOnImageRefreshedEventListener(OnImageRefreshedEventListener eventListener) {
        mListenerImageRefreshed = eventListener;
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
}
