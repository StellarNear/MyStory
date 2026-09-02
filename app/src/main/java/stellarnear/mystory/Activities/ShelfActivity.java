package stellarnear.mystory.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.Activities.Fragments.MainActivityFragment;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentSearchBooks;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentWishList;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.BooksLibs.BookType;
import stellarnear.mystory.BooksLibs.Note;
import stellarnear.mystory.Constants;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.DatePickerFragment;
import stellarnear.mystory.UITools.ListBookAdapter;
import stellarnear.mystory.UITools.MyLottieDialog;

public class ShelfActivity extends CustomActivity {


    private Window window;
    private Toolbar toolbar;

    private MainActivityFragment mainFrag;
    private MainActivityFragmentSearchBooks searchFrag;
    private MainActivityFragmentWishList wishListFrag;

    private FloatingActionButton fabSearchPanel;
    private FloatingActionButton fabWishList;
    private ListBookAdapter bookAdapter;
    private Book selectedBook;
    private final Tools tools = new Tools();

    private LinearLayout scrollviewNotes;

    private ChronoLocalDate minDate = null;
    private ChronoLocalDate maxDate = null;
    private String currentSearch = null; // null = pas de filtre texte actif
    private DiscreteScrollView scrollView;
    private Slider shelfSlider;


    @Override
    protected void onCreateCustom() throws Exception {
        int themeId = getResources().getIdentifier("AppThemeBrown", "style", getPackageName());
        setTheme(themeId);

        setContentView(R.layout.activity_shelf);

        toolbar = findViewById(R.id.toolbar);
        window = getWindow();
        window.setStatusBarColor(getColor(R.color.primary_middle_brown));
        toolbar.setBackgroundColor(getColor(R.color.primary_dark_brown));
        toolbar.setTitleTextColor(getColor(R.color.primary_light_brown));
        toolbar.getOverflowIcon().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.primary_light_brown), BlendModeCompat.SRC_ATOP));
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("L'étagère");
                toolbar.setBackground(getDrawable(R.drawable.shelf_bar_back2));
            }
        });
        initShelf();
    }

    private void initShelf() {
        List<Book> listShelf = LibraryLoader.getShelf();
        if (minDate != null) {
            listShelf = filterWithDate("min", listShelf);
        }
        if (maxDate != null) {
            listShelf = filterWithDate("max", listShelf);
        }
        // Filtre texte — combiné avec les filtres de date
        if (currentSearch != null && !currentSearch.trim().isEmpty()) {
            listShelf = filterWithSearch(currentSearch, listShelf);
        }
        if (listShelf != null && listShelf.size() > 0) {
            initPage(listShelf);
        } else {
            findViewById(R.id.shelfPicker).setVisibility(View.GONE);
            findViewById(R.id.shelf_info_sub).setVisibility(View.GONE);
            findViewById(R.id.shelf_book_linear).setVisibility(View.GONE);
            findViewById(R.id.shelf_no_book).setVisibility(View.VISIBLE);
            findViewById(R.id.shelf_toolbar_infos_dates_line).setVisibility(View.GONE);
            findViewById(R.id.shelf_slider).setVisibility(View.GONE);
        }

    }

    private List<Book> filterWithSearch(String query, List<Book> listShelf) {
        String q = query.toLowerCase().trim();
        List<Book> result = new ArrayList<>();
        for (Book book : listShelf) {
            boolean titleMatch = book.getName() != null
                    && book.getName().toLowerCase().contains(q);
            boolean authorMatch = book.getAutor() != null
                    && book.getAutor().getFullName() != null
                    && book.getAutor().getFullName().toLowerCase().contains(q);
            if (titleMatch || authorMatch) {
                result.add(book);
            }
        }
        return result;
    }


    private List<Book> filterWithDate(String mode, List<Book> listShelf) {
        List<Book> result = new ArrayList<>();

        for (Book book : listShelf) {
            if (book.getLastEndTime() == null) {
                continue; // filtrage date actif par choix utilisateur -> on exclut les livres sans fin de lecture
            }
            try {
                if (mode.equalsIgnoreCase("min")) {
                    LocalDate dt = LocalDate.parse(book.getLastEndTime(), Constants.DATE_FORMATTER);
                    if (dt.isAfter(minDate) || dt.isEqual(minDate)) {
                        result.add(book);
                    }
                } else if (mode.equalsIgnoreCase("max")) {
                    LocalDate dt = LocalDate.parse(book.getLastEndTime(), Constants.DATE_FORMATTER);
                    if (dt.isBefore(maxDate) || dt.isEqual(maxDate)) {
                        result.add(book);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.err("Error while getting dates for a book (" + book.getName() + ") in shelf selection", e);
            }
        }
        return result;
    }

    private void initPage(List<Book> listShelf) {
        scrollView = findViewById(R.id.shelfPicker);
        scrollView.removeAllViews();
        scrollView.setSlideOnFling(true);
        scrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());


        bookAdapter = new ListBookAdapter(listShelf, scrollView, true);
        scrollView.setAdapter(bookAdapter);
        scrollView.scrollToPosition(listShelf.size() - 1);

        findViewById(R.id.shelf_forward_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView.smoothScrollToPosition(listShelf.size() - 1);
            }
        });
        findViewById(R.id.shelf_backward_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView.smoothScrollToPosition(0);
            }
        });

        shelfSlider = findViewById(R.id.shelf_slider);
        shelfSlider.setValueFrom(0);
        if (listShelf.size() <= 1) {
            // Un seul livre : le slider n'a pas de sens, on le cache
            shelfSlider.setVisibility(View.GONE);
        } else {
            shelfSlider.setVisibility(View.VISIBLE);
            shelfSlider.setValueTo(listShelf.size() - 1);
            shelfSlider.setStepSize(1);
        }

        shelfSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                // Optional: pause updates, animations, etc.
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // Do the actual scroll once user has released the slider
                float value = slider.getValue();
                scrollView.smoothScrollToPosition(Math.round(value));
            }
        });


        shelfSlider.setLabelFormatter(value -> {
            int index = Math.round(value);
            String bookDate = listShelf.get(index).getLastEndTime();
            return bookDate; // Implement this method
        });

        // then current book info

        TextView infoLine1 = findViewById(R.id.shelf_book_info_line1);
        infoLine1.setVisibility(View.VISIBLE);
        infoLine1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpSummary();
            }
        });

        infoLine1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                popupSelectMaxPage();
                return false;
            }
        });


        TextView infoLine2 = findViewById(R.id.shelf_book_info_line2);
        infoLine2.setVisibility(View.VISIBLE);

        TextView toolBarInfo = findViewById(R.id.shelf_toolbar_infos);
        Integer nNovel = 0;
        Integer nManga = 0;
        for (Book book : bookAdapter.getBooks()) {
            if (book.getBookType() == BookType.ROMAN) {
                nNovel++;
            } else {
                nManga++;
            }
        }
        toolBarInfo.setText(bookAdapter.getItemCount() + " livres" + "\n(" + nNovel + " romans" + ", " + nManga + " mangas)");
        findStartAndEndDate(findViewById(R.id.shelf_toolbar_infos_start_date), findViewById(R.id.shelf_toolbar_infos_end_date));

        // --- Bouton recherche ---
        ImageView searchButton = findViewById(R.id.shelf_search_button);
        searchButton.setOnClickListener(v -> popupSearch());
