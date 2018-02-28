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
    
    private TextView latitudeT;
    private TextView longitudeT;
    private TextView altitudeT;
    private TextView dataT;

    //variaveis de banco
    private SQLiteDatabase bancoDados;

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

        //cria banco
        bancoDados = openOrCreateDatabase("db_DespesaLegalBeta", MODE_PRIVATE, null);

        //cria tabela
        String sql = "CREATE TABLE IF NOT EXISTS tb_dadosFoto (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nomeFoto TEXT," +
                "latitudeF TEXT," +
                "longitudeF TEXT," +
                "altitudeF TEXT," +
                "dataF TEXT," +
                "numObra INTEGER," +
                "numMedicao INTEGER," +
                "anguloF INTEGER" +
                ")";
        bancoDados.execSQL(sql);

        //recuperar dados banco
       // Cursor curso = bancoDados.rawQuery("SELECT * FROM tb_dadosFoto")


        //instancia demais objetos
        photo = (ImageView) findViewById(R.id.photo);
        botaoSair = (ImageView) findViewById(R.id.btSairID);
        botaoCamera = (Button) findViewById(R.id.btCameraID);
        latitudeT = (TextView)findViewById(R.id.latitudeID);
        longitudeT = (TextView) findViewById(R.id.longitudeID);
        altitudeT = (TextView) findViewById(R.id.altitudeID);
        dataT = (TextView) findViewById(R.id.dataID);


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
            //salvarDados();
        }
    }

    private static String SaveImage(Bitmap imageBitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/Despesa Legal");
        if (!myDir.exists())
            myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt();
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
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

    private void salvarDados() {
        /*//teste
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
        Location ultimoLocal = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        String latitudef = String.valueOf(ultimoLocal.getLatitude());
        String longitudef = String.valueOf(ultimoLocal.getLongitude());
        String altitudef = String.valueOf(ultimoLocal.getAltitude());
        DateFormat formatacaoData = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date(ultimoLocal.getTime());
        String dataFormatada = formatacaoData.format(date);*/



        bancoDados.execSQL("INSERT INTO tb_dadosFoto (nomeFoto,latitudef,longitudeF,altitudeF,dataF,numObra,NumMedicao,angulo)" +
                "VALUES(teste,teste,teste,teste,teste,1,2,3)");
        //Toast.makeText(MainActivity.this,"SUCESSO",Toast.LENGTH_LONG).show();
    }




    @Override
    public void onLocationChanged(Location location) {
        //localizacao.setText("Atualização de localização " + location.toString());
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
        locationRequest.setInterval(1000);

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
        //captura ultima localização conhecida

        Location ultimaLocalizacao = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        //captura dados de localização
        String latitude = String.valueOf(ultimaLocalizacao.getLatitude());
        String longitude = String.valueOf(ultimaLocalizacao.getLongitude());
        String altitude = String.valueOf(ultimaLocalizacao.getAltitude());

        //Formata data e hora
        DateFormat formatacaoData = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date(ultimaLocalizacao.getTime());
        String dataFormatada = formatacaoData.format(date);

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
