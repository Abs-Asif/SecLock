/*
 * Copyright (c) 2026 monalisa.love.abdullah
 * SPDX-License-Identifier: GPL-3.0-only
 */
package monalisa.love.abdullah.config.password;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import monalisa.love.abdullah.R;
import monalisa.love.abdullah.lock.LockStore;

public final class SetNuclearWipePasswordDialog extends PasswordDialog {

    public SetNuclearWipePasswordDialog(Activity activity, LockStore lockStore, Runnable onSuccess) {
        super(activity, lockStore, onSuccess, R.string.nuclear_wipe_password_set_title,
                R.layout.password_first_set);
    }

    public SetNuclearWipePasswordDialog(Activity activity, LockStore lockStore, Runnable onSuccess, Runnable onCancel) {
        super(activity, lockStore, onSuccess, onCancel, R.string.nuclear_wipe_password_set_title,
                R.layout.password_first_set);
    }

    @Override
    protected void build() {
        final EditText passwordField = dialog.findViewById(R.id.passwordFieldView);
        final EditText repeatField = dialog.findViewById(R.id.repeatFieldView);
        final Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

        final TextListener validator = buildValidator(passwordField, repeatField, positiveBtn);
        passwordField.addTextChangedListener(validator);
        repeatField.addTextChangedListener(validator);

        positiveBtn.setVisibility(View.VISIBLE);
        positiveBtn.setText(R.string.password_set_action);
        positiveBtn.setEnabled(false);
        positiveBtn.setOnClickListener(v -> {
            final String passwordValue = passwordField.getText().toString();
            if (lockStore.setNuclearWipePassword(passwordValue)) {
                dismiss();
                onSuccess.run();
            }
        });
    }

    private TextListener buildValidator(EditText passwordField, EditText repeatField,
            Button positiveBtn) {
        return text -> {
            final String passwordValue = passwordField.getText().toString();
            final String repeatValue = repeatField.getText().toString();

            if (passwordValue.length() < MIN_PASSWORD_LENGTH) {
                positiveBtn.setEnabled(false);
                passwordField.setError(
                        res.getString(R.string.password_error_length, MIN_PASSWORD_LENGTH),
                        getErrorIcon());
                repeatField.setError(null);
            } else if (!passwordValue.equals(repeatValue)) {
                positiveBtn.setEnabled(false);
                passwordField.setError(null);
                repeatField.setError(res.getString(R.string.password_error_mismatch),
                        getErrorIcon());
            } else {
                positiveBtn.setEnabled(true);
                passwordField.setError(null);
                repeatField.setError(null);
            }
        };
    }
}
