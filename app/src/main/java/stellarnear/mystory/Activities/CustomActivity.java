package stellarnear.mystory.Activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import stellarnear.mystory.Log.CustomLog;
import stellarnear.mystory.R;


public abstract class CustomActivity extends AppCompatActivity {
    protected transient CustomLog log = new CustomLog(this.getClass());
    protected transient SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (settings.getBoolean("switch_fullscreen_mode", getApplicationContext().getResources().getBoolean(R.bool.switch_fullscreen_mode_def))) {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            super.onCreate(savedInstanceState);
            onCreateCustom();
        } catch (Exception e) {
            e.printStackTrace();
            log.fatal(this, "Error in activity : " + this.getLocalClassName(), e);
        }
    }

    abstract protected void onCreateCustom() throws Exception;

    @Override
    protected void onResume() {
        super.onResume();
        try {
            onResumeCustom();
        } catch (Exception e) {
            e.printStackTrace();
            log.fatal(this, "Error in activity : " + this.getLocalClassName(), e);
        }
    }

    protected abstract void onResumeCustom() throws Exception;

    @Override
    public void onBackPressed() {
        try {
            onBackPressedCustom();
        } catch (Exception e) {
            e.printStackTrace();
            log.fatal(this, "Error in activity : " + this.getLocalClassName(), e);
        }
    }

    protected abstract void onBackPressedCustom() throws Exception;

    @Override
    protected void onDestroy() {
        try {
            onDestroyCustom();
        } catch (Exception e) {
            e.printStackTrace();
            log.fatal(this, "Error in activity : " + this.getLocalClassName(), e);
        }
        super.onDestroy();
    }

    protected abstract void onDestroyCustom();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        boolean result = false;
        try {
            result = onOptionsItemSelectedCustom(item);
        } catch (Exception e) {
            e.printStackTrace();
            log.fatal(this, "Error in activity : " + this.getLocalClassName(), e);
        }

        return result;
    }

    protected abstract boolean onOptionsItemSelectedCustom(MenuItem item) throws Exception;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            onConfigurationChangedCustom();
        } catch (Exception e) {
            e.printStackTrace();
            log.fatal(this, "Error in activity : " + this.getLocalClassName(), e);
        }
    }

    protected abstract void onConfigurationChangedCustom();
}
