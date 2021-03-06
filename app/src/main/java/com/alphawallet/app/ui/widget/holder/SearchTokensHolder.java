package com.alphawallet.app.ui.widget.holder;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.alphawallet.app.R;
import com.alphawallet.app.ui.widget.entity.ManageTokensData;

public class SearchTokensHolder extends BinderViewHolder<ManageTokensData> {
    public static final int VIEW_TYPE = 2021;

    public interface SearchHandler {
        void onFocus();
    }

    public interface AddHideTokenHandler {
        void onAddHideToken();
    }

    final EditText editSearch;
    final SearchHandler searchHandler;
    final AddHideTokenHandler tokenHandler;
    final View searchTokenClick;
    final AppCompatImageView addHideToken;
    String wallet;

    @Override
    public void bind(@Nullable ManageTokensData data, @NonNull Bundle addition) {
        if (wallet != null) return; //don't re-bind if view exists
        if (data != null) wallet = data.walletAddress;

        editSearch.setOnFocusChangeListener((v, hasFocus) -> {
            editSearch.clearFocus();
            if (hasFocus) searchHandler.onFocus();
        });

        searchTokenClick.setOnClickListener(v -> {
            if (searchHandler != null) searchHandler.onFocus();
        });

        addHideToken.setOnClickListener(v -> {
            if (tokenHandler != null) tokenHandler.onAddHideToken();
        });
    }

    public SearchTokensHolder(int res_id, ViewGroup parent, SearchHandler handler, AddHideTokenHandler tokenHandler) {
        super(res_id, parent);
        this.editSearch = findViewById(R.id.edit_search);
        this.searchHandler = handler;
        this.tokenHandler = tokenHandler;
        this.searchTokenClick = findViewById(R.id.click_layer);
        this.addHideToken = findViewById(R.id.manage_tokens);
        this.wallet = null;
    }
}
