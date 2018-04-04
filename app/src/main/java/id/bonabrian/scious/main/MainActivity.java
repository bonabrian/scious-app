package id.bonabrian.scious.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.bonabrian.scious.AbsSciousActivity;
import id.bonabrian.scious.R;
import id.bonabrian.scious.discovery.DiscoveryActivity;
import id.bonabrian.scious.main.history.HistoryFragment;
import id.bonabrian.scious.main.home.HomeFragment;
import id.bonabrian.scious.main.learns.LearnsFragment;
import id.bonabrian.scious.main.profile.ProfileFragment;
import id.bonabrian.scious.util.AppConstant;
import id.bonabrian.scious.util.BottomNavigationViewHelper;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class MainActivity extends AbsSciousActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.main_bottom_navigation_view)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    private Stack<Fragment> fragmentStack;
    private Stack<String> fragmentNameStack;
    private FragmentManager fragmentManager;

    private CompositeSubscription subscription;
    private boolean logout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        subscription = new CompositeSubscription();

        fragmentStack = new Stack<Fragment>();
        fragmentNameStack = new Stack<String>();
        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container, new HomeFragment());
        fragmentStack.push(new HomeFragment());
        fragmentNameStack.push(AppConstant.Tag.TAG_HOME_FRAGMENT);
        fragmentTransaction.show(new HomeFragment());
        fragmentTransaction.commit();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchDiscoveryActivity();
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermissions() {
        List<String> wantedPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
            wantedPermissions.add(Manifest.permission.BLUETOOTH);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED) {
            wantedPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            wantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
            wantedPermissions.add(Manifest.permission.INTERNET);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED) {
            wantedPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (!wantedPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, wantedPermissions.toArray(new String[wantedPermissions.size()]), 0);
        }
    }

    private void launchDiscoveryActivity() {
        startActivity(new Intent(this, DiscoveryActivity.class));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_activity:
                pushFragment(new HomeFragment(), AppConstant.Tag.TAG_HOME_FRAGMENT, 0);
                return true;
            case R.id.action_history:
                pushFragment(new HistoryFragment(), AppConstant.Tag.TAG_HISTORY_FRAGMENT, 1);
                return true;
            case R.id.action_learns:
                pushFragment(new LearnsFragment(), AppConstant.Tag.TAG_LEARNS_FRAGMENT, 2);
                return true;
            case R.id.action_profile:
                pushFragment(new ProfileFragment(), AppConstant.Tag.TAG_PROFILE_FRAGMENT, 3);
                return true;
        }
        return false;
    }

    protected void pushFragment(Fragment fragment, String name, int position) {
        if (!fragmentNameStack.lastElement().equalsIgnoreCase(name)) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.main_container, fragment);
            fragmentStack.lastElement().onPause();
            fragmentTransaction.hide(fragmentStack.lastElement());
            fragmentStack.push(fragment);
            fragmentNameStack.push(name);
            fragmentTransaction.show(fragment);
            fragmentTransaction.replace(R.id.main_container, fragment);
            fragmentTransaction.commit();
        }
        if (position == 0) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentStack.size() > 1) {
            Log.i(TAG, "Popping backstack");
            FragmentTransaction ft = fragmentManager.beginTransaction();
            fragmentStack.lastElement().onPause();
            ft.remove(fragmentStack.pop());
            fragmentNameStack.pop();
            fragmentStack.lastElement().onResume();
            ft.show(fragmentStack.lastElement());
            ft.replace(R.id.main_container, fragmentStack.lastElement());
            ft.commit();

            if (fragmentNameStack.lastElement().equalsIgnoreCase(AppConstant.Tag.TAG_HOME_FRAGMENT)) {
                bottomNavigationView.setSelectedItemId(R.id.action_activity);
            }
            if (fragmentNameStack.lastElement().equalsIgnoreCase(AppConstant.Tag.TAG_HISTORY_FRAGMENT)) {
                bottomNavigationView.setSelectedItemId(R.id.action_history);
            }
            if (fragmentNameStack.lastElement().equalsIgnoreCase(AppConstant.Tag.TAG_LEARNS_FRAGMENT)) {
                bottomNavigationView.setSelectedItemId(R.id.action_learns);
            }
            if (fragmentNameStack.lastElement().equalsIgnoreCase(AppConstant.Tag.TAG_PROFILE_FRAGMENT)) {
                bottomNavigationView.setSelectedItemId(R.id.action_profile);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (!isLogout()) {
            subscription.clear();
        }
        super.onDestroy();
    }

    public boolean isLogout() {
        return logout;
    }

    public void setLogout(boolean logout) {
        this.logout = logout;
    }
}
