package be.kuleuven.msec.iot.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;


import be.kuleuven.msec.iot.iotframework.callbackinterfaces.OnEventOccurred;
import be.kuleuven.msec.iot.iotframework.callbackinterfaces.OnRequestCompleted;
import be.kuleuven.msec.iot.iotframework.generic.devicelayer.Lamp;
import be.kuleuven.msec.iot.iotframework.generic.devicelayer.Plug;
import be.kuleuven.msec.iot.iotframework.generic.devicelayer.PressureSensor;
import be.kuleuven.msec.iot.iotframework.generic.devicelayer.TemperatureSensor;


public class MainActivity extends AppCompatActivity {

    Lamp lamp1;
    TemperatureSensor temp1;
    PressureSensor pres1;
    Plug plug1;



    TextView temperatureTextView;
    TextView pressureTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temperatureTextView = findViewById(R.id.temperature_textview);
        pressureTextView = findViewById(R.id.pressure_textview);

        final ApplicationEnvironment e = new ApplicationEnvironment(this);
        e.loadDevices(this, "configurations.json", new OnRequestCompleted() {
           @Override
           public void onSuccess(Object o) {
               lamp1 = (Lamp) e.getDeviceBySystemID("LAMP1");
               temp1 = (TemperatureSensor) e.getDeviceBySystemID("TEMP1");
               pres1 = (PressureSensor) e.getDeviceBySystemID("PRES1");
               plug1= (Plug) e.getDeviceBySystemID("PLUG1");

               temp1.monitorTemperature(new OnEventOccurred<Double>() {
                   @Override
                   public void onUpdate(Double aDouble) {
                       temperatureTextView.setText("TEMPERATURE: "+ aDouble + "°C");
                   }
               });
               pres1.monitorPressure(new OnEventOccurred<Double>() {
                   @Override
                   public void onUpdate(Double aDouble) {
                       pressureTextView.setText("PRESSURE: " + aDouble+"");
                   }
               });
           }
       });


    }

    public void buttonLightOnPush(View view) {
        lamp1.turnOn(new OnRequestCompleted<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                //todo
            }
        });
    }
    public void buttonLightOffPush(View view) {
        lamp1.turnOff(new OnRequestCompleted<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                //todo
            }
        });
    }

    public void buttonPlugOnPush(View view) {
        plug1.turnOn(new OnRequestCompleted<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                //todo
            }
        });
    }

    public void buttonPlugOffPush(View view) {
        plug1.turnOff(new OnRequestCompleted<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                //todo
            }
        });
    }

}
