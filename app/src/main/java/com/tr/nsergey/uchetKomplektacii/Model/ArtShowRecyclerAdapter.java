package com.tr.nsergey.uchetKomplektacii.Model;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tr.nsergey.uchetKomplektacii.App;
import com.tr.nsergey.uchetKomplektacii.R;

import java.util.ArrayList;
import java.util.List;

public class ArtShowRecyclerAdapter extends RecyclerView.Adapter<ArtShowRecyclerAdapter.ViewHolder> {

    private List<ArtObject> artObjects;
    private boolean hasQty = false;
    private boolean hasMod = false;
    private boolean hasOldQty = false;

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
        LinearLayout artShowItemLayout;

        ViewHolder(View view) {
            super(view);
            artModification = (TextView) view.findViewById(R.id.artModification);
            artOldQuantity = (TextView) view.findViewById(R.id.artOldQuantity);
            artQuantity = (TextView) view.findViewById(R.id.artQuantity);
            artShowItemLayout = (LinearLayout) view.findViewById(R.id.artShowItemLayout);
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
            holder.artShowItemLayout.setBackgroundColor(App.APP_CONTEXT.getResources().getColor(R.color.tableBackgroundDark));
            for(int i = 1; i < artObjects.size(); i++){
                if (!artObjects.get(i).getQuantity().equals("-1")) {
                    hasQty = true;
                }
                if (!artObjects.get(i).getModification().equals("")) {
                    hasMod = true;
                }
                if(!artObjects.get(i).getOldQuantity().equals("-1")){
                    hasOldQty = true;
                }
            }
            if(hasMod) {
                holder.artModification.setText(Html.fromHtml("<b>Мод.</b>"));
            }
            if(hasOldQty) {
                holder.artOldQuantity.setText(Html.fromHtml("<b>Было</b>"));
                holder.artOldQuantity.setVisibility(View.VISIBLE);
            } else {
                holder.artOldQuantity.setVisibility(View.GONE);
            }
            if(hasQty) {
                if(hasOldQty) {
                    holder.artQuantity.setText(Html.fromHtml("<b>Стало</b>"));
                } else {
                    holder.artQuantity.setText(Html.fromHtml("<b>Кол.</b>"));
                }
            }
        } else {
            if(hasMod) {
                holder.artModification.setText(artObjects.get(position).getModification());
            }
            if(hasOldQty) {
                holder.artOldQuantity.setText(artObjects.get(position).getOldQuantity());
                holder.artOldQuantity.setVisibility(View.VISIBLE);
            } else {
                holder.artOldQuantity.setVisibility(View.GONE);
            }
            if(hasQty) {
                holder.artQuantity.setText(artObjects.get(position).getQuantity());
            }
        }
    }

    @Override
    public int getItemCount() {
        return artObjects == null ? 0 : artObjects.size();
    }
}