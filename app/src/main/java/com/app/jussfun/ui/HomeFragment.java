package com.app.jussfun.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.app.jussfun.base.App;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.util.concurrent.ListenableFuture;
import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.apprtc.util.AppRTCUtils;
import com.app.jussfun.external.CrystalRangeSeekbar;
import com.app.jussfun.external.OnRangeSeekbarChangeListener;
import com.app.jussfun.external.OnRangeSeekbarFinalValueListener;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.callback.OnOkCancelClickListener;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileRequest;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.Logging;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static com.app.jussfun.ui.RandomCallActivity.EXTRA_CIRCULAR_REVEAL_X;
import static com.app.jussfun.ui.RandomCallActivity.EXTRA_CIRCULAR_REVEAL_Y;

public class HomeFragment extends Fragment implements RandomWebSocket.WebSocketChannelEvents {

    private static final String TAG = HomeFragment.class.getSimpleName();
    @BindView(R.id.previewView)
    PreviewView previewView;
    @BindView(R.id.gemsLay)
    FrameLayout gemsLay;
    @BindView(R.id.filterLay)
    RelativeLayout filterLay;
    @BindView(R.id.recentLay)
    LinearLayout recentLay;
    @BindView(R.id.filterImageLay)
    RelativeLayout filterImageLay;
    @BindView(R.id.ivFilter)
    ImageView ivFilter;
    @BindView(R.id.radioBtnVideo)
    RadioButton radioBtnVideo;
    @BindView(R.id.btnVideoCall)
    FrameLayout btnVideoCall;
    @BindView(R.id.radioBtnAudio)
    RadioButton radioBtnAudio;
    @BindView(R.id.btnAudioCall)
    FrameLayout btnAudioCall;
    @BindView(R.id.txtTouchToStart)
    AppCompatTextView txtTouchToStart;
    @BindView(R.id.audioLay)
    FrameLayout audioLay;
    @BindView(R.id.bgImage)
    ImageView bgImage;
    private ApiInterface apiInterface;
    /*Filter Dialog*/
    BottomSheetDialog filterDialog;
    RelativeLayout filterSheet;
    LinearLayout btnMale, btnFemale, btnBoth, btnLocation;
    ImageView imageMale, imageFemale, imageBoth, imageLocation;
    TextView txtAge, txtFilterGems;
    CrystalRangeSeekbar seekBar;
    @BindView(R.id.parentLay)
    ConstraintLayout parentLay;
    @BindView(R.id.txtAudioStart)
    TextView txtAudioStart;
    @BindView(R.id.rippleBackground)
    RippleBackground rippleBackground;
    @BindView(R.id.audioRippleBackground)
    RippleBackground audioRippleBackground;
    @BindView(R.id.optionLay)
    LinearLayout optionLay;
    Animation zoomIn;
    Animation zoomOut;
    private int bottomNavHeight;
    private String genderSelected = Constants.TAG_BOTH;
    private int minimumAge = Constants.MIN_AGE;
    private int maximumAge = Constants.MAX_AGE;
    public static boolean filterApplied = false;
    private int heightPixels, widthPixels;
    private Context context;
    private ExecutorService executor;
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private AppUtils appUtils;

    private boolean isFilterApplied = false, isAllLocationSelected = false, isLocationFilter = false;
    private ArrayList<String> filterLocations = new ArrayList<>();
    private ArrayList<String> tempLocations = new ArrayList<>();
    boolean isCameraInit = false;

    public HomeFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightPixels = displayMetrics.heightPixels;
        widthPixels = displayMetrics.widthPixels;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_random_home, container, false);
        ButterKnife.bind(this, rootView);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(context);
        zoomIn = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_zoom_in);
        zoomOut = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_zoom_out);
        executor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        bgImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.his_msg_voicecall_bg));
        rippleBackground.startRippleAnimation();
        Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.fade);
        txtTouchToStart.startAnimation(a);
        ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) optionLay.getLayoutParams();
        newLayoutParams.topMargin = 0;
        newLayoutParams.leftMargin = 0;
        newLayoutParams.rightMargin = 0;
        newLayoutParams.bottomMargin = AppUtils.dpToPx(context, 66);
        optionLay.setLayoutParams(newLayoutParams);
