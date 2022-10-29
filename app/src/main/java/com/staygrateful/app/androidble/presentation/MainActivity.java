package com.staygrateful.app.androidble.presentation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.staygrateful.app.androidble.ble.ConnectionState;
import com.staygrateful.app.androidble.ble.model.Resource;
import com.staygrateful.app.androidble.ble.model.TempHumidityResult;
import com.staygrateful.app.androidble.presentation.viewmodel.HomeViewModel;
import com.staygrateful.app.androidble.databinding.ActivityMainBinding;
import com.staygrateful.app.androidble.util.DialogUtils;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pub.devrel.easypermissions.EasyPermissions;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    @Inject
    BluetoothAdapter bluetoothAdapter;

    private ActivityMainBinding binding;

    private HomeViewModel viewModel;

    private boolean isBluetootDialogAlreadyShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViewModel();
        initEvent();
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.setBluetoothStateListener(new HomeViewModel.HomeStateListener() {
            @Override
            public void onBluetoothStateChanged() {
                showBluetoothDialog();
            }

            @Override
            public void onStateChange(Resource<TempHumidityResult> resource) {
                runOnUiThread(() -> updateState(resource));
            }
        });
        getLifecycle().addObserver(viewModel);
    }

    private void initEvent() {
        binding.btnStart.setOnClickListener(v -> {
            if (viewModel.locationPermissionsGranted()) {
                viewModel.initializeConnection();
                DialogUtils.showToast(this, "Start");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLifecycle().removeObserver(viewModel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewModel.requestPermission(this);
        showBluetoothDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void showBluetoothDialog() {
        if (!bluetoothAdapter.isEnabled()) {
            if (!isBluetootDialogAlreadyShown) {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startBluetoothIntentForResult.launch(enableBluetoothIntent);
                isBluetootDialogAlreadyShown = true;
            }
        }
    }

    private final ActivityResultLauncher<Intent> startBluetoothIntentForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                isBluetootDialogAlreadyShown = false;
                if (result.getResultCode() != Activity.RESULT_OK) {
                    showBluetoothDialog();
                }
            });

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        DialogUtils.showToast(this, "Permission denied!");
    }

    private void updateState(Resource<TempHumidityResult> resource) {
        final ConnectionState state = viewModel.connectionState;
        if(state == ConnectionState.CurrentlyInitializing){
            if(viewModel.initializingMessage != null) {
                binding.tvInfo.setText(viewModel.initializingMessage);
                binding.tvInfo.setVisibility(View.VISIBLE);
            } else  {
                binding.tvInfo.setVisibility(View.GONE);
            }
            binding.progress.setVisibility(View.VISIBLE);
            binding.tvError.setVisibility(View.GONE);
            binding.btnStart.setVisibility(View.GONE);
        } else if(viewModel.errorMessage != null){
            //Try again
            binding.tvError.setText(viewModel.errorMessage);
            binding.tvError.setVisibility(View.VISIBLE);
            binding.btnStart.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.GONE);
            binding.tvInfo.setVisibility(View.GONE);
        }else if(state == ConnectionState.Connected){
            binding.tvInfo.setText(String.format("Berat : %s kg", viewModel.temperature));
            binding.tvInfo.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.GONE);
            binding.tvError.setVisibility(View.GONE);
            binding.btnStart.setVisibility(View.GONE);
        }else if(state == ConnectionState.Disconnected){
            //Initialize again
            binding.btnStart.setVisibility(View.VISIBLE);
            binding.tvInfo.setVisibility(View.GONE);
            binding.progress.setVisibility(View.GONE);
            binding.tvError.setVisibility(View.GONE);
        }
    }
}
