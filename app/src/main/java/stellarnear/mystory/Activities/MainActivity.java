package stellarnear.mystory.Activities;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import androidx.appcompat.app.AppCompatActivity;

import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import stellarnear.mystory.Activities.Fragments.MainActivityFragment;
import stellarnear.mystory.BookNodeAPI.BookNodeCalls;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.ListBookAdapter;
import stellarnear.mystory.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mainFrameFrag = findViewById(R.id.fragment_start_main_frame_layout);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().show();
        startFragment();
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