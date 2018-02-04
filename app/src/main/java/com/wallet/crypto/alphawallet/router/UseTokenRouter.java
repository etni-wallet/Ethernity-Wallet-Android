package com.wallet.crypto.trustapp.router;

import android.content.Context;
import android.content.Intent;

import com.wallet.crypto.trustapp.C;
import com.wallet.crypto.trustapp.entity.Ticket;
import com.wallet.crypto.trustapp.entity.Token;
import com.wallet.crypto.trustapp.ui.AddTokenActivity;
import com.wallet.crypto.trustapp.ui.UseTokenActivity;

import static com.wallet.crypto.trustapp.C.Key.TICKET;

/**
 * Created by James on 22/01/2018.
 */

public class UseTokenRouter {

    public void open(Context context, String name, String venue, String date, String address, double price, double balance, Token ticket) {
        Intent intent = new Intent(context, UseTokenActivity.class);
        intent.putExtra(TICKET, ticket);
//        intent.putExtra(C.EXTRA_CONTRACT_NAME, name);
//        intent.putExtra(C.EXTRA_TICKET_DATE, date);
//        intent.putExtra(C.EXTRA_TICKET_PRICE, price);
//        intent.putExtra(C.EXTRA_TICKET_VENUE, venue);
//        intent.putExtra(C.EXTRA_TOKEN_BALANCE, balance);
//        intent.putExtra(C.EXTRA_ADDRESS, address);
        context.startActivity(intent);
    }
}