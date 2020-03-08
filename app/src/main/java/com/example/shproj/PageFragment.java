package com.example.shproj;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import java.util.Calendar;

import static com.example.shproj.MainActivity.log;


public class PageFragment extends Fragment {

    Calendar c;

    public PageFragment() {
        // Required empty public constructor
        log("constructor");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("oncreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        log("oncreateview");
        View v = inflater.inflate(R.layout.fragment_page, container, false);
//        ((TextView) v.findViewById(R.id.test)).setText(new Date(c.getTimeInMillis()).toString());
        return v;
    }
}
