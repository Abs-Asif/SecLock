/*
 * Copyright (c) 2026 monalisa.love.abdullah
 * SPDX-License-Identifier: GPL-3.0-only
 */
package monalisa.love.abdullah.config;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
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

import monalisa.love.abdullah.R;
import monalisa.love.abdullah.lock.LockStore;
import monalisa.love.abdullah.lock.SecurityAdminReceiver;
import monalisa.love.abdullah.lock.UnlockActivity;
import monalisa.love.abdullah.shell.AnemoShell;

public final class SettingsActivity extends Activity {

    private static final int REQUEST_UNLOCK = 101;

    private ViewGroup rootView;
    private Switch uninstallPreventionSwitch;
    private LockStore lockStore;
    private DevicePolicyManager dpm;
    private ComponentName componentName;
    private boolean isUiSetup = false;
    private int masterClickCount = 0;
    private long lastMasterClickTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE);

        lockStore = LockStore.getInstance(getApplicationContext());
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, SecurityAdminReceiver.class);

        if (lockStore.hasPassword() && lockStore.isLocked()) {
            Intent intent = new Intent(this, UnlockActivity.class);
            startActivityForResult(intent, REQUEST_UNLOCK);
        } else {
            setupSettingsUI();
        }
    }

    private void setupSettingsUI() {
        setContentView(R.layout.settings);
        rootView = findViewById(R.id.root_view);
        enableEdgeToEdge();

        uninstallPreventionSwitch = findViewById(R.id.settings_uninstall_prevention);
        refreshUninstallPreventionState();

        uninstallPreventionSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                if (!dpm.isAdminActive(componentName)) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Protects the app from unauthorized uninstallation.");
                    startActivity(intent);
                }
            } else {
                if (dpm.isAdminActive(componentName)) {
                    dpm.removeActiveAdmin(componentName);
                }
            }
        });

        final Switch shortcutSwitch = findViewById(R.id.settings_show_shortcut);
        shortcutSwitch.setChecked(AnemoShell.isEnabled(getApplication()));
        shortcutSwitch.setOnCheckedChangeListener(
                (v, isChecked) -> AnemoShell.setEnabled(getApplication(), isChecked));

        setupCredits();
        isUiSetup = true;
    }

    private void refreshUninstallPreventionState() {
        if (uninstallPreventionSwitch != null) {
            boolean isActive = dpm.isAdminActive(componentName);
            uninstallPreventionSwitch.setChecked(isActive);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isUiSetup) {
            refreshUninstallPreventionState();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_UNLOCK) {
            if (resultCode == RESULT_OK) {
                setupSettingsUI();
            } else {
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupCredits() {
        final TextView creditsLine1 = findViewById(R.id.credits_line1);
        final TextView creditsLine2 = findViewById(R.id.credits_line2);
        final TextView creditsLine3 = findViewById(R.id.credits_line3);

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

        setHtmlText(creditsLine1, "Originally created by <b>2bllw8</b>.");
        setHtmlText(creditsLine2, "Forked and Modified by <b>Abdullah Bari</b>.");
        setHtmlText(creditsLine3, "Version <b>" + versionName + "</b>");

        creditsLine2.setClickable(true);
        creditsLine2.setFocusable(true);
        creditsLine2.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMasterClickTime > 2000) {
                masterClickCount = 0;
            }
            masterClickCount++;
            lastMasterClickTime = currentTime;
            if (masterClickCount >= 5) {
                masterClickCount = 0;
                startActivity(new Intent(this, MasterSettingsActivity.class));
            }
        });
    }

    private void setHtmlText(TextView view, String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.setText(android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            view.setText(android.text.Html.fromHtml(html));
        }
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
