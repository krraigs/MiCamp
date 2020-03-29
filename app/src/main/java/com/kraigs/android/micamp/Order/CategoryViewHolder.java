package com.kraigs.android.micamp.Order;

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.kraigs.android.micamp.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class CategoryViewHolder extends GroupViewHolder {

    private TextView genreName;
    private ImageView arrow;

    public CategoryViewHolder(View itemView) {
        super(itemView);
        genreName = (TextView) itemView.findViewById(R.id.categoryTv);
        arrow = (ImageView) itemView.findViewById(R.id.categoryView);
//        image = (ImageView) itemView.findViewById(R.id.image);
    }

    public void setGenreTitle(ExpandableGroup genre) {
        if (genre instanceof Category) {
            genreName.setText(genre.getTitle());


//            if (((Category) genre).getType() != null){
//                if (((Category) genre).getType().equals("Veg")){
//                    image.setColorFilter(Color.GREEN);
//
//                } else if (((Category) genre).getType().equals("NonVeg")){
//                    image.setColorFilter(Color.RED);
//
//                }
//
//            } else{
//                image.setVisibility(View.GONE);
//            }
        }
    }

    @Override
    public void expand() {
        animateExpand();
    }

    @Override
    public void collapse() {
        animateCollapse();
    }

    private void animateExpand() {
        RotateAnimation rotate =
                new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.setAnimation(rotate);
    }

    private void animateCollapse() {
        RotateAnimation rotate =
                new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.setAnimation(rotate);
    }

}
