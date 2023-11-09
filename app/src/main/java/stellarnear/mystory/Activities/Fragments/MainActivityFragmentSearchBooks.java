package stellarnear.mystory.Activities.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.ListBookAdapter;


public class MainActivityFragmentSearchBooks extends Fragment /* implements
        DiscreteScrollView.ScrollStateChangeListener<ListBookAdapter.ViewHolder>*/ {

    private View returnFragView;

    private Tools tools = Tools.getTools();
    private ListBookAdapter bookAdapter;

    public MainActivityFragmentSearchBooks() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (container != null) {
            container.removeAllViews();
        }

        returnFragView = inflater.inflate(R.layout.fragment_main_searsh_books, container, false);


        buildPage1();

        animate(((ImageButton) returnFragView.findViewById(R.id.back_main_from_search)));
        return returnFragView;
    }

    public void startSearch() {

        BookNodeCalls bookCall = new BookNodeCalls();
        final List<Book> booksList = new ArrayList<>();
        bookCall.setOnDataRecievedEventListener(new BookNodeCalls.OnDataRecievedEventListener() {
            @Override
            public void onEvent(List<Book> allBooks) {
                tools.customSnack(getContext(),returnFragView, allBooks.size() + " livres trouvés","yellow");
                booksList.addAll(allBooks);
                popupDisplayListBookToPick(booksList);
            }
        });

        EditText authorPrompt = returnFragView.findViewById(R.id.search_author_prompt);
        EditText titlePrompt = returnFragView.findViewById(R.id.search_title_prompt);
        String search = titlePrompt.getText().toString().trim()+" "+ authorPrompt.getText().toString().trim();
        try {
            bookCall.getBooksFromSearch(search);
        } catch (Exception e) {
            tools.customToast(getContext(), "Erreur lors de la recherche : " + e.getLocalizedMessage());
        }

    }

    private void popupDisplayListBookToPick(List<Book> booksList) {
        returnFragView.findViewById(R.id.linearSearchPrompt).setVisibility(View.GONE);
        returnFragView.findViewById(R.id.linearBooksFound).setVisibility(View.VISIBLE);
        returnFragView.findViewById(R.id.picker).setVisibility(View.VISIBLE);

        DiscreteScrollView scrollView = returnFragView.findViewById(R.id.picker);
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
                Book selectedBook = bookAdapter.getBook(adapterPosition);
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

    private void buildPage1() {
        //do the actual stuff
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

