package de.timschubert.mediiva.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.library.Library;

public class NewLibraryDialog extends DialogFragment
{

    private Callback callback;

    private Spinner typeSpinner;
    private EditText nameEditText;
    private EditText hostnameEditText;
    private EditText smbShareEditText;
    private EditText pathEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); //TODO
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        Context context = requireContext();
        View view = View.inflate(context, R.layout.dialog_new_library, null);

        typeSpinner = view.findViewById(R.id.dialog_new_library_type_spinner);
        nameEditText = view.findViewById(R.id.dialog_new_library_name);
        hostnameEditText = view.findViewById(R.id.dialog_new_library_hostname);
        smbShareEditText = view.findViewById(R.id.dialog_new_library_share);
        pathEditText = view.findViewById(R.id.dialog_new_library_path);
        usernameEditText = view.findViewById(R.id.dialog_new_library_username);
        passwordEditText = view.findViewById(R.id.dialog_new_library_password);

        TextWatcher textWatcher = new TextWatcher()
        {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                updateButtons();
            }
        };

        nameEditText.addTextChangedListener(textWatcher);
        hostnameEditText.addTextChangedListener(textWatcher);
        smbShareEditText.addTextChangedListener(textWatcher);
        pathEditText.addTextChangedListener(textWatcher);
        usernameEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        return new AlertDialog.Builder(context)
                .setView(view)
                .setTitle("New library") //TODO
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> onSendResult())
                .create();
    }

    /**
     * Used to give the positive button a disabled state upon launch
     */
    @Override
    public void onStart()
    {
        super.onStart();
        updateButtons();
    }

    /**
     * Disables the positive button if not all of the forms are filled out
     */
    private void updateButtons()
    {
        AlertDialog dialog = (AlertDialog) getDialog();
        if(dialog == null) return;

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(
                !TextUtils.isEmpty(nameEditText.getText()) &&
                !TextUtils.isEmpty(hostnameEditText.getText()) &&
                !TextUtils.isEmpty(smbShareEditText.getText()) &&
                !TextUtils.isEmpty(pathEditText.getText()));
    }

    private void onSendResult()
    {
        if(callback == null) return;

        Library.Type type = Library.Type.IMAGE_SET;
        if(typeSpinner.getSelectedItem().toString().equals(getString(R.string.dialog_new_library_type_movie)))
        {
            type = Library.Type.MOVIE;
        }

        callback.onSend(type, nameEditText.getText().toString(),
                hostnameEditText.getText().toString(), smbShareEditText.getText().toString(),
                pathEditText.getText().toString(), usernameEditText.getText().toString(),
                passwordEditText.getText().toString());
    }

    public void setCallback(@NonNull Callback callback) { this.callback = callback; }

    public interface Callback
    {
        void onSend(Library.Type type, String name, String hostname, String smbShare,
                    String path, String username, String password);
    }
}
