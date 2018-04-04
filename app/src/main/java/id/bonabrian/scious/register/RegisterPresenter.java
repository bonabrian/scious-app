package id.bonabrian.scious.register;

import android.util.Log;

import id.bonabrian.scious.R;
import id.bonabrian.scious.service.ApiService;
import id.bonabrian.scious.source.dao.BaseModel;
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

public class RegisterPresenter implements RegisterContract.Presenter {

    RegisterContract.View registerView;
    private CompositeSubscription subscription;

    @Override
    public void onAttach(RegisterContract.View view) {
        this.registerView = view;
        subscription = new CompositeSubscription();
    }

    @Override
    public void onDetach() {
        subscription.clear();
        this.registerView = null;
    }

    public boolean isValidDataRegister(String email, String name, String password, String retypePassword, String weight, String height) {
        if (name.isEmpty()) {
            registerView.showErrorMessage(registerView.getContext().getResources().getString(R.string.name_cannot_be_empty));
            return false;
        }
        if (email.isEmpty()) {
            registerView.showErrorMessage(registerView.getContext().getResources().getString(R.string.email_cannot_be_empty));
            return false;
        }
        if (password.isEmpty()) {
            registerView.showErrorMessage(registerView.getContext().getResources().getString(R.string.password_cannot_be_empty));
            return false;
        }
        if (!password.isEmpty() && retypePassword.isEmpty()) {
            registerView.showErrorMessage(registerView.getContext().getResources().getString(R.string.please_retype_password));
            return false;
        }
        if (weight.isEmpty()) {
            registerView.showErrorMessage(registerView.getContext().getResources().getString(R.string.weight_cannot_be_empty));
            return false;
        }
        if (height.isEmpty()) {
            registerView.showErrorMessage(registerView.getContext().getResources().getString(R.string.height_cannot_be_empty));
            return false;
        }
//        if (email.isEmpty() && name.isEmpty() && password.isEmpty() && weight.isEmpty() && height.isEmpty()) {
//            registerView.showErrorMessage(registerView.getContext().getResources().getString(R.string.all_field_required));
//            return false;
//        }
        if (!Validator.isValidEmail(email)) {
            registerView.showErrorMessage(registerView.getContext().getResources().getString(R.string.invalid_email));
            return false;
        }
        if (password.length() < 6) {
            registerView.showErrorMessage(registerView.getContext().getResources().getString(R.string.password_too_short));
            return false;
        }
        if (!password.equalsIgnoreCase(retypePassword)) {
            registerView.showErrorMessage(registerView.getContext().getResources().getString(R.string.password_mismatch));
            return false;
        }
        return true;
    }

    @Override
    public void doLoginGoogle(final String email, final String name) {
        registerView.showProgress();
        subscription.clear();
        Observable<User.UserList> call = ApiService.Factory.create().loginWithGoogle(email, name);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<User.UserList>() {
                    @Override
                    public void onCompleted() {
                        registerView.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        registerView.hideProgress();
                        registerView.showErrorMessage("Something wrong :( " + e.getMessage());
                        Log.e("Register", "Error: " + e.getMessage());
                    }

                    @Override
                    public void onNext(User.UserList userList) {
                        if (userList.getStatus() == 200) {
                            if (SessionManager.setLoggedUser(registerView.getContext(), userList.getResult())) {
                                registerView.showMainView();
                            }
                        } else if (userList.getStatus() == 300 && userList.getMessage().equalsIgnoreCase("No users found")) {
                            registerView.showRegisterForm(email, name);
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void registerUser(String name, String email, String password, String retypePassword, String weight, String height, String birthday) {
        if (isValidDataRegister(email, name, password, retypePassword, weight, height)) {
            registerView.showProgress();
            subscription.clear();
            Observable<BaseModel> call = ApiService.Factory.create().registerUser(name, email, password, retypePassword, weight, height, birthday);
            Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<BaseModel>() {
                        @Override
                        public void onCompleted() {
                            registerView.hideProgress();
                        }

                        @Override
                        public void onError(Throwable e) {
                            registerView.hideProgress();
                            registerView.showErrorMessage("Something wrong :( " + e.getMessage());
                            Log.e("Register", "Error: " + e.getMessage());
                        }

                        @Override
                        public void onNext(BaseModel baseModel) {
                            if (baseModel.getStatus() == 200) {
                                registerView.showSuccessMessage(registerView.getContext().getResources().getString(R.string.register_successfull));
                            } else if (baseModel.getStatus() == 300) {
                                registerView.showErrorMessage(baseModel.getMessage());
                            }
                        }
                    });
            this.subscription.add(subscription);
        }
    }
}
