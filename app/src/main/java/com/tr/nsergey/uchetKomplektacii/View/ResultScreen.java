package com.tr.nsergey.uchetKomplektacii.View;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tr.nsergey.uchetKomplektacii.Model.ArtEditRecyclerAdapter;
import com.tr.nsergey.uchetKomplektacii.Model.ArtObject;
import com.tr.nsergey.uchetKomplektacii.Model.ArtShowRecyclerAdapter;
import com.tr.nsergey.uchetKomplektacii.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResultScreen extends Fragment {
    private OnResultScreenInteractionListener mListener;
    private List<ArtObject> resultArts;
    private ArtShowRecyclerAdapter artShowRecyclerAdapter;

    @BindView(R.id.infoTextViewRS)
    TextView infoTextView;
    @BindView(R.id.recyclerViewRS)
    RecyclerView mRecyclerView;
    @BindView(R.id.toMainScreen)
    Button toMainScreen;

    public ResultScreen() {
        // Required empty public constructor
    }

    public static ResultScreen newInstance() {
        ResultScreen fragment = new ResultScreen();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (infoTextView != null) {
            artShowRecyclerAdapter.onNext(resultArts);
            infoTextView.setText(String.format("%1$s: %2$s (склад - %3$s)", resultArts.get(0).getArt(),
                    resultArts.get(0).getName(), resultArts.get(0).getLocation()));
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        artShowRecyclerAdapter = new ArtShowRecyclerAdapter(null);
        mRecyclerView.setAdapter(artShowRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        toMainScreen.setOnClickListener(v -> mListener.onToMainScreenClick());
    }

    public void setResultArts(List<ArtObject> resultArts) {
        this.resultArts = resultArts;
    }
    public interface OnResultScreenInteractionListener {
        void onToMainScreenClick();
    }
}
