package fr.free.nrw.commons.nearby.fragments;

import static fr.free.nrw.commons.location.LocationServiceManager.LocationChangeType.CUSTOM_QUERY;
import static fr.free.nrw.commons.location.LocationServiceManager.LocationChangeType.LOCATION_SIGNIFICANTLY_CHANGED;
import static fr.free.nrw.commons.location.LocationServiceManager.LocationChangeType.LOCATION_SLIGHTLY_CHANGED;
import static fr.free.nrw.commons.location.LocationServiceManager.LocationChangeType.MAP_UPDATED;
import static fr.free.nrw.commons.wikidata.WikidataConstants.PLACE_OBJECT;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleCoroutineScope;
import androidx.lifecycle.LifecycleOwnerKt;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding3.appcompat.RxSearchView;
import fr.free.nrw.commons.CommonsApplication;
import fr.free.nrw.commons.CommonsApplication.BaseLogoutListener;
import fr.free.nrw.commons.MapController.NearbyPlacesInfo;
import fr.free.nrw.commons.R;
import fr.free.nrw.commons.Utils;
import fr.free.nrw.commons.bookmarks.locations.BookmarkLocationsDao;
import fr.free.nrw.commons.contributions.ContributionController;
import fr.free.nrw.commons.contributions.MainActivity;
import fr.free.nrw.commons.contributions.MainActivity.ActiveFragment;
import fr.free.nrw.commons.databinding.FragmentNearbyParentBinding;
import fr.free.nrw.commons.di.CommonsDaggerSupportFragment;
import fr.free.nrw.commons.kvstore.JsonKvStore;
import fr.free.nrw.commons.location.LatLng;
import fr.free.nrw.commons.location.LocationPermissionsHelper;
import fr.free.nrw.commons.location.LocationPermissionsHelper.LocationPermissionCallback;
import fr.free.nrw.commons.location.LocationServiceManager;
import fr.free.nrw.commons.location.LocationServiceManager.LocationChangeType;
import fr.free.nrw.commons.location.LocationUpdateListener;
import fr.free.nrw.commons.nearby.BottomSheetAdapter;
import fr.free.nrw.commons.nearby.CheckBoxTriStates;
import fr.free.nrw.commons.nearby.Label;
import fr.free.nrw.commons.nearby.MarkerPlaceGroup;
import fr.free.nrw.commons.nearby.NearbyController;
import fr.free.nrw.commons.nearby.NearbyFilterSearchRecyclerViewAdapter;
import fr.free.nrw.commons.nearby.NearbyFilterState;
import fr.free.nrw.commons.nearby.Place;
import fr.free.nrw.commons.nearby.PlacesRepository;
import fr.free.nrw.commons.nearby.Sitelinks;
import fr.free.nrw.commons.nearby.WikidataFeedback;
import fr.free.nrw.commons.nearby.contract.NearbyParentFragmentContract;
import fr.free.nrw.commons.nearby.fragments.AdvanceQueryFragment.Callback;
import fr.free.nrw.commons.nearby.model.BottomSheetItem;
import fr.free.nrw.commons.nearby.presenter.NearbyParentFragmentPresenter;
import fr.free.nrw.commons.upload.FileUtils;
import fr.free.nrw.commons.utils.DialogUtil;
import fr.free.nrw.commons.utils.ExecutorUtils;
import fr.free.nrw.commons.utils.LayoutUtils;
import fr.free.nrw.commons.utils.MapUtils;
import fr.free.nrw.commons.utils.NearbyFABUtils;
import fr.free.nrw.commons.utils.NetworkUtils;
import fr.free.nrw.commons.utils.SystemThemeUtils;
import fr.free.nrw.commons.utils.ViewUtil;
import fr.free.nrw.commons.wikidata.WikidataEditListener;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.constants.GeoConstants.UnitOfMeasure;
import org.osmdroid.views.CustomZoomButtonsController.Visibility;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.ScaleDiskOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import timber.log.Timber;


