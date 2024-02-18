package stellarnear.mystory.Activities.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.ListBookAdapter;
import stellarnear.mystory.UITools.MyLottieDialog;


public class MainActivityFragmentWishList extends CustomFragment {

    private View returnFragView;

    private final Tools tools = Tools.getTools();
    private ListBookAdapter bookAdapter;
    private final boolean resultShown = false;
    private Book selectedBook;
    private ImageButton backButton;

    public MainActivityFragmentWishList() {
    }

    private OnFramentViewCreatedEventListener mLoadedListner;

    public void setOnFramentViewCreatedEventListener(OnFramentViewCreatedEventListener eventListener) {
        mLoadedListner = eventListener;
    }


    public interface OnFramentViewCreatedEventListener {
        void onEvent();
    }

    @Override
    public View onCreateViewCustom(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        TransitionInflater inflaterTrannsi = TransitionInflater.from(requireContext());
        setExitTransition(inflaterTrannsi.inflateTransition(R.transition.slide_right));
        setEnterTransition(inflaterTrannsi.inflateTransition(R.transition.slide_left));
        int themeId = getResources().getIdentifier("AppThemePink", "style", getActivity().getPackageName());
        getActivity().setTheme(themeId);
        super.onCreate(savedInstanceState);
        if (container != null) {
            container.removeAllViews();
        }

        returnFragView = inflater.inflate(R.layout.fragment_main_wish_list, container, false);

        backButton = (ImageButton) returnFragView.findViewById(R.id.back_main_from_wish_list);

        Animation right = AnimationUtils.loadAnimation(getContext(), R.anim.infromright);

        right.setAnimationListener(new Animation.AnimationListener() {
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

        backButton.startAnimation(right);

        loadWishList();

        if (mLoadedListner != null) {
            mLoadedListner.onEvent();
        }
        return returnFragView;
    }


    public void clearAnimation() {
        if (backButton != null) {
            backButton.clearAnimation();
            ((ViewGroup) backButton.getParent()).removeView(backButton);
        }
    }

    private void loadWishList() {
        List<Book> wishList = MainActivity.getWishList();
        if (wishList.size() > 0) {

            returnFragView.findViewById(R.id.linearBooksFoundInfosSub).setVisibility(View.VISIBLE);
            returnFragView.findViewById(R.id.wishScroller).setVisibility(View.VISIBLE);
            returnFragView.findViewById(R.id.icon_book_linear).setVisibility(View.VISIBLE);


            DiscreteScrollView scrollView = returnFragView.findViewById(R.id.wishScroller);
            scrollView.setSlideOnFling(true);
            scrollView.setItemTransformer(new ScaleTransformer.Builder()
                    .setMinScale(0.8f)
                    .build());
            bookAdapter = new ListBookAdapter(wishList, scrollView);
            scrollView.setAdapter(bookAdapter);
            scrollView.scrollToPosition(wishList.size() - 1);

            returnFragView.findViewById(R.id.wish_forward_arrow).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollView.smoothScrollToPosition(wishList.size() - 1);
                }
            });
            returnFragView.findViewById(R.id.wish_backward_arrow).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollView.smoothScrollToPosition(0);
                }
            });

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
            title.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    popupSelectMaxPage();
                    return false;
                }
            });


            TextView author = returnFragView.findViewById(R.id.list_book_author);
            author.setVisibility(View.VISIBLE);
            author.setText(bookZero.getAutor().getFullName());
            TextView pages = returnFragView.findViewById(R.id.list_book_page_count);
            if (bookZero.getMaxPages() != null) {
                pages.setVisibility(View.VISIBLE);
                pages.setText(bookZero.getMaxPages() + " pages");
            }

            scrollView.addOnItemChangedListener(new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
                @Override
                public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
                    selectedBook = bookAdapter.getBook(adapterPosition);
                    title.setText(selectedBook.getName());
                    author.setText(selectedBook.getAutor().getFullName());
                    if (selectedBook.getMaxPages() != null) {
                        pages.setVisibility(View.VISIBLE);
                        pages.setText(selectedBook.getMaxPages() + " pages");
                    } else {
                        pages.setVisibility(View.GONE);
                    }
                }
            });

            returnFragView.findViewById(R.id.start_reading).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupAddToCurrent();
                }
            });

            returnFragView.findViewById(R.id.delete_book).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupDeleteBook();
                }
            });
        } else {
            returnFragView.findViewById(R.id.linearBooksFoundInfosSub).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.wishScroller).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.icon_book_linear).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.no_wish_list).setVisibility(View.VISIBLE);
        }

    }

    private void popupSelectMaxPage() {

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View alert = inflater.inflate(R.layout.my_lottie_alert, null);

        View alertInnerInfo = inflater.inflate(R.layout.inner_alert_infos, null);

        ((TextView) alertInnerInfo.findViewById(R.id.alert_title_info)).setText(selectedBook.getName());
        ((TextView) alertInnerInfo.findViewById(R.id.alert_author_info)).setText(selectedBook.getAutor().getFullName());
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

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(getResources().getDimensionPixelSize(R.dimen.general_margin), 0, 0, 0);

        MyLottieDialog dialog = new MyLottieDialog(getContext(), alert)
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
                    EditText valuePage = (EditText) alertInnerInfo.findViewById(R.id.radio_page_other_prompt);
                    Integer page = Integer.parseInt(valuePage.getText().toString());
                    selectedBook.setMaxPages(page);
                    tools.customSnack(getContext(), returnFragView, "Nombre de pages mis à jour à " + page + " !", "pinkshort");
                    MainActivity.saveBook(selectedBook);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                loadWishList();
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


    private void popupDeleteBook() {
        String text = "Le livre " + selectedBook.getName() + " sera supprimé de la liste des envies.";

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
                MainActivity.removeBookFromWishList(selectedBook);
                MainActivity.deleteBook(selectedBook);
                loadWishList();
                tools.customSnack(getContext(), returnFragView, "Livre supprimé !", "pinkshort");
                dialog.dismiss();
            }
        });
        dialog.show();
        cancelButton.setLayoutParams(param);
        okButton.setLayoutParams(param);
    }


    private void popupAddToCurrent() {
        String text = "Commencer la lecture de " + selectedBook.getName() + " ?";

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

        MyLottieDialog dialog = new MyLottieDialog(getContext())
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
                    MainActivity.addStartTime(selectedBook);
                    MainActivity.setCurrentBook(selectedBook);
                    MainActivity.removeBookFromWishList(selectedBook);
                    loadWishList();
                    tools.customSnack(getContext(), returnFragView, "Bonne lecture !", "pinkshort");
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
        String text = "Tu lis actuellement " + MainActivity.getCurrentBook().getName() + " il sera mis sur l'étagère si tu veux commencer " + selectedBook.getName();

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
                MainActivity.addStartTime(selectedBook);
                MainActivity.setCurrentBook(selectedBook);
                MainActivity.removeBookFromWishList(selectedBook);
                loadWishList();
                tools.customSnack(getContext(), returnFragView, "Bonne lecture !", "pinkshort");
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
        }, getResources().getInteger(R.integer.translationFragDuration));
    }

    public View getBackButtonView() {
        return returnFragView.findViewById(R.id.back_main_from_wish_list);
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

