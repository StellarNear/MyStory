package stellarnear.mystory.SettingsFraments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import stellarnear.mystory.Activities.LibraryLoader;
import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;


public class PrefResetScreenFragment extends Preference {
    private Context mC;
    private View mainView;

    public PrefResetScreenFragment(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public PrefResetScreenFragment(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public PrefResetScreenFragment(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        this.mC = getContext();

        mainView = new View(getContext());
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(parent.getWidth(), parent.getHeight());  //pour full screen
        mainView.setLayoutParams(params);
        addResetScreen();
        return mainView;
    }

    public void addResetScreen() {
        mainView.setBackgroundResource(R.drawable.reset_background);
        new AlertDialog.Builder(mC)
                .setIcon(R.drawable.ic_warning_24dp)
                .setTitle("Remise à zéro des paramètres")
                .setMessage("Es-tu sûre de vouloir tout réinitialiser ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearSettings();
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void clearSettings() {
        int time = 1500; // in milliseconds
        final Tools tools = Tools.getTools();
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mC);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                LibraryLoader.resetLibrary();
                LibraryLoader.loadLibrary(mC);
                tools.customToast(mC, "Remise à zero de tout les paramètres de l'application");
                Intent intent = new Intent(mC, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mC.startActivity(intent);
            }
        }, time);
    }
}
