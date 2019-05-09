package com.example.a300cem_assignment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.a300cem_assignment.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    EditText edtPhone, edtName, edtPassword;
    Button btnSignUp;
    FirebaseAuth firebaseAuth;

    String userName, userEmail, userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setUpView();

        firebaseAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Dialog box when function is loading
                final ProgressDialog loadDialog = new ProgressDialog(SignUp.this);
                loadDialog.setMessage(getString(R.string.wait));
                loadDialog.show();
                if (validateForm()) {
                    String user_email = edtPhone.getText().toString().trim();
                    String user_password = edtPassword.getText().toString().trim();

                    firebaseAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                loadDialog.dismiss();
                                saveUserData();
                                Toast.makeText(SignUp.this, getString(R.string.signUpCompleted), Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(SignUp.this, MainActivity.class));
                            } else {
                                //Catch Error
                                try {
                                    throw task.getException();
                                }
                                //Weak password
                                catch (FirebaseAuthWeakPasswordException weakPassword)
                                {
                                    loadDialog.dismiss();
                                    Toast.makeText(SignUp.this, getString(R.string.passwordWeak), Toast.LENGTH_SHORT).show();
                                }
                                //Email malformed
                                catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                                {
                                    loadDialog.dismiss();
                                    Toast.makeText(SignUp.this, getString(R.string.wrongTypeEmail), Toast.LENGTH_SHORT).show();
                                }
                                //Email exists
                                catch (FirebaseAuthUserCollisionException existEmail) {
                                    loadDialog.dismiss();
                                    Toast.makeText(SignUp.this, getString(R.string.existEmail), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    loadDialog.dismiss();
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    firebaseAuth.signOut();
                }
            }
        });
    }

    private void saveUserData() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userTable = firebaseDatabase.getReference("User");
        User user = new User(userName, userEmail, userPassword);
        userTable.child(firebaseAuth.getUid()).setValue(user);
    }

    private void setUpView() {
        edtPhone = findViewById(R.id.edtEmail);
        edtName = findViewById(R.id.edtName);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
    }

    private boolean validateForm() {
        boolean result = false;
        userName = edtName.getText().toString();
        userEmail = edtPhone.getText().toString();
        userPassword = edtPassword.getText().toString();

        if (userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(SignUp.this, getString(R.string.fillInfo), Toast.LENGTH_SHORT).show();
        } else {
            result = true;
        }
        return result;
    }
}
