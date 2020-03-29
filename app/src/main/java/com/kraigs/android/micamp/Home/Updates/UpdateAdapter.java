package com.kraigs.android.micamp.Home.Updates;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kraigs.android.micamp.R;
import com.kraigs.android.micamp.extras.WebViewClass;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class UpdateAdapter extends FirestoreRecyclerAdapter<Update,UpdateAdapter.UpdateHolder> {
    Context context;
    public UpdateAdapter(@NonNull FirestoreRecyclerOptions<Update> options,Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final UpdateHolder holder, int position, @NonNull final Update model) {
        holder.updateName.setText(model.getUpdateName());
        context = holder.itemView.getContext();

        holder.updateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inent = new Intent(context, WebViewClass.class);
                inent.putExtra("webUrl",model.getUpdateUrl());
                context.startActivity(inent);

//                    int SDK_INT = android.os.Build.VERSION.SDK_INT;
//                    if (SDK_INT > 8) {
//                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
//                                .permitAll().build();
//                        StrictMode.setThreadPolicy(policy);
//
//                        String url = downloadUrl;
//                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//                        request.setDescription("Downloading...");
//                        request.setTitle("Downloads");
//// in order for this if to run, you must use the android 3.2 to compile your app
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                            request.allowScanningByMediaScanner();
//                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                        }
//                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Documents.pdf");
//
//// get download service and enqueue send_document
//                        DownloadManager manager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
//                        manager.enqueue(request);
//                        Toast.makeText(holder.itemView.getContext(), "Downloading...", Toast.LENGTH_SHORT).show();
//                    }
                }
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @NonNull
    @Override
    public UpdateHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zunit_update,viewGroup,false);

        return new UpdateHolder(v);
    }

    public class UpdateHolder extends RecyclerView.ViewHolder {
        TextView updateName;
        public UpdateHolder(@NonNull View itemView) {
            super(itemView);
            updateName = itemView.findViewById(R.id.updateName);
        }
    }






}
