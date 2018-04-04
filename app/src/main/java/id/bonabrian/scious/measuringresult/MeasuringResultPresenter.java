package id.bonabrian.scious.measuringresult;

import android.util.Log;

import id.bonabrian.scious.service.ApiService;
import id.bonabrian.scious.source.dao.BaseModel;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class MeasuringResultPresenter implements MeasuringResultContract.Presenter {

    private static final String TAG = MeasuringResultPresenter.class.getSimpleName();
    MeasuringResultContract.View resultView;
    private CompositeSubscription subscription;

    @Override
    public void onAttach(MeasuringResultContract.View view) {
        this.resultView = view;
        subscription = new CompositeSubscription();
    }

    @Override
    public void onDetach() {
        subscription.clear();
        this.resultView = null;
    }

    @Override
    public void saveMeasurement(String user_id, String stress_level, double sdnn, double mean_hr, double mean_rr, String time) {
        subscription.clear();
        resultView.showProgress();
        Observable<BaseModel> call = ApiService.Factory.create().saveMeasurement(user_id,
                stress_level,
                sdnn,
                mean_hr,
                mean_rr,
                time
        );
        Subscription subscription = call.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<BaseModel>() {
                    @Override
                    public void onCompleted() {
                        resultView.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        resultView.hideProgress();
                        Log.e(TAG, "Error: " + e.getMessage());
                    }

                    @Override
                    public void onNext(BaseModel baseModel) {
                        if (baseModel.getStatus() == 200) {
                            resultView.showSuccessMessage(baseModel.getMessage());
                        } else {
                            resultView.showErrorMessage(baseModel.getMessage());
                        }
                    }
                });
        this.subscription.add(subscription);
    }
}
