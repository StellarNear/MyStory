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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

import stellarnear.mystory.Activities.LibraryLoader;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.Log.CustomLog;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.ListBookAdapter;
import stellarnear.mystory.UITools.MyLottieDialog;


public class MainActivityFragmentDownloadList extends CustomFragment {

    private View returnFragView;

    private final Tools tools = Tools.getTools();
    private ListBookAdapter bookAdapter;
    private final boolean resultShown = false;
    private Book selectedBook;
    private ImageButton backButton;
    private ImageButton email;

    public MainActivityFragmentDownloadList() {
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
        setExitTransition(inflaterTrannsi.inflateTransition(R.transition.slide_top));
        setEnterTransition(inflaterTrannsi.inflateTransition(R.transition.slide_bottom));
        int themeId = getResources().getIdentifier("AppThemeGreen", "style", getActivity().getPackageName());
        getActivity().setTheme(themeId);
        super.onCreate(savedInstanceState);
        if (container != null) {
            container.removeAllViews();
        }

        returnFragView = inflater.inflate(R.layout.fragment_main_download_list, container, false);

        backButton = (ImageButton) returnFragView.findViewById(R.id.back_main_from_download_list);

        email = (ImageButton) returnFragView.findViewById(R.id.email_download_list);

        Animation top = AnimationUtils.loadAnimation(getContext(), R.anim.infromtop);

        top.setAnimationListener(new Animation.AnimationListener() {
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

        email.startAnimation(top);
        backButton.startAnimation(top);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LibraryLoader.getDownloadList() != null && LibraryLoader.getDownloadList().size() > 0) {
                    try {
                        CustomLog.sendDownloadEmail(getActivity(), LibraryLoader.getDownloadList());
                    } catch (Exception e) {
                        tools.customToast(getContext(), "L'email n'a pas pu être envoyé : " + e.getMessage());
                    }
                } else {
                    tools.customToast(getContext(), "Aucun livres à envoyer...");
                }
            }
        });

        loadDownloadList();

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
        if (email != null) {
            email.clearAnimation();
            ((ViewGroup) email.getParent()).removeView(email);
        }
    }

    private void loadDownloadList() {
        List<Book> downloadList = LibraryLoader.getDownloadList();
        if (downloadList.size() > 0) {

            returnFragView.findViewById(R.id.linearBooksFoundInfosSub).setVisibility(View.VISIBLE);
            returnFragView.findViewById(R.id.downloadScroller).setVisibility(View.VISIBLE);
            returnFragView.findViewById(R.id.icon_book_linear).setVisibility(View.VISIBLE);

            DiscreteScrollView scrollView = returnFragView.findViewById(R.id.downloadScroller);
            scrollView.setSlideOnFling(true);
            scrollView.setItemTransformer(new ScaleTransformer.Builder()
                    .setMinScale(0.8f)
                    .build());
            bookAdapter = new ListBookAdapter(downloadList, scrollView);
            scrollView.setAdapter(bookAdapter);

            Book bookZero = bookAdapter.getBook(0);
            TextView title = returnFragView.findViewById(R.id.list_book_title);
            title.setVisibility(View.VISIBLE);
            title.setText(bookZero.getName());
            TextView author = returnFragView.findViewById(R.id.list_book_author);
            author.setVisibility(View.VISIBLE);
            author.setText(bookZero.getAutor().getFullName());


            scrollView.addOnItemChangedListener(new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
                @Override
                public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
                    selectedBook = bookAdapter.getBook(adapterPosition);
                    title.setText(selectedBook.getName());
                    author.setText(selectedBook.getAutor().getFullName());
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
            returnFragView.findViewById(R.id.downloadScroller).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.icon_book_linear).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.no_download_list).setVisibility(View.VISIBLE);
        }

    }


    private void popupDeleteBook() {
        String text = "Le livre " + selectedBook.getName() + " sera supprimé de la liste des téléchargement.";

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
                LibraryLoader.removeBookFromDownloadList(selectedBook);
                loadDownloadList();
                tools.customSnack(getContext(), returnFragView, "Livre supprimé !", "greenshort");
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
                if (LibraryLoader.getCurrentBook() == null) {
                    LibraryLoader.addStartTime(selectedBook);
                    LibraryLoader.setCurrentBook(selectedBook);
                    LibraryLoader.removeBookFromDownloadList(selectedBook);
                    loadDownloadList();
                    tools.customSnack(getContext(), returnFragView, "Bonne lecture !", "greenshort");
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
        String text = "Tu lis actuellement " + LibraryLoader.getCurrentBook().getName() + " il sera mis sur l'étagère si tu veux commencer " + selectedBook.getName();

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
                LibraryLoader.putCurrentToShelf();
                LibraryLoader.addStartTime(selectedBook);
                LibraryLoader.setCurrentBook(selectedBook);
                LibraryLoader.removeBookFromDownloadList(selectedBook);
                loadDownloadList();
                tools.customSnack(getContext(), returnFragView, "Bonne lecture !", "greenshort");
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
        return returnFragView.findViewById(R.id.back_main_from_download_list);
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

