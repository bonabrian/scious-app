package id.bonabrian.scious.login;

import id.bonabrian.scious.BasePresenter;
import id.bonabrian.scious.BaseView;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class LoginContract {
    public interface View extends BaseView {
        void showMessageError(String errorMessage);
        void showProgress();
        void showRegisterView();
        void showRegisterForm(String email, String name);
        void hideProgress();
        void showMainView();
    }

    public interface Presenter extends BasePresenter<View> {
        void doLoginEmail(String email, String password);
        void doLoginGoogle(String email, String name);
        boolean isValidLoginData(String email, String password);
    }
}
