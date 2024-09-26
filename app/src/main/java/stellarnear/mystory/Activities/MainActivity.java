package stellarnear.mystory.Activities;

import android.animation.LayoutTransition;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.util.DisplayMetrics;
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

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import stellarnear.mystory.Activities.Fragments.MainActivityFragment;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentDownloadList;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentSearchBooks;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentWishList;
import stellarnear.mystory.DailyChecker.DailyCheckWorker;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.MyLottieDialog;

public class MainActivity extends CustomActivity {

    private FrameLayout mainFrameFrag;

    private ConstraintLayout mConstraintLayout;
    private Window window;
    private Toolbar toolbar;


    private MainActivityFragment mainFrag;
    private MainActivityFragmentSearchBooks searchFrag;
    private MainActivityFragmentWishList wishListFrag;
    private MainActivityFragmentDownloadList downloadFrag;

    private FloatingActionButton fabSearchPanel;
    private FloatingActionButton fabWishList;
    private FloatingActionButton fabDownload;

    private GestureDetector gestureDetector;

    private FragShown fragShown = null;

    private Tools tools = Tools.getTools();

    private static SharedPreferences prefs;

    @Override
    protected void onCreateCustom() throws Exception {
        int themeId = getResources().getIdentifier("AppThemePurple", "style", getPackageName());
        setTheme(themeId);
        createChannelNotif();


        if (LibraryLoader.getLibrary() == null) {
            LibraryLoader.loadLibrary(getApplicationContext());
        }

        if (LibraryLoader.getLibrary().getAccessStats().getnStreak() == 0 && LibraryLoader.shouldDisplayBreakStreakAnim() ) {
            MyLottieDialog ohNoChain = new MyLottieDialog(MainActivity.this)
                    .setTitle("Oh no ! RIP la chaîne...")
                    .setAnimation(R.raw.crying)
                    .setMessage("Moi qui pensais que tu aimais cette application, sniff...")
                    .setAnimationRepeatCount(-1)
                    .setAutoPlayAnimation(true)
                    .setCancelable(false)
                    .setOnShowListener(dialogInterface -> {
                    })
                    .setOnDismissListener(dialogInterface -> {
                    })
                    .setOnCancelListener(dialogInterface -> {
                    });
            ohNoChain.show();
            LibraryLoader.setDisplayBreakStreakAnim(false);

            final Handler closeAnim = new Handler();
            closeAnim.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ohNoChain.dismiss();
                }
            }, 5000);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        gestureDetector = new GestureDetector(this, listener);
        setContentView(R.layout.activity_main);
        mainFrameFrag = findViewById(R.id.fragment_start_main_frame_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().show();

        fabSearchPanel = findViewById(R.id.fabSearch);
        fabWishList = findViewById(R.id.fabWishList);
        fabDownload = findViewById(R.id.fabDownloadList);

        mainFrag = new MainActivityFragment();
        searchFrag = new MainActivityFragmentSearchBooks();
        wishListFrag = new MainActivityFragmentWishList();
        downloadFrag = new MainActivityFragmentDownloadList();

        window = getWindow();

        mConstraintLayout = (ConstraintLayout) findViewById(R.id.main_constrain);
        mConstraintLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        fabSearchPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fragShown == null || fragShown != FragShown.SEARCH) {
                    startSearchFragment();
                } else {
                    if (searchFrag.hasResultShown()) {
                        ConstraintSet set = new ConstraintSet();
                        set.clone(mConstraintLayout);
                        set.connect(fabSearchPanel.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.START, 0);
                        set.connect(fabSearchPanel.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.END, 0);
                        set.applyTo(mConstraintLayout);
                        fabSearchPanel.setImageDrawable(getDrawable(R.drawable.ic_baseline_search_24));
                        searchFrag.clearResult();
                    } else {
                        searchFrag.startSearch();
                    }
                }
            }
        });

        fabSearchPanel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (fragShown != null && fragShown == FragShown.SEARCH) {
                    searchFrag.addCustomBook();
                    return true;
                } else {
                    return false;
                }


            }
        });

        fabWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWishListFragment();
            }
        });

        fabDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDownloadFragment();
            }
        });

        initMainFragment();
    }

    private void createChannelNotif() {
        String channelId = "daily_reminder_channel_MS";
        CharSequence name = "Rappel journalier";
        String description = "Canal pour rappel journalier de l'applciation MS";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);

        // Register the channel with the system
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void setUpDailyChecker(Context mC) {
        // Get the current time and the time for 18:00 tomorrow
        Calendar current = Calendar.getInstance();
        Calendar nextDay = Calendar.getInstance();

        // Set nextDay to 18:00 tomorrow
        nextDay.add(Calendar.DATE, 1);  // Move to the next day
        nextDay.set(Calendar.HOUR_OF_DAY, 18);
        nextDay.set(Calendar.MINUTE, 0);
        nextDay.set(Calendar.SECOND, 0);

        // Calculate the delay in milliseconds until tomorrow at 18:00
        long delayInMillis = nextDay.getTimeInMillis() - current.getTimeInMillis();


        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false).setRequiresStorageNotLow(false).setRequiresDeviceIdle(false)
                .build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(DailyCheckWorker.class)
                .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(mC).enqueueUniqueWork("daily_checker_MS", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest);
    }

    @Override
    protected void onResumeCustom() throws Exception {
        checkOrientStart(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setUpDailyChecker(getApplicationContext());
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
        toolbar.setBackground(getDrawable(R.drawable.search_bar_back2));

        //make the download button out of bound
        set.clear(fabDownload.getId(), ConstraintSet.BOTTOM);
        set.connect(fabDownload.getId(), ConstraintSet.TOP, mConstraintLayout.getId(), ConstraintSet.BOTTOM, 0);
        //make the wish button out of bound
        set.clear(fabWishList.getId(), ConstraintSet.START);
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
                set.connect(fabSearchPanel.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.END, margin);
                set.applyTo(mConstraintLayout);
                fabSearchPanel.setImageDrawable(getDrawable(R.drawable.ic_baseline_searched_again_for_24));
            }
        });

        startFragment(R.id.fragment_main, searchFrag, "frag_search");
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
        toolbar.setBackground(getDrawable(R.drawable.wish_list_bar_back2));

        //make the download button out of bound
        set.clear(fabDownload.getId(), ConstraintSet.BOTTOM);
        set.connect(fabDownload.getId(), ConstraintSet.TOP, mConstraintLayout.getId(), ConstraintSet.BOTTOM, 0);

        //make the search button out of bound
        set.clear(fabSearchPanel.getId(), ConstraintSet.END);
        set.connect(fabSearchPanel.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.END, 0);

        //make the wish button out of bound
        set.clear(fabWishList.getId(), ConstraintSet.END);
        set.connect(fabWishList.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.END, 0);

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

        startFragment(R.id.fragment_main, wishListFrag, "frag_wishlist");
    }


    private void startDownloadFragment() {
        downloadFrag = new MainActivityFragmentDownloadList();
        ConstraintSet set = new ConstraintSet();
        set.clone(mConstraintLayout);

        window.setStatusBarColor(getColor(R.color.primary_middle_green));
        toolbar.setBackgroundColor(getColor(R.color.primary_dark_green));
        toolbar.setTitleTextColor(getColor(R.color.primary_light_green));
        toolbar.getOverflowIcon().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.primary_light_green), BlendModeCompat.SRC_ATOP));
        toolbar.setTitle("Liste des téléchargements");
        toolbar.setBackground(getDrawable(R.drawable.download_bar_back));

        //make the wish button out of bound
        set.clear(fabWishList.getId(), ConstraintSet.START);
        set.connect(fabWishList.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.START, 0);

        //make the search button out of bound
        set.clear(fabSearchPanel.getId(), ConstraintSet.END);
        set.connect(fabSearchPanel.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.END, 0);

        //make the download button out of bound
        set.clear(fabDownload.getId(), ConstraintSet.TOP);
        set.connect(fabDownload.getId(), ConstraintSet.BOTTOM, mConstraintLayout.getId(), ConstraintSet.TOP, 0);


        set.applyTo(mConstraintLayout);

        //set the back button
        downloadFrag.setOnFramentViewCreatedEventListener(new MainActivityFragmentDownloadList.OnFramentViewCreatedEventListener() {
            @Override
            public void onEvent() {
                downloadFrag.getBackButtonView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        downloadFrag.clearAnimation();
                        restartMainFramemnt(R.id.fragment_download);
                    }
                });
            }
        });


        startFragment(R.id.fragment_main, downloadFrag, "frag_downloadlist");
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
        toolbar.setBackground(getDrawable(R.drawable.daily_book_bar_back2));

        int margin = getResources().getDimensionPixelSize(R.dimen.fab_margin);

        if (previousFragmentId == R.id.fragment_wish) {
            mainFrag.setCustomEnterTransition(R.transition.slide_right, getApplicationContext());
            startFragment(previousFragmentId, mainFrag, "frag_main");

            set.clear(fabWishList.getId(), ConstraintSet.END);
            set.connect(fabWishList.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.START, margin);

            set.clear(fabSearchPanel.getId(), ConstraintSet.START);
            set.connect(fabSearchPanel.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.END, margin);

            set.clear(fabDownload.getId(), ConstraintSet.TOP);
            set.connect(fabDownload.getId(), ConstraintSet.BOTTOM, mConstraintLayout.getId(), ConstraintSet.BOTTOM, margin);

            set.applyTo(mConstraintLayout);
        }
        if (previousFragmentId == R.id.fragment_search) {
            mainFrag.setCustomEnterTransition(R.transition.slide_left, getApplicationContext());
            fabSearchPanel.setImageDrawable(getDrawable(R.drawable.ic_book_add));

            startFragment(previousFragmentId, mainFrag, "frag_main");

            set.clear(fabSearchPanel.getId(), ConstraintSet.START);
            set.connect(fabSearchPanel.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.END, margin);

            set.clear(fabWishList.getId(), ConstraintSet.END);
            set.connect(fabWishList.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.START, margin);

            set.clear(fabDownload.getId(), ConstraintSet.TOP);
            set.connect(fabDownload.getId(), ConstraintSet.BOTTOM, mConstraintLayout.getId(), ConstraintSet.BOTTOM, margin);

            set.applyTo(mConstraintLayout);
        }
        if (previousFragmentId == R.id.fragment_download) {
            mainFrag.setCustomEnterTransition(R.transition.slide_top, getApplicationContext());
            startFragment(previousFragmentId, mainFrag, "frag_main");

            set.clear(fabWishList.getId(), ConstraintSet.END);
            set.connect(fabWishList.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.START, margin);

            set.clear(fabSearchPanel.getId(), ConstraintSet.START);
            set.connect(fabSearchPanel.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.END, margin);

            set.clear(fabSearchPanel.getId(), ConstraintSet.START);
            set.connect(fabSearchPanel.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.END, margin);

            set.clear(fabDownload.getId(), ConstraintSet.TOP);
            set.connect(fabDownload.getId(), ConstraintSet.BOTTOM, mConstraintLayout.getId(), ConstraintSet.BOTTOM, margin);

            set.applyTo(mConstraintLayout);
        }
        mainFrag.setScreen();
    }


    private void startFragment(final int fragId, final Fragment frag, final String tag) {
        if (frag instanceof MainActivityFragment) {
            fragShown = FragShown.MAIN;
            unlockOrient();
        }
        if (frag instanceof MainActivityFragmentSearchBooks) {
            fragShown = FragShown.SEARCH;
            lockOrient();
        }
        if (frag instanceof MainActivityFragmentWishList) {
            fragShown = FragShown.WISH;
            lockOrient();
        }
        if (frag instanceof MainActivityFragmentDownloadList) {
            fragShown = FragShown.DOWNLOAD;
            lockOrient();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(fragId, frag, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    protected void onBackPressedCustom() throws Exception {
        if (fragShown == FragShown.SEARCH) {
            restartMainFramemnt(R.id.fragment_search);
        } else if (fragShown == FragShown.WISH) {
            restartMainFramemnt(R.id.fragment_wish);
        } else if (fragShown == FragShown.DOWNLOAD) {
            restartMainFramemnt(R.id.fragment_download);
        }
    }

    @Override
    protected void onDestroyCustom() {

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
                toolbar.setBackground(getDrawable(R.drawable.daily_book_bar_back2));
                fragShown = FragShown.MAIN;
            }
        });
    }

    @Override
    protected void onConfigurationChangedCustom() {
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
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
            return true;
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected boolean onOptionsItemSelectedCustom(MenuItem item) throws Exception {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            unlockOrient();
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("fromActivity", "MainActivity");
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final GestureDetector.SimpleOnGestureListener listener =
            new GestureDetector.SimpleOnGestureListener() {
                public boolean onDown(MotionEvent e1) {
                    return false;
                }

                public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int totalScreenWidth = displayMetrics.widthPixels;
                    int totalScreenHeight = displayMetrics.heightPixels;
                    int startX = (int) e1.getAxisValue(MotionEvent.AXIS_X);
                    int startY = (int) e1.getAxisValue(MotionEvent.AXIS_Y);
                    int endX = (int) e2.getAxisValue(MotionEvent.AXIS_X);
                    int endY = (int) e2.getAxisValue(MotionEvent.AXIS_Y);

                    if ((Math.abs(endY - startY) > Math.abs(endX - startX)) && vy < -500 && startY > (totalScreenHeight * 0.75)) {
                        if (fragShown == FragShown.MAIN && !mainFrag.isZoomedProgress()) {
                            startDownloadFragment();
                        }
                    }
                    if ((Math.abs(endY - startY) > Math.abs(endX - startX)) && vy > 500 && startY < (totalScreenHeight * 0.25)) {
                        if (fragShown == FragShown.DOWNLOAD) {
                            View downloadScroller = findViewById(R.id.downloadScroller);
                            if (downloadScroller != null && downloadScroller.isShown()) {
                                int[] location = new int[2];
                                downloadScroller.getLocationInWindow(location);
                                int y = location[1];
                                //si le scroll a lieu sur le scroller on ignore
                                if (e1.getAxisValue(MotionEvent.AXIS_Y) > y && e1.getAxisValue(MotionEvent.AXIS_Y) < y + downloadScroller.getMeasuredHeight()) {
                                    return false;
                                }
                            }
                            restartMainFramemnt(R.id.fragment_download);
                        }
                    }


                    if (vx < -500 && startX > (totalScreenWidth * 0.75)) {
                        if (fragShown == FragShown.MAIN && !mainFrag.isZoomedProgress()) {
                            startSearchFragment();
                        }
                        if (fragShown == FragShown.WISH) {
                            View wishScroller = findViewById(R.id.wishScroller);
                            if (wishScroller != null && wishScroller.isShown()) {
                                int[] location = new int[2];
                                wishScroller.getLocationInWindow(location);
                                int y = location[1];
                                //si le scroll a lieu sur le scroller on ignore
                                if (e1.getAxisValue(MotionEvent.AXIS_Y) > y && e1.getAxisValue(MotionEvent.AXIS_Y) < y + wishScroller.getMeasuredHeight()) {
                                    return false;
                                }
                            }
                            restartMainFramemnt(R.id.fragment_wish);
                        }
                    }
                    if (vx > 500 && startX < (totalScreenWidth * 0.25)) {
                        if (fragShown == FragShown.MAIN && !mainFrag.isZoomedProgress()) {
                            startWishListFragment();
                        }
                        if (fragShown == FragShown.SEARCH) {
                            View pickerScroller = findViewById(R.id.pickerScroller);
                            if (pickerScroller != null && pickerScroller.isShown()) {
                                int[] location = new int[2];
                                pickerScroller.getLocationInWindow(location);
                                int y = location[1];
                                //si le scroll a lieu sur le scroller on ignore
                                if (e1.getAxisValue(MotionEvent.AXIS_Y) > y && e1.getAxisValue(MotionEvent.AXIS_Y) < y + pickerScroller.getMeasuredHeight()) {
                                    return false;
                                }

                            }
                            restartMainFramemnt(R.id.fragment_search);
                        }
                    }
                    return false;
                }
            };


    private enum FragShown {
        MAIN, SEARCH, WISH, DOWNLOAD
    }
}