package com.example.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class EditarNotaActivity extends AppCompatActivity {

    private ImageButton btn_regresar;
    private EditText et_titulo_nota, et_contenido_nota;
    private Button btn_actualizar_nota;
    private FirebaseFirestore db;
    private String notaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.editarnota_main);

        db = FirebaseFirestore.getInstance();

        btn_regresar = findViewById(R.id.btn_regresar);
        et_titulo_nota = findViewById(R.id.et_titulo_nota);
        et_contenido_nota = findViewById(R.id.et_contenido_nota);
        btn_actualizar_nota = findViewById(R.id.btn_guardar_nota);

        Intent intent = getIntent();
        notaId = intent.getStringExtra("notaId");
        String titulo = intent.getStringExtra("titulo");
        String contenido = intent.getStringExtra("contenido");

        et_titulo_nota.setText(titulo);
        et_contenido_nota.setText(contenido);

        btn_regresar.setOnClickListener(v -> regresar());
        btn_actualizar_nota.setOnClickListener(v -> editarNota());
    }

    private void editarNota() {
        String tituloNota = et_titulo_nota.getText().toString().trim();
        String contenidoNota = et_contenido_nota.getText().toString().trim();

        if (tituloNota.isEmpty() || contenidoNota.isEmpty()) {
            Toast.makeText(this, "El título y el contenido no pueden estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedNota = new HashMap<>();
        updatedNota.put("titulo", tituloNota);
        updatedNota.put("contenido", contenidoNota);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("America/Lima"));
        String tiempoModificado = sdf.format(new Date());
        updatedNota.put("tiempo", tiempoModificado);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("usuario").document(uid).collection("notas").document(notaId)
                .update(updatedNota)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditarNotaActivity.this, "Nota actualizada", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("notaId", notaId);
                        intent.putExtra("titulo", tituloNota);
                        intent.putExtra("contenido", contenidoNota);
                        intent.putExtra("tiempo", tiempoModificado);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Log.e("EditarNotaActivity", "Error al actualizar la nota, Es posible que haya sido eliminada en la base de datos.");
                        Toast.makeText(EditarNotaActivity.this, "Error al actualizar la nota: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void regresar() {
        Intent intent = new Intent(EditarNotaActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}