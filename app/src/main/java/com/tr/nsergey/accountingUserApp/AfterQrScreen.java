package com.tr.nsergey.accountingUserApp;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.logging.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAfterQrInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AfterQrScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AfterQrScreen extends Fragment {

    private static final String MESSAGE_PARAM = "message";
    private static final String BTN_NAME_PARAM = "btnname";

    private OnAfterQrInteractionListener mListener;
    @Bind(R.id.infoTextViewAQR)
    protected TextView infoLabel;
    @Bind(R.id.mQtyEditText)
    protected EditText mQty;
    @Bind(R.id.sendRequestButton)
    protected Button sendRequestButton;

    private String infoMessage;
    private String btnName;

    public AfterQrScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AfterQrScreen.
     */
    public static AfterQrScreen newInstance(String message, String buttonName) {
        AfterQrScreen fragment = new AfterQrScreen();
        Bundle args = new Bundle();
        args.putString(MESSAGE_PARAM, message);
        args.putString(BTN_NAME_PARAM, buttonName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //only interested in the first restore state
        if (getArguments() != null && infoMessage == null) {
            infoMessage = getArguments().getString(MESSAGE_PARAM);
            btnName = getArguments().getString(BTN_NAME_PARAM);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        infoLabel.setText(infoMessage);
        sendRequestButton.setText(btnName);
        sendRequestButton.setOnClickListener(v -> {
            String qty = mQty.getText().toString();
            try {
                Integer.valueOf(mQty.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Введите число!", Toast.LENGTH_SHORT).show();
                return;
            }
            mQty.setText("");
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null :
                    getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            mListener.onSendRequestClick(qty);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_after_qr_screen, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mQty != null) {
            mQty.setText("");
        }
        if (infoLabel != null) {
            infoLabel.setText(infoMessage);
        }
        if (sendRequestButton != null) {
            sendRequestButton.setText(btnName);
        }
        /*
        InputMethodManager imm = (InputMethodManager)mQty.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null :
                getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        mQty.requestFocus();
        imm.showSoftInput(mQty, InputMethodManager.SHOW_IMPLICIT);
        */
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_after_qr_screen, menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAfterQrInteractionListener) {
            mListener = (OnAfterQrInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAfterQrInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setMessage(String message, String buttonName) {
        infoMessage = message;
        btnName = buttonName;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAfterQrInteractionListener {
        void onSendRequestClick(String quantity);
    }
}
