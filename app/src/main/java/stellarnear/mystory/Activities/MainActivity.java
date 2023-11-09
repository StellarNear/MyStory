package stellarnear.mystory.Activities;

import android.animation.LayoutTransition;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import androidx.appcompat.app.AppCompatActivity;

import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.TransitionManager;

import stellarnear.mystory.Activities.Fragments.MainActivityFragment;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentSearchBooks;
import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.ListBookAdapter;
import stellarnear.mystory.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    private FrameLayout mainFrameFrag;
    private boolean toSearch=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mainFrameFrag = findViewById(R.id.fragment_start_main_frame_layout);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().show();



        FloatingActionButton fabSearchPanel = findViewById(R.id.fabSearch);

        fabSearchPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Window window = getWindow();
                ConstraintSet set = new ConstraintSet();
                ConstraintLayout mConstraintLayout  = (ConstraintLayout) findViewById(R.id.main_constrain);
                set.clone(mConstraintLayout);
                mConstraintLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
                if (toSearch) {
                    startFragment(R.id.fragment_main,new MainActivityFragmentSearchBooks(),R.animator.infromrightfrag, R.animator.outfadefrag, "frag_search");
                    window.setStatusBarColor(getColor(R.color.primary_middle_yellow));
                    toolbar.setBackgroundColor(getColor(R.color.primary_dark_yellow));

                    set.connect(fabSearchPanel.getId(), ConstraintSet.START,mConstraintLayout.getId(), ConstraintSet.START, 0);
                    set.applyTo(mConstraintLayout);
                    toSearch = false;
                } else {
                    startFragment(R.id.fragment_search,new MainActivityFragment(),R.animator.infromleftfrag,  R.animator.outfadefrag, "frag_main");
                    window.setStatusBarColor(getColor(R.color.primary_middle_purple));
                    toolbar.setBackgroundColor(getColor(R.color.primary_dark_purple));

                    set.clear(fabSearchPanel.getId(), ConstraintSet.START);
                    set.applyTo(mConstraintLayout);
                    toSearch = true;
                }
            }
        });

        startFragment();
    }



    private void startFragment(final int fragId,final Fragment ActivityFragment, final int animIn, final int animOut, final String tag) {
                lockOrient();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(animIn, animOut);
                fragmentTransaction.replace(fragId, ActivityFragment, tag);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
    }



    private void unlockOrient() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    private void lockOrient() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    private void startFragment() {
        Fragment fragment = new MainActivityFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(mainFrameFrag.getId(), fragment, "frag_main");
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment_start_main_frame_layout);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}