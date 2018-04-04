package id.bonabrian.scious.walkthrough;

import android.util.Log;

import id.bonabrian.scious.util.SessionManager;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class WalkthroughPresenter implements WalkthroughContract.Presenter {

    public WalkthroughContract.View view;

    @Override
    public void onAttach(WalkthroughContract.View view) {
        this.view = view;
    }

    @Override
    public void onDetach() {
        this.view = null;
    }

    @Override
    public void finishWalkthrough() {
        if (SessionManager.setFinishedWalkthrough(view.getContext(), true)) {
            Log.i("Walkthrough", "Status: " + SessionManager.isLoggedIn(view.getContext()));
            if (!SessionManager.isLoggedIn(view.getContext())) {
                view.showLoginView();
            } else {
                view.showMainView();
            }
        }
    }
}
