package com.example.olxclone.activities;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olxclone.R;
import com.example.olxclone.helper.FirebaseHelper;
import com.example.olxclone.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.santalu.maskara.widget.MaskEditText;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class PerfilActivity extends AppCompatActivity {

    private final int SELECAO_GALERIA = 100;
    private EditText edit_nome;
    private MaskEditText edit_telefone;
    private EditText edit_email;
    private ImageView imagem_perfil;
    private ProgressBar progressBar;
    private Usuario usuario;

    private String caminhoImagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        iniciaComponentes();
        configCliques();
        carregaPerfil();
    }

    public void validaDados(View view) {
        String nome = edit_nome.getText().toString();
        String telefone = edit_telefone.getMasked();

        if (!nome.isEmpty()) {
            if (!telefone.isEmpty()) {
                if (telefone.length() == 15) {
                    usuario.setNome(nome);
                    usuario.setTelefone(telefone);
                    if (caminhoImagem != null) {
                        salvarImagemPerfil();
                    } else {
                        usuario.salvar(progressBar, getBaseContext());
                    }
                } else {
                    edit_telefone.requestFocus();
                    edit_telefone.setError("Telefone inválido");
                }
            } else {
                edit_telefone.requestFocus();
                edit_telefone.setError("Coloque o seu telefone");
            }
        } else {
            edit_nome.requestFocus();
            edit_nome.setError("Preencha o seu nome");
        }
    }

    private void carregaPerfil() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(FirebaseHelper.getIdFirebase());
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuario = snapshot.getValue(Usuario.class);
                configDados();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void salvarImagemPerfil() {
        StorageReference storageReference = FirebaseHelper.getStorageReference()
                .child("imagens")
                .child("perfil")
                .child(FirebaseHelper.getIdFirebase() + ".jpeg");

        UploadTask uploadTask = storageReference.putFile(Uri.parse(caminhoImagem));
        uploadTask.addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnCompleteListener(task -> {
            String urlImagem = task.getResult().toString();
            usuario.setImagemPerfil(urlImagem);
            usuario.salvar(progressBar, getBaseContext());
        })).addOnFailureListener(e -> Toast.makeText(this, "Erro no upload, tente novamente mais tarde.", Toast.LENGTH_SHORT).show());
    }

    private void configDados() {
        edit_nome.setText(usuario.getNome());
        edit_telefone.setText(usuario.getTelefone());
        edit_email.setText(usuario.getEmail());

        progressBar.setVisibility(View.GONE);

        if (usuario.getImagemPerfil() != null) {
            Picasso.get().load(usuario.getImagemPerfil()).into(imagem_perfil);
        }
    }

    private void iniciaComponentes() {
        TextView text_titulo = findViewById(R.id.text_toolbar);
        text_titulo.setText("Perfil");

        edit_nome = findViewById(R.id.edt_nome);
        edit_telefone = findViewById(R.id.edt_telefone);
        edit_email = findViewById(R.id.edt_email);
        progressBar = findViewById(R.id.progressBar);
        imagem_perfil = findViewById(R.id.imagem_perfil);
    }

    private void configCliques() {
        findViewById(R.id.ib_toolbar_voltar).setOnClickListener(v -> finish());

        imagem_perfil.setOnClickListener(v -> {
            verificaPermissaoGaleria();
        });
    }

    private void verificaPermissaoGaleria() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                abrirGaleria();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(PerfilActivity.this, "Permissão negada.", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedTitle("Permissões negadas")
                .setDeniedMessage("Se você não aceitar a permissão, não poderá acessar a galeria do dispositivo. Deseja permitir?")
                .setDeniedCloseButtonText("Não")
                .setGotoSettingButtonText("Sim")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECAO_GALERIA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECAO_GALERIA) {
                Uri imagemSelecionada = data.getData();
                Bitmap imagemRecuperada;

                try {
                    imagemRecuperada = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemSelecionada);
                    imagem_perfil.setImageBitmap(imagemRecuperada);
                    caminhoImagem = imagemSelecionada.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}