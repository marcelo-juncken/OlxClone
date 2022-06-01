package com.example.olxclone.model;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.example.olxclone.activities.FormAnuncioActivity;
import com.example.olxclone.activities.MainActivity;
import com.example.olxclone.helper.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Anuncio implements Serializable {

    private String id;
    private String idUsuario;
    private String titulo;
    private double valor;
    private String categoria;
    private String descricao;
    private Local local;
    private long dataPublicacao;
    private List<String> urlImagens = new ArrayList<>();

    public Anuncio() {
        DatabaseReference anuncioRef = FirebaseHelper.getDatabaseReference();
        this.setId(anuncioRef.push().getKey());
    }

    public void salvar(Activity activity, boolean novoAnuncio) {
        DatabaseReference anuncioPublicoRef = FirebaseHelper.getDatabaseReference()
                .child("anuncios_publicos")
                .child(this.getId());
        anuncioPublicoRef.setValue(this);

        DatabaseReference meusAnunciosRef = FirebaseHelper.getDatabaseReference()
                .child("meus_anuncios")
                .child(FirebaseHelper.getIdFirebase())
                .child(this.getId());
        meusAnunciosRef.setValue(this);

        if (novoAnuncio) {
            DatabaseReference dataAnuncioPublico = anuncioPublicoRef
                    .child("dataPublicacao");
            dataAnuncioPublico.setValue(ServerValue.TIMESTAMP);

            DatabaseReference dataMeusAnuncios = meusAnunciosRef
                    .child("dataPublicacao");
            dataMeusAnuncios.setValue(ServerValue.TIMESTAMP).addOnCompleteListener(task -> {
                activity.finish();
                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("id", 2);
                activity.startActivity(intent);
            });
        } else {
            activity.finish();
        }
    }

    public void deletar() {

        for (int i = 0; i < getUrlImagens().size(); i++) {
            StorageReference storageReference = FirebaseHelper.getStorageReference()
                    .child("imagens")
                    .child("anuncios")
                    .child(this.getId())
                    .child("imagem" + i + ".jpeg");
            storageReference.delete();
        }

        DatabaseReference anuncioPublicoRef = FirebaseHelper.getDatabaseReference()
                .child("anuncios_publicos")
                .child(this.getId());
        anuncioPublicoRef.removeValue();

        DatabaseReference meusAnunciosRef = FirebaseHelper.getDatabaseReference()
                .child("meus_anuncios")
                .child(FirebaseHelper.getIdFirebase())
                .child(this.getId());
        meusAnunciosRef.removeValue();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public long getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(long dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public List<String> getUrlImagens() {
        return urlImagens;
    }

    public void setUrlImagens(List<String> urlImagens) {
        this.urlImagens = urlImagens;
    }


}
