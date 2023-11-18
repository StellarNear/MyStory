package stellarnear.mystory.Activities;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import stellarnear.mystory.R;
import stellarnear.mystory.SettingsFraments.SettingsFragment;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category pref shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends CustomActivity {
    SettingsFragment settingsFragment;



    @Override
    protected boolean onOptionsItemSelectedCustom(MenuItem item) throws Exception {
        switch (item.getItemId()) {
            case android.R.id.home:
                settingsFragment.onUpButton();
                return true;
        }
        return true;
    }

    @Override
    protected void onConfigurationChangedCustom() {

    }

    @Override
    protected void onCreateCustom() throws Exception {
        setContentView(R.layout.activity_settings);

        settingsFragment = new SettingsFragment();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.pref_content, settingsFragment)
                .commit();

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    protected void onResumeCustom() throws Exception {

    }

    @Override
    protected void onBackPressedCustom() throws Exception {
        settingsFragment.onUpButton();
    }


    @Override
    protected void onDestroyCustom() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
        finish();
        super.onDestroy();
    }
}