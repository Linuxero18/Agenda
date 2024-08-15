package com.example.agenda;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agenda.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText usernameEditText, emailEditText, passwordEditText, nombresEditText, apellidosEditText, numeroTelefonicoEditText;
    private Button btn_register;
    private ImageButton btn_regresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.username);
        nombresEditText = findViewById(R.id.nombres);
        apellidosEditText = findViewById(R.id.apellidos);
        numeroTelefonicoEditText = findViewById(R.id.numeroTelefonico);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        btn_register = findViewById(R.id.btn_register);
        btn_regresar = findViewById(R.id.btn_regresar);

        btn_register.setOnClickListener(v -> registrarUsuario(this));
        btn_regresar.setOnClickListener(v -> regresar(this));
    }

    private void regresar(Context c){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void registrarUsuario(Context c) {
        String username = usernameEditText.getText().toString().trim();
        String nombres = nombresEditText.getText().toString().trim();
        String apellidos = apellidosEditText.getText().toString().trim();
        String numeroString = numeroTelefonicoEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("El nombre de usuario es necesario");
            usernameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(nombres)) {
            nombresEditText.setError("Su nombre es necesario");
            nombresEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(apellidos)) {
            apellidosEditText.setError("Su apellido es necesario");
            apellidosEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(numeroString)) {
            numeroTelefonicoEditText.setError("Su número telefónico es de necesidad");
        }

        int numero;
        try {
            numero = Integer.parseInt(numeroString);
        } catch (NumberFormatException e) {
            numeroTelefonicoEditText.setError("Su número telefónico es de necesidad");
            numeroTelefonicoEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("El correo es necesario");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("La contraseña es necesaria");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            passwordEditText.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                Usuario nuevoUsuario = new Usuario(uid, username, nombres, apellidos, numero, email);

                                db.collection("usuario")
                                        .document(uid)
                                        .set(nuevoUsuario)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task){
                                                if (task.isSuccessful()){
                                                    Toast.makeText(RegisterActivity.this, "Usuario registrado con exito!.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(RegisterActivity.this, "Ocurrio un error al registrar el usuario.", Toast.LENGTH_SHORT).show();
                                                }
                                        }
                                });
                            }
                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "Ocurrio un error al registrar el usuario.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
