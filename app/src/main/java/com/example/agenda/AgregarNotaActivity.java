package com.example.agenda;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agenda.modelo.Nota;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AgregarNotaActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ImageButton btn_regresar;
    private FloatingActionButton btn_agregar_foto, btn_guardar_nota;
    private EditText et_titulo_nota, et_contenido_nota;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private ImageView imagenItemNota;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.agregarnota_main);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("America/Lima"));

        mAuth = FirebaseAuth.getInstance();
        btn_regresar = findViewById(R.id.btn_regresar);
        btn_agregar_foto = findViewById(R.id.btn_agregar_foto);
        btn_guardar_nota = findViewById(R.id.btn_guardar_nota);
        imagenItemNota = findViewById(R.id.imagenItemNota);
        et_titulo_nota = findViewById(R.id.et_titulo_nota);
        et_contenido_nota = findViewById(R.id.et_contenido_nota);

        btn_regresar.setOnClickListener(v -> regresar());
        btn_guardar_nota.setOnClickListener(v -> guardarNota());
        btn_agregar_foto.setOnClickListener(v -> mostrarOpciones());
    }

    private void mostrarOpciones() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar imagen");
        builder.setItems(new CharSequence[]{"Tomar foto", "Seleccionar desde galería"}, (dialog, which) -> {
            if (which == 0) {
                tomarFoto();
            } else {
                seleccionarDeGaleria();
            }
        });
        builder.show();
    }

    private void tomarFoto() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            Intent intentfoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intentfoto, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            } else {
                Toast.makeText(this, "Permiso de cámara requerido", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void seleccionarDeGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
        int width = 150;
        int height = 150;
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Bitmap resizedBitmap = resizeBitmap(imageBitmap);
                imagenItemNota.setImageBitmap(resizedBitmap);
                imagenItemNota.setVisibility(View.VISIBLE);
            } else if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri imageUri = data.getData();
                imagenItemNota.setImageURI(imageUri);
                imagenItemNota.setVisibility(View.VISIBLE);
            }
        }
    }

    private void guardarNota() {
        String tituloNota = et_titulo_nota.getText().toString();
        String contenidoNota = et_contenido_nota.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("America/Lima"));
        String tiempoNota = sdf.format(new Date());

        if (!tituloNota.isEmpty() && !contenidoNota.isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = mAuth.getCurrentUser().getUid();
            DocumentReference docUser = db.collection("usuario").document(uid);
            Map<String, Object> nota = new HashMap<>();
            nota.put("titulo", tituloNota);
            nota.put("contenido", contenidoNota);
            nota.put("tiempo", tiempoNota);

            docUser.collection("notas").add(nota)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                String notaId = task.getResult().getId();
                                Nota nuevaNota = new Nota(notaId, tituloNota, contenidoNota, tiempoNota, uid);

                                if (imagenItemNota.getVisibility() == View.VISIBLE) {
                                    uploadImage(notaId);
                                } else {
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("notaId", notaId);
                                    returnIntent.putExtra("titulo", tituloNota);
                                    returnIntent.putExtra("contenido", contenidoNota);
                                    returnIntent.putExtra("tiempo", tiempoNota);
                                    returnIntent.putExtra("uid", uid);
                                    setResult(RESULT_OK, returnIntent);
                                    Toast.makeText(AgregarNotaActivity.this, "Nota guardada", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } else {
                                Toast.makeText(AgregarNotaActivity.this, "Error al guardar la nota", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void uploadImage(String notaId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Uri imageUri = (Uri) imagenItemNota.getTag(); // Convertir Object a Uri

        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("notas/" + notaId + "/imagen.jpg");

            imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String uid = mAuth.getCurrentUser().getUid();
                    DocumentReference docUser = db.collection("usuario").document(uid);
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("imagenUrl", uri.toString());

                    docUser.collection("notas").document(notaId)
                            .update(updateMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("notaId", notaId);
                                        returnIntent.putExtra("titulo", et_titulo_nota.getText().toString());
                                        returnIntent.putExtra("contenido", et_contenido_nota.getText().toString());
                                        returnIntent.putExtra("tiempo", sdf.format(new Date()));
                                        returnIntent.putExtra("uid", uid);
                                        setResult(RESULT_OK, returnIntent);
                                        Toast.makeText(AgregarNotaActivity.this, "Nota guardada con imagen", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(AgregarNotaActivity.this, "Error al guardar la URL de la imagen", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                });
            }).addOnFailureListener(exception -> {
                Log.e("AgregarNotaActivity", "Error al subir la imagen", exception);
                Toast.makeText(AgregarNotaActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
            });
        }
    }


    private void regresar() {
        Intent intent = new Intent(AgregarNotaActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Metodo de abrir galeria
//    private void abrirGaleria() {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, PICK_IMAGE);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == PICK_IMAGE && data != null) {
//                Uri imagenSeleccionada = data.getData();
//                Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}