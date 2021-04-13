package js.dev.com.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Button;

import js.dev.com.R;
import js.dev.com.config.ConfiguracaoFirebase;
import js.dev.com.model.Requisicao;
import js.dev.com.model.Usuario;

public class HelpActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private Button buttonHelp;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localTecnico;
    private LatLng localCliente;
    private Usuario tecnico;
    private Usuario cliente;
    private String idRequisicao;
    private Requisicao requisicao;
    private DatabaseReference firebaseRef;
    private Marker marcadorTecnico;
    private Marker marcadorCliente;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        inicializarComponentes();

        //Recupera dados do usuário
        if (getIntent().getExtras().containsKey("idRequisicao")
                && getIntent().getExtras().containsKey("tecnico")){
            Bundle extras = getIntent().getExtras();
            tecnico = (Usuario) extras.getSerializable("tecnico");
            idRequisicao = extras.getString("idRequisicao");
            verificaStatusRequisicao();
        }
    }

    private void verificaStatusRequisicao(){
        DatabaseReference requisicoes = firebaseRef.child("requisicoes")
                .child(idRequisicao);
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Recupera a requisição
                requisicao = dataSnapshot.getValue(Requisicao.class);
                cliente = requisicao.getCliente();
                localCliente = new LatLng(
                        Double.parseDouble(cliente.getLatitude()),
                        Double.parseDouble(cliente.getLongitude())
                );

                switch (requisicao.getStatus()){
                    case Requisicao.STATUS_AGUARDANDO:
                        requisicaoAguardando();
                        break;
                    case  Requisicao.STATUS_A_CAMINHO:
                        requisicaoACaminho();
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void requisicaoAguardando(){
        buttonHelp.setText("Prestar Serviço");
    }

    private void requisicaoACaminho(){
        buttonHelp.setText("A Caminho");

        //Exibe marcador do Técnico
        adicionaMarcadorTecnico(localTecnico,tecnico.getNome());

        //Exibe marcador do Cliente
        adicionaMarcadorCliente(localCliente, cliente.getNome());

        //Centralizar os marcadores Técnico e Cliente
        centralizarMarcadores(marcadorTecnico,marcadorCliente);
    }

    private void centralizarMarcadores(Marker marcador1, Marker marcador2){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(marcador1.getPosition());
        builder.include(marcador2.getPosition());

        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(bounds,largura, altura, espacoInterno)
        );
    }

    private void adicionaMarcadorTecnico(LatLng localizacao, String titulo){

        if (marcadorTecnico != null)
            marcadorTecnico.remove();

       marcadorTecnico = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tecnico))

        );

    }

    private void adicionaMarcadorCliente(LatLng localizacao, String titulo){

        if (marcadorCliente != null)
            marcadorCliente.remove();

        marcadorCliente = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.casa))

        );

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
    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                //Recuperar latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localTecnico = new LatLng(latitude, longitude);

                mMap.clear();
                mMap.addMarker(
                        new MarkerOptions()
                                .position(localTecnico)
                                .title("Meu Local")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.tecnico))

                );
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localTecnico, 17)
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

    public void prestarAjuda(View view){
        //Confirar Requisição
        requisicao = new Requisicao();
        requisicao.setId(idRequisicao);
        requisicao.setTecnico(tecnico);
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);

        requisicao.atualizar();


    }

    private void inicializarComponentes(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Prestar Suporte");

        buttonHelp = findViewById(R.id.buttonHelp);
        //Configurações iniciais
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

}

