package com.joogat.elevateddock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;

import com.joogat.logger.L;


public abstract class Popup {

    private View view;

    private ElevatedDock elevatedDock;

    private boolean isViewInflated, isAttached = false;


    public void attach(ElevatedDock container){

        if(isAttached) return;

        isViewInflated = false;
        isAttached = true;

        elevatedDock = container;

        view = LayoutInflater.from(container.getContext()).inflate( getLayoutResourceId(), container, false);

        container.addView(view);

        initialize(view);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                L.e("inflated layout = inside popup");
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                isViewInflated = true;
            }
        });
    }

    public abstract void initialize(View view);

    public void setViewInflated(boolean viewInflated) {
        isViewInflated = viewInflated;
    }

    public boolean isViewInflated() {
        return isViewInflated;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public abstract int getLayoutResourceId();

    public void dismiss(){
        elevatedDock.dismiss();
    }

    public void dismissToShow(String popupId){
        elevatedDock.dismissToShow(popupId);
    }
}