package stellarnear.mystory.Activities;

import android.animation.LayoutTransition;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import stellarnear.mystory.Activities.Fragments.MainActivityFragment;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentSearchBooks;
import stellarnear.mystory.R;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    private FrameLayout mainFrameFrag;
    private boolean toSearch = true;
    private MainActivityFragment mainFrag;
    private MainActivityFragmentSearchBooks searchFrag;
    private ConstraintLayout mConstraintLayout;
    private Window window;
    private Toolbar toolbar;
    private FloatingActionButton fabSearchPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mainFrameFrag = findViewById(R.id.fragment_start_main_frame_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().show();


        fabSearchPanel = findViewById(R.id.fabSearch);
        mainFrag = new MainActivityFragment();
        searchFrag = new MainActivityFragmentSearchBooks();
        window = getWindow();

        mConstraintLayout = (ConstraintLayout) findViewById(R.id.main_constrain);
        mConstraintLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        fabSearchPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toSearch) {
                    startSearchFragment();
                    toSearch = false;
                } else {
                    if(searchFrag.hasResultShown()){
                        ConstraintSet set = new ConstraintSet();
                        set.clone(mConstraintLayout);
                        set.connect(fabSearchPanel.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.START, 0);
                        set.connect(fabSearchPanel.getId(),ConstraintSet.END,mConstraintLayout.getId(),ConstraintSet.END,0);
                        set.applyTo(mConstraintLayout);
                        fabSearchPanel.setImageDrawable(getDrawable(R.drawable.ic_baseline_search_24));
                        searchFrag.clearResult();
                    } else {
                        searchFrag.startSearch();
                    }
                }
            }
        });

        initMainFragment();
    }


    private void startSearchFragment() {
        searchFrag = new MainActivityFragmentSearchBooks();
        ConstraintSet set = new ConstraintSet();
        set.clone(mConstraintLayout);
        startFragment(R.id.fragment_main, searchFrag, R.animator.infromrightfrag, R.animator.outfadefrag, "frag_search");
        window.setStatusBarColor(getColor(R.color.primary_middle_yellow));
        toolbar.setBackgroundColor(getColor(R.color.primary_dark_yellow));



        set.connect(fabSearchPanel.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.START, 0);
        set.connect(fabSearchPanel.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.END, 0);
        set.applyTo(mConstraintLayout);
        fabSearchPanel.setImageDrawable(getDrawable(R.drawable.ic_baseline_search_24));

        //set the back button
        searchFrag.setOnFramentViewCreatedEventListener(new MainActivityFragmentSearchBooks.OnFramentViewCreatedEventListener() {
            @Override
            public void onEvent() {
                searchFrag.getBackButtonView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchFrag = new MainActivityFragmentSearchBooks();
                        restartMainFramemnt();
                        toSearch = true;
                    }
                });
            }
        });

        searchFrag.setOnSearchedEventListener(new MainActivityFragmentSearchBooks.OnSearchedEventListener() {
            @Override
            public void onEvent() {
                int margin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
                set.clear(fabSearchPanel.getId(), ConstraintSet.START);
                set.connect(fabSearchPanel.getId(),ConstraintSet.END,mConstraintLayout.getId(),ConstraintSet.END,margin);
                set.applyTo(mConstraintLayout);
                fabSearchPanel.setImageDrawable(getDrawable(R.drawable.ic_baseline_searched_again_for_24));
            }
        });

    }

    private void restartMainFramemnt() {
        mainFrag = new MainActivityFragment();
        ConstraintSet set = new ConstraintSet();
        set.clone(mConstraintLayout);
        startFragment(R.id.fragment_search, mainFrag, R.animator.infromleftfrag, R.animator.outfadefrag, "frag_main");
        window.setStatusBarColor(getColor(R.color.primary_middle_purple));
        toolbar.setBackgroundColor(getColor(R.color.primary_dark_purple));



        int margin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        set.clear(fabSearchPanel.getId(), ConstraintSet.START);
        set.connect(fabSearchPanel.getId(),ConstraintSet.END,mConstraintLayout.getId(),ConstraintSet.END,margin);
        set.applyTo(mConstraintLayout);
        fabSearchPanel.setImageDrawable(getDrawable(R.drawable.ic_book_add));
    }


    private void startFragment(final int fragId, final Fragment ActivityFragment, final int animIn, final int animOut, final String tag) {
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


    private void initMainFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(mainFrameFrag.getId(), mainFrag, "frag_main");
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