package stellarnear.mystory.UITools;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;

import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.BooksLibs.Book;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {


    private final Book book;
    private final String mode;
    private final String previousDate;

    public DatePickerFragment(Book book,String previousDate,String mode) {
        this.book=book;
        this.previousDate=previousDate;
        this.mode=mode;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker.
        try{
            String[] dateParsed = previousDate.split("/");
            int year =Integer.parseInt(dateParsed[2]);
            int month = Integer.parseInt(dateParsed[1]);
            int day = Integer.parseInt(dateParsed[0]);
            return new DatePickerDialog(requireContext(), this, year, month-1, day);
        } catch (Exception e){
            e.printStackTrace();
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(requireContext(), this, year, month, day);
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        if(this.mode.equalsIgnoreCase("end")){
            book.saveNewEndInstant(day+"/"+(month+1)+"/"+year);
        }
        if(this.mode.equalsIgnoreCase("start")){
            book.saveNewStartInstant(day+"/"+(month+1)+"/"+year);
        }

        MainActivity.saveLibrary();
    }
}