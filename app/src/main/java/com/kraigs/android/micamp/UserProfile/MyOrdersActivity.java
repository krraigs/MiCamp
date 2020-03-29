package com.kraigs.android.micamp.UserProfile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.kraigs.android.micamp.Order.Item;
import com.kraigs.android.micamp.Order.Order;
import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.NotificationActivity;
import com.kraigs.android.micamp.extras.SharedPrefs;

import java.util.ArrayList;
import java.util.HashMap;

public class MyOrdersActivity extends AppCompatActivity {

    final int UPI_PAYMENT = 0;
    RecyclerView orderRv;
    CollectionReference colRef;
    CollectionReference userRef,pendingRef;
    DocumentReference userOrderRef,cancelref;
    DatabaseReference notifyRef;
    ProgressDialog loadingBar;
    String currentUserID;
    RelativeLayout emptyRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        orderRv = findViewById(R.id.orderRv);
        emptyRl = findViewById(R.id.emptyRl);
        orderRv.setVisibility(View.GONE);
        orderRv.setLayoutManager(new LinearLayoutManager(MyOrdersActivity.this));

        loadingBar = new ProgressDialog(MyOrdersActivity.this);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        colRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order")
                .collection("Snakkers").document("UserOrders").collection(currentUserID);

        pendingRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order")
                .collection("Snakkers").document("OrderCategory").collection("PendingOrders");

        userRef = FirebaseFirestore.getInstance().collection("User");
        userOrderRef = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order").collection("Snakkers").document("UserOrders");
        notifyRef = FirebaseDatabase.getInstance().getReference().child("NotificationsUserOrder");

        cancelref = FirebaseFirestore.getInstance().collection(SharedPrefs.getCollege()).document("Order")
                .collection("Snakkers").document("OrderCategory").collection("CancelOrders").document();

        colRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (snapshots.isEmpty()){

                    emptyRl.setVisibility(View.VISIBLE);
                    orderRv.setVisibility(View.GONE);

                } else {

                    emptyRl.setVisibility(View.GONE);
                    orderRv.setVisibility(View.VISIBLE);
                    setUpRv();

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_noti, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.ic_noti) {
            Intent intent = new Intent(MyOrdersActivity.this, NotificationActivity.class);
            intent.putExtra("type","snackers");
            startActivity(intent);
        }

        return true;
    }

    private void setUpRv() {

        Query query = colRef.orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Order> options = new FirestoreRecyclerOptions.Builder<Order>()
                .setQuery(query,Order.class)
                .build();

        FirestoreRecyclerAdapter<Order,OrderHolder> adapter = new FirestoreRecyclerAdapter<Order, OrderHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderHolder orderHolder, int i, @NonNull Order order) {

                String key = getSnapshots().getSnapshot(i).getId();

                ArrayList<Item> list= order.getOrder();
                orderHolder.itemsRv.setLayoutManager(new LinearLayoutManager(MyOrdersActivity.this));
                ItemAdapter adapter1 = new ItemAdapter(list);
                orderHolder.itemsRv.setAdapter(adapter1);

                orderHolder.priceTv.setText("Rs. " + order.getPrice());

                orderHolder.statusTv.setText(order.getStatus());

                if (order.getStatus() != null){
                    if (order.getStatus().equals("Pending")){
                        orderHolder.otpTv.setVisibility(View.GONE);

                    } else if(order.getStatus().equals("Preparing")){
                        orderHolder.otpTv.setText("OTP - " + order.getOtp());

                        orderHolder.doneBt.setVisibility(View.GONE);
                        orderHolder.cancelBt.setVisibility(View.GONE);

                    } else if(order.getStatus().equals("Cancelled")){
                        orderHolder.otpTv.setVisibility(View.GONE);
                        orderHolder.doneBt.setVisibility(View.GONE);
                        orderHolder.cancelBt.setVisibility(View.GONE);

                    } else if(order.getStatus().equals("Delivered")){
                        orderHolder.otpTv.setVisibility(View.GONE);
                        orderHolder.doneBt.setVisibility(View.GONE);
                        orderHolder.cancelBt.setVisibility(View.GONE);

                    }
                } else{
                    orderHolder.otpTv.setVisibility(View.GONE);
                    orderHolder.doneBt.setVisibility(View.GONE);
                    orderHolder.cancelBt.setVisibility(View.GONE);
                }

                orderHolder.cancelBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        loadingBar.setTitle("Order");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.setMessage("Please wait");
                        loadingBar.show();

                        HashMap<String,Object> map = new HashMap<>();
                        map.put("order",list);
                        map.put("name",order.getName());
                        map.put("address",order.getAddress());
                        map.put("price",order.getPrice());
                        map.put("user",order.getUser());
                        map.put("userOrderKey",order.getUserOrderKey());
                        map.put("timestamp", FieldValue.serverTimestamp());
                        map.put("mode","User");

                        cancelref.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    HashMap<String,Object> map2 = new HashMap<>();
                                    map2.put("status","Cancelled");

                                    colRef.document(key).update(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                Log.d("Cancel",order.getOrderKey());

                                                pendingRef.document(order.getOrderKey()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            loadingBar.dismiss();
                                                            Toast.makeText(MyOrdersActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                        }else{
                                                            loadingBar.dismiss();
                                                            Toast.makeText(MyOrdersActivity.this, task.getException().toString() + "delete error", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            }else{
                                                loadingBar.dismiss();
                                                Toast.makeText(MyOrdersActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else{
                                    loadingBar.dismiss();
                                    Toast.makeText(MyOrdersActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                    }
                });

                orderHolder.doneBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        payUsingUpi((order.getPrice()) + "", "praveen4030@gmail.com", "Order payment", "Order from " + order.getName());
                    }
                });

            }

            @NonNull
            @Override
            public OrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_pending_order,parent,false);
                return new OrderHolder(v);

            }
        };

        orderRv.setAdapter(adapter);
        adapter.startListening();

    }

    class ItemAdapter extends RecyclerView.Adapter<ItemHolder>{

        ArrayList<Item> list;

        public ItemAdapter(ArrayList<Item> list){
            this.list = list;
        }

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_item,parent,false);
            return new ItemHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            Item item = list.get(position);
            holder.nameTv.setText(item.getName());
            holder.qtyTv.setText("Qty: " + item.getQty());
            holder.priceTv.setText("Rs. " + item.getQty()* Long.parseLong(item.getPrice()));

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

    }

    private class OrderHolder extends RecyclerView.ViewHolder {

        Button doneBt,cancelBt;
        TextView priceTv,otpTv,statusTv;
        RecyclerView itemsRv;

        public OrderHolder(@NonNull View itemView) {
            super(itemView);

            doneBt = itemView.findViewById(R.id.doneBt);
            priceTv = itemView.findViewById(R.id.priceTv);
            itemsRv = itemView.findViewById(R.id.itemsRv);
            otpTv = itemView.findViewById(R.id.otpTv);
            cancelBt = itemView.findViewById(R.id.cancelBt);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        TextView nameTv,priceTv,qtyTv;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
            priceTv = itemView.findViewById(R.id.priceTv);
            qtyTv = itemView.findViewById(R.id.qtyTv);

        }
    }

    void payUsingUpi(String amount, String upiId, String name, String note) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText( MyOrdersActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.d("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        Log.d("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(MyOrdersActivity.this)) {
            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(MyOrdersActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr: "+approvalRefNo);
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(MyOrdersActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MyOrdersActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MyOrdersActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }
}
