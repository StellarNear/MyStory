package stellarnear.mystory.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import stellarnear.mystory.Activities.Fragments.MainActivityFragment;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentSearchBooks;
import stellarnear.mystory.Activities.Fragments.MainActivityFragmentWishList;
import stellarnear.mystory.R;

public class ObservatoryActivity extends AppCompatActivity {


    private Window window;
    private Toolbar toolbar;

    private MainActivityFragment mainFrag;
    private MainActivityFragmentSearchBooks searchFrag;
    private MainActivityFragmentWishList wishListFrag;

    private FloatingActionButton fabSearchPanel;
    private FloatingActionButton fabWishList;
    private SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeId = getResources().getIdentifier("AppThemeBlue", "style", getPackageName());
        setTheme(themeId);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (settings.getBoolean("switch_fullscreen_mode", getApplicationContext().getResources().getBoolean(R.bool.switch_fullscreen_mode_def))) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_observatory);
        toolbar = findViewById(R.id.toolbar);
        window = getWindow();

        initObervatory();
    }

    private void initObervatory() {
        window.setStatusBarColor(getColor(R.color.primary_middle_blue));
        toolbar.setBackgroundColor(getColor(R.color.primary_dark_blue));
        toolbar.setTitleTextColor(getColor(R.color.primary_light_blue));
        toolbar.getOverflowIcon().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.primary_light_blue), BlendModeCompat.SRC_ATOP));
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("L'observatoire");
                toolbar.setBackground(getDrawable(R.drawable.observatory_bar_back2));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOrientStart(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    private void checkOrientStart(int screenOrientation) {
        if (getRequestedOrientation() != screenOrientation) {
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setActivityFromOrientation();
    }

    private void setActivityFromOrientation() {
        final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                Intent intent_main = new Intent(ObservatoryActivity.this, MainActivity.class);
                startActivity(intent_main);
                finish();
                break;

            case Surface.ROTATION_90:
                Intent intent_shelf = new Intent(ObservatoryActivity.this, ShelfActivity.class);
                startActivity(intent_shelf);
                finish();
                break;

            case Surface.ROTATION_180:
                break;

            case Surface.ROTATION_270:
                //on y est
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
}