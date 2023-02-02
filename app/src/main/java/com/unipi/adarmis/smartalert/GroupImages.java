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
    private List<Upload> mUploads;
    IncidentGroup group;

    int counter = 0;
    int pointCounter = 0;

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

        getImagesFromFirestore("jpg");

    }

    private void getImagesFromFirestore(String extension) {
        pointCounter = 0;
        for (IncidentPoint p : group.getIncidents()) {
            String filePath = p.getId()+"."+extension;
            Uri uri = Uri.parse(filePath);
            Log.d("IMG_FILEPATH",filePath);
            storage.child(filePath).getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Toast.makeText(GroupImages.this,uri.toString(),Toast.LENGTH_SHORT).show();
                            Upload upload = new Upload(filePath,uri.toString(),p.getDate());
                            mUploads.add(upload);
                            counter+=1;
                            Log.d("IMG_COUNTER",String.valueOf(counter));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Toast.makeText(GroupImages.this,"Failed",Toast.LENGTH_SHORT).show();
                            Log.d("IMAGE_GET",e.getMessage());
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()) {
                                mAdapter = new ImageAdapter(GroupImages.this,mUploads);
                                mRecyclerView.setAdapter(mAdapter);
                            } else {
                                if(pointCounter==group.getIncidents().size() && counter==0) {
                                    Toast.makeText(GroupImages.this,"No images found for this event.",Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }
                        }
                    });
            pointCounter+=1;
        }
    }


}