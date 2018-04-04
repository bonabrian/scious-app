package id.bonabrian.scious.main.profile;

import android.util.Log;

import id.bonabrian.scious.service.ApiService;
import id.bonabrian.scious.source.dao.User;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class ProfilePresenter implements ProfileContract.Presenter {

    private static final String TAG = ProfilePresenter.class.getSimpleName();

    CompositeSubscription subscription;
    ProfileContract.View viewProfile;

    @Override
    public void onAttach(ProfileContract.View view) {
        subscription = new CompositeSubscription();
        viewProfile = view;
    }

    @Override
    public void onDetach() {
        subscription.clear();
        viewProfile = null;
    }

    @Override
    public void doLogout() {
        viewProfile.showLoginView();
    }

    @Override
    public void doEditName(final String userId, String name) {
        viewProfile.showProgress();
        subscription.clear();
        Observable<User.UserData> call = ApiService.Factory.create().editUserName(userId, name);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new Observer<User.UserData>() {
                    @Override
                    public void onCompleted() {
                        viewProfile.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        viewProfile.hideProgress();
                        Log.e(TAG, "Error: " + e.getMessage());
                    }

                    @Override
                    public void onNext(User.UserData userData) {
                        if (userData.getStatus() == 200) {
                            viewProfile.showSuccessMessage(userData.getMessage(), userData.getData());
                        } else {
                            viewProfile.showErrorMessage(userData.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void doEditEmail(String userId, String email) {
        viewProfile.showProgress();
        subscription.clear();
        Observable<User.UserData> call = ApiService.Factory.create().editUserEmail(userId, email);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new Observer<User.UserData>() {
                    @Override
                    public void onCompleted() {
                        viewProfile.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        viewProfile.hideProgress();
                        Log.e(TAG, "Error: " + e.getMessage());
                    }

                    @Override
                    public void onNext(User.UserData userData) {
                        if (userData.getStatus() == 200) {
                            viewProfile.showSuccessMessage(userData.getMessage(), userData.getData());
                        } else {
                            viewProfile.showErrorMessage(userData.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void doEditWeight(String userId, double weight) {
        viewProfile.showProgress();
        subscription.clear();
        Observable<User.UserData> call = ApiService.Factory.create().editUserWeight(userId, weight);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new Observer<User.UserData>() {
                    @Override
                    public void onCompleted() {
                        viewProfile.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        viewProfile.hideProgress();
                        Log.e(TAG, "Error: " + e.getMessage());
                    }

                    @Override
                    public void onNext(User.UserData userData) {
                        if (userData.getStatus() == 200) {
                            viewProfile.showSuccessMessage(userData.getMessage(), userData.getData());
                        } else {
                            viewProfile.showErrorMessage(userData.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void doEditHeight(String userId, double height) {
        viewProfile.showProgress();
        subscription.clear();
        Observable<User.UserData> call = ApiService.Factory.create().editUserHeight(userId, height);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new Observer<User.UserData>() {
                    @Override
                    public void onCompleted() {
                        viewProfile.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        viewProfile.hideProgress();
                        Log.e(TAG, "Error: " + e.getMessage());
                    }

                    @Override
                    public void onNext(User.UserData userData) {
                        if (userData.getStatus() == 200) {
                            viewProfile.showSuccessMessage(userData.getMessage(), userData.getData());
                        } else {
                            viewProfile.showErrorMessage(userData.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }

    @Override
    public void doEditBirthday(String userId, String birthday) {
        viewProfile.showProgress();
        subscription.clear();
        Observable<User.UserData> call = ApiService.Factory.create().editUserBirthday(userId, birthday);
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new Observer<User.UserData>() {
                    @Override
                    public void onCompleted() {
                        viewProfile.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        viewProfile.hideProgress();
                        Log.e(TAG, "Error: " + e.getMessage());
                    }

                    @Override
                    public void onNext(User.UserData userData) {
                        if (userData.getStatus() == 200) {
                            viewProfile.showSuccessMessage(userData.getMessage(), userData.getData());
                        } else {
                            viewProfile.showErrorMessage(userData.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }
}
