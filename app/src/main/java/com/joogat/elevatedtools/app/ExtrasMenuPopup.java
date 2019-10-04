package com.joogat.elevatedtools.app;

import android.view.View;

import com.joogat.elevatedtools.Popup;
import com.joogat.logger.L;

public class ExtrasMenuPopup extends Popup implements View.OnClickListener {

    public static final String ID = "ExtrasMenuPopup";

    @Override public int getLayoutResourceId() { return R.layout.menu_extras; }

    @Override
    public void initialize(View view) {
        view.findViewById(R.id.item_1).setOnClickListener(this);
        view.findViewById(R.id.item_2).setOnClickListener(this);
        view.findViewById(R.id.item_3).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.item_1:
                dismissToShow(MainMenuPopup.ID);
                L.e("Show Main Menu");
                break;

            case R.id.item_2:
                dismiss();
                break;

            case R.id.item_3:
                L.e("Extra Menu Popup Item 3");
                break;
        }
    }
}