package com.example.a300cem_assignment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.a300cem_assignment.Common.Common;
import com.example.a300cem_assignment.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    EditText edtPhone, edtPassword;
    Button btnLogin;
    public static final String NAME_KEY = "NAME_KEY";
    public static final String PASSWORD_KEY = "PASSWORD_KEY";
    public static final String CHECKBOX_KEY = "CHECKBOX_KEY";
    private SharedPreferences sharedPreferences;
    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtPhone = (EditText) findViewById(R.id.edtPhone);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        checkBox = (CheckBox) findViewById(R.id.checkbox);

        //Get login info form shared preference
        sharedPreferences = getSharedPreferences("MySharedPreMain", Context.MODE_PRIVATE);

        if (sharedPreferences.contains(NAME_KEY)) {
            edtPhone.setText(sharedPreferences.getString(NAME_KEY, ""));
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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Dialog box when function is loading
                final ProgressDialog loadDialog = new ProgressDialog(Login.this);
                loadDialog.setMessage("Please wait...");
                loadDialog.show();
                if (edtPhone.getText().toString().equals("")){
                    loadDialog.dismiss();
                    Toast.makeText(Login.this, "Please type phone", Toast.LENGTH_SHORT).show();
                } else {
                //Save login info to shared preference
                if (checkBox.isChecked()) {
                    checkBox.setChecked(true);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(NAME_KEY, edtPhone.getText().toString());
                    editor.putString(PASSWORD_KEY, edtPassword.getText().toString());
                    editor.putBoolean(CHECKBOX_KEY, checkBox.isChecked());
                    editor.commit();
                }
                if (!checkBox.isChecked()) {
                    checkBox.setChecked(false);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(NAME_KEY, "");
                    editor.putString(PASSWORD_KEY, "");
                    editor.putBoolean(CHECKBOX_KEY, checkBox.isChecked());
                    editor.commit();
                }

                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Check if user exists
                        if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                            //Get user info
                            loadDialog.dismiss();
                            User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                            if (user.getPassword().equals(edtPassword.getText().toString())) {
                                {
                                    Intent home = new Intent(Login.this, Home.class);
                                    Common.currentUser = user;
                                    home.putExtra("UserId", edtPhone.getText().toString());
                                    startActivity(home);
                                    finish();
                                }
                            } else {
                                Toast.makeText(Login.this, "Login failed!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            loadDialog.dismiss();
                            Toast.makeText(Login.this, "User doesn't exists!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }}
        });
    }
}
