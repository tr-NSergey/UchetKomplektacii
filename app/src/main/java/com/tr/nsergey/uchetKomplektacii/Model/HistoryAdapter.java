package com.tr.nsergey.uchetKomplektacii.Model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tr.nsergey.uchetKomplektacii.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observer;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> implements Observer<Void>{
    private List<ArtObject> requestObjects;
    public HistoryAdapter(){
        requestObjects = new ArrayList<>();
    }

    public List<ArtObject> getList() {
        return requestObjects;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView mainText;
        TextView secondaryText;
        public ViewHolder(View itemView) {
            super(itemView);
            mainText = (TextView)itemView.findViewById(R.id.mainText);
            secondaryText = (TextView)itemView.findViewById(R.id.secondaryText);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);
        return new HistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ArtObject obj = this.requestObjects.get(position);
        String mainText = obj.getFunction() + " " + obj.getName() + " " + obj.getQuantity();
        holder.mainText.setText(mainText);
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM HH:mm");
        String dateString = formatter.format(new Date(Integer.valueOf(obj.getOldQuantity()) * 1000L));
        String secondaryText = obj.getUserName() + " " + dateString;
        holder.secondaryText.setText(secondaryText);
    }

    @Override
    public int getItemCount() {
        return requestObjects.size();
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        this.requestObjects.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onNext(Void v) {
        notifyDataSetChanged();
    }
}
