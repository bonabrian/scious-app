package id.bonabrian.scious.splash;

import id.bonabrian.scious.BasePresenter;
import id.bonabrian.scious.BaseView;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SplashContract {
    public interface View extends BaseView {
        void showWalkthroughView();
        void showMainView();
        void showLoginView();
    }

    public interface Presenter extends BasePresenter<View> {
        void finishLoading();
    }
}
