package com.ecollimsac.ecolim.menu.historial;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecollimsac.ecolim.R;
import com.ecollimsac.ecolim.model.RecoleccionRegistro;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Historial extends AppCompatActivity {

    private RecyclerView rvHistorial;
    private TextView tvVacio;
    private ProgressBar progressBar;
    private HistorialAdapter adapter;
    private final List<RecoleccionRegistro> data = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvHistorial = findViewById(R.id.rvHistorial);
        tvVacio = findViewById(R.id.tvHistorialVacio);
        progressBar = findViewById(R.id.progressHistorial);

        adapter = new HistorialAdapter(data);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        rvHistorial.setAdapter(adapter);

        cargarHistorial();
    }

    private void cargarHistorial() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, R.string.msg_sesion_requerida, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String rol = documentSnapshot.getString("rol");
                    boolean esAdministrador = rol != null && rol.equalsIgnoreCase("administrador");
                    consultarRecolecciones(uid, esAdministrador);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.msg_error, e.getMessage()), Toast.LENGTH_LONG).show();
                });
    }

    private void consultarRecolecciones(String uid, boolean esAdministrador) {
        Query query = esAdministrador
                ? db.collection("recolecciones")
                : db.collection("recolecciones").whereEqualTo("uid", uid);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    data.clear();
                    queryDocumentSnapshots.getDocuments().forEach(document -> data.add(RecoleccionRegistro.fromDocument(document)));
                    data.sort(Comparator.comparing(
                            RecoleccionRegistro::getCreatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    ));

                    if (esAdministrador) {
                        cargarNombresUsuariosYRender();
                    } else {
                        adapter.setModoAdministrador(false);
                        adapter.setNombresPorUid(null);
                        renderData();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvVacio.setVisibility(View.VISIBLE);
                    Toast.makeText(this, getString(R.string.msg_error, e.getMessage()), Toast.LENGTH_LONG).show();
                });
    }

    private void cargarNombresUsuariosYRender() {
        db.collection("usuarios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, String> nombresPorUid = new HashMap<>();
                    queryDocumentSnapshots.getDocuments().forEach(document -> {
                        String nombre = document.getString("nombre");
                        String apellido = document.getString("apellido");
                        String display = construirNombre(nombre, apellido);
                        nombresPorUid.put(document.getId(), display);
                    });

                    adapter.setModoAdministrador(true);
                    adapter.setNombresPorUid(nombresPorUid);
                    renderData();
                })
                .addOnFailureListener(e -> {
                    // Si falla la carga de nombres, igual mostramos historial con UID como respaldo.
                    adapter.setModoAdministrador(true);
                    adapter.setNombresPorUid(null);
                    renderData();
                });
    }

    private String construirNombre(String nombre, String apellido) {
        String n = nombre == null ? "" : nombre.trim();
        String a = apellido == null ? "" : apellido.trim();
        String full = (n + " " + a).trim();
        if (TextUtils.isEmpty(full)) {
            return getString(R.string.md_rol_no_definido);
        }
        return full;
    }

    private void renderData() {
        progressBar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        tvVacio.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }
}