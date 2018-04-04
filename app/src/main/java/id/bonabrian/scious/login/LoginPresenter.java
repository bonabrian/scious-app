package id.bonabrian.scious.login;

import id.bonabrian.scious.R;
import id.bonabrian.scious.service.ApiService;
import id.bonabrian.scious.source.dao.User;
import id.bonabrian.scious.util.SessionManager;
import id.bonabrian.scious.util.Validator;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class LoginPresenter implements LoginContract.Presenter {

    LoginContract.View loginView;
    private CompositeSubscription subscription;

    @Override
    public void onAttach(LoginContract.View view) {
        this.loginView = view;
        subscription = new CompositeSubscription();
    }

    @Override
    public void onDetach() {
        subscription.clear();
        this.loginView = null;
    }

    @Override
    public void doLoginEmail(String email, String password) {
        loginView.showProgress();
        subscription.clear();
        Observable<User.UserList> call = ApiService.Factory.create().loginEmail(email, password);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<User.UserList>() {
                    @Override
                    public void onCompleted() {
                        loginView.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        loginView.hideProgress();
                        loginView.showMessageError("Something wrong :( " + e.getMessage());
                    }

                    @Override
                    public void onNext(User.UserList userList) {
                        if (userList.getStatus() == 200) {
                            if (SessionManager.setLoggedUser(loginView.getContext(), userList.getResult())) {
                                loginView.showMainView();
                            }
                        } else if (userList.getStatus() == 300) {
                            loginView.showMessageError(userList.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void doLoginGoogle(final String email, final String name) {
        loginView.showProgress();
        subscription.clear();
        Observable<User.UserList> call = ApiService.Factory.create().loginWithGoogle(email, name);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Observer<User.UserList>() {
            @Override
            public void onCompleted() {
                loginView.hideProgress();
            }

            @Override
            public void onError(Throwable e) {
                loginView.hideProgress();
                loginView.showMessageError("Something wrong :( " + e.getMessage());
            }

            @Override
            public void onNext(User.UserList userList) {
                if (userList.getStatus() == 200) {
                    if (SessionManager.setLoggedUser(loginView.getContext(), userList.getResult())) {
                        loginView.showMainView();
                    }
                } else if (userList.getStatus() == 300 && userList.getMessage().equalsIgnoreCase("You are not yet registered")) {
                    loginView.showRegisterForm(email, name);
                } else {
                    loginView.showMessageError(userList.getMessage());
                }
            }
        });
        this.subscription.add(subscription);
    }

    @Override
    public boolean isValidLoginData(String email, String password) {
        if (email.isEmpty()) {
            loginView.showMessageError(loginView.getContext().getResources().getString(R.string.email_cannot_be_empty));
            return false;
        }
        if (password.isEmpty()) {
            loginView.showMessageError(loginView.getContext().getResources().getString(R.string.password_cannot_be_empty));
            return false;
        }
//        if (email.isEmpty() || password.isEmpty()) {
//            loginView.showMessageError(loginView.getContext().getResources().getString(R.string.all_field_required));
//            return false;
//        }
        if (!Validator.isValidEmail(email)) {
            loginView.showMessageError(loginView.getContext().getResources().getString(R.string.invalid_email));
            return false;
        }
        return true;
    }
}
