package stellarnear.mystory.Activities.Fragments;

import android.app.Activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import stellarnear.mystory.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private View returnFragView;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (container != null) {
            container.removeAllViews();
        }
        unlockOrient();

        returnFragView = inflater.inflate(R.layout.fragment_main, container, false);

        Button fabSearchPanel = returnFragView.findViewById(R.id.button_search);

        setButtonActivity(fabSearchPanel,new MainActivityFragmentSearchBooks(),R.animator.infromrightfrag, R.animator.outfadefrag, "frag_search");

        return returnFragView;
    }


    private void setButtonActivity(Button button, final MainActivityFragmentSearchBooks ActivityFragment, final int animIn, final int animOut, final String tag) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lockOrient();
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(animIn, animOut);
                fragmentTransaction.replace(R.id.fragment_main, ActivityFragment, tag);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    private void unlockOrient() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    private void lockOrient() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

}
