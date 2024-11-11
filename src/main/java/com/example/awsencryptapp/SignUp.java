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
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {

    EditText txtEmail,txtPassword,txtConfirmPassword;
    Button btn_register;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setTitle("SignUp");

        txtEmail = (EditText) findViewById(R.id.email_id);
        txtPassword = (EditText)findViewById(R.id.password);
        txtConfirmPassword = (EditText)findViewById(R.id.confirm_password);
        btn_register = (Button)findViewById(R.id.register);


        firebaseAuth = FirebaseAuth.getInstance();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();
                String confirmPassword = txtConfirmPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(SignUp.this,"Please Enter Email",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(SignUp.this,"Please Enter Password",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(confirmPassword))
                {
                    Toast.makeText(SignUp.this,"Please Enter Confirm Password",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password.length()<6)
                {
                    Toast.makeText(SignUp.this,"Password is too short",Toast.LENGTH_SHORT).show();
                }

                if(password.equals(confirmPassword))
                {

                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {


                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                      startActivity(new Intent(getApplicationContext(),Login.class));
                                        Toast.makeText(SignUp.this, "Registration Successfull.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        // If sign in fails, display a message to the user.

                                        Toast.makeText(SignUp.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();

                                    }

                                    // ...
                                }
                            });
                }
            }
        });

    }
}
