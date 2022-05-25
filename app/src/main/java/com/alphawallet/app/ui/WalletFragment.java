package com.alphawallet.app.ui;

import static android.app.Activity.RESULT_OK;
import static com.alphawallet.app.C.ADDED_TOKEN;
import static com.alphawallet.app.C.ErrorCode.EMPTY_COLLECTION;
import static com.alphawallet.app.C.Key.WALLET;
import static com.alphawallet.app.repository.TokensRealmSource.ADDRESS_FORMAT;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.ActivityMeta;
import com.alphawallet.app.entity.BackupOperationType;
import com.alphawallet.app.entity.BackupTokenCallback;
import com.alphawallet.app.entity.ContractLocator;
import com.alphawallet.app.entity.CreateWalletCallbackInterface;
import com.alphawallet.app.entity.CustomViewSettings;
import com.alphawallet.app.entity.ErrorEnvelope;
import com.alphawallet.app.entity.ServiceSyncCallback;
import com.alphawallet.app.entity.SyncCallback;
import com.alphawallet.app.entity.TokenFilter;
import com.alphawallet.app.entity.TransactionMeta;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.entity.WalletPage;
import com.alphawallet.app.entity.WalletType;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.entity.tokens.TokenCardMeta;
import com.alphawallet.app.interact.ActivityDataInteract;
import com.alphawallet.app.interact.GenericWalletInteract;
import com.alphawallet.app.repository.EthereumNetworkRepository;
import com.alphawallet.app.repository.TokensRealmSource;
import com.alphawallet.app.repository.entity.RealmToken;
import com.alphawallet.app.repository.entity.RealmTransfer;
import com.alphawallet.app.service.KeyService;
import com.alphawallet.app.service.TokensService;
import com.alphawallet.app.ui.widget.TokensAdapterCallback;
import com.alphawallet.app.ui.widget.adapter.ActivityAdapter;
import com.alphawallet.app.ui.widget.adapter.TokensAdapter;
import com.alphawallet.app.ui.widget.adapter.WalletAdapter;
import com.alphawallet.app.ui.widget.entity.AvatarWriteCallback;
import com.alphawallet.app.ui.widget.entity.TokenTransferData;
import com.alphawallet.app.ui.widget.entity.WarningData;
import com.alphawallet.app.ui.widget.holder.TokenGridHolder;
import com.alphawallet.app.ui.widget.holder.TokenHolder;
import com.alphawallet.app.ui.widget.holder.WarningHolder;
import com.alphawallet.app.util.LocaleUtils;
import com.alphawallet.app.viewmodel.ActivityViewModel;
import com.alphawallet.app.viewmodel.WalletViewModel;
import com.alphawallet.app.viewmodel.WalletsViewModel;
import com.alphawallet.app.widget.AWalletAlertDialog;
import com.alphawallet.app.widget.AddWalletView;
import com.alphawallet.app.widget.EmptyTransactionsView;
import com.alphawallet.app.widget.NotificationView;
import com.alphawallet.app.widget.ProgressView;
import com.alphawallet.app.widget.SystemView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import kotlin.Unit;

/**
 * Created by justindeguzman on 2/28/18.
 */
