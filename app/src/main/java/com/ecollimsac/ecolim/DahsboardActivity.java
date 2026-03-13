package com.ecollimsac.ecolim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ecollimsac.ecolim.menu.estadistica.Estadistica;
import com.ecollimsac.ecolim.menu.historial.Historial;
import com.ecollimsac.ecolim.menu.misdatos.MisDatos;
import com.ecollimsac.ecolim.menu.nuevarecoleccion.NuevaRecoleccion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DahsboardActivity extends AppCompatActivity {

    CardView cvEstadistica, cvNuevaRecoleccion, cvHistorial, cvMisDatos;
    TextView txtNombreApellido, txtCodigoUser;

    Button btnCerrarSesion, btnDesarrollado;
    FirebaseAuth mAuth;
    FirebaseUser fireBaseUser;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dahsboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cvEstadistica = findViewById(R.id.cvEstadistica);
        cvNuevaRecoleccion = findViewById(R.id.cvNuevaColeccion);
        cvHistorial = findViewById(R.id.cvHistorial);
        cvMisDatos = findViewById(R.id.cvMisDatos);

        txtNombreApellido = findViewById(R.id.txtNombreApellido);
        txtCodigoUser = findViewById(R.id.txtCodigoUser);

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnDesarrollado = findViewById(R.id.btnDesarrollado);

        mAuth = FirebaseAuth.getInstance();
        fireBaseUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();



        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cerrarSesion();
            }
        });

        cvEstadistica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DahsboardActivity.this, "Es es Estadistica", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DahsboardActivity.this, Estadistica.class));
            }
        });

        cvNuevaRecoleccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DahsboardActivity.this, "esto es Gastos", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DahsboardActivity.this, NuevaRecoleccion.class));
            }
        });

        cvHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DahsboardActivity.this, "esto es ,Mi Hisorial,", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DahsboardActivity.this, Historial.class));
            }
        });


        cvMisDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DahsboardActivity.this, "Esto es Mis Datos", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DahsboardActivity.this, MisDatos.class));
            }
        });
    }

    private void cerrarSesion() {
        mAuth.signOut();
        startActivity(new Intent(DahsboardActivity.this, MainActivity.class));
        Toast.makeText(this, "Cerraste sesion exitosamente", Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    protected void onStart() {
        super.onStart();
        fireBaseUser = mAuth.getCurrentUser();
        comprobarSesion();
    }

    private void comprobarSesion() {
        if (fireBaseUser != null){
            cargarDatos();
        } else {
            startActivity(new Intent(DahsboardActivity.this, MainActivity.class));
            finish();
        }

    }

    private void cargarDatos() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        // Vamos a Firestore, a la colección "usuarios", y buscamos el documento de este UID
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Extraemos los datos exactos que guardamos en el Registro
                        String nombre = documentSnapshot.getString("nombre");
                        String apellido = documentSnapshot.getString("apellido");
                        String rol = documentSnapshot.getString("rol"); // ¡El campo clave!

                        txtNombreApellido.setText("Usuario: " + nombre + " " + apellido);
                        txtCodigoUser.setText("Código: " + uid);

                        // --- AQUÍ APLICAMOS LA LÓGICA DE ROLES ---
                        if (rol != null && rol.equals("operador")) {
                            // Si es operador, ocultamos TODO el contenedor de la estadística
                            ((View) cvEstadistica.getParent()).setVisibility(View.GONE);

                        } else if (rol != null && rol.equals("administrador")) {
                            // Si es administrador, nos aseguramos de que todo sea visible
                            ((View) cvEstadistica.getParent()).setVisibility(View.VISIBLE);

                        }

                    } else {
                        Toast.makeText(DahsboardActivity.this, "El documento del usuario no existe", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DahsboardActivity.this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}