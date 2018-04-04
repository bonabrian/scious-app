package id.bonabrian.scious.splash;

import android.os.Handler;

import id.bonabrian.scious.util.SessionManager;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SplashPresenter implements SplashContract.Presenter {

    private static int SPLASH_TIMEOUT = 500;
    private SplashContract.View splashView;

    @Override
    public void onAttach(SplashContract.View view) {
        splashView = view;
    }

    @Override
    public void onDetach() {
        splashView = null;
    }

    @Override
    public void finishLoading() {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (SessionManager.isFinishedWalkthrough(splashView.getContext())) {
                        if (SessionManager.isLoggedIn(splashView.getContext())) {
                            splashView.showMainView();
                        } else {
                            splashView.showLoginView();
                        }
                    } else {
                        splashView.showWalkthroughView();
                    }
                }
            }, SPLASH_TIMEOUT);
        } catch (NullPointerException e) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    splashView.showWalkthroughView();
                }
            }, SPLASH_TIMEOUT);
        }
    }
}
