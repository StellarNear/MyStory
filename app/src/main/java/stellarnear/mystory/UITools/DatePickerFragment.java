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


    private final Book book;
    private String previousDate = null;

    public DatePickerFragment(Book book) {
        this.book = book;
    }

    public DatePickerFragment(Book book, String previousDate) {
        this.book = book;
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
        book.saveNewEndInstant(day + "/" + (month + 1) + "/" + year);
        book.saveNewStartInstant(day + "/" + (month + 1) + "/" + year);
        MainActivity.saveBook(book);
    }
}