// Si une recherche est déjà active, on colore le bouton en vert dès le départ
        if (currentSearch != null && !currentSearch.trim().isEmpty()) {
            searchButton.setImageTintList(
                    android.content.res.ColorStateList.valueOf(
                            getColor(R.color.start_gradient_button_ok)));
        }

        scrollView.addOnItemChangedListener(new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
            @Override
            public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
                selectedBook = bookAdapter.getBook(adapterPosition);

                shelfSlider.setValue(adapterPosition);

                String info1 = selectedBook.getName();

                info1 += " de " + selectedBook.getAutor().getFullName();

                if (selectedBook.getMaxPages() != null) {
                    info1 += " (" + selectedBook.getMaxPages() + " pages)";
                }
                if (selectedBook.getBookType() == BookType.MANGA) {
                    info1 += " [Manga]";
                } else {
                    info1 += " [Roman]";
                }

                infoLine1.setText(info1);

                String info2 = "";
                if (selectedBook.getLastStartTime() != null) {
                    info2 += "Débuté le " + selectedBook.getLastStartTime();
                }
                if (selectedBook.getLastEndTime() != null) {
                    info2 += " fini le " + selectedBook.getLastEndTime();
                }
                if (selectedBook.getEndTimes().size() > 1) {
                    info2 += " (lu " + selectedBook.getEndTimes().size() + " fois)";
                }
                infoLine2.setText(info2);
                infoLine2.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        DatePickerFragment datePickerFragment = new DatePickerFragment(selectedBook, selectedBook.getLastStartTime());
                        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
                        return false;
                    }
                });
            }
        });


        findViewById(R.id.shelf_add_to_current).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupAddToCurrent();
            }
        });

        findViewById(R.id.shelf_book_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupDiplayNotes();
            }
        });

        findViewById(R.id.shelf_delete_book).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupDeleteBook();
            }
        });
    }


    private void popupSelectMaxPage() {

        LayoutInflater inflater = getLayoutInflater();

        View alert = inflater.inflate(R.layout.my_lottie_alert, null);

        View alertInnerInfo = inflater.inflate(R.layout.inner_alert_infos, null);

        ((TextView) alertInnerInfo.findViewById(R.id.alert_title_info)).setText(selectedBook.getName());
        ((TextView) alertInnerInfo.findViewById(R.id.alert_author_info)).setText(selectedBook.getAutor().getFullName());
        if (selectedBook.getMaxPages() != null) {
            ((EditText) alertInnerInfo.findViewById(R.id.radio_page_other_prompt)).setHint(selectedBook.getMaxPages() + " pages");
        }
        if (selectedBook.getBookType() == BookType.MANGA) {
            ((RadioButton) alertInnerInfo.findViewById(R.id.radio_manga)).setChecked(true);
        }

        Button okButton = new Button(getApplicationContext());
        okButton.setBackground(getApplicationContext().getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Valider");
        LinearLayout.LayoutParams paramInner = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = getResources().getDimensionPixelSize(R.dimen.general_margin);
        paramInner.setMargins(margin, margin, margin, margin);
        alertInnerInfo.setLayoutParams(paramInner);

        okButton.setTextColor(getApplicationContext().getColor(R.color.end_gradient_button_ok));


        Button cancelButton = new Button(getApplicationContext());
        cancelButton.setBackground(getApplicationContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Annuler");
        cancelButton.setTextColor(getApplicationContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(ShelfActivity.this, alert)
                .setTitle("Nombre de pages")
                .setMessage(alertInnerInfo)
                .setCancelable(false)
                .addActionButton(cancelButton)
                .addActionButton(okButton)
                .setOnShowListener(dialogInterface -> {
                })
                .setOnDismissListener(dialogInterface -> {
                })
                .setOnCancelListener(dialogInterface -> {
                });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton mangaButton = alertInnerInfo.findViewById(R.id.radio_manga);
                if (mangaButton.isChecked()) {
                    selectedBook.setBookType(BookType.MANGA);
                } else {
                    selectedBook.setBookType(BookType.ROMAN);
                }
                try {
                    EditText valuePage = alertInnerInfo.findViewById(R.id.radio_page_other_prompt);
                    Integer page = Integer.parseInt(valuePage.getText().toString());
                    selectedBook.setMaxPages(page);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.err("Error while getting setting max page", e);
                }
                tools.customSnack(getApplicationContext(), okButton, "Infos mise à jour, le " + selectedBook.getBookTypeDisplay() + " a " + selectedBook.getMaxPages() + " pages !", "brownshort");
                LibraryLoader.saveBook(selectedBook);
                dialog.dismiss();
                initShelf();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(getButtonParam());
        okButton.setLayoutParams(getButtonParam());


    }


    private void popUpSummary() {
        if (selectedBook != null) {
            LayoutInflater inflater = getLayoutInflater();
            View alert = inflater.inflate(R.layout.my_lottie_alert, null);

            View summary = inflater.inflate(R.layout.summary_scroll, null);

            TextView sumTxt = summary.findViewById(R.id.summary_text);
            sumTxt.setText(selectedBook.getSummary());

            LinearLayout.LayoutParams paramInner = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, getApplicationContext().getResources().getDimensionPixelSize(R.dimen.scroll_wish_list_height));
            int margin = getResources().getDimensionPixelSize(R.dimen.general_margin);
            paramInner.setMargins(margin, margin, margin, margin);
            summary.setLayoutParams(paramInner);

            Button cancelButton = new Button(getApplicationContext());
            cancelButton.setBackground(getApplicationContext().getDrawable(R.drawable.button_cancel_gradient));
            cancelButton.setText("Fermer");
            cancelButton.setTextColor(getApplicationContext().getColor(R.color.end_gradient_button_cancel));

            MyLottieDialog dialog = new MyLottieDialog(ShelfActivity.this, alert)
                    .setTitle("Résumé du livre")
                    .setMessage(summary)
                    .setCancelable(false)
                    .addActionButton(cancelButton)
                    .setOnShowListener(dialogInterface -> {
                    })
                    .setOnDismissListener(dialogInterface -> {
                    })
                    .setOnCancelListener(dialogInterface -> {
                    });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            cancelButton.setLayoutParams(getButtonParam());
        }
    }

    private LinearLayout.LayoutParams getButtonParam() {
        int margin = getResources().getDimensionPixelSize(R.dimen.general_margin);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(0, margin, 0, 0);
        return param;
    }


    private void findStartAndEndDate(View startId, View endId) {
        TextView start = (TextView) startId;
        TextView end = (TextView) endId;
        for (Book book : bookAdapter.getBooks()) {
            if (book.getLastEndTime() == null) {
                continue;
            }
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
                log.err("Error while getting dates for a book (" + book.getName() + ") in shelf selection", e);
            }
        }
        start.setText(Constants.DATE_FORMATTER.format(minDate));
        end.setText(Constants.DATE_FORMATTER.format(maxDate));

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment datePickerFragment = new DatePickerFragment(Constants.DATE_FORMATTER.format(minDate));
                datePickerFragment.show(getSupportFragmentManager(), "datePicker");
                datePickerFragment.setOnDateSetListener(new DatePickerFragment.OnDateSetListener() {
                    @Override
                    public void onEvent(String result) {
                        minDate = LocalDate.parse(result, Constants.DATE_FORMATTER);
                        initShelf();
                    }
                });

            }
        });
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment datePickerFragment = new DatePickerFragment(Constants.DATE_FORMATTER.format(maxDate));
                datePickerFragment.show(getSupportFragmentManager(), "datePicker");
                datePickerFragment.setOnDateSetListener(new DatePickerFragment.OnDateSetListener() {
                    @Override
                    public void onEvent(String result) {
                        maxDate = LocalDate.parse(result, Constants.DATE_FORMATTER);
                        initShelf();
                    }
                });

            }
        });
    }

    private void popupAddToCurrent() {
        String text = "Recommencer la lecture de " + selectedBook.getName() + " ?";

        Button okButton = new Button(ShelfActivity.this);
        okButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Oui");

        okButton.setTextColor(ShelfActivity.this.getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(ShelfActivity.this);
        cancelButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Non");
        cancelButton.setTextColor(ShelfActivity.this.getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(ShelfActivity.this)
                .setAnimation(R.raw.reading_blank_bakground)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Commencer la lecture")
                .setMessage(text)
                .setCancelable(false)
                .addActionButton(cancelButton)
                .addActionButton(okButton)
                .setOnShowListener(dialogInterface -> {
                })
                .setOnDismissListener(dialogInterface -> {
                })
                .setOnCancelListener(dialogInterface -> {
                });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LibraryLoader.getCurrentBook() == null) {
                    LibraryLoader.addStartTime(selectedBook);
                    LibraryLoader.setCurrentBook(selectedBook);
                    LibraryLoader.removeBookFromShelf(selectedBook);
                    tools.customSnack(ShelfActivity.this, okButton, "Bonne lecture !", "brownshort");
                    selectedBook.setCurrentPercent(0);
                    initShelf();
                    dialog.dismiss();
                } else {
                    popupPutCurrentToShelf();
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(getButtonParam());
        okButton.setLayoutParams(getButtonParam());
    }

    private void popupPutCurrentToShelf() {
        String text = "Tu lis actuellement " + LibraryLoader.getCurrentBook().getName() + " il sera mis sur l'étagère si tu veux recommencer " + selectedBook.getName();

        Button okButton = new Button(ShelfActivity.this);
        okButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Oui");

        okButton.setTextColor(ShelfActivity.this.getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(ShelfActivity.this);
        cancelButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Non");
        cancelButton.setTextColor(ShelfActivity.this.getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(ShelfActivity.this)
                .setAnimation(R.raw.swap_book)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Tu as un livre en cours")
                .setMessage(text)
                .setCancelable(false)
                .addActionButton(cancelButton)
                .addActionButton(okButton)
                .setOnShowListener(dialogInterface -> {
                })
                .setOnDismissListener(dialogInterface -> {
                })
                .setOnCancelListener(dialogInterface -> {
                });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LibraryLoader.putCurrentToShelf();
                LibraryLoader.addStartTime(selectedBook);
                LibraryLoader.setCurrentBook(selectedBook);
                LibraryLoader.removeBookFromShelf(selectedBook);
                tools.customSnack(ShelfActivity.this, okButton, "Bonne lecture !", "brownshort");
                selectedBook.setCurrentPercent(0);
                initShelf();
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(getButtonParam());
        okButton.setLayoutParams(getButtonParam());
    }


    private void popupDiplayNotes() {
        LayoutInflater inflater = getLayoutInflater();
        View alert = inflater.inflate(R.layout.my_lottie_alert, null);
        View notes = inflater.inflate(R.layout.list_notes, null);
        notes.findViewById(R.id.add_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupCreateNote();
            }
        });

        scrollviewNotes = notes.findViewById(R.id.list_note_scrollview);
        addNotesToScrollView();

        Button cancelButton = new Button(ShelfActivity.this);
        cancelButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Fermer");

        cancelButton.setTextColor(ShelfActivity.this.getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(ShelfActivity.this, alert)
                .setAnimation(R.raw.writting)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Les notes sur ce livre")
                .setMessage(notes)
                .setCancelable(false)
                .addActionButton(cancelButton)
                .setOnShowListener(dialogInterface -> {
                })
                .setOnDismissListener(dialogInterface -> {
                })
                .setOnCancelListener(dialogInterface -> {
                });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(getButtonParam());

    }

    private void addNotesToScrollView() {
        scrollviewNotes.removeAllViews();

        for (Note note : selectedBook.getNotes()) {
            View noteView = getLayoutInflater().inflate(R.layout.note, null);
            ((TextView) noteView.findViewById(R.id.note_creation_date)).setText(note.getCreationDate());
            ((TextView) noteView.findViewById(R.id.note_title)).setText(note.getTitle());
            noteView.findViewById(R.id.note_title).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Tools().customToast(ShelfActivity.this, note.getNote());
                }
            });
            noteView.findViewById(R.id.note_deletion).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedBook.deleteNote(note);
                    LibraryLoader.saveBook(selectedBook);
                    addNotesToScrollView();
                }
            });
            scrollviewNotes.addView(noteView);
        }
    }

    private void popupCreateNote() {
        LayoutInflater inflater = getLayoutInflater();
        View alert = inflater.inflate(R.layout.my_lottie_alert, null);
        View notes = inflater.inflate(R.layout.create_note, null);

        Button okButton = new Button(ShelfActivity.this);
        okButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Créer");

        okButton.setTextColor(ShelfActivity.this.getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(ShelfActivity.this);
        cancelButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Fermer");
        cancelButton.setTextColor(ShelfActivity.this.getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(ShelfActivity.this, alert)
                .setAnimation(R.raw.writting)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Creation de note")
                .setMessage(notes)
                .setCancelable(false)
                .addActionButton(cancelButton)
                .addActionButton(okButton)
                .setOnShowListener(dialogInterface -> {
                })
                .setOnDismissListener(dialogInterface -> {
                })
                .setOnCancelListener(dialogInterface -> {
                });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = ((TextView) notes.findViewById(R.id.note_creat_title)).getText().toString();
                String note = ((TextView) notes.findViewById(R.id.note_creat_note)).getText().toString();
                selectedBook.createNote(title, note);
                LibraryLoader.saveBook(selectedBook);
                addNotesToScrollView();
                dialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
        okButton.setLayoutParams(getButtonParam());
        cancelButton.setLayoutParams(getButtonParam());
    }


    private void popupDeleteBook() {
        String text = "Le livre " + selectedBook.getName() + " sera supprimé de la bibliotheque.";

        Button okButton = new Button(ShelfActivity.this);
        okButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Supprimer");

        okButton.setTextColor(ShelfActivity.this.getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(ShelfActivity.this);
        cancelButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Annuler");
        cancelButton.setTextColor(ShelfActivity.this.getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(ShelfActivity.this)
                .setAnimation(R.raw.deletion)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Suppression du livre")
                .setMessage(text)
                .setCancelable(false)
                .addActionButton(cancelButton)
                .addActionButton(okButton)
                .setOnShowListener(dialogInterface -> {
                })
                .setOnDismissListener(dialogInterface -> {
                })
                .setOnCancelListener(dialogInterface -> {
                });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LibraryLoader.removeBookFromShelf(selectedBook);
                initShelf();
                tools.customSnack(ShelfActivity.this, okButton, "Livre supprimé !", "pinkshort");
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(getButtonParam());
        okButton.setLayoutParams(getButtonParam());
    }

    private void popupSearch() {
        Context ctx = ShelfActivity.this;

        // Champ texte de recherche
        EditText searchInput = new EditText(ctx);
        searchInput.setHint("Titre ou auteur...");
        searchInput.setTextColor(getColor(R.color.primary_light_brown));
        searchInput.setHintTextColor(getColor(R.color.primary_middle_brown));
        searchInput.setSingleLine(true);
        // Pré-remplir si une recherche est déjà active
        if (currentSearch != null) {
            searchInput.setText(currentSearch);
            searchInput.setSelection(currentSearch.length());
        }
        int pad = getResources().getDimensionPixelSize(R.dimen.general_margin);
        searchInput.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        inputParams.setMargins(pad, pad, pad, pad);
        searchInput.setLayoutParams(inputParams);

        Button okButton = new Button(ctx);
        okButton.setBackground(getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Filtrer");
        okButton.setTextColor(getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(ctx);
        cancelButton.setBackground(getDrawable(R.drawable.button_cancel_gradient));
        cancelButton.setText("Annuler");
        cancelButton.setTextColor(getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(ctx)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Rechercher des livres")
                .setMessage(searchInput)
                .setCancelable(false)
                .addActionButton(cancelButton)
                .addActionButton(okButton)
                .setOnShowListener(dialogInterface -> {
                })
                .setOnDismissListener(dialogInterface -> {
                })
                .setOnCancelListener(dialogInterface -> {
                });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        okButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                // Stocke la recherche et recolore le bouton en vert pour signaler l'état actif
                currentSearch = query;
                ImageView btn = findViewById(R.id.shelf_search_button);
                if (btn != null) {
                    btn.setImageTintList(
                            android.content.res.ColorStateList.valueOf(
                                    getColor(R.color.start_gradient_button_ok)));
                }
            } else {
                // Champ vide = reset du filtre texte
                currentSearch = null;
                ImageView btn = findViewById(R.id.shelf_search_button);
                if (btn != null) {
                    // Remet la couleur d'origine en supprimant le tint
                    // Remet la couleur d'origine définie dans le XML
                    btn.setImageTintList(
                            android.content.res.ColorStateList.valueOf(
                                    getColor(R.color.primary_dark_brown)));
                }
            }
            dialog.dismiss();
            // Relance initShelf avec le nouveau filtre combiné
            initShelf();
        });

        dialog.show();
        cancelButton.setLayoutParams(getButtonParam());
        okButton.setLayoutParams(getButtonParam());
    }

    @Override
    protected void onResumeCustom() throws Exception {
        checkOrientStart(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
            intent.putExtra("fromActivity", "shelfActivity");
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
                Intent intent_main = new Intent(ShelfActivity.this, MainActivity.class);
                intent_main.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent_main);
                finish();
                break;

            case Surface.ROTATION_90:
                //on est là
                break;

            case Surface.ROTATION_180:
                break;

            case Surface.ROTATION_270:
                Intent intent_observatory = new Intent(ShelfActivity.this, ObservatoryActivity.class);
                startActivity(intent_observatory);
                finish();
                break;
        }
    }
}