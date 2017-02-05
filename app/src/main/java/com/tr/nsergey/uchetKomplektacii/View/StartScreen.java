package com.tr.nsergey.uchetKomplektacii.View;

import android.content.Context;
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
import com.tr.nsergey.uchetKomplektacii.App;
import com.tr.nsergey.uchetKomplektacii.R;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import rx.Observer;

import static com.tr.nsergey.uchetKomplektacii.App.I_MODE_MANUAL;
import static com.tr.nsergey.uchetKomplektacii.App.I_MODE_QR;

public class StartScreen extends Fragment {

    private static final String USERNAME = "username";

    @BindViews({R.id.addQtyButton,
            R.id.removeQtyButton,
            R.id.remainsButton,
            R.id.checkButton})
    protected List<Button> buttons;
    @BindView(R.id.userName)
    protected TextView userNameTextView;
    @BindView(R.id.qrMode)
    protected Button qrModeButton;
    @BindView(R.id.manualMode)
    protected Button manualModeButton;

    private String userName;

    private Observer<Integer> mListener;

    public StartScreen() {
        // Required empty public constructor
    }

    public static StartScreen newInstance(String userName) {
        StartScreen fragment = new StartScreen();
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
    public void onResume() {
        super.onResume();
        switchMode(App.INPUT_MODE);
        userNameTextView.setText(userName);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        for (Button button : buttons) {
            RxView.clicks(button)
                    .map(aVoid -> button.getId())
                    .subscribe(mListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_screen, container, false);
        ButterKnife.bind(this, view);
        qrModeButton.setOnClickListener((v -> switchMode(I_MODE_QR)));
        manualModeButton.setOnClickListener((v -> switchMode(I_MODE_MANUAL)));
        return view;
    }

    private void switchMode(String mode) {
        if(mode.equals(I_MODE_QR)) {
            App.INPUT_MODE = mode;
            qrModeButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            qrModeButton.setTextColor(getResources().getColor(R.color.brightText));
            manualModeButton.setBackgroundColor(getResources().getColor(R.color.mainBackground));
            manualModeButton.setTextColor(getResources().getColor(R.color.darkText));
        } else if(mode.equals(I_MODE_MANUAL)) {
            App.INPUT_MODE = mode;
            manualModeButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            manualModeButton.setTextColor(getResources().getColor(R.color.brightText));
            qrModeButton.setBackgroundColor(getResources().getColor(R.color.mainBackground));
            qrModeButton.setTextColor(getResources().getColor(R.color.darkText));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_start_screen, menu);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
