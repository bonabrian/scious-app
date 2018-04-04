package id.bonabrian.scious.register;

import id.bonabrian.scious.BasePresenter;
import id.bonabrian.scious.BaseView;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class RegisterContract {
    public interface View extends BaseView {
        void showErrorMessage(String message);
        void showLoginView();
        void showSuccessMessage(String message);
        void showProgress();
        void hideProgress();
        void showMainView();
        void showRegisterForm(String email, String name);
    }

    public interface Presenter extends BasePresenter<View> {
        void doLoginGoogle(String email, String name);
        void registerUser(String name, String email, String password, String retypePassword, String weight, String height, String birthday);
    }
}
