package com.joogat.elevateddock;


import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.joogat.helpers.SoftInputHelper;
import com.joogat.helpers.SystemBarHelper;
import com.joogat.helpers.ViewHelper;

import androidx.core.view.ViewCompat;

// We are not extending ScrollView instead of FrameLayout is for pageActivity reason.
// FrameLayout can have multiple direct child where ScrollView can only have on direct child.
// Mainly we require content cover for ElevatedDock alongside the inflated menu or popup ScrollView is not suitable as ElevatedDock.
// And some of the styling aspects which are not that major but still should be considered.

public class ElevatedDock extends FrameLayout{

    private boolean isActive = false;
    private boolean remainActive = false;

    private int gapFromLeft, gapFromRight, gapFromTop, gapFromBottom, defaultGap;
    private int currentGravity = Gravity.NO_GRAVITY;
    private int contentCoverBackground;

    private int animationDuration;

    private View currentAnchor, currentPopupView;
    private View oldPopupView, contentCover;

    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

    private SystemBarHelper systemBarHelper;

    private PopupRouter popupRouter;
    private Popup currentPopup;


    private ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            currentPopupView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            currentPopup.setViewInflated(true);
            setLocation();
            showIn();
        }
    };


    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            int viewId = v.getId();

            if (viewId == contentCover.getId()){
                dismiss();
            }
        }
    };




    public ElevatedDock(Context context) {
        super(context);
        init(context, null, 0, 0);
    }


    public ElevatedDock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }


    public ElevatedDock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ElevatedDock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {


        if (attrs != null) {

            TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.ElevatedDock);

            animationDuration = a.getInteger(R.styleable.ElevatedDock_animationDuration, 180);
            contentCoverBackground = a.getColor(R.styleable.ElevatedDock_contentCoverBackground, 0x77000000);

            a.recycle();
        }

        defaultGap = getResources().getDimensionPixelSize(R.dimen.default_gap_from_window);

        contentCover = new View(getContext());
        contentCover.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        contentCover.setAlpha(0);
        contentCover.setBackgroundColor(contentCoverBackground);
        contentCover.setClickable(false);
        //contentCover.setOnClickListener(clickListener); // We are setting it after animation completes
        addView(contentCover);

        systemBarHelper = new SystemBarHelper((Activity) getContext());
        systemBarHelper.setDuration(animationDuration);
    }



    private void showPopupUsingLayoutListener(View popup){
        popup.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }


    public void setCurrentPopup(String popupId){
        currentPopup = popupRouter.getPopup(popupId);
        currentPopup.attach(this);
        currentPopupView = currentPopup.getView();
    }

    public void inflatePopup(String popupId){
        Popup popup = popupRouter.getPopup(popupId);
        popup.attach(this);
    }




    public boolean isActive(){
        return isActive;
    }


    public void setGap(int[] gap){

        gapFromLeft     = gap[0];
        gapFromTop      = gap[1];
        gapFromRight    = gap[2];
        gapFromBottom   = gap[3];

        if (ViewCompat.getLayoutDirection( this ) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            gapFromLeft = gap[2];
            gapFromRight = gap[0];
        }

        if(gap.length == 4){
            defaultGap = defaultGap;
        } else {
            defaultGap = gap[4]; // This has some thing wrongly wired
        }
    }

    public void show(String popupId){
        show(popupId, Gravity.CENTER);
    }

    public void show(String popupId, int gravity) {
        show( popupId, gravity, defaultGap);
    }

    public void show(String popupId, int gravity, int gap){
        gapFromLeft = gapFromRight = gapFromTop = gapFromBottom = gap;
        showOn( popupId, gravity);
    }

    public void show(String popupId, int gravity, int[] gap){
        setGap(gap);
        showOn( popupId, gravity );
    }

    private void showOn(String popupId, int gravity){

        setCurrentPopup(popupId);
        currentAnchor = null;
        currentGravity = gravity;

        if(currentPopup.isViewInflated()) {
            setLocation(gravity);
            showIn();
        } else {
            showPopupUsingLayoutListener(currentPopupView);
        }
    }




    public void show(View anchor, String popupId) {
        show(anchor, popupId, defaultGap);
    }

    public void show(View anchor, String popupId, int gap){
        gapFromLeft = gapFromRight = gapFromTop = gapFromBottom = gap;
        showOn( anchor, popupId );
    }

    public void show(View anchor, String popupId, int[] gap){
        setGap(gap);
        showOn( anchor, popupId );
    }


    private void showOn(View anchor, String popupId){

        setCurrentPopup(popupId);
        currentGravity = Gravity.NO_GRAVITY;
        currentAnchor = anchor;

        if(currentPopup.isViewInflated()) {
            setLocation(anchor);
            showIn();
        } else {
            showPopupUsingLayoutListener(currentPopupView);
        }
    }



    public void dismissToShow(String popupId){
        dismissToShow();
        show(popupId, Gravity.CENTER);
    }

    public void dismissToShow(String popupId, int gravity) {
        dismissToShow();
        show(popupId, gravity, defaultGap);
    }

    public void dismissToShow(String popupId, int gravity, int gap){
        dismissToShow();
        show(popupId, gravity, gap);
    }

    public void dismissToShow(String popupId, int gravity, int[] gap){
        dismissToShow();
        show(popupId, gravity, gap);
    }






    public void setPopupRouter(PopupRouter popupRouter){
        this.popupRouter = popupRouter;
    }



    private void setLocation(){
        if(currentAnchor != null){
            setLocation(currentAnchor);
        } else if(currentGravity != Gravity.NO_GRAVITY){
            setLocation(currentGravity);
        } else {
            setLocation(Gravity.CENTER);
        }
    }


    private void setLocation(int gravity){

        LayoutParams popupParams = (LayoutParams) currentPopupView.getLayoutParams();

        if(currentPopupView.getHeight() > getHeight() - gapFromTop - gapFromBottom ){
            gapFromTop = defaultGap;
            gapFromBottom = defaultGap;
            if(currentPopupView.getHeight() > getHeight() - gapFromTop - gapFromBottom ) {
                popupParams.height = getHeight() - gapFromTop - gapFromBottom;
            }
        }

        if(currentPopupView.getWidth() > getWidth() - gapFromLeft - gapFromRight ){
            gapFromLeft = defaultGap;
            gapFromRight = defaultGap;
            if(currentPopupView.getWidth() > getWidth() - gapFromLeft - gapFromRight ) {
                popupParams.width = getWidth() - gapFromLeft - gapFromRight;
            }
        }

        popupParams.leftMargin = gapFromLeft;
        popupParams.topMargin = gapFromTop;
        popupParams.rightMargin = gapFromRight;
        popupParams.bottomMargin = gapFromBottom;

        popupParams.gravity = gravity;

        currentPopupView.setLayoutParams(popupParams);
    }


    private void setLocation(View anchor){

        /*
         * We should not use "windowDisplayFrame" as this always return the screen
         * positions and not the activity or app window related positions.
         * While in SPLIT WINDOW mode we require the positions of views as
         * inside the respective window of the respective activity or app.
         * So instead of using the "windowDisplayFrame" we obtain the ROOT rootView
         * of the activity and then get the location of that rootView
         * to position the desired menu respectively.
         */

        //Rect windowDisplayFrame = new Rect();
        //getWindow().getDecorView().getWindowVisibleDisplayFrame(windowDisplayFrame);

        //int rootWidth = Resources.getSystem().getDisplayMetrics().widthPixels; is for complete screen
        //int rootHeight = Resources.getSystem().getDisplayMetrics().heightPixels; is for complete screen

        /**/

        //View root = ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0); // SHOULD BE USED FOR ACTIVITY


        LayoutParams popupParams = (LayoutParams) currentPopupView.getLayoutParams();

        // popupParams.height or popupParams.width MAY give constants -1 and -2 for MATCH_PARENT and WRAP_CONTENT respectively.
        // Hence consider using "currentPopupView.getHeight()" and "currentPopupView.getWidth()" for getting height and width.


        int popupHeight = currentPopupView.getHeight();
        int popupWidth = currentPopupView.getWidth();

        if( popupHeight > getHeight() - gapFromTop - gapFromBottom ){
            gapFromTop = defaultGap;
            gapFromBottom = defaultGap;
            if( popupHeight > getHeight() - gapFromTop - gapFromBottom ) {
                popupHeight = popupParams.height = getHeight() - gapFromTop - gapFromBottom;
            }
        }

        if( popupWidth > getWidth() - gapFromLeft - gapFromRight ){
            gapFromLeft = defaultGap;
            gapFromRight = defaultGap;
            if( popupWidth > getWidth() - gapFromLeft - gapFromRight ) {
                popupWidth = popupParams.width = getWidth() - gapFromLeft - gapFromRight;
            }
        }



        Rect rootCoordinates = new Rect();
        getGlobalVisibleRect(rootCoordinates);

        Rect anchorCoordinates = new Rect();
        anchor.getGlobalVisibleRect(anchorCoordinates);

        int x = anchorCoordinates.left;
        int y = anchorCoordinates.top - rootCoordinates.top; // BECAUSE we are placing rootView using margin we need to subtract status bar height

        int cx = anchorCoordinates.centerX();
        //int cy = anchorCoordinates.centerY();

        /*

        THIS IS VERY IMPORTANT
        IF we use this dual condition which specifically includes -------- "( menuWidth >> 1 ) < cx" ----------- as pageActivity CONDITION
        Then the "x" position of the popup
        will start from the "x" location of the anchor
        if "menuWidth / 2" is less then centerX of anchor

        if(( menuWidth >> 1 ) < cx && (( menuWidth >> 1 ) + cx ) < rootCoordinates.right ) {
            x = cx - ( menuWidth >> 1 );
        }

        --- If we use the below mentioned CONDITION --- then we get the desired result of positioning the popup.

        if( (( menuWidth >> 1 ) + cx ) < rootCoordinates.right ) {
            x = cx - ( menuWidth >> 1 );
        }

        */

        ///////////// ORDER of the CONDITIONS is very important

        if( (( popupWidth >> 1 ) + cx ) < rootCoordinates.right ) {
            x = cx - ( popupWidth >> 1 );
        }

        if( x < gapFromLeft ){
            x = gapFromLeft;
        }

        if( y < rootCoordinates.top + gapFromTop ){
            y = gapFromTop;
        }

        if( rootCoordinates.right < x + popupWidth + gapFromRight ){
            x = rootCoordinates.right - popupWidth - gapFromRight;
        }

        if( rootCoordinates.bottom < y + popupHeight + gapFromBottom ){
            y = rootCoordinates.bottom - popupHeight - rootCoordinates.top - gapFromBottom;
        }

        popupParams.leftMargin = x;
        popupParams.topMargin = y;

        popupParams.rightMargin = 0;
        popupParams.bottomMargin = 0;

        popupParams.gravity = Gravity.NO_GRAVITY;

        currentPopupView.setLayoutParams(popupParams);
    }



    private void showIn(){

        if(isActive) return;

        if(!remainActive) systemBarHelper.animateColorTo(contentCoverBackground);

        isActive = true;
        remainActive = true;

        setVisibility(View.VISIBLE);
        currentPopupView.setVisibility(View.VISIBLE);

        ViewHelper.setClickable(currentPopupView, true);

        contentCover.setClickable(true);

        contentCover.animate().setDuration(animationDuration).alpha(1)
                .setInterpolator(decelerateInterpolator);

        currentPopupView.animate().setDuration(animationDuration)
                .alpha(1).scaleX(1f).scaleY(1f)
                .setInterpolator(decelerateInterpolator)
                .setListener(inAnimationListener);

        SoftInputHelper.hideSoftInput(getContext(), this);
    }



    public void dismiss(){

        if(!isActive) return;

        isActive = false;
        remainActive = false;

        contentCover.setClickable(false);
        contentCover.setOnClickListener(null);

        ViewHelper.setClickable(currentPopupView, false);

        contentCover.animate().alpha(0).setDuration(animationDuration)
                .setInterpolator(decelerateInterpolator);

        oldPopupView = currentPopupView;

        currentPopupView.animate().setDuration(animationDuration)
                .alpha(0).scaleX(0.9f).scaleY(0.9f)
                .setInterpolator(decelerateInterpolator)
                .setListener(outAnimationListener);

        currentPopupView = null;

        systemBarHelper.animateColorFrom(contentCoverBackground);

    }




    private void dismissToShow(){

        if(!isActive) return;

        isActive = false;
        remainActive = true;

        contentCover.setOnClickListener(null);

        oldPopupView = currentPopupView;

        currentPopupView.animate().setDuration(animationDuration)
                .alpha(0).scaleX(0.9f).scaleY(0.9f)
                .setInterpolator(decelerateInterpolator)
                .setListener(outToShowAnimationListener);
    }



    private Animator.AnimatorListener inAnimationListener = new Animator.AnimatorListener() {

        @Override public void onAnimationStart(Animator animation){}
        @Override public void onAnimationCancel(Animator animation){}
        @Override public void onAnimationRepeat(Animator animation){}

        @Override
        public void onAnimationEnd(Animator animation) {
            //contentCover.setClickable(true);
            contentCover.setOnClickListener(clickListener);
        }
    };



    private Animator.AnimatorListener outAnimationListener = new Animator.AnimatorListener() {

        @Override public void onAnimationStart(Animator animation){}
        @Override public void onAnimationCancel(Animator animation){}
        @Override public void onAnimationRepeat(Animator animation){}

        @Override
        public void onAnimationEnd(Animator animation) {

            if(remainActive) {
                if(oldPopupView != currentPopupView) {
                    if(oldPopupView != null) oldPopupView.setVisibility(View.INVISIBLE);
                }
            } else {
                if(oldPopupView != null) oldPopupView.setVisibility(View.INVISIBLE); // This produces error of null pointer exception when clicking on contentCover after clicking menuItem with pageActivity click listener having dismiss action
                ElevatedDock.this.setVisibility(View.INVISIBLE);
            }

            oldPopupView = null;
        }
    };



    private Animator.AnimatorListener outToShowAnimationListener = new Animator.AnimatorListener() {

        @Override public void onAnimationStart(Animator animation){}
        @Override public void onAnimationCancel(Animator animation){}
        @Override public void onAnimationRepeat(Animator animation){}

        @Override
        public void onAnimationEnd(Animator animation) {
            if(oldPopupView != null) oldPopupView.setVisibility(View.INVISIBLE);
            oldPopupView = null;
        }
    };

}