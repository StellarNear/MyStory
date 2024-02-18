package stellarnear.mystory.UITools;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.BooksLibs.Book;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {


    private Book book=null;
    private String previousDate = null;
    private DatePickerFragment.OnDateSetListener mListner=null;

    public interface OnDateSetListener {
        void onEvent(String result);
    }

    public void setOnDateSetListener(DatePickerFragment.OnDateSetListener eventListener) {
        mListner = eventListener;
    }

    public DatePickerFragment(Book book) {
        this.book = book;
    }

    public DatePickerFragment(Book book, String previousDate) {
        this.book = book;
        this.previousDate = previousDate;
    }

    public DatePickerFragment( String previousDate) {
        this.previousDate = previousDate;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker.
        try {
            if (previousDate != null) {
                String[] dateParsed = previousDate.split("/");
                int year = Integer.parseInt(dateParsed[2]);
                int month = Integer.parseInt(dateParsed[1]);
                int day = Integer.parseInt(dateParsed[0]);
                return new DatePickerDialog(requireContext(), this, year, month - 1, day);
            } else {
                return defaultPicker();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defaultPicker();
        }
    }

    private Dialog defaultPicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(requireContext(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        String saveDate="";
        if(day<10){
            saveDate+="0"+day;
        } else {
            saveDate+=day;
        }
        if(month<9){
            saveDate+="/0"+(month+1);
        } else {
            saveDate+="/"+(month+1);
        }
        saveDate+="/"+year;

        if(book!=null){
            book.saveNewEndInstant(saveDate);
            book.saveNewStartInstant(saveDate);
            MainActivity.saveBook(book);
        }

        if(mListner!=null){
            mListner.onEvent(saveDate);
        }

    }
}