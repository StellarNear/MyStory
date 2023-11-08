package stellarnear.mystory.Activities.Fragments;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import org.json.JSONException;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.ListBookAdapter;


public class MainActivityFragmentSearchBooks extends Fragment {

    private View returnFragView;

    private Tools tools = Tools.getTools();

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

        ImageButton fab = returnFragView.findViewById(R.id.fabSearch);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupSearch();
            }
        });

        returnFragView.findViewById(R.id.back_main_from_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToMain();
            }
        });
        animate(((ImageButton) returnFragView.findViewById(R.id.back_main_from_search)));
        return returnFragView;
    }

    private void popupSearch() {
        android.app.AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        final EditText edittext = new EditText(getContext());
        alert.setMessage("On cherche quoi ?");
        alert.setTitle("Recherche");
        alert.setView(edittext);
        alert.setIcon(R.drawable.ic_notifications_black_24dp);
        alert.setPositiveButton("Chercher", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    BookNodeCalls bookCall = new BookNodeCalls();
                    final List<Book> booksList = new ArrayList<>();
                    bookCall.setOnDataRecievedEventListener(new BookNodeCalls.OnDataRecievedEventListener() {
                        @Override
                        public void onEvent(List<Book> allBooks) {
                            Tools.getTools().customToast(getContext(),allBooks.size()+" livres trouvés");
                            booksList.addAll(allBooks);
                            popupDisplayListBookToPick(booksList);
                        }
                    });
                    bookCall.getBooksFromSearch(edittext.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        alert.show();
    }

    private void popupDisplayListBookToPick(List<Book> booksList) {
        DiscreteScrollView scrollView = returnFragView.findViewById(R.id.picker);
        scrollView.setAdapter(new ListBookAdapter(booksList));
    }

    private void backToMain() {
        Fragment fragment = new MainActivityFragment();
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.infromleftfrag, R.animator.outfadefrag);
        fragmentTransaction.replace(R.id.fragment_search, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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


}

