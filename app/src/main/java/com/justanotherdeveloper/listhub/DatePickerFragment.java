package com.justanotherdeveloper.listhub;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    private int year, month, day, theme;
    private Calendar min = null;
    private Calendar max = null;

    public DatePickerFragment(Calendar date, int theme) {
        this.theme = theme;

        year = date.get(Calendar.YEAR);
        month = date.get(Calendar.MONTH);
        day = date.get(Calendar.DAY_OF_MONTH);
    }

    public DatePickerFragment(Calendar date, Calendar min, Calendar max, int theme) {
        this(date, theme);
        this.min = min;
        this.max = max;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog dpd = new DatePickerDialog(requireActivity(), theme,
                (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);

        if(min != null) dpd.getDatePicker().setMinDate(min.getTimeInMillis());
        if(max != null) dpd.getDatePicker().setMaxDate(max.getTimeInMillis());

        return dpd;
    }
}
