package com.ecollimsac.ecolim;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private TextInputEditText etUsuario, etPassword;
    private Button btnIngresar;
    private TextView tvCrearCuenta;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    String usuario="", password="";

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if (usuarioActual != null) {
            // Si hay sesion persistida, saltamos el login.
            startActivity(new Intent(MainActivity.this, DahsboardActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        etUsuario     = findViewById(R.id.etUsuario);
        etPassword    = findViewById(R.id.etPassword);
        btnIngresar   = findViewById(R.id.Lbutton);
        tvCrearCuenta = findViewById(R.id.textView6);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Iniciando sesión");
        progressDialog.setCanceledOnTouchOutside(false);


        btnIngresar.setOnClickListener(v -> validarDatos());

        tvCrearCuenta.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegistroActivity.class));
        });
    }

    private void validarDatos() {
        usuario = etUsuario.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(usuario).matches()){
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Debe ingresar una contraseña", Toast.LENGTH_SHORT).show();
        } else {
            loginUsuario();
        }
    }

    private void loginUsuario() {
        progressDialog.setMessage("Iniciando Sesión ...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(usuario, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss(); // Lo cerramos en ambos casos (éxito o fallo)

                        if (task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Bienvenido " + user.getEmail(), Toast.LENGTH_SHORT).show();

                            // Pasamos al Dashboard
                            startActivity(new Intent(MainActivity.this, DahsboardActivity.class));
                            finish(); // Evita regresar al Login con el botón "Atrás"

                        } else {
                            Toast.makeText(MainActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}