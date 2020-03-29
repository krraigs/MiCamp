package com.kraigs.android.micamp.Order;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kraigs.android.micamp.R;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

public class ItemViewHolder extends ChildViewHolder {

    public TextView nameTv,priceTv,qtyTv;
    Button addBt;
    RelativeLayout qtyRl;
    ImageView addQtyBt,subBt;

    public ItemViewHolder(View itemView) {
        super(itemView);
        nameTv = (TextView) itemView.findViewById(R.id.list_item_name);
        priceTv = itemView.findViewById(R.id.priceTv);
        addBt = itemView.findViewById(R.id.addItemBt);
        addQtyBt = itemView.findViewById(R.id.addQtyBt);
        subBt = itemView.findViewById(R.id.subBt);
        qtyTv = itemView.findViewById(R.id.qtyTv);
        qtyRl = itemView.findViewById(R.id.qtyRl);

    }
}