//        updateCallType();

        tempLocations = (ArrayList<String>) AdminData.locationList;

        updateGemsCount();

        parentLay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (NetworkReceiver.isConnected()) {
                    if (isPermissionsGranted(AppRTCUtils.MANDATORY_PERMISSIONS)) {
                        App.preventMultipleClick(parentLay);
                        startCallActivity(parentLay);
                    } else {
                        requestMandatoryPermissions(AppRTCUtils.MANDATORY_PERMISSIONS);
                    }
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
                return false;
            }
        });
        return rootView;
    }

    private void initCameraView() {
        if (!isCameraInit) {
            isCameraInit = true;
            // Wait for the views to be properly laid out
            previewView.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "initCameraView: ");
                    // Bind use cases
                    bindCameraUseCases();
                }
            });
        }
    }

    private void bindCameraUseCases() {
        // Get screen metrics used to setup camera for full screen resolution
        int screenAspectRatio = aspectRatio(widthPixels, heightPixels);
        int rotation = previewView.getDisplay().getRotation();
        // Bind the CameraProvider to the LifeCycleOwner
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                // CameraProvider
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    // Preview
                    Preview preview = new Preview.Builder()
                            // We request aspect ratio but no resolution
                            .setTargetAspectRatio(screenAspectRatio)
                            // Set initial target rotation
                            .setTargetRotation(rotation)
                            .build();

                    // Attach the viewfinder's surface provider to preview use case
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                    // Must unbind the use-cases before rebinding them

                    cameraProvider.unbindAll();

                    try {
                        // A variable number of use-cases can be passed here -
                        // camera provides access to CameraControl & CameraInfo
                        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview);
                    } catch (Exception exception) {
                        Logging.e(TAG, "Use case binding failed: " + exception.getMessage());
                    }

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * Method used to re-draw the camera UI controls, called every time configuration changes.
     */
    private void updateCameraUi() {
        // Remove previous UI if any
//        parentLay.removeView(this);

        // In the background, load latest photo taken (if any) for gallery thumbnail
        // We can only change the foreground Drawable using API level 23+ API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Display flash animation to indicate that photo was captured
            parentLay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    parentLay.setForeground(new ColorDrawable(Color.WHITE));
                    parentLay.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            parentLay.setForeground(null);
                        }
                    }, Constants.ANIMATION_FAST_MILLIS);
                }
            }, Constants.ANIMATION_SLOW_MILLIS);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        RandomWebSocket.setCallEvents(this);
        if (checkCameraPermission()) {
            Log.e("checkCaemra", "true");
            initCameraView();
        } else {
            Log.e("checkCaemra", "false");
            requestCameraPermission();
        }
        connectWebSocket();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onPause() {
        super.onPause();
        RandomWebSocket.setCallEvents(null);
        // Shut down our background executor
        if (executor != null) {
            executor.shutdown();
        }
        isCameraInit = false;
        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            cameraProvider.unbindAll();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     * [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     * <p>
     * Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     * of preview ratio to one of the provided values.
     *
     * @param width  - preview width
     * @param height - preview height
     * @return suitable aspect ratio
     */
    private int aspectRatio(int width, int height) {
        int previewRatio = Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - AspectRatio.RATIO_4_3) <= Math.abs(previewRatio - AspectRatio.RATIO_16_9)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void connectWebSocket() {
        if (GetSet.getUserId() != null) {
            RandomWebSocket.mInstance = null;
            RandomWebSocket.getInstance(context);
        }
    }

    @OnClick({R.id.gemsLay, R.id.filterLay, R.id.recentLay,
            R.id.btnVideoCall, R.id.btnAudioCall, R.id.radioBtnVideo, R.id.radioBtnAudio})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.gemsLay:
                Intent gemsIntent = new Intent(context, GemsStoreActivity.class);
                gemsIntent.putExtra("OnClick", "ClickHere");
                gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gemsIntent);

                break;
            case R.id.filterLay:
                App.preventMultipleClick(filterLay);
                openFilterDialog();
                break;
            case R.id.recentLay:
                Intent recentIntent = new Intent(getActivity(), RecentHistoryActivity.class);
                recentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(recentIntent);
                break;
            case R.id.btnVideoCall:
            case R.id.radioBtnVideo: {
                txtTouchToStart.setVisibility(View.VISIBLE);
                radioBtnVideo.setChecked(true);
                radioBtnAudio.setChecked(false);
                audioLay.setVisibility(View.GONE);
                audioRippleBackground.stopRippleAnimation();
                rippleBackground.startRippleAnimation();
                Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.fade);
                txtTouchToStart.startAnimation(a);
                txtAudioStart.clearAnimation();
//                updateCallType();
            }
            break;
            case R.id.btnAudioCall:
            case R.id.radioBtnAudio: {
                txtTouchToStart.setVisibility(View.GONE);
                radioBtnVideo.setChecked(false);
                radioBtnAudio.setChecked(true);
                audioLay.setVisibility(View.VISIBLE);
                rippleBackground.stopRippleAnimation();
                audioRippleBackground.startRippleAnimation();
                Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.fade);
                txtAudioStart.startAnimation(a);
                txtTouchToStart.clearAnimation();
