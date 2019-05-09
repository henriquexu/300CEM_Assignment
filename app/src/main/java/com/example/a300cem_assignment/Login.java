package com.example.a300cem_assignment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.a300cem_assignment.Common.Common;
import com.example.a300cem_assignment.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    Button btnLogin;
    public static final String EMAIL_KEY = "EMAIL_KEY";
    public static final String PASSWORD_KEY = "PASSWORD_KEY";
    public static final String CHECKBOX_KEY = "CHECKBOX_KEY";
    private SharedPreferences sharedPreferences;
    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        checkBox = findViewById(R.id.checkbox);

        //Get login info form shared preference
        sharedPreferences = getSharedPreferences("MySharedPreMain", Context.MODE_PRIVATE);

        if (sharedPreferences.contains(EMAIL_KEY)) {
            edtEmail.setText(sharedPreferences.getString(EMAIL_KEY, ""));
        }

        if (sharedPreferences.contains(PASSWORD_KEY)) {
            edtPassword.setText(sharedPreferences.getString(PASSWORD_KEY, ""));
        }
        if (sharedPreferences.contains(CHECKBOX_KEY)) {
            checkBox.setChecked(sharedPreferences.getBoolean("CHECKBOX_KEY", false));
        }

        //Init Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseAuth.signOut();
        }


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Dialog box when function is loading
                final ProgressDialog loadDialog = new ProgressDialog(Login.this);
                loadDialog.setMessage(getString(R.string.wait));
                loadDialog.show();
                if (edtEmail.getText().toString().isEmpty() || edtPassword.getText().toString().isEmpty()) {
                    loadDialog.dismiss();
                    Toast.makeText(Login.this, getString(R.string.fillInfo), Toast.LENGTH_SHORT).show();
                } else {
                    //Save login info to shared preference
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(true);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(EMAIL_KEY, edtEmail.getText().toString());
                        editor.putString(PASSWORD_KEY, edtPassword.getText().toString());
                        editor.putBoolean(CHECKBOX_KEY, checkBox.isChecked());
                        editor.apply();
                    }
                    if (!checkBox.isChecked()) {
                        checkBox.setChecked(false);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(EMAIL_KEY, "");
                        editor.putString(PASSWORD_KEY, "");
                        editor.putBoolean(CHECKBOX_KEY, checkBox.isChecked());
                        editor.apply();
                    }

                    firebaseAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                table_user.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        loadDialog.dismiss();
                                        Common.currentUser = dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()).getValue(User.class);
                                        finish();
                                        Toast.makeText(Login.this, getString(R.string.loginSuccess), Toast.LENGTH_SHORT).show();
                                        Intent home = new Intent(Login.this, Home.class);
                                        home.putExtra("UserId", firebaseAuth.getUid());
                                        startActivity(home);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                try
                                {
                                    throw task.getException();
                                }
                                //Wrong email.
                                catch (FirebaseAuthInvalidUserException invalidEmail)
                                {
                                    loadDialog.dismiss();
                                    Toast.makeText(Login.this, getString(R.string.userNotExist), Toast.LENGTH_SHORT).show();
                                }
                                //Wrong password.
                                catch (FirebaseAuthInvalidCredentialsException wrongPassword)
                                {
                                    loadDialog.dismiss();
                                    Toast.makeText(Login.this, getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e)
                                {
                                    loadDialog.dismiss();
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
                }
            }
        });
    }
}
