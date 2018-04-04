package id.bonabrian.scious;

import android.content.Context;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface BaseView {
    Context getContext();

    void onAttachView();

    void onDetachView();
}
