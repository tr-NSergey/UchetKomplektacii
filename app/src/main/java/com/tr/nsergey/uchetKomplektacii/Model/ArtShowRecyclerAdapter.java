package com.tr.nsergey.uchetKomplektacii.Model;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tr.nsergey.uchetKomplektacii.R;

import java.util.ArrayList;
import java.util.List;

public class ArtShowRecyclerAdapter extends RecyclerView.Adapter<ArtShowRecyclerAdapter.ViewHolder> {

    private List<ArtObject> artObjects;

    public ArtShowRecyclerAdapter(List<ArtObject> artObjects) {
        this.artObjects = new ArrayList<>();
        //hack for header
        if (artObjects != null) {
            this.artObjects.add(null);
            this.artObjects.addAll(artObjects);
        }
    }

    public void onNext(List<ArtObject> artObjects) {
        this.artObjects = new ArrayList<>();
        //hack for header
        if (artObjects != null) {
            this.artObjects.add(null);
            this.artObjects.addAll(artObjects);
        }
        notifyDataSetChanged();
    }

    final static class ViewHolder extends RecyclerView.ViewHolder {
        TextView artModification;
        TextView artOldQuantity;
        TextView artQuantity;

        ViewHolder(View view) {
            super(view);
            artModification = (TextView) view.findViewById(R.id.artModification);
            artOldQuantity = (TextView) view.findViewById(R.id.artOldQuantity);
            artQuantity = (TextView) view.findViewById(R.id.artQuantity);
        }
    }

    @Override
    public ArtShowRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.art_show_item, parent, false);
        return new ArtShowRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ArtShowRecyclerAdapter.ViewHolder holder, int position) {
        if (position == 0) {
            boolean hasQty = false;
            boolean hasMod = false;
            boolean hasOldQty = false;
            for(int i = 1; i < artObjects.size(); i++){
                if (!artObjects.get(i).getQuantity().equals("")) {
                    hasQty = true;
                }
                if (!artObjects.get(i).getModification().equals("")) {
                    hasMod = true;
                }
                if(!artObjects.get(i).getOldQuantity().equals("")){
                    hasOldQty = true;
                }
            }
            if(hasMod) {
                holder.artModification.setText(Html.fromHtml("<b>Модификация</b>"));
            }
            if(hasOldQty) {
                holder.artOldQuantity.setText(Html.fromHtml("<b>Было</b>"));
            }
            if(hasQty) {
                if(hasOldQty) {
                    holder.artQuantity.setText(Html.fromHtml("<b>Стало</b>"));
                } else {
                    holder.artQuantity.setText(Html.fromHtml("<b>Количество</b>"));
                }
            }
        } else {
            holder.artModification.setText(artObjects.get(position).getModification());
            holder.artOldQuantity.setText(artObjects.get(position).getOldQuantity());
            holder.artQuantity.setText(artObjects.get(position).getQuantity());
        }
    }

    @Override
    public int getItemCount() {
        return artObjects == null ? 0 : artObjects.size();
    }
}