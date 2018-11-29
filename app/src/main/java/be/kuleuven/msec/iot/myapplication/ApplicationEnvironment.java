package be.kuleuven.msec.iot.myapplication;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import be.kuleuven.msec.iot.iotframework.callbackinterfaces.OnRequestCompleted;
import be.kuleuven.msec.iot.iotframework.generic.componentlayer.Environment;
import be.kuleuven.msec.iot.iotframework.generic.devicelayer.HumiditySensor;
import be.kuleuven.msec.iot.iotframework.generic.devicelayer.Lamp;
import be.kuleuven.msec.iot.iotframework.generic.devicelayer.PressureSensor;
import be.kuleuven.msec.iot.iotframework.generic.devicelayer.TemperatureSensor;
import be.kuleuven.msec.iot.iotframework.generic.devicelayer.VirtualIoTConnector;
import be.kuleuven.msec.iot.iotframework.generic.devicelayer.VirtualIoTDevice;
import be.kuleuven.msec.iot.iotframework.implementations.lamps.huelamp.HueGateway;
import be.kuleuven.msec.iot.iotframework.implementations.plugs.tplinkhs110.HS110Connector;
import be.kuleuven.msec.iot.iotframework.implementations.sensorkits.allthingstalk.AllThingsTalkGateway;
import be.kuleuven.msec.iot.iotframework.implementations.sensorkits.versasense.VersaSenseGateway;
import be.kuleuven.msec.iot.iotframework.systemmanagement.constants.Connector_constants;
import be.kuleuven.msec.iot.iotframework.systemmanagement.constants.Device_constants;
import be.kuleuven.msec.iot.iotframework.systemmanagement.jsonmodel.JSMConnector;
import be.kuleuven.msec.iot.iotframework.systemmanagement.jsonmodel.JSMDevice;

/**
 * Created by ilsebohe on 16/01/2018.
 */

public class ApplicationEnvironment extends Environment {

    final private String TAG = "ApplicationEnvironment";
    Environment thisEnvironment;

    private static List<VirtualIoTDevice> devices;

    private Context context;

    public ApplicationEnvironment(Context context) {
        super();
        this.thisEnvironment=this;
        this.context = context;
        devices = new ArrayList<VirtualIoTDevice>();
        //room = new Room("Room");
    }
    public List<VirtualIoTDevice> getDevices(){
        return devices;
    };

    public VirtualIoTDevice getDeviceBySystemID(String systemID){
        for (VirtualIoTDevice dev: devices
             ) {
            if (dev.getSystemID().equals(systemID)) return dev;

        }
        return null;
    }

    public void loadDevices(Context context, String filename, final OnRequestCompleted orc ){
        this.getConfigurationFromServer(context, filename, new OnRequestCompleted<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                thisEnvironment.loadEnvironment(new OnRequestCompleted<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        orc.onSuccess(true);
                    }
                });
            }
        });
    }


    @Override
    public void loadEnvironment(final OnRequestCompleted<Boolean> orc) {

        final CountDownLatch latch = new CountDownLatch(configuration.getConnectors().size());
        System.out.println("number of connectors "+latch.getCount());
        new Thread(new Runnable() {
            @Override
            public void run() {
                //initializeConnectors
                for (JSMConnector connector : configuration.getConnectors()) {
                    switch (connector.getType()) {
                        case Connector_constants.CONNECTORTYPE_HUE:
                            virtualIoTConnectors.add(new HueGateway(connector.getSystemID(), connector.getSettings()));
                            break;
                        case Connector_constants.CONNECTORTYPE_ALL_THINGS_TALK:
                            virtualIoTConnectors.add(new AllThingsTalkGateway(connector.getSystemID(), connector.getSettings()));
                            break;
                        case Connector_constants.CONNECTORTYPE_VERSASENSE:
                            virtualIoTConnectors.add(new VersaSenseGateway(connector.getSystemID(), connector.getSettings()));
                            break;
                        case Connector_constants.CONNECTORTYPE_TPLINKPLUG:
                            virtualIoTConnectors.add(new HS110Connector(connector.getSystemID(), connector.getSettings()));
                            break;
                    }
                }
                for (final VirtualIoTConnector c : virtualIoTConnectors) {
                    c.initialize(new OnRequestCompleted() {
                        @Override
                        public void onSuccess(Object response) {
                            c.updateConnectedDeviceList(new OnRequestCompleted<Boolean>() {
                                @Override
                                public void onSuccess(Boolean response) {
                                    latch.countDown();
                                    System.out.println("SUCCES UPDATE "+c.getSystemID());
                                }
                            }, configuration.getComponents().get(0).getDevices());
                        }
                    });
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (JSMDevice d : configuration.getComponents().get(0).getDevices()) {
                    System.out.println("devices" + devices + " " + d.getConnector() +  " " + getConnectorBySystemID(d.getConnector()) );
                    devices.add(getConnectorBySystemID(d.getConnector()).getConnectedDeviceBasedOnSystemID(d.getSystemID()));
                }
                orc.onSuccess(true);
            }
        }).start();
    }
}
