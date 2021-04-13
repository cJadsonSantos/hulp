package js.dev.com.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import js.dev.com.R;
import js.dev.com.config.ConfiguracaoFirebase;
import js.dev.com.helper.UsuarioFirebase;
import js.dev.com.model.Destino;
import js.dev.com.model.Requisicao;
import js.dev.com.model.Usuario;

public class ClienteActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    //Componentes
    private EditText editDestino;
    private LinearLayout linearLayoutDestino;
    private Button buttonChamarHulp;

    private GoogleMap mMap;
    private FirebaseAuth autenticacao;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localCliente;
    private boolean hulpChamado = false;
    private DatabaseReference firebaseRef;
    private Requisicao requisicao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);



        inicializarComponentes();

        //Adicionar listener para status da requisição
        verificaStatusRequisicao();
    }

    private void verificaStatusRequisicao(){
        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("passageiro/id")
                .equalTo(usuarioLogado.getId());

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Requisicao> lista = new ArrayList<>();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    lista.add(ds.getValue(Requisicao.class));
                }

                Collections.reverse(lista);
                //Recupera a requisição mais recente
                if (lista != null && lista.size()>0){
                    requisicao = lista.get(0);

                    switch (requisicao.getStatus()){
                        case Requisicao.STATUS_AGUARDANDO:
                            linearLayoutDestino.setVisibility(View.GONE);
                            buttonChamarHulp.setText("Cancelar hulp");
                            hulpChamado = true;
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        //Recuperar localizacao do usuário
        recuperarLocalizacaoUsuario();

    }

    public void chamarHulp(View view){


        if(!hulpChamado){
            //Hulp não foi chamado

            String enderecoDestino = editDestino.getText().toString();
            if (!enderecoDestino.equals("")|| enderecoDestino != null){
                Address addressDestino = recuperarEndereco(enderecoDestino);
                if (addressDestino != null){
                    Destino destino = new Destino();
                    destino.setCidade(addressDestino.getAdminArea());
                    destino.setCep(addressDestino.getPostalCode());
                    destino.setBairro(addressDestino.getSubLocality());
                    destino.setRua(addressDestino.getThoroughfare());
                    destino.setNumero(addressDestino.getFeatureName());
                    destino.setLatitude(String.valueOf(addressDestino.getLatitude()));
                    destino.setLongitude(String.valueOf(addressDestino.getLongitude()));

                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append("Cidade: "+destino.getCidade());
                    mensagem.append("\nRua: "+destino.getRua());
                    mensagem.append("\nBairro: "+destino.getBairro());
                    mensagem.append("\nNúmero: "+destino.getNumero());
                    mensagem.append("\nCep: "+destino.getCep());

                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Confirme seu endereço")
                            .setMessage(mensagem)
                            .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Salvar Requisição
                                    salvarRequisicao(destino);
                                    hulpChamado = true;
                                }
                            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }else{
                Toast.makeText(this,
                        "Informe o endereço de destino!",
                        Toast.LENGTH_SHORT).show();
            }
            //Fim
        }else{
            //Cancelar a requisição
            hulpChamado = false;
        }

    }

    private void salvarRequisicao(Destino destino){
        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);

        Usuario usuarioCliente = UsuarioFirebase.getDadosUsuarioLogado();
        usuarioCliente.setLatitude(String.valueOf(localCliente.latitude));
        usuarioCliente.setLongitude(String.valueOf(localCliente.longitude));

        requisicao.setCliente(usuarioCliente);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

        linearLayoutDestino.setVisibility(View.GONE);
        buttonChamarHulp.setText("Cancelar Hulp");
    }

    private Address recuperarEndereco(String endereco){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco,1);
            if (listaEnderecos != null && listaEnderecos.size() >0){
                Address address = listaEnderecos.get(0);

                return  address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  null;
    }

    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                //Recuperar latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localCliente = new LatLng(latitude, longitude);

                mMap.clear();
                mMap.addMarker(
                        new MarkerOptions()
                                .position(localCliente)
                                .title("Meu Local")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.casa))

                );
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localCliente, 17)
                );
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }
        };

        //Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair:
                autenticacao.signOut();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void inicializarComponentes(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Buscar profissional");
        setSupportActionBar(toolbar);

        //Inicializar os componentes
        editDestino = findViewById(R.id.editDestino);
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino);
        buttonChamarHulp = findViewById(R.id.buttonChamarHulp);

        //Configurações iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

}