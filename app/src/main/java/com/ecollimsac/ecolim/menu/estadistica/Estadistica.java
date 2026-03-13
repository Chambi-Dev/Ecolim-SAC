package com.ecollimsac.ecolim.menu.estadistica;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ecollimsac.ecolim.R;
import com.ecollimsac.ecolim.model.RecoleccionRegistro;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Estadistica extends AppCompatActivity {

    private Button btnDesde;
    private Button btnHasta;
    private Spinner spFiltroTipo;
    private TextView tvTotalKg;
    private TextView tvTotalOps;
    private TextView tvSinDatos;
    private ProgressBar progressBar;
    private PieChart pieChart;

    private final List<RecoleccionRegistro> allData = new ArrayList<>();
    private final List<RecoleccionRegistro> filteredData = new ArrayList<>();

    private final List<String> filtrosTipo = Arrays.asList(
            "Todos",
            "Aprovechable",
            "No Aprovechable",
            "Organico",
            "Peligroso"
    );

    private Date fechaDesde;
    private Date fechaHasta;

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private final SimpleDateFormat dateOnly = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estadistica);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupSpinner();
        setupChart();
        setupEvents();
        cargarRecolecciones();
    }

    private void initViews() {
        btnDesde = findViewById(R.id.btnDesde);
        btnHasta = findViewById(R.id.btnHasta);
        spFiltroTipo = findViewById(R.id.spFiltroTipo);
        tvTotalKg = findViewById(R.id.tvTotalKg);
        tvTotalOps = findViewById(R.id.tvTotalOps);
        tvSinDatos = findViewById(R.id.tvSinDatos);
        progressBar = findViewById(R.id.progressEstadistica);
        pieChart = findViewById(R.id.pieChart);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filtrosTipo
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltroTipo.setAdapter(adapter);
    }

    private void setupChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Distribucion");

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    }

    private void setupEvents() {
        btnDesde.setOnClickListener(v -> abrirDatePicker(true));
        btnHasta.setOnClickListener(v -> abrirDatePicker(false));

        spFiltroTipo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Sin accion.
            }
        });

        findViewById(R.id.btnExportar).setOnClickListener(v -> mostrarOpcionesExportacion());
    }

    private void abrirDatePicker(boolean esDesde) {
        Calendar base = Calendar.getInstance();
        Date inicial = esDesde ? fechaDesde : fechaHasta;
        if (inicial != null) {
            base.setTime(inicial);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar seleccionado = Calendar.getInstance();
                    seleccionado.set(year, month, dayOfMonth, esDesde ? 0 : 23, esDesde ? 0 : 59, esDesde ? 0 : 59);

                    if (esDesde) {
                        fechaDesde = seleccionado.getTime();
                        btnDesde.setText(getString(R.string.est_desde) + ": " + dateOnly.format(fechaDesde));
                    } else {
                        fechaHasta = seleccionado.getTime();
                        btnHasta.setText(getString(R.string.est_hasta) + ": " + dateOnly.format(fechaHasta));
                    }
                    aplicarFiltros();
                },
                base.get(Calendar.YEAR),
                base.get(Calendar.MONTH),
                base.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void cargarRecolecciones() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance()
                .collection("recolecciones")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    allData.clear();
                    queryDocumentSnapshots.getDocuments()
                            .forEach(document -> allData.add(RecoleccionRegistro.fromDocument(document)));
                    aplicarFiltros();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.msg_error, e.getMessage()), Toast.LENGTH_LONG).show();
                });
    }

    private void aplicarFiltros() {
        String tipoSeleccionado = spFiltroTipo.getSelectedItem() == null
                ? "Todos"
                : spFiltroTipo.getSelectedItem().toString();

        filteredData.clear();
        for (RecoleccionRegistro item : allData) {
            if (!"Todos".equals(tipoSeleccionado) && !tipoSeleccionado.equals(item.getTipoResiduo())) {
                continue;
            }

            Timestamp ts = item.getCreatedAt();
            if (ts == null) {
                continue;
            }
            Date fecha = ts.toDate();

            if (fechaDesde != null && fecha.before(fechaDesde)) {
                continue;
            }
            if (fechaHasta != null && fecha.after(fechaHasta)) {
                continue;
            }

            filteredData.add(item);
        }

        actualizarKpis();
        actualizarGrafico();
    }

    private void actualizarKpis() {
        double totalKg = 0.0;
        for (RecoleccionRegistro item : filteredData) {
            totalKg += item.getPesoKg();
        }

        tvTotalKg.setText(decimalFormat.format(totalKg));
        tvTotalOps.setText(String.valueOf(filteredData.size()));
        tvSinDatos.setVisibility(filteredData.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void actualizarGrafico() {
        Map<String, Float> acumulado = new LinkedHashMap<>();
        acumulado.put("Aprovechable", 0f);
        acumulado.put("No Aprovechable", 0f);
        acumulado.put("Organico", 0f);
        acumulado.put("Peligroso", 0f);

        for (RecoleccionRegistro item : filteredData) {
            float actual = acumulado.getOrDefault(item.getTipoResiduo(), 0f);
            acumulado.put(item.getTipoResiduo(), actual + (float) item.getPesoKg());
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : acumulado.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Tipos de residuo");
        dataSet.setColors(
                Color.rgb(76, 175, 80),
                Color.rgb(255, 152, 0),
                Color.rgb(33, 150, 243),
                Color.rgb(244, 67, 54)
        );
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        pieChart.setData(new PieData(dataSet));
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }

    private void mostrarOpcionesExportacion() {
        if (filteredData.isEmpty()) {
            Toast.makeText(this, R.string.est_sin_datos, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] opciones = {getString(R.string.export_pdf), getString(R.string.export_excel)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.est_exportar)
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        exportarPdf();
                    } else {
                        exportarCsv();
                    }
                })
                .show();
    }

    private void exportarCsv() {
        File file = new File(getCacheDir(), "reporte_recolecciones.csv");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            StringBuilder csv = new StringBuilder();
            csv.append("Tipo,Clasificacion,PesoKg,Area,Fecha\n");
            for (RecoleccionRegistro item : filteredData) {
                String fecha = item.getCreatedAt() == null ? "" : dateTime.format(item.getCreatedAt().toDate());
                csv.append(item.getTipoResiduo()).append(',')
                        .append(item.getClasificacion()).append(',')
                        .append(decimalFormat.format(item.getPesoKg())).append(',')
                        .append(item.getAreaOrigen()).append(',')
                        .append(fecha)
                        .append('\n');
            }
            fos.write(csv.toString().getBytes());
            compartirArchivo(file, "text/csv");
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.msg_error, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void exportarPdf() {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12f);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        int y = 40;
        page.getCanvas().drawText("Reporte de Recolecciones", 40, y, paint);
        y += 24;
        for (RecoleccionRegistro item : filteredData) {
            if (y > 800) {
                break;
            }
            String fecha = item.getCreatedAt() == null ? "" : dateTime.format(item.getCreatedAt().toDate());
            String row = item.getTipoResiduo() + " | " + item.getClasificacion() + " | "
                    + decimalFormat.format(item.getPesoKg()) + " Kg | " + item.getAreaOrigen() + " | " + fecha;
            page.getCanvas().drawText(row, 40, y, paint);
            y += 18;
        }

        document.finishPage(page);

        File file = new File(getCacheDir(), "reporte_recolecciones.pdf");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            compartirArchivo(file, "application/pdf");
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.msg_error, e.getMessage()), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }

    private void compartirArchivo(File file, String mimeType) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, getString(R.string.est_exportar)));
    }
}