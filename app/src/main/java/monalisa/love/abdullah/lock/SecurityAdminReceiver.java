/*
 * Copyright (c) 2026 monalisa.love.abdullah
 * SPDX-License-Identifier: GPL-3.0-only
 */
package monalisa.love.abdullah.lock;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

public class SecurityAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        super.onDisabled(context, intent);
    }
}
