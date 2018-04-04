package id.bonabrian.scious.measuringresult;

import id.bonabrian.scious.BasePresenter;
import id.bonabrian.scious.BaseView;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class MeasuringResultContract {
    interface View extends BaseView {
        void showProgress();
        void hideProgress();
        void showErrorMessage(String message);
        void showSuccessMessage(String message);
    }
    interface Presenter extends BasePresenter<View> {
       void saveMeasurement(String user_id,
                            String stress_level,
                            double sdnn,
                            double mean_hr,
                            double mean_rr,
                            String time);
    }
}
