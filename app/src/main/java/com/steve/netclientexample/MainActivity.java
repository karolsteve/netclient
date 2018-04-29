package com.steve.netclientexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.steve.netclient.NetCallback;
import com.steve.netclient.NetClient;
import com.steve.netclient.NetError;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String testUrl = "www.test.com";

        String body = "...put raw body here...";

        /*
         * in your json response, you should have 2 keys, error and value...
         * error type should be boolean
         * JSONObjet is the type of value key...
         * if your webservice return and integer, replace it with Integer class
         */
        NetClient.newRequest(testUrl)
                .setBody(body)
                .enqueue(new NetCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject value) {
                        //On success
                    }

                    @Override
                    public void onError(NetError error) {
                        //On Error
                    }
                });
    }
}
