package br.gov.pb.tce.despesalegalbeta;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    //cria variaveis
    private FirebaseAuth firebaseAuth;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView botaoSair, photo;
    private Button botaoCamera, btPhoto;

    //variaveis de localização
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private static String nomeArquivo;
    private static String dataFormatada;

    private TextView latitudeT;
    private TextView longitudeT;
    private TextView altitudeT;
    private TextView dataT;

    //teste banco
    BancoDadosController bancoDadosController;
    static Foto foto = new Foto();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //cria a api do google
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //instancia autenticador
        firebaseAuth = FirebaseAuth.getInstance();


        //instancia demais objetos
        photo = (ImageView) findViewById(R.id.photo);
        botaoSair = (ImageView) findViewById(R.id.btSairID);
        botaoCamera = (Button) findViewById(R.id.btCameraID);
        latitudeT = (TextView) findViewById(R.id.latitudeID);
        longitudeT = (TextView) findViewById(R.id.longitudeID);
        altitudeT = (TextView) findViewById(R.id.altitudeID);
        dataT = (TextView) findViewById(R.id.dataID);

        bancoDadosController = new BancoDadosController(this);


        botaoSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));       //volta pra tela de login
                firebaseAuth.signOut();                                                             //desvalida a sessao
            }
        });

        botaoCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            SaveImage(imageBitmap);
            bancoDadosController.salvar(foto);

        }
    }

    private static String SaveImage(Bitmap imageBitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/Despesa Legal");
        if (!myDir.exists())
            myDir.mkdirs();
        nomeArquivo =  "IMG_" + dataFormatada + ".jpg";
        //foto.nomeFoto = nomeArquivo;
        File file = new File(myDir, nomeArquivo);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    @Override
    public void onLocationChanged(Location location) {

       /*
        //teste banco - gps att
        String latitudeFoto = String.valueOf(location.getLatitude());
        String longitudeFoto = String.valueOf(location.getLongitude());
        String altitudeFoto = String.valueOf(location.getAltitude());

        //Formata data e hora
        DateFormat formatacaoData = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date(location.getTime());
        dataFormatada = formatacaoData.format(date);

        foto.latitudeFoto = latitudeFoto;
        foto.longitudeFoto = longitudeFoto;
        foto.altitudeFoto = altitudeFoto;
        foto.dataFoto = dataFormatada;*/


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10*1000);

        //bloco recomendado pelo android studio
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
       /*
       try{
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest, (com.google.android.gms.location.LocationListener) this);
        }catch (Exception e){
            e.printStackTrace();
        }*/


        //captura ultima localização conhecida

        Location ultimaLocalizacao = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        //captura dados de localização
        String latitude = String.valueOf(ultimaLocalizacao.getLatitude());
        String longitude = String.valueOf(ultimaLocalizacao.getLongitude());
        String altitude = String.valueOf(ultimaLocalizacao.getAltitude());

        //Formata data e hora
        DateFormat formatacaoData = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        Date date = new Date(ultimaLocalizacao.getTime());
        dataFormatada = formatacaoData.format(date);

        foto.latitudeFoto = latitude;
        foto.longitudeFoto = longitude;
        foto.altitudeFoto = altitude;
        foto.dataFoto = dataFormatada;

        //testes em uma textview
        latitudeT.setText(latitude);
        longitudeT.setText(longitude);
        altitudeT.setText(altitude);
        dataT.setText(dataFormatada);

    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