@AndroidEntryPoint
public class WalletFragment extends BaseFragment implements
        TokensAdapterCallback,
        View.OnClickListener,
        Runnable,
        BackupTokenCallback,
        AvatarWriteCallback,
        ServiceSyncCallback,
        SyncCallback,
        ActivityDataInteract {
    private static final String TAG = "WFRAG";

    public static final String SEARCH_FRAGMENT = "w_search";

    private final long balanceChain = EthereumNetworkRepository.getOverrideToken().chainId;

    private WalletViewModel viewModel;
    private WalletsViewModel walletsViewModel;
    private SystemView systemView;
    private TokensAdapter adapter;
    private WalletAdapter walletAdapter;
    private View selectedToken;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String importFileName;
    private RecyclerView assetRecyclerView;
    private FrameLayout transactionHolder;
    private ActivityFragment transactionFragment;
    private ViewPager2 walletHorizontalList;
    private SwipeRefreshLayout refreshAssetsLayout;
    private boolean isVisible;
    private TokenFilter currentTabPos = TokenFilter.ALL;
    private Realm realm = null;
    private RealmResults<RealmToken> realmUpdates;
    private long realmUpdateTime;
    private TextView assetsTab;
    private TextView transactionsTab;
    private AWalletAlertDialog aDialog;
    private String dialogError;
    private Dialog dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        LocaleUtils.setActiveLocale(getContext()); // Can't be placed before above line

        if (CustomViewSettings.canAddTokens()) {
            toolbar(view, R.menu.menu_wallet, this::onMenuItemClick);
        } else {
            toolbar(view);
        }

        initViews(view);

        initViewModel();

        initList();

        initTabLayout(view);

        initNotificationView(view);

        setImportToken();

        viewModel.prepare();
        viewModel.defaultWallet().observe(getViewLifecycleOwner(), this::onDefaultWallet);
        walletsViewModel.onPrepare(balanceChain, this);

        getChildFragmentManager()
                .setFragmentResultListener(SEARCH_FRAGMENT, this, (requestKey, bundle) ->
                {
                    Fragment fragment = getChildFragmentManager().findFragmentByTag(SEARCH_FRAGMENT);
                    if (fragment != null && fragment.isVisible() && !fragment.isDetached()) {
                        fragment.onDetach();
                        getChildFragmentManager().beginTransaction()
                                .remove(fragment)
                                .commitAllowingStateLoss();
                    }
                });

        transactionFragment = new ActivityFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.transaction_holder, transactionFragment)
                .commit();
        return view;
    }

    private void initList() {
        adapter = new TokensAdapter(this, viewModel.getAssetDefinitionService(), viewModel.getTokensService(),
                tokenManagementLauncher);
        adapter.setHasStableIds(true);
        setLinearLayoutManager(TokenFilter.ALL.ordinal());
        if (assetRecyclerView.getItemAnimator() != null)
            ((SimpleItemAnimator) assetRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeCallback(adapter));
        itemTouchHelper.attachToRecyclerView(assetRecyclerView);

        refreshAssetsLayout.setOnRefreshListener(this::refreshList);
        assetRecyclerView.addRecyclerListener(holder -> adapter.onRViewRecycled(holder));
        assetRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        walletAdapter = new WalletAdapter(
                this::onWalletAdd,
                this::onWalletMenu,
                this::onWalletSend,
                this::onWalletReceive
        );
        walletHorizontalList.setAdapter(walletAdapter);
        walletHorizontalList.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Wallet wallet = walletAdapter.getWallet(position);
                adapter.setWalletAddress(wallet.address);
                walletsViewModel.setDefaultWallet(wallet);
                transactionFragment.setWallet(wallet);
                refreshList();
            }
        });

        SnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(assetRecyclerView);
    }

    private void initViewModel() {
        walletsViewModel = new ViewModelProvider(this).get(WalletsViewModel.class);
        walletsViewModel.wallets().observe(getViewLifecycleOwner(), this::onFetchWallets);

        viewModel = new ViewModelProvider(this)
                .get(WalletViewModel.class);
        viewModel.progress().observe(getViewLifecycleOwner(), systemView::showProgress);
        viewModel.tokens().observe(getViewLifecycleOwner(), this::onTokens);
        viewModel.backupEvent().observe(getViewLifecycleOwner(), this::backupEvent);
        viewModel.defaultWallet().observe(getViewLifecycleOwner(), this::onDefaultWallet);
        viewModel.onFiatValues().observe(getViewLifecycleOwner(), this::updateValue);
        viewModel.getTokensService().startWalletSync(this);
    }

    private Unit onWalletAdd() {
        AddWalletView addWalletView = new AddWalletView(getContext());
        addWalletView.setOnNewWalletClickListener(this::onNewWallet);
        addWalletView.setOnImportWalletClickListener(this::onImportWallet);
//        addWalletView.setOnWatchWalletClickListener(getContext());
        addWalletView.setOnCloseActionListener(this::onCloseDialogMenu);
        dialog = new BottomSheetDialog(requireContext());
//        dialog = new BottomSheetDialog(this, R.style.Aw_Component_BottomSheetDialog);
        dialog.setContentView(addWalletView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
//        BottomSheetBehavior behavior = BottomSheetBehavior.from((View) addWalletView.getParent());
//        dialog.setOnShowListener(dialog -> behavior.setPeekHeight(addWalletView.getHeight()));
        dialog.show();
        return null;
    }

    private void onNewWallet(View view) {
        walletsViewModel.newWallet(getActivity(), new CreateWalletCallbackInterface() {
            @Override
            public void HDKeyCreated(String address, Context ctx, KeyService.AuthenticationLevel level) {
                if (address == null) onCreateWalletError(new ErrorEnvelope(""));
                else walletsViewModel.StoreHDWallet(address, level);
                dialog.dismiss();
            }

            @Override
            public void keyFailure(String message) {
                onCreateWalletError(new ErrorEnvelope(message));
                dialog.dismiss();
            }

            @Override
            public void cancelAuthentication() {
                onCreateWalletError(new ErrorEnvelope(getString(R.string.authentication_cancelled)));
                dialog.dismiss();
            }

            @Override
            public void fetchMnemonic(String mnemonic) {

            }
        });
    }

    private void onImportWallet(View view) {
        hideDialog();
        walletsViewModel.importWallet(getActivity());
    }

    private void onCloseDialogMenu(View view) {
        hideDialog();
    }

    private void hideDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }

        if (aDialog != null && aDialog.isShowing()) {
            aDialog.dismiss();
            aDialog = null;
        }
    }

    private void onCreateWalletError(ErrorEnvelope errorEnvelope) {
        dialogError = errorEnvelope.message;
        if (handler != null) handler.post(displayWalletError);
    }

    private final Runnable displayWalletError = new Runnable() {
        @Override
        public void run() {
            aDialog = new AWalletAlertDialog(getActivity());
            aDialog.setTitle(R.string.title_dialog_error);
            aDialog.setIcon(AWalletAlertDialog.ERROR);
            aDialog.setMessage(TextUtils.isEmpty(dialogError)
                    ? getString(R.string.error_create_wallet)
                    : dialogError);
            aDialog.setButtonText(R.string.dialog_ok);
            aDialog.setButtonListener(v -> aDialog.dismiss());
            aDialog.show();
        }
    };

    private Unit onWalletMenu() {
        viewModel.showMyAddress(requireContext());
        return null;
    }

    private Unit onWalletSend() {
        Wallet wallet = viewModel.getWallet();
        viewModel.sendTokens(requireActivity(), wallet);
        return null;
    }

    private Unit onWalletReceive() {
        viewModel.showMyWalletAddress(requireContext());
        return null;
    }

    private List<ActivityMeta> buildTransactionList(Realm realm, ActivityMeta[] activityItems) {
        //selectively filter the items with the following rules:
        // - allow through all normal transactions with no token transfer consequences
        // - for any transaction with token transfers; if there's only one token transfer, only show the transfer
        // - for any transaction with more than one token transfer, show the transaction and show the child transfer consequences
        List<ActivityMeta> filteredList = new ArrayList<>();

        for (ActivityMeta am : activityItems) {
            if (am instanceof TransactionMeta) {
                List<TokenTransferData> tokenTransfers = getTokenTransfersForHash(realm, (TransactionMeta) am);
                if (tokenTransfers.size() != 1) {
                    filteredList.add(am);
                } //only 1 token transfer ? No need to show the underlying transaction
                filteredList.addAll(tokenTransfers);
            }
        }

        return filteredList;
    }

    private List<TokenTransferData> getTokenTransfersForHash(Realm realm, TransactionMeta tm) {
        List<TokenTransferData> transferData = new ArrayList<>();
        //summon realm items
        //get matching entries for this transaction
        RealmResults<RealmTransfer> transfers = realm.where(RealmTransfer.class)
                .equalTo("hash", tm.hash)
                .findAll();

        if (transfers != null && transfers.size() > 0) {
            //list of transfers, descending in time to give ordered list
            long nextTransferTime = transfers.size() == 1 ? tm.getTimeStamp() : tm.getTimeStamp() - 1; // if there's only 1 transfer, keep the transaction timestamp
            for (RealmTransfer rt : transfers) {
                TokenTransferData ttd = new TokenTransferData(rt.getHash(), tm.chainId,
                        rt.getTokenAddress(), rt.getEventName(), rt.getTransferDetail(), nextTransferTime);
                transferData.add(ttd);
                nextTransferTime--;
            }
        }

        return transferData;
    }

    private void onFetchWallets(Wallet[] wallets) {
        walletAdapter.setWallets(Arrays.asList(wallets));
    }

    private void initViews(@NonNull View view) {
        refreshAssetsLayout = view.findViewById(R.id.refresh_assets_layout);
        transactionHolder = view.findViewById(R.id.transaction_holder);
        systemView = view.findViewById(R.id.system_view);
        assetRecyclerView = view.findViewById(R.id.asset_list);
        walletHorizontalList = view.findViewById(R.id.wallet_list_horizontal);
        ImageView addImageView = view.findViewById(R.id.toolbar_action_add);
        addImageView.setOnClickListener(l -> onWalletAdd());

        systemView.showProgress(true);

        systemView.attachRecyclerView(assetRecyclerView);
        systemView.attachSwipeRefreshLayout(refreshAssetsLayout);

        ((ProgressView) view.findViewById(R.id.progress_view)).hide();
    }

    private void onDefaultWallet(Wallet wallet) {
        if (CustomViewSettings.showManageTokens()) {
            adapter.setWalletAddress(wallet.address);
        }

        //Do we display new user backup popup?
        Bundle result = new Bundle();
        result.putBoolean(C.SHOW_BACKUP, wallet.lastBackupTime > 0);
        getParentFragmentManager().setFragmentResult(C.SHOW_BACKUP, result); //reset tokens service and wallet page with updated filters
    }

    private void setRealmListener(final long updateTime) {
        if (realm == null || realm.isClosed()) realm = viewModel.getRealmInstance();
        if (realmUpdates != null) {
            realmUpdates.removeAllChangeListeners();
            realm.removeAllChangeListeners();
        }

        realmUpdates = realm.where(RealmToken.class).equalTo("isEnabled", true)
                .like("address", ADDRESS_FORMAT)
                .greaterThan("addedTime", (updateTime + 1))
                .findAllAsync();
        realmUpdates.addChangeListener(realmTokens ->
        {
            long lastUpdateTime = updateTime;
            List<TokenCardMeta> metas = new ArrayList<>();
            //make list
            for (RealmToken t : realmTokens) {
                if (t.getUpdateTime() > lastUpdateTime) lastUpdateTime = t.getUpdateTime();
                if (!viewModel.getTokensService().getNetworkFilters().contains(t.getChainId()))
                    continue;
                if (viewModel.isChainToken(t.getChainId(), t.getTokenAddress())) continue;

                String balance = TokensRealmSource.convertStringBalance(t.getBalance(), t.getContractType());

                TokenCardMeta meta = new TokenCardMeta(t.getChainId(), t.getTokenAddress(), balance,
                        t.getUpdateTime(), viewModel.getAssetDefinitionService(), t.getName(), t.getSymbol(), t.getContractType(),
                        viewModel.getTokenGroup(t.getChainId(), t.getTokenAddress()));
                meta.lastTxUpdate = t.getLastTxTime();
                meta.isEnabled = t.isEnabled();
                metas.add(meta);
            }

            if (metas.size() > 0) {
                realmUpdateTime = lastUpdateTime;
                updateMetas(metas);
                handler.postDelayed(() -> setRealmListener(realmUpdateTime), 500);
            }
        });
    }

    private void updateMetas(List<TokenCardMeta> metas) {
        handler.post(() ->
        {
            if (metas.size() > 0) {
                adapter.setTokens(metas.toArray(new TokenCardMeta[0]));
                systemView.hide();
            }
        });
    }

    //Refresh value of wallet once sync is complete
    @Override
    public void syncComplete(TokensService svs, int syncCount) {
        if (viewModel.getTokensService().isMainNetActive()) {
            svs.getFiatValuePair()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::updateValue)
                    .isDisposed();
        }

        if (syncCount > 0) {
            //now refresh the tokens to pick up any new ticker updates
            viewModel.getTokensService().getTickerUpdateList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(adapter::notifyTickerUpdate)
                    .isDisposed();
        }
    }

    //Could the view have been destroyed?
    private void updateValue(Pair<Double, Double> fiatValues) {
        try {
            // to avoid NaN
            double changePercent = fiatValues.first != 0 ? ((fiatValues.first - fiatValues.second) / fiatValues.second) * 100.0 : 0.0;

            if (viewModel.getWallet() != null && viewModel.getWallet().type != WalletType.WATCH && isVisible) {
                viewModel.checkBackup(fiatValues.first);
            }
        } catch (Exception e) {
            // empty: expected if view has terminated before we can shut down the service return
        }
    }

    private void refreshTransactions() {
        handler.post(() -> {
            transactionFragment.resetTransactions();
        });
    }

    private void refreshList() {
        handler.post(() ->
        {
            if (assetsTab.isSelected()) {
                adapter.clear();
                viewModel.prepare();
                viewModel.notifyRefresh();
            }
            if (transactionsTab.isSelected()) {
                transactionFragment.resetTokens();
                transactionFragment.resetTransactions();
            }
        });
    }

    @Override
    public void comeIntoFocus() {
        isVisible = true;
        if (viewModel.getWallet() != null && !TextUtils.isEmpty(viewModel.getWallet().address)) {
            setRealmListener(realmUpdateTime);
        }
    }

    @Override
    public void leaveFocus() {
        if (realmUpdates != null) {
            realmUpdates.removeAllChangeListeners();
            realmUpdates = null;
        }
        if (realm != null && !realm.isClosed()) realm.close();
        softKeyboardGone();
    }

    @Override
    public void onPause() {
        super.onPause();
        transactionFragment.leaveFocus();
    }

    private void initTabLayout(View view) {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        if (CustomViewSettings.hideTabBar()) {
            tabLayout.setVisibility(View.GONE);
            return;
        }
        tabLayout.addTab(tabLayout.newTab().setText(R.string.all));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.assets));