//                updateCallType();
            }
            break;
        }
    }

    private void startCallActivity(View view) {
        //calculates the center of the View v you are passing
        int revealX = (int) (view.getX() + view.getWidth() / 2);
        int revealY = (int) (view.getY() + view.getHeight() / 2);
        Intent randomChat = new Intent(getActivity(), RandomCallActivity.class);
        randomChat.putExtra(EXTRA_CIRCULAR_REVEAL_X, revealX);
        randomChat.putExtra(EXTRA_CIRCULAR_REVEAL_Y, revealY);
        randomChat.putExtra(Constants.TAG_CALL_TYPE, radioBtnVideo.isChecked() ? Constants.TAG_VIDEO : Constants.TAG_AUDIO);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(getActivity(), view, "transition");
        //just start the activity as an shared transition, but set the options bundle to null
        ActivityCompat.startActivity(context, randomChat, options.toBundle());
    }

    private void openFilterDialog() {
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        filterDialog = new BottomSheetDialog(getActivity(), R.style.Bottom_FilterDialog); // Style here
        filterDialog.setContentView(sheetView);
        filterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (getActivity() != null)
                    ((MainActivity) getActivity()).bottomNavigation.setVisibility(View.VISIBLE);
                if (filterApplied) {
                    GetSet.setFilterApplied(true);
                    GetSet.setFilterMinAge("" + minimumAge);
                    GetSet.setFilterMaxAge("" + maximumAge);
                    GetSet.setFilterGender(genderSelected);
                }
                updateFilterView();
            }
        });
        btnMale = sheetView.findViewById(R.id.btnMale);
        btnFemale = sheetView.findViewById(R.id.btnFemale);
        btnBoth = sheetView.findViewById(R.id.btnBoth);
        btnLocation = sheetView.findViewById(R.id.btnLocation);
        seekBar = sheetView.findViewById(R.id.seekBar);
        txtAge = sheetView.findViewById(R.id.txtAge);
        txtFilterGems = sheetView.findViewById(R.id.txtFilterGems);
        imageMale = sheetView.findViewById(R.id.imageMale);
        imageFemale = sheetView.findViewById(R.id.imageFemale);
        imageBoth = sheetView.findViewById(R.id.imageBoth);
        imageLocation = sheetView.findViewById(R.id.imageLocation);

        resetSeekBar();
        setListeners();

        if (GetSet.isFilterApplied()) {
            minimumAge = GetSet.getFilterMinAge() != null ? Integer.parseInt(GetSet.getFilterMinAge()) : Constants.MIN_AGE;
            maximumAge = GetSet.getFilterMaxAge() != null ? Integer.parseInt(GetSet.getFilterMaxAge()) : Constants.MAX_AGE;
            genderSelected = GetSet.getFilterGender();

            seekBar.setMinStartValue(minimumAge);
            seekBar.setMaxStartValue(maximumAge);
            seekBar.apply();

            switch (genderSelected) {
                case Constants.TAG_MALE:
                    imageMale.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                    imageFemale.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
                    imageBoth.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
                    break;
                case Constants.TAG_FEMALE:
                    imageMale.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
                    imageFemale.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                    imageBoth.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
                    break;
                case Constants.TAG_BOTH:
                    imageMale.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
                    imageFemale.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
                    imageBoth.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                    break;
            }
        } else {
            imageMale.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
            imageFemale.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
            imageBoth.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
        }

        if (getActivity() != null)
            ((MainActivity) getActivity()).bottomNavigation.setVisibility(View.GONE);
        filterDialog.show();
    }

    private void resetSeekBar() {
        seekBar.setMinValue(Constants.MIN_AGE);
        seekBar.setMaxValue(Constants.MAX_AGE);
        seekBar.setMinStartValue(Constants.MIN_AGE);
        seekBar.setMaxStartValue(Constants.MAX_AGE);
        seekBar.apply();
        txtAge.setText(Constants.MIN_AGE + " - " + Constants.MAX_AGE);
    }

    private void setListeners() {
        if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            txtFilterGems.setText(!TextUtils.isEmpty(AdminData.filterGems.getSubFilterPrice()) ? "" + Long.parseLong(AdminData.filterGems.getSubFilterPrice()) : "0");
        } else {
            txtFilterGems.setText(!TextUtils.isEmpty(AdminData.filterGems.getUnSubFilterPrice()) ? "" + Long.parseLong(AdminData.filterGems.getUnSubFilterPrice()) : "0");
        }
        btnMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkReceiver.isConnected()) {
                    if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                        if (GetSet.getGems() >= Long.parseLong(!TextUtils.isEmpty(AdminData.filterGems.getSubFilterPrice()) ? AdminData.filterGems.getSubFilterPrice() : "0")) {
                            applyGenderMale();
                        } else {
                            showGemsErrorDialog();
                        }
                    } else {
                        if (("" + AdminData.filterOptions.getGender().getNonPrime()).equals("1")) {
                            if (GetSet.getGems() >= Long.parseLong(!TextUtils.isEmpty(AdminData.filterGems.getUnSubFilterPrice()) ? AdminData.filterGems.getUnSubFilterPrice() : "0")) {
                                applyGenderMale();
                            } else {
                                showGemsErrorDialog();
                            }
                        } else {
                            goToPrime();
                        }
                    }
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
            }
        });

        btnFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkReceiver.isConnected()) {
                    if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                        if (GetSet.getGems() >= Long.parseLong(!TextUtils.isEmpty(AdminData.filterGems.getSubFilterPrice()) ? AdminData.filterGems.getSubFilterPrice() : "0")) {
                            applyGenderFemale();
                        } else {
                            showGemsErrorDialog();
                        }
                    } else {
                        if (("" + AdminData.filterOptions.getGender().getNonPrime()).equals("1")) {
                            if (GetSet.getGems() >= Long.parseLong(!TextUtils.isEmpty(AdminData.filterGems.getUnSubFilterPrice()) ? AdminData.filterGems.getUnSubFilterPrice() : "0")) {
                                applyGenderFemale();
                            } else {
                                showGemsErrorDialog();
                            }
                        } else {
                            goToPrime();
                        }
                    }
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
            }
        });

        btnBoth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkReceiver.isConnected()) {
                    if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                        if (GetSet.getGems() >= Long.parseLong(!TextUtils.isEmpty(AdminData.filterGems.getSubFilterPrice()) ? AdminData.filterGems.getSubFilterPrice() : "0")) {
                            applyGenderBoth();
                        } else {
                            showGemsErrorDialog();
                        }
                    } else {
                        if (("" + AdminData.filterOptions.getGender().getNonPrime()).equals("1")) {
                            if (GetSet.getGems() >= Long.parseLong(!TextUtils.isEmpty(AdminData.filterGems.getUnSubFilterPrice()) ? AdminData.filterGems.getUnSubFilterPrice() : "0")) {
                                applyGenderBoth();
                            } else {
                                showGemsErrorDialog();
                            }
                        } else {
                            goToPrime();
                        }
                    }
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkReceiver.isConnected()) {
                    App.preventMultipleClick(btnLocation);
                    if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                        if (GetSet.getGems() >= Long.parseLong(!TextUtils.isEmpty(AdminData.filterGems.getSubFilterPrice()) ? AdminData.filterGems.getSubFilterPrice() : "0")) {
                            Intent locationIntent = new Intent(getActivity(), LocationFilterActivity.class);
                            locationIntent.putExtra(Constants.TAG_FROM, "random");
                            locationIntent.putExtra(Constants.TAG_LOCATION_SELECTED, isAllLocationSelected);
                            if (isAllLocationSelected) {
                                locationIntent.putStringArrayListExtra(Constants.TAG_FILTER_LOCATION, (ArrayList<String>) AdminData.locationList);
                            } else {
                                locationIntent.putStringArrayListExtra(Constants.TAG_FILTER_LOCATION, tempLocations);
                            }
                            startActivityForResult(locationIntent, 100);
                        } else {
                            showGemsErrorDialog();
                        }
                    } else {
                        if (("" + AdminData.filterOptions.getLocation().getNonPrime()).equals("1")) {
                            if (GetSet.getGems() >= Long.parseLong(!TextUtils.isEmpty(AdminData.filterGems.getUnSubFilterPrice()) ? AdminData.filterGems.getUnSubFilterPrice() : "0")) {
                                Intent locationIntent = new Intent(getActivity(), LocationFilterActivity.class);
                                locationIntent.putExtra(Constants.TAG_FROM, "random");
                                locationIntent.putExtra(Constants.TAG_LOCATION_SELECTED, isAllLocationSelected);
                                if (isAllLocationSelected) {
                                    locationIntent.putStringArrayListExtra(Constants.TAG_FILTER_LOCATION, (ArrayList<String>) AdminData.locationList);
                                } else {
                                    locationIntent.putStringArrayListExtra(Constants.TAG_FILTER_LOCATION, tempLocations);
                                }
                                startActivityForResult(locationIntent, 100);
                            } else {
                                showGemsErrorDialog();
                            }
                        } else {
                            goToPrime();
                        }
                    }
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
            }
        });

        seekBar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                if (NetworkReceiver.isConnected()) {

                    if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                        if (GetSet.getGems() >= Long.parseLong(!TextUtils.isEmpty(AdminData.filterGems.getSubFilterPrice()) ? AdminData.filterGems.getSubFilterPrice() : "0")) {
                            applyAge(minValue, maxValue);
                        } else {
                            showGemsErrorDialog();
                            resetSeekBar();
                        }
                    } else {
                        if (("" + AdminData.filterOptions.getAge().getNonPrime()).equals("1")) {
                            if (GetSet.getGems() >= Long.parseLong(!TextUtils.isEmpty(AdminData.filterGems.getUnSubFilterPrice()) ? AdminData.filterGems.getUnSubFilterPrice() : "0")) {
                                applyAge(minValue, maxValue);
                            } else {
                                showGemsErrorDialog();
                                resetSeekBar();
                            }
                        } else {
                            resetSeekBar();
                            goToPrime();
                        }
                    }
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                    resetSeekBar();
                }
            }
        });

        seekBar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                txtAge.setText(minValue + " - " + maxValue);
            }
        });
    }

    private void goToPrime() {
        Intent prime = new Intent(getActivity(), PrimeActivity.class);
        prime.putExtra("OnCLick", "ClickHere");
        prime.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(prime);
    }

    void applyGenderBoth() {
//        App.makeToast(getString(R.string.both));
        filterApplied = true;
        imageBoth.startAnimation(zoomIn);
        imageBoth.startAnimation(zoomOut);
        imageMale.setAnimation(null);
        imageFemale.setAnimation(null);
        genderSelected = Constants.TAG_BOTH;
        imageMale.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
        imageFemale.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
        imageBoth.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
    }

    void applyGenderMale() {
//        App.makeToast(getString(R.string.male));
        filterApplied = true;
        imageMale.startAnimation(zoomIn);
        imageMale.startAnimation(zoomOut);
        imageFemale.setAnimation(null);
        imageBoth.setAnimation(null);
        genderSelected = Constants.TAG_MALE;
        imageMale.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
        imageFemale.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
        imageBoth.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
    }

    void applyGenderFemale() {
//        App.makeToast(getString(R.string.female));
        filterApplied = true;
        imageFemale.startAnimation(zoomIn);
        imageFemale.startAnimation(zoomOut);
        imageMale.setAnimation(null);
        imageBoth.setAnimation(null);
        genderSelected = Constants.TAG_FEMALE;
        imageMale.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
        imageFemale.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
        imageBoth.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
    }

    void applyAge(Number minValue, Number maxValue) {
        filterApplied = true;
        minimumAge = minValue.intValue();
        maximumAge = maxValue.intValue();
        txtAge.setText(minValue + " - " + maxValue);
    }

    private void updateCallType() {
        if (NetworkReceiver.isConnected()) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            request.setCallType(radioBtnVideo.isChecked() ? Constants.TAG_VIDEO : Constants.TAG_AUDIO);
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
//                    hideLoading();
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        MainActivity.profileResponse = profile;
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
//                    hideLoading();
                    call.cancel();
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            tempLocations = data.getStringArrayListExtra(Constants.TAG_FILTER_LOCATION);
            isAllLocationSelected = data.getBooleanExtra(Constants.TAG_LOCATION_SELECTED, false);

            filterApplied = true;
            GetSet.setFilterApplied(true);
            GetSet.setLocationApplied(true);
            updateFilterView();


        }
    }


    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     * <p>
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateCameraUi();
    }

    private void requestMandatoryPermissions(String[] permissions) {
        requestPermissions(permissions, AppRTCUtils.CALL_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case AppRTCUtils.CALL_PERMISSIONS_REQUEST_CODE:
                if (!isPermissionsGranted(permissions)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(MODIFY_AUDIO_SETTINGS) &&
                                shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                                shouldShowRequestPermissionRationale(INTERNET) && shouldShowRequestPermissionRationale(WAKE_LOCK)) {
                            requestPermissions(permissions, AppRTCUtils.CALL_PERMISSIONS_REQUEST_CODE);
                        } else {
                            App.makeToast(getString(R.string.camera_microphone_permission_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                } else {
                    Intent randomChat = new Intent(getActivity(), RandomCallActivity.class);
                    randomChat.putExtra(Constants.TAG_CALL_TYPE, radioBtnVideo.isChecked() ? Constants.TAG_VIDEO : Constants.TAG_AUDIO);
                    startActivity(randomChat);
                }
                break;
            case Constants.CAMERA_REQUEST_CODE:
                if (!isPermissionsGranted(permissions)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA)) {
                            requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                        } else {
//                            openPermissionDialog(permissions);
                            App.makeToast(getString(R.string.camera_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                } else {
                    initCameraView();
                }
                break;
        }
    }

    public boolean isPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showGemsErrorDialog() {
        DialogBuyGems dialogBuyGems = new DialogBuyGems();
        dialogBuyGems.setContext(context);
        dialogBuyGems.setCallBack(new OnOkCancelClickListener() {
            @Override
            public void onOkClicked(Object o) {
                Intent gemsIntent = new Intent(getActivity(), GemsStoreActivity.class);
                gemsIntent.putExtra("OnCLick", "ClickHere");
                gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gemsIntent);
                dialogBuyGems.dismiss();
            }

            @Override
            public void onCancelClicked(Object o) {
                dialogBuyGems.dismiss();
            }
        });
        dialogBuyGems.show(getActivity().getSupportFragmentManager(), TAG);
    }

    public void updateProfile() {
        updateGemsCount();
        updateFilterView();
    }

    public void updateGemsCount() {

    }

    public void updateFilterView() {
        if (GetSet.isFilterApplied()) {
            ivFilter.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            ivFilter.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite));
        }
    }

    private void changeLiveStatus(String liveStatus) {
        AppUtils.setCurrentStatus(liveStatus);
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_STATUS, liveStatus);
            json.put(Constants.TAG_FILTER_APPLIED, GetSet.isFilterApplied() ? "1" : "0");
            json.put(Constants.TAG_TYPE, Constants.TAG_LIVESTATUS);
            Logging.i(TAG, "changeLiveStatus: " + json);
            RandomWebSocket.getInstance(getActivity()).send(json.toString());
        } catch (JSONException e) {
            Logging.e(TAG, "changeLiveStatus: " + e.getMessage());
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(context,
                CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{CAMERA}, Constants.CAMERA_REQUEST_CODE);
        }
    }

    private void locationFilter() {

    }

    @Override
    public void onWebSocketConnected() {
        changeLiveStatus(Constants.ONLINE);
    }

    @Override
    public void onWebSocketMessage(String message) {

    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String description) {

    }
}
