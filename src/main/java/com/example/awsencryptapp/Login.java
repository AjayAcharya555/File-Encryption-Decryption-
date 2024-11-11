package com.example.awsencryptapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText txtEmail, txtPassword;
    Button btn_login;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");

        txtEmail = (EditText)findViewById(R.id.email_id);
        txtPassword = (EditText)findViewById(R.id.login_pass);
        btn_login = (Button)findViewById(R.id.login);

        firebaseAuth =FirebaseAuth.getInstance();



        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(Login.this,"Please Enter Email",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(Login.this,"Please Enter Password",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length()<6)
                {
                    Toast.makeText(Login.this,"Password is too short",Toast.LENGTH_SHORT).show();
                }

                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information

                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));


                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Login.this,"Login Failed or User not Available",Toast.LENGTH_SHORT).show();
                                }

                                // ...
                            }
                        });
            }
        });
    }

    public void btn_signup(View view)
    {
        startActivity(new Intent(getApplicationContext(),SignUp.class));
    }
}
