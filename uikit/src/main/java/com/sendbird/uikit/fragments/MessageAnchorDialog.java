package com.sendbird.uikit.fragments;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.sendbird.uikit.R;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;

class MessageAnchorDialog {
    private final static Handler mainHandler = new Handler(Looper.getMainLooper());
    private final View anchorView;
    private final View parent;
    private final DialogListItem[] items;
    private OnItemClickListener<Integer> itemClickListener;
    private final PopupWindow window;
    private final Context context;

    private MessageAnchorDialog(@NonNull View anchorView, @NonNull View parent, @NonNull DialogListItem[] items) {
        this.context = anchorView.getContext();
        this.anchorView = anchorView;
        this.parent = parent;
        this.items = items;
        int width = (int) context.getResources().getDimension(R.dimen.sb_dialog_width_212);
        this.window = new PopupWindow(width, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void show() {
        mainHandler.post(() -> {
            Logger.d(">> MessageAnchorDialog::show()");
            showAnchorList();
        });
    }

    public void dismiss() {
        mainHandler.post(() -> {
            try {
                Logger.d(">> MessageAnchorDialog::dismiss()");
                window.dismiss();
            } catch (Exception e) {
                Logger.d(e);
            }
        });
    }

    public boolean isShowing() {
        if (window == null) {
            return false;
        }

        return window.isShowing();
    }

    private void showAnchorList() {
        final DialogView dialogView = new DialogView(context);
        dialogView.setItems(items, (view, position, key) -> {
            if (window != null) {
                window.dismiss();
            }
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, position, key);
            }
        }, false, R.dimen.sb_size_16);
        dialogView.setBackgroundAnchor();

        window.setContentView(dialogView);
        window.setOutsideTouchable(true);
        window.setFocusable(true);
        window.setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent));

        int x = getXoff(anchorView);
        int y = getYoff(parent, anchorView, dialogView);
        window.showAtLocation(anchorView, Gravity.START|Gravity.TOP, x, y);
    }

    private static int getXoff(View anchorView) {
        int[] loc = new int[2];
        anchorView.getLocationOnScreen(loc);
        return loc[0];
    }

    private static int getYoff(View parent, View anchorView, View contentView) {
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int contentViewMeasuredHeight = contentView.getMeasuredHeight();
        int[] loc = new int[2];
        anchorView.getLocationOnScreen(loc);

        if (isDropDown(parent, anchorView)) {
            return loc[1]+anchorView.getMeasuredHeight();
        }
        return loc[1]-(contentViewMeasuredHeight);
    }

    private static boolean isDropDown(View parent, View anchorView) {
        int[] loc = new int[2];
        int[] parentLoc = new int[2];
        anchorView.getLocationOnScreen(loc);
        parent.getLocationOnScreen(parentLoc);
        int parentHeight = parent.getMeasuredHeight();
        return (parentHeight / 2 > loc[1]-parentLoc[1]);
    }

    void setOnItemClickListener(OnItemClickListener<Integer> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    void setOnDismissListener(PopupWindow.OnDismissListener dismissListener) {
        this.window.setOnDismissListener(dismissListener);
    }

    public static class Builder {
        private final View anchorView;
        private final View parent;
        private final DialogListItem[] items;
        private OnItemClickListener<Integer> itemClickListener;
        private PopupWindow.OnDismissListener dismissListener;

        public Builder(View anchorView, View parent, DialogListItem[] items) {
            this.anchorView = anchorView;
            this.parent = parent;
            this.items = items;
        }

        public Builder setOnItemClickListener(OnItemClickListener<Integer> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        public Builder setOnDismissListener(PopupWindow.OnDismissListener dismissListener) {
            this.dismissListener = dismissListener;
            return this;
        }

        public MessageAnchorDialog build() {
            MessageAnchorDialog dialog = new MessageAnchorDialog(anchorView, parent, items);
            dialog.setOnItemClickListener(itemClickListener);
            dialog.setOnDismissListener(dismissListener);
            return dialog;
        }
    }
}
