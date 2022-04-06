package project.sheridancollege.wash2goproject.util;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import project.sheridancollege.wash2goproject.ProviderLocation;
import project.sheridancollege.wash2goproject.R;
import project.sheridancollege.wash2goproject.ui.MainActivity;
import project.sheridancollege.wash2goproject.ui.maps.MapsFragment;

public class coorActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGELot = "cusLot";
    public static final String EXTRA_MESSAGELng = "cusLng";

    Button btnShowCoor;
    Button mapsBtn;
    EditText edtAddress;
    TextView txtCoordinates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coor);


        edtAddress = (EditText) findViewById(R.id.edtAddress);
        txtCoordinates = (TextView) findViewById(R.id.txtCoordinates);
        btnShowCoor = (Button) findViewById(R.id.btnShowCoor);
        mapsBtn = (Button) findViewById(R.id.mapBtn);

        btnShowCoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetCoordinates().execute(edtAddress.getText().toString().replace(" ","+"));
            }
        });


        mapsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(coorActivity.this, MainActivity.class );
                //intent.putExtra(EXTRA_MESSAGELot, loc.getLatitude());
                //intent.putExtra(EXTRA_MESSAGELng, loc.getLongitude() );
                startActivity(intent);
            }
        });

    }





    public class GetCoordinates extends AsyncTask<String, Void, String> {
        ProgressDialog dialog = new ProgressDialog(coorActivity.this);



        //Getting  from json value
        @Override
        protected String doInBackground(String... strings) {
            String response;
            try{
                String address = strings[0];
                HTTPDataHandler http = new HTTPDataHandler();
                String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=AIzaSyBcNe5mLxKAaeJSmsFz0F2E7jd-SmO_v5o",address);
                response = http.getHTTPData(url);
                return response;
            }catch (Exception ex){

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait ... ");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s){

             Location loc = new Location("");

            try{
                JSONObject jsonObject = new JSONObject (s);
                String lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lat").toString();
                String lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lng").toString();

                txtCoordinates.setText(String.format("Coordinates : %s / %s " , lat , lng));

                //Gettign the
                loc.setLatitude(Double.parseDouble(lat));
                loc.setLongitude(Double.parseDouble(lng));



                double latloc = loc.getLatitude();
                double lngLoc = loc.getLongitude();

                loc = createNewLocation(latloc, lngLoc);

                //customerLocaion.put("1", new ProviderLocation(loc.getLatitude() , loc.getLongitude()));

                System.out.println("I am in coor" + loc.getLatitude() );

                if(dialog.isShowing())
                    dialog.dismiss();


                Intent intent = new Intent(coorActivity.this, MainActivity.class );
                intent.putExtra(EXTRA_MESSAGELot, loc.getLatitude());
                intent.putExtra(EXTRA_MESSAGELng, loc.getLongitude() );
                startActivity(intent);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        Location createNewLocation(double latitude,  double longitude) {
            Location location = new Location("");
            location.setLongitude(longitude);
            location.setLatitude(latitude);
            return location;
        }

    }
}

