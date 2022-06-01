package com.example.olxclone.model;

import com.example.olxclone.helper.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;
import java.util.List;

public class Favorito {

    private List<String> favoritos;

    public void salvar(){
        DatabaseReference favoritosRef = FirebaseHelper.getDatabaseReference()
                .child("favoritos")
                .child(FirebaseHelper.getIdFirebase());
        favoritosRef.setValue(this.getFavoritos());
    }

    public List<String> getFavoritos() {
        return favoritos;
    }

    public void setFavoritos(List<String> favoritos) {
        this.favoritos = favoritos;
    }

}
