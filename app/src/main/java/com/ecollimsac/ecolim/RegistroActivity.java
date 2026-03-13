package com.ecollimsac.ecolim;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.HashMap;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistroActivity extends AppCompatActivity {

    private TextInputLayout etNuevoUsuario, etNuevoAppelido, etNuevoCorreo,etNuevaPassword, etConfirmarPassword;
    private Button btnRegistrar;
    TextView lblLogin;

    FirebaseAuth mAuth;
    String nombre="", apellido="", password="",correo="", confirmarPassword="";
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        etNuevoUsuario= findViewById(R.id.RNombreUsuario);
        etNuevoAppelido = findViewById(R.id.RApellidoUsuario);
        etNuevoCorreo = findViewById(R.id.REmailUsuario);
        etNuevaPassword = findViewById(R.id.RNuevaPass);
        etConfirmarPassword = findViewById(R.id.RConfirmarPass);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        //generar las intasncias de firebase
        // instanciar el progeres dialog

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espera porfavor..");
        progressDialog.setCanceledOnTouchOutside(false);

        lblLogin = findViewById(R.id.lblVolverLogin);
        lblLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistroActivity.this, MainActivity.class));
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidarDatos();
            }
        });
    }

    private void ValidarDatos() {
        nombre = etNuevoUsuario.getEditText().getText().toString().trim();
        apellido = etNuevoAppelido.getEditText().getText().toString().trim();
        correo = etNuevoCorreo.getEditText().getText().toString().trim();
        password = etNuevaPassword.getEditText().getText().toString().trim();
        confirmarPassword = etConfirmarPassword.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "esta vacio el campo nombre", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(apellido)) {
            Toast.makeText(this, "esta vacio el apellido", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingresa un correo valido", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)|| password.length()<8) {
            Toast.makeText(this, "Ingrese un password  como minimo de 8 letters", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(confirmarPassword)|| confirmarPassword.length()<8) {
            Toast.makeText(this, "esta vacio el apellido", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmarPassword)) {
            Toast.makeText(this, "las contraseñas deben ser iguales", Toast.LENGTH_SHORT).show();
        }else {
            registrar();
        }
    }

    private void registrar() {
        progressDialog.setMessage("Registrando...");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        guardarUsuario();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegistroActivity.this, "Este correo ya está registrado. Intenta iniciar sesión.", Toast.LENGTH_LONG).show();
                        } else if (e instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(RegistroActivity.this, "La contraseña es muy débil. Usa al menos 8 caracteres.", Toast.LENGTH_LONG).show();
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(RegistroActivity.this, "El formato del correo no es válido.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegistroActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void guardarUsuario() {
        progressDialog.setMessage("Guardando usuario...");
        progressDialog.show();

        String uid = mAuth.getUid();

        // Usamos HashMap para estructurar el documento
        HashMap<String, Object> datousuario = new HashMap<>();

        datousuario.put("uid", uid);
        datousuario.put("nombre", nombre);
        datousuario.put("apellido", apellido);
        datousuario.put("correo", correo);
        datousuario.put("rol", "operador"); // <-- Aquí inyectamos el rol automáticamente
        // ELIMINADO: datousuario.put("password", password); por seguridad

        // Instanciamos FIRESTORE en lugar de Realtime Database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Guardamos en la colección "usuarios" usando el UID como ID del documento
        db.collection("usuarios").document(uid).set(datousuario)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(RegistroActivity.this, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegistroActivity.this, DahsboardActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegistroActivity.this, "Ocurrió un problema: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}