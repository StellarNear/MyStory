package stellarnear.mystory.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.stream.Collectors;

import stellarnear.mystory.BuildConfig;
import stellarnear.mystory.Log.CustomLog;
import stellarnear.mystory.Tools;


public class SaveSharedPreferencesActivity extends Activity {

    private final transient CustomLog log = new CustomLog(SaveSharedPreferencesActivity.class);

    private final Tools tools = Tools.getTools();
    private String saveNamePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saveNamePath = BuildConfig.APPLICATION_ID + "_save";

        String action = getIntent().getExtras().getString("ACTION_TYPE");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);


        if (action.equalsIgnoreCase("save")) {
            tools.customToast(getApplicationContext(), "Selection du dossier cible pour la sauvegarde");
            startActivityForResult(intent, 42);
        } else if (action.equalsIgnoreCase("load")) {
            tools.customToast(getApplicationContext(), "Selection du dossier cible pour charger : " + saveNamePath + ".json");
            startActivityForResult(intent, 666);
        } else {
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i("SAVE_INF", "Request:" + requestCode + " " + "Result:" + resultCode);
        if (requestCode == 42 && resultCode == Activity.RESULT_OK) {
            Uri treeUri = resultData.getData();

            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);

            DocumentFile previousSave = pickedDir.findFile(saveNamePath + ".json");
            if (previousSave == null) {
                writeFile(pickedDir, saveNamePath + ".json");
            } else {
                int nSave = 0;
                while (previousSave != null) {
                    nSave++;
                    previousSave = pickedDir.findFile(saveNamePath + "_" + nSave + ".json");
                }
                writeFile(pickedDir, saveNamePath + "_" + nSave + ".json");
            }
            finish();
        } else if (requestCode == 666 && resultCode == Activity.RESULT_OK) {
            Uri treeUri = resultData.getData();
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);

            DocumentFile previousSave = pickedDir.findFile(saveNamePath + ".json");
            if (previousSave == null) {
                tools.customToast(getApplicationContext(), "Aucune Sauvegarde présente ...");
            } else {
                loadFile(previousSave);
            }
            finish(); // Finish current activity
        } else {
            finish();
        }


    }

    private void writeFile(DocumentFile pickedDir, String nameFile) {
        try {
            DocumentFile newFile = pickedDir.createFile("application/json", nameFile);
            OutputStream out = this.getContentResolver().openOutputStream(newFile.getUri());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Map<String, ?> prefsMap = prefs.getAll();
            Gson gson = new Gson();
            String jsonString = gson.toJson(prefsMap);

            PrintStream printStream = new PrintStream(out);
            printStream.print(jsonString);
            printStream.close();

            tools.customToast(getApplicationContext(), "Sauvegarde crée : " + nameFile);
        } catch (Exception e) {
            e.printStackTrace();

            log.err("Error while writting json save", e);
            tools.customToast(getApplicationContext(), "Erreur:" + e.getStackTrace()[0]);
            DocumentFile previousSave = pickedDir.findFile(saveNamePath + ".json");
            if (previousSave != null) {
                previousSave.delete();
            }
        }
    }

    private void loadFile(DocumentFile previousSave) {
        try {
            InputStream in = this.getContentResolver().openInputStream(previousSave.getUri());

            String jsonString = new BufferedReader(new InputStreamReader(in))
                    .lines().collect(Collectors.joining("\n"));

            Gson gson = new Gson();

            Map<String, ?> savedMap = gson.fromJson(jsonString, Map.class);


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            for (Map.Entry<String, ?> entry : savedMap.entrySet()) {
                if (entry.getValue() instanceof String) {
                    prefs.edit().putString(entry.getKey(), (String) entry.getValue()).apply();
                } else if (entry.getValue() instanceof Boolean) {
                    prefs.edit().putBoolean(entry.getKey(), (Boolean) entry.getValue()).apply();
                } else if (entry.getValue() instanceof Integer || entry.getValue() instanceof Double) { //on a pas de double dans sharedPref mais ils sont store comme ca
                    prefs.edit().putInt(entry.getKey(), (int) Math.round((Double) entry.getValue())).apply();
                }
            }

            LibraryLoader.loadCurrentFromSave();
            LibraryLoader.loadAllListFromSave();
            tools.customToast(getApplicationContext(), "Sauvegarde chargée");
        } catch (Exception e) {
            e.printStackTrace();
            log.err("Error while laoding save json", e);
            tools.customToast(getApplicationContext(), "Erreur:" + e.getStackTrace()[0]);
        }
    }

    @Override
    public void onBackPressed() {
        //ne rien faire il faut finir cette activité !
    }
}
