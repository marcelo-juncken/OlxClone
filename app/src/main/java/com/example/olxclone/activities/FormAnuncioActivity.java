package com.example.olxclone.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.olxclone.R;
import com.example.olxclone.api.CEPService;
import com.example.olxclone.helper.FirebaseHelper;
import com.example.olxclone.helper.GetMask;
import com.example.olxclone.model.Anuncio;
import com.example.olxclone.model.Categoria;
import com.example.olxclone.model.Endereco;
import com.example.olxclone.model.Imagem;
import com.example.olxclone.model.Local;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.santalu.maskara.widget.MaskEditText;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FormAnuncioActivity extends AppCompatActivity {

    private EditText edt_titulo;
    private EditText edt_descricao;
    private CurrencyEditText edt_valor;
    private MaskEditText edit_cep;
    private Button btn_categoria;
    private static final int REQUEST_CATEGORIA = 100;
    private String categoriaSelecionada = "";

    private ProgressBar progressBar;

    private Endereco enderecoUsuario;

    private Retrofit retrofit;

    private Local local;

    private TextView txt_endereco;
    private TextView text_toolbar;

    private ImageView img_1;
    private ImageView img_2;
    private ImageView img_3;
    private Bitmap imagem;
    private String caminhoImagem;
    private String currentPhotoPath;

    private List<Imagem> imagemList = new ArrayList<>();
    private Anuncio anuncio;
    private boolean novoAnuncio = true;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_anuncio);

        iniciaComponentes();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            anuncio = (Anuncio) bundle.getSerializable("anuncioSelecionado");

            configDados();
        }
        configCliques();
        carregaEndereco();
        iniciaRetrofit();
        recuperaEndereco();

    }

    private void configDados() {
        text_toolbar.setText("Editando anúncio");

        edt_titulo.setText(anuncio.getTitulo());
        edt_valor.setText(GetMask.getValor(anuncio.getValor()));
        edt_descricao.setText(anuncio.getDescricao());

        categoriaSelecionada = anuncio.getCategoria();
        btn_categoria.setText(categoriaSelecionada);
        edit_cep.setText(anuncio.getLocal().getCep());

        Picasso.get().load(anuncio.getUrlImagens().get(0)).into(img_1);
        Picasso.get().load(anuncio.getUrlImagens().get(1)).into(img_2);
        Picasso.get().load(anuncio.getUrlImagens().get(2)).into(img_3);

        novoAnuncio = false;


    }

    public void selecionarCategoria(View view) {
        Intent intent = new Intent(this, CategoriasActivity.class);
        startActivityForResult(intent, REQUEST_CATEGORIA);

    }


    private void recuperaEndereco() {
        DatabaseReference enderecoRef = FirebaseHelper.getDatabaseReference()
                .child("enderecos")
                .child(FirebaseHelper.getIdFirebase());
        enderecoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                enderecoUsuario = snapshot.getValue(Endereco.class);
                if (enderecoUsuario!=null) {
                    edit_cep.setText(enderecoUsuario.getCep());
                }
                else{
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void carregaEndereco() {
        edit_cep.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String cep = s.toString().replaceAll("_", "").replace("-", "");
                if (cep.length() == 8) {
                    buscarEndereco(cep);
                } else {
                    local = null;
                    configEndereco();
                    progressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void buscarEndereco(String cep) {

        progressBar.setVisibility(View.VISIBLE);

        CEPService cepService = retrofit.create(CEPService.class);
        Call<Local> call = cepService.recuperaCEP(cep);

        call.enqueue(new Callback<Local>() {
            @Override
            public void onResponse(Call<Local> call, Response<Local> response) {
                if (response.isSuccessful()) {
                    local = response.body();

                    if (local.getLocalidade() == null) {
                        Toast.makeText(FormAnuncioActivity.this, "CEP inválido", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FormAnuncioActivity.this, "Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                configEndereco();
            }

            @Override
            public void onFailure(Call<Local> call, Throwable t) {
                Toast.makeText(FormAnuncioActivity.this, "Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void configEndereco() {
        if (local != null) {
            if (local.getLocalidade() != null) {
                String endereco = local.getLocalidade() + ", " + local.getBairro() + " - DDD" + local.getDdd();
                txt_endereco.setText(endereco);
            } else {
                txt_endereco.setText("");
            }
        } else {
            txt_endereco.setText("");
        }
        progressBar.setVisibility(View.GONE);
    }

    private void iniciaRetrofit() {
        retrofit = new Retrofit
                .Builder()
                .baseUrl("https://viacep.com.br/ws/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void showBottomDialog(int requestCode) {
        View modalbottomsheet = getLayoutInflater().inflate(R.layout.layout_bottom_sheet, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialog);
        bottomSheetDialog.setContentView(modalbottomsheet);
        bottomSheetDialog.show();

        modalbottomsheet.findViewById(R.id.btn_camera).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            verificaPermissaoCamera(requestCode);
        });
        modalbottomsheet.findViewById(R.id.btn_galeria).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            verificaPermissaoGaleria(requestCode);
        });
        modalbottomsheet.findViewById(R.id.btn_close).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });
    }

    private void verificaPermissaoCamera(int requestCode) {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                dispatchTakePictureIntent(requestCode);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(FormAnuncioActivity.this, "Permissão negada.", Toast.LENGTH_SHORT).show();
            }
        };
        showDialogPermissaoGaleria(permissionListener,
                new String[]{Manifest.permission.CAMERA},
                "Você negou as permissões para acessar a câmera do dispositivo, deseja permitir?");
    }

    private void verificaPermissaoGaleria(int requestCode) {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                abrirGaleria(requestCode);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(FormAnuncioActivity.this, "Permissão negada.", Toast.LENGTH_SHORT).show();
            }
        };
        showDialogPermissaoGaleria(permissionListener,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                "Você negou as permissões para acessar a galeria do dispositivo, deseja permitir?");
    }

    private void configUpload(int requestCode, String caminhoImagem) {
        int request = 0;
        switch (requestCode) {
            case 0:
            case 3:
                request = 0;
                break;
            case 1:
            case 4:
                request = 1;
                break;
            case 2:
            case 5:
                request = 2;
                break;
        }

        Imagem imagem = new Imagem(caminhoImagem, request);
        if (imagemList.size() > 0) {
            boolean encontrou = false;
            for (int i = 0; i < imagemList.size(); i++) {
                if (imagemList.get(i).getIndex() == request) {
                    encontrou = true;
                }
            }

            if (encontrou) {
                imagemList.set(request, imagem);
            } else {
                imagemList.add(imagem);
            }
        } else {
            imagemList.add(imagem);
        }

    }

    private void abrirGaleria(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent(int requestCode) {

        int request = 0;
        switch (requestCode) {
            case 0:
                request = 3;
                break;
            case 1:
                request = 4;
                break;
            case 2:
                request = 5;
                break;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File

        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.olxclone.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, request);
        }
    }

    private void showDialogPermissaoGaleria(PermissionListener listener, String[] permissoes, String msg) {
        TedPermission.create()
                .setPermissionListener(listener)
                .setDeniedTitle("Permissões negadas.")
                .setDeniedMessage(msg)
                .setDeniedCloseButtonText("Não")
                .setGotoSettingButtonText("Sim")
                .setPermissions(permissoes)
                .check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CATEGORIA) {
                Categoria categoria = (Categoria) data.getSerializableExtra("categoriaSelecionada");
                categoriaSelecionada = categoria.getNome();
                btn_categoria.setText(categoriaSelecionada);
            } else if (requestCode <= 2) {
                Uri localImagemSelecionada = data.getData();
                caminhoImagem = localImagemSelecionada.toString();
                try {
                    if (Build.VERSION.SDK_INT < 31) {
                        imagem = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), localImagemSelecionada);
                    } else {
                        ImageDecoder.Source source = ImageDecoder.createSource(getBaseContext().getContentResolver(), localImagemSelecionada);
                        imagem = ImageDecoder.decodeBitmap(source);
                    }
                    switch (requestCode) {
                        case 0:
                            img_1.setImageBitmap(imagem);
                            break;
                        case 1:
                            img_2.setImageBitmap(imagem);
                            break;
                        case 2:
                            img_3.setImageBitmap(imagem);
                            break;
                    }
                    configUpload(requestCode, caminhoImagem);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                File file = new File(currentPhotoPath);
                caminhoImagem = String.valueOf(file.toURI());
                switch (requestCode) {
                    case 3:
                        img_1.setImageURI(Uri.fromFile(file));
                        break;
                    case 4:
                        img_2.setImageURI(Uri.fromFile(file));
                        break;
                    case 5:
                        img_3.setImageURI(Uri.fromFile(file));
                        break;
                }
                configUpload(requestCode, caminhoImagem);
            }
        }
    }

    public void validaDados() {
        String titulo = edt_titulo.getText().toString().trim();
        double preco = (double) edt_valor.getRawValue() / 100;
        String descricao = edt_descricao.getText().toString().trim();

        if (!titulo.isEmpty()) {
            if (preco > 0) {
                if (!categoriaSelecionada.isEmpty()) {
                    if (!descricao.isEmpty()) {
                        if (local != null) {
                            if (local.getLocalidade() != null) {
                                if (anuncio == null) anuncio = new Anuncio();
                                anuncio.setIdUsuario(FirebaseHelper.getIdFirebase());
                                anuncio.setTitulo(titulo);
                                anuncio.setValor(preco);
                                anuncio.setCategoria(categoriaSelecionada);
                                anuncio.setDescricao(descricao);
                                anuncio.setLocal(local);

                                if (novoAnuncio) {
                                    if (imagemList.size() == 3) {
                                        for (int i = 0; i < imagemList.size(); i++) {
                                            salvarImagemFireBase(imagemList.get(i), i);
                                        }
                                    } else {
                                        Toast.makeText(this, "Selecione 3 imagens para o anúncio", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if(imagemList.size() > 0){
                                        for (int i = 0; i < imagemList.size(); i++) {
                                            salvarImagemFireBase(imagemList.get(i), i);
                                        }
                                    }else{
                                        button.setText("Salvando anúncio...");
                                        anuncio.salvar(this, false);
                                    }
                                }
                            } else {
                                edit_cep.requestFocus();
                                edit_cep.setError("Informe um CEP válido");
                            }
                        } else {
                            edit_cep.requestFocus();
                            edit_cep.setError("Informe um CEP válido");
                        }
                    } else {
                        edt_descricao.requestFocus();
                        edt_descricao.setError("Informe uma descrição");
                    }
                } else {
                    Toast.makeText(this, "Informe uma categoria;", Toast.LENGTH_SHORT).show();
                }
            } else {
                edt_valor.requestFocus();
                edt_valor.setError("Informe um valor diferente de 0");
            }
        } else {
            edt_titulo.requestFocus();
            edt_titulo.setError("Informe o título");
        }
    }

    private void salvarImagemFireBase(Imagem imagem, int index) {
        button.setText("Salvando anúncio...");
        StorageReference storageReference = FirebaseHelper.getStorageReference()
                .child("imagens")
                .child("anuncios")
                .child(anuncio.getId())
                .child("imagem" + index + ".jpeg");

        UploadTask uploadTask = storageReference.putFile(Uri.parse(imagem.getCaminhoImagem()));
        uploadTask.addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnCompleteListener(task -> {
            if (novoAnuncio) {
                anuncio.getUrlImagens().add(index, task.getResult().toString());
            } else {
                anuncio.getUrlImagens().set(imagem.getIndex(), task.getResult().toString());
            }
            if (imagemList.size() == index + 1) {
                anuncio.salvar(this, novoAnuncio);
            }
        })).addOnFailureListener(e -> Toast.makeText(this, "Erro no upload, tente novamente mais tarde.", Toast.LENGTH_SHORT).show());

    }

    private void iniciaComponentes() {
        text_toolbar = findViewById(R.id.text_toolbar);
        text_toolbar.setText("Novo anúncio");

        edt_titulo = findViewById(R.id.edt_titulo);
        edt_descricao = findViewById(R.id.edt_descricao);
        edt_valor = findViewById(R.id.edt_valor);
        edt_valor.setLocale(new Locale("PT", "br"));
        btn_categoria = findViewById(R.id.btn_categoria);
        edit_cep = findViewById(R.id.edt_cep);
        progressBar = findViewById(R.id.progressBar);
        txt_endereco = findViewById(R.id.txt_endereco);

        img_1 = findViewById(R.id.img_1);
        img_2 = findViewById(R.id.img_2);
        img_3 = findViewById(R.id.img_3);
        button = findViewById(R.id.button);
    }

    private void configCliques() {
        findViewById(R.id.ib_toolbar_voltar).setOnClickListener(v -> finish());

        img_1.setOnClickListener(v -> showBottomDialog(0));
        img_2.setOnClickListener(v -> showBottomDialog(1));
        img_3.setOnClickListener(v -> showBottomDialog(2));

        button.setOnClickListener(v -> validaDados());
    }
}