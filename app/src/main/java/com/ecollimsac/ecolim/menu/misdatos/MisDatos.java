package com.ecollimsac.ecolim.menu.misdatos;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ecollimsac.ecolim.MainActivity;
import com.ecollimsac.ecolim.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MisDatos extends AppCompatActivity {

    private EditText etNombre, etApellido;
    private TextView tvCorreo, tvUid, tvNivelAcceso;
    private Button btnGuardarNombre;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mis_datos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        tvCorreo = findViewById(R.id.tvCorreo);
        tvUid = findViewById(R.id.tvUid);
        tvNivelAcceso = findViewById(R.id.tvNivelAcceso);
        btnGuardarNombre = findViewById(R.id.btnGuardarNombre);

        btnGuardarNombre.setOnClickListener(v -> guardarCambiosNombre());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!validarSesion()) {
            return;
        }
        cargarDatosUsuario();
    }

    private boolean validarSesion() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, getString(R.string.msg_sesion_requerida), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MisDatos.this, MainActivity.class));
            finish();
            return false;
        }
        return true;
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        String email = user.getEmail();

        tvUid.setText(uid);
        tvCorreo.setText(TextUtils.isEmpty(email) ? "-" : email);

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        tvNivelAcceso.setText(getString(R.string.md_nivel_acceso_valor, getString(R.string.md_rol_no_definido)));
                        return;
                    }

                    String nombre = documentSnapshot.getString("nombre");
                    String apellido = documentSnapshot.getString("apellido");
                    String rol = documentSnapshot.getString("rol");

                    etNombre.setText(TextUtils.isEmpty(nombre) ? "" : nombre);
                    etApellido.setText(TextUtils.isEmpty(apellido) ? "" : apellido);

                    String rolFormateado = formatearRol(rol);
                    tvNivelAcceso.setText(getString(R.string.md_nivel_acceso_valor, rolFormateado));
                })
                .addOnFailureListener(e -> Toast.makeText(
                        MisDatos.this,
                        getString(R.string.msg_error, e.getMessage()),
                        Toast.LENGTH_SHORT
                ).show());
    }

    private void guardarCambiosNombre() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido)) {
            Toast.makeText(this, R.string.md_datos_incompletos, Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> cambios = new HashMap<>();
        cambios.put("nombre", nombre);
        cambios.put("apellido", apellido);

        db.collection("usuarios").document(user.getUid())
                .set(cambios, SetOptions.merge())
                .addOnSuccessListener(unused -> Toast.makeText(
                        MisDatos.this,
                        R.string.md_datos_actualizados,
                        Toast.LENGTH_SHORT
                ).show())
                .addOnFailureListener(e -> Toast.makeText(
                        MisDatos.this,
                        getString(R.string.msg_error, e.getMessage()),
                        Toast.LENGTH_SHORT
                ).show());
    }

    private String formatearRol(String rol) {
        if (TextUtils.isEmpty(rol)) {
            return getString(R.string.md_rol_no_definido);
        }
        String lower = rol.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}