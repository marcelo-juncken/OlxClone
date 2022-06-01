package com.example.olxclone.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.olxclone.R;
import com.example.olxclone.activities.EnderecoActivity;
import com.example.olxclone.activities.MainActivity;
import com.example.olxclone.activities.PerfilActivity;
import com.example.olxclone.autenticacao.LoginActivity;
import com.example.olxclone.helper.FirebaseHelper;
import com.example.olxclone.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import okhttp3.Call;

public class MinhaContaFragment extends Fragment {

    private TextView text_conta;
    private TextView text_usuario;

    private Usuario usuario;
    private ImageView imagem_perfil;

    private ProgressBar progressBar;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_minha_conta, container, false);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        iniciaComponentes(view);

        configCliques(view);

        recuperaUsuario();
    }

    private void configCliques(View view) {
        view.findViewById(R.id.menu_perfil).setOnClickListener(v -> redirecionaUsuario(PerfilActivity.class));
        view.findViewById(R.id.menu_endereco).setOnClickListener(v -> redirecionaUsuario(EnderecoActivity.class));
        if (FirebaseHelper.getAutenticado()) {
            text_conta.setText("Sair");
            text_conta.setOnClickListener(v -> {
                FirebaseHelper.getAuth().signOut();
                Picasso.get().cancelRequest(imagem_perfil);
                onStart();
            });
        } else {
            text_conta.setText("Clique aqui");
            text_conta.setOnClickListener(v ->
                    startActivity(new Intent(getActivity(), LoginActivity.class)));
        }

    }

    private void redirecionaUsuario(Class<?> classe) {
        if (FirebaseHelper.getAutenticado()) {
            startActivity(new Intent(getActivity(), classe));
        } else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }
    }

    private void iniciaComponentes(View view) {
        text_conta = view.findViewById(R.id.text_conta);
        text_usuario = view.findViewById(R.id.text_usuario);
        imagem_perfil = view.findViewById(R.id.imagem_perfil);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        imagem_perfil.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        text_usuario.setText("Acesse sua conta agora!");
    }

    private void recuperaUsuario() {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                    .child("usuarios")
                    .child(FirebaseHelper.getIdFirebase());
            usuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    usuario = snapshot.getValue(Usuario.class);
                    configConta();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    imagem_perfil.setImageResource(R.drawable.ic_user_perfil);
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            imagem_perfil.setImageResource(R.drawable.ic_user_perfil);

        }
    }

    private void configConta() {
        text_usuario.setText(usuario.getNome());
        if (usuario.getImagemPerfil() != null) {
            imagem_perfil.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.get().load(usuario.getImagemPerfil())
                    .placeholder(R.drawable.loading)
                    .into(imagem_perfil, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            imagem_perfil.setImageResource(R.drawable.ic_user_perfil);
        }
    }
}