//        tabLayout.addTab(tabLayout.newTab().setText(R.string.collectibles));
//        tabLayout.addTab(tabLayout.newTab().setText(R.string.defi_header));
//        tabLayout.addTab(tabLayout.newTab().setText(R.string.governance_header));
        //tabLayout.addTab(tabLayout.newTab().setText(R.string.attestations));

//        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FF0000"));
//        tabLayout.setTabTextColors(Color.parseColor("#6D6D6D"), Color.parseColor("#ffffff"));

        View headerView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_wallet_tab, view.findViewById(R.id.pager_root));
//        View headerView = ((LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
//                .inflate(R.layout.layout_wallet_tab, null);

        assetsTab = headerView.findViewById(R.id.tab_assets);
        transactionsTab = headerView.findViewById(R.id.tab_transactions);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setCustomView(assetsTab);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setCustomView(transactionsTab);

        tabLayout.getTabAt(0).getCustomView().setActivated(true);
        assetsTab.setTextColor(requireContext().getColor(R.color.white));
        transactionsTab.setTextColor(requireContext().getColor(R.color.ethernity_tab_selected));
        assetRecyclerView.setAdapter(adapter);

        TabLayout dotsTabLayout = view.findViewById(R.id.wallet_list_indicator);
        new TabLayoutMediator(dotsTabLayout, walletHorizontalList, (tab, position) -> {
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.view.setSelected(true);
                TokenFilter newFilter = setLinearLayoutManager(tab.getPosition());
                adapter.setFilterType(newFilter);
                switch (newFilter) {
                    case ALL: //Assets tab
                        transactionsTab.setTextColor(requireContext().getColor(R.color.ethernity_tab_selected));
                        assetsTab.setTextColor(requireContext().getColor(R.color.white));
                        viewModel.prepare();

                        refreshAssetsLayout.setVisibility(View.VISIBLE);
                        transactionHolder.setVisibility(View.GONE);
                        break;
                    case ASSETS: //Transactions tab
                        transactionsTab.setTextColor(requireContext().getColor(R.color.white));
                        assetsTab.setTextColor(requireContext().getColor(R.color.ethernity_tab_selected));

                        refreshAssetsLayout.setVisibility(View.GONE);
                        transactionHolder.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.view.setSelected(true);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setTabActive(TabLayout tabLayout, TabLayout.Tab tab, boolean selected) {
        if (tabLayout.getTabCount() - 1 == tabLayout.getSelectedTabPosition()) {
            //last position show '+' instead of dot
            if (selected) {
                tab.view.setSelected(true);
                tab.view.setActivated(false);
            } else {
                tab.view.setSelected(false);
                tab.view.setActivated(false);
            }
        } else {
            if (selected) {
                tab.view.setSelected(true);
                tab.view.setActivated(true);
            } else {
                tab.view.setSelected(false);
                tab.view.setActivated(true);
            }
        }
    }

    private void setGridLayoutManager(TokenFilter tab) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == TokenGridHolder.VIEW_TYPE) {
                    return 1;
                }
                return 2;
            }
        });
        assetRecyclerView.setLayoutManager(gridLayoutManager);
        currentTabPos = tab;
    }

    private TokenFilter setLinearLayoutManager(int selectedTab) {
        currentTabPos = TokenFilter.values()[selectedTab];
        return currentTabPos;
    }

    @Override
    public void onTokenClick(View view, Token token, List<BigInteger> ids, boolean selected) {
        if (selectedToken == null) {
            getParentFragmentManager().setFragmentResult(C.TOKEN_CLICK, new Bundle());
            selectedToken = view;
            Token clickOrigin = viewModel.getTokenFromService(token);
            if (clickOrigin == null) clickOrigin = token;
            viewModel.showTokenDetail(getActivity(), clickOrigin);
            handler.postDelayed(this, 700);
        }
    }

    @Override
    public void onLongTokenClick(View view, Token token, List<BigInteger> tokenId) {

    }

    @Override
    public void reloadTokens() {
        viewModel.reloadTokens();
    }

    @Override
    public void onBuyToken() {
        Intent intent = viewModel.getBuyIntent(getCurrentWallet().address);
        ((HomeActivity) getActivity()).onActivityResult(C.TOKEN_SEND_ACTIVITY, RESULT_OK, intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        currentTabPos = TokenFilter.ALL;
        selectedToken = null;
        transactionFragment.comeIntoFocus();
        if (viewModel == null) {
            ((HomeActivity) getActivity()).resetFragment(WalletPage.WALLET);
        }
    }

    private void onTokens(TokenCardMeta[] tokens) {
        if (tokens != null) {
            adapter.setTokens(tokens);
            checkScrollPosition();
            viewModel.calculateFiatValues();
        }
        systemView.showProgress(false);

        realmUpdateTime = 0;
        for (TokenCardMeta tcm : tokens) {
            if (tcm.lastUpdate > realmUpdateTime) realmUpdateTime = tcm.lastUpdate;
        }

        if (isVisible) {
            setRealmListener(realmUpdateTime);
        }
    }

    /**
     * Checks to see if the current session was started from clicking on a TokenScript notification
     * If it was, identify the contract and pass information to adapter which will identify the corresponding contract token card
     */
    private void setImportToken() {
        if (importFileName != null) {
            ContractLocator importToken = viewModel.getAssetDefinitionService().getHoldingContract(importFileName);
            if (importToken != null)
                Toast.makeText(getContext(), importToken.address, Toast.LENGTH_LONG).show();
            if (importToken != null && adapter != null) adapter.setScrollToken(importToken);
            importFileName = null;
        }
    }

    /**
     * If the adapter has identified the clicked-on script update from the above call and that card is present, scroll to the card.
     */
    private void checkScrollPosition() {
        int scrollPos = adapter.getScrollPosition();
        if (scrollPos > 0 && assetRecyclerView != null) {
            ((LinearLayoutManager) assetRecyclerView.getLayoutManager()).scrollToPositionWithOffset(scrollPos, 0);
        }
    }

    private void backupEvent(GenericWalletInteract.BackupLevel backupLevel) {
        if (adapter.hasBackupWarning()) return;

        WarningData wData;
        switch (backupLevel) {
            case BACKUP_NOT_REQUIRED:
                break;
            case WALLET_HAS_LOW_VALUE:
                wData = new WarningData(this);
                wData.title = getString(R.string.time_to_backup_wallet);
                wData.detail = getString(R.string.recommend_monthly_backup);
                wData.buttonText = getString(R.string.back_up_wallet_action, viewModel.getWalletAddr().substring(0, 5));
                wData.colour = R.color.text_secondary;
                wData.wallet = viewModel.getWallet();
                adapter.addWarning(wData);
                break;
            case WALLET_HAS_HIGH_VALUE:
                wData = new WarningData(this);
                wData.title = getString(R.string.wallet_not_backed_up);
                wData.detail = getString(R.string.not_backed_up_detail);
                wData.buttonText = getString(R.string.back_up_wallet_action, viewModel.getWalletAddr().substring(0, 5));
                wData.colour = R.color.error;
                wData.wallet = viewModel.getWallet();
                adapter.addWarning(wData);
                break;
        }
    }

    private void onError(ErrorEnvelope errorEnvelope) {
        if (errorEnvelope.code == EMPTY_COLLECTION) {
            systemView.showEmpty(getString(R.string.no_tokens));
        } else {
            systemView.showError(getString(R.string.error_fail_load_tokens), this);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.try_again) {
            viewModel.prepare();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (realmUpdates != null) {
            realmUpdates.removeAllChangeListeners();
        }
        if (realm != null && !realm.isClosed()) realm.close();
        if (adapter != null && assetRecyclerView != null) adapter.onDestroy(assetRecyclerView);
    }

    public void resetTokens() {
        if (viewModel != null && adapter != null) {
            //reload tokens
            viewModel.reloadTokens();

            handler.post(() ->
            {
                //first abort the current operation
                adapter.clear();
                //show syncing
            });
        }
    }

    @Override
    public void run() {
//        if (selectedToken != null && selectedToken.findViewById(R.id.token_layout) != null)
//        {
//            selectedToken.findViewById(R.id.token_layout).setBackgroundResource(R.drawable.background_marketplace_event);
//        }
        selectedToken = null;
    }

    ActivityResultLauncher<Intent> handleBackupClick = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
                String keyBackup = null;
                boolean noLockScreen = false;
                Intent data = result.getData();
                if (data != null) keyBackup = data.getStringExtra("Key");
                if (data != null) noLockScreen = data.getBooleanExtra("nolock", false);
                if (result.getResultCode() == RESULT_OK) {
                    ((HomeActivity) getActivity()).backupWalletSuccess(keyBackup);
                } else {
                    ((HomeActivity) getActivity()).backupWalletFail(keyBackup, noLockScreen);
                }
            });

    @Override
    public void BackupClick(Wallet wallet) {
        Intent intent = new Intent(getContext(), BackupKeyActivity.class);
        intent.putExtra(WALLET, wallet);

        switch (viewModel.getWalletType()) {
            case HDKEY:
                intent.putExtra("TYPE", BackupOperationType.BACKUP_HD_KEY);
                break;
            case KEYSTORE:
                intent.putExtra("TYPE", BackupOperationType.BACKUP_KEYSTORE_KEY);
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        handleBackupClick.launch(intent);
    }

    @Override
    public void remindMeLater(Wallet wallet) {
        handler.post(() ->
        {
            if (viewModel != null) viewModel.setKeyWarningDismissTime(wallet.address);
            if (adapter != null) adapter.removeBackupWarning();
        });
    }

    final ActivityResultLauncher<Intent> tokenManagementLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->
            {
                if (result.getData() == null) return;
                ArrayList<ContractLocator> tokenData = result.getData().getParcelableArrayListExtra(ADDED_TOKEN);
                Bundle b = new Bundle();
                b.putParcelableArrayList(C.ADDED_TOKEN, tokenData);
                getParentFragmentManager().setFragmentResult(C.ADDED_TOKEN, b);
            });

    public void storeWalletBackupTime(String backedUpKey) {
        handler.post(() ->
        {
            if (viewModel != null) viewModel.setKeyBackupTime(backedUpKey);
            if (adapter != null) adapter.removeBackupWarning();
        });
    }

    public void setImportFilename(String fName) {
        importFileName = fName;
    }

    @Override
    public void avatarFound(Wallet wallet) {
        //write to database
        viewModel.saveAvatar(wallet);
    }

    @Override
    public void syncUpdate(String wallet, Pair<Double, Double> value) {

    }

    @Override
    public void syncCompleted(String wallet, Pair<Double, Double> value) {

    }

    @Override
    public void syncStarted(String wallet, Pair<Double, Double> value) {

    }

    @Override
    public void fetchMoreData(long latestDate) {
        //todo see ActivityFragment implementation
    }

    public class SwipeCallback extends ItemTouchHelper.SimpleCallback {
        private final TokensAdapter mAdapter;
        private Drawable icon;
        private ColorDrawable background;

        SwipeCallback(TokensAdapter adapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mAdapter = adapter;
            if (getActivity() != null) {
                icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_hide_token);
                if (icon != null) {
                    icon.setTint(ContextCompat.getColor(getActivity(), R.color.error_inverse));
                }
                background = new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.error));
            }
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder instanceof WarningHolder) {
                remindMeLater(viewModel.getWallet());
            } else if (viewHolder instanceof TokenHolder) {
                Token token = ((TokenHolder) viewHolder).token;
                viewModel.setTokenEnabled(token, false);
                adapter.removeToken(token.tokenInfo.chainId, token.getAddress());

                if (getContext() != null) {
                    Snackbar snackbar = Snackbar
                            .make(viewHolder.itemView, token.tokenInfo.name + " " + getContext().getString(R.string.token_hidden), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.action_snackbar_undo), view ->
                            {
                                viewModel.setTokenEnabled(token, true);
                                //adapter.updateToken(token.tokenInfo.chainId, token.getAddress(), true);
                            });

                    snackbar.show();
                }
            }
        }

        @Override
        public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() == TokenHolder.VIEW_TYPE) {
                Token t = ((TokenHolder) viewHolder).token;
                if (t != null && t.isEthereum()) return 0;
            } else {
                return 0;
            }

            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int offset = 20;
            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX > 0) {
                int iconLeft = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                int iconRight = itemView.getLeft() + iconMargin;
                icon.setBounds(iconRight, iconTop, iconLeft, iconBottom);
                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + offset,
                        itemView.getBottom());
            } else if (dX < 0) {
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.setBounds(itemView.getRight() + ((int) dX) - offset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else {
                background.setBounds(0, 0, 0, 0);
            }

            background.draw(c);
            icon.draw(c);
        }
    }

    public Wallet getCurrentWallet() {
        return viewModel.getWallet();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_scan) {
            viewModel.showQRCodeScanning(getActivity());
        }
        return super.onMenuItemClick(menuItem);
    }

    private void initNotificationView(View view) {
        NotificationView notificationView = view.findViewById(R.id.wallet_notification);
        boolean hasShownWarning = viewModel.isMarshMallowWarningShown();

        if (!hasShownWarning && android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            notificationView.setTitle(getContext().getString(R.string.title_version_support_warning));
            notificationView.setMessage(getContext().getString(R.string.message_version_support_warning));
            notificationView.setPrimaryButtonText(getContext().getString(R.string.hide_notification));
            notificationView.setPrimaryButtonListener(() ->
            {
                notificationView.setVisibility(View.GONE);
                viewModel.setMarshMallowWarning(true);
            });
        } else {
            notificationView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSearchClicked() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAddHideToken() {
        viewModel.addHideToken(requireContext());
    }
}
