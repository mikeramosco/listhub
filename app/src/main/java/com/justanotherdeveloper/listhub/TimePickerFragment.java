package com.justanotherdeveloper.listhub;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment {

    private int hour, minute, theme;

    public TimePickerFragment(int hour, int minute, int theme) {
        this.hour = hour;
        this.minute = minute;
        this.theme = theme;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(requireActivity(), theme,
                (TimePickerDialog.OnTimeSetListener) getActivity(),
                hour, minute, android.text.format.DateFormat.is24HourFormat(requireContext()));
    }
}
