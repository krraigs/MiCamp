package com.kraigs.android.micamp.Drawer;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kraigs.android.micamp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ContactAdapter extends FirestoreRecyclerAdapter<Contact,ContactAdapter.ContactViewHolder> {

    public ContactAdapter(@NonNull FirestoreRecyclerOptions<Contact> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int position, @NonNull final Contact model) {

        holder.facultyName.setText(model.getName());
        holder.facultyType.setText(model.getType());
        if(model.getNumber()!=null){
            holder.dialer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Uri u = Uri.parse("tel:" + model.getNumber());
                    Intent i = new Intent(Intent.ACTION_DIAL, u);
                    holder.dialer.getContext().startActivity(i);

                }
            });
        } else{

            holder.dialer.setVisibility(View.GONE);
            holder.dialer.setEnabled(false);

        }

        if (model.getMail().equals(" ") || model.getMail() == null){
            holder.mail.setVisibility(View.GONE);
            holder.mail.setEnabled(false);
        } else{
            holder.mail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto",model.getMail(), null));
                    holder.dialer.getContext().startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }
            });
        }
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zunit_contacts_list,viewGroup,false);
        return  new ContactViewHolder(view);
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView facultyName, facultyType;
        ImageView dialer,mail;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            facultyName = (TextView)itemView.findViewById(R.id.contact_faculty_name);
            facultyType = (TextView)itemView.findViewById(R.id.contact_faculty_type);
            dialer = (ImageView) itemView.findViewById(R.id.contact_faculty_number);
            mail = (ImageView) itemView.findViewById(R.id.contact_faculty_mail);
        }
    }
}
