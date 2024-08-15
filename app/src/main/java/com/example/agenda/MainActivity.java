package com.example.agenda;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agenda.modelo.Nota;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private static final int AGREGAR_NOTA_CODE = 1;
    private static final int EDITAR_NOTA_CODE = 2;
    private LinearLayout contenedorNotas;
    private FloatingActionButton btn_agregar_nota;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImageButton btn_regresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        contenedorNotas = findViewById(R.id.contenedorNotas);
        btn_agregar_nota = findViewById(R.id.btn_agregar_nota);
        btn_regresar = findViewById(R.id.btn_regresar);

        btn_agregar_nota.setOnClickListener(v -> agregarNota());
        btn_regresar.setOnClickListener(v -> regresar(this));
        cargarNotas();
    }

    private void regresar(Context c){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void agregarNota() {
        Intent intent = new Intent(MainActivity.this, AgregarNotaActivity.class);
        startActivityForResult(intent, AGREGAR_NOTA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == AGREGAR_NOTA_CODE || requestCode == EDITAR_NOTA_CODE) && resultCode == RESULT_OK && data != null) {
            cargarNotas();
        }
    }

    private void cargarNotas() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("usuario")
                .document(uid)
                .collection("notas")
                .orderBy("tiempo", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        contenedorNotas.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String notaId = document.getId();
                            String titulo = document.getString("titulo");
                            String contenido = document.getString("contenido");
                            String tiempo = document.getString("tiempo");

                            if (titulo != null && contenido != null && tiempo != null) {
                                Nota nuevaNota = new Nota(notaId, titulo, contenido, tiempo, uid);
                                agregarNotas(nuevaNota);
                            } else {
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error al cargar las notas", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void agregarNotas(Nota nota) {
        View v = getLayoutInflater().inflate(R.layout.item_nota, null);

        TextView tituloNota = v.findViewById(R.id.tituloItemNota);
        TextView contenidoNota = v.findViewById(R.id.contenidoItemNota);
        TextView contenidoCompletoNota = v.findViewById(R.id.contenidoCompletoItemNota);
        TextView tiempoNota = v.findViewById(R.id.tiempoItemNota);
        TextView btnVerMas = v.findViewById(R.id.btn_ver_mas);

        tituloNota.setText(nota.getTitulo());
        String contenido = nota.getContenido();
        contenidoNota.setText(contenido.length() > 100 ? contenido.substring(0, 100) + "..." : contenido);
        contenidoCompletoNota.setText(contenido);
        tiempoNota.setText(nota.getTiempo());

        btnVerMas.setVisibility(contenido.length() > 100 ? View.VISIBLE : View.GONE);

        btnVerMas.setOnClickListener(view -> {
            if (contenidoCompletoNota.getVisibility() == View.GONE) {
                contenidoCompletoNota.setVisibility(View.VISIBLE);
                contenidoNota.setVisibility(View.GONE);
                btnVerMas.setText("Ver menos");
            } else {
                contenidoCompletoNota.setVisibility(View.GONE);
                contenidoNota.setVisibility(View.VISIBLE);
                btnVerMas.setText("Ver más");
            }
        });

        v.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditarNotaActivity.class);
            intent.putExtra("notaId", nota.getId());
            intent.putExtra("titulo", nota.getTitulo());
            intent.putExtra("contenido", nota.getContenido());
            intent.putExtra("tiempo", nota.getTiempo());
            startActivityForResult(intent, EDITAR_NOTA_CODE);
        });

        contenedorNotas.addView(v);

        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                15
        ));
        divider.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        contenedorNotas.addView(divider);
    }
}