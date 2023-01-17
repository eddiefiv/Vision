package com.example.vision;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.settings, container, false);

        Button testBtn = (Button) view.findViewById(R.id.testButton);
        testBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        // Check which button is being clicked
        switch (v.getId()) {
            case R.id.testButton:
                MainActivity.getInstance().testChangeFragment();
                break;
        }
    }
}