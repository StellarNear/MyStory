package stellarnear.mystory.Activities.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.CustomAlertDialog;
import stellarnear.mystory.UITools.ListBookAdapter;


public class MainActivityFragmentSearchBooks extends Fragment /* implements
        DiscreteScrollView.ScrollStateChangeListener<ListBookAdapter.ViewHolder>*/ {

    private View returnFragView;

    private Tools tools = Tools.getTools();
    private ListBookAdapter bookAdapter;
    private boolean resultShown=false;
    private Book selectedBook;

    public MainActivityFragmentSearchBooks() {
    }

    private OnFramentViewCreatedEventListener mLoadedListner;

    public void setOnFramentViewCreatedEventListener(OnFramentViewCreatedEventListener eventListener) {
        mLoadedListner = eventListener;
    }

    public boolean hasResultShown() {
        return this.resultShown;
    }

    public void clearResult() {
        bookAdapter.reset();

        returnFragView.findViewById(R.id.linearBooksFoundInfosSub).setVisibility(View.GONE);
        returnFragView.findViewById(R.id.pickerScroller).setVisibility(View.GONE);
        returnFragView.findViewById(R.id.loading_search).setVisibility(View.GONE);

        returnFragView.findViewById(R.id.linearSearchPrompt).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.search_title_prompt).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.search_author_prompt).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.loading_search).clearAnimation();
        returnFragView.findViewById(R.id.loading_search).setVisibility(View.GONE);
        resultShown=false;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (container != null) {
            container.removeAllViews();
        }

        returnFragView = inflater.inflate(R.layout.fragment_main_searsh_books, container, false);

       ImageButton backButton = (ImageButton) returnFragView.findViewById(R.id.back_main_from_search);

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


        if(mLoadedListner!=null){
            mLoadedListner.onEvent();
        }
        return returnFragView;
    }

    public void startSearch() {
        EditText authorPrompt = returnFragView.findViewById(R.id.search_author_prompt);
        EditText titlePrompt = returnFragView.findViewById(R.id.search_title_prompt);
        String search = titlePrompt.getText().toString().trim()+" "+ authorPrompt.getText().toString().trim();
        if(search.trim().length()<3){
            tools.customToast(getContext(),"Entres au moins un titre ou un auteur");
            return;
        }

        BookNodeCalls bookCall = new BookNodeCalls();
        final List<Book> booksList = new ArrayList<>();
        bookCall.setOnDataRecievedEventListener(new BookNodeCalls.OnDataRecievedEventListener() {
            @Override
            public void onEvent(List<Book> allBooks) {
                tools.customSnack(getContext(),returnFragView, allBooks.size() + " livres trouvés","yellow");
                booksList.addAll(allBooks);
                displayListBookToPick(booksList);
            }
        });

        try {
            bookCall.getBooksFromSearch(search);
        } catch (Exception e) {
            tools.customToast(getContext(), "Erreur lors de la recherche : " + e.getLocalizedMessage());
        }

        if(mSearchedListner!=null){
            mSearchedListner.onEvent();
            resultShown=true;
        }

        authorPrompt.setVisibility(View.GONE);
        titlePrompt.setVisibility(View.GONE);
        returnFragView.findViewById(R.id.loading_search).setVisibility(View.VISIBLE);

        ScaleAnimation mAnimation = new ScaleAnimation(1f,1.25f,1f,1.25f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
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
                TextView title = returnFragView.findViewById(R.id.list_book_title);
                title.setText(selectedBook.getName());
                TextView author = returnFragView.findViewById(R.id.list_book_author);
                author.setText(selectedBook.getAutor().getFullName());
                TextView pages = returnFragView.findViewById(R.id.list_book_page_count);
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
                LayoutInflater inflater = requireActivity().getLayoutInflater();

                // Inflate and set the layout for the dialog.
                // Pass null as the parent view because it's going in the dialog layout.
                View alert = inflater.inflate(R.layout.dialog_alert_add_to_wish,null);
                ((TextView)alert.findViewById(R.id.alert_title_info)).setText(selectedBook.getName());
                ((TextView)alert.findViewById(R.id.alert_author_info)).setText(selectedBook.getAutor().getFullName());
                CustomAlertDialog alertDialog = new CustomAlertDialog(getActivity(),getContext(),alert);
                alertDialog.addCancelButton("non");
                alertDialog.addConfirmButton("oui");
                alertDialog.setFill("widthheight");
                alertDialog.showAlert();


                /*
                      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(alert)
                        // Add action buttons
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                tools.customToast(getContext(),"c'est bon on ajoute wishlist");
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                builder.show();*/
            }
        });

        returnFragView.findViewById(R.id.add_to_current).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Get the layout inflater.
                LayoutInflater inflater = requireActivity().getLayoutInflater();

                // Inflate and set the layout for the dialog.
                // Pass null as the parent view because it's going in the dialog layout.
                builder.setView(inflater.inflate(R.layout.dialog_alert_add_to_wish, null))
                        // Add action buttons
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                tools.customToast(getContext(),"c'est bon on ajoute nen courrant");
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                builder.show();
            }
        });
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

