package com.unipi.adarmis.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.adarmis.smartalert.backend.IncidentGroup;
import com.unipi.adarmis.smartalert.backend.IncidentPoint;

import java.util.ArrayList;
import java.util.List;

public class GroupImages extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;

    private StorageReference storage;
    private FirebaseFirestore db;
    private List<Upload> mUploads;
    IncidentGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_images);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mRecyclerView.setAdapter(mAdapter)

        Bundle extras = getIntent().getExtras();
        group = (IncidentGroup) extras.get("group");

        mUploads = new ArrayList<>();

        storage = FirebaseStorage.getInstance().getReference("images");
        db = FirebaseFirestore.getInstance();


        for (IncidentPoint p : group.getIncidents()) {
            String filePath = p.getId()+".jpg";
            Uri uri = Uri.parse(filePath);
            storage.child(filePath).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Toast.makeText(GroupImages.this,uri.toString(),Toast.LENGTH_SHORT).show();
                            Upload upload = new Upload(filePath,uri.toString());
                            mUploads.add(upload);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(GroupImages.this,"Failed",Toast.LENGTH_SHORT).show();
                            Log.d("IMAGE_GET",e.getMessage());
                        }
                    });
        }

        mAdapter = new ImageAdapter(GroupImages.this,mUploads);
        Toast.makeText(GroupImages.this,String.valueOf(mUploads.size()),Toast.LENGTH_SHORT).show();
        mRecyclerView.setAdapter(mAdapter);
    }


}