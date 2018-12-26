package com.example.sabin.projectkcal.Login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sabin.projectkcal.MainActivity;
import com.example.sabin.projectkcal.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText mEmail;
    EditText mPassword;
    Button mLoginBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mEmail = (EditText) findViewById(R.id.email_field);
        mPassword = (EditText) findViewById(R.id.password_field);
        mLoginBtn = (Button) findViewById(R.id.login_button);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mInputEmail = mEmail.getText().toString();
                String mInputPass = mPassword.getText().toString();

                //if inputs are not empty
                if (!TextUtils.isEmpty(mInputEmail) && !TextUtils.isEmpty(mInputPass)) {

                    mAuth.signInWithEmailAndPassword(mInputEmail, mInputPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                sendToMain();
                            } else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                } else
                    Toast.makeText(LoginActivity.this, "Error: Your input is empty", Toast.LENGTH_LONG).show();

            }
        });


    }

    public void clickToRegister(View view) {
        //Toast.makeText(LoginActivity.this, "AI APASAT!", Toast.LENGTH_LONG).show();
        // intent catre RegisterActivity
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();

    }

}


