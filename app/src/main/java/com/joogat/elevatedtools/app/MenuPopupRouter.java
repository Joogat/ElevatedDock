package com.joogat.elevatedtools.app;

import com.joogat.elevatedtools.PopupRouter;
import com.joogat.elevatedtools.Popup;

public class MenuPopupRouter extends PopupRouter {

    public MainMenuPopup mainMenuPopup;

    public MainMenuPopup getMainMenuPopup() {
        if(mainMenuPopup == null) mainMenuPopup = new MainMenuPopup();
        return mainMenuPopup;
    }

    public ExtrasMenuPopup extrasMenuPopup;

    public ExtrasMenuPopup getExtrasMenuPopup() {
        if(extrasMenuPopup == null) extrasMenuPopup = new ExtrasMenuPopup();
        return extrasMenuPopup;
    }

    @Override
    public Popup getPopup(String popupId) {

        switch (popupId){
            case MainMenuPopup.ID: return getMainMenuPopup();
            case ExtrasMenuPopup.ID: return getExtrasMenuPopup();
            default: return null;
        }
    }
}
