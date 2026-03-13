package com.ecollimsac.ecolim.menu.nuevarecoleccion;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ecollimsac.ecolim.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NuevaRecoleccion extends AppCompatActivity {

    private Spinner spTipoResiduo;
    private Spinner spClasificacion;
    private Spinner spAreaOrigen;
    private EditText etPesoKg;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<String> tiposResiduo = Arrays.asList(
            "Seleccione una opcion",
            "Aprovechable",
            "No Aprovechable",
            "Organico",
            "Peligroso"
    );

    private final List<String> areasOrigen = Arrays.asList(
            "Seleccione una opcion",
            "Planta Principal",
            "Comedor",
            "Almacen",
            "Oficinas"
    );

    private final Map<String, List<String>> clasificacionesPorTipo = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nueva_recoleccion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initClasificaciones();
        initViews();
        setupSpinners();
        setupEvents();
    }

    private void initViews() {
        spTipoResiduo = findViewById(R.id.spTipoResiduo);
        spClasificacion = findViewById(R.id.spClasificacion);
        spAreaOrigen = findViewById(R.id.spAreaOrigen);
        etPesoKg = findViewById(R.id.etPesoKg);
        progressBar = findViewById(R.id.progressNuevaRecoleccion);
    }

    private void setupEvents() {
        Button btnRegistrar = findViewById(R.id.btnRegistrarRecoleccion);

        spTipoResiduo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                actualizarClasificaciones(spTipoResiduo.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Sin accion.
            }
        });

        btnRegistrar.setOnClickListener(v -> validarYRegistrar());
    }

    private void setupSpinners() {
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tiposResiduo
        );
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoResiduo.setAdapter(tipoAdapter);

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                areasOrigen
        );
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAreaOrigen.setAdapter(areaAdapter);

        actualizarClasificaciones(tiposResiduo.get(0));
    }

    private void initClasificaciones() {
        clasificacionesPorTipo.put("Aprovechable", Arrays.asList(
                "Seleccione una opcion", "Papel", "Carton", "Plastico", "Vidrio", "Metal"
        ));
        clasificacionesPorTipo.put("No Aprovechable", Arrays.asList(
                "Seleccione una opcion", "Papel higienico", "Envolturas contaminadas", "Ceramica"
        ));
        clasificacionesPorTipo.put("Organico", Arrays.asList(
                "Seleccione una opcion", "Restos de comida", "Cesped", "Hojas", "Madera"
        ));
        clasificacionesPorTipo.put("Peligroso", Arrays.asList(
                "Seleccione una opcion", "Pilas", "Aceite industrial", "Quimicos", "Medicamentos"
        ));
    }

    private void actualizarClasificaciones(String tipoResiduo) {
        List<String> opciones = clasificacionesPorTipo.get(tipoResiduo);
        if (opciones == null) {
            opciones = new ArrayList<>();
            opciones.add(getString(R.string.nr_opcion));
        }

        ArrayAdapter<String> clasificacionAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opciones
        );
        clasificacionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spClasificacion.setAdapter(clasificacionAdapter);
    }

    private void validarYRegistrar() {
        String tipo = spTipoResiduo.getSelectedItem().toString();
        String clasificacion = spClasificacion.getSelectedItem().toString();
        String pesoTexto = etPesoKg.getText().toString().trim();
        String area = spAreaOrigen.getSelectedItem().toString();

        if ("Seleccione una opcion".equals(tipo)) {
            Toast.makeText(this, R.string.msg_seleccione_tipo, Toast.LENGTH_SHORT).show();
            return;
        }
        if ("Seleccione una opcion".equals(clasificacion)) {
            Toast.makeText(this, R.string.msg_seleccione_clasificacion, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pesoTexto)) {
            Toast.makeText(this, R.string.msg_ingrese_peso, Toast.LENGTH_SHORT).show();
            return;
        }

        double pesoKg;
        try {
            pesoKg = Double.parseDouble(pesoTexto);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.msg_ingrese_peso, Toast.LENGTH_SHORT).show();
            return;
        }

        if (pesoKg <= 0) {
            Toast.makeText(this, R.string.msg_ingrese_peso, Toast.LENGTH_SHORT).show();
            return;
        }
        if ("Seleccione una opcion".equals(area)) {
            Toast.makeText(this, R.string.msg_seleccione_area, Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getUid();
        if (uid == null) {
            Toast.makeText(this, R.string.msg_sesion_requerida, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        guardarEnFirestore(uid, tipo, clasificacion, pesoKg, area);
    }

    private void guardarEnFirestore(String uid, String tipo, String clasificacion, double pesoKg,
                                    String area) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("tipoResiduo", tipo);
        data.put("clasificacion", clasificacion);
        data.put("pesoKg", pesoKg);
        data.put("areaOrigen", area);
        data.put("fotoUrl", "");
        data.put("estado", "Subido a la nube");
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("recolecciones")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);
                    limpiarFormulario();
                    Toast.makeText(this, R.string.msg_registro_ok, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.msg_error, e.getMessage()), Toast.LENGTH_LONG).show();
                });
    }

    private void limpiarFormulario() {
        spTipoResiduo.setSelection(0);
        spAreaOrigen.setSelection(0);
        etPesoKg.setText("");
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}