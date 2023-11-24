package stellarnear.mystory.Activities.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.DatePickerFragment;
import stellarnear.mystory.UITools.ListBookAdapter;
import stellarnear.mystory.UITools.MyLottieDialog;


public class MainActivityFragmentSearchBooks extends CustomFragment {

    private View returnFragView;

    private final Tools tools = Tools.getTools();
    private ListBookAdapter bookAdapter;
    private boolean resultShown = false;
    private Book selectedBook;
    private ImageButton backButton;
    private EditText titlePrompt;
    private EditText authorPrompt;

    public MainActivityFragmentSearchBooks() {
    }

    private OnFramentViewCreatedEventListener mLoadedListner;

    public void setOnFramentViewCreatedEventListener(OnFramentViewCreatedEventListener eventListener) {
        mLoadedListner = eventListener;
    }

    public interface OnFramentViewCreatedEventListener {
        void onEvent();
    }


    private OnSearchedEventListener mSearchedListner;

    public void setOnSearchedEventListener(OnSearchedEventListener eventListener) {
        mSearchedListner = eventListener;
    }

    public interface OnSearchedEventListener {
        void onEvent();
    }

    @Override
    public View onCreateViewCustom(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        TransitionInflater inflaterTrannsi = TransitionInflater.from(requireContext());
        setExitTransition(inflaterTrannsi.inflateTransition(R.transition.slide_left));
        setEnterTransition(inflaterTrannsi.inflateTransition(R.transition.slide_right));
        int themeId = getResources().getIdentifier("AppThemeYellow", "style", getActivity().getPackageName());
        getActivity().setTheme(themeId);

        super.onCreate(savedInstanceState);
        if (container != null) {
            container.removeAllViews();
        }

        returnFragView = inflater.inflate(R.layout.fragment_main_search_books, container, false);

        backButton = (ImageButton) returnFragView.findViewById(R.id.back_main_from_search);

        Animation left = AnimationUtils.loadAnimation(getContext(), R.anim.infromleft);

        left.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animate(backButton);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        backButton.startAnimation(left);


        if (mLoadedListner != null) {
            mLoadedListner.onEvent();
        }

        titlePrompt = returnFragView.findViewById(R.id.search_title_prompt);
        titlePrompt.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        titlePrompt.setRawInputType(InputType.TYPE_CLASS_TEXT);

        authorPrompt = returnFragView.findViewById(R.id.search_author_prompt);
        authorPrompt.setImeOptions(EditorInfo.IME_ACTION_SEND);
        authorPrompt.setRawInputType(InputType.TYPE_CLASS_TEXT);

        authorPrompt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    startSearch();
                    handled = true;
                }
                return handled;
            }
        });

        return returnFragView;
    }

    public boolean hasResultShown() {
        return this.resultShown;
    }

    public void clearResult() {
        if (bookAdapter != null) {
            bookAdapter.reset();
        }

        returnFragView.findViewById(R.id.add_book_linear).setVisibility(View.GONE);
        returnFragView.findViewById(R.id.linearBooksFoundInfosSub).setVisibility(View.GONE);
        returnFragView.findViewById(R.id.pickerScroller).setVisibility(View.GONE);
        returnFragView.findViewById(R.id.loading_search).setVisibility(View.GONE);

        returnFragView.findViewById(R.id.linearSearchPrompt).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.search_title_prompt).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.search_author_prompt).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.loading_search).clearAnimation();
        returnFragView.findViewById(R.id.loading_search).setVisibility(View.GONE);
        resultShown = false;
    }

    public void clearAnimation() {
        if (backButton != null) {
            backButton.clearAnimation();
            ((ViewGroup) backButton.getParent()).removeView(backButton);
        }
    }

    public void startSearch() {
        String search = titlePrompt.getText().toString().trim() + " " + authorPrompt.getText().toString().trim();
        if (search.trim().length() < 3) {
            tools.customToast(getContext(), "Entres au moins un titre ou un auteur");
            return;
        }

        BookNodeCalls bookCall = new BookNodeCalls();
        final List<Book> booksList = new ArrayList<>();
        bookCall.setOnDataRecievedEventListener(new BookNodeCalls.OnDataRecievedEventListener() {
            @Override
            public void onEvent(List<Book> allBooks) {
                tools.customSnack(getContext(), returnFragView, allBooks.size() + " livres trouvés", "yellow");
                booksList.addAll(allBooks);
                displayListBookToPick(booksList);
            }
        });

        try {
            bookCall.getBooksFromSearch(search);
        } catch (Exception e) {
            tools.customToast(getContext(), "Erreur lors de la recherche : " + e.getLocalizedMessage());
        }

        if (mSearchedListner != null) {
            mSearchedListner.onEvent();
            resultShown = true;
        }

        authorPrompt.setVisibility(View.GONE);
        titlePrompt.setVisibility(View.GONE);
        returnFragView.findViewById(R.id.loading_search).setVisibility(View.VISIBLE);

        ScaleAnimation mAnimation = new ScaleAnimation(1f, 1.25f, 1f, 1.25f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mAnimation.setDuration(1000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new AccelerateInterpolator());
        returnFragView.findViewById(R.id.loading_search).startAnimation(mAnimation);
    }

    private void displayListBookToPick(List<Book> booksList) {
        returnFragView.findViewById(R.id.linearSearchPrompt).setVisibility(View.GONE);
        returnFragView.findViewById(R.id.linearBooksFoundInfosSub).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.pickerScroller).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.add_book_linear).setVisibility(View.VISIBLE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sharedPreferences.getBoolean("switch_direct_add_to_shelf", getContext().getResources().getBoolean(R.bool.switch_direct_add_to_shelf_def))) {
            returnFragView.findViewById(R.id.add_to_shelf).setVisibility(View.VISIBLE);
            returnFragView.findViewById(R.id.add_to_shelf).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupAddToShelf();
                }
            });
        } else {
            returnFragView.findViewById(R.id.add_to_shelf).setVisibility(View.GONE);
        }

        DiscreteScrollView scrollView = returnFragView.findViewById(R.id.pickerScroller);
        scrollView.setSlideOnFling(true);
        scrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());
        bookAdapter = new ListBookAdapter(booksList, scrollView);
        scrollView.setAdapter(bookAdapter);

        Book bookZero = bookAdapter.getBook(0);
        TextView title = returnFragView.findViewById(R.id.list_book_title);
        title.setVisibility(View.VISIBLE);
        title.setText(bookZero.getName());

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpSummary();
            }
        });

        TextView author = returnFragView.findViewById(R.id.list_book_author);
        author.setVisibility(View.VISIBLE);
        author.setText(bookZero.getAutor().getFullName());
        TextView pages = returnFragView.findViewById(R.id.list_book_page_count);
        pages.setVisibility(View.VISIBLE);
        pages.setText(bookZero.getMeanPagesFoundTxt());

        bookZero.setOnPageDataRecievedEventListener(new Book.OnPageDataRecievedEventListener() {
            @Override
            public void onEvent() {
                pages.setText(bookZero.getMeanPagesFoundTxt());
            }
        });

        scrollView.addOnItemChangedListener(new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
            @Override
            public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
                selectedBook = bookAdapter.getBook(adapterPosition);
                title.setText(selectedBook.getName());
                author.setText(selectedBook.getAutor().getFullName());
                pages.setText(selectedBook.getMeanPagesFoundTxt());
                selectedBook.setOnPageDataRecievedEventListener(new Book.OnPageDataRecievedEventListener() {
                    @Override
                    public void onEvent() {
                        pages.setText(selectedBook.getMeanPagesFoundTxt());
                    }
                });
            }
        });

        returnFragView.findViewById(R.id.add_to_wishlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupAddToWishList();
            }
        });

        returnFragView.findViewById(R.id.add_to_current).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupAddToCurrent();
            }
        });

        returnFragView.findViewById(R.id.add_to_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupAddToDownload();
            }
        });
    }

    private void popupAddToShelf() {

        String text = "Veux tu mettre " + selectedBook.getName() + " directement sur l'étagère ?";

        Button okButton = new Button(getContext());
        okButton.setBackground(getContext().getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Etagère");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

        okButton.setTextColor(getContext().getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(getContext());
        cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Annuler");
        cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(getContext())
                .setAnimation(R.raw.add_shelf)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Mise sur l'étagère")
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
                MainActivity.addBookToShelf(selectedBook);
                tools.customSnack(getContext(), returnFragView, "Livre ajouté à l'étagère !", "yellowshort");
                DatePickerFragment datePickerFragment = new DatePickerFragment(selectedBook);
                datePickerFragment.show(getParentFragmentManager(), "datePicker");
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);


    }

    private void popUpSummary() {
        if (selectedBook != null) {
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View alert = inflater.inflate(R.layout.my_lottie_alert, null);

            View summary = inflater.inflate(R.layout.summary_scroll, null);

            TextView sumTxt = summary.findViewById(R.id.summary_text);
            sumTxt.setText(selectedBook.getSummary());

            LinearLayout.LayoutParams paramInner = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, getContext().getResources().getDimensionPixelSize(R.dimen.scroll_wish_list_height));
            int margin = getResources().getDimensionPixelSize(R.dimen.general_margin);
            paramInner.setMargins(margin, margin, margin, margin);
            summary.setLayoutParams(paramInner);

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            Button cancelButton = new Button(getContext());
            cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));
            cancelButton.setText("Fermer");
            cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

            MyLottieDialog dialog = new MyLottieDialog(getContext(), alert)
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
            cancelButton.setLayoutParams(param);
        }
    }

    private void popupAddToWishList() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View alert = inflater.inflate(R.layout.my_lottie_alert, null);

        View alertInnerInfo = inflater.inflate(R.layout.inner_alert_infos, null);

        ((TextView) alertInnerInfo.findViewById(R.id.alert_title_info)).setText(selectedBook.getName());
        ((TextView) alertInnerInfo.findViewById(R.id.alert_author_info)).setText(selectedBook.getAutor().getFullName());

        RadioGroup radioGroup = (RadioGroup) alertInnerInfo.findViewById(R.id.radio_page_group);

        RadioButton pageOtherRadio = alertInnerInfo.findViewById(R.id.radio_page_other);
        pageOtherRadio.findViewById(R.id.radio_page_other).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertInnerInfo.findViewById(R.id.radio_page_other_prompt).setVisibility(View.VISIBLE);
            }
        });
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_enabled} //enabled
                },
                new int[]{getContext().getColor(R.color.primary_light_yellow)}
        );
        for (int i : selectedBook.getPagesFounds()) {
            RadioButton button = new RadioButton(getContext());
            button.setTextColor(getContext().getColor(R.color.primary_light_yellow));
            button.setButtonTintList(colorStateList);
            button.setText(i + " pages");
            radioGroup.addView(button, 0);
        }

        Button okButton = new Button(getContext());
        okButton.setBackground(getContext().getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Oui");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

        okButton.setTextColor(getContext().getColor(R.color.end_gradient_button_ok));


        Button cancelButton = new Button(getContext());
        cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Non");
        cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(getContext(), alert)
                .setAnimation(R.raw.add_wish_list)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Souhaites tu ajouter ce livre à ta liste d'envie ?")
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
                if (radioGroup.getCheckedRadioButtonId() != -1) {
                    //on check si other a été selectionner
                    if (radioGroup.getCheckedRadioButtonId() == pageOtherRadio.getId()) {
                        try {
                            EditText valuePage = (EditText) alertInnerInfo.findViewById(R.id.radio_page_other_prompt);
                            Integer page = Integer.parseInt(valuePage.getText().toString());
                            selectedBook.setMaxPages(page);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            RadioButton radioChecked = (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
                            Integer page = Integer.parseInt(radioChecked.getText().toString().replace(" pages", ""));
                            selectedBook.setMaxPages(page);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                MainActivity.addToWishList(selectedBook);
                tools.customSnack(getContext(), returnFragView, "Livre ajouté à la liste d'envie !", "yellowshort");
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
    }


    private void popupAddToDownload() {
        String text = "Veux tu ajouter " + selectedBook.getName() + " à ta liste de téléchargements ?";

        Button okButton = new Button(getContext());
        okButton.setBackground(getContext().getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Oui");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

        okButton.setTextColor(getContext().getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(getContext());
        cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Annuler");
        cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(getContext())
                .setAnimation(R.raw.book_download)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Ajout aux téléchargemnts")
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
                MainActivity.addBookToDownload(selectedBook);
                tools.customSnack(getContext(), returnFragView, "Livre ajouté aux téléchargements !", "yellowshort");
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
    }

    private void popupAddToCurrent() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View alert = inflater.inflate(R.layout.my_lottie_alert, null);

        View alertInnerInfo = inflater.inflate(R.layout.inner_alert_infos, null);

        ((TextView) alertInnerInfo.findViewById(R.id.alert_title_info)).setText(selectedBook.getName());
        ((TextView) alertInnerInfo.findViewById(R.id.alert_author_info)).setText(selectedBook.getAutor().getFullName());

        RadioGroup radioGroup = (RadioGroup) alertInnerInfo.findViewById(R.id.radio_page_group);

        RadioButton pageOtherRadio = alertInnerInfo.findViewById(R.id.radio_page_other);
        pageOtherRadio.findViewById(R.id.radio_page_other).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertInnerInfo.findViewById(R.id.radio_page_other_prompt).setVisibility(View.VISIBLE);
            }
        });
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_enabled} //enabled
                },
                new int[]{getContext().getColor(R.color.primary_light_yellow)}
        );
        for (int i : selectedBook.getPagesFounds()) {
            RadioButton button = new RadioButton(getContext());
            button.setTextColor(getContext().getColor(R.color.primary_light_yellow));
            button.setButtonTintList(colorStateList);
            button.setText(i + " pages");
            radioGroup.addView(button, 0);
        }

        Button okButton = new Button(getContext());
        okButton.setBackground(getContext().getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Oui");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

        okButton.setTextColor(getContext().getColor(R.color.end_gradient_button_ok));
        Button cancelButton = new Button(getContext());
        cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Non");
        cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(getContext(), alert)
                .setAnimation(R.raw.reading_blank_bakground)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Souhaites commencer la lecture de ce livre ?")
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
                if (radioGroup.getCheckedRadioButtonId() != -1) {
                    //on check si other a été selectionner
                    if (radioGroup.getCheckedRadioButtonId() == pageOtherRadio.getId()) {
                        try {
                            EditText valuePage = (EditText) alertInnerInfo.findViewById(R.id.radio_page_other_prompt);
                            Integer page = Integer.parseInt(valuePage.getText().toString());
                            selectedBook.setMaxPages(page);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            RadioButton radioChecked = (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
                            Integer page = Integer.parseInt(radioChecked.getText().toString().replace(" pages", ""));
                            selectedBook.setMaxPages(page);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (MainActivity.getCurrentBook() == null) {
                    selectedBook.addStartTime();
                    MainActivity.setCurrentBook(selectedBook);

                    tools.customSnack(getContext(), returnFragView, "Bonne lecture !", "yellowshort");
                } else {
                    popupSwapBooks();
                    dialog.dismiss();
                }

                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
    }

    private void popupSwapBooks() {
        String text = "Tu lis actuellement " + MainActivity.getCurrentBook().getName() + " il sera mis sur l'étagère si tu veux commencer " + selectedBook.getName();

        Button okButton = new Button(getContext());
        okButton.setBackground(getContext().getDrawable(R.drawable.button_ok_gradient));
        okButton.setText("Ok");
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

        okButton.setTextColor(getContext().getColor(R.color.end_gradient_button_ok));

        Button cancelButton = new Button(getContext());
        cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

        cancelButton.setText("Annuler");
        cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

        MyLottieDialog dialog = new MyLottieDialog(getContext())
                .setAnimation(R.raw.add_shelf)
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
                tools.customSnack(getContext(), returnFragView, "Bonne lecture !", "yellowshort");
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
    }


    private void animate(final ImageButton buttonMain) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Animation anim = new ScaleAnimation(1f, 1.25f, 1f, 1.25f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setRepeatCount(1);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setDuration(666);

                buttonMain.startAnimation(anim);
            }
        }, getContext().getResources().getInteger(R.integer.translationFragDuration));
    }

    public View getBackButtonView() {
        return returnFragView.findViewById(R.id.back_main_from_search);
    }

 /*
    @Override
    public void onScrollStart(@NonNull ListBookAdapter.ViewHolder currentItemHolder, int adapterPosition) {

    }

    @Override
    public void onScrollEnd(@NonNull ListBookAdapter.ViewHolder currentItemHolder, int adapterPosition) {

    }


    @Override
    public void onScroll(
            float position,
            int currentIndex, int newIndex,
            @Nullable ListBookAdapter.ViewHolder currentHolder,
            @Nullable ListBookAdapter.ViewHolder newHolder) {
        Book current = bookAdapter.getData().get(currentIndex);
        RecyclerView.Adapter<?> adapter = bookAdapter;
        int itemCount = adapter != null ? adapter.getItemCount() : 0;
        if (newIndex >= 0 && newIndex < itemCount) {
            Book next = bookAdapter.getData().get(newIndex);
            returnFragView.onScroll(1f - Math.abs(position), current, next);
        }
    }*/
}

