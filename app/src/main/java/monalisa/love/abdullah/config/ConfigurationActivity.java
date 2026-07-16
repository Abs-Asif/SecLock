/*
 * Copyright (c) 2026 monalisa.love.abdullah
 * SPDX-License-Identifier: GPL-3.0-only
 */
package monalisa.love.abdullah.config;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.function.Consumer;

import monalisa.love.abdullah.R;
import monalisa.love.abdullah.config.password.ChangePasswordDialog;
import monalisa.love.abdullah.config.password.SetPasswordDialog;
import monalisa.love.abdullah.lock.LockStore;
import monalisa.love.abdullah.lock.UnlockActivity;

public final class ConfigurationActivity extends Activity {

    private ViewGroup rootView;
    private TextView passwordSetView;
    private TextView changeLockView;

    private LockStore lockStore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE);

        lockStore = LockStore.getInstance(getApplicationContext());
        lockStore.addListener(onLockChanged);

        setContentView(R.layout.configuration);

        rootView = findViewById(R.id.root_view);
        enableEdgeToEdge();

        passwordSetView = findViewById(R.id.configuration_password_set);

        changeLockView = findViewById(R.id.configuration_lock);
        changeLockView.setText(lockStore.isLocked()
                ? R.string.configuration_storage_unlock
                : R.string.configuration_storage_lock);
        changeLockView.setOnClickListener($ -> {
            if (lockStore.isLocked()) {
                startActivity(new Intent(this, UnlockActivity.class));
            } else {
                if (lockStore.hasPassword()) {
                    lockStore.lock();
                } else {
                    new SetPasswordDialog(this, lockStore, () -> {
                        setupPasswordViews();
                        if (lockStore.hasPassword()) {
                            lockStore.lock();
                        }
                    }).show();
                }
            }
        });

        final TextView autoLockDelayView = findViewById(R.id.configuration_auto_lock_delay);

        // Always ensure auto-lock is enabled so delay is functional
        lockStore.setAutoLockEnabled(true);
        updateAutoLockDelayText(autoLockDelayView, lockStore.getAutoLockMinutes());

        autoLockDelayView.setOnClickListener($ -> showAutoLockDelayDialog(autoLockDelayView));

        setupPasswordViews();
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
        changeLockView.setText(isLocked
                ? R.string.configuration_storage_unlock
                : R.string.configuration_storage_lock);
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
