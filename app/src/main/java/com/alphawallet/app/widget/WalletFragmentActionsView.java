package com.alphawallet.app.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;

import com.alphawallet.app.R;


public class WalletFragmentActionsView extends FrameLayout implements View.OnClickListener {
    private OnClickListener onCopyWalletAddressClickListener;
    private OnClickListener onShowSeedPhraseClickListener;
    private OnClickListener onRenameWalletListener;
    private OnClickListener onRemoveWalletClickListener;

    public WalletFragmentActionsView(Context context) {
        this(context, R.layout.layout_dialog_wallet_actions);
    }

    public WalletFragmentActionsView(Context context, @LayoutRes int layoutId) {
        super(context);

        init(layoutId);
    }

    private void init(@LayoutRes int layoutId) {
        LayoutInflater.from(getContext()).inflate(layoutId, this, true);
        findViewById(R.id.show_copy_wallet_address_action).setOnClickListener(this);
        findViewById(R.id.show_seed_action).setOnClickListener(this);
        findViewById(R.id.rename_wallet_action).setOnClickListener(this);
        findViewById(R.id.remove_wallet_action).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.show_copy_wallet_address_action: {
                if (onCopyWalletAddressClickListener != null) {
                    onCopyWalletAddressClickListener.onClick(view);
                }
                break;
            }
            case R.id.show_seed_action: {
                if (onShowSeedPhraseClickListener != null) {
                    onShowSeedPhraseClickListener.onClick(view);
                }
                break;
            }
            case R.id.rename_wallet_action: {
                if (onRenameWalletListener != null) {
                    onRenameWalletListener.onClick(view);
                }
                break;
            }
            case R.id.remove_wallet_action: {
                if (onRemoveWalletClickListener != null) {
                    onRemoveWalletClickListener.onClick(view);
                }
            }
        }
    }

    public void setOnCopyWalletAddressClickListener(OnClickListener onClickListener) {
        this.onCopyWalletAddressClickListener = onClickListener;
    }

    public void setOnShowSeedPhraseClickListener(OnClickListener onClickListener) {
        this.onShowSeedPhraseClickListener = onClickListener;
    }

    public void setOnRemoveWalletClickListener(OnClickListener onClickListener) {
        this.onRemoveWalletClickListener = onClickListener;
    }

    public void setOnRenameThisWalletClickListener(OnClickListener onClickListener) {
        this.onRenameWalletListener = onClickListener;
    }
}
