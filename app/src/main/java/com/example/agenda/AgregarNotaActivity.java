package com.example.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agenda.modelo.Nota;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AgregarNotaActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ImageButton btn_regresar;
    private Button btn_guardar_nota;
    private EditText et_titulo_nota, et_contenido_nota;
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
        btn_guardar_nota = findViewById(R.id.btn_guardar_nota);
        et_titulo_nota = findViewById(R.id.et_titulo_nota);
        et_contenido_nota = findViewById(R.id.et_contenido_nota);

        btn_regresar.setOnClickListener(v -> regresar());
        btn_guardar_nota.setOnClickListener(v -> guardarNota());
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

                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("notaId", notaId);
                                returnIntent.putExtra("titulo", tituloNota);
                                returnIntent.putExtra("contenido", contenidoNota);
                                returnIntent.putExtra("tiempo", tiempoNota);
                                returnIntent.putExtra("uid", uid);
                                setResult(RESULT_OK, returnIntent);
                                Toast.makeText(AgregarNotaActivity.this, "Nota guardada", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(AgregarNotaActivity.this, "Error al guardar la nota", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void regresar() {
        Intent intent = new Intent(AgregarNotaActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
