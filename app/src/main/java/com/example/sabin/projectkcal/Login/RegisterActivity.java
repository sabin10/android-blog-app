package com.example.sabin.projectkcal.Login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sabin.projectkcal.MainActivity;
import com.example.sabin.projectkcal.R;
import com.example.sabin.projectkcal.SetupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText mEmail;
    EditText mPassword;
    EditText mConfirmPassword;
    Button mRegisterBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mEmail = (EditText) findViewById(R.id.email_field);
        mPassword = (EditText) findViewById(R.id.password_field);
        mConfirmPassword = (EditText) findViewById(R.id.confirm_password_field);
        mRegisterBtn = (Button) findViewById(R.id.register_button);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                String pass = mPassword.getText().toString();
                String confirmPass = mConfirmPassword.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmPass)) {

                    if (pass.equals(confirmPass)) {

                        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    sendToSetup();

                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(RegisterActivity.this, "Passwords doesn't match", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(RegisterActivity.this, "Empty fields", Toast.LENGTH_LONG).show();
                }


            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sendToMain();
        }

    }

    private void sendToMain() {
        Intent toMain = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(toMain);
        finish();
    }

    private void sendToSetup() {
        Intent toSetup = new Intent(RegisterActivity.this, SetupActivity.class);
        startActivity(toSetup);
        finish();
    }

    public void clickToLogin(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }
}
