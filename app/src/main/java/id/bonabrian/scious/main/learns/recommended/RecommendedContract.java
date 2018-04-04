package id.bonabrian.scious.main.learns.recommended;

import java.util.List;

import id.bonabrian.scious.BasePresenter;
import id.bonabrian.scious.BaseView;
import id.bonabrian.scious.source.dao.Recommended;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class RecommendedContract {
    public interface View extends BaseView {
        void showRecommendedData(List<Recommended> recommendedList);
        void showError(String message);
        void showProgress();
        void hideProgress();
        void showRecommendedDetailView(String extras);
    }

    public interface Presenter extends BasePresenter<View> {
        void loadData(String user_id, int page);
        void openRecommendedDetail(String extras);
    }
}
