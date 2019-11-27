package com.example.visitormgmt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.zxing.Result;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.OkHttpClient;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CheckOutActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    public RetrofitInterface ApiInterfaceObject;

    public String visitorID, mobileNumber, visitorCheckout;

    private ZXingScannerView mScannerView;

    private static final int CAMERA_PERMISSION_CODE = 1460;


    TextInputEditText visitorID_object, mobileNumber_object;
    Button checkout, scan_qr_code;
    TextView result ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeStatusBarColor("#40a7e5");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);

        visitorID_object = (TextInputEditText) findViewById(R.id.visitorid_txt);
        mobileNumber_object = (TextInputEditText) findViewById(R.id.mobile_txt);

        checkout = (Button) findViewById(R.id.proceed);
        scan_qr_code = (Button) findViewById(R.id.qrcode);

        result = (TextView) findViewById(R.id.validateInput);

        mScannerView = new ZXingScannerView(CheckOutActivity.this);



        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitorID = visitorID_object.getText().toString();
                mobileNumber = mobileNumber_object.getText().toString();
                if (visitorID.isEmpty() && mobileNumber.isEmpty()) {
                    result.setText("Please enter yout VisitorID");
                    return;
                } else if (!visitorID.isEmpty() && !mobileNumber.isEmpty()) {
                    result.setText("Please enter only one");
                    return;
                } else {

                    if(visitorID.isEmpty()){
                        create_checkout_mobile();
                    } else {

                        create_checkout();
                    }

                }
            }
        });



        scan_qr_code.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                if (EasyPermissions.hasPermissions(CheckOutActivity.this, Manifest.permission.CAMERA)) {

                    // Creating the object for Zxing to scan the QRCode.
                    mScannerView = new ZXingScannerView(CheckOutActivity.this);
                    // Set the scanner view as the content view.
                    setContentView(mScannerView);
                    // The camera is started to scan the QRCode.
                    mScannerView.startCamera();
                    // Setting the result handler to handle the result that getting from the QRCode.
                    mScannerView.setResultHandler(CheckOutActivity.this);

                } else {

                    //If permission is not present request for the same.
                    EasyPermissions.requestPermissions(CheckOutActivity.this, getString(R.string.permission_text), CAMERA_PERMISSION_CODE, Manifest.permission.CAMERA);
                }


//
            }
        });
    }





    private void   create_checkout() {



        OkHttpClient client1 = new OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS)
                .readTimeout(1000,TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client1)
                .baseUrl(RetrofitInterface.BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterfaceObject = retrofit.create(RetrofitInterface.class);



        JsonObject fields = new JsonObject();
        fields.addProperty("pnr", visitorID);



        System.out.println(">>>>>>>>>>>> >>>  In Checkout  pnr = "+ visitorID);




//        Call<Object> call = ApiInterfaceObject.createPost( token, fields);
        Call<checkOutStatus> call = ApiInterfaceObject.createCheckOut(visitorID);


        call.enqueue(new Callback<checkOutStatus>() {
            @Override
            public void onResponse(Call<checkOutStatus> call, Response<checkOutStatus> response) {
                System.out.println(">>>>>>>>>>>> >>>  before Post method");

                // textViewResult.setText();
                if (!response.isSuccessful()) {
                    result.setText("Response!!! failure");
                    return;
                }else{


                    try {
                        int status = response.body().getStatus();

                        System.out.println(">>>>>>>>>>>> >>> Response is coming  code=" + response.code() + " and status=" + status);

                        if(status == 400) {

                            result.setText("VisitorID does not exist");
                            System.out.println("Visitor Id does not exist");
                        }
                        else{
                            System.out.println(">>>>>>>>>>>> >>> code  CheckOut Succesfully  " + response.code());

                            Intent thankyouActivity = new Intent(CheckOutActivity.this, Thankyou.class);
                            // Start the new activity
                            startActivity(thankyouActivity);

                        }

                    }catch (Exception e) {
                        e.printStackTrace();
                    }



                }

            }

            @Override
            public void onFailure(Call<checkOutStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });


    }


    private void   create_checkout_mobile() {



        OkHttpClient client1 = new OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.SECONDS)
                .readTimeout(1000,TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client1)
                .baseUrl(RetrofitInterface.BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterfaceObject = retrofit.create(RetrofitInterface.class);


        System.out.println(">>>>>>>>>>>> >>>  In Checkout  mobile number = "+ mobileNumber);




//        Call<Object> call = ApiInterfaceObject.createPost( token, fields);
        Call<checkOutStatus> call = ApiInterfaceObject.createCheckOut_mobileNumber(mobileNumber);


        System.out.println(">>>>>>>>>>>> >>>  after calling Post method");



        call.enqueue(new Callback<checkOutStatus>() {
            @Override
            public void onResponse(Call<checkOutStatus> call, Response<checkOutStatus> response) {
                System.out.println(">>>>>>>>>>>> >>>  before Post method");

                // textViewResult.setText();
                if (!response.isSuccessful()) {
                    result.setText("Response!!! failure");
                    System.out.println(">>>>>>>>>>>> >>>Response!!! failure" );

                    return;
                }else{

                    System.out.println(">>>>>>>>>>>> >>> Checked-Out Successfully & code=  " + response.code());

                    try {
                        int status = response.body().getStatus();

                        if(status == 400) {

                            result.setText("Mobile number does not exist");
                        }
                        else{
                            Intent thankyouActivity = new Intent(CheckOutActivity.this, Thankyou.class);
                            // Start the new activity
                            startActivity(thankyouActivity);

                        }

                    }catch (Exception e) {
                        e.printStackTrace();
                    }



                }

            }

            @Override
            public void onFailure(Call<checkOutStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });


    }



    private void changeStatusBarColor(String color){
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
            //window.setNavigationBarColor(Color.parseColor(color));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE: {

                /* If the camera permission is granted the QRcode scanner will start*/
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Creating the object for Zxing to scan the QRCode.
                    mScannerView = new ZXingScannerView(CheckOutActivity.this);
                    // Set the scanner view as the content view
                    setContentView(mScannerView);
                    // The camera is started to scan the QRCode.
                    mScannerView.startCamera();
                    // Setting the result handler to handle the result that getting from the QRCode.
                    mScannerView.setResultHandler(CheckOutActivity.this);
                } else {
                    /* If the camera permission is not acceepted the error meassage will display*/
                    Toast.makeText(CheckOutActivity.this, "The app was not allowed to read your store.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }




    @Override
    public void handleResult(Result result) {

        visitorID = result.getText();
        System.out.println("VIsitor Id from QR Code" + visitorID);
        CheckOutWithQRCode();
    }

    private void CheckOutWithQRCode() {
        setContentView(R.layout.checkout_thankyou);
        create_checkout();

        //For Auto redirection to first page
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(CheckOutActivity.this,MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, 5000);

    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(CheckOutActivity.this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    @Override
    public void onStop() {
        mScannerView.stopCamera();
        super.onStop();
    }

}



