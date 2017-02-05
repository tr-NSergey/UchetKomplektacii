package com.tr.nsergey.uchetKomplektacii.Model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.tr.nsergey.uchetKomplektacii.R;

import java.util.List;

public class ArtEditRecyclerAdapter extends RecyclerView.Adapter<ArtEditRecyclerAdapter.ViewHolder>{

    private List<ArtObject> artObjects;

    public ArtEditRecyclerAdapter(List<ArtObject> artObjects) {
        this.artObjects = artObjects;
    }

    public void onNext(List<ArtObject> artObjects) {
        this.artObjects = artObjects;
        notifyDataSetChanged();
    }

    final static class ViewHolder extends RecyclerView.ViewHolder {
        TextView artTextView;
        EditText artEditText;

        ViewHolder(View view) {
            super(view);
            artTextView = (TextView) view.findViewById(R.id.artTextView);
            artEditText = (EditText) view.findViewById(R.id.artEditText);
        }
    }

    @Override
    public ArtEditRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.art_button, parent, false);
        return new ArtEditRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ArtEditRecyclerAdapter.ViewHolder holder, int position) {
        holder.itemView.setTag(artObjects.get(position));
        holder.artTextView.setText(String.format("%1$s %2$s (%3$s шт.)",
                artObjects.get(position).getArt(),
                artObjects.get(position).getModification(),
                artObjects.get(position).getQuantity()));
    }

    @Override
    public int getItemCount() {
        return artObjects ==null? 0 : artObjects.size();
    }
}