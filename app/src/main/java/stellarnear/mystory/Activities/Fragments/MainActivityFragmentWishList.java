package stellarnear.mystory.Activities.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.ListBookAdapter;
import stellarnear.mystory.UITools.MyLottieDialog;


public class MainActivityFragmentWishList extends Fragment {

    private View returnFragView;

    private Tools tools = Tools.getTools();
    private ListBookAdapter bookAdapter;
    private boolean resultShown = false;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
       if(backButton!=null){
           backButton.clearAnimation();
           ((ViewGroup)backButton.getParent()).removeView(backButton);
       }
    }

    private void loadWishList() {
        List<Book> wishList = MainActivity.getWishList();
        if(wishList.size()>0){

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

            Book bookZero = bookAdapter.getBook(0);
            TextView title = returnFragView.findViewById(R.id.list_book_title);
            title.setVisibility(View.VISIBLE);
            title.setText(bookZero.getName());
            TextView author = returnFragView.findViewById(R.id.list_book_author);
            author.setVisibility(View.VISIBLE);
            author.setText(bookZero.getAutor().getFullName());
            if(bookZero.getMaxPages()!=null){
                TextView pages = returnFragView.findViewById(R.id.list_book_page_count);
                pages.setVisibility(View.VISIBLE);
                pages.setText(bookZero.getMaxPages()+" pages");
            }

            scrollView.addOnItemChangedListener(new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
                @Override
                public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
                    selectedBook = bookAdapter.getBook(adapterPosition);
                    TextView title = returnFragView.findViewById(R.id.list_book_title);
                    title.setText(selectedBook.getName());
                    TextView author = returnFragView.findViewById(R.id.list_book_author);
                    author.setText(selectedBook.getAutor().getFullName());
                    if(selectedBook.getMaxPages()!=null){
                        TextView pages = returnFragView.findViewById(R.id.list_book_page_count);
                        pages.setText(selectedBook.getMaxPages() +" pages");
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
             returnFragView.findViewById(R.id.icon_book_linear).setVisibility(View.GONE);
            returnFragView.findViewById(R.id.no_wish_list).setVisibility(View.VISIBLE);
        }

    }




    private void popupDeleteBook() {
        String text = "Le livre " + selectedBook.getName() +" sera supprimé de la bibliotheque.";

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
                .setTitleColor(getContext().getColor(R.color.primary_light_pink))
                .setMessage(text)
                .setMessageColor(getContext().getColor(R.color.primary_light_pink))
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
                .setAnimation(R.raw.swap_book)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Commencer la lecture")
                .setTitleColor(getContext().getColor(R.color.primary_light_pink))
                .setMessage(text)
                .setMessageColor(getContext().getColor(R.color.primary_light_pink))
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
                if(MainActivity.getCurrentBook()==null){
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
                .setAnimation(R.raw.swap_book)
                .setAnimationRepeatCount(-1)
                .setAutoPlayAnimation(true)
                .setTitle("Tu as un livre en cours")
                .setTitleColor(getContext().getColor(R.color.primary_light_pink))
                .setMessage(text)
                .setMessageColor(getContext().getColor(R.color.primary_light_pink))
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
                if(MainActivity.getCurrentBook()==null){
                    MainActivity.putCurrentToShelf();
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

