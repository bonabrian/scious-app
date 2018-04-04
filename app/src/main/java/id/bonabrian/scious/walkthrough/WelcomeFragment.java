package id.bonabrian.scious.walkthrough;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import id.bonabrian.scious.R;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }
}
