package com.example.olxclone.model;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.olxclone.helper.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Endereco {
    private String cep;
    private String uf;
    private String cidade;
    private String bairro;

    public Endereco() {
    }

    public void salvar(String idUser, Context context, ProgressBar progressBar) {
        DatabaseReference reference = FirebaseHelper.getDatabaseReference()
                .child("enderecos")
                .child(idUser);
        reference.setValue(this).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(context, "Endereço salvo com sucesso", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
        });
    }


    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }
}
