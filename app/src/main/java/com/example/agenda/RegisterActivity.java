package com.example.agenda;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agenda.modelo.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText usernameEditText, emailEditText, passwordEditText, nombresEditText, apellidosEditText, numeroTelefonicoEditText;
    private Button btn_register, btn_seleccionar_foto;
    private ImageButton btn_regresar;
    private Uri imagenUri;
    private ImageView Img_perfil;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int REQUEST_PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("imagenes_perfil");

        usernameEditText = findViewById(R.id.username);
        nombresEditText = findViewById(R.id.nombres);
        apellidosEditText = findViewById(R.id.apellidos);
        numeroTelefonicoEditText = findViewById(R.id.numeroTelefonico);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        btn_register = findViewById(R.id.btn_register);
        btn_regresar = findViewById(R.id.btn_regresar);
        Img_perfil = findViewById(R.id.Img_perfil);
        btn_seleccionar_foto = findViewById(R.id.btn_seleccionar_foto);

        btn_register.setOnClickListener(v -> registrarUsuario(this));
        btn_regresar.setOnClickListener(v -> regresar(this));
        verificarPermisos();
        btn_seleccionar_foto.setOnClickListener(v -> mostrarOpciones(this));
    }

    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        }
    }

    private void mostrarOpciones(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Seleccionar imagen")
                .setItems(new CharSequence[]{"Abrir Galería", "Tomar una foto"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            abrirGaleria();
                            break;
                        case 1:
                            abrirCamara();
                            break;
                    }
                })
                .show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                if (data != null && data.getData() != null) {
                    imagenUri = data.getData();
                    if (imagenUri != null) {
                        Img_perfil.setImageURI(imagenUri);
                    }
                }
            } else if (requestCode == CAMERA_REQUEST) {
                if (data != null && data.getExtras() != null) {
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    if (imageBitmap != null) {
                        imagenUri = getImageUri(this, imageBitmap);
                        Img_perfil.setImageBitmap(imageBitmap);
                    }
                }
            }
        } else {
            Toast.makeText(this, "Acción cancelada o error en la captura", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Permisos necesarios para continuar", Toast.LENGTH_SHORT).show();
            }
        }
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

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(nombres) || TextUtils.isEmpty(apellidos) ||
                TextUtils.isEmpty(numeroString) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Por favor complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            passwordEditText.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            Usuario nuevoUsuario = new Usuario(uid, username, nombres, apellidos, Integer.parseInt(numeroString), email);

                            if (imagenUri != null) {
                                StorageReference fileReference = storageReference.child(uid + ".png");
                                fileReference.putFile(imagenUri)
                                        .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                            String url = uri.toString();
                                            nuevoUsuario.setimagenUrl(url);

                                            db.collection("usuario")
                                                    .document(uid)
                                                    .set(nuevoUsuario)
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            Toast.makeText(RegisterActivity.this, "Usuario registrado con éxito!", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            Log.d("URL_IMAGEN", "URL de la imagen: " + url);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "Error al guardar los datos del usuario.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }))
                                        .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            } else {
                                db.collection("usuario")
                                        .document(uid)
                                        .set(nuevoUsuario)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Usuario registrado con éxito!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Error al guardar los datos del usuario.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error al registrar el usuario.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
