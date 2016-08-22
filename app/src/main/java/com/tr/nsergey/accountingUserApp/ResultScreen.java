package com.tr.nsergey.accountingUserApp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ResultScreen extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String MESSAGE_PARAM = "message";
    private OnResultScreenInteractionListener mListener;
    private String infoMessage;

    @Bind(R.id.infoTextViewRS)
    TextView infoTextView;
    @Bind(R.id.backToMain)
    Button backToMain;

    public ResultScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ResultScreen.
     */
    public static ResultScreen newInstance(String message) {
        ResultScreen fragment = new ResultScreen();
        Bundle args = new Bundle();
        args.putString(MESSAGE_PARAM, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //only interested in the first restore state
        if (getArguments() != null && infoMessage == null) {
            infoMessage = getArguments().getString(MESSAGE_PARAM);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (infoTextView != null) {
            infoTextView.setText(Html.fromHtml(infoMessage));
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnResultScreenInteractionListener) {
            mListener = (OnResultScreenInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnResultScreenInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_result, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        infoTextView.setText(infoMessage);
        backToMain.setOnClickListener(v -> mListener.onBackToMainClick());
    }

    public void setMessage(String message) {
        infoMessage = message;
    }

    public interface OnResultScreenInteractionListener {
        void onBackToMainClick();
    }
}
