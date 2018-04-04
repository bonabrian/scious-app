package id.bonabrian.scious.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.R;
import id.bonabrian.scious.main.MainActivity;
import id.bonabrian.scious.register.RegisterActivity;
import id.bonabrian.scious.util.AppConstant;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class LoginActivity extends AppCompatActivity implements LoginContract.View, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private LoginContract.Presenter presenter;

    @BindView(R.id.login_layout)
    LinearLayout layout;

    @BindView(R.id.input_email)
    EditText inputEmail;

    @BindView(R.id.input_password)
    EditText inputPassword;

    @BindView(R.id.btn_login)
    Button btnLogin;

    @BindView(R.id.btn_action_google)
    Button btnGoogle;

    @BindView(R.id.action_register)
    TextView linkRegister;

    private ProgressDialog progressDialog;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        initObject();
        onAttachView();

        btnLogin.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
        linkRegister.setOnClickListener(this);
    }

    private void initObject() {
        presenter = new LoginPresenter();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                if (presenter.isValidLoginData(inputEmail.getText().toString().trim(), inputPassword.getText().toString().trim())) {
                    presenter.doLoginEmail(inputEmail.getText().toString().trim(), inputPassword.getText().toString().trim());
                }
                break;
            case R.id.btn_action_google:
                startSignInGoogle();
                break;
            case R.id.action_register:
                showRegisterView();
                break;
        }
    }

    private void startSignInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, AppConstant.Tag.RC_SIGN_IN_GOOGLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstant.Tag.RC_SIGN_IN_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            Log.e("Login", "ActivityResultFailed");
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.i("Login", "handleSignInResult: " + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            Log.i("Login", "Display name: " + account.getDisplayName());
            String personName = account.getDisplayName();
            String email = account.getEmail();
            String idUserGoogle = account.getId();
            Log.i("Login", "Id: " + idUserGoogle + ", Name: " + personName + ", Email: " + email);
            presenter.doLoginGoogle(account.getEmail(), account.getDisplayName());
        } else {
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(layout, "Connection failed. Try again", 5000).show();
    }

    @Override
    public Context getContext() {
        return LoginActivity.this;
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
    public void showMessageError(String errorMessage) {
        Snackbar.make(layout, errorMessage, 5000).show();
    }

    @Override
    public void showProgress() {
        progressDialog = ProgressDialog.show(LoginActivity.this, "", "Authenticating", true, false);
    }

    @Override
    public void showRegisterView() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public void showRegisterForm(String email, String name) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        intent.putExtra("register-email", email);
        intent.putExtra("register-name", name);
        startActivity(intent);
    }

    @Override
    public void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showMainView() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
