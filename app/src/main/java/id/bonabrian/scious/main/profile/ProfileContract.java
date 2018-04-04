package id.bonabrian.scious.main.profile;

import id.bonabrian.scious.BasePresenter;
import id.bonabrian.scious.BaseView;
import id.bonabrian.scious.source.dao.User;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ProfileContract {
    interface View extends BaseView {
        void showLoginView();
        void showProgress();
        void hideProgress();
        void showSuccessMessage(String message, User user);
        void showErrorMessage(String message);
    }

    interface Presenter extends BasePresenter<View> {
        void doLogout();
        void doEditName(String userId, String name);
        void doEditEmail(String userId, String email);
        void doEditWeight(String userId, double weight);
        void doEditHeight(String userId, double height);
        void doEditBirthday(String userId, String birthday);
    }
}
