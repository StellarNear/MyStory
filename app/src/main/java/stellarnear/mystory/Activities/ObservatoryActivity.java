package stellarnear.mystory.Activities;

import android.content.Context;
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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

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

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.BooksLibs.BookType;
import stellarnear.mystory.BooksLibs.Library;
import stellarnear.mystory.Constants;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;

public class ObservatoryActivity extends CustomActivity {

    private Window window;
    private Toolbar toolbar;

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
    private boolean pagePlot = false;

    // --- NOUVEAU ---
    private boolean readingTimeMode = false;
    private boolean sessionCountPlot = false; // false = Temps, true = Sessions
    private ModeSelectWeek modeSelectWeek = null; // null = pas en mode semaine
    private Button weekButton;
    private ToggleButton toggleReadingTime;

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
        toolbar.getOverflowIcon().setColorFilter(
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getColor(R.color.primary_dark_blue), BlendModeCompat.SRC_ATOP));
        toolbar.post(() -> {
            toolbar.setTitle("L'observatoire");
            toolbar.setBackground(getDrawable(R.drawable.observatory_bar_back2));
        });

        currentDataBooksList = new ArrayList<>(LibraryLoader.getShelf());

        // --- Boutons de sélection temporelle ---
        listSelectButtons = new ArrayList<>();
        listSelectButtons.add(findViewById(R.id.observ_select_all));
        listSelectButtons.add(findViewById(R.id.observ_select_year));
        listSelectButtons.add(findViewById(R.id.observ_select_month));

        weekButton = findViewById(R.id.observ_select_week);

        for (Button button : listSelectButtons) {
            button.setOnClickListener(v -> triggerSelect(button));
        }

        weekButton.setOnClickListener(v -> triggerSelectWeek());

        // --- Toggle Temps de lecture ---
        toggleReadingTime = findViewById(R.id.observ_toggle_reading_time);
        toggleReadingTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            readingTimeMode = isChecked;

            // Grise / dégrise Roman/Manga/Tout
            RadioButton radioRoman = findViewById(R.id.observatory_radio_roman);
            RadioButton radioManga = findViewById(R.id.observatory_radio_manga);
            RadioButton radioAll = findViewById(R.id.observatory_radio_allbooks);
            float alphaBookType = isChecked ? 0.35f : 0.9f;
            radioRoman.setAlpha(alphaBookType);
            radioManga.setAlpha(alphaBookType);
            radioAll.setAlpha(alphaBookType);
            radioRoman.setEnabled(!isChecked);
            radioManga.setEnabled(!isChecked);
            radioAll.setEnabled(!isChecked);
            findViewById(R.id.observatory_radio_group)
                    .setVisibility(isChecked ? View.GONE : View.VISIBLE);

            // Swap des 4 radio buttons plot
            RadioButton radioPlotBook = findViewById(R.id.observatory_radio_plot_book);
            RadioButton radioPlotPage = findViewById(R.id.observatory_radio_plot_page);
            RadioButton radioPlotTime = findViewById(R.id.observatory_radio_plot_time);
            RadioButton radioPlotSessions = findViewById(R.id.observatory_radio_plot_sessions);

            if (isChecked) {
                radioPlotBook.setVisibility(View.GONE);
                radioPlotPage.setVisibility(View.GONE);
                radioPlotTime.setVisibility(View.VISIBLE);
                radioPlotSessions.setVisibility(View.VISIBLE);
                radioPlotTime.setChecked(true);
                sessionCountPlot = false;
                weekButton.setVisibility(View.VISIBLE);
                toggleReadingTime.setBackground(getDrawable(R.drawable.button_ok_gradient));
            } else {
                radioPlotTime.setVisibility(View.GONE);
                radioPlotSessions.setVisibility(View.GONE);
                radioPlotBook.setVisibility(View.VISIBLE);
                radioPlotPage.setVisibility(View.VISIBLE);
                radioPlotBook.setChecked(true);
                pagePlot = false;
                weekButton.setVisibility(View.GONE);
                modeSelectWeek = null;
                toggleReadingTime.setBackground(getDrawable(R.drawable.button_basic_gradient));
            }

            filterBooks();
            addInfos();
            initBarChart();
        });

        // --- RadioGroup type de livre ---
        findViewById(R.id.observatory_radio_allbooks).setOnClickListener(v -> {
            selectedBookType = BookType.ALL;
            filterBooks();
            addInfos();
            initBarChart();
        });
        findViewById(R.id.observatory_radio_roman).setOnClickListener(v -> {
            selectedBookType = BookType.ROMAN;
            filterBooks();
            addInfos();
            initBarChart();
        });
        findViewById(R.id.observatory_radio_manga).setOnClickListener(v -> {
            selectedBookType = BookType.MANGA;
            filterBooks();
            addInfos();
            initBarChart();
        });

        // --- RadioGroup plot ---
        findViewById(R.id.observatory_radio_plot_book).setOnClickListener(v -> {
            pagePlot = false;
            filterBooks();
            addInfos();
            initBarChart();
        });
        findViewById(R.id.observatory_radio_plot_page).setOnClickListener(v -> {
            pagePlot = true;
            filterBooks();
            addInfos();
            initBarChart();
        });

        findViewById(R.id.observatory_radio_plot_time).setOnClickListener(v -> {
            sessionCountPlot = false;
            initBarChart();
        });
        findViewById(R.id.observatory_radio_plot_sessions).setOnClickListener(v -> {
            sessionCountPlot = true;
            initBarChart();
        });

        initBarChart();
        addInfos();
    }

    // -------------------------------------------------------------------------
    // SÉLECTION TEMPORELLE
    // -------------------------------------------------------------------------

    private void triggerSelectWeek() {
        // Désélectionne les autres boutons visuellement
        for (Button b : listSelectButtons) {
            b.setBackground(getDrawable(R.drawable.button_basic_gradient));
        }
        weekButton.setBackground(getDrawable(R.drawable.button_ok_gradient));
        modeSelectWeek = ModeSelectWeek.WEEK;
        weekButton.setText("Semaine");
        addInfos();
        initBarChart();
    }

    private void triggerSelect(Button buttonSelected) {
        // Si on quitte le mode semaine
        modeSelectWeek = null;
        weekButton.setBackground(getDrawable(R.drawable.button_basic_gradient));

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
                    android.app.AlertDialog.Builder alert =
                            new android.app.AlertDialog.Builder(ObservatoryActivity.this);
                    alert.setTitle("Saisie de l'année");
                    final EditText input = new EditText(getApplicationContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setRawInputType(Configuration.KEYBOARD_12KEY);
                    int currentYear = LocalDate.now().getYear();
                    input.setHint(String.valueOf(currentYear));
                    alert.setView(input);
                    alert.setPositiveButton("Valider", (dialog, which) -> {
                        try {
                            String value = input.getText().toString();
                            if (value.equalsIgnoreCase("")) value = input.getHint().toString();
                            Integer year = Integer.parseInt(value);
                            ((Button) findViewById(R.id.observ_select_year))
                                    .setText(String.valueOf(year));
                            modeSelect = ModeSelect.YEAR;
                            selectedYear = year;
                            selectedMonth = null;
                            filterBooks();
                            addInfos();
                            initBarChart();
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.err("Error setting year for observatory", e);
                        }
                    });
                    alert.setNegativeButton("Annuler", (dialog, which) -> {
                        modeSelect = ModeSelect.ALL;
                        triggerSelect(findViewById(R.id.observ_select_all));
                    });
                    alert.show();

                } else if (button.equals(findViewById(R.id.observ_select_month))) {
                    ((Button) findViewById(R.id.observ_select_year)).setText("année");
                    RackMonthPicker picker = new RackMonthPicker(this);
                    picker.setLocale(Locale.FRANCE)
                            .setPositiveButton((month, startDate, endDate, year, monthLabel) -> {
                                currentDataBooksList = new ArrayList<>(LibraryLoader.getShelf());
                                ((Button) findViewById(R.id.observ_select_month))
                                        .setText(month + "/" + year);
                                modeSelect = ModeSelect.MONTH;
                                selectedMonth = month;
                                selectedYear = year;
                                filterBooks();
                                addInfos();
                                initBarChart();
                            })
                            .setNegativeButton(dialog -> {
                                modeSelect = ModeSelect.ALL;
                                triggerSelect(findViewById(R.id.observ_select_all));
                                dialog.dismiss();
                            });
                    picker.setColorTheme(R.color.primary_light_blue);
                    picker.show();
                }
            } else {
                button.setBackground(getDrawable(R.drawable.button_basic_gradient));
            }
        }
    }

    // -------------------------------------------------------------------------
    // FILTRAGE DES SESSIONS DE TEMPS DE LECTURE
    // -------------------------------------------------------------------------

    /**
     * Retourne les sessions filtrées selon la plage temporelle sélectionnée.
     * Les sessions sont dans AccessStats.getSessionLog() sous forme Map<String, SessionData>
     * avec clé "yyyy-MM-dd HH:mm".
     */
    private List<Map.Entry<String, Library.AccessStats.SessionData>> getFilteredSessions() {
        Map<String, Library.AccessStats.SessionData> all =
                LibraryLoader.getAccessStats().getSessionLog();
        if (all == null) return new ArrayList<>();

        List<Map.Entry<String, Library.AccessStats.SessionData>> result = new ArrayList<>();
        for (Map.Entry<String, Library.AccessStats.SessionData> entry : all.entrySet()) {
            String key = entry.getKey(); // "2026-04-30 22:45"
            try {
                LocalDate date = LocalDate.parse(key.substring(0, 10));
                boolean keep = true;
                if (selectedYear != null && date.getYear() != selectedYear) keep = false;
                if (selectedMonth != null && date.getMonthValue() != selectedMonth) keep = false;
                if (keep) result.add(entry);
            } catch (Exception e) {
                // clé malformée, on ignore
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // STATS TEXTE
    // -------------------------------------------------------------------------

    private void addInfos() {
        LinearLayout infos = findViewById(R.id.obser_data_line_info);
        infos.removeAllViews();
        alternate = false;

        if (readingTimeMode) {
            addInfosReadingTime();
        } else {
            addInfosBooks();
        }
    }

    private void addInfosReadingTime() {
        List<Map.Entry<String, Library.AccessStats.SessionData>> sessions = getFilteredSessions();

        if (sessions.isEmpty()) {
            findViewById(R.id.obs_list_infos).setVisibility(View.GONE);
            findViewById(R.id.obs_no_book).setVisibility(View.VISIBLE);
            return;
        }
        findViewById(R.id.obs_list_infos).setVisibility(View.VISIBLE);
        findViewById(R.id.obs_no_book).setVisibility(View.GONE);

// Stats du jour — toujours sur toutes les sessions, indépendant du filtre
        String todayKey = LocalDate.now().toString(); // "2026-05-01"
        int todaySessions = 0;
        int todayMinutes = 0;
        Map<String, Library.AccessStats.SessionData> allSessions =
                LibraryLoader.getAccessStats().getSessionLog();
        if (allSessions != null) {
            for (Map.Entry<String, Library.AccessStats.SessionData> e : allSessions.entrySet()) {
                if (e.getKey().startsWith(todayKey)) {
                    todaySessions++;
                    todayMinutes += e.getValue().getMinutes();
                }
            }
        }
        addInfo("Sessions aujourd'hui", String.valueOf(todaySessions));
        addInfo("Temps de lecture aujourd'hui", formatMinutes(todayMinutes));

        int totalMinutes = 0;
        int totalPages = 0;
        int minMinutes = Integer.MAX_VALUE;
        int maxMinutes = 0;
        String maxDay = "";

        // Accumulation par jour de semaine (0=Lundi … 6=Dimanche)
        int[] minutesByDow = new int[7];
        int[] countByDow = new int[7];

        // Accumulation par date pour trouver le meilleur jour
        Map<String, Integer> minutesByDate = new TreeMap<>();

        for (Map.Entry<String, Library.AccessStats.SessionData> entry : sessions) {
            int min = entry.getValue().getMinutes();
            int pages = entry.getValue().getPages();
            String dateStr = entry.getKey().substring(0, 10); // "2026-04-30"

            totalMinutes += min;
            totalPages += pages;
            if (min < minMinutes) minMinutes = min;
            if (min > maxMinutes) {
                maxMinutes = min;
                maxDay = dateStr;
            }

            try {
                LocalDate date = LocalDate.parse(dateStr);
                int dow = date.getDayOfWeek().getValue() - 1; // 0=Lun
                minutesByDow[dow] += min;
                countByDow[dow]++;
            } catch (Exception ignored) {
            }

            minutesByDate.merge(dateStr, min, Integer::sum);
        }

        // Meilleur jour absolu (somme sur la journée)
        String bestDate = "";
        int bestDateMinutes = 0;
        for (Map.Entry<String, Integer> e : minutesByDate.entrySet()) {
            if (e.getValue() > bestDateMinutes) {
                bestDateMinutes = e.getValue();
                bestDate = e.getKey();
            }
        }

        int sessionCount = sessions.size();
        int avgMinutes = sessionCount > 0 ? totalMinutes / sessionCount : 0;

        addInfo("Nombre de sessions enregistrées", String.valueOf(sessionCount));
        addInfo("Temps total de lecture", formatMinutes(totalMinutes));
        addInfo("Durée moyenne par session", formatMinutes(avgMinutes));
        addInfo("Session la plus courte", formatMinutes(minMinutes == Integer.MAX_VALUE ? 0 : minMinutes));
        addInfo("Session la plus longue", formatMinutes(maxMinutes));

        if (!bestDate.isEmpty()) {
            // Reformater "2026-04-30" → "30/04/26"
            String bestDateFr = bestDate.substring(8) + "/"
                    + bestDate.substring(5, 7) + "/"
                    + bestDate.substring(2, 4);
            addInfo("Jour où tu as le plus lu (" + bestDateFr + ")",
                    formatMinutes(bestDateMinutes));
        }

        // Moyenne par jour de semaine
        String[] dowNames = {"Lundi", "Mardi", "Mercredi", "Jeudi",
                "Vendredi", "Samedi", "Dimanche"};
        for (int i = 0; i < 7; i++) {
            if (countByDow[i] > 0) {
                addInfo("Temps moyen le " + dowNames[i],
                        formatMinutes(minutesByDow[i] / countByDow[i]));
            }
        }

        // Temps par page
        if (totalPages > 0) {
            float minPerPage = (float) totalMinutes / totalPages;
            addInfo("Temps moyen par page",
                    String.format("%.1f min/page", minPerPage));
        }

        // Nombre de jours distincts lus
        addInfo("Jours distincts avec lecture", String.valueOf(minutesByDate.size()));

        // Moyenne quotidienne (totalMinutes / jours distincts)
        if (!minutesByDate.isEmpty()) {
            addInfo("Temps de lecture quotidien moyen",
                    formatMinutes(totalMinutes / minutesByDate.size()));
        }


    }

    /**
     * Formate des minutes en "Xh YYmin" ou "YYmin" si < 60.
     */
    private String formatMinutes(int totalMin) {
        if (totalMin <= 0) return "0 min";
        int h = totalMin / 60;
        int m = totalMin % 60;
        if (h == 0) return m + " min";
        if (m == 0) return h + "h";
        return h + "h " + String.format("%02d", m) + "min";
    }


    // Stats livres — méthode existante renommée pour clarté
    private void addInfosBooks() {
        if (currentDataBooksList == null || currentDataBooksList.size() < 1) {
            findViewById(R.id.obs_list_infos).setVisibility(View.GONE);
            findViewById(R.id.obs_no_book).setVisibility(View.VISIBLE);
            return;
        } else {
            findViewById(R.id.obs_list_infos).setVisibility(View.VISIBLE);
            findViewById(R.id.obs_no_book).setVisibility(View.GONE);
        }

        // --- contenu identique à ton addInfos() original ---
        addInfo("Nombre de " + getBookTypeDisplay() + "s",
                String.valueOf(currentDataBooksList.size()));

        LocalDate minDate = null, maxDate = null;
        Integer nBookPaged = 0, minPage = null, maxPage = null, totalPages = 0;
        Integer nUnfinishedBooks = 0, nTotalBooksRead = 0, nTotalPagesRead = 0;
        Book mostReadBook = null;
        Map<Integer, Integer> numberOfReadHisto = new HashMap<>();

        for (Book book : currentDataBooksList) {
            if (book.getMaxPages() != null) {
                nBookPaged++;
                totalPages += book.getMaxPages();
                if (maxPage == null || maxPage < book.getMaxPages()) maxPage = book.getMaxPages();
                if (minPage == null || minPage > book.getMaxPages()) minPage = book.getMaxPages();
            }
            if (book.getLastEndTime() != null) {
                try {
                    LocalDate dt = LocalDate.parse(book.getLastEndTime(), Constants.DATE_FORMATTER);
                    if (minDate == null || dt.isBefore(minDate)) minDate = dt;
                    if (maxDate == null || dt.isAfter(maxDate)) maxDate = dt;
                } catch (Exception e) {
                    e.printStackTrace();
                    log.err("Error while parsing dates for observatory", e);
                }
                nTotalBooksRead += book.getEndTimes().size();
                if (book.getMaxPages() != null)
                    nTotalPagesRead += book.getMaxPages() * book.getEndTimes().size();
                if (mostReadBook == null ||
                        book.getEndTimes().size() > mostReadBook.getEndTimes().size())
                    mostReadBook = book;
                numberOfReadHisto.put(book.getEndTimes().size(),
                        numberOfReadHisto.getOrDefault(book.getEndTimes().size(), 0) + 1);
            } else {
                nUnfinishedBooks++;
            }
        }

        if (nBookPaged != 0)
            addInfo("Nombre de " + getBookTypeDisplay() + "s avec pages", String.valueOf(nBookPaged));
        if (minPage != null) addInfo("Nombre minimum de pages", String.valueOf(minPage));
        if (maxPage != null) addInfo("Nombre maximum de pages", String.valueOf(maxPage));
        if (nBookPaged != 0)
            addInfo("Nombre de pages en moyenne par " + getBookTypeDisplay(), String.valueOf(totalPages / nBookPaged));

        try {
            long nMonth = minDate.until(maxDate, ChronoUnit.MONTHS) + 1;
            addInfo("Nombre de " + getBookTypeDisplay() + "s lu par mois en moyenne",
                    String.format("%.2f", (1.0 * nTotalBooksRead) / (1.0 * nMonth)));
        } catch (Exception ignored) {
        }

        if (nBookPaged != 0)
            addInfo("Estimation nombre de pages lu", String.valueOf(nTotalPagesRead));
        try {
            addInfo("Nombre de " + getBookTypeDisplay() + " pas fini", String.valueOf(nUnfinishedBooks));
        } catch (Exception ignored) {
        }

        try {
            long nDays = minDate.until(maxDate, ChronoUnit.DAYS);
            addInfo("Date du " + getBookTypeDisplay() + " le plus ancien fini", Constants.DATE_FORMATTER.format(minDate));
            addInfo("Date du " + getBookTypeDisplay() + " le plus recemment fini", Constants.DATE_FORMATTER.format(maxDate));
            addInfo("Nombre de jour passé à lire", String.valueOf(nDays));
            addInfo("Nombre de pages par jour en moyenne", String.format("%.2f", (1.0 * nTotalPagesRead) / (1.0 * nDays)));
        } catch (Exception ignored) {
        }

        for (Map.Entry<Integer, Integer> entry : numberOfReadHisto.entrySet())
            addInfo("Nombre de " + getBookTypeDisplay() + " lu " + entry.getKey() + " fois", String.valueOf(entry.getValue()));

        try {
            addInfo("Le " + getBookTypeDisplay() + " le plus lu (" + mostReadBook.getEndTimes().size() + " fois)",
                    mostReadBook.getName().substring(0, 30) +
                            (mostReadBook.getName().length() > 30 ? "..." : ""));
        } catch (Exception ignored) {
        }

        try {
            addInfo("Nombre de connexions total", String.valueOf(LibraryLoader.getAccessStats().getnTotal()));
            addInfo("Plus grande chaine de connexion", String.valueOf(LibraryLoader.getAccessStats().getBestStreak()));
            addInfo("Première connexion", LibraryLoader.getAccessStats().getFirstLog());
            addInfo("Dernière connexion", LibraryLoader.getAccessStats().getLastLog());
            addInfo("Chaine de connexion actuelle", String.valueOf(LibraryLoader.getAccessStats().getnStreak()));
            float nConnexionDay = (float) LibraryLoader.getAccessStats().getnTotal()
                    / LibraryLoader.getAccessStats().getNdaysBetweenFirstAndCurrent();
            addInfo("Nombre de connexion par jour", String.format("%.1f", nConnexionDay));
            addInfo("Nombre de connexion par semaine", String.format("%.1f", nConnexionDay * 7));
        } catch (Exception ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // GRAPH
    // -------------------------------------------------------------------------

    private void initBarChart() {
        chart = findViewById(R.id.line_chart);
        chart.invalidate();
        chart.fitScreen();

        boolean hasData = readingTimeMode
                ? !getFilteredSessions().isEmpty()
                : currentDataBooksList.size() > 0;

        if (!hasData) {
            chart.setVisibility(View.GONE);
            findViewById(R.id.obs_no_graph).setVisibility(View.VISIBLE);
            return;
        }

        chart.setVisibility(View.VISIBLE);
        findViewById(R.id.obs_no_graph).setVisibility(View.GONE);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        chart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        chart.getXAxis().setDrawGridLines(false);

        labelList = new ArrayList<>();
        LineData data = new LineData();

        if (readingTimeMode) {
            data.addDataSet(sessionCountPlot
                    ? computeSessionCountDataSet()
                    : computeReadingTimeDataSet());
            data.setValueTextColor(getColor(R.color.primary_dark_blue));
            chart.setData(data);
            if (sessionCountPlot) {
                chart.getAxisLeft().setValueFormatter(new LargeValueFormatter());
                chart.getAxisLeft().setGranularity(1f);
                chart.getAxisLeft().setGranularityEnabled(true);
            } else {
                chart.getAxisLeft().setValueFormatter(
                        new com.github.mikephil.charting.formatter.ValueFormatter() {
                            @Override
                            public String getFormattedValue(float value) {
                                int h = (int) value / 60;
                                int m = (int) value % 60;
                                if (h == 0) return m + "m";
                                if (m == 0) return h + "h";
                                return h + "h" + String.format("%02d", m);
                            }
                        });
            }
        } else {
            data.addDataSet(computeLineDataSet(
                    "nombre de " + (pagePlot ? "pages pour les " : "") +
                            getBookTypeDisplay() + "s lu"));
            data.setValueTextColor(getColor(R.color.primary_dark_blue));
            chart.setData(data);
            chart.getAxisLeft().setValueFormatter(new com.github.mikephil.charting.formatter.LargeValueFormatter());
        }

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
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelList));
        xAxis.setTextColor(getColor(R.color.primary_dark_blue));
        xAxis.setLabelRotationAngle(-90);

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

    private LineDataSet computeReadingTimeDataSet() {
        List<Map.Entry<String, Library.AccessStats.SessionData>> sessions = getFilteredSessions();
        String[] dowNames = {"Lundi", "Mardi", "Mercredi", "Jeudi",
                "Vendredi", "Samedi", "Dimanche"};

        ArrayList<Entry> listVal = new ArrayList<>();

        if (modeSelectWeek != null) {
            // Mode Semaine : somme par jour de semaine
            int[] minutesByDow = new int[7];
            for (Map.Entry<String, Library.AccessStats.SessionData> entry : sessions) {
                try {
                    LocalDate date = LocalDate.parse(entry.getKey().substring(0, 10));
                    int dow = date.getDayOfWeek().getValue() - 1;
                    minutesByDow[dow] += entry.getValue().getMinutes();
                } catch (Exception ignored) {
                }
            }
            for (int i = 0; i < 7; i++) {
                labelList.add(dowNames[i]);
                String descr = formatMinutes(minutesByDow[i]) + " le " + dowNames[i];
                listVal.add(new Entry(i, minutesByDow[i], descr));
            }
        } else {
            // Mode normal : même logique que computeLineDataSet mais en minutes
            TreeMap<String, Integer> minutesByPeriod = new TreeMap<>();
            for (Map.Entry<String, Library.AccessStats.SessionData> entry : sessions) {
                String key = entry.getKey(); // "2026-04-30 22:45"
                String dateStr = key.substring(0, 10); // "2026-04-30"
                try {
                    LocalDate date = LocalDate.parse(dateStr);
                    String periodKey;
                    if (modeSelect == ModeSelect.MONTH) {
                        // Par jour dans le mois
                        periodKey = String.format("%02d/%02d/%02d",
                                date.getYear() % 100,
                                date.getMonthValue(),
                                date.getDayOfMonth());
                    } else {
                        // Par mois
                        periodKey = String.format("%02d/%02d",
                                date.getYear() % 100,
                                date.getMonthValue());
                    }
                    minutesByPeriod.merge(periodKey, entry.getValue().getMinutes(), Integer::sum);
                } catch (Exception ignored) {
                }
            }

            int index = 0;
            for (Map.Entry<String, Integer> entry : minutesByPeriod.entrySet()) {
                labelList.add(entry.getKey());
                String descr = formatMinutes(entry.getValue()) + " en " + entry.getKey();
                listVal.add(new Entry(index, entry.getValue(), descr));
                index++;
            }
        }

        LineDataSet set = new LineDataSet(listVal,
                "temps de lecture (" + (modeSelectWeek != null ? "par jour" : "total") + ")");
        set.setValueTextSize(15);
        set.setValueTextColor(getColor(R.color.primary_dark_blue));
        set.setCircleHoleColor(getColor(R.color.primary_light_blue));
        set.setColor(getColor(R.color.primary_dark_blue));
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setCircleColor(getColor(R.color.primary_dark_blue));
        // Formateur inline pour les labels sur les points
        set.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return formatMinutes((int) entry.getY());
            }
        });
        return set;
    }

    private LineDataSet computeSessionCountDataSet() {
        List<Map.Entry<String, Library.AccessStats.SessionData>> sessions = getFilteredSessions();
        String[] dowNames = {"Lundi", "Mardi", "Mercredi", "Jeudi",
                "Vendredi", "Samedi", "Dimanche"};
        ArrayList<Entry> listVal = new ArrayList<>();

        if (modeSelectWeek != null) {
            int[] sessionsByDow = new int[7];
            for (Map.Entry<String, Library.AccessStats.SessionData> entry : sessions) {
                try {
                    LocalDate date = LocalDate.parse(entry.getKey().substring(0, 10));
                    sessionsByDow[date.getDayOfWeek().getValue() - 1]++;
                } catch (Exception ignored) {
                }
            }
            for (int i = 0; i < 7; i++) {
                labelList.add(dowNames[i]);
                listVal.add(new Entry(i, sessionsByDow[i],
                        sessionsByDow[i] + " session(s) le " + dowNames[i]));
            }
        } else {
            TreeMap<String, Integer> sessionsByPeriod = new TreeMap<>();
            for (Map.Entry<String, Library.AccessStats.SessionData> entry : sessions) {
                try {
                    LocalDate date = LocalDate.parse(entry.getKey().substring(0, 10));
                    String periodKey = (modeSelect == ModeSelect.MONTH)
                            ? String.format("%02d/%02d/%02d",
                            date.getYear() % 100, date.getMonthValue(), date.getDayOfMonth())
                            : String.format("%02d/%02d",
                            date.getYear() % 100, date.getMonthValue());
                    sessionsByPeriod.merge(periodKey, 1, Integer::sum);
                } catch (Exception ignored) {
                }
            }
            int index = 0;
            for (Map.Entry<String, Integer> entry : sessionsByPeriod.entrySet()) {
                labelList.add(entry.getKey());
                listVal.add(new Entry(index, entry.getValue(),
                        entry.getValue() + " session(s) en " + entry.getKey()));
                index++;
            }
        }

        LineDataSet set = new LineDataSet(listVal,
                "sessions (" + (modeSelectWeek != null ? "par jour" : "par période") + ")");
        set.setValueTextSize(15);
        set.setValueTextColor(getColor(R.color.primary_dark_blue));
        set.setCircleHoleColor(getColor(R.color.primary_light_blue));
        set.setColor(getColor(R.color.primary_dark_blue));
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setCircleColor(getColor(R.color.primary_dark_blue));
        set.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.valueOf((int) entry.getY());
            }
        });
        return set;
    }

    // -------------------------------------------------------------------------
    // MÉTHODES EXISTANTES INCHANGÉES
    // -------------------------------------------------------------------------

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
            if (pagePlot) {
                int nPages = book.getMaxPages() == null ? 0 : book.getMaxPages();
                monthCount.put(valueMap, monthCount.get(valueMap) + nPages);
            } else {
                monthCount.put(valueMap, monthCount.get(valueMap) + 1);
            }

        }

        ArrayList<Entry> listVal = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : monthCount.entrySet()) {
            labelList.add(entry.getKey());
            String descr;
            if (modeSelect.equals(ModeSelect.MONTH)) {
                int day = Integer.parseInt(entry.getKey().substring(6));
                int month = Integer.parseInt(entry.getKey().substring(3, 5));

                descr = entry.getValue() + " " + (pagePlot ? "pages pour les " : "") + getBookTypeDisplay() + "s lu le " + day + " " + DateFormatSymbols.getInstance().getMonths()[month - 1].toLowerCase() + " " + "20" + entry.getKey().substring(0, 2);
            } else {
                int month = Integer.parseInt(entry.getKey().substring(3));
                descr = entry.getValue() + " " + (pagePlot ? "pages pour les " : "") + getBookTypeDisplay() + "s lu en " + DateFormatSymbols.getInstance().getMonths()[month - 1].toLowerCase() + " " + "20" + entry.getKey().substring(0, 2);
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

    // -------------------------------------------------------------------------
    // CYCLE DE VIE & ENUM
    // -------------------------------------------------------------------------

    @Override
    protected void onResumeCustom() {
        checkOrientStart(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @Override
    protected void onBackPressedCustom() {
    }

    @Override
    protected void onDestroyCustom() {
    }

    private void checkOrientStart(int screenOrientation) {
        if (getRequestedOrientation() != screenOrientation) {
            setRequestedOrientation(screenOrientation);
            new Handler().postDelayed(
                    () -> setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR),
                    1000);
        }
    }

    @Override
    public boolean onOptionsItemSelectedCustom(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
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
        final Display display =
                ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case Surface.ROTATION_90:
                startActivity(new Intent(this, ShelfActivity.class));
                finish();
                break;
        }
    }

    private enum ModeSelect {ALL, YEAR, MONTH}

    private enum ModeSelectWeek {WEEK}
}