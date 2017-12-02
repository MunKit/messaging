package com.example.munkit.messaging;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.munkit.messaging.internal.Connections;
import com.example.munkit.messaging.model.ConnectionModel;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Connection connection;
    private ConnectionModel formModel;
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random random = new Random();
    private final MainActivity mainActivity = this;


    private ArrayList<String> connectionMap;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            /*Map<String, Connection> connections = Connections.getInstance(mainActivity)
                    .getConnections();
            connectionMap = new ArrayList<String>();
            int connectionIndex = 0;
            Iterator connectionIterator = connections.entrySet().iterator();
            while (connectionIterator.hasNext()){
                Map.Entry pair = (Map.Entry) connectionIterator.next();
                connectionMap.add((String) pair.getKey());
                ++connectionIndex;
            }*/

            FragmentManager fragmentManager = getSupportFragmentManager();
            Bundle arguments = new Bundle();
            arguments.putString(ActivityConstants.CONNECTION_KEY, connectionMap.get(0));
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            getSupportActionBar().setTitle("car messaging");
            FragmentMessage framessge = new FragmentMessage();
            framessge.setArguments(arguments);

            Fragmentvehiclenav fravehicle = new Fragmentvehiclenav();
            fravehicle.setArguments(arguments);
            switch (item.getItemId()) {
                case R.id.navigation_message:
                    transaction.replace(R.id.content, framessge).commit();
                    return true;
                case R.id.navigation_dashboard:
                    transaction.replace(R.id.content, fravehicle).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getSupportActionBar().setTitle("car messaging");
        populateConnection();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // add connection/ get new connection
        Map<String, Connection> connections = Connections.getInstance(mainActivity)
                .getConnections();
        connectionMap = new ArrayList<String>();
        int connectionIndex = 0;
        Iterator connectionIterator = connections.entrySet().iterator();
        while (connectionIterator.hasNext()){
            Map.Entry pair = (Map.Entry) connectionIterator.next();
            connectionMap.add((String) pair.getKey());
            ++connectionIndex;
        }

        // jump to message fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle arguments = new Bundle();
        arguments.putString(ActivityConstants.CONNECTION_KEY, connectionMap.get(0));
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        FragmentMessage framessge = new FragmentMessage();
        framessge.setArguments(arguments);
        transaction.replace(R.id.content, framessge).commit();

    }
    private void populateConnection(){

        // get all the available connections
        Map<String, Connection> connections = Connections.getInstance(this)
                .getConnections();
        int connectionIndex = 0;
        connectionMap = new ArrayList<String>();

        Iterator connectionIterator = connections.entrySet().iterator();
        while (connectionIterator.hasNext()){
            Map.Entry pair = (Map.Entry) connectionIterator.next();
            connectionMap.add((String) pair.getKey());
            ++connectionIndex;
        }
        if (connectionMap.size() == 0) {
            int length = 8;
            formModel = new ConnectionModel();
            //formModel.setClientId("Car messageing");
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(AB.charAt(random.nextInt(AB.length())));
            }
            formModel.setClientId(sb.toString() + "-Car messageing");
            String clientHandle = sb.toString() + '-' + formModel.getServerHostName() + '-' + formModel.getClientId();
            formModel.setClientHandle(clientHandle);
            persistAndConnect(formModel);
        }
        else
        {
            connection = connections.get(connectionMap.get(0));
            connect(connection);
        }

    }

    public void persistAndConnect(ConnectionModel model){
        Connection connection = Connection.createConnection(model.getClientHandle(),model.getClientId(),model.getServerHostName(),model.getServerPort(),this,model.isTlsConnection());
        connection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);


        String[] actionArgs = new String[1];
        actionArgs[0] = model.getClientId();
        final ActionListener callback = new ActionListener(this,
                ActionListener.Action.CONNECT, connection, actionArgs);
        connection.getClient().setCallback(new MqttCallbackHandler(this, model.getClientHandle()));



        connection.getClient().setTraceCallback(new MqttTraceCallback());

        MqttConnectOptions connOpts = optionsFromModel(model);

        connection.addConnectionOptions(connOpts);
        Connections.getInstance(this).addConnection(connection);


        try {
            connection.getClient().connect(connOpts, null, callback);

        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(),
                    "MqttException occurred", e);
        }

    }

    private MqttConnectOptions optionsFromModel(ConnectionModel model){

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(model.isCleanSession());
        connOpts.setConnectionTimeout(model.getTimeout());
        connOpts.setKeepAliveInterval(model.getKeepAlive());
        if(!model.getUsername().equals(ActivityConstants.empty)){
            connOpts.setUserName(model.getUsername());
        }

        if(!model.getPassword().equals(ActivityConstants.empty)){
            connOpts.setPassword(model.getPassword().toCharArray());
        }
        if(!model.getLwtTopic().equals(ActivityConstants.empty) && !model.getLwtMessage().equals(ActivityConstants.empty)){
            connOpts.setWill(model.getLwtTopic(), model.getLwtMessage().getBytes(), model.getLwtQos(), model.isLwtRetain());
        }
        //   if(tlsConnection){
        //       // TODO Add Keys to conOpts here
        //       //connOpts.setSocketFactory();
        //   }
        return connOpts;
    }
    public void connect(Connection connection) {
        String[] actionArgs = new String[1];
        actionArgs[0] = connection.getId();
        final ActionListener callback = new ActionListener(this,
                ActionListener.Action.CONNECT, connection, actionArgs);
        connection.getClient().setCallback(new MqttCallbackHandler(this, connection.handle()));
        try {
            connection.getClient().connect(connection.getConnectionOptions(), null, callback);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(),
                    "MqttException occurred", e);
        }
    }

}
