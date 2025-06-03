package stellarnear.mystory.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kal.rackmonthpicker.RackMonthPicker;
import com.kal.rackmonthpicker.listener.DateMonthDialogListener;
import com.kal.rackmonthpicker.listener.OnCancelMonthDialogListener;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import stellarnear.mystory.Activities.Fragments.MainActivityFragment;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentSearchBooks;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentWishList;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.BooksLibs.BookType;
import stellarnear.mystory.Constants;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;

public class ObservatoryActivity extends CustomActivity {


    private Window window;
    private Toolbar toolbar;

    private MainActivityFragment mainFrag;
    private MainActivityFragmentSearchBooks searchFrag;
    private MainActivityFragmentWishList wishListFrag;

    private FloatingActionButton fabSearchPanel;
    private FloatingActionButton fabWishList;
    private SharedPreferences settings;

    private final Tools tools = Tools.getTools();
    private ArrayList<String> labelList;
    private List<Book> currentDataBooksList;
    private ArrayList<Button> listSelectButtons;
    private LineChart chart;

    private ModeSelect modeSelect = ModeSelect.ALL;
    private boolean alternate = false;

    private BookType selectedBookType = BookType.ROMAN;
    private Integer selectedYear = null;
    private Integer selectedMonth = null;

    @Override
    protected void onCreateCustom() throws Exception {
        int themeId = getResources().getIdentifier("AppThemeBlue", "style", getPackageName());
        setTheme(themeId);

        setContentView(R.layout.activity_observatory);
        toolbar = findViewById(R.id.toolbar);
        window = getWindow();

        initObervatory();
    }

