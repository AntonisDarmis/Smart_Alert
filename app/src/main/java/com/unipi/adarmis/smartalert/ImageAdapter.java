package com.unipi.adarmis.smartalert;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context mContext;
    private List<Upload> mUploads;

    private FirebaseStorage storage;
    private FirebaseFirestore db;

    public ImageAdapter(Context context, List<Upload> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Upload uploadCurrent = mUploads.get(position);
        holder.textViewName.setText(uploadCurrent.getDate().toString());
        Picasso.with(mContext)
                .load(uploadCurrent.getImageUrl())
                .fit()
                .centerCrop()
                .into(holder.imageView);

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //storage = FirebaseStorage.getInstance();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("images");

                // Create a reference to the file to delete
                Log.d("IMAGEURL",uploadCurrent.getName());
                StorageReference desertRef = storageRef.child(uploadCurrent.getName());

                // Delete the file
                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteIncident(uploadCurrent.getName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                    }
                });


            }
        });
    }

    public void deleteIncident(String imgName) {
        int length = imgName.length();
        String incidentId = imgName.substring(0,length-4);
        Log.d("INCIDENT_ID",incidentId);
        db = FirebaseFirestore.getInstance();
        db.collection("incidents").document(incidentId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(mContext, "Successfully deleted report",Toast.LENGTH_SHORT).show();
                            Intent intent  = new Intent(mContext,Incidents.class);
                            mContext.startActivity(intent);
                            ((Activity) mContext).finish();
                        } else {
                            Toast.makeText(mContext, "Delete failed",Toast.LENGTH_SHORT).show();
                            Log.d("DELETE_INCIDENT","Delete failed");
                            //Intent intent  = new Intent(mContext,Incidents.class);
                            //mContext.startActivity(intent);
                            //((Activity) mContext).finish();
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public ImageView imageView;

        public Button deleteButton;

        public ImageViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewImageDate);
            imageView = itemView.findViewById(R.id.imageViewUpload);
            deleteButton = itemView.findViewById(R.id.deleteImageButton);

        }
    }
}