public class NearbyParentFragment extends CommonsDaggerSupportFragment
    implements NearbyParentFragmentContract.View,
    WikidataEditListener.WikidataP18EditListener, LocationUpdateListener,
    LocationPermissionCallback, BottomSheetAdapter.ItemClickListener {

    FragmentNearbyParentBinding binding;

    public final MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
        @Override
        public boolean singleTapConfirmedHelper(GeoPoint p) {
            if (clickedMarker != null) {
                clickedMarker.closeInfoWindow();
            } else {
                Timber.e("CLICKED MARKER IS NULL");
            }
            if (isListBottomSheetExpanded()) {
                // Back should first hide the bottom sheet if it is expanded
                hideBottomSheet();
            } else if (isDetailsBottomSheetVisible()) {
                hideBottomDetailsSheet();
            }
            return true;
        }

        @Override
        public boolean longPressHelper(GeoPoint p) {
            return false;
        }
    });

    @Inject
    LocationServiceManager locationManager;
    @Inject
    NearbyController nearbyController;
    @Inject
    @Named("default_preferences")
    JsonKvStore applicationKvStore;
    @Inject
    BookmarkLocationsDao bookmarkLocationDao;
    @Inject
    PlacesRepository placesRepository;
    @Inject
    ContributionController controller;
    @Inject
    WikidataEditListener wikidataEditListener;
    @Inject
    SystemThemeUtils systemThemeUtils;
    @Inject
    CommonPlaceClickActions commonPlaceClickActions;

    private LocationPermissionsHelper locationPermissionsHelper;
    private NearbyFilterSearchRecyclerViewAdapter nearbyFilterSearchRecyclerViewAdapter;
    private BottomSheetBehavior bottomSheetListBehavior;
    private BottomSheetBehavior bottomSheetDetailsBehavior;
    private Animation rotate_backward;
    private Animation fab_close;
    private Animation fab_open;
    private Animation rotate_forward;
    private static final float ZOOM_LEVEL = 15f;
    private final String NETWORK_INTENT_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private BroadcastReceiver broadcastReceiver;
    private boolean isNetworkErrorOccurred;
    private Snackbar snackbar;
    private View view;
    private LifecycleCoroutineScope scope;
    private NearbyParentFragmentPresenter presenter;
    private boolean isDarkTheme;
    private boolean isFABsExpanded;
    private Place selectedPlace;
    private Marker clickedMarker;
    private ProgressDialog progressDialog;
    private final double CAMERA_TARGET_SHIFT_FACTOR_PORTRAIT = 0.005;
    private final double CAMERA_TARGET_SHIFT_FACTOR_LANDSCAPE = 0.004;
    private boolean isPermissionDenied;
    private boolean recenterToUserLocation;
    private GeoPoint mapCenter;
    IntentFilter intentFilter = new IntentFilter(NETWORK_INTENT_ACTION);
    private Place lastPlaceToCenter;
    private LatLng lastKnownLocation;
    private boolean isVisibleToUser;
    private LatLng lastFocusLocation;
    private PlaceAdapter adapter;
    private GeoPoint lastMapFocus;
    private NearbyParentFragmentInstanceReadyCallback nearbyParentFragmentInstanceReadyCallback;
    private boolean isAdvancedQueryFragmentVisible = false;
    private Place nearestPlace;
    private volatile boolean stopQuery;

    // Explore map data (for if we came from Explore)
    private double prevZoom;
    private double prevLatitude;
    private double prevLongitude;

    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    private LatLng updatedLatLng;
    private boolean searchable;

    private ConstraintLayout nearbyLegend;

    private GridLayoutManager gridLayoutManager;
    private List<BottomSheetItem> dataList;
    private BottomSheetAdapter bottomSheetAdapter;

    private final ActivityResultLauncher<Intent> galleryPickLauncherForResult =
        registerForActivityResult(new StartActivityForResult(),
            result -> {
                controller.handleActivityResultWithCallback(requireActivity(), callbacks -> {
                    controller.onPictureReturnedFromGallery(result, requireActivity(), callbacks);
                });
            });

    private final ActivityResultLauncher<Intent> customSelectorLauncherForResult =
        registerForActivityResult(new StartActivityForResult(),
            result -> {
                controller.handleActivityResultWithCallback(requireActivity(), callbacks -> {
                    controller.onPictureReturnedFromCustomSelector(result, requireActivity(),
                        callbacks);
                });
            });

    private final ActivityResultLauncher<Intent> cameraPickLauncherForResult =
        registerForActivityResult(new StartActivityForResult(),
            result -> {
                controller.handleActivityResultWithCallback(requireActivity(), callbacks -> {
                    controller.onPictureReturnedFromCamera(result, requireActivity(), callbacks);
                });
            });

    private ActivityResultLauncher<String[]> inAppCameraLocationPermissionLauncher = registerForActivityResult(
        new RequestMultiplePermissions(),
        new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean areAllGranted = true;
                for (final boolean b : result.values()) {
                    areAllGranted = areAllGranted && b;
                }

                if (areAllGranted) {
                    controller.locationPermissionCallback.onLocationPermissionGranted();
                } else {
                    if (shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)) {
                        controller.handleShowRationaleFlowCameraLocation(getActivity(),
                            inAppCameraLocationPermissionLauncher, cameraPickLauncherForResult);
                    } else {
                        controller.locationPermissionCallback.onLocationPermissionDenied(
                            getActivity().getString(
                                R.string.in_app_camera_location_permission_denied));
                    }
                }
            }
        });

    private ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
        new RequestPermission(), isGranted -> {
            if (isGranted) {
                locationPermissionGranted();
            } else {
                if (shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)) {
                    DialogUtil.showAlertDialog(getActivity(),
                        getActivity().getString(R.string.location_permission_title),
                        getActivity().getString(R.string.location_permission_rationale_nearby),
                        getActivity().getString(android.R.string.ok),
                        getActivity().getString(android.R.string.cancel),
                        () -> {
                            askForLocationPermission();
                        },
                        null,
                        null
                    );
                } else {
                    if (isPermissionDenied) {
                        locationPermissionsHelper.showAppSettingsDialog(getActivity(),
                            R.string.nearby_needs_location);
                    }
                    Timber.d("The user checked 'Don't ask again' or denied the permission twice");
                    isPermissionDenied = true;
                }
            }
        });

    /**
     * WLM URL
     */
    public static final String WLM_URL = "https://commons.wikimedia.org/wiki/Commons:Mobile_app/Contributing_to_WLM_using_the_app";

    @NonNull
    public static NearbyParentFragment newInstance() {
        NearbyParentFragment fragment = new NearbyParentFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
        final Bundle savedInstanceState) {
        loadExploreMapData();

        binding = FragmentNearbyParentBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        initNetworkBroadCastReceiver();
        scope = LifecycleOwnerKt.getLifecycleScope(getViewLifecycleOwner());
        presenter = new NearbyParentFragmentPresenter(bookmarkLocationDao, placesRepository,
            nearbyController);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Saving in progress...");
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        return view;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu,
        @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.nearby_fragment_menu, menu);
        MenuItem refreshButton = menu.findItem(R.id.item_refresh);
        MenuItem listMenu = menu.findItem(R.id.list_sheet);
        MenuItem showInExploreButton = menu.findItem(R.id.list_item_show_in_explore);
        MenuItem saveAsGPXButton = menu.findItem(R.id.list_item_gpx);
        MenuItem saveAsKMLButton = menu.findItem(R.id.list_item_kml);
        refreshButton.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    emptyCache();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        });
        listMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                listOptionMenuItemClicked();
                return false;
            }
        });
        showInExploreButton.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                ((MainActivity) getContext()).loadExploreMapFromNearby(
                    binding.map.getZoomLevelDouble(),
                    binding.map.getMapCenter().getLatitude(),
                    binding.map.getMapCenter().getLongitude()
                );
                return false;
            }
        });
        saveAsGPXButton.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                try {
                    progressDialog.setTitle(getString(R.string.saving_gpx_file));
                    progressDialog.show();
                    savePlacesAsGPX();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        });
        saveAsKMLButton.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                try {
                    progressDialog.setTitle(getString(R.string.saving_kml_file));
                    progressDialog.show();
                    savePlacesAsKML();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isDarkTheme = systemThemeUtils.isDeviceInNightMode();
        if (Utils.isMonumentsEnabled(new Date())) {
            binding.rlContainerWlmMonthMessage.setVisibility(View.VISIBLE);
        } else {
            binding.rlContainerWlmMonthMessage.setVisibility(View.GONE);
        }
        locationPermissionsHelper = new LocationPermissionsHelper(getActivity(), locationManager,
            this);

        // Set up the floating activity button to toggle the visibility of the legend
        binding.fabLegend.setOnClickListener(v -> {
            if (binding.nearbyLegendLayout.getRoot().getVisibility() == View.VISIBLE) {
                binding.nearbyLegendLayout.getRoot().setVisibility(View.GONE);
            } else {
                binding.nearbyLegendLayout.getRoot().setVisibility(View.VISIBLE);
            }
        });

        presenter.attachView(this);
        isPermissionDenied = false;
        recenterToUserLocation = false;
        initThemePreferences();
        initViews();
        presenter.setActionListeners(applicationKvStore);
        org.osmdroid.config.Configuration.getInstance().load(this.getContext(),
            PreferenceManager.getDefaultSharedPreferences(this.getContext()));

        // Use the Wikimedia tile server, rather than OpenStreetMap (Mapnik) which has various
        // restrictions that we do not satisfy.
        binding.map.setTileSource(TileSourceFactory.WIKIMEDIA);
        binding.map.setTilesScaledToDpi(true);

        // Add referer HTTP header because the Wikimedia tile server requires it.
        // This was suggested by Dmitry Brant within an email thread between us and WMF.
        org.osmdroid.config.Configuration.getInstance().getAdditionalHttpRequestProperties().put(
            "Referer", "http://maps.wikimedia.org/"
        );

        if (applicationKvStore.getString("LastLocation")
            != null) { // Checking for last searched location
            String[] locationLatLng = applicationKvStore.getString("LastLocation").split(",");
            lastMapFocus = new GeoPoint(Double.valueOf(locationLatLng[0]),
                Double.valueOf(locationLatLng[1]));
        } else {
            lastMapFocus = new GeoPoint(51.50550, -0.07520);
        }
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(binding.map);
        scaleBarOverlay.setScaleBarOffset(15, 25);
        Paint barPaint = new Paint();
        barPaint.setARGB(200, 255, 250, 250);
        scaleBarOverlay.setBackgroundPaint(barPaint);
        scaleBarOverlay.enableScaleBar();
        binding.map.getOverlays().add(scaleBarOverlay);
        binding.map.getZoomController().setVisibility(Visibility.NEVER);
        binding.map.getController().setZoom(ZOOM_LEVEL);
        // if we came from Explore map using 'Show in Nearby', load Explore map camera position
        if (isCameFromExploreMap()) {
            moveCameraToPosition(
                new GeoPoint(prevLatitude, prevLongitude),
                prevZoom,
                1L
            );
        }
        binding.map.getOverlays().add(mapEventsOverlay);

        binding.map.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                presenter.handleMapScrolled(scope, !isNetworkErrorOccurred);
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }

        });

        binding.map.setMultiTouchControls(true);
        if (nearbyParentFragmentInstanceReadyCallback != null) {
            nearbyParentFragmentInstanceReadyCallback.onReady();
        }
        initNearbyFilter();
        addCheckBoxCallback();
        if (!isCameFromExploreMap()) {
            moveCameraToPosition(lastMapFocus);
        }
        initRvNearbyList();
        onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.tvAttribution.setText(
                Html.fromHtml(getString(R.string.map_attribution), Html.FROM_HTML_MODE_LEGACY));
        } else {
            //noinspection deprecation
            binding.tvAttribution.setText(Html.fromHtml(getString(R.string.map_attribution)));
        }
        binding.tvAttribution.setMovementMethod(LinkMovementMethod.getInstance());
        binding.nearbyFilterList.btnAdvancedOptions.setOnClickListener(v -> {
            binding.nearbyFilter.searchViewLayout.searchView.clearFocus();
            showHideAdvancedQueryFragment(true);
            final AdvanceQueryFragment fragment = new AdvanceQueryFragment();
            final Bundle bundle = new Bundle();
            try {
                bundle.putString("query",
                    FileUtils.INSTANCE.readFromResource(
                        "/queries/radius_query_for_upload_wizard.rq")
                );
            } catch (IOException e) {
                Timber.e(e);
            }
            fragment.setArguments(bundle);
            fragment.callback = new Callback() {
                @Override
                public void close() {
                    showHideAdvancedQueryFragment(false);
                }

                @Override
                public void reset() {
                    presenter.setAdvancedQuery(null);
                    presenter.updateMapAndList(LOCATION_SIGNIFICANTLY_CHANGED);
                    showHideAdvancedQueryFragment(false);
                }

                @Override
                public void apply(@NotNull final String query) {
                    presenter.setAdvancedQuery(query);
                    presenter.updateMapAndList(CUSTOM_QUERY);
                    showHideAdvancedQueryFragment(false);
                }
            };
            getChildFragmentManager().beginTransaction()
                .replace(R.id.fl_container_nearby_children, fragment)
                .commit();
        });

        binding.tvLearnMore.setOnClickListener(v -> onLearnMoreClicked());

        if (!locationPermissionsHelper.checkLocationPermission(getActivity())) {
            askForLocationPermission();
        }
    }

    /**
     * Fetch Explore map camera data from fragment arguments if any.
     */
    public void loadExploreMapData() {
        // get fragment arguments
        if (getArguments() != null) {
            prevZoom = getArguments().getDouble("prev_zoom");
            prevLatitude = getArguments().getDouble("prev_latitude");
            prevLongitude = getArguments().getDouble("prev_longitude");
        }
    }

    /**
     * Checks if fragment arguments contain data from Explore map. if present, then the user
     * navigated from Explore using 'Show in Nearby'.
     *
     * @return true if user navigated from Explore map
     **/
    public boolean isCameFromExploreMap() {
        return prevZoom != 0.0 || prevLatitude != 0.0 || prevLongitude != 0.0;
    }

    /**
     * Initialise background based on theme, this should be doe ideally via styles, that would need
     * another refactor
     */
    private void initThemePreferences() {
        if (isDarkTheme) {
            binding.bottomSheetNearby.rvNearbyList.setBackgroundColor(
                getContext().getResources().getColor(R.color.contributionListDarkBackground));
            binding.nearbyFilterList.checkboxTriStates.setTextColor(
                getContext().getResources().getColor(android.R.color.white));
            binding.nearbyFilterList.checkboxTriStates.setTextColor(
                getContext().getResources().getColor(android.R.color.white));
            binding.nearbyFilterList.getRoot().setBackgroundColor(
                getContext().getResources().getColor(R.color.contributionListDarkBackground));
            binding.map.getOverlayManager().getTilesOverlay()
                .setColorFilter(TilesOverlay.INVERT_COLORS);
        } else {
            binding.bottomSheetNearby.rvNearbyList.setBackgroundColor(
                getContext().getResources().getColor(android.R.color.white));
            binding.nearbyFilterList.checkboxTriStates.setTextColor(
                getContext().getResources().getColor(R.color.contributionListDarkBackground));
            binding.nearbyFilterList.getRoot().setBackgroundColor(
                getContext().getResources().getColor(android.R.color.white));
            binding.nearbyFilterList.getRoot().setBackgroundColor(
                getContext().getResources().getColor(android.R.color.white));
        }
    }

    private void initRvNearbyList() {
        binding.bottomSheetNearby.rvNearbyList.setLayoutManager(
            new LinearLayoutManager(getContext()));
        adapter = new PlaceAdapter(bookmarkLocationDao,
            place -> {
                moveCameraToPosition(
                    new GeoPoint(place.location.getLatitude(), place.location.getLongitude()));
                return Unit.INSTANCE;
            },
            (place, isBookmarked) -> {
                presenter.toggleBookmarkedStatus(place);
                return Unit.INSTANCE;
            },
            commonPlaceClickActions,
            inAppCameraLocationPermissionLauncher,
            galleryPickLauncherForResult,
            cameraPickLauncherForResult
        );
        binding.bottomSheetNearby.rvNearbyList.setAdapter(adapter);
    }

    private void addCheckBoxCallback() {
        binding.nearbyFilterList.checkboxTriStates.setCallback(
            (o, state, b, b1) -> presenter.filterByMarkerType(o, state, b, b1));
    }

    private void performMapReadyActions() {
        if (((MainActivity) getActivity()).activeFragment == ActiveFragment.NEARBY) {
            if (applicationKvStore.getBoolean("doNotAskForLocationPermission", false) &&
                !locationPermissionsHelper.checkLocationPermission(getActivity())) {
                isPermissionDenied = true;
            }
        }
        presenter.onMapReady();
    }

    @Override
    public void askForLocationPermission() {
        Timber.d("Asking for location permission");
        locationPermissionLauncher.launch(permission.ACCESS_FINE_LOCATION);
    }

    private void locationPermissionGranted() {
        isPermissionDenied = false;
        applicationKvStore.putBoolean("doNotAskForLocationPermission", false);
        lastKnownLocation = locationManager.getLastLocation();
        LatLng target = lastKnownLocation;
        if (lastKnownLocation != null) {
            GeoPoint targetP = new GeoPoint(target.getLatitude(), target.getLongitude());
            mapCenter = targetP;
            binding.map.getController().setCenter(targetP);
            recenterMarkerToPosition(targetP);
            if (!isCameFromExploreMap()) {
                moveCameraToPosition(targetP);
            }
        } else if (locationManager.isGPSProviderEnabled()
            || locationManager.isNetworkProviderEnabled()) {
            locationManager.requestLocationUpdatesFromProvider(LocationManager.NETWORK_PROVIDER);
            locationManager.requestLocationUpdatesFromProvider(LocationManager.GPS_PROVIDER);
            setProgressBarVisibility(true);
        } else {
            locationPermissionsHelper.showLocationOffDialog(getActivity(),
                R.string.ask_to_turn_location_on_text);
        }
        presenter.onMapReady();
        registerUnregisterLocationListener(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();
        presenter.attachView(this);
        registerNetworkReceiver();
        if (isResumed() && ((MainActivity) getActivity()).activeFragment == ActiveFragment.NEARBY) {
            if (locationPermissionsHelper.checkLocationPermission(getActivity())) {
                locationPermissionGranted();
            } else {
                startMapWithoutPermission();
            }
        }
    }

    /**
     * Starts the map without GPS and without permission By default it points to 51.50550,-0.07520
     * coordinates, other than that it points to the last known location which can be get by the key
     * "LastLocation" from applicationKvStore
     */
    private void startMapWithoutPermission() {
        if (applicationKvStore.getString("LastLocation") != null) {
            final String[] locationLatLng
                = applicationKvStore.getString("LastLocation").split(",");
            lastKnownLocation
                = new LatLng(Double.parseDouble(locationLatLng[0]),
                Double.parseDouble(locationLatLng[1]), 1f);
        } else {
            lastKnownLocation = MapUtils.getDefaultLatLng();
        }
        if (binding.map != null && !isCameFromExploreMap()) {
            moveCameraToPosition(
                new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
        }
        presenter.onMapReady();
    }

    private void registerNetworkReceiver() {
        if (getActivity() != null) {
            getActivity().registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
        getCompositeDisposable().clear();
        presenter.detachView();
        registerUnregisterLocationListener(true);
        try {
            if (broadcastReceiver != null && getActivity() != null) {
                getContext().unregisterReceiver(broadcastReceiver);
            }

            if (locationManager != null && presenter != null) {
                locationManager.removeLocationListener(presenter);
                locationManager.unregisterLocationManager();
            }
        } catch (final Exception e) {
            Timber.e(e);
            //Broadcast receivers should always be unregistered inside catch, you never know if they were already registered or not
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchHandler.removeCallbacks(searchRunnable);
        presenter.removeNearbyPreferences(applicationKvStore);
    }

    private void initViews() {
        Timber.d("init views called");
        initBottomSheets();
        loadAnimations();
        setBottomSheetCallbacks();
        addActionToTitle();
        if (!Utils.isMonumentsEnabled(new Date())) {
            NearbyFilterState.setWlmSelected(false);
        }
    }

    /**
     * a) Creates bottom sheet behaviours from bottom sheets, sets initial states and visibility b)
     * Gets the touch event on the map to perform following actions: if fab is open then close fab.
     * if bottom sheet details are expanded then collapse bottom sheet details. if bottom sheet
     * details are collapsed then hide the bottom sheet details. if listBottomSheet is open then
     * hide the list bottom sheet.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initBottomSheets() {
        bottomSheetListBehavior = BottomSheetBehavior.from(binding.bottomSheetNearby.bottomSheet);
        bottomSheetDetailsBehavior = BottomSheetBehavior.from(binding.bottomSheetDetails.getRoot());
        bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        binding.bottomSheetDetails.getRoot().setVisibility(View.VISIBLE);
        bottomSheetListBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    /**
     * Determines the number of spans (columns) in the RecyclerView based on device orientation and
     * adapter item count.
     *
     * @return The number of spans to be used in the RecyclerView.
     */
    private int getSpanCount() {
        int orientation = getResources().getConfiguration().orientation;
        if (bottomSheetAdapter != null) {
            return (orientation == Configuration.ORIENTATION_PORTRAIT) ? 3
                : bottomSheetAdapter.getItemCount();
        } else {
            return (orientation == Configuration.ORIENTATION_PORTRAIT) ? 3 : 6;
        }
    }

    public void initNearbyFilter() {
        binding.nearbyFilterList.getRoot().setVisibility(View.GONE);
        hideBottomSheet();
        binding.nearbyFilter.searchViewLayout.searchView.setOnQueryTextFocusChangeListener(
            (v, hasFocus) -> {
                LayoutUtils.setLayoutHeightAlignedToWidth(1.25,
                    binding.nearbyFilterList.getRoot());
                if (hasFocus) {
                    binding.nearbyFilterList.getRoot().setVisibility(View.VISIBLE);
                    presenter.searchViewGainedFocus();
                } else {
                    binding.nearbyFilterList.getRoot().setVisibility(View.GONE);
                }
            });
        binding.nearbyFilterList.searchListView.setHasFixedSize(true);
        binding.nearbyFilterList.searchListView.addItemDecoration(
            new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.nearbyFilterList.searchListView.setLayoutManager(linearLayoutManager);
        nearbyFilterSearchRecyclerViewAdapter = new NearbyFilterSearchRecyclerViewAdapter(
            getContext(), new ArrayList<>(Label.valuesAsList()),
            binding.nearbyFilterList.searchListView);
        nearbyFilterSearchRecyclerViewAdapter.setCallback(
            new NearbyFilterSearchRecyclerViewAdapter.Callback() {
                @Override
                public void setCheckboxUnknown() {
                    presenter.setCheckboxUnknown();
                }

                @Override
                public void filterByMarkerType(final ArrayList<Label> selectedLabels, final int i,
                    final boolean b, final boolean b1) {
                    presenter.filterByMarkerType(selectedLabels, i, b, b1);
                }

                @Override
                public boolean isDarkTheme() {
                    return isDarkTheme;
                }
            });
        binding.nearbyFilterList.getRoot()
            .getLayoutParams().width = (int) LayoutUtils.getScreenWidth(getActivity(),
            0.75);
        binding.nearbyFilterList.searchListView.setAdapter(nearbyFilterSearchRecyclerViewAdapter);
        LayoutUtils.setLayoutHeightAlignedToWidth(1.25, binding.nearbyFilterList.getRoot());
        getCompositeDisposable().add(
            RxSearchView.queryTextChanges(binding.nearbyFilter.searchViewLayout.searchView)
                .takeUntil(RxView.detaches(binding.nearbyFilter.searchViewLayout.searchView))
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(query -> {
                    ((NearbyFilterSearchRecyclerViewAdapter) binding.nearbyFilterList.searchListView.getAdapter()).getFilter()
                        .filter(query.toString());
                }));
    }

    @Override
    public void setCheckBoxAction() {
        binding.nearbyFilterList.checkboxTriStates.addAction();
        binding.nearbyFilterList.checkboxTriStates.setState(CheckBoxTriStates.UNKNOWN);
    }

    @Override
    public void setCheckBoxState(final int state) {
        binding.nearbyFilterList.checkboxTriStates.setState(state);
    }

    @Override
    public void setFilterState() {
        if (NearbyController.currentLocation != null) {
            presenter.filterByMarkerType(nearbyFilterSearchRecyclerViewAdapter.selectedLabels,
                binding.nearbyFilterList.checkboxTriStates.getState(), true, false);
        }
    }

    /**
     * Defines how bottom sheets will act on click
     */
    private void setBottomSheetCallbacks() {
        bottomSheetDetailsBehavior.setBottomSheetCallback(new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull final View bottomSheet, final int newState) {
                prepareViewsForSheetPosition(newState);
            }

            @Override
            public void onSlide(@NonNull final View bottomSheet, final float slideOffset) {

            }
        });

        binding.bottomSheetDetails.getRoot().setOnClickListener(v -> {
            if (bottomSheetDetailsBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else if (bottomSheetDetailsBehavior.getState()
                == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        binding.bottomSheetNearby.bottomSheet.getLayoutParams().height =
            getActivity().getWindowManager()
                .getDefaultDisplay().getHeight() / 16 * 9;
        bottomSheetListBehavior = BottomSheetBehavior.from(binding.bottomSheetNearby.bottomSheet);
        bottomSheetListBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetListBehavior.setBottomSheetCallback(new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull final View bottomSheet, final int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }

            @Override
            public void onSlide(@NonNull final View bottomSheet, final float slideOffset) {

            }
        });
    }

    /**
     * Loads animations will be used for FABs
     */
    private void loadAnimations() {
        fab_open = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward);
    }

    /**
     *
     */
    private void addActionToTitle() {
        binding.bottomSheetDetails.title.setOnLongClickListener(view -> {
            Utils.copy("place", binding.bottomSheetDetails.title.getText().toString(),
                getContext());
            Toast.makeText(getContext(), R.string.text_copy, Toast.LENGTH_SHORT).show();
            return true;
        });

        binding.bottomSheetDetails.title.setOnClickListener(view -> {
            bottomSheetListBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            if (bottomSheetDetailsBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    /**
     * Centers the map in nearby fragment to a given place and updates nearestPlace
     *
     * @param place is new center of the map
     */
    @Override
    public void centerMapToPlace(@Nullable final Place place) {
        Timber.d("Map is centered to place");
        final double cameraShift;
        if (null != place) {
            lastPlaceToCenter = place;
            nearestPlace = place;
        }

        if (null != lastPlaceToCenter) {
            final Configuration configuration = getActivity().getResources().getConfiguration();
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                cameraShift = CAMERA_TARGET_SHIFT_FACTOR_PORTRAIT;
            } else {
                cameraShift = CAMERA_TARGET_SHIFT_FACTOR_LANDSCAPE;
            }
            recenterMap(new LatLng(
                lastPlaceToCenter.location.getLatitude() - cameraShift,
                lastPlaceToCenter.getLocation().getLongitude(), 0));
        }
        highlightNearestPlace(place);
    }


    @Override
    public void updateListFragment(final List<Place> placeList) {
        adapter.clear();
        adapter.setItems(placeList);
        binding.bottomSheetNearby.noResultsMessage.setVisibility(
            placeList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public LatLng getLastLocation() {
        return lastKnownLocation;
    }

    @Override
    public LatLng getLastMapFocus() {
        LatLng latLng = new LatLng(
            lastMapFocus.getLatitude(), lastMapFocus.getLongitude(), 100);
        return latLng;
    }

    /**
     * Computes location where map should be centered
     *
     * @return returns the last location, if available, else returns default location
     */
    @Override
    public LatLng getMapCenter() {
        if (applicationKvStore.getString("LastLocation") != null) {
            final String[] locationLatLng
                = applicationKvStore.getString("LastLocation").split(",");
            lastKnownLocation
                = new LatLng(Double.parseDouble(locationLatLng[0]),
                Double.parseDouble(locationLatLng[1]), 1f);
        } else {
            lastKnownLocation = new LatLng(51.50550,
                -0.07520, 1f);
        }
        LatLng latLnge = lastKnownLocation;
        if (mapCenter != null) {
            latLnge = new LatLng(
                mapCenter.getLatitude(), mapCenter.getLongitude(), 100);
        }
        return latLnge;
    }

    @Override
    public LatLng getMapFocus() {
        LatLng mapFocusedLatLng = new LatLng(
            binding.map.getMapCenter().getLatitude(), binding.map.getMapCenter().getLongitude(),
            100);
        return mapFocusedLatLng;
    }

    @Override
    public boolean isAdvancedQueryFragmentVisible() {
        return isAdvancedQueryFragmentVisible;
    }

    @Override
    public void showHideAdvancedQueryFragment(final boolean shouldShow) {
        setHasOptionsMenu(!shouldShow);
        binding.flContainerNearbyChildren.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        isAdvancedQueryFragmentVisible = shouldShow;
    }

    @Override
    public boolean isNetworkConnectionEstablished() {
        return NetworkUtils.isInternetConnectionEstablished(getActivity());
    }

    /**
     * Adds network broadcast receiver to recognize connection established
     */
    private void initNetworkBroadCastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (getActivity() != null) {
                    if (NetworkUtils.isInternetConnectionEstablished(getActivity())) {
                        if (isNetworkErrorOccurred) {
                            presenter.updateMapAndList(LOCATION_SIGNIFICANTLY_CHANGED);
                            isNetworkErrorOccurred = false;
                        }

                        if (snackbar != null) {
                            snackbar.dismiss();
                            snackbar = null;
                        }
                    } else {
                        if (snackbar == null) {
                            snackbar = Snackbar.make(view, R.string.no_internet,
                                Snackbar.LENGTH_INDEFINITE);
                            searchable = false;
                            setProgressBarVisibility(false);
                        }

                        isNetworkErrorOccurred = true;
                        snackbar.show();
                    }
                }
            }
        };
    }

    /**
     * Updates the internet unavailable snackbar to reflect whether cached pins are shown.
     *
     * @param offlinePinsShown Whether there are pins currently being shown on map.
     */
    @Override
    public void updateSnackbar(final boolean offlinePinsShown) {
        if (!isNetworkErrorOccurred || snackbar == null) {
            return;
        }
        if (offlinePinsShown) {
            snackbar.setText(R.string.nearby_showing_pins_offline);
        } else {
            snackbar.setText(R.string.no_internet);
        }
    }

    /**
     * Hide or expand bottom sheet according to states of all sheets
     */
    @Override
    public void listOptionMenuItemClicked() {
        bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        if (bottomSheetListBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED
            || bottomSheetListBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetListBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else if (bottomSheetListBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetListBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    /**
     * Returns the location of the top right corner of the map view.
     *
     * @return a `LatLng` object denoting the location of the top right corner of the map.
     */
    @Override
    public LatLng getScreenTopRight() {
        final IGeoPoint screenTopRight = binding.map.getProjection()
            .fromPixels(binding.map.getWidth(), 0);
        return new LatLng(
            screenTopRight.getLatitude(), screenTopRight.getLongitude(), 0);
    }

    /**
     * Returns the location of the bottom left corner of the map view.
     *
     * @return a `LatLng` object denoting the location of the bottom left corner of the map.
     */
    @Override
    public LatLng getScreenBottomLeft() {
        final IGeoPoint screenBottomLeft = binding.map.getProjection()
            .fromPixels(0, binding.map.getHeight());
        return new LatLng(
            screenBottomLeft.getLatitude(), screenBottomLeft.getLongitude(), 0);
    }

    @Override
    public void populatePlaces(final LatLng currentLatLng) {
        // these two variables have historically been assigned values the opposite of what their
        // names imply, and quite some existing code depends on this fact
        LatLng screenTopRightLatLng = getScreenBottomLeft();
        LatLng screenBottomLeftLatLng = getScreenTopRight();

        // When the nearby fragment is opened immediately upon app launch, the {screenTopRightLatLng}
        // and {screenBottomLeftLatLng} variables return {LatLng(0.0,0.0)} as output.
        // To address this issue, A small delta value {delta = 0.02} is used to adjust the latitude
        // and longitude values for {ZOOM_LEVEL = 15f}.
        // This adjustment helps in calculating the east and west corner LatLng accurately.
        // Note: This only happens when the nearby fragment is opened immediately upon app launch,
        // otherwise {screenTopRightLatLng} and {screenBottomLeftLatLng} are used to determine
        // the east and west corner LatLng.
        if (screenTopRightLatLng.getLatitude() == 0.0 && screenTopRightLatLng.getLongitude() == 0.0
            && screenBottomLeftLatLng.getLatitude() == 0.0
            && screenBottomLeftLatLng.getLongitude() == 0.0) {
            final double delta = 0.009;
            final double westCornerLat = currentLatLng.getLatitude() - delta;
            final double westCornerLong = currentLatLng.getLongitude() - delta;
            final double eastCornerLat = currentLatLng.getLatitude() + delta;
            final double eastCornerLong = currentLatLng.getLongitude() + delta;
            screenTopRightLatLng = new LatLng(westCornerLat,
                westCornerLong, 0);
            screenBottomLeftLatLng = new LatLng(eastCornerLat,
                eastCornerLong, 0);
            if (currentLatLng.equals(
                getLastMapFocus())) { // Means we are checking around current location
                populatePlacesForCurrentLocation(getMapFocus(), screenTopRightLatLng,
                    screenBottomLeftLatLng, currentLatLng, null);
            } else {
                populatePlacesForAnotherLocation(getMapFocus(), screenTopRightLatLng,
                    screenBottomLeftLatLng, currentLatLng, null);
            }
        } else {
            if (currentLatLng.equals(
                getLastMapFocus())) { // Means we are checking around current location
                populatePlacesForCurrentLocation(getMapFocus(), screenTopRightLatLng,
                    screenBottomLeftLatLng, currentLatLng, null);
            } else {
                populatePlacesForAnotherLocation(getMapFocus(), screenTopRightLatLng,
                    screenBottomLeftLatLng, currentLatLng, null);
            }
        }

        if (recenterToUserLocation) {
            recenterToUserLocation = false;
        }
    }

    @Override
    public void populatePlaces(final LatLng currentLatLng,
        @Nullable final String customQuery) {
        if (customQuery == null || customQuery.isEmpty()) {
            populatePlaces(currentLatLng);
            return;
        }
        // these two variables have historically been assigned values the opposite of what their
        // names imply, and quite some existing code depends on this fact
        final LatLng screenTopRightLatLng = getScreenBottomLeft();
        final LatLng screenBottomLeftLatLng = getScreenTopRight();

        if (currentLatLng.equals(lastFocusLocation) || lastFocusLocation == null
            || recenterToUserLocation) { // Means we are checking around current location
            populatePlacesForCurrentLocation(lastKnownLocation, screenTopRightLatLng,
                screenBottomLeftLatLng, currentLatLng, customQuery);
        } else {
            populatePlacesForAnotherLocation(lastKnownLocation, screenTopRightLatLng,
                screenBottomLeftLatLng, currentLatLng, customQuery);
        }
        if (recenterToUserLocation) {
            recenterToUserLocation = false;
        }
    }

    /**
     * Clears the Nearby local cache and then calls for pin details to be fetched afresh.
     */
    private void emptyCache() {
        // reload the map once the cache is cleared
        getCompositeDisposable().add(
            placesRepository.clearCache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .andThen(Completable.fromAction(() -> {
                    // reload only the pin details, by making all loaded pins gray:
                    ArrayList<MarkerPlaceGroup> newPlaceGroups = new ArrayList<>(
                        NearbyController.markerLabelList.size());
                    for (final MarkerPlaceGroup placeGroup : NearbyController.markerLabelList) {
                        final Place place = new Place("", "", placeGroup.getPlace().getLabel(), "",
                            placeGroup.getPlace().getLocation(), "",
                            placeGroup.getPlace().siteLinks, "", placeGroup.getPlace().exists,
                            placeGroup.getPlace().entityID);
                        place.setDistance(placeGroup.getPlace().distance);
                        place.setMonument(placeGroup.getPlace().isMonument());
                        newPlaceGroups.add(
                            new MarkerPlaceGroup(placeGroup.getIsBookmarked(), place));
                    }
                    presenter.loadPlacesDataAsync(newPlaceGroups, scope);
                }))
                .subscribe(
                    () -> {
                        Timber.d("Nearby Cache cleared successfully.");
                    },
                    throwable -> {
                        Timber.e(throwable, "Failed to clear the Nearby Cache");
                    }
                )
        );
    }

    private void savePlacesAsKML() {
        final Observable<String> savePlacesObservable = Observable
            .fromCallable(() -> nearbyController
                .getPlacesAsKML(getMapFocus()));
        getCompositeDisposable().add(savePlacesObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(kmlString -> {
                    if (kmlString != null) {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                            Locale.getDefault()).format(new Date());
                        String fileName =
                            "KML_" + timeStamp + "_" + System.currentTimeMillis() + ".kml";
                        boolean saved = saveFile(kmlString, fileName);
                        progressDialog.hide();
                        if (saved) {
                            showOpenFileDialog(getContext(), fileName, false);
                        } else {
                            Toast.makeText(this.getContext(),
                                getString(R.string.failed_to_save_kml_file),
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                throwable -> {
                    Timber.d(throwable);
                    showErrorMessage(getString(R.string.error_fetching_nearby_places)
                        + throwable.getLocalizedMessage());
                    setProgressBarVisibility(false);
                    presenter.lockUnlockNearby(false);
                    setFilterState();
                }));
    }

    private void savePlacesAsGPX() {
        final Observable<String> savePlacesObservable = Observable
            .fromCallable(() -> nearbyController
                .getPlacesAsGPX(getMapFocus()));
        getCompositeDisposable().add(savePlacesObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(gpxString -> {
                    if (gpxString != null) {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                            Locale.getDefault()).format(new Date());
                        String fileName =
                            "GPX_" + timeStamp + "_" + System.currentTimeMillis() + ".gpx";
                        boolean saved = saveFile(gpxString, fileName);
                        progressDialog.hide();
                        if (saved) {
                            showOpenFileDialog(getContext(), fileName, true);
                        } else {
                            Toast.makeText(this.getContext(),
                                getString(R.string.failed_to_save_gpx_file),
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                throwable -> {
                    Timber.d(throwable);
                    showErrorMessage(getString(R.string.error_fetching_nearby_places)
                        + throwable.getLocalizedMessage());
                    setProgressBarVisibility(false);
                    presenter.lockUnlockNearby(false);
                    setFilterState();
                }));
    }

    public static boolean saveFile(String string, String fileName) {

        if (!isExternalStorageWritable()) {
            return false;
        }

        File downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS);
        File kmlFile = new File(downloadsDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(kmlFile);
            fos.write(string.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showOpenFileDialog(Context context, String fileName, Boolean isGPX) {
        String title = getString(R.string.file_saved_successfully);
        String message =
            (isGPX) ? getString(R.string.do_you_want_to_open_gpx_file)
                : getString(R.string.do_you_want_to_open_kml_file);
        Runnable runnable = () -> openFile(context, fileName, isGPX);
        DialogUtil.showAlertDialog(getActivity(), title, message, runnable, () -> {
        });
    }

    private void openFile(Context context, String fileName, Boolean isGPX) {
        File file = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName);
        Uri uri = FileProvider.getUriForFile(context,
            context.getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (isGPX) {
            intent.setDataAndType(uri, "application/gpx");
        } else {
            intent.setDataAndType(uri, "application/kml");
        }

        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.no_application_available_to_open_gpx_files,
                Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Fetches and updates the data for a specific place, then updates the corresponding marker on
     * the map.
     *
     * @param entity       The entity ID of the place.
     * @param place        The Place object containing the initial place data.
     * @param marker       The Marker object on the map representing the place.
     * @param isBookMarked A boolean indicating if the place is bookmarked.
     */
    private void getPlaceData(String entity, Place place, Marker marker, Boolean isBookMarked) {
        final Observable<List<Place>> getPlaceObservable = Observable
            .fromCallable(() -> nearbyController
                .getPlaces(List.of(place)));
        getCompositeDisposable().add(getPlaceObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(placeList -> {
                    Place updatedPlace = placeList.get(0);
                    updatedPlace.distance = place.distance;
                    updatedPlace.location = place.location;
                    marker.setTitle(updatedPlace.name);
                    marker.setSnippet(
                        containsParentheses(updatedPlace.getLongDescription())
                            ? getTextBetweenParentheses(
                            updatedPlace.getLongDescription()) : updatedPlace.getLongDescription());
                    marker.showInfoWindow();
                    presenter.handlePinClicked(updatedPlace);
                    savePlaceToDatabase(place);
                    Drawable icon = ContextCompat.getDrawable(getContext(),
                        getIconFor(updatedPlace, isBookMarked));
                    marker.setIcon(icon);
                    binding.map.invalidate();
                    binding.bottomSheetDetails.dataCircularProgress.setVisibility(View.GONE);
                    binding.bottomSheetDetails.icon.setVisibility(View.VISIBLE);
                    binding.bottomSheetDetails.wikiDataLl.setVisibility(View.VISIBLE);
                    passInfoToSheet(updatedPlace);
                    hideBottomSheet();
                },
                throwable -> {
                    Timber.d(throwable);
                    showErrorMessage(getString(R.string.could_not_load_place_data)
                        + throwable.getLocalizedMessage());
                }));
    }

    private void populatePlacesForCurrentLocation(
        final LatLng currentLatLng,
        final LatLng screenTopRight,
        final LatLng screenBottomLeft,
        final LatLng searchLatLng,
        @Nullable final String customQuery) {
        final Observable<NearbyPlacesInfo> nearbyPlacesInfoObservable = Observable
            .fromCallable(() -> nearbyController
                .loadAttractionsFromLocation(currentLatLng, screenTopRight, screenBottomLeft,
                    searchLatLng,
                    false, true, Utils.isMonumentsEnabled(new Date()), customQuery));

        getCompositeDisposable().add(nearbyPlacesInfoObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(nearbyPlacesInfo -> {
                    if (nearbyPlacesInfo.placeList == null || nearbyPlacesInfo.placeList.isEmpty()) {
                        showErrorMessage(getString(R.string.no_nearby_places_around));
                        setProgressBarVisibility(false);
                        presenter.lockUnlockNearby(false);
                    } else {
                        updateMapMarkers(nearbyPlacesInfo.placeList, searchLatLng, true);
                        lastFocusLocation = searchLatLng;
                        lastMapFocus = new GeoPoint(searchLatLng.getLatitude(),
                            searchLatLng.getLongitude());
                    }
                },
                throwable -> {
                    Timber.d(throwable);
                    showErrorMessage(getString(R.string.error_fetching_nearby_places)
                        + throwable.getLocalizedMessage());
                    setProgressBarVisibility(false);
                    presenter.lockUnlockNearby(false);
                    setFilterState();
                }));
    }

    private void populatePlacesForAnotherLocation(
        final LatLng currentLatLng,
        final LatLng screenTopRight,
        final LatLng screenBottomLeft,
        final LatLng searchLatLng,
        @Nullable final String customQuery) {
        final Observable<NearbyPlacesInfo> nearbyPlacesInfoObservable = Observable
            .fromCallable(() -> nearbyController
                .loadAttractionsFromLocation(currentLatLng, screenTopRight, screenBottomLeft,
                    searchLatLng,
                    false, true, Utils.isMonumentsEnabled(new Date()), customQuery));

        getCompositeDisposable().add(nearbyPlacesInfoObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(nearbyPlacesInfo -> {
                    if (nearbyPlacesInfo.placeList == null || nearbyPlacesInfo.placeList.isEmpty()) {
                        showErrorMessage(getString(R.string.no_nearby_places_around));
                        setProgressBarVisibility(false);
                        presenter.lockUnlockNearby(false);
                    } else {
                        // Updating last searched location
                        applicationKvStore.putString("LastLocation",
                            searchLatLng.getLatitude() + "," + searchLatLng.getLongitude());

                        // curLatLng is used to calculate distance from the current location to the place
                        // and distance is later on populated to the place
                        updateMapMarkers(nearbyPlacesInfo.placeList, searchLatLng, false);
                        lastMapFocus = new GeoPoint(searchLatLng.getLatitude(),
                            searchLatLng.getLongitude());
                        stopQuery();
                    }
                },
                throwable -> {
                    Timber.e(throwable);
                    showErrorMessage(getString(R.string.error_fetching_nearby_places)
                        + throwable.getLocalizedMessage());
                    setProgressBarVisibility(false);
                    presenter.lockUnlockNearby(false);
                    setFilterState();
                }));
    }

    public void savePlaceToDatabase(Place place) {
        getCompositeDisposable().add(placesRepository
            .save(place)
            .subscribeOn(Schedulers.io())
            .subscribe());
    }

    /**
     * Stops any ongoing queries and clears all disposables. This method sets the stopQuery flag to
     * true and clears the compositeDisposable to prevent any further processing.
     */
    @Override
    public void stopQuery() {
        stopQuery = true;
        getCompositeDisposable().clear();
    }

    /**
     * Populates places for your location, should be used for finding nearby places around a
     * location where you are.
     *
     * @param nearbyPlaces This variable has place list information and distances.
     */
    private void updateMapMarkers(final List<Place> nearbyPlaces, final LatLng curLatLng,
        final boolean shouldUpdateSelectedMarker) {
        presenter.updateMapMarkers(nearbyPlaces, curLatLng, scope);
    }


    @Override
    public boolean isListBottomSheetExpanded() {
        return bottomSheetListBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    @Override
    public boolean isDetailsBottomSheetVisible() {
        return !(bottomSheetDetailsBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void setBottomSheetDetailsSmaller() {
        if (bottomSheetDetailsBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void setRecyclerViewAdapterAllSelected() {
        if (nearbyFilterSearchRecyclerViewAdapter != null
            && NearbyController.currentLocation != null) {
            nearbyFilterSearchRecyclerViewAdapter.setRecyclerViewAdapterAllSelected();
        }
    }

    @Override
    public void setRecyclerViewAdapterItemsGreyedOut() {
        if (nearbyFilterSearchRecyclerViewAdapter != null
            && NearbyController.currentLocation != null) {
            nearbyFilterSearchRecyclerViewAdapter.setRecyclerViewAdapterItemsGreyedOut();
        }
    }

    @Override
    public void setProgressBarVisibility(final boolean isVisible) {
        if (isVisible) {
            binding.mapProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.mapProgressBar.setVisibility(View.GONE);
        }
    }

    public void setTabItemContributions() {
        ((MainActivity) getActivity()).binding.pager.setCurrentItem(0);
        // TODO
    }

    /**
     * Starts animation of fab plus (turning on opening) and other FABs
     */
    @Override
    public void animateFABs() {
        if (binding.fabPlus.isShown()) {
            if (isFABsExpanded) {
                collapseFABs(isFABsExpanded);
            } else {
                expandFABs(isFABsExpanded);
            }
        }
    }

    private void showFABs() {
        NearbyFABUtils.addAnchorToBigFABs(binding.fabPlus,
            binding.bottomSheetDetails.getRoot().getId());
        binding.fabPlus.show();
        NearbyFABUtils.addAnchorToSmallFABs(binding.fabGallery,
            getView().findViewById(R.id.empty_view).getId());
        NearbyFABUtils.addAnchorToSmallFABs(binding.fabCamera,
            getView().findViewById(R.id.empty_view1).getId());
        NearbyFABUtils.addAnchorToSmallFABs(binding.fabCustomGallery,
            getView().findViewById(R.id.empty_view2).getId());
    }

    /**
     * Expands camera and gallery FABs, turn forward plus FAB
     *
     * @param isFABsExpanded true if they are already expanded
     */
    private void expandFABs(final boolean isFABsExpanded) {
        if (!isFABsExpanded) {
            showFABs();
            binding.fabPlus.startAnimation(rotate_forward);
            binding.fabCamera.startAnimation(fab_open);
            binding.fabGallery.startAnimation(fab_open);
            binding.fabCustomGallery.startAnimation(fab_open);
            binding.fabCustomGallery.show();
            binding.fabCamera.show();
            binding.fabGallery.show();
            this.isFABsExpanded = true;
        }
    }

    /**
     * Hides all fabs
     */
    private void hideFABs() {
        NearbyFABUtils.removeAnchorFromFAB(binding.fabPlus);
        binding.fabPlus.hide();
        NearbyFABUtils.removeAnchorFromFAB(binding.fabCamera);
        binding.fabCamera.hide();
        NearbyFABUtils.removeAnchorFromFAB(binding.fabGallery);
        binding.fabGallery.hide();
        NearbyFABUtils.removeAnchorFromFAB(binding.fabCustomGallery);
        binding.fabCustomGallery.hide();
    }

    /**
     * Collapses camera and gallery FABs, turn back plus FAB
     *
     * @param isFABsExpanded
     */
    private void collapseFABs(final boolean isFABsExpanded) {
        if (isFABsExpanded) {
            binding.fabPlus.startAnimation(rotate_backward);
            binding.fabCamera.startAnimation(fab_close);
            binding.fabGallery.startAnimation(fab_close);
            binding.fabCustomGallery.startAnimation(fab_close);
            binding.fabCustomGallery.hide();
            binding.fabCamera.hide();
            binding.fabGallery.hide();
            this.isFABsExpanded = false;
        }
    }

    @Override
    public void displayLoginSkippedWarning() {
        if (applicationKvStore.getBoolean("login_skipped", false)) {
            // prompt the user to login
            new Builder(getContext())
                .setMessage(R.string.login_alert_message)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                })
                .setPositiveButton(R.string.login, (dialog, which) -> {
                    // logout of the app
                    BaseLogoutListener logoutListener = new BaseLogoutListener(getActivity());
                    CommonsApplication app = (CommonsApplication) getActivity().getApplication();
                    app.clearApplicationData(getContext(), logoutListener);
                })
                .show();
        }
    }

    private void handleLocationUpdate(final LatLng latLng,
        final LocationChangeType locationChangeType) {
        lastKnownLocation = latLng;
        NearbyController.currentLocation = lastKnownLocation;
        presenter.updateMapAndList(locationChangeType);
    }

    @Override
    public void onLocationChangedSignificantly(final LatLng latLng) {
        Timber.d("Location significantly changed");
        if (latLng != null) {
            handleLocationUpdate(latLng, LOCATION_SIGNIFICANTLY_CHANGED);
        }
    }

    @Override
    public void onLocationChangedSlightly(final LatLng latLng) {
        Timber.d("Location slightly changed");
        if (latLng != null) {//If the map has never ever shown the current location, lets do it know
            handleLocationUpdate(latLng, LOCATION_SLIGHTLY_CHANGED);
        }
    }

    @Override
    public void onLocationChangedMedium(final LatLng latLng) {
        Timber.d("Location changed medium");
        if (latLng != null) {//If the map has never ever shown the current location, lets do it know
            handleLocationUpdate(latLng, LOCATION_SIGNIFICANTLY_CHANGED);
        }
    }

    public boolean backButtonClicked() {
        return presenter.backButtonClicked();
    }

    @Override
    public void onLocationPermissionDenied(String toastMessage) {
    }

    @Override
    public void onLocationPermissionGranted() {
    }

    /**
     * onLogoutComplete is called after shared preferences and data stored in local database are
     * cleared.
     */

    @Override
    public void setFABPlusAction(final OnClickListener onClickListener) {
        binding.fabPlus.setOnClickListener(onClickListener);
    }

    @Override
    public void setFABRecenterAction(final OnClickListener onClickListener) {
        binding.fabRecenter.setOnClickListener(onClickListener);
    }

    @Override
    public void disableFABRecenter() {
        binding.fabRecenter.setEnabled(false);
    }

    @Override
    public void enableFABRecenter() {
        binding.fabRecenter.setEnabled(true);
    }

    /**
     * Adds a marker for the user's current position. Adds a circle which uses the accuracy * 2, to
     * draw a circle which represents the user's position with an accuracy of 95%.
     * <p>
     * Should be called only on creation of Map, there is other method to update markers location
     * with users move.
     *
     * @param currentLatLng current location
     */
    @Override
    public void addCurrentLocationMarker(final LatLng currentLatLng) {
        if (null != currentLatLng && !isPermissionDenied
            && locationManager.isGPSProviderEnabled()) {
            ExecutorUtils.get().submit(() -> {
                Timber.d("Adds current location marker");
                recenterMarkerToPosition(
                    new GeoPoint(currentLatLng.getLatitude(), currentLatLng.getLongitude()));
            });
        } else {
            Timber.d("not adding current location marker..current location is null");
        }
    }

    @Override
    public void filterOutAllMarkers() {
        clearAllMarkers();
    }

    /**
     * Filters markers based on selectedLabels and chips
     *
     * @param selectedLabels       label list that user clicked
     * @param filterForPlaceState  true if we filter places for place state
     * @param filterForAllNoneType true if we filter places with all none button
     */
    @Override
    public void filterMarkersByLabels(final List<Label> selectedLabels,
        final boolean filterForPlaceState,
        final boolean filterForAllNoneType) {
        final boolean displayExists = false;
        final boolean displayNeedsPhoto = false;
        final boolean displayWlm = false;
        if (selectedLabels == null || selectedLabels.size() == 0) {
            replaceMarkerOverlays(NearbyController.markerLabelList);
            return;
        }
        final ArrayList<MarkerPlaceGroup> placeGroupsToShow = new ArrayList<>();
        for (final MarkerPlaceGroup markerPlaceGroup : NearbyController.markerLabelList) {
            final Place place = markerPlaceGroup.getPlace();
            // When label filter is engaged
            // then compare it against place's label
            if (selectedLabels != null && (selectedLabels.size() != 0 || !filterForPlaceState)
                && (!selectedLabels.contains(place.getLabel())
                && !(selectedLabels.contains(Label.BOOKMARKS)
                && markerPlaceGroup.getIsBookmarked()))) {
                continue;
            }

            if (!displayWlm && place.isMonument()) {
                continue;
            }

            boolean shouldUpdateMarker = false;

            if (displayWlm && place.isMonument()) {
                shouldUpdateMarker = true;
            } else if (displayExists && displayNeedsPhoto) {
                // Exists and needs photo
                if (place.exists && place.pic.trim().isEmpty()) {
                    shouldUpdateMarker = true;
                }
            } else if (displayExists && !displayNeedsPhoto) {
                // Exists and all included needs and doesn't needs photo
                if (place.exists) {
                    shouldUpdateMarker = true;
                }
            } else if (!displayExists && displayNeedsPhoto) {
                // All and only needs photo
                if (place.pic.trim().isEmpty()) {
                    shouldUpdateMarker = true;
                }
            } else if (!displayExists && !displayNeedsPhoto) {
                // all
                shouldUpdateMarker = true;
            }

            if (shouldUpdateMarker) {
                placeGroupsToShow.add(
                    new MarkerPlaceGroup(markerPlaceGroup.getIsBookmarked(), place)
                );
            }
        }
        replaceMarkerOverlays(placeGroupsToShow);
    }

    @Override
    public LatLng getCameraTarget() {
        return binding.map == null ? null : getMapFocus();
    }

    /**
     * Highlights nearest place when user clicks on home nearby banner
     *
     * @param nearestPlace nearest place, which has to be highlighted
     */
    private void highlightNearestPlace(final Place nearestPlace) {
        binding.bottomSheetDetails.icon.setVisibility(View.VISIBLE);
        passInfoToSheet(nearestPlace);
        hideBottomSheet();
        bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Returns drawable of marker icon for given place
     *
     * @param place        where marker is to be added
     * @param isBookmarked true if place is bookmarked
     * @return returns the drawable of marker according to the place information
     */
    private @DrawableRes int getIconFor(Place place, Boolean isBookmarked) {
        if (nearestPlace != null && place.name.equals(nearestPlace.name)) {
            // Highlight nearest place only when user clicks on the home nearby banner
//            highlightNearestPlace(place);
            return (isBookmarked ?
                R.drawable.ic_custom_map_marker_purple_bookmarked :
                R.drawable.ic_custom_map_marker_purple
            );
        }

        if (place.isMonument()) {
            return R.drawable.ic_custom_map_marker_monuments;
        }
        if (!place.pic.trim().isEmpty()) {
            return (isBookmarked ?
                R.drawable.ic_custom_map_marker_green_bookmarked :
                R.drawable.ic_custom_map_marker_green
            );
        }
        if (!place.exists) { // Means that the topic of the Wikidata item does not exist in the real world anymore, for instance it is a past event, or a place that was destroyed
            return (R.drawable.ic_clear_black_24dp);
        }
        if (place.name.isEmpty()) {
            return (isBookmarked ?
                R.drawable.ic_custom_map_marker_grey_bookmarked :
                R.drawable.ic_custom_map_marker_grey
            );
        }
        return (isBookmarked ?
            R.drawable.ic_custom_map_marker_red_bookmarked :
            R.drawable.ic_custom_map_marker_red
        );
    }

    public Marker convertToMarker(Place place, boolean isBookMarked) {
        Drawable icon = ContextCompat.getDrawable(getContext(), getIconFor(place, isBookMarked));
        GeoPoint point = new GeoPoint(place.location.getLatitude(), place.location.getLongitude());
        Marker marker = new Marker(binding.map);
        marker.setPosition(point);
        marker.setIcon(icon);
        if (!Objects.equals(place.name, "")) {
            marker.setTitle(place.name);
            marker.setSnippet(
                containsParentheses(place.getLongDescription())
                    ? getTextBetweenParentheses(
                    place.getLongDescription()) : place.getLongDescription());
        }
        marker.setTextLabelFontSize(40);
        // anchorV is 21.707/28.0 as icon height is 28dp while the pin base is at 21.707dp from top
        marker.setAnchor(Marker.ANCHOR_CENTER, 0.77525f);
        marker.setOnMarkerClickListener((marker1, mapView) -> {
            if (clickedMarker != null) {
                clickedMarker.closeInfoWindow();
            }
            clickedMarker = marker1;
            if (!isNetworkErrorOccurred) {
                binding.bottomSheetDetails.dataCircularProgress.setVisibility(View.VISIBLE);
                binding.bottomSheetDetails.icon.setVisibility(View.GONE);
                binding.bottomSheetDetails.wikiDataLl.setVisibility(View.GONE);
                if (Objects.equals(place.name, "")) {
                    getPlaceData(place.getWikiDataEntityId(), place, marker1, isBookMarked);
                } else {
                    marker.showInfoWindow();
                    binding.bottomSheetDetails.dataCircularProgress.setVisibility(View.GONE);
                    binding.bottomSheetDetails.icon.setVisibility(View.VISIBLE);
                    binding.bottomSheetDetails.wikiDataLl.setVisibility(View.VISIBLE);
                    passInfoToSheet(place);
                    hideBottomSheet();
                }
                bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                marker.showInfoWindow();
            }
            return true;
        });
        return marker;
    }

    /**
     * Adds multiple markers representing places to the map and handles item gestures.
     *
     * @param markerPlaceGroups The list of marker place groups containing the places and their
     *                          bookmarked status
     */
    @Override
    public void replaceMarkerOverlays(final List<MarkerPlaceGroup> markerPlaceGroups) {
        ArrayList<Marker> newMarkers = new ArrayList<>(markerPlaceGroups.size());
        // iterate in reverse so that the nearest pins get rendered on top
        for (int i = markerPlaceGroups.size() - 1; i >= 0; i--) {
            newMarkers.add(
                convertToMarker(markerPlaceGroups.get(i).getPlace(),
                    markerPlaceGroups.get(i).getIsBookmarked())
            );
        }
        clearAllMarkers();
        binding.map.getOverlays().addAll(newMarkers);
    }


    /**
     * Extracts text between the first occurrence of '(' and its corresponding ')' in the input
     * string.
     *
     * @param input The input string from which to extract text between parentheses.
     * @return The text between parentheses if found, or {@code null} if no parentheses are found.
     */
    public static String getTextBetweenParentheses(String input) {
        int startIndex = input.indexOf('(');
        int endIndex = input.indexOf(')', startIndex);
        if (startIndex != -1 && endIndex != -1) {
            return input.substring(startIndex + 1, endIndex);
        } else {
            return null;
        }
    }

    /**
     * Checks if the given text contains '(' or ')'.
     *
     * @param input The input text to check.
     * @return {@code true} if '(' or ')' is found, {@code false} otherwise.
     */
    public static boolean containsParentheses(String input) {
        return input.contains("(") || input.contains(")");
    }

    @Override
    public void recenterMap(LatLng currentLatLng) {
        // if user has denied permission twice, then show dialog
        if (isPermissionDenied) {
            if (locationPermissionsHelper.checkLocationPermission(getActivity())) {
                // this will run when user has given permission by opening app's settings
                isPermissionDenied = false;
                locationPermissionGranted();
                return;
            } else {
                askForLocationPermission();
            }
        } else {
            if (!locationPermissionsHelper.checkLocationPermission(getActivity())) {
                askForLocationPermission();
            } else {
                locationPermissionGranted();
            }
        }
        if (currentLatLng == null) {
            recenterToUserLocation = true;
            return;
        }

        /*
         * FIXME: With the revamp of the location permission helper in the MR
         *  #5494[1], there is a doubt that the following code is redundant.
         *  If we could confirm the same, the following code can be removed. If it
         *  turns out to be necessary, we could replace this with a comment
         *  clarifying why it is necessary.
         *
         * Ref: https://github.com/commons-app/apps-android-commons/pull/5494#discussion_r1560404794
         */
        if (lastMapFocus != null) {
            Location mylocation = new Location("");
            Location dest_location = new Location("");
            dest_location.setLatitude(binding.map.getMapCenter().getLatitude());
            dest_location.setLongitude(binding.map.getMapCenter().getLongitude());
            mylocation.setLatitude(lastMapFocus.getLatitude());
            mylocation.setLongitude(lastMapFocus.getLongitude());
            Float distance = mylocation.distanceTo(dest_location);//in meters
            if (lastMapFocus != null) {
                if (isNetworkConnectionEstablished()) {
                    if (distance > 2000.0) {
                        searchable = true;
                    } else {
                        searchable = false;
                    }
                }
            } else {
                searchable = false;
            }
        }
    }

    @Override
    public void openLocationSettings() {
        // This method opens the location settings of the device along with a followup toast.
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        final PackageManager packageManager = getActivity().getPackageManager();

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
            Toast.makeText(getContext(), R.string.recommend_high_accuracy_mode, Toast.LENGTH_LONG)
                .show();
        } else {
            Toast.makeText(getContext(), R.string.cannot_open_location_settings, Toast.LENGTH_LONG)
                .show();
        }
    }

    @Override
    public void hideBottomSheet() {
        bottomSheetListBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void hideBottomDetailsSheet() {
        bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    /**
     * If nearby details bottom sheet state is collapsed: show fab plus If nearby details bottom
     * sheet state is expanded: show fab plus If nearby details bottom sheet state is hidden: hide
     * all fabs
     *
     * @param bottomSheetState see bottom sheet states
     */
    public void prepareViewsForSheetPosition(final int bottomSheetState) {

        switch (bottomSheetState) {
            case (BottomSheetBehavior.STATE_COLLAPSED):
                collapseFABs(isFABsExpanded);
                if (!binding.fabPlus.isShown()) {
                    showFABs();
                }
                break;
            case (BottomSheetBehavior.STATE_HIDDEN):
                binding.transparentView.setClickable(false);
                binding.transparentView.setAlpha(0);
                collapseFABs(isFABsExpanded);
                hideFABs();
                break;
        }
    }

    /**
     * Same bottom sheet carries information for all nearby places, so we need to pass information
     * (title, description, distance and links) to view on nearby marker click
     *
     * @param place Place of clicked nearby marker
     */
    private void passInfoToSheet(final Place place) {
        selectedPlace = place;
        dataList = new ArrayList<>();
        // TODO: Decide button text for fitting in the screen
        dataList.add(new BottomSheetItem(R.drawable.ic_round_star_border_24px, ""));
        dataList.add(new BottomSheetItem(R.drawable.ic_directions_black_24dp,
            getResources().getString(R.string.nearby_directions)));
        if (place.hasWikidataLink()) {
            dataList.add(new BottomSheetItem(R.drawable.ic_wikidata_logo_24dp,
                getResources().getString(R.string.nearby_wikidata)));
        }
        dataList.add(new BottomSheetItem(R.drawable.ic_feedback_black_24dp,
            getResources().getString(R.string.nearby_wikitalk)));
        if (place.hasWikipediaLink()) {
            dataList.add(new BottomSheetItem(R.drawable.ic_wikipedia_logo_24dp,
                getResources().getString(R.string.nearby_wikipedia)));
        }
        if (selectedPlace.hasCommonsLink()) {
            dataList.add(new BottomSheetItem(R.drawable.ic_commons_icon_vector,
                getResources().getString(R.string.nearby_commons)));
        }
        int spanCount = getSpanCount();
        gridLayoutManager = new GridLayoutManager(this.getContext(), spanCount);
        binding.bottomSheetDetails.bottomSheetRecyclerView.setLayoutManager(gridLayoutManager);
        bottomSheetAdapter = new BottomSheetAdapter(this.getContext(), dataList);
        bottomSheetAdapter.setClickListener(this);
        binding.bottomSheetDetails.bottomSheetRecyclerView.setAdapter(bottomSheetAdapter);
        updateBookmarkButtonImage(selectedPlace);
        binding.bottomSheetDetails.icon.setImageResource(selectedPlace.getLabel().getIcon());
        binding.bottomSheetDetails.title.setText(selectedPlace.name);
        binding.bottomSheetDetails.category.setText(selectedPlace.distance);
        // Remove label since it is double information
        String descriptionText = selectedPlace.getLongDescription()
            .replace(selectedPlace.getName() + " (", "");
        descriptionText = (descriptionText.equals(selectedPlace.getLongDescription())
            ? descriptionText : descriptionText.replaceFirst(".$", ""));
        // Set the short description after we remove place name from long description
        binding.bottomSheetDetails.description.setText(descriptionText);

        binding.fabCamera.setOnClickListener(view -> {
            if (binding.fabCamera.isShown()) {
                Timber.d("Camera button tapped. Place: %s", selectedPlace.toString());
                storeSharedPrefs(selectedPlace);
                controller.initiateCameraPick(getActivity(), inAppCameraLocationPermissionLauncher,
                    cameraPickLauncherForResult);
            }
        });

        binding.fabGallery.setOnClickListener(view -> {
            if (binding.fabGallery.isShown()) {
                Timber.d("Gallery button tapped. Place: %s", selectedPlace.toString());
                storeSharedPrefs(selectedPlace);
                controller.initiateGalleryPick(getActivity(),
                    galleryPickLauncherForResult,
                    false);
            }
        });

        binding.fabCustomGallery.setOnClickListener(view -> {
            if (binding.fabCustomGallery.isShown()) {
                Timber.d("Gallery button tapped. Place: %s", selectedPlace.toString());
                storeSharedPrefs(selectedPlace);
                controller.initiateCustomGalleryPickWithPermission(getActivity(),
                    customSelectorLauncherForResult);
            }
        });
    }

    private void storeSharedPrefs(final Place selectedPlace) {
        applicationKvStore.putJson(PLACE_OBJECT, selectedPlace);
        Place place = applicationKvStore.getJson(PLACE_OBJECT, Place.class);

        Timber.d("Stored place object %s", place.toString());
    }

    private void updateBookmarkButtonImage(final Place place) {
        final int bookmarkIcon;
        if (bookmarkLocationDao.findBookmarkLocation(place)) {
            bookmarkIcon = R.drawable.ic_round_star_filled_24px;
        } else {
            bookmarkIcon = R.drawable.ic_round_star_border_24px;
        }
        bottomSheetAdapter.updateBookmarkIcon(bookmarkIcon);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        wikidataEditListener.setAuthenticationStateListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wikidataEditListener.setAuthenticationStateListener(null);
    }

    @Override
    public void onWikidataEditSuccessful() {
        if (presenter != null && locationManager != null) {
            presenter.updateMapAndList(MAP_UPDATED);
        }
    }

    private void showErrorMessage(final String message) {
        Timber.e(message);
        ViewUtil.showLongToast(getActivity(), message);
    }

    public void registerUnregisterLocationListener(final boolean removeLocationListener) {
        try {
            if (removeLocationListener) {
                locationManager.unregisterLocationManager();
                locationManager.removeLocationListener(this);
                Timber.d("Location service manager unregistered and removed");
            } else {
                locationManager.addLocationListener(this);
                locationManager.registerLocationManager();
                Timber.d("Location service manager added and registered");
            }
        } catch (final Exception e) {
            Timber.e(e);
            //Broadcasts are tricky, should be catchedonR
        }
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        if (isResumed() && isVisibleToUser) {
            performMapReadyActions();
        } else {
            if (null != bottomSheetListBehavior) {
                bottomSheetListBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }

            if (null != bottomSheetDetailsBehavior) {
                bottomSheetDetailsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }
    }

    /**
     * Clears all markers from the map and resets certain map overlays and gestures. After clearing
     * markers, it re-adds a scale bar overlay and rotation gesture overlay to the map.
     */
    @Override
    public void clearAllMarkers() {
        binding.map.getOverlayManager().clear();
        binding.map.invalidate();
        GeoPoint geoPoint = mapCenter;
        if (geoPoint != null) {
            ScaleDiskOverlay diskOverlay =
                new ScaleDiskOverlay(this.getContext(),
                    geoPoint, 2000, UnitOfMeasure.foot);
            Paint circlePaint = new Paint();
            circlePaint.setColor(Color.rgb(128, 128, 128));
            circlePaint.setStyle(Style.STROKE);
            circlePaint.setStrokeWidth(2f);
            diskOverlay.setCirclePaint2(circlePaint);
            Paint diskPaint = new Paint();
            diskPaint.setColor(Color.argb(40, 128, 128, 128));
            diskPaint.setStyle(Style.FILL_AND_STROKE);
            diskOverlay.setCirclePaint1(diskPaint);
            diskOverlay.setDisplaySizeMin(900);
            diskOverlay.setDisplaySizeMax(1700);
            binding.map.getOverlays().add(diskOverlay);
            Marker startMarker = new Marker(
                binding.map);
            startMarker.setPosition(geoPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM);
            startMarker.setIcon(
                ContextCompat.getDrawable(this.getContext(), R.drawable.current_location_marker));
            startMarker.setTitle("Your Location");
            startMarker.setTextLabelFontSize(24);
            binding.map.getOverlays().add(startMarker);
        }
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(binding.map);
        scaleBarOverlay.setScaleBarOffset(15, 25);
        Paint barPaint = new Paint();
        barPaint.setARGB(200, 255, 250, 250);
        scaleBarOverlay.setBackgroundPaint(barPaint);
        scaleBarOverlay.enableScaleBar();
        binding.map.getOverlays().add(scaleBarOverlay);
        binding.map.getOverlays().add(mapEventsOverlay);
        binding.map.setMultiTouchControls(true);
    }

    /**
     * Recenters the map to the Center and adds a scale disk overlay and a marker at the position.
     *
     * @param geoPoint The GeoPoint representing the new center position of the map.
     */
    private void recenterMarkerToPosition(GeoPoint geoPoint) {
        if (geoPoint != null) {
            binding.map.getController().setCenter(geoPoint);
            List<Overlay> overlays = binding.map.getOverlays();
            for (int i = 0; i < overlays.size(); i++) {
                if (overlays.get(i) instanceof Marker) {
                    binding.map.getOverlays().remove(i);
                } else if (overlays.get(i) instanceof ScaleDiskOverlay) {
                    binding.map.getOverlays().remove(i);
                }
            }
            ScaleDiskOverlay diskOverlay =
                new ScaleDiskOverlay(this.getContext(),
                    geoPoint, 2000, UnitOfMeasure.foot);
            Paint circlePaint = new Paint();
            circlePaint.setColor(Color.rgb(128, 128, 128));
            circlePaint.setStyle(Style.STROKE);
            circlePaint.setStrokeWidth(2f);
            diskOverlay.setCirclePaint2(circlePaint);
            Paint diskPaint = new Paint();
            diskPaint.setColor(Color.argb(40, 128, 128, 128));
            diskPaint.setStyle(Style.FILL_AND_STROKE);
            diskOverlay.setCirclePaint1(diskPaint);
            diskOverlay.setDisplaySizeMin(900);
            diskOverlay.setDisplaySizeMax(1700);
            binding.map.getOverlays().add(diskOverlay);
            Marker startMarker = new Marker(
                binding.map);
            startMarker.setPosition(geoPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM);
            startMarker.setIcon(
                ContextCompat.getDrawable(this.getContext(), R.drawable.current_location_marker));
            startMarker.setTitle("Your Location");
            startMarker.setTextLabelFontSize(24);
            binding.map.getOverlays().add(startMarker);
        }
    }

    private void moveCameraToPosition(GeoPoint geoPoint) {
        binding.map.getController().animateTo(geoPoint);
    }

    /**
     * Moves the camera of the map view to the specified GeoPoint at specified zoom level and speed
     * using an animation.
     *
     * @param geoPoint The GeoPoint representing the new camera position for the map.
     * @param zoom     Zoom level of the map camera
     * @param speed    Speed of animation
     */
    private void moveCameraToPosition(GeoPoint geoPoint, double zoom, long speed) {
        binding.map.getController().animateTo(geoPoint, zoom, speed);
    }

    @Override
    public void onBottomSheetItemClick(@Nullable View view, int position) {
        BottomSheetItem item = dataList.get(position);
        switch (item.getImageResourceId()) {
            case R.drawable.ic_round_star_border_24px:
                presenter.toggleBookmarkedStatus(selectedPlace);
                updateBookmarkButtonImage(selectedPlace);
                break;
            case R.drawable.ic_round_star_filled_24px:
                presenter.toggleBookmarkedStatus(selectedPlace);
                updateBookmarkButtonImage(selectedPlace);
                break;
            case R.drawable.ic_directions_black_24dp:
                Utils.handleGeoCoordinates(this.getContext(), selectedPlace.getLocation(),
                    binding.map.getZoomLevelDouble());
                break;
            case R.drawable.ic_wikidata_logo_24dp:
                Utils.handleWebUrl(this.getContext(), selectedPlace.siteLinks.getWikidataLink());
                break;
            case R.drawable.ic_feedback_black_24dp:
                Intent intent = new Intent(this.getContext(), WikidataFeedback.class);
                intent.putExtra("lat", selectedPlace.location.getLatitude());
                intent.putExtra("lng", selectedPlace.location.getLongitude());
                intent.putExtra("place", selectedPlace.name);
                intent.putExtra("qid", selectedPlace.getWikiDataEntityId());
                startActivity(intent);
                break;
            case R.drawable.ic_wikipedia_logo_24dp:
                Utils.handleWebUrl(this.getContext(), selectedPlace.siteLinks.getWikipediaLink());
                break;
            case R.drawable.ic_commons_icon_vector:
                Utils.handleWebUrl(this.getContext(), selectedPlace.siteLinks.getCommonsLink());
                break;
            default:
                break;
        }
    }

    @Override
    public void onBottomSheetItemLongClick(@Nullable View view, int position) {
        BottomSheetItem item = dataList.get(position);
        String message;
        switch (item.getImageResourceId()) {
            case R.drawable.ic_round_star_border_24px:
                message = getString(R.string.menu_bookmark);
                break;
            case R.drawable.ic_round_star_filled_24px:
                message = getString(R.string.menu_bookmark);
                break;
            case R.drawable.ic_directions_black_24dp:
                message = getString(R.string.nearby_directions);
                break;
            case R.drawable.ic_wikidata_logo_24dp:
                message = getString(R.string.nearby_wikidata);
                break;
            case R.drawable.ic_feedback_black_24dp:
                message = getString(R.string.nearby_wikitalk);
                break;
            case R.drawable.ic_wikipedia_logo_24dp:
                message = getString(R.string.nearby_wikipedia);
                break;
            case R.drawable.ic_commons_icon_vector:
                message = getString(R.string.nearby_commons);
                break;
            default:
                message = "Long click";
                break;
        }
        Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public interface NearbyParentFragmentInstanceReadyCallback {

        void onReady();
    }

    public void setNearbyParentFragmentInstanceReadyCallback(
        NearbyParentFragmentInstanceReadyCallback nearbyParentFragmentInstanceReadyCallback) {
        this.nearbyParentFragmentInstanceReadyCallback = nearbyParentFragmentInstanceReadyCallback;
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutParams rlBottomSheetLayoutParams = binding.bottomSheetNearby.bottomSheet.getLayoutParams();
        rlBottomSheetLayoutParams.height =
            getActivity().getWindowManager().getDefaultDisplay().getHeight() / 16 * 9;
        binding.bottomSheetNearby.bottomSheet.setLayoutParams(rlBottomSheetLayoutParams);
        int spanCount = getSpanCount();
        if (gridLayoutManager != null) {
            gridLayoutManager.setSpanCount(spanCount);
        }
    }

    public void onLearnMoreClicked() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(WLM_URL));
        startActivity(intent);
    }
}