    private void initObervatory() {
        window.setStatusBarColor(getColor(R.color.primary_middle_blue));
        toolbar.setBackgroundColor(getColor(R.color.primary_dark_blue));
        toolbar.setTitleTextColor(getColor(R.color.primary_light_blue));
        toolbar.getOverflowIcon().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.primary_dark_blue), BlendModeCompat.SRC_ATOP));
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("L'observatoire");
                toolbar.setBackground(getDrawable(R.drawable.observatory_bar_back2));
            }
        });

        currentDataBooksList = new ArrayList<>(LibraryLoader.getShelf());

        listSelectButtons = new ArrayList<>();
        listSelectButtons.add(findViewById(R.id.observ_select_all));
        listSelectButtons.add(findViewById(R.id.observ_select_year));
        listSelectButtons.add(findViewById(R.id.observ_select_month));

        for (Button button : listSelectButtons) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    triggerSelect(button);
                }
            });
        }

        findViewById(R.id.observatory_radio_allbooks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDataBooksList = new ArrayList<>(LibraryLoader.getShelf());
                selectedBookType = BookType.ALL;
                filterBooks();
                addInfos();
                initBarChart();
            }
        });

        findViewById(R.id.observatory_radio_roman).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedBookType = BookType.ROMAN;
                filterBooks();
                addInfos();
                initBarChart();
            }
        });
        findViewById(R.id.observatory_radio_manga).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDataBooksList = new ArrayList<>();
                for (Book book : LibraryLoader.getShelf()) {
                    if (book.getBookType().equals(BookType.MANGA)) {
                        currentDataBooksList.add(book);
                    }
                }
                selectedBookType = BookType.MANGA;
                filterBooks();
                addInfos();
                initBarChart();
            }
        });

        initBarChart();
        addInfos();
    }


    private void addInfos() {
        LinearLayout infos = findViewById(R.id.obser_data_line_info);
        infos.removeAllViews();

        if (currentDataBooksList == null || currentDataBooksList.size() < 1) {
            findViewById(R.id.obs_list_infos).setVisibility(View.GONE);
            findViewById(R.id.obs_no_book).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.obs_list_infos).setVisibility(View.VISIBLE);
            findViewById(R.id.obs_no_book).setVisibility(View.GONE);
        }

        addInfo("Nombre de " + getBookTypeDisplay() + "s", String.valueOf(currentDataBooksList.size()));

        LocalDate minDate = null;
        LocalDate maxDate = null;

        Integer nBookPaged = 0;
        Integer minPage = null;
        Integer maxPage = null;
        Integer totalPages = 0;
        Integer nUnfinishedBooks = 0;

        //to account for multiple time read books
        Integer nTotalBooksRead = 0;
        Integer nTotalPagesRead = 0;

        Book mostReadBook=null;
        Map<Integer,Integer> numberOfReadHisto = new HashMap<>();

        for (Book book : currentDataBooksList) {

            if (book.getMaxPages() != null) {
                nBookPaged++;
                totalPages += book.getMaxPages();
                if (maxPage == null || maxPage < book.getMaxPages()) {
                    maxPage = book.getMaxPages();
                }
                if (minPage == null || minPage > book.getMaxPages()) {
                    minPage = book.getMaxPages();
                }
            }

            if (book.getLastEndTime() != null) {

                try {
                    LocalDate dt = LocalDate.parse(book.getLastEndTime(), Constants.DATE_FORMATTER);

                    if (minDate == null || dt.isBefore(minDate)) {
                        minDate = dt;
                    }

                    if (maxDate == null || dt.isAfter(maxDate)) {
                        maxDate = dt;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.err("Error while parsing dates for observatory", e);
                }
                nTotalBooksRead += book.getEndTimes().size();
                if(book.getMaxPages() != null){
                    nTotalPagesRead += book.getMaxPages()*book.getEndTimes().size();
                }

                if(mostReadBook == null || book.getEndTimes().size() > mostReadBook.getEndTimes().size()){
                    mostReadBook = book;
                }
                numberOfReadHisto.put(book.getEndTimes().size(),numberOfReadHisto.getOrDefault(book.getEndTimes().size(),0)+1);

            } else {
                nUnfinishedBooks++;
            }

        }

        if (nBookPaged != 0) {
            addInfo("Nombre de " + getBookTypeDisplay() + "s avec pages", String.valueOf(nBookPaged));
        }

        if (minPage != null) {
            addInfo("Nombre minimum de pages", String.valueOf(minPage));
        }
        if (maxPage != null) {
            addInfo("Nombre maximum de pages", String.valueOf(maxPage));
        }

        if (nBookPaged != 0) {
            addInfo("Nombre de pages en moyenne par " + getBookTypeDisplay(), String.valueOf(totalPages / nBookPaged));
        }

        try {
            long nMonth = minDate.until(maxDate, ChronoUnit.MONTHS) + 1;
            addInfo("Nombre de " + getBookTypeDisplay() + "s lu par mois en moyenne", String.format("%.2f",(1.0*nTotalBooksRead) / (1.0*nMonth)));
        } catch (Exception e) {
            //nah
        }


        if (nBookPaged != 0) {
            addInfo("Estimation nombre de pages lu", String.valueOf(nTotalPagesRead));
        }

        addInfo("Nombre de " + getBookTypeDisplay() + " pas fini", String.valueOf(nUnfinishedBooks));

        try {
            long nDays = minDate.until(maxDate, ChronoUnit.DAYS);
            addInfo("Date du "+getBookTypeDisplay() + " le plus ancien fini", Constants.DATE_FORMATTER.format(minDate));
            addInfo("Date du "+getBookTypeDisplay() + " le plus recemment fini", Constants.DATE_FORMATTER.format(maxDate));
            addInfo("Nombre de jour passé à lire", String.valueOf(nDays));
            addInfo("Nombre de pages par jour en moyenne", String.format("%.2f",(1.0*nTotalPagesRead) / (1.0*nDays)));
        } catch (Exception e) {
            //nah
        }


        for(Map.Entry<Integer,Integer> entry : numberOfReadHisto.entrySet()){
            addInfo("Nombre de " + getBookTypeDisplay() + " lu " + entry.getKey() + " fois", String.valueOf(entry.getValue()));
        }

        addInfo("Le " + getBookTypeDisplay() + " le plus lu ("+mostReadBook.getEndTimes().size()+" fois)", mostReadBook.getName().substring(0,30)+(mostReadBook.getName().length()>30?"...":""));

        // connexion chain
        try {
            addInfo("Nombre de connexions total", String.valueOf(LibraryLoader.getAccessStats().getnTotal()));
            addInfo("Plus grande chaine de connexion", String.valueOf(LibraryLoader.getAccessStats().getBestStreak()));
            addInfo("Première connexion", LibraryLoader.getAccessStats().getFirstLog());


            addInfo("Dernière connexion", LibraryLoader.getAccessStats().getLastLog());
            addInfo("Chaine de connexion actuelle", String.valueOf(LibraryLoader.getAccessStats().getnStreak()));

            float nConnexionDay = (float) LibraryLoader.getAccessStats().getnTotal() / LibraryLoader.getAccessStats().getNdaysBetweenFirstAndCurrent();

            addInfo("Nombre de connexion par jour", String.format("%.1f", nConnexionDay));
            addInfo("Nombre de connexion par semaine", String.format("%.1f", nConnexionDay * 7));
        } catch (Exception e) {
            //nah
        }

    }

    private String getBookTypeDisplay() {
        if (selectedBookType.equals(BookType.ROMAN)) {
            return "roman";
        } else if (selectedBookType.equals(BookType.MANGA)) {
            return "manga";
        } else {
            return "livre";
        }
    }

    private View addInfo(String s, String s2) {
        LinearLayout infos = findViewById(R.id.obser_data_line_info);

        LinearLayout line = new LinearLayout(getApplicationContext());
        line.setOrientation(LinearLayout.HORIZONTAL);
        if (alternate) {
            line.setBackground(getDrawable(R.drawable.background_obs_info_line));
        } else {
            line.setBackground(getDrawable(R.drawable.background_obs_info_line2));
        }
        alternate = !alternate;

        int margin = getResources().getDimensionPixelSize(R.dimen.general_margin);
        line.setPadding(margin, margin, margin, margin);


        TextView i1 = (TextView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.info_text_data, null);
        i1.setText(s);

        line.addView(i1);

        TextView i2 = getEditTextInfo(s2);
        i2.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        i2.setPadding(0, 0, getResources().getDimensionPixelSize(R.dimen.general_margin), 0);
        i2.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        i2.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        line.addView(i2);

        infos.addView(line);
        return line;
    }

    private TextView getEditTextInfo(String s) {
        TextView text = new TextView(getApplicationContext());
        text.setTextColor(getColor(R.color.primary_light_blue));
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.obs_info_text_size));
        text.setText(s);
        return text;
    }

    private void triggerSelect(Button buttonSelected) {
        for (Button button : listSelectButtons) {
            if (buttonSelected.equals(button)) {
                button.setBackground(getDrawable(R.drawable.button_ok_gradient));
                if (button.equals(findViewById(R.id.observ_select_all))) {
                    currentDataBooksList = new ArrayList<>(LibraryLoader.getShelf());
                    modeSelect = ModeSelect.ALL;
                    selectedMonth = null;
                    selectedYear = null;
                    filterBooks();
                    addInfos();
                    initBarChart();
                    ((Button) findViewById(R.id.observ_select_month)).setText("mois");
                    ((Button) findViewById(R.id.observ_select_year)).setText("année");
                } else if (button.equals(findViewById(R.id.observ_select_year))) {
                    ((Button) findViewById(R.id.observ_select_month)).setText("mois");
                    android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(ObservatoryActivity.this);
                    alert.setTitle("Saisie de l'année");
                    final EditText input = new EditText(getApplicationContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setRawInputType(Configuration.KEYBOARD_12KEY);
                    int currentYear = LocalDate.now().getYear();
                    input.setHint(String.valueOf(currentYear));
                    alert.setView(input);
                    alert.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                String value = input.getText().toString();
                                if (value.equalsIgnoreCase("")) {
                                    value = input.getHint().toString();
                                }
                                Integer year = Integer.parseInt(value);
                                ((Button) findViewById(R.id.observ_select_year)).setText(String.valueOf(year));
                                modeSelect = ModeSelect.YEAR;
                                selectedYear = year;
                                selectedMonth = null;
                                filterBooks();
                                addInfos();
                                initBarChart();
                            } catch (Exception e) {
                                e.printStackTrace();
                                log.err("Error setting a new year for observatory", e);
                            }

                        }
                    });
                    alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            modeSelect = ModeSelect.ALL;
                            triggerSelect(findViewById(R.id.observ_select_all));
                        }
                    });
                    alert.show();
                } else if (button.equals(findViewById(R.id.observ_select_month))) {
                    ((Button) findViewById(R.id.observ_select_year)).setText("année");
                    RackMonthPicker picker = new RackMonthPicker(this);
                    picker.setLocale(Locale.FRANCE)
                            .setPositiveButton(new DateMonthDialogListener() {
                                @Override
                                public void onDateMonth(int month, int startDate, int endDate, int year, String monthLabel) {
                                    currentDataBooksList = new ArrayList<>(LibraryLoader.getShelf());

                                    ((Button) findViewById(R.id.observ_select_month)).setText(month + "/" + year);
                                    modeSelect = ModeSelect.MONTH;
                                    selectedMonth = month;
                                    selectedYear = year;
                                    filterBooks();
                                    addInfos();
                                    initBarChart();
                                }
                            }).setNegativeButton(new OnCancelMonthDialogListener() {
                                @Override
                                public void onCancel(AlertDialog dialog) {
                                    modeSelect = ModeSelect.ALL;
                                    triggerSelect(findViewById(R.id.observ_select_all));
                                    dialog.dismiss();
                                }
                            });
                    picker.setColorTheme(R.color.primary_light_blue);
                    picker.show();
                }
            } else {
                button.setBackground(getDrawable(R.drawable.button_basic_gradient));
            }
        }
    }

    private void filterBooks() {
        List<Book> selectedBooks = new ArrayList<>();
        for (Book book : LibraryLoader.getShelf()) {
            if (selectedBookType.equals(BookType.ALL) || book.getBookType().equals(selectedBookType)) {
                selectedBooks.add(book);
            }
        }
        if (selectedYear != null) {
            List<Book> selectedBooksDate = new ArrayList<>();
            for (Book book : selectedBooks) {
                if (book.getLastEndTime() == null) {
                    continue;
                }
                String dateString = book.getLastEndTime();
                Integer monthBook = Integer.parseInt(dateString.substring(3, 5));
                Integer yearBook = Integer.parseInt(dateString.substring(6));
                if (yearBook.equals(selectedYear) && (selectedMonth == null || monthBook.equals(selectedMonth))) {
                    selectedBooksDate.add(book);
                }
            }
            currentDataBooksList = selectedBooksDate;
        } else {
            currentDataBooksList = selectedBooks;
        }

    }

    private void initBarChart() {
        chart = findViewById(R.id.line_chart);
        chart.invalidate();
        chart.fitScreen();
        if (currentDataBooksList.size() == 0) {
            chart.setVisibility(View.GONE);
            findViewById(R.id.obs_no_graph).setVisibility(View.VISIBLE);
        } else {
            chart.setVisibility(View.VISIBLE);
            findViewById(R.id.obs_no_graph).setVisibility(View.GONE);

            chart.getDescription().setEnabled(false);
            chart.setDrawGridBackground(false);
            chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            chart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            chart.getXAxis().setDrawGridLines(false);

            labelList = new ArrayList<>();
            LineData data = new LineData();
            data.addDataSet(computeLineDataSet("nombre de " + getBookTypeDisplay() + "s lu"));
            data.setValueTextColor(getColor(R.color.primary_dark_blue));

            chart.setData(data);

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setGridColor(getColor(R.color.primary_light_blue));
            leftAxis.setTextColor(getColor(R.color.primary_dark_blue));
            leftAxis.setGranularity(1.0f);
            leftAxis.setGranularityEnabled(true);
            chart.getAxisRight().setEnabled(false);
            XAxis xAxis = chart.getXAxis();
            xAxis.setGridColor(getColor(R.color.primary_light_blue));
            xAxis.setGranularity(1.0f);
            xAxis.setGranularityEnabled(true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            leftAxis.setGranularity(1.0f);
            leftAxis.setGranularityEnabled(true);

            chart.getAxisRight().setEnabled(false);
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labelList));
            xAxis.setTextColor(getColor(R.color.primary_dark_blue));
            xAxis.setLabelRotationAngle(-90);
            xAxis.setGranularity(1);
            xAxis.setGranularityEnabled(true);

            chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    tools.customToast(getApplicationContext(), e.getData().toString());
                }

                @Override
                public void onNothingSelected() {

                }
            });
            chart.animateXY(750, 1000);
        }
    }


    private LineDataSet computeLineDataSet(String mode) {
        TreeMap<String, Integer> monthCount = new TreeMap<>();

        for (Book book : currentDataBooksList) {
            if (book.getLastEndTime() == null) {
                continue;
            }
            String dateString = book.getLastEndTime();
            String monthString = dateString.substring(3, 5);
            String yearString = dateString.substring(8);
            //to remove the start of year
            String valueMap;
            if (modeSelect == ModeSelect.MONTH) {
                valueMap = yearString + "/" + monthString + "/" + dateString.substring(0, 2);
            } else {
                valueMap = yearString + "/" + monthString;
            }

            monthCount.putIfAbsent(valueMap, 0);
            monthCount.put(valueMap, monthCount.get(valueMap) + 1);
        }

        ArrayList<Entry> listVal = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : monthCount.entrySet()) {
            labelList.add(entry.getKey());
            String descr;
            if (modeSelect.equals(ModeSelect.MONTH)) {
                int day = Integer.parseInt(entry.getKey().substring(6));
                int month = Integer.parseInt(entry.getKey().substring(3, 5));
                descr = entry.getValue() + " " + getBookTypeDisplay() + "s lu le " + day + " " + DateFormatSymbols.getInstance().getMonths()[month - 1].toLowerCase() + " " + "20" + entry.getKey().substring(0, 2);
            } else {
                int month = Integer.parseInt(entry.getKey().substring(3));
                descr = entry.getValue() + " " + getBookTypeDisplay() + "s lu en " + DateFormatSymbols.getInstance().getMonths()[month - 1].toLowerCase() + " " + "20" + entry.getKey().substring(0, 2);
            }
            listVal.add(new Entry(index, entry.getValue(), descr));
            index++;
        }
        LineDataSet set = new LineDataSet(listVal, mode);
        set.setValueTextSize(15);
        set.setValueTextColor(getColor(R.color.primary_dark_blue));
        set.setCircleHoleColor(getColor(R.color.primary_light_blue));
        set.setColor(getColor(R.color.primary_dark_blue));
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setCircleColor(getColor(R.color.primary_dark_blue));
        set.setValueFormatter(new LargeValueFormatter());
        return set;
    }


    @Override
    protected void onResumeCustom() {
        checkOrientStart(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @Override
    protected void onBackPressedCustom() throws Exception {

    }

    @Override
    protected void onDestroyCustom() {

    }

    private void checkOrientStart(int screenOrientation) {
        if (getRequestedOrientation() != screenOrientation) {
            setRequestedOrientation(screenOrientation);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                }
            }, 1000);
        }
    }


    @Override
    public boolean onOptionsItemSelectedCustom(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("fromActivity", "observatoryActivity");
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onConfigurationChangedCustom() {
        setActivityFromOrientation();
    }


    private void setActivityFromOrientation() {
        final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                Intent intent_main = new Intent(ObservatoryActivity.this, MainActivity.class);
                startActivity(intent_main);
                finish();
                break;

            case Surface.ROTATION_90:
                Intent intent_shelf = new Intent(ObservatoryActivity.this, ShelfActivity.class);
                startActivity(intent_shelf);
                finish();
                break;

            case Surface.ROTATION_180:
                break;

            case Surface.ROTATION_270:
                //on y est
                break;
        }
    }

    private enum ModeSelect {
        ALL, YEAR, MONTH
    }

}