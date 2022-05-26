package com.alphawallet.app.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;

import com.alphawallet.app.R;


public class AddWalletView extends FrameLayout implements View.OnClickListener {
    private OnNewWalletClickListener onNewWalletClickListener;
    private OnImportWalletClickListener onImportWalletClickListener;

    public AddWalletView(Context context) {
        this(context, R.layout.layout_dialog_add_account);
    }

    public AddWalletView(Context context, @LayoutRes int layoutId) {
        super(context);

        init(layoutId);
    }

    private void init(@LayoutRes int layoutId) {
        LayoutInflater.from(getContext()).inflate(layoutId, this, true);
        findViewById(R.id.new_account_action).setOnClickListener(this);
        findViewById(R.id.import_account_action).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        final int new_account_action = R.id.new_account_action;
        final int import_account_action = R.id.import_account_action;

        switch (view.getId()) {
            case new_account_action: {
                if (onNewWalletClickListener != null) {
                    onNewWalletClickListener.onNewWallet(view);
                }
                break;
            }
            case import_account_action: {
                if (onImportWalletClickListener != null) {
                    onImportWalletClickListener.onImportWallet(view);
                }
                break;
            }
        }
    }

    public void setOnNewWalletClickListener(OnNewWalletClickListener onNewWalletClickListener) {
        this.onNewWalletClickListener = onNewWalletClickListener;
    }

    public void setOnImportWalletClickListener(OnImportWalletClickListener onImportWalletClickListener) {
        this.onImportWalletClickListener = onImportWalletClickListener;
    }

    public interface OnNewWalletClickListener {
        void onNewWallet(View view);
    }

    public interface OnImportWalletClickListener {
        void onImportWallet(View view);
    }
}
