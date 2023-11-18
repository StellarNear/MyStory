package stellarnear.mystory.Activities.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import stellarnear.mystory.Log.CustomLog;

public abstract class CustomFragment extends Fragment {

    protected transient CustomLog log = new CustomLog(this.getClass());

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            return onCreateViewCustom(inflater, container, savedInstanceState);
        } catch (Exception e) {
            log.fatal(getActivity(), "Error in fragment : " + this.getClass(), e);
        }
        return null;
    }

    protected abstract View onCreateViewCustom(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);


}
