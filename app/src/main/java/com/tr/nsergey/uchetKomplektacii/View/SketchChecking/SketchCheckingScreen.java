package com.tr.nsergey.uchetKomplektacii.View.SketchChecking;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.tr.nsergey.uchetKomplektacii.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;

public class SketchCheckingScreen extends Fragment {

    private static final String USERNAME = "username";

    @BindView(R.id.scsUserName)
    protected TextView scsUserName;
    @BindView(R.id.checkSketch)
    protected Button checkSketchButton;
    private String userName;

    private Observer<Integer> mListener;

    public SketchCheckingScreen() {
        // Required empty public constructor
    }

    public static SketchCheckingScreen newInstance(String userName) {
        SketchCheckingScreen fragment = new SketchCheckingScreen();
        Bundle args = new Bundle();
        args.putString(USERNAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userName = getArguments().getString(USERNAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sketch_checking_screen, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RxView.clicks(checkSketchButton)
                .map(aVoid -> checkSketchButton.getId())
                .subscribe(mListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        scsUserName.setText(userName);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Observer) {
            mListener = (Observer<Integer>) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Observer<Integer>");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_sketch_checking_screen, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
