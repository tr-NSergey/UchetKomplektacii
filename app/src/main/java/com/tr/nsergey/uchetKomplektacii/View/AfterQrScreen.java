package com.tr.nsergey.uchetKomplektacii.View;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tr.nsergey.uchetKomplektacii.App;
import com.tr.nsergey.uchetKomplektacii.Model.ArtObject;
import com.tr.nsergey.uchetKomplektacii.Model.ArtEditRecyclerAdapter;
import com.tr.nsergey.uchetKomplektacii.OpNames;
import com.tr.nsergey.uchetKomplektacii.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AfterQrScreen extends Fragment implements RecyclerView.OnItemTouchListener {

    private OnAfterQrInteractionListener mListener;
    @BindView(R.id.infoTextViewAQR)
    protected TextView infoLabel;
    @BindView(R.id.recyclerView)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.sendRequestButton)
    protected Button sendRequestButton;

    private ArtEditRecyclerAdapter artEditRecyclerAdapter;
    private GestureDetectorCompat gestureDetector;
    private List<ArtObject> possibleArts;

    public AfterQrScreen() {
        // Required empty public constructor
    }

    public static AfterQrScreen newInstance() {
        AfterQrScreen fragment = new AfterQrScreen();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        artEditRecyclerAdapter = new ArtEditRecyclerAdapter(null);
        mRecyclerView.setAdapter(artEditRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(getActivity(), new RecyclerViewOnGestureListener());
        sendRequestButton.setText(OpNames.get(possibleArts.get(0).getFunction()));
        sendRequestButton.setOnClickListener(v -> {
            List<ArtObject> artObjects = new ArrayList<>();
            for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
                View view = mRecyclerView.getChildAt(i);
                EditText mQty = (EditText) view.findViewById(R.id.artEditText);
                String qty = mQty.getText().toString();
                if (!qty.equals("")) {
                    ArtObject selectedObject = (ArtObject) view.getTag();
                    try {
                        selectedObject.setQuantity(Integer.valueOf(qty));
                        artObjects.add(selectedObject);
                    } catch (NumberFormatException e) {
                    }
                }
            }
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null :
                    getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            mListener.onSendRequestClick(artObjects);
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
        this.artEditRecyclerAdapter.onNext(this.possibleArts);
        if (infoLabel != null) {
            infoLabel.setText(String.format("%1$s: %2$s (склад - %3$s)", this.possibleArts.get(0).getArt(),
                    this.possibleArts.get(0).getName(), this.possibleArts.get(0).getLocation()));
        }
        if (sendRequestButton != null) {
            sendRequestButton.setText(OpNames.get(possibleArts.get(0).getFunction()));
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

    public void setPossibleArts(List<ArtObject> artObjects) {
        this.possibleArts = new ArrayList<>(artObjects);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
//            View view = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
//            if (view != null) {
//                TextView artTextView = (TextView) view.findViewById(R.id.artTextView);
//                if (event.getY() >= artTextView.getTop() + view.getTop() &&
//                        event.getY() <= artTextView.getBottom() + view.getTop()) {
//                    EditText editText = (EditText) view.findViewById(R.id.artEditText);
//                    if (editText.getVisibility() == View.GONE) {
//                        editText.setVisibility(View.VISIBLE);
//                        artTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.chevron_up), null);
//                        artTextView.setBackgroundColor(getResources().getColor(R.color.colorButtonBackgroundBright));
//                    } else {
//                        editText.setVisibility(View.GONE);
//                        artTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.chevron_down), null);
//                        artTextView.setBackgroundColor(getResources().getColor(R.color.colorButtonBackground));
//                    }
//                }
//            }
            return super.onSingleTapConfirmed(event);
        }

        @Override
        public void onLongPress(MotionEvent event) {
            super.onLongPress(event);
        }
    }

    public interface OnAfterQrInteractionListener {
        void onSendRequestClick(List<ArtObject> artObjects);
    }
}
