package com.example.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button btn_login, btn_registrarse;
    private EditText correo, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_main);

        mAuth = FirebaseAuth.getInstance();
        btn_login = findViewById(R.id.btn_login);
        btn_registrarse = findViewById(R.id.btn_registrarse);
        correo = findViewById(R.id.correo);
        password = findViewById(R.id.password);

        btn_login.setOnClickListener(v -> iniciarSesion());
        btn_registrarse.setOnClickListener(v -> registrarse());
    }

    private void iniciarSesion() {
        String email = correo.getText().toString().trim();
        String pass = password.getText().toString().trim();


        if (TextUtils.isEmpty(email)) {
            correo.setError("El correo es necesario");
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            password.setError("La contraseña es necesaria");
            return;
        }
        if (pass.length() < 6) {
            password.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error en la autenticación: Correo o Contraseña incorrecta.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registrarse() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}