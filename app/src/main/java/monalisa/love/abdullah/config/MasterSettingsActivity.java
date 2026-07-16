/*
 * Copyright (c) 2026 monalisa.love.abdullah
 * SPDX-License-Identifier: GPL-3.0-only
 */
package monalisa.love.abdullah.config;

import android.app.Activity;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;

import monalisa.love.abdullah.R;
import monalisa.love.abdullah.config.password.SetNuclearWipePasswordDialog;
import monalisa.love.abdullah.config.password.ChangeNuclearWipePasswordDialog;
import monalisa.love.abdullah.config.password.SetFallbackStoragePasswordDialog;
import monalisa.love.abdullah.config.password.ChangeFallbackStoragePasswordDialog;
import monalisa.love.abdullah.lock.LockStore;

public final class MasterSettingsActivity extends Activity {

    private ViewGroup rootView;
    private LockStore lockStore;

    private Switch nuclearWipeSwitch;
    private TextView nuclearWipePasswordText;
    private Switch fallbackSwitch;
    private TextView fallbackPasswordText;

    private boolean isUpdatingSwitches = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE);

        lockStore = LockStore.getInstance(getApplicationContext());

        setContentView(R.layout.master_settings);
        rootView = findViewById(R.id.root_view);
        enableEdgeToEdge();

        nuclearWipeSwitch = findViewById(R.id.master_settings_nuclear_wipe_switch);
        nuclearWipePasswordText = findViewById(R.id.master_settings_nuclear_wipe_password);
        fallbackSwitch = findViewById(R.id.master_settings_fallback_switch);
        fallbackPasswordText = findViewById(R.id.master_settings_fallback_password);

        nuclearWipeSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            if (isUpdatingSwitches) return;
            if (isChecked) {
                if (!lockStore.hasNuclearWipePassword()) {
                    isUpdatingSwitches = true;
                    nuclearWipeSwitch.setChecked(false);
                    isUpdatingSwitches = false;
                    new SetNuclearWipePasswordDialog(this, lockStore, () -> {
                        lockStore.setNuclearWipeEnabled(true);
                        updateUI();
                    }, () -> {
                        lockStore.setNuclearWipeEnabled(false);
                        updateUI();
                    }).show();
                } else {
                    lockStore.setNuclearWipeEnabled(true);
                }
            } else {
                lockStore.setNuclearWipeEnabled(false);
            }
        });

        nuclearWipePasswordText.setOnClickListener(v -> {
            if (!lockStore.hasNuclearWipePassword()) {
                new SetNuclearWipePasswordDialog(this, lockStore, this::updateUI).show();
            } else {
                new ChangeNuclearWipePasswordDialog(this, lockStore, this::updateUI).show();
            }
        });

        fallbackSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            if (isUpdatingSwitches) return;
            if (isChecked) {
                if (!lockStore.hasFallbackStoragePassword()) {
                    isUpdatingSwitches = true;
                    fallbackSwitch.setChecked(false);
                    isUpdatingSwitches = false;
                    new SetFallbackStoragePasswordDialog(this, lockStore, () -> {
                        lockStore.setFallbackStorageEnabled(true);
                        updateUI();
                    }, () -> {
                        lockStore.setFallbackStorageEnabled(false);
                        updateUI();
                    }).show();
                } else {
                    lockStore.setFallbackStorageEnabled(true);
                }
            } else {
                lockStore.setFallbackStorageEnabled(false);
            }
        });

        fallbackPasswordText.setOnClickListener(v -> {
            if (!lockStore.hasFallbackStoragePassword()) {
                new SetFallbackStoragePasswordDialog(this, lockStore, this::updateUI).show();
            } else {
                new ChangeFallbackStoragePasswordDialog(this, lockStore, this::updateUI).show();
            }
        });

        updateUI();
    }

    private void updateUI() {
        isUpdatingSwitches = true;
        nuclearWipeSwitch.setChecked(lockStore.isNuclearWipeEnabled());
        fallbackSwitch.setChecked(lockStore.isFallbackStorageEnabled());
        isUpdatingSwitches = false;

        if (lockStore.hasNuclearWipePassword()) {
            nuclearWipePasswordText.setText("nuclear wipe password set/change");
        } else {
            nuclearWipePasswordText.setText("nuclear wipe password set/change (Not set)");
        }

        if (lockStore.hasFallbackStoragePassword()) {
            fallbackPasswordText.setText("Fallback storage password set/change");
        } else {
            fallbackPasswordText.setText("Fallback storage password set/change (Not set)");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

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
