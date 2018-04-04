package id.bonabrian.scious.main.history;

import java.util.List;

import id.bonabrian.scious.BasePresenter;
import id.bonabrian.scious.BaseView;
import id.bonabrian.scious.source.dao.Measurements;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class HistoryContract {
    public interface View extends BaseView {
        void showHistoryData(List<Measurements> measurementsList);
        void showError(String message);
        void showProgress();
        void hideProgress();
        void showHistoryDetailView(String extras);
    }

    public interface Presenter extends BasePresenter<View> {
        void loadData(String user_id, int page);
        void openHistoryDetail(String extras);
    }
}
