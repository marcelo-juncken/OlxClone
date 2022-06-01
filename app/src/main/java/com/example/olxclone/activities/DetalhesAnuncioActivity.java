package com.example.olxclone.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olxclone.R;
import com.example.olxclone.adapter.SliderAdapter;
import com.example.olxclone.autenticacao.LoginActivity;
import com.example.olxclone.helper.FirebaseHelper;
import com.example.olxclone.helper.GetMask;
import com.example.olxclone.model.Anuncio;
import com.example.olxclone.model.Favorito;
import com.example.olxclone.model.Usuario;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;


public class DetalhesAnuncioActivity extends AppCompatActivity {

    private SliderView sliderView;
    private TextView text_titulo;
    private TextView text_valor;
    private TextView text_data;
    private TextView text_descricao;
    private TextView text_categoria;
    private TextView text_cep;
    private TextView text_municipio;
    private TextView text_bairro;

    private ImageButton ib_ligar;

    private Anuncio anuncio;
    private Usuario usuario;

    private LikeButton ib_likebutton;

    private List<String> favoritosList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_anuncio);

        iniciaComponentes();


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            anuncio = (Anuncio) bundle.getSerializable("anuncioSelecionado");
            configDados();
        }
        recuperaUsuario();
        configCliques();

        configLikeButton();


    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperaFavorito();
    }

    private void configLikeButton() {
        ib_likebutton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                if (FirebaseHelper.getAutenticado()) {
                    configSnackBar("", "Anúncio salvo", R.drawable.ic_detalhes_favorito_on_red, true);
                } else {
                    likeButton.setLiked(false);
                    alertAutenticacao("Para favoritar um anúncio você precisa estar logado. Deseja fazer o login?");
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                configSnackBar("DESFAZER", "Anúncio removido", R.drawable.ic_detalhes_favorito_off, false);
            }
        });
    }

    private void configSnackBar(String actionMsg, String msg, int icon, Boolean like) {

        configFavoritos(like);

        Snackbar snackbar = Snackbar.make(ib_likebutton, msg, Snackbar.LENGTH_SHORT);
        snackbar.setAction(actionMsg, v -> {
            if (!like) {
                configFavoritos(true);
            }
        });

        TextView text_snack_bar = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        text_snack_bar.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        text_snack_bar.setCompoundDrawablePadding(24);
        snackbar.setActionTextColor(Color.parseColor("#F78323"))
                .setTextColor(Color.parseColor("#FFFFFF"))
                .show();
    }

    private void alertAutenticacao(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Você não está autenticado.");
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setNegativeButton("Não", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setPositiveButton("Sim", (dialog, which) -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void configFavoritos(Boolean like) {
        if (like) {
            ib_likebutton.setLiked(true);
            favoritosList.add(anuncio.getId());
        } else {
            ib_likebutton.setLiked(false);
            favoritosList.remove(anuncio.getId());
        }

        Favorito favorito = new Favorito();
        favorito.setFavoritos(favoritosList);
        favorito.salvar();

    }

    private void recuperaFavorito() {
        if (FirebaseHelper.getAutenticado()) {
            DatabaseReference favoritosRef = FirebaseHelper.getDatabaseReference()
                    .child("favoritos")
                    .child(FirebaseHelper.getIdFirebase());
            favoritosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        favoritosList.add(ds.getValue(String.class));
                    }

                    if (favoritosList.contains(anuncio.getId())) {
                        ib_likebutton.setLiked(true);
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void configCliques() {
        findViewById(R.id.ib_voltar).setOnClickListener(v -> finish());


        if (FirebaseHelper.getAutenticado()) {
            if (!anuncio.getIdUsuario().contentEquals(FirebaseHelper.getIdFirebase())) {
                ib_ligar.setOnClickListener(v -> ligarAnunciante());
            } else {
                ib_ligar.setVisibility(View.GONE);
            }
        }
    }

    private void ligarAnunciante() {
        if (FirebaseHelper.getAutenticado()) {
            if (!anuncio.getIdUsuario().contentEquals(FirebaseHelper.getIdFirebase())) {
                Intent intent = new Intent(Intent.ACTION_DIAL,
                        Uri.fromParts("tel", usuario.getTelefone(), null));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Esse anúncio é seu.", Toast.LENGTH_SHORT).show();
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void recuperaUsuario() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(anuncio.getIdUsuario());
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuario = snapshot.getValue(Usuario.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configDados() {

        sliderView.setSliderAdapter(new SliderAdapter(anuncio.getUrlImagens()));
        sliderView.startAutoCycle();
        sliderView.setScrollTimeInSec(4);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);

        text_titulo.setText(anuncio.getTitulo());
        text_valor.setText(getString(R.string.valor_anuncio, GetMask.getValor(anuncio.getValor())));
        text_data.setText(getString(R.string.data_publicacao, GetMask.getDate(anuncio.getDataPublicacao(), 3)));
        text_descricao.setText(anuncio.getDescricao());
        text_categoria.setText(anuncio.getCategoria());
        text_cep.setText(anuncio.getLocal().getCep());
        text_municipio.setText(anuncio.getLocal().getLocalidade());
        text_bairro.setText(anuncio.getLocal().getBairro());
    }

    private void iniciaComponentes() {
        ib_likebutton = findViewById(R.id.ib_likebutton);
        sliderView = findViewById(R.id.sliderView);
        text_titulo = findViewById(R.id.text_titulo);
        text_valor = findViewById(R.id.text_valor);
        text_data = findViewById(R.id.text_data);
        text_descricao = findViewById(R.id.text_descricao);
        text_categoria = findViewById(R.id.text_categoria);
        text_cep = findViewById(R.id.text_cep);
        text_municipio = findViewById(R.id.text_municipio);
        text_bairro = findViewById(R.id.text_bairro);
        ib_ligar = findViewById(R.id.ib_ligar);
    }
}