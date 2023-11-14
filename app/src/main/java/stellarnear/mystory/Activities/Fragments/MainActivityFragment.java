package stellarnear.mystory.Activities.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import com.seosh817.circularseekbar.BarStrokeCap;
import com.seosh817.circularseekbar.CircularSeekBar;
import com.seosh817.circularseekbar.CircularSeekBarAnimation;
import com.seosh817.circularseekbar.callbacks.OnProgressChangedListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.BooksLibs.Note;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.MyLottieDialog;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private View returnFragView;
    private boolean zoomedProgress = false;
    private ConstraintLayout constrainLayoutProgress;
    private ConstraintLayout constrainLayoutMainFrag;
    private FrameLayout mainCenter;
    private CircularSeekBar seekBar;

    private boolean lockRefreshOnChange = false;
    private boolean firstSet;
    private Handler handler;
    private Book book;

    private TextView movingPercent;
    private TextView centerPageInfo;

    private LinearLayout scrollviewNotes;
    private ImageView lockIcon;
    private boolean locked=true;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        int themeId = getResources().getIdentifier("AppThemePurple", "style", getActivity().getPackageName());
        getActivity().setTheme(themeId);
        super.onCreate(savedInstanceState);
        if (container != null) {
            container.removeAllViews();
        }

        unlockOrient();

        returnFragView = inflater.inflate(R.layout.fragment_main, container, false);

        setScreen();

        return returnFragView;
    }

    public void setScreen() {
        book = MainActivity.getCurrentBook();
        if (book != null) {
            mainCenter = returnFragView.findViewById(R.id.mainframe_center_for_progress);
            constrainLayoutProgress = (ConstraintLayout) returnFragView.findViewById(R.id.mainfrag_center);
            constrainLayoutMainFrag = (ConstraintLayout) returnFragView.findViewById(R.id.fragment_main);
            mainCenter.removeAllViews();


            seekBar = createProgress();
            mainCenter.addView(seekBar);
            seekBar.setProgress(book.getCurrentPercent());

            ((TextView) returnFragView.findViewById(R.id.mainfram_title)).setText(book.getName());
            ((TextView) returnFragView.findViewById(R.id.mainfram_author)).setText(book.getAutor().getFullName());

            setImage(book);

            returnFragView.findViewById(R.id.mainframe_book_open).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!zoomedProgress) {
                        zoomedProgress = true;
                        zoomProgress();

                    } else {
                        zoomedProgress = false;
                        if (seekBar != null) {
                            book.setLastRead();
                            book.setCurrentPercent((int) seekBar.getProgress());
                            if (seekBar.getProgress() == 100) {
                                popupEndBookPutOnShelf();
                            }
                            MainActivity.saveLibrary();
                        }
                        unzoomProgress();
                    }
                }
            });

            returnFragView.findViewById(R.id.mainframe_book_notes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupDiplayNotes();
                }
            });

        } else {
            returnFragView.findViewById(R.id.mainframe_book_notes).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.mainfrag_info_linear).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.mainfrag_center).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.mainframe_no_current).setVisibility(View.VISIBLE);
        }


    }

    private void popupDiplayNotes() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
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

        Button cancelButton = new Button(getContext());
        cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Fermer");
        cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(getContext(), alert)
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
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cancelButton.setLayoutParams(param);
    }

    private void addNotesToScrollView() {
        scrollviewNotes.removeAllViews();

            for (Note note : book.getNotes()) {
                View noteView = getActivity().getLayoutInflater().inflate(R.layout.note, null);
                ((TextView) noteView.findViewById(R.id.note_creation_date)).setText(note.getCreationDate());
                ((TextView) noteView.findViewById(R.id.note_title)).setText(note.getTitle());
                ((TextView) noteView.findViewById(R.id.note_title)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Tools().customToast(getContext(), note.getNote());
                    }
                });
                noteView.findViewById(R.id.note_deletion).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        book.deleteNote(note);
                        MainActivity.saveLibrary();
                        addNotesToScrollView();
                    }
                });
                scrollviewNotes.addView(noteView);
            }
    }

    private void popupCreateNote() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View alert = inflater.inflate(R.layout.my_lottie_alert, null);
        View notes = inflater.inflate(R.layout.create_note, null);

        Button okButton = new Button(getContext());
        okButton.setBackground(getContext().getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Créer");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

        okButton.setTextColor(getContext().getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(getContext());
        cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Fermer");
        cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(getContext(), alert)
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
                book.createNote(title, note);
                MainActivity.saveLibrary();
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


    private void zoomProgress() {
        returnFragView.findViewById(R.id.mainframe_book_notes).setVisibility(View.GONE);
        returnFragView.findViewById(R.id.mainfram_cover).setVisibility(View.GONE);
        returnFragView.findViewById(R.id.mainframe_progress_allcenter_info).setVisibility(View.GONE);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) constrainLayoutProgress.getLayoutParams();
        params.verticalBias = 0.5f;
        constrainLayoutProgress.setLayoutParams(params);

        mainCenter.removeAllViews();
        seekBar = createProgress();

        FrameLayout.LayoutParams paramSeek = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, getContext().getResources().getDimensionPixelSize(R.dimen.maximize_progress));
        seekBar.setLayoutParams(paramSeek);

        mainCenter.addView(seekBar);

        seekBar.setProgress(MainActivity.getCurrentBook().getCurrentPercent());
    }

    private void unzoomProgress() {
        returnFragView.findViewById(R.id.mainframe_book_notes).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.mainfram_cover).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.mainframe_progress_allcenter_info).setVisibility(View.VISIBLE);
        if (movingPercent != null) {
            ((ViewGroup) movingPercent.getParent()).removeView(movingPercent);
        }
        if(lockIcon != null){
            ((ViewGroup) lockIcon.getParent()).removeView(lockIcon);
        }
        if (centerPageInfo != null) {
            ((ViewGroup) centerPageInfo.getParent()).removeView(centerPageInfo);
        }


        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) constrainLayoutProgress.getLayoutParams();
        params.verticalBias = 0.1f;
        constrainLayoutProgress.setLayoutParams(params);

        mainCenter.removeAllViews();
        seekBar = createProgress();

        FrameLayout.LayoutParams paramSeek = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, getContext().getResources().getDimensionPixelSize(R.dimen.minimize_progress));
        seekBar.setLayoutParams(paramSeek);
        mainCenter.addView(seekBar);


        seekBar.setProgress(MainActivity.getCurrentBook().getCurrentPercent());


    }

    private void setImage(Book book) {
        byte[] b = book.getImage();
        if (b == null || b.length < 7) {
            new BookNodeCalls().refreshImage(book);
            book.setOnImageRefreshedEventListener(new Book.OnImageRefreshedEventListener() {
                @Override
                public void onEvent() {
                    setImage(book);
                }
            });
            String file = "res/raw/no_image.png";
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(file)) {
                int nRead;
                byte[] dataBytes = new byte[16384];
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                while ((nRead = in.read(dataBytes, 0, dataBytes.length)) != -1) {
                    buffer.write(dataBytes, 0, nRead);
                }
                byte[] bNoimg = buffer.toByteArray();
                Drawable noImage = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bNoimg, 0, bNoimg.length));
                ((ImageView) returnFragView.findViewById(R.id.mainfram_cover)).setImageDrawable(noImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(b, 0, b.length));
            ((ImageView) returnFragView.findViewById(R.id.mainfram_cover)).setImageDrawable(image);
        }
    }

    private CircularSeekBar createProgress() {
        CircularSeekBar seekBar = new CircularSeekBar(getContext());

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, getContext().getResources().getDimensionPixelSize(R.dimen.minimize_progress));
        seekBar.setLayoutParams(param);
        seekBar.setShowAnimation(true);
        seekBar.setAnimationInterpolator(CircularSeekBarAnimation.DECELERATE.getInterpolator());

        seekBar.setDashGap(2);
        seekBar.setDashWidth(1);
        if (zoomedProgress) {
            setZoomedSeek(seekBar);
        } else {
            setUnzoomedSeek(seekBar);
        }

        seekBar.setBarStrokeCap(BarStrokeCap.BUTT);
        seekBar.setInnerThumbColor(getContext().getColor(R.color.primary_dark_purple));
        seekBar.setOuterThumbColor(getContext().getColor(R.color.primary_middle_purple));
        seekBar.setProgressGradientColorsArray(getContext().getResources().getIntArray(R.array.rainbow));
        seekBar.setStartAngle(45);
        seekBar.setSweepAngle(270);
        return seekBar;
    }

    private void setUnzoomedSeek(CircularSeekBar seekBar) {
        seekBar.setAnimationDurationMillis(3000);
        seekBar.setBarWidth(20);
        seekBar.setInteractive(false);
        seekBar.setInnerThumbRadius(getContext().getResources().getDimension(R.dimen.progress_inner_radius));
        seekBar.setInnerThumbStrokeWidth(getContext().getResources().getDimension(R.dimen.progress_inner_stroke));
        seekBar.setOuterThumbRadius(getContext().getResources().getDimension(R.dimen.progress_outer_radius));
        seekBar.setOuterThumbStrokeWidth(getContext().getResources().getDimension(R.dimen.progress_outer_stroke));

        seekBar.setOnProgressChangedListener(new OnProgressChangedListener() {
            @Override
            public void onProgressChanged(float v) {
                if (((TextView) returnFragView.findViewById(R.id.mainframe_progress_text)) != null) {
                    ((TextView) returnFragView.findViewById(R.id.mainframe_progress_text)).setText(((int) v) + " %");
                }

                if (((TextView) returnFragView.findViewById(R.id.mainframe_progress_page_text)) != null) {
                    if (book != null && book.getMaxPages() != null && book.getMaxPages() > 0) {
                        ((TextView) returnFragView.findViewById(R.id.mainframe_progress_page_text)).setText("(" + book.getCurrentPage() + "/" + book.getMaxPages() + " pages)");
                    } else {
                        returnFragView.findViewById(R.id.mainframe_progress_page_text).setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void setZoomedSeek(CircularSeekBar seekBar) {
        seekBar.setAnimationDurationMillis(500);
        seekBar.setBarWidth(60);
        seekBar.setInteractive(true);
        seekBar.setInnerThumbRadius(getContext().getResources().getDimension(R.dimen.progress_inner_radius_zoomed));
        seekBar.setInnerThumbStrokeWidth(getContext().getResources().getDimension(R.dimen.progress_inner_stroke_zoomed));
        seekBar.setOuterThumbRadius(getContext().getResources().getDimension(R.dimen.progress_outer_radius_zoomed));
        seekBar.setOuterThumbStrokeWidth(getContext().getResources().getDimension(R.dimen.progress_outer_stroke_zoomed));


        if (book.getMaxPages() != null) {
            centerPageInfo = new TextView(getContext());
            centerPageInfo.setId(View.generateViewId());
            centerPageInfo.setTextSize(18);
            centerPageInfo.setText("-/- pages");
            centerPageInfo.setTextColor(getContext().getColor(R.color.primary_light_purple));
            centerPageInfo.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            centerPageInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle("Saisie de la page");
                    final EditText input = new EditText(getContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setRawInputType(Configuration.KEYBOARD_12KEY);
                    alert.setView(input);
                    alert.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            int percent = (int) ((int) 100*(1.0*Integer.parseInt(input.getText().toString())/(1.0*book.getMaxPages())));
                            seekBar.setProgress(percent);
                        }
                    });
                    alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });
                    alert.show();
                }
            });

            constrainLayoutProgress.addView(centerPageInfo);
            ConstraintSet setPages = new ConstraintSet();
            setPages.clone(constrainLayoutProgress);
            setPages.connect(centerPageInfo.getId(), ConstraintSet.BOTTOM, constrainLayoutProgress.getId(), ConstraintSet.BOTTOM);
            setPages.connect(centerPageInfo.getId(), ConstraintSet.END, constrainLayoutProgress.getId(), ConstraintSet.END);
            setPages.connect(centerPageInfo.getId(), ConstraintSet.START, constrainLayoutProgress.getId(), ConstraintSet.START);
            setPages.connect(centerPageInfo.getId(), ConstraintSet.TOP, constrainLayoutProgress.getId(), ConstraintSet.TOP);
            setPages.applyTo(constrainLayoutProgress);
        }
        seekBar.post(new Runnable() {
            @Override
            public void run() {
                movingPercent = new TextView(getContext());
                movingPercent.setTextColor(getContext().getColor(R.color.primary_light_purple));
                movingPercent.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                movingPercent.setGravity(View.TEXT_ALIGNMENT_CENTER);
                movingPercent.setId(View.generateViewId());
                constrainLayoutProgress.addView(movingPercent);



                lockIcon = new ImageView(getContext());
                lockIcon.setId(View.generateViewId());
                lockIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_lock_24));
                constrainLayoutProgress.addView(lockIcon);

                int width = constrainLayoutProgress.getMeasuredWidth();
                int height = constrainLayoutProgress.getMeasuredHeight();
                ConstraintSet set = new ConstraintSet();
                set.clone(constrainLayoutProgress);

                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        lockRefreshOnChange = false;
                        handler.postDelayed(this, 1000);
                    }
                }, 1000);

                firstSet = true;
                seekBar.setOnProgressChangedListener(new OnProgressChangedListener() {
                    @Override
                    public void onProgressChanged(float v) {
                        if (centerPageInfo != null) {
                            int page = (int) ((1.0 * (int) v * book.getMaxPages()) / 100.0);
                            centerPageInfo.setText(page + "/" + book.getMaxPages() + " pages");
                        }
                        movingPercent.setText((int) v + " %");

                        int x = (int) ((width / 2) + 470 * Math.cos(((135 + 270 * (v / 100)) / 180) * Math.PI)) - 25;
                        int y = (int) ((height / 2) + 470 * Math.sin(((135 + 270 * (v / 100)) / 180) * Math.PI)) - 25;

                        set.connect(movingPercent.getId(), ConstraintSet.START, constrainLayoutProgress.getId(), ConstraintSet.START, x);
                        set.connect(movingPercent.getId(), ConstraintSet.TOP, constrainLayoutProgress.getId(), ConstraintSet.TOP, y);
                        set.applyTo(constrainLayoutProgress);

                        if (firstSet && (v ==0)) {
                            return;
                        }
                        firstSet = false;

                        if ((int) v <= 5 && v != 0 && !lockRefreshOnChange) {
                            MainActivityFragment.this.seekBar.setProgress(0);
                            lockRefreshOnChange = true;
                            return;
                        }
                        if ((int) v >= 95 && v != 100 && !lockRefreshOnChange) {
                            MainActivityFragment.this.seekBar.setProgress(100);
                            lockRefreshOnChange = true;
                            return;
                        }
                    }
                });


                int xLock = (int) ((width / 2) + 305 * Math.cos(((135 + 270 * (1.0*book.getCurrentPercent() / 100)) / 180) * Math.PI)) - 25;
                int yLock = (int) ((height / 2) + 305 * Math.sin(((135 + 270 * (1.0*book.getCurrentPercent()  / 100)) / 180) * Math.PI)) - 25;

                set.connect(lockIcon.getId(), ConstraintSet.START, constrainLayoutProgress.getId(), ConstraintSet.START, xLock);
                set.connect(lockIcon.getId(), ConstraintSet.TOP, constrainLayoutProgress.getId(), ConstraintSet.TOP, yLock);
                set.applyTo(constrainLayoutProgress);

                lockIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(locked){
                            lockIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_lock_open_24));
                            locked=false;
                        } else {
                            lockIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_lock_24));
                            locked=true;
                        }
                    }
                });


            }
        });
    }

    private void popupEndBookPutOnShelf() {
        String text = "Bravo tu as fini " + book.getName() + " !\nIl va être mit sur l'étagère. Si tu ne souhaites pas le conserver tu peux le supprimer.";

        Button okButton = new Button(getContext());
        okButton.setBackground(getContext().getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Etagère");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

        okButton.setTextColor(getContext().getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(getContext());
        cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Supprimer");
        cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(getContext())
                .setAnimation(R.raw.add_shelf)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Fin de lecture")
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
                popupDeleteBook();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.endBookAndPutToShelf();
                setScreen();
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
    }

    private void popupDeleteBook() {
        String text = "Le livre " + book.getName() + " sera supprimé de la bibliotheque.";

        Button okButton = new Button(getContext());
        okButton.setBackground(getContext().getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Supprimer");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

        okButton.setTextColor(getContext().getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(getContext());
        cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Annuler");
        cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(getContext())
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
                MainActivity.deleteCurrent();
                setScreen();
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
    }


    private void unlockOrient() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    private void lockOrient() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

}
