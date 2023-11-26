package stellarnear.mystory.SettingsFraments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.view.Surface;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.Activities.ObservatoryActivity;
import stellarnear.mystory.Activities.SaveSharedPreferencesActivity;
import stellarnear.mystory.Activities.ShelfActivity;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;

public class SettingsFragment extends CustomPreferenceFragment {
    private Activity mA;
    private Context mC;
    private final List<String> histoPrefKeys = new ArrayList<>();
    private final List<String> histoTitle = new ArrayList<>();

    private String currentPageKey;
    private String currentPageTitle;

    private final Tools tools = Tools.getTools();
    private PrefInfoScreenFragment prefInfoScreenFragment;

/*
    private OnSharedPreferenceChangeListener listener =
            new OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.contains("they_key_that_triggersomething")) {
                        do nomething
                    }
                }
            };

 */

    @Override
    protected void onCreateFragment() {
        //settings.registerOnSharedPreferenceChangeListener(listener);  if we need to catch something
        this.mA = getActivity();
        this.mC = getContext();

        addPreferencesFromResource(R.xml.pref);
        this.histoPrefKeys.add("pref");

        this.histoTitle.add("Paramètres");

        this.prefInfoScreenFragment = new PrefInfoScreenFragment(mA, mC);
    }

    @Override
    protected void onPreferenceTreeClickFragment(android.preference.PreferenceScreen preferenceScreen, android.preference.Preference preference) throws Exception {
        if (preference.getKey().contains("pref_")) {
            histoPrefKeys.add(preference.getKey());
            histoTitle.add(preference.getTitle().toString());
            this.currentPageKey = preference.getKey();
            this.currentPageTitle = preference.getTitle().toString();
            navigate();
        } else {
            action(preference);
        }
    }

    // will be called by SettingsActivity (Host Activity)
    public void onUpButton() {
        if (histoPrefKeys.get(histoPrefKeys.size() - 1).equalsIgnoreCase("pref") || histoPrefKeys.size() <= 1) // in top-level
        {
            Intent intent;
            if (getActivity().getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_0) {
                intent = new Intent(mA, MainActivity.class);
            } else if (getActivity().getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_180) {
                intent = new Intent(mA, ObservatoryActivity.class);
            } else {
                intent = new Intent(mA, ShelfActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            mA.startActivity(intent);
        } else // in sub-level
        {
            currentPageKey = histoPrefKeys.get(histoPrefKeys.size() - 2);
            currentPageTitle = histoTitle.get(histoTitle.size() - 2);
            navigate();
            histoPrefKeys.remove(histoPrefKeys.size() - 1);
            histoTitle.remove(histoTitle.size() - 1);
        }
    }

    @Override
    protected void onDestroyFragment() {
        //settings.unregisterOnSharedPreferenceChangeListener(listener);
    }


    private void navigate() {
        if (currentPageKey.equalsIgnoreCase("pref")) {
            displayMainPage();
        } else if (currentPageKey.contains("pref_")) {
            loadPage();
        }
    }

    private void displayMainPage() {
        getPreferenceScreen().removeAll();
        int xmlID = R.xml.pref;
        addPreferencesFromResource(xmlID);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentPageTitle);
    }

    private void loadPage() {
        try {
            getPreferenceScreen().removeAll();
            int xmlID = getResources().getIdentifier(currentPageKey, "xml", getContext().getPackageName());
            addPreferencesFromResource(xmlID);
        } catch (Exception e) {
            e.printStackTrace();
            displayMainPage();
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentPageTitle);
    }

    private void action(Preference preference) throws Exception {
        switch (preference.getKey()) {
            case "infos":
                prefInfoScreenFragment.showInfo();
                break;

            case "export_save":
                Intent intentSave = new Intent(mC, SaveSharedPreferencesActivity.class);
                intentSave.putExtra("ACTION_TYPE", "SAVE");
                intentSave.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mC.startActivity(intentSave);
                break;
            case "import_save":
                Intent intentLoad = new Intent(mC, SaveSharedPreferencesActivity.class);
                intentLoad.putExtra("ACTION_TYPE", "LOAD");
                intentLoad.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mC.startActivity(intentLoad);
                break;


            case "send_report":
                log.sendReport(getActivity());
        }

    }
}