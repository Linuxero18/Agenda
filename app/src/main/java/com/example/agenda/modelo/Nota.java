package com.example.agenda.modelo;

public class Nota {
    private String id;
    private String titulo;
    private String contenido;
    private String tiempo;
    private String uid;

    public Nota() {

    }

    public Nota(String id, String titulo, String contenido, String tiempo, String uid) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.tiempo = tiempo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public String getUid() {
        return uid;
    }

    public void getUid(String uid) {
        this.uid = uid;
    }

}

