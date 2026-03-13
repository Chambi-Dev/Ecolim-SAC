package com.ecollimsac.ecolim.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class RecoleccionRegistro {
    private final String id;
    private final String uid;
    private final String tipoResiduo;
    private final String clasificacion;
    private final double pesoKg;
    private final String areaOrigen;
    private final String fotoUrl;
    private final Timestamp createdAt;

    public RecoleccionRegistro(String id, String uid, String tipoResiduo, String clasificacion,
                               double pesoKg, String areaOrigen, String fotoUrl, Timestamp createdAt) {
        this.id = id;
        this.uid = uid;
        this.tipoResiduo = tipoResiduo;
        this.clasificacion = clasificacion;
        this.pesoKg = pesoKg;
        this.areaOrigen = areaOrigen;
        this.fotoUrl = fotoUrl;
        this.createdAt = createdAt;
    }

    public static RecoleccionRegistro fromDocument(DocumentSnapshot document) {
        String uid = valueOrEmpty(document.getString("uid"));
        String tipo = valueOrEmpty(document.getString("tipoResiduo"));
        String clasificacion = valueOrEmpty(document.getString("clasificacion"));
        String area = valueOrEmpty(document.getString("areaOrigen"));
        String fotoUrl = valueOrEmpty(document.getString("fotoUrl"));
        Double peso = document.getDouble("pesoKg");
        Timestamp createdAt = document.getTimestamp("createdAt");
        return new RecoleccionRegistro(
                document.getId(),
                uid,
                tipo,
                clasificacion,
                peso == null ? 0.0 : peso,
                area,
                fotoUrl,
                createdAt
        );
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getTipoResiduo() {
        return tipoResiduo;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public String getAreaOrigen() {
        return areaOrigen;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}

