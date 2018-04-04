package id.bonabrian.scious.walkthrough;

import id.bonabrian.scious.BasePresenter;
import id.bonabrian.scious.BaseView;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class WalkthroughContract {
    public interface View extends BaseView {
        void showMainView();
        void showLoginView();
    }

    public interface Presenter extends BasePresenter<View> {
        void finishWalkthrough();
    }
}
