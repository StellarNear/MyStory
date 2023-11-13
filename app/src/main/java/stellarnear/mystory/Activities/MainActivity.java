package stellarnear.mystory.Activities;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.Activities.Fragments.MainActivityFragment;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentSearchBooks;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentWishList;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.BooksLibs.Library;
import stellarnear.mystory.R;
import stellarnear.mystory.TinyDB;

public class MainActivity extends AppCompatActivity {

    private static Library library=null;
    private FrameLayout mainFrameFrag;

    private ConstraintLayout mConstraintLayout;
    private Window window;
    private Toolbar toolbar;

    private static  TinyDB tinyDB;

    private MainActivityFragment mainFrag;
    private MainActivityFragmentSearchBooks searchFrag;
    private MainActivityFragmentWishList wishListFrag;

    private FloatingActionButton fabSearchPanel;
    private FloatingActionButton fabWishList;
    private SharedPreferences settings;
    private GestureDetector gestureDetector;

    private FragShown fragShown=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeId = getResources().getIdentifier("AppThemePurple", "style", getPackageName());
        setTheme(themeId);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (settings.getBoolean("switch_fullscreen_mode", getApplicationContext().getResources().getBoolean(R.bool.switch_fullscreen_mode_def))) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(getApplicationContext());
        if(library==null){
            try {
                library=tinyDB.getLibrary();
            } catch (Exception e){
                e.printStackTrace();
                library=new Library();
                tinyDB.saveLibrary(library);
            }
        }
        gestureDetector = new GestureDetector(this, listener);
        setContentView(R.layout.activity_main);
        mainFrameFrag = findViewById(R.id.fragment_start_main_frame_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().show();

        fabSearchPanel = findViewById(R.id.fabSearch);
        fabWishList = findViewById(R.id.fabWishList);

        mainFrag = new MainActivityFragment();
        searchFrag = new MainActivityFragmentSearchBooks();
        wishListFrag =  new MainActivityFragmentWishList();
        window = getWindow();

        mConstraintLayout = (ConstraintLayout) findViewById(R.id.main_constrain);
        mConstraintLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        fabSearchPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fragShown==null || fragShown!=FragShown.SEARCH) {
                    startSearchFragment();
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

        fabWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWishListFragment();
            }
        });

        initMainFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOrientStart(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void checkOrientStart(int screenOrientation) {
        if (getResources().getConfiguration().orientation != screenOrientation) {
            setRequestedOrientation(screenOrientation);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                }
            }, 1000);
        }
    }

    private void startSearchFragment() {
        searchFrag = new MainActivityFragmentSearchBooks();
        ConstraintSet set = new ConstraintSet();
        set.clone(mConstraintLayout);

        window.setStatusBarColor(getColor(R.color.primary_middle_yellow));
        toolbar.setBackgroundColor(getColor(R.color.primary_dark_yellow));
        toolbar.setTitleTextColor(getColor(R.color.primary_light_yellow));
        toolbar.getOverflowIcon().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.primary_light_yellow), BlendModeCompat.SRC_ATOP));
        toolbar.setTitle("Recherche d'un nouveau livre");
        toolbar.setBackground(getDrawable(R.drawable.search_bar_back));

        //make the wish button out of bound
        set.clear(fabWishList.getId(),ConstraintSet.START);
        set.connect(fabWishList.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.START, 0);
        //then move the other center
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
                        searchFrag.clearAnimation();
                        restartMainFramemnt(R.id.fragment_search);
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

        startFragment(R.id.fragment_main, searchFrag, R.animator.infromrightfrag, R.animator.outfadefrag, "frag_search");
    }


    private void startWishListFragment() {
        wishListFrag = new MainActivityFragmentWishList();
        ConstraintSet set = new ConstraintSet();
        set.clone(mConstraintLayout);

        window.setStatusBarColor(getColor(R.color.primary_middle_pink));
        toolbar.setBackgroundColor(getColor(R.color.primary_dark_pink));
        toolbar.setTitleTextColor(getColor(R.color.primary_light_pink));
        toolbar.getOverflowIcon().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.primary_light_pink), BlendModeCompat.SRC_ATOP));
        toolbar.setTitle("Liste d'envies");
        toolbar.setBackground(getDrawable(R.drawable.wish_list_bar_back));

        //make the wish button out of bound
        set.clear(fabSearchPanel.getId(),ConstraintSet.END);
        set.connect(fabSearchPanel.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.END, 0);
        //then make the other center
        set.connect(fabWishList.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.START, 0);
        set.connect(fabWishList.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.END, 0);
        set.applyTo(mConstraintLayout);

        //set the back button
        wishListFrag.setOnFramentViewCreatedEventListener(new MainActivityFragmentWishList.OnFramentViewCreatedEventListener() {
            @Override
            public void onEvent() {
                wishListFrag.getBackButtonView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        wishListFrag.clearAnimation();
                        restartMainFramemnt(R.id.fragment_wish);
                    }
                });
            }
        });

        startFragment(R.id.fragment_main, wishListFrag, R.animator.infromleftfrag, R.animator.outfadefrag, "frag_wishlist");
    }


    private void restartMainFramemnt(int previousFragmentId) {
         mainFrag = new MainActivityFragment();
        ConstraintSet set = new ConstraintSet();
        set.clone(mConstraintLayout);

        window.setStatusBarColor(getColor(R.color.primary_middle_purple));
       toolbar.setBackgroundColor(getColor(R.color.primary_dark_purple));
        toolbar.setTitleTextColor(getColor(R.color.primary_light_purple));
        toolbar.getOverflowIcon().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.primary_light_purple), BlendModeCompat.SRC_ATOP));
        toolbar.setTitle("Livre du jour");
        toolbar.setBackground(getDrawable(R.drawable.dayly_book_bar_back));

        int margin = getResources().getDimensionPixelSize(R.dimen.fab_margin);

        if(previousFragmentId==R.id.fragment_wish){
            startFragment(previousFragmentId, mainFrag, R.animator.infromrightfrag, R.animator.outfadefrag, "frag_main");

            set.clear(fabWishList.getId(), ConstraintSet.END);
            set.connect(fabWishList.getId(),ConstraintSet.START,mConstraintLayout.getId(),ConstraintSet.START,margin);

            set.clear(fabSearchPanel.getId(),ConstraintSet.START);
            set.connect(fabSearchPanel.getId(),ConstraintSet.END,mConstraintLayout.getId(),ConstraintSet.END,margin);

            set.applyTo(mConstraintLayout);
        }
        if(previousFragmentId==R.id.fragment_search){

            fabSearchPanel.setImageDrawable(getDrawable(R.drawable.ic_book_add));

            startFragment(previousFragmentId, mainFrag, R.animator.infromleftfrag, R.animator.outfadefrag, "frag_main");

            set.clear(fabSearchPanel.getId(), ConstraintSet.START);
            set.connect(fabSearchPanel.getId(),ConstraintSet.END,mConstraintLayout.getId(),ConstraintSet.END,margin);

            set.clear(fabWishList.getId(),ConstraintSet.END);
            set.connect(fabWishList.getId(),ConstraintSet.START,mConstraintLayout.getId(),ConstraintSet.START,margin);

            set.applyTo(mConstraintLayout);
        }
        mainFrag.setScreen();
    }


    private void startFragment(final int fragId, final Fragment frag, final int animIn, final int animOut, final String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(animIn, animOut);
        fragmentTransaction.replace(fragId, frag, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
        if(frag instanceof MainActivityFragment){
           fragShown=FragShown.MAIN;
           unlockOrient();
        }
        if(frag instanceof MainActivityFragmentSearchBooks){
            fragShown=FragShown.SEARCH;
            lockOrient();
        }
        if(frag instanceof MainActivityFragmentWishList){
            fragShown=FragShown.WISH;
            lockOrient();
        }
    }

    @Override
    public void onBackPressed() {
        if(fragShown==FragShown.SEARCH){
            restartMainFramemnt(R.id.fragment_search);
        }  else if (fragShown==FragShown.WISH){
            restartMainFramemnt(R.id.fragment_wish);
        }
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
        getSupportFragmentManager().executePendingTransactions();
        window.setStatusBarColor(getColor(R.color.primary_middle_purple));
        toolbar.setBackgroundColor(getColor(R.color.primary_dark_purple));
        toolbar.setTitleTextColor(getColor(R.color.primary_light_purple));
        toolbar.getOverflowIcon().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.primary_light_purple), BlendModeCompat.SRC_ATOP));
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("Livre du jour");
                toolbar.setBackground(getDrawable(R.drawable.dayly_book_bar_back));
                fragShown=FragShown.MAIN;
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setActivityFromOrientation();
    }

    private void setActivityFromOrientation() {
        final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                //on y est déja
                break;

            case Surface.ROTATION_90:
                Intent intent_shelf = new Intent(MainActivity.this, ShelfActivity.class);
                startActivity(intent_shelf);
                finish();
                break;

            case Surface.ROTATION_180:
                break;

            case Surface.ROTATION_270:
                Intent intent_observatory = new Intent(MainActivity.this, ObservatoryActivity.class);
                startActivity(intent_observatory);
                finish();
                break;
        }
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
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
            return true;
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
    private final GestureDetector.SimpleOnGestureListener listener =
            new GestureDetector.SimpleOnGestureListener() {
                public boolean onDown(MotionEvent e1) { return false; }
                public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {

                    if(vx<-500){
                        if(fragShown==FragShown.MAIN){
                            startSearchFragment();
                        }
                        if(fragShown==FragShown.WISH){
                            restartMainFramemnt(R.id.fragment_wish);
                        }
                    }
                    if(vx>500){
                        if(fragShown==FragShown.MAIN){
                            startWishListFragment();
                        }
                        if(fragShown==FragShown.SEARCH){
                            restartMainFramemnt(R.id.fragment_search);
                        }
                    }
                    return false;
                }
            };


    // part to handle library

    public static void saveLibrary() {
        tinyDB.saveLibrary(library);
    }

    public static Book getCurrentBook() {
        return library.getCurrentBook();
    }

    public static List<Book> getWishList() {
        return library.getWishList();
    }

    public static void setCurrentBook(Book selectedBook) {
        if(selectedBook!=null) {
            library.setCurrentBook(selectedBook);
            saveLibrary();
        }
    }


    public static void putCurrentToShelf() {
        if(getCurrentBook()!=null){
            library.putCurrentToShelf();
            saveLibrary();
        }
    }

    public static void endBookAndPutToShelf() {
        library.getCurrentBook().addEndTime();
        putCurrentToShelf();
        saveLibrary();
    }

    public static void deleteCurrent() {
        library.deleteCurrent();
        saveLibrary();
    }

    public static void removeBookFromWishList(Book selectedBook) {
        if(selectedBook!=null){
            library.removeFromWishList(selectedBook);
            saveLibrary();
        }
    }

    public static void removeBookFromShelf(Book selectedBook) {
        if(selectedBook!=null){
            library.removeFromShelf(selectedBook);
            saveLibrary();
        }
    }


    public static void addToWishList(Book selectedBook) {
        if(selectedBook!=null){
            library.addToWishList(selectedBook);
            saveLibrary();
        }
    }

    public static List<Book> getShelf() {
        return  library.getShelfList();
    }


    private enum FragShown {
        MAIN,SEARCH,WISH
    }
}