/*
 * Copyright (c) 2022 2bllw8
 * SPDX-License-Identifier: GPL-3.0-only
 */
package monalisa.love.abdullah.config;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.function.Consumer;

import monalisa.love.abdullah.R;
import monalisa.love.abdullah.config.password.ChangePasswordDialog;
import monalisa.love.abdullah.config.password.SetPasswordDialog;
import monalisa.love.abdullah.lock.LockStore;
import monalisa.love.abdullah.lock.UnlockActivity;
import monalisa.love.abdullah.shell.AnemoShell;

public final class ConfigurationActivity extends Activity {

    private ViewGroup rootView;
    private TextView passwordSetView;
    private TextView changeLockView;
    private Switch biometricSwitch;
    private TextView lockImmediatelyView;
    private View lockImmediatelyDivider;

    private LockStore lockStore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lockStore = LockStore.getInstance(getApplicationContext());
        lockStore.addListener(onLockChanged);

        setContentView(R.layout.configuration);

        rootView = findViewById(R.id.root_view);
        enableEdgeToEdge();

        passwordSetView = findViewById(R.id.configuration_password_set);

        final Switch shortcutSwitch = findViewById(R.id.configuration_show_shortcut);
        shortcutSwitch.setChecked(AnemoShell.isEnabled(getApplication()));
        shortcutSwitch.setOnCheckedChangeListener(
                (v, isChecked) -> AnemoShell.setEnabled(getApplication(), isChecked));

        changeLockView = findViewById(R.id.configuration_lock);
        changeLockView.setText(lockStore.isLocked()
                ? R.string.configuration_storage_unlock
                : R.string.configuration_storage_lock);
        changeLockView.setOnClickListener($ -> {
            if (lockStore.isLocked()) {
                startActivity(new Intent(this, UnlockActivity.class));
            } else {
                lockStore.lock();
            }
        });

        lockImmediatelyDivider = findViewById(R.id.configuration_lock_immediately_divider);
        lockImmediatelyView = findViewById(R.id.configuration_lock_immediately);
        lockImmediatelyView.setOnClickListener($ -> {
            lockStore.lock();
            finishAffinity();
        });

        boolean isLocked = lockStore.isLocked();
        lockImmediatelyView.setVisibility(isLocked ? View.GONE : View.VISIBLE);
        lockImmediatelyDivider.setVisibility(isLocked ? View.GONE : View.VISIBLE);

        final Switch autoLockSwitch = findViewById(R.id.configuration_auto_lock);
        final View autoLockDivider = findViewById(R.id.configuration_auto_lock_divider);
        final TextView autoLockDelayView = findViewById(R.id.configuration_auto_lock_delay);

        autoLockSwitch.setChecked(lockStore.isAutoLockEnabled());
        autoLockDelayView.setVisibility(lockStore.isAutoLockEnabled() ? View.VISIBLE : View.GONE);
        autoLockDivider.setVisibility(lockStore.isAutoLockEnabled() ? View.VISIBLE : View.GONE);
        updateAutoLockDelayText(autoLockDelayView, lockStore.getAutoLockMinutes());

        autoLockSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            lockStore.setAutoLockEnabled(isChecked);
            autoLockDelayView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            autoLockDivider.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        autoLockDelayView.setOnClickListener($ -> showAutoLockDelayDialog(autoLockDelayView));

        biometricSwitch = findViewById(R.id.configuration_biometric_unlock);
        biometricSwitch.setVisibility(lockStore.canAuthenticateBiometric()
                ? View.VISIBLE
                : View.GONE);
        biometricSwitch.setChecked(lockStore.isBiometricUnlockEnabled());
        biometricSwitch.setOnCheckedChangeListener(
                (v, isChecked) -> lockStore.setBiometricUnlockEnabled(isChecked));

        setupPasswordViews();
        setupCredits();
    }

    private void setupCredits() {
        final TextView creditsView = findViewById(R.id.configuration_credits);
        String versionName = "";
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                versionName = getPackageManager().getPackageInfo(getPackageName(),
                        PackageManager.PackageInfoFlags.of(0)).versionName;
            } else {
                versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            }
        } catch (Exception e) {
            versionName = "1.0.0";
        }

        String creditsHtml = getString(R.string.configuration_credits, versionName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            creditsView.setText(android.text.Html.fromHtml(creditsHtml, android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            creditsView.setText(android.text.Html.fromHtml(creditsHtml));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lockStore.removeListener(onLockChanged);
    }

    private void setupPasswordViews() {
        if (lockStore.hasPassword()) {
            passwordSetView.setText(R.string.configuration_password_change);
            passwordSetView.setOnClickListener(
                    $ -> new ChangePasswordDialog(this, lockStore, this::setupPasswordViews)
                            .show());
        } else {
            passwordSetView.setText(R.string.configuration_password_set);
            passwordSetView.setOnClickListener(
                    $ -> new SetPasswordDialog(this, lockStore, this::setupPasswordViews).show());
        }
        final boolean enableViews = !lockStore.isLocked();
        passwordSetView.setEnabled(enableViews);
        biometricSwitch.setEnabled(enableViews);
    }

    private void updateAutoLockDelayText(TextView view, int minutes) {
        view.setText(getString(R.string.configuration_storage_lock_auto_delay, minutes));
    }

    private void showAutoLockDelayDialog(TextView delayView) {
        final android.widget.NumberPicker picker = new android.widget.NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(60);
        picker.setValue(lockStore.getAutoLockMinutes());

        final android.widget.FrameLayout layout = new android.widget.FrameLayout(this);
        final android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.CENTER;
        picker.setLayoutParams(params);
        layout.addView(picker);

        new android.app.AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("Set lock delay")
                .setView(layout)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    int selectedMinutes = picker.getValue();
                    lockStore.setAutoLockMinutes(selectedMinutes);
                    updateAutoLockDelayText(delayView, selectedMinutes);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private final Consumer<Boolean> onLockChanged = isLocked -> {
        passwordSetView.setEnabled(!isLocked);
        biometricSwitch.setEnabled(!isLocked);
        changeLockView.setText(isLocked
                ? R.string.configuration_storage_unlock
                : R.string.configuration_storage_lock);
        lockImmediatelyView.setVisibility(isLocked ? View.GONE : View.VISIBLE);
        lockImmediatelyDivider.setVisibility(isLocked ? View.GONE : View.VISIBLE);
    };

    private void enableEdgeToEdge() {
        if (Build.VERSION.SDK_INT < 35) {
            return;
        }

        rootView.setOnApplyWindowInsetsListener((v, windowInsets) -> {
            final Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars());
            final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                    v.getLayoutParams();
            mlp.topMargin = insets.top;
            mlp.rightMargin = insets.right;
            mlp.bottomMargin = insets.bottom;
            mlp.leftMargin = insets.left;
            v.setLayoutParams(mlp);
            return WindowInsets.CONSUMED;
        });
    }
}
