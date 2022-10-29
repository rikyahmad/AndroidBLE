package com.staygrateful.app.androidble.presentation.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.staygrateful.app.androidble.ble.ConnectionState;
import com.staygrateful.app.androidble.ble.TemperatureAndHumidityReceiveManager;
import com.staygrateful.app.androidble.ble.model.Resource;
import com.staygrateful.app.androidble.ble.model.TempHumidityResult;
import com.staygrateful.app.androidble.receiver.BluetoothBroadcastReceiver;
import com.staygrateful.app.androidble.util.DialogUtils;
import com.staygrateful.app.androidble.util.PermissionUtils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pub.devrel.easypermissions.EasyPermissions;

@HiltViewModel
public class HomeViewModel extends AndroidViewModel implements
        DefaultLifecycleObserver {

    private final TemperatureAndHumidityReceiveManager manager;
    private HomeStateListener homeStateListener;
    public ConnectionState connectionState = ConnectionState.Uninitialized;
    public String initializingMessage = null;
    public String errorMessage = null;
    public float temperature = 0f;
    public float humidity = 0f;

    @Inject
    public HomeViewModel(Application application, TemperatureAndHumidityReceiveManager manager) {
        super(application);
        this.manager = manager;
    }

    private final BluetoothBroadcastReceiver receiver = new BluetoothBroadcastReceiver(new BluetoothBroadcastReceiver.EventState() {
        @Override
        public void onBluetoothStateChanged(Intent intent) {
            if (homeStateListener != null) {
                homeStateListener.onBluetoothStateChanged();
            }
        }
    });

    public boolean locationPermissionsGranted() {
        return EasyPermissions.hasPermissions(
                getApplication().getBaseContext(), PermissionUtils.permissions()
        );
    }

    public void requestPermission(Activity activity) {
        if (locationPermissionsGranted()) {
            if(connectionState == ConnectionState.Uninitialized){
                initializeConnection();
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(activity,
                    "Required location permission access",
                    1,
                    PermissionUtils.permissions()
            );
        }
    }

    public void setBluetoothStateListener(HomeStateListener homeStateListener) {
        this.homeStateListener = homeStateListener;
    }

    public void disconnect(){
        manager.disconnect();
    }

    public void reconnect(){
        manager.reconnect();
    }

    public void initializeConnection(){
        errorMessage = null;
        subscribeToChanges();
        manager.startReceiving();
    }

    private void subscribeToChanges() {
        manager.setTemperatureListener(resource -> {
            if (resource instanceof Resource.Success) {
                final Resource.Success<TempHumidityResult> result =
                        (Resource.Success<TempHumidityResult>) resource;
                connectionState = result.getData().getConnectionState();
                temperature = result.getData().getTemperature();
                humidity = result.getData().getHumidity();
            } else if (resource instanceof Resource.Loading) {
                final Resource.Loading<TempHumidityResult> result =
                        (Resource.Loading<TempHumidityResult>) resource;
                initializingMessage = result.getMessage();
                connectionState = ConnectionState.CurrentlyInitializing;
            } else if (resource instanceof Resource.Error) {
                final Resource.Error<TempHumidityResult> result =
                        (Resource.Error<TempHumidityResult>) resource;
                errorMessage = result.getErrorMessage();
                connectionState = ConnectionState.Uninitialized;
            }
            if (homeStateListener != null) {
                homeStateListener.onStateChange(resource);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        BluetoothBroadcastReceiver.register(getApplication().getBaseContext(), receiver);

        if (locationPermissionsGranted()) {
            if (connectionState == ConnectionState.Disconnected) {
                reconnect();
            }
        }
        //DialogUtils.showToast(getApplication().getBaseContext(), "On Start");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        BluetoothBroadcastReceiver.unregister(getApplication().getBaseContext(), receiver);

        if (connectionState == ConnectionState.Connected){
            disconnect();
        }
        //DialogUtils.showToast(getApplication().getBaseContext(), "On Stop");
    }

    public interface HomeStateListener {
        void onBluetoothStateChanged();
        void onStateChange(Resource<TempHumidityResult> resource);
    }
}
