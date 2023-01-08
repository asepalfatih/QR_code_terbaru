package com.example.qrcodescanner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //view object
    private Button buttonScanning;
    private TextView textViewName, textViewClass, textViewId;

    //QR code scanner
    private IntentIntegrator qrScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //view object
        buttonScanning = (Button) findViewById(R.id.Scanning);
        textViewName = (TextView) findViewById(R.id.textNama);
        textViewClass = (TextView) findViewById(R.id.textkelas);
        textViewId = (TextView) findViewById(R.id.textNIM);

        //inisialisasi scan object
        qrScan = new IntentIntegrator(this);

        //implementasi oncilck listener
        buttonScanning.setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
        }
    }
        //unutk hasil scanning
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data){
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                try {
                    //Konversi datanya ke json
                    JSONObject obj = new JSONObject(result.getContents());
                    //di set nilai datanya ke textview
                    textViewName.setText(obj.getString("nama"));
                    textViewClass.setText(obj.getString("kelas"));
                    textViewId.setText(obj.getString("nim"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
                //jika qrcode tidak ada sama sekali
                if (result.getContents() == null) {
                    Toast.makeText(this, "Hasil SCANNING tidak ada", Toast.LENGTH_LONG).show();
                }else if (Patterns.WEB_URL.matcher(result.getContents()).matches()) {
                    //jika qrcode ada/ditemukan datanya
                    Intent visitUrl = new Intent(Intent.ACTION_VIEW, Uri.parse(result.getContents()));
                    startActivity(visitUrl);

                    if (result.getContents().contains("geo:")) {
// Memisahkan latitude dan longitude dari data yang di scan
                        String[] geoLocation = result.getContents().split(":")[1].split("\\?")[0].split(",");
                        double latitude = Double.parseDouble(geoLocation[0]);
                        double longitude = Double.parseDouble(geoLocation[1]);

                        // Membuka aplikasi Google Maps dan menampilkan lokasi yang di scan
                        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }

                }else{
                    try {
                        Intent intent2 = new Intent(Intent.ACTION_CALL, Uri.parse(result.getContents()));
                        startActivity(intent2);
                    } catch (Exception e2) {
                        Toast.makeText(this, "Not Scanned", Toast.LENGTH_LONG).show();
                    }
                    if (result.getContents() != null) {
                        String string = result.getContents();
                        String[] parts = string.split("[:;]");
                        String to = parts[2];
                        String subject = parts[4];
                        String text = parts[6];
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {to});
                        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        intent.putExtra(Intent.EXTRA_TEXT, text);
                        intent.setType("text/html");
                        intent.setPackage("com.google.android.gm");
                        startActivity(Intent.createChooser(intent, "Send mail"));
                    }
                }
            }
            else {
                super.onActivityResult(requestCode, resultCode, data);

            }
        }

        @Override
        public void onClick(View v) {
            qrScan.initiateScan();
        }
}
