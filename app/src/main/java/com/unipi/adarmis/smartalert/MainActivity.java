package com.unipi.adarmis.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View view)
    {
        //login function
        TextView name = findViewById(R.id.username);
        TextView pass = findViewById(R.id.password);
        String username = name.getText().toString();
        String password = pass.getText().toString();
        if(username.equals(""))
        {
            Toast.makeText(this,"Username is required!",Toast.LENGTH_SHORT).show();
        }
        else if(password.equals(""))
        {
            Toast.makeText(this,"Password is required!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Intent intent = new Intent(this,UserPage.class);
            startActivity(intent);

        }

    }

}