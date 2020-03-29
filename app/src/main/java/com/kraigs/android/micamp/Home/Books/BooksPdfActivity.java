package com.kraigs.android.micamp.Home.Books;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.kraigs.android.micamp.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kraigs.android.micamp.extras.SharedPrefs;

public class BooksPdfActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView booksRv;
    Spinner branchSp,degreeSp,semSp;
    Button searchBt;
    private static String FLAG_DEGREE,FLAG_BRANCH,FLAG_SEM;
    Dialog dialog;

    public static final String TAG = "Book";
    CollectionReference colRef;
    RelativeLayout emptyRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_pdf);
        getSupportActionBar().setTitle("Books");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.custom_books_dialog);

        branchSp = dialog.findViewById(R.id.dialog_branch);
        degreeSp = dialog.findViewById(R.id.dialog_degree);
        semSp = dialog.findViewById(R.id.dialog_semester);
        searchBt = dialog.findViewById(R.id.dialog_search);
        emptyRl = findViewById(R.id.emptyRl);

        setUpDegreeSpinner();
        setUpBranchSpinner();
        setUpSemesterSpinner();

        booksRv = findViewById(R.id.booksPdfRv);
        booksRv.setHasFixedSize(true);
        booksRv.setLayoutManager(new LinearLayoutManager(BooksPdfActivity.this));

        dialog.show();

        searchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(FLAG_BRANCH) && !TextUtils.isEmpty(FLAG_DEGREE) && !TextUtils.isEmpty(FLAG_SEM)){
                    colRef = db.collection(SharedPrefs.getCollege()).document("Book").collection(FLAG_DEGREE).document(FLAG_BRANCH).collection(FLAG_SEM);

                    colRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                            if (snapshots.isEmpty()){

                                emptyRl.setVisibility(View.VISIBLE);
                                booksRv.setVisibility(View.GONE);

                            } else {
                                emptyRl.setVisibility(View.GONE);
                                booksRv.setVisibility(View.VISIBLE);
                                setUpRv();

                            }
                        }
                    });

                    dialog.dismiss();

                } else{
                    Toast.makeText(BooksPdfActivity.this, "Please select all columns!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpRv(){

        Query colRef = db.collection(SharedPrefs.getCollege()).document("Book").collection(FLAG_DEGREE).document(FLAG_BRANCH).collection(FLAG_SEM).orderBy("name");
        FirestoreRecyclerOptions<Books> options = new FirestoreRecyclerOptions.Builder<Books>()
                .setQuery(colRef, Books.class)
                .build();

        FirestoreRecyclerAdapter<Books,BookHolder> adapter = new FirestoreRecyclerAdapter<Books, BookHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BookHolder holder, int i, @NonNull Books model) {
                holder.name.setText(model.getName());
                holder.author.setText(model.getAuthor());

                String url = model.getBooksUrl();
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(holder.itemView.getContext());
                        alertDialog.setTitle("Delete").setMessage("Do you want to download this book?").setPositiveButton("Download", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                                if (SDK_INT > 8) {
                                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                            .permitAll().build();// get download service and enqueue send_document

                                    StrictMode.setThreadPolicy(policy);

                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                    request.setDescription("Downloading...");
                                    request.setTitle("Books Download");
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        request.allowScanningByMediaScanner();
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    }
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Book.pdf");

                                    DownloadManager manager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                    manager.enqueue(request);
                                    Toast.makeText(holder.itemView.getContext(), "Downloading...", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();

                    }
                });
            }

            @NonNull
            @Override
            public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_books_pdf,parent,false);
                return new BookHolder(v);
            }
        };

        booksRv.setAdapter(adapter);
        adapter.startListening();
    }

    private void setUpDegreeSpinner(){
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_course,android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        degreeSp.setAdapter(categorySpinnerAdapter);
        degreeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)){
                    if (selection.equals("B.Tech")){
                        FLAG_DEGREE = "B.Tech";
                    }
                    else if (selection.equals("M.Tech")){
                        FLAG_DEGREE = "M.Tech";
                    }
                    else if (selection.equals("PhD")){
                        FLAG_DEGREE = "PhD";
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpSemesterSpinner() {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_semester,android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        semSp.setAdapter(categorySpinnerAdapter);
        semSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)){
                    if (selection.equals("1 Sem")){
                        FLAG_SEM = "1 Sem";
                    }
                    else if (selection.equals("2 Sem")){
                        FLAG_SEM = "2 Sem";
                    }
                    else if (selection.equals("3 Sem")){
                        FLAG_SEM = "3 Sem";
                    }else if (selection.equals("4 Sem")){
                        FLAG_SEM = "4 Sem";
                    }
                    else if (selection.equals("5 Sem")){
                        FLAG_SEM = "5 Sem";
                    }
                    else if (selection.equals("6 Sem")){
                        FLAG_SEM = "6 Sem";
                    }
                    else if (selection.equals("7 Sem")){
                        FLAG_SEM = "7 Sem";
                    }
                    else if (selection.equals("8 Sem")){
                        FLAG_SEM = "8 Sem";
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_books, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.search_books) {
            dialog.show();
        }

        return true;
    }

    private void setUpBranchSpinner() {
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_user_branch,android.R.layout.simple_spinner_dropdown_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        branchSp.setAdapter(categorySpinnerAdapter);
        branchSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)){
                    if (!TextUtils.isEmpty(selection)) {
                        switch (selection) {
                            case "Computer Science":
                                FLAG_BRANCH = "Computer Science Engineering";
                                break;
                            case "Instrumentation and Control":
                                FLAG_BRANCH = "Instrumentation and Control";
                                break;
                            case "Electronics And Comm.":
                                FLAG_BRANCH = "Electronics And Comm.";
                                break;
                            case "Electrical":
                                FLAG_BRANCH = "Electrical Engineering";
                                break;
                            case "Mechanical":
                                FLAG_BRANCH = "Mechanical Engineering";
                                break;
                            case "Information Technology":
                                FLAG_BRANCH = "Information Technology";
                                break;
                            case "Chemical":
                                FLAG_BRANCH = "Chemical Engineering";
                                break;
                            case "Industrial And Production":
                                FLAG_BRANCH = "Industrial And Production";
                                break;
                            case "Civil":
                                FLAG_BRANCH = "Civil Engineering";
                                break;
                            case "Biotechnology":
                                FLAG_BRANCH = "Biotechnology";
                                break;
                            case "Textile":
                                FLAG_BRANCH = "Textile";
                                break;
                            case "Humanities":
                                FLAG_BRANCH = "Humanities";
                                break;
                            case "Chemistry":
                                FLAG_BRANCH = "Chemistry";
                                break;
                            case "Physics ":
                                FLAG_BRANCH = "Physics";
                                break;
                            case "Mathematics":
                                FLAG_BRANCH = "Mathematics";
                                break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private class BookHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView author;
        CardView booksCard;
        public BookHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.bookName);
            author = (TextView) itemView.findViewById(R.id.bookAuthor);
            booksCard = (CardView) itemView.findViewById(R.id.booksPdfCard);
        }
    }
}
