package id.bonabrian.scious.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import id.bonabrian.scious.AbsSciousActivity;
import id.bonabrian.scious.R;
import id.bonabrian.scious.login.LoginActivity;
import id.bonabrian.scious.main.MainActivity;
import id.bonabrian.scious.walkthrough.WalkthroughActivity;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class SplashActivity extends AbsSciousActivity implements SplashContract.View {

    private SplashContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        presenter = new SplashPresenter();
        onAttachView();
        presenter.finishLoading();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onAttachView() {
        presenter.onAttach(this);
    }

    @Override
    public void onDetachView() {
        presenter.onDetach();
    }

    @Override
    public void showWalkthroughView() {
        Intent intent = new Intent(SplashActivity.this, WalkthroughActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showMainView() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showLoginView() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
