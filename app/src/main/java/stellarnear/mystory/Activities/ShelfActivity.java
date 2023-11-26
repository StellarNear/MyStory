package stellarnear.mystory.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import stellarnear.mystory.Activities.Fragments.MainActivityFragment;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentSearchBooks;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentWishList;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.BooksLibs.Note;
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
    private SharedPreferences settings;


    @Override
    protected void onCreateCustom() throws Exception {
        int themeId = getResources().getIdentifier("AppThemeBrown", "style", getPackageName());
        setTheme(themeId);

        setContentView(R.layout.activity_shelf);

        toolbar = findViewById(R.id.toolbar);
        window = getWindow();

        initShelf();
    }

    private void initShelf() {
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


        if (MainActivity.getShelf() != null && MainActivity.getShelf().size() > 0) {
            populatePicker();
        } else {
            findViewById(R.id.shelfPicker).setVisibility(View.GONE);
            findViewById(R.id.shelf_info_sub).setVisibility(View.GONE);
            findViewById(R.id.shelf_book_linear).setVisibility(View.GONE);
            findViewById(R.id.shelf_no_book).setVisibility(View.VISIBLE);
        }

    }

    private void populatePicker() {
        DiscreteScrollView scrollView = findViewById(R.id.shelfPicker);
        scrollView.removeAllViews();
        scrollView.setSlideOnFling(true);
        scrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());
        bookAdapter = new ListBookAdapter(MainActivity.getShelf(), scrollView, true);
        scrollView.setAdapter(bookAdapter);

        scrollView.scrollToPosition(MainActivity.getShelf().size() - 1);
        TextView infoLine1 = findViewById(R.id.shelf_book_info_line1);
        infoLine1.setVisibility(View.VISIBLE);
        TextView infoLine2 = findViewById(R.id.shelf_book_info_line2);
        infoLine2.setVisibility(View.VISIBLE);
        scrollView.addOnItemChangedListener(new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
            @Override
            public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
                selectedBook = bookAdapter.getBook(adapterPosition);

                String info1 = selectedBook.getName();

                info1 += " de " + selectedBook.getAutor().getFullName();

                if (selectedBook.getMaxPages() != null) {
                    info1 += "(" + selectedBook.getMaxPages() + " pages)";
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
                    info2 += " lu " + selectedBook.getEndTimes().size() + " fois";
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

    private void popupAddToCurrent() {
        String text = "Recommencer la lecture de " + selectedBook.getName() + " ?";

        Button okButton = new Button(ShelfActivity.this);
        okButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Oui");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.button_width_land), LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

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
                if (MainActivity.getCurrentBook() == null) {
                    selectedBook.addStartTime();
                    MainActivity.setCurrentBook(selectedBook);
                    MainActivity.removeBookFromShelf(selectedBook);
                    tools.customSnack(ShelfActivity.this, okButton, "Bonne lecture !", "brownshort");
                    initShelf();
                    dialog.dismiss();
                } else {
                    popupPutCurrentToShelf();
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
    }

    private void popupPutCurrentToShelf() {
        String text = "Tu lis actuellement " + MainActivity.getCurrentBook().getName() + " il sera mis sur l'étagère si tu veux recommencer " + selectedBook.getName();

        Button okButton = new Button(ShelfActivity.this);
        okButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Oui");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.button_width_land), LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

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
                MainActivity.putCurrentToShelf();
                selectedBook.addStartTime();
                MainActivity.setCurrentBook(selectedBook);
                MainActivity.removeBookFromShelf(selectedBook);
                tools.customSnack(ShelfActivity.this, okButton, "Bonne lecture !", "brownshort");
                initShelf();
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
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
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.button_width_land), LinearLayout.LayoutParams.WRAP_CONTENT);
        cancelButton.setLayoutParams(param);

    }

    private void addNotesToScrollView() {
        scrollviewNotes.removeAllViews();

        for (Note note : selectedBook.getNotes()) {
            View noteView = getLayoutInflater().inflate(R.layout.note, null);
            ((TextView) noteView.findViewById(R.id.note_creation_date)).setText(note.getCreationDate());
            ((TextView) noteView.findViewById(R.id.note_title)).setText(note.getTitle());
            ((TextView) noteView.findViewById(R.id.note_title)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Tools().customToast(ShelfActivity.this, note.getNote());
                }
            });
            noteView.findViewById(R.id.note_deletion).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedBook.deleteNote(note);
                    MainActivity.saveBook(selectedBook);
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
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.button_width_land), LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

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
                MainActivity.saveBook(selectedBook);
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
        okButton.setLayoutParams(param);
        cancelButton.setLayoutParams(param);
    }


    private void popupDeleteBook() {
        String text = "Le livre " + selectedBook.getName() + " sera supprimé de la bibliotheque.";

        Button okButton = new Button(ShelfActivity.this);
        okButton.setBackground(ShelfActivity.this.getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Supprimer");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.button_width_land), LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(0, getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0);

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
                MainActivity.removeBookFromShelf(selectedBook);
                initShelf();
                tools.customSnack(ShelfActivity.this, okButton, "Livre supprimé !", "pinkshort");
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
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