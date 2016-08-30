package com.tr.nsergey.accountingUserApp;

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

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnStartScreenInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StartScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartScreen extends Fragment {

    private static final String MESSAGE_PARAM = "message";
    private static final String USERNAME = "username";

    @BindView(R.id.userName)
    protected TextView userNameTextView;
    @BindView(R.id.addQtyButton)
    protected Button mAddToWhButton;
    @BindView(R.id.removeQtyButton)
    protected Button mTakeFromWhButton;
    @BindView(R.id.remainsButton)
    protected Button mRemainsButton;
    @BindView(R.id.checkButton)
    protected Button mCheckButton;
    @BindView(R.id.infoTextViewSS)
    protected TextView mInfoTextView;

    private String infoMessage;
    private String userName;

    private OnStartScreenInteractionListener mListener;

    public StartScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StartScreen.
     */
    public static StartScreen newInstance(String message, String userName) {
        StartScreen fragment = new StartScreen();
        Bundle args = new Bundle();
        args.putString(MESSAGE_PARAM, message);
        args.putString(USERNAME, userName);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            infoMessage = getArguments().getString(MESSAGE_PARAM);
            userName = getArguments().getString(USERNAME);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        userNameTextView.setText(userName);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAddToWhButton.setOnClickListener(v -> mListener.onAddToWhClick());
        mTakeFromWhButton.setOnClickListener(v -> mListener.onTakeFromWhClick());
        mRemainsButton.setOnClickListener(v -> mListener.onRemainsClick());
        mCheckButton.setOnClickListener(v -> mListener.onCheckClick());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_screen, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_start_screen, menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStartScreenInteractionListener) {
            mListener = (OnStartScreenInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStartScreenInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void setMessage(String message){
        infoMessage = message;
        if(mInfoTextView != null){
            mInfoTextView.setText(message);
        }
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
    public interface OnStartScreenInteractionListener {
        void onAddToWhClick();
        void onTakeFromWhClick();
        void onRemainsClick();
        void onCheckClick();
    }
}
