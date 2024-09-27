package stellarnear.mystory.Activities.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionInflater;

import com.seosh817.circularseekbar.BarStrokeCap;
import com.seosh817.circularseekbar.CircularSeekBar;
import com.seosh817.circularseekbar.CircularSeekBarAnimation;
import com.seosh817.circularseekbar.callbacks.OnProgressChangedListener;

import java.util.LinkedHashMap;
import java.util.Map;

import stellarnear.mystory.Activities.LibraryLoader;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.BooksLibs.Note;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.MyLottieDialog;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends CustomFragment {
    private View returnFragView;
    private boolean zoomedProgress = false;
    private ConstraintLayout constrainLayoutProgress;
    private ConstraintLayout constrainLayoutMainFrag;
    private FrameLayout mainCenter;
    private CircularSeekBar seekBar;

    private boolean firstSet;

    private Book book;

    private TextView movingPercent;

    private LinearLayout centerPageInfo;
    private TextView centerPageInfoPercent;
    private TextView centerPageInfoPages;

    private LinearLayout scrollviewNotes;
    private ImageView lockIcon;
    private ImageView skipStart;
    private ImageView skipEnd;
    private boolean locked = true;
    private final Tools tools = new Tools();


    public MainActivityFragment() {
    }

    @Override
    public View onCreateViewCustom(final LayoutInflater inflater, final ViewGroup container,
                                   Bundle savedInstanceState) {

        if (LibraryLoader.getLibrary() == null) {
            LibraryLoader.loadLibrary(getContext());
        }

        int themeId = getResources().getIdentifier("AppThemePurple", "style", getActivity().getPackageName());
        getActivity().setTheme(themeId);

        if (container != null) {
            container.removeAllViews();
        }

        unlockOrient();

        returnFragView = inflater.inflate(R.layout.fragment_main, container, false);

        setScreen();

        returnFragView.post(new Runnable() {
            @Override
            public void run() {
                checkPrize();
            }
        });

        return returnFragView;
    }


    public void setCustomExitTransition(int anim, Context applicationContext) {
        TransitionInflater inflaterTrannsi = TransitionInflater.from(applicationContext);
        setExitTransition(inflaterTrannsi.inflateTransition(anim));
    }


    public void setCustomEnterTransition(int anim, Context applicationContext) {
        TransitionInflater inflaterTrannsi = TransitionInflater.from(applicationContext);
        setEnterTransition(inflaterTrannsi.inflateTransition(anim));
    }

    public void setScreen() {
        book = LibraryLoader.getCurrentBook();
        mainCenter = returnFragView.findViewById(R.id.mainframe_center_for_progress);
        if (book != null) {

            constrainLayoutProgress = returnFragView.findViewById(R.id.mainfrag_center);
            constrainLayoutMainFrag = returnFragView.findViewById(R.id.fragment_main);
            mainCenter.removeAllViews();


            seekBar = createProgress();
            mainCenter.addView(seekBar);
            seekBar.setProgress(book.getCurrentPercent());

            ((TextView) returnFragView.findViewById(R.id.mainfram_title)).setText(book.getName());

            returnFragView.findViewById(R.id.mainfram_title).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popUpSummary();
                }
            });

            returnFragView.findViewById(R.id.mainfram_title).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    popupSelectMaxPage();
                    return false;
                }
            });

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
                            LibraryLoader.saveBook(book);
                            LibraryLoader.saveCurrent();
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


    private void popupSelectMaxPage() {

        LayoutInflater inflater = getLayoutInflater();

        View alert = inflater.inflate(R.layout.my_lottie_alert, null);

        View alertInnerInfo = inflater.inflate(R.layout.inner_alert_infos, null);

        ((TextView) alertInnerInfo.findViewById(R.id.alert_title_info)).setText(book.getName());
        ((TextView) alertInnerInfo.findViewById(R.id.alert_author_info)).setText(book.getAutor().getFullName());
        alertInnerInfo.findViewById(R.id.radio_page_other_prompt).setVisibility(View.VISIBLE);
        alertInnerInfo.findViewById(R.id.radio_page_group).setVisibility(View.GONE);

        Button okButton = new Button(getContext());
        okButton.setBackground(getContext().getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Valider");
        LinearLayout.LayoutParams paramInner = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = getResources().getDimensionPixelSize(R.dimen.general_margin);
        paramInner.setMargins(margin, margin, margin, margin);
        alertInnerInfo.setLayoutParams(paramInner);

        okButton.setTextColor(getContext().getColor(R.color.end_gradient_button_ok));


        Button cancelButton = new Button(getContext());
        cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Annuler");
        cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(getActivity(), alert)
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
                try {
                    EditText valuePage = alertInnerInfo.findViewById(R.id.radio_page_other_prompt);
                    Integer page = Integer.parseInt(valuePage.getText().toString());
                    book.setMaxPages(page);
                    tools.customSnack(getContext(), okButton, "Nombre de pages mis à jour à " + page + " !", "purpleshort");
                    LibraryLoader.saveBook(book);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.err("Error when changing page size", e);
                }
                dialog.dismiss();
                setScreen();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(getButtonParam());
        okButton.setLayoutParams(getButtonParam());


    }


    private void popUpSummary() {
        if (book != null) {
            LayoutInflater inflater = getLayoutInflater();
            View alert = inflater.inflate(R.layout.my_lottie_alert, null);

            View summary = inflater.inflate(R.layout.summary_scroll, null);

            TextView sumTxt = summary.findViewById(R.id.summary_text);
            sumTxt.setText(book.getSummary());

            LinearLayout.LayoutParams paramInner = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, getContext().getResources().getDimensionPixelSize(R.dimen.scroll_wish_list_height));
            int margin = getResources().getDimensionPixelSize(R.dimen.general_margin);
            paramInner.setMargins(margin, margin, margin, margin);
            summary.setLayoutParams(paramInner);

            Button cancelButton = new Button(getContext());
            cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));
            cancelButton.setText("Fermer");
            cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

            MyLottieDialog dialog = new MyLottieDialog(getActivity(), alert)
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
            noteView.findViewById(R.id.note_title).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Tools().customToast(getContext(), note.getNote());
                }
            });
            noteView.findViewById(R.id.note_deletion).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    book.deleteNote(note);
                    LibraryLoader.saveBook(book);
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
                LibraryLoader.saveBook(book);
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

        mainCenter.removeAllViews();
        seekBar = createProgress();
        mainCenter.addView(seekBar);

        ConstraintSet cs = new ConstraintSet();
        cs.clone(constrainLayoutMainFrag);
        cs.setVerticalBias(constrainLayoutProgress.getId(), 0.5f);
        cs.applyTo(constrainLayoutMainFrag);
        seekBar.setProgress(LibraryLoader.getCurrentBook().getCurrentPercent());
    }

    private void unzoomProgress() {
        returnFragView.findViewById(R.id.mainframe_book_notes).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.mainfram_cover).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.mainframe_progress_allcenter_info).setVisibility(View.VISIBLE);

        if (movingPercent != null) {
            ((ViewGroup) movingPercent.getParent()).removeView(movingPercent);
        }
        if (lockIcon != null) {
            ((ViewGroup) lockIcon.getParent()).removeView(lockIcon);
        }
        if (skipStart != null) {
            ((ViewGroup) skipStart.getParent()).removeView(skipStart);
        }
        if (skipEnd != null) {
            ((ViewGroup) skipEnd.getParent()).removeView(skipEnd);
        }

        if (centerPageInfo != null) {
            ((ViewGroup) centerPageInfo.getParent()).removeView(centerPageInfo);
        }

        mainCenter.removeAllViews();
        seekBar = createProgress();
        mainCenter.addView(seekBar);

        ConstraintSet cs = new ConstraintSet();
        cs.clone(constrainLayoutMainFrag);
        cs.setVerticalBias(constrainLayoutProgress.getId(), 0.1f);
        cs.applyTo(constrainLayoutMainFrag);
        seekBar.setProgress(LibraryLoader.getCurrentBook().getCurrentPercent());
    }

    private void setImage(Book book) {
        byte[] b = book.getImage();
        if (b == null || b.length < 7) {
            ((ImageView) returnFragView.findViewById(R.id.mainfram_cover)).setImageDrawable(getResources().getDrawable(R.drawable.no_image));
        } else {
            Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(b, 0, b.length));
            ((ImageView) returnFragView.findViewById(R.id.mainfram_cover)).setImageDrawable(image);
        }
    }

    private CircularSeekBar createProgress() {
        CircularSeekBar seekBar = new CircularSeekBar(getContext());
        if (zoomedProgress) {
            setZoomedSeek(seekBar);
            FrameLayout.LayoutParams paramSeek = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, getContext().getResources().getDimensionPixelSize(R.dimen.maximize_progress));
            seekBar.setLayoutParams(paramSeek);
        } else {
            setUnzoomedSeek(seekBar);
            FrameLayout.LayoutParams paramSeek = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, getContext().getResources().getDimensionPixelSize(R.dimen.minimize_progress));
            seekBar.setLayoutParams(paramSeek);
        }

        seekBar.setShowAnimation(true);
        seekBar.setAnimationInterpolator(CircularSeekBarAnimation.DECELERATE.getInterpolator());
        seekBar.setDashGap(2);
        seekBar.setDashWidth(1);
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
                if (returnFragView.findViewById(R.id.mainframe_progress_text) != null) {
                    ((TextView) returnFragView.findViewById(R.id.mainframe_progress_text)).setText(((int) v) + " %");
                }

                if (returnFragView.findViewById(R.id.mainframe_progress_page_text) != null) {
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


        centerPageInfo = new LinearLayout(getContext());
        centerPageInfo.setOrientation(LinearLayout.VERTICAL);
        centerPageInfo.setId(View.generateViewId());
        centerPageInfo.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.general_margin));

        centerPageInfoPercent = new TextView(getContext());

        centerPageInfoPercent.setTextSize(42);
        centerPageInfoPercent.setText("- %");
        centerPageInfoPercent.setTextColor(getContext().getColor(R.color.primary_light_purple));
        centerPageInfoPercent.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        centerPageInfoPercent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Saisie de la progression");
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setRawInputType(Configuration.KEYBOARD_12KEY);
                alert.setView(input);
                alert.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int percent = Integer.parseInt(input.getText().toString());
                        if (percent < 0) {
                            percent = 0;
                        }
                        if (percent > 100) {
                            percent = 100;
                        }
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
        centerPageInfo.addView(centerPageInfoPercent);
        if (book.getMaxPages() != null) {
            centerPageInfoPages = new TextView(getContext());

            centerPageInfoPages.setTextSize(18);
            centerPageInfoPages.setText("-/- pages");
            centerPageInfoPages.setTextColor(getContext().getColor(R.color.primary_light_purple));
            centerPageInfoPages.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            centerPageInfoPages.setOnClickListener(new View.OnClickListener() {
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
                            int percent = (int) (100 * (1.0 * Integer.parseInt(input.getText().toString()) / (1.0 * book.getMaxPages())));
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

            centerPageInfo.addView(centerPageInfoPages);
        }

        constrainLayoutProgress.addView(centerPageInfo);
        ConstraintSet setPages = new ConstraintSet();
        setPages.clone(constrainLayoutProgress);
        setPages.connect(centerPageInfo.getId(), ConstraintSet.BOTTOM, constrainLayoutProgress.getId(), ConstraintSet.BOTTOM);
        setPages.connect(centerPageInfo.getId(), ConstraintSet.END, constrainLayoutProgress.getId(), ConstraintSet.END);
        setPages.connect(centerPageInfo.getId(), ConstraintSet.START, constrainLayoutProgress.getId(), ConstraintSet.START);
        setPages.connect(centerPageInfo.getId(), ConstraintSet.TOP, constrainLayoutProgress.getId(), ConstraintSet.TOP);
        setPages.applyTo(constrainLayoutProgress);

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
                locked = true;

                skipStart = new ImageView(getContext());
                skipStart.setId(View.generateViewId());
                skipStart.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_skip_previous_24));
                constrainLayoutProgress.addView(skipStart);

                skipEnd = new ImageView(getContext());
                skipEnd.setId(View.generateViewId());
                skipEnd.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_skip_next_24));
                constrainLayoutProgress.addView(skipEnd);

                int width = constrainLayoutProgress.getMeasuredWidth();
                int height = constrainLayoutProgress.getMeasuredHeight();
                ConstraintSet set = new ConstraintSet();
                set.clone(constrainLayoutProgress);


                firstSet = true;
                seekBar.setOnProgressChangedListener(new OnProgressChangedListener() {
                    @Override
                    public void onProgressChanged(float v) {
                        if (centerPageInfoPercent != null) {
                            centerPageInfoPercent.setText((int) v + " %");
                        }
                        if (centerPageInfoPages != null) {
                            int page = (int) ((1.0 * (int) v * book.getMaxPages()) / 100.0);
                            centerPageInfoPages.setText(page + "/" + book.getMaxPages() + " pages");
                        }
                        movingPercent.setText((int) v + " %");

                        int x = (int) ((width / 2) + 470 * Math.cos(((135 + 270 * (v / 100)) / 180) * Math.PI)) - 25;
                        int y = (int) ((height / 2) + 470 * Math.sin(((135 + 270 * (v / 100)) / 180) * Math.PI)) - 25;

                        set.connect(movingPercent.getId(), ConstraintSet.START, constrainLayoutProgress.getId(), ConstraintSet.START, x);
                        set.connect(movingPercent.getId(), ConstraintSet.TOP, constrainLayoutProgress.getId(), ConstraintSet.TOP, y);
                        set.applyTo(constrainLayoutProgress);


                        //le premier setup va nous mener à la valeur actuelle du coup apres ca set firstSet à false
                        if (firstSet && v < book.getCurrentPercent()) {
                            return;
                        }
                        firstSet = false;

                        if (locked && v < book.getCurrentPercent()) {
                            seekBar.setProgress(book.getCurrentPercent());
                        }
                    }
                });


                int xLock = (int) ((width / 2) + 305 * Math.cos(((135 + 270 * (1.0 * book.getCurrentPercent() / 100)) / 180) * Math.PI)) - 25;
                int yLock = (int) ((height / 2) + 305 * Math.sin(((135 + 270 * (1.0 * book.getCurrentPercent() / 100)) / 180) * Math.PI)) - 25;

                set.connect(lockIcon.getId(), ConstraintSet.START, constrainLayoutProgress.getId(), ConstraintSet.START, xLock);
                set.connect(lockIcon.getId(), ConstraintSet.TOP, constrainLayoutProgress.getId(), ConstraintSet.TOP, yLock);

                set.connect(skipStart.getId(), ConstraintSet.START, constrainLayoutProgress.getId(), ConstraintSet.START, 20);
                set.connect(skipStart.getId(), ConstraintSet.BOTTOM, constrainLayoutProgress.getId(), ConstraintSet.BOTTOM, 20);

                set.connect(skipEnd.getId(), ConstraintSet.END, constrainLayoutProgress.getId(), ConstraintSet.END, 20);
                set.connect(skipEnd.getId(), ConstraintSet.BOTTOM, constrainLayoutProgress.getId(), ConstraintSet.BOTTOM, 20);

                set.applyTo(constrainLayoutProgress);

                lockIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (locked) {
                            lockIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_lock_open_24));
                            locked = false;
                        } else {
                            lockIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_lock_24));
                            locked = true;
                        }
                    }
                });

                skipStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        seekBar.setProgress(0);
                    }
                });
                skipEnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        seekBar.setProgress(100);
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
                LibraryLoader.endBookAndPutToShelf();
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
                LibraryLoader.deleteCurrent();
                setScreen();
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
    }

    private void checkPrize() {
        int streak = LibraryLoader.getAccessStats().getnStreak();

        Map<Integer, String> daysPrize = new LinkedHashMap<>();
        daysPrize.put(7, "un bisou (utilisable n'importe quand)");
        daysPrize.put(15, "un massage du dos");
        daysPrize.put(30, "un massage intégrale (oui avec pieds aussi !)");
        daysPrize.put(60, "un repas au restaurant en amoureux");
        daysPrize.put(90, "un curry japonais du doudou");
        daysPrize.put(120, "le cours d'oenologie (enfin...)");
        daysPrize.put(150, "une nouvelle smartwatch !");
        daysPrize.put(180, "une journée au spa en amoureux");

        if (daysPrize.containsKey(streak) && !(LibraryLoader.getAccessStats().getLastGrantedStreakReward() == streak)) {
            // if (true) {
            int nextReward = Integer.MAX_VALUE;
            for (Integer day : daysPrize.keySet()) {
                if (day > streak && day < nextReward) {
                    nextReward = day;
                }
            }
            popupGift(streak, daysPrize.get(streak), "Prochaine récompense à " + nextReward + " jours");

            LibraryLoader.getAccessStats().addGiftToclaim(daysPrize.get(streak));
            LibraryLoader.getAccessStats().setLastGrantedStreakReward(streak);
            LibraryLoader.saveAccessStats();
        }
    }

    private void popupGift(int nDay, String cadeau, String next) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View alert = inflater.inflate(R.layout.my_lottie_alert, null);
        View alertInnerInfo = inflater.inflate(R.layout.inner_gift, null);

        ((TextView) alertInnerInfo.findViewById(R.id.alert_first_line)).setText(cadeau);
        ((TextView) alertInnerInfo.findViewById(R.id.alert_second_line)).setText(next);

        MyLottieDialog dialog = new MyLottieDialog(getContext(), alert)
                .setAnimation(R.raw.gift)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Tu as recu un cadeau ! (" + nDay + " j)")
                .setMessage(alertInnerInfo)
                .setCanceledOnTouchOutside(true)
                .setCancelOnTouchItself(true)
                .setOnShowListener(dialogInterface -> {
                })
                .setOnDismissListener(dialogInterface -> {
                })
                .setOnCancelListener(dialogInterface -> {
                });

        dialog.show();

    }

    private void unlockOrient() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    private void lockOrient() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    public boolean isZoomedProgress() {
        return zoomedProgress;
    }

}
