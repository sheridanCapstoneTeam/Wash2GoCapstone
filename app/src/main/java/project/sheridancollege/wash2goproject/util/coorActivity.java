package project.sheridancollege.wash2goproject.util;

import android.app.ProgressDialog;
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

import project.sheridancollege.wash2goproject.R;

public class coorActivity extends AppCompatActivity {

    Button btnShowCoor;
    EditText edtAddress;
    TextView txtCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coor);


        edtAddress = (EditText) findViewById(R.id.edtAddress);
        txtCoordinates = (TextView) findViewById(R.id.txtCoordinates);
        btnShowCoor = (Button) findViewById(R.id.btnShowCoor);

        btnShowCoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetCoordinates().execute(edtAddress.getText().toString().replace(" ","+"));
            }
        });

    }


    private class GetCoordinates extends AsyncTask<String, Void, String> {
        ProgressDialog dialog = new ProgressDialog(coorActivity.this);


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
            try{
                JSONObject jsonObject = new JSONObject (s);
                String lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lat").toString();
                String lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lng").toString();

                txtCoordinates.setText(String.format("Coordinates : %s / %s " , lat , lng));

                if(dialog.isShowing())
                    dialog.dismiss();


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

