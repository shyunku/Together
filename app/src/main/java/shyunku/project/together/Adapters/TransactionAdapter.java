package shyunku.project.together.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import shyunku.project.together.Constants.Global;
import shyunku.project.together.Engines.FirebaseManageEngine;
import shyunku.project.together.Objects.MoneyTransaction;
import shyunku.project.together.R;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private ArrayList<MoneyTransaction> transactions;
    private Context context;

    public static final int GENERAL = 0;
    public static final int SETTLE = 1;

    public TransactionAdapter(ArrayList<MoneyTransaction> transactions){
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = null;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(viewType == GENERAL){
            view = inflater.inflate(R.layout.money_transaction_item, parent, false);
        }else{
            view = inflater.inflate(R.layout.money_transaction_clear, parent, false);
        }
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MoneyTransaction trans = transactions.get(position);
        //new LogEngine().sendLog("holder : "+trans.transactionName+" "+trans.OwedUsername+" "+trans.value+" "+trans.timestamp);
        if(holder.transName!= null)
            holder.transName.setText(trans.transactionName);
        try {
            Date date = Global.transactionDateFormat.parse(trans.timestamp);
            holder.timestamp.setText(Global.transactionReleaseDateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String direction = trans.OwedUsername.equals(Global.getOwner())?" → ":" ← ";
        String directionStr = Global.getOwner()+direction+Global.getOpper();
        if(holder.transDirection != null)
            holder.transDirection.setText(directionStr);
        if(holder.value != null) {
            holder.value.setText(trans.value+" 원");
            holder.value.setTextColor(ContextCompat.getColor(context,
                    trans.OwedUsername.equals(Global.getOpper())?R.color.transaction_blue:R.color.transaction_red));
        }
        if(holder.cancelButton!= null) {
            holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final DatabaseReference ref = FirebaseManageEngine.getPartyTransactionsRef();
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot data : dataSnapshot.getChildren()) {
                                MoneyTransaction transaction = data.getValue(MoneyTransaction.class);
                                if(transaction.timestamp == trans.timestamp){
                                    ref.child(data.getKey()).removeValue();
                                    return;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    @Override
    public int getItemViewType(int position) {
        MoneyTransaction trans =  transactions.get(position);
        return trans.isGeneral?GENERAL:SETTLE;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView transName;
        public TextView transDirection;
        public TextView timestamp;
        public TextView value;

        //settle
        public Button cancelButton;

        public ViewHolder(@NonNull View v) {
            super(v);
            transName = (TextView) v.findViewById(R.id.transaction_item_name);
            transDirection = (TextView) v.findViewById(R.id.transaction_direction);
            timestamp = (TextView)v.findViewById(R.id.transaction_timestamp_view);
            value = (TextView) v.findViewById(R.id.transaction_value_view);

            cancelButton = (Button) v.findViewById(R.id.settle_cancel_button);
        }
    }
}
