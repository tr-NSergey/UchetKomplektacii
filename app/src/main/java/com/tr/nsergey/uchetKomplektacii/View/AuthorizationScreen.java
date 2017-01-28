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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tr.nsergey.uchetKomplektacii.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;


public class AuthorizationScreen extends Fragment {

    private OnAuthScreenInteractionListener mListener;

    @BindView(R.id.firstRow)
    protected LinearLayout firstRow;
    @BindView(R.id.firstCircle)
    protected ImageView firstCircle;
    @BindView(R.id.secondCircle)
    protected ImageView secondCircle;
    @BindView(R.id.thirdCircle)
    protected ImageView thirdCircle;
    @BindViews({R.id.oneBtn, R.id.twoBtn, R.id.threeBtn, R.id.fourBtn, R.id.fiveBtn, R.id.sixBtn,
            R.id.sevenBtn, R.id.eightBtn, R.id.nineBtn, R.id.cancelBtn, R.id.zeroBtn, R.id.deleteBtn})
    protected List<Button> buttons;
    private List<ImageView> circles;
    private String password;

    public AuthorizationScreen() {
        // Required empty public constructor
    }

    public static AuthorizationScreen newInstance() {
        return new AuthorizationScreen();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        password = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_authorisation_screen, container, false);
        ButterKnife.bind(this, view);
        ButterKnife.apply(buttons, ((view1, index) -> {
            view1.setOnClickListener(v -> receivePwdDigit(((Button) v).getText().toString()));
        }));
        circles = new ArrayList<>(3);
        circles.add(firstCircle);
        circles.add(secondCircle);
        circles.add(thirdCircle);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_authorisation_screen, menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAuthScreenInteractionListener) {
            mListener = (OnAuthScreenInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAuthScreenInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void receivePwdDigit(String text) {
        switch (text) {
            case "ОТМЕНА":
                for (int i = 0; i < password.length(); i++) {
                    dimCircle(i + 1);
                }
                password = "";
                break;
            case "Х":
                if (password.length() > 0) {
                    dimCircle(password.length());
                    password = password.substring(0, password.length() - 1);
                }
                break;
            default:
                if (password.length() < 3) {
                    password += text;
                    lightupCircle(password.length());
                }
                if (password.length() == 3) {
                    String userName = mListener.checkUser(password);
                    password = "";
                    for (int i = 0; i < 3; i++) {
                        dimCircle(i + 1);
                    }
                    if (userName.equals("")) {
                        Toast.makeText(getContext(), R.string.noUserForPassword, Toast.LENGTH_SHORT).show();
                    } else {
                        mListener.onAuthSuccess(userName);
                    }
                }
                break;
        }
    }

    private void dimCircle(int num) {
        circles.get(num - 1).setImageDrawable(getResources().getDrawable(R.drawable.before_input));
    }

    private void lightupCircle(int num) {
        circles.get(num - 1).setImageDrawable(getResources().getDrawable(R.drawable.after_input));
    }

    protected interface OnAuthScreenInteractionListener {
        String checkUser(String userPwd);
        void onAuthSuccess(String userName);
    }
}
