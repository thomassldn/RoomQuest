/* Copyright 2014 ESRI
*
* All rights reserved under the copyright laws of the United States
* and applicable international laws, treaties, and conventions.
*
* You may freely redistribute and use this sample code, with or
* without modification, provided you include the original copyright
* notice and use restrictions.
*
* See the sample code usage restrictions document for further information.
*
*/

package roomquest.cse.csusb.edu.roomquest;
/**
 *
 * Modified By: Jose Banuelos, Thomas Saldana, Christopher Koenig
 * Date: 6 Feb 17
 *
 */
import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.android.toolkit.map.MapViewHelper;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorSuggestionParameters;
import com.esri.core.tasks.geocode.LocatorSuggestionResult;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;
import java.util.ArrayList;


import roomquest.cse.csusb.edu.roomquest.Compass.Compass.Compass;

/**
 * PlaceSearch app uses the geocoding service to convert addresses to and from
 * geographic coordinates and place them on the map.  Search for places, addresses,
 * etc. and get suggestions as you type.
 *
 */

//public class MainActivity extends Activity {
public class MainActivity extends AppCompatActivity implements Grid.Communicator{


    //Declaring the floating action button variables
    //They correspond to the floor buttons
    FloatingActionButton plus, base, floor1, floor2, floor3, floor4, floor5;

    Animation FabOpen, FabClose, FabRotateClockwise, FabRotateCounter;
    boolean isOpen;

    //this variable will be used to keep track of what floor the user is on
    public int whichFloor;

    private static final String TAG = "PlaceSearch";
    private static final String COLUMN_NAME_ADDRESS = "address";
    private static final String COLUMN_NAME_X = "x";
    private static final String COLUMN_NAME_Y = "y";
    private static final String FIND_PLACE = "Find";
    private static final String SUGGEST_PLACE = "Suggest";
    private static boolean suggestClickFlag = false;
    private static boolean searchClickFlag = false;
    private static boolean searchMiss = true;

    //This layer will be the uppermost layer at all times, it will contain the CSUSB pawprint graphic
    GraphicsLayer pawGraphicsLayer;

    //This layer will contain the lines, polygons, and the labels
    ArcGISDynamicMapServiceLayer dynamicMapServiceLayer;

    // array of ints for the layer ids {polygon layer, line layer, label layer}
    //these numbers represent each layer id from ArcGIS MapServer

    int[] layerId5 = {45,34,33,1,12};         //fifth floor layer
    int[] layerId4 = {46,36,35,3,12};         //fourth floor layer
    int[] layerId3 = {47,38,37,5,12};         //third floor layer
    int[] layerId2 = {48,40,39,7,12};         //second floor layer
    int[] layerId  = {49,42,41,9,12};          //first floor layer
    int[] layerId0 = {50,44,43,11,12};        //basement layer



    //Grid Layers
    int[] bikeRacks               = {49,42,41,9,14,12};
    int[] parkingDispensers       = {49,42,41,9,15,12};
    int[] disabilityParkingAreas  = {49,42,41,9,16,12};
    int[] informationCenters      = {49,42,41,9,17,12};
    int[] palmDesertShuttle       = {49,42,41,9,18,12};
    int[] emergencyPhones         = {49,42,41,9,19,12};
    int[] restRooms               = {49,42,41,9,20,12};
    int[] evchargingstations      = {49,42,41,9,21,12};
    int[] healthCenter            = {49,42,41,9,22,12};
    int[] atm                     = {49,42,41,9,23,12};
    int[] campusEvacuationSites   = {49,42,41,9,24,12};
    int[] dining                  = {49,42,41,9,26,12};

    public LocationManager manager;
    public static Point mLocation = null;
    LocationDisplayManager ldm;

    // Spatial references used for projecting points
    final SpatialReference wm = SpatialReference.create(102100);
    final SpatialReference egs = SpatialReference.create(4326);

    //Request GPS
    //Graphics are objects held in memory that store a shape (geometry)
    // and are displayed on a map via a GraphicsLayer.
    GraphicsLayer mGraphicsLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.DYNAMIC);
    private int requestCode = 2;
    String[] reqPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private String mMapViewState;
    private MapViewHelper mMapViewHelper;

    //These variables will be used for the search suggestions to be displayed
    private SearchView mSearchView;
    private MenuItem searchMenuItem;
    private MatrixCursor mSuggestionCursor;

    private static ProgressDialog mProgressDialog;
    private LocatorSuggestionParameters suggestParams;
    private LocatorFindParameters findParams;

    private SpatialReference mapSpatialReference;
    private static ArrayList<LocatorSuggestionResult> suggestionsList;

    MapView mMapView;
    Locator mLocator = null;

    //***********END ADDED CODE ***************

    //Compass - Initialization of compass object
    Compass mCompass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup and show progress dialog
        mProgressDialog = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                // Back key pressed - just dismiss the dialog
                mProgressDialog.dismiss();
            }
        };

        // After the content of this activity is set the map can be accessed from the layout
        mMapView = (MapView) findViewById(R.id.map);
        mMapViewHelper = new MapViewHelper(mMapView);

        //The following allows us to license the app, removes esri logo
        ArcGISRuntime.setClientId("nVzh9JCohNTXQOfu");

        //Adding the DynamicMapServiceLayer on the mapView
        //Initializing the first floor layer on startup
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId);
        mMapView.addLayer(dynamicMapServiceLayer);

        //Placing an empty GraphicsLayer at the top
        pawGraphicsLayer = new GraphicsLayer();
        mMapView.addLayer(pawGraphicsLayer);


        //Initializing the custom RoomQuest Locator
        String url1 = getResources().getString(R.string.CSUSBLocator);
        mLocator = Locator.createOnlineLocator(url1);

        // Setup listener for map initialized
        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (source == mMapView && status == OnStatusChangedListener.STATUS.INITIALIZED) {
                    mapSpatialReference = mMapView.getSpatialReference();
                    if (mMapViewState == null) {
                        Log.i(TAG, "MapView.setOnStatusChangedListener() status=" + status.toString());
                    } else {
                        mMapView.restoreState(mMapViewState);
                    }
                }
            }
        });



        //Compass Code starts here

        // Set the MapView to allow the user to rotate the map when as part of a pinch gesture.
        mMapView.setAllowRotationByPinch(false);//set this to true if you want the map
                                                //on the app to rotate
        // Enabled wrap around map.
        mMapView.enableWrapAround(false);//change this to true if above line of code
                                         //is set to true

        // Create the Compass custom view, and add it onto the MapView.
        mCompass = new Compass(this, null, mMapView);
        mMapView.addView(mCompass);

        // Set a single tap listener on the MapView.
        mMapView.setOnSingleTapListener(new OnSingleTapListener() {

            public void onSingleTap(float x, float y) {

                // When a single tap gesture is received, reset the map to its default rotation angle,
                // where North is shown at the top of the device.
                mMapView.setRotationAngle(0);

                // Also reset the compass angle.
                mCompass.setRotationAngle(0);
            }
        });

        //Compas code ends here


        // Get the location display manager and start reading location. Don't auto-pan
        // to center our position
        ldm = mMapView.getLocationDisplayManager();
        ldm.setLocationListener(new MyLocationListener());
        ldm.start();
        ldm.setAutoPanMode(LocationDisplayManager.AutoPanMode.OFF);

        //Initializing the Floating Action Buttons to respond to the user clicks
        plus = (FloatingActionButton)findViewById(R.id.plus);

        //Clicking on any Floor Button will clear the screen of any graphics
        base = (FloatingActionButton)findViewById(R.id.fab_basement);
        base.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Removes any Graphics on the Graphics Layer if there are any
                checkAndRemoveGraphics();
                if ((mSearchView != null) && (!mSearchView.isIconified())) {
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                        invalidateOptionsMenu();
                    }
                }
                //Display the corresponding floor layer
                setBasementFloors();
            }
        });

        floor1 = (FloatingActionButton)findViewById(R.id.fab_first);
        floor1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkAndRemoveGraphics();
                if ((mSearchView != null) && (!mSearchView.isIconified())) {
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                        invalidateOptionsMenu();
                    }
                }
                setFirstFloors();
            }
        });

        floor2 = (FloatingActionButton)findViewById(R.id.fab_second);
        floor2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkAndRemoveGraphics();
                if ((mSearchView != null) && (!mSearchView.isIconified())) {
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                        invalidateOptionsMenu();
                    }
                }
                setSecondFloors();
            }
        });

        floor3 = (FloatingActionButton)findViewById(R.id.fab_third);
        floor3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkAndRemoveGraphics();
                if ((mSearchView != null) && (!mSearchView.isIconified())) {
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                        invalidateOptionsMenu();
                    }
                }
                setThirdFloors();
            }
        });

        floor4 = (FloatingActionButton)findViewById(R.id.fab_fourth);
        floor4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkAndRemoveGraphics();
                if ((mSearchView != null) && (!mSearchView.isIconified())) {
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                        invalidateOptionsMenu();
                    }
                }
                setFourthFloors();
            }
        });

        floor5 = (FloatingActionButton)findViewById(R.id.fab_five);
        floor5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkAndRemoveGraphics();
                if ((mSearchView != null) && (!mSearchView.isIconified())) {
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                        invalidateOptionsMenu();
                    }
                }
                setFifthFloors();
            }
        });

        //Set the current floor level at floor 1
        whichFloor = 1;
        //Will change the background color of the button of the floor the user is currently on
        //based on what the user clicks or searches
        changeButtonBackgroundColor(whichFloor);


        //Now adding the animation to the fab buttons
        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        FabRotateClockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        FabRotateCounter = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_counter_clockwise);

        //fab on click listener
        plus.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(isOpen) {
                    showButtons(isOpen);
                }
                else {
                    showButtons(isOpen);
                }
            }
        });


        //Initializing Map with Campus Extend
        Envelope myExtents = new Envelope(-13061415.588940706,4052665.227615348,-13059240.709590949,4054092.392969663);//(xmin,ymin,xmax,ymax)
        myExtents = (Envelope) GeometryEngine.project(myExtents, SpatialReference.create(102100), mMapView.getSpatialReference());
        mMapView.setMaxExtent(myExtents);
        mMapView.setMaxScale(350);
        mMapView.setMinScale(25000);


        // GPS Request
        ActivityCompat.requestPermissions(MainActivity.this, reqPermissions, requestCode);


    } //end onCreate

    //Removes any graphics on the GraphicsLayer without removing the basemap
    public void checkAndRemoveGraphics(){
        //Check if there is already a graphic on the GraphicsLayer
        if(pawGraphicsLayer.getNumberOfGraphics() > 0){
            //If there are currently graphics then retrieve an array of graphic IDs
            int[] tempGraphicIDs = pawGraphicsLayer.getGraphicIDs();
            //Now delete that array of IDs to remove all the graphics
            pawGraphicsLayer.removeGraphics(tempGraphicIDs);
        }
    }

    //GPS
    //Permision for GPS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Location permission was granted. This would have been triggered in response to failing to start the
            // LocationDisplay, so try starting this again.
            //initialize Routing
            manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //if users gps is not enabled, let them know
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            }

            //displayLocation Code
            //initialize Routing
            mGraphicsLayer = new GraphicsLayer();
            manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //if users gps is not enabled, let them know
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            }

            // Get the location display manager and start reading location. Don'
            //auto-pan
            // to center our position
            ldm = mMapView.getLocationDisplayManager();
            ldm.setLocationListener(new MyLocationListener());
            ldm.start();
            ldm.setAutoPanMode(LocationDisplayManager.AutoPanMode.OFF);

        } else {
            // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
            // request permission UX will be shown again, option should be shown to allow never showing the UX again.
            // Alternative would be to disable functionality so request is not shown again.
            Toast.makeText(MainActivity.this, getResources().getString(R.string.location_permission_denied), Toast
                    .LENGTH_SHORT).show();
        }
    }

    /**
     * If GPS is disabled, app won't be able to route. Hence display a dialoge window to enable the GPS
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please enable your GPS before proceeding")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private class MyLocationListener implements LocationListener {

        public MyLocationListener() {
            super();
        }

        /**
         * If location changes, update our current location. If being found for
         * the first time, zoom to our current position with a resolution of 20
         */
        public void onLocationChanged(Location loc) {
            if (loc == null)
                return;
            boolean zoomToMe = (mLocation == null);
            mLocation = new Point(loc.getLongitude(), loc.getLatitude());
            //if (zoomToMe) { //this sets initial location at start
            //Point p = (Point) GeometryEngine.project(mLocation, egs, wm);
            //mMapView.zoomToResolution(p, 20.0);
            //}
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Disabled",
                    Toast.LENGTH_SHORT).show();
            buildAlertMessageNoGps();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Enabled",
                    Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
    //END GPS


    //This method handles the fab plus click
    public void showButtons(boolean bool){
        FloatingActionButton[] floorArray = {base, floor1, floor2, floor3, floor4, floor5};
        if(bool){
            for(FloatingActionButton button : floorArray){
                button.startAnimation(FabClose);
            }
            plus.startAnimation(FabRotateCounter);
            for(FloatingActionButton button : floorArray){
                button.setClickable(false);
            }
            isOpen = false;
        }
        else{
            for(FloatingActionButton button : floorArray){
                button.startAnimation(FabOpen);
            }
            plus.startAnimation(FabRotateClockwise);
            for(FloatingActionButton button : floorArray){
                button.setClickable(true);
            }
            isOpen = true;
        }
    }



    public void onDialogMessage(int boxNum) { //box number of the grid

        switch(boxNum){
            case 1:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),bikeRacks);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 2:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),parkingDispensers);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 3:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),disabilityParkingAreas);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 4:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),informationCenters);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 5:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),palmDesertShuttle);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 6:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),emergencyPhones);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 7:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),restRooms);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 8:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),evchargingstations);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 9:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),healthCenter);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 10:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),atm);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 11:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),campusEvacuationSites);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            case 12:
                mMapView.removeLayer(dynamicMapServiceLayer);
                dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),dining);
                mMapView.addLayer(dynamicMapServiceLayer);
                break;
            default:
                //by default set it to the first floor
                resetButtonBackgroundColor();
                break;
        }
    }//END onDIalog

    //Sets the basement layer
    public void setBasementFloors(){
        //Removes any graphics first
        checkAndRemoveGraphics();
        //Checks if the floor being loaded is the same as the current floor
        //If they are the same then do nothing
        if(whichFloor == 0){
            return;}

        //Remove the current layers first
        mMapView.removeLayer(pawGraphicsLayer);
        mMapView.removeLayer(dynamicMapServiceLayer);

        //Now load the new layers (basement layer)
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId0);
        mMapView.addLayer(dynamicMapServiceLayer);
        mMapView.addLayer(pawGraphicsLayer);

        //Set the background color of the fab button to the current floor
        whichFloor = 0;
        changeButtonBackgroundColor(whichFloor);
    }

    //Sets the first floor layer
    public void setFirstFloors(){
        checkAndRemoveGraphics();
        if(whichFloor == 1){
            return;}

        mMapView.removeLayer(pawGraphicsLayer);
        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId);
        mMapView.addLayer(dynamicMapServiceLayer);
        mMapView.addLayer(pawGraphicsLayer);

        whichFloor = 1;
        changeButtonBackgroundColor(whichFloor);
    }

    //Sets the second floor layer
    public void setSecondFloors(){
        checkAndRemoveGraphics();
        if(whichFloor == 2){
            return;}

        mMapView.removeLayer(pawGraphicsLayer);
        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId2);
        mMapView.addLayer(dynamicMapServiceLayer);
        mMapView.addLayer(pawGraphicsLayer);

        whichFloor = 2;
        changeButtonBackgroundColor(whichFloor);
    }

    //Sets the third floor layer
    public void setThirdFloors(){
        checkAndRemoveGraphics();
        if(whichFloor == 3){
            return;}

        mMapView.removeLayer(pawGraphicsLayer);
        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId3);
        mMapView.addLayer(dynamicMapServiceLayer);
        mMapView.addLayer(pawGraphicsLayer);

        whichFloor = 3;
        changeButtonBackgroundColor(whichFloor);
    }

    //Sets the fourth floor layer
    public void setFourthFloors(){
        checkAndRemoveGraphics();
        if(whichFloor == 4){
            return;}

        mMapView.removeLayer(pawGraphicsLayer);
        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId4);
        mMapView.addLayer(dynamicMapServiceLayer);
        mMapView.addLayer(pawGraphicsLayer);

        whichFloor = 4;
        changeButtonBackgroundColor(whichFloor);
    }

    //Sets the fifth floors
    public void setFifthFloors(){
        checkAndRemoveGraphics();
        if(whichFloor == 5){
            return;}

        mMapView.removeLayer(pawGraphicsLayer);
        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId5);
        mMapView.addLayer(dynamicMapServiceLayer);
        mMapView.addLayer(pawGraphicsLayer);

        whichFloor = 5;
        changeButtonBackgroundColor(whichFloor);
    }

    public void changeButtonBackgroundColor(int whatFloor)
    {
        FloatingActionButton[] floorArray = {base, floor1, floor2, floor3, floor4, floor5};
        //Check what floor level the user is currently on and change the button color of the corresponding floor button
        switch(whatFloor){

            case 0:
                for(FloatingActionButton buttons : floorArray){
                    if(buttons != base){
                        buttons.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    }
                    base.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
                break;
            case 1:
                for(FloatingActionButton buttons : floorArray){
                    if(buttons != floor1){
                        buttons.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    }
                    floor1.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
                break;
            case 2:
                for(FloatingActionButton buttons : floorArray){
                    if(buttons != floor2){
                        buttons.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    }
                    floor2.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
                break;
            case 3:
                for(FloatingActionButton buttons : floorArray){
                    if(buttons != floor3){
                        buttons.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    }
                    floor3.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
                break;
            case 4:
                for(FloatingActionButton buttons : floorArray){
                    if(buttons != floor4){
                        buttons.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    }
                    floor4.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
                break;
            case 5:
                for(FloatingActionButton buttons : floorArray){
                    if(buttons != floor5){
                        buttons.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    }
                    floor5.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
                break;
            default:
                //by default set it to the first floor
                resetButtonBackgroundColor();
                break;
        }
    }

    //now this will reset the floors and by default highlight
    //the first floor
    public void resetButtonBackgroundColor(){
        FloatingActionButton[] floorArray = {base, floor1, floor2, floor3, floor4, floor5};
        for(FloatingActionButton buttons : floorArray){
            if(buttons != floor1){
                buttons.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            floor1.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_search) {
            searchMenuItem = item;
            // Create search view and display on the Action Bar
            initSearchView();
            item.setActionView(mSearchView);
            return true;
        }else if (id == R.id.grid){

            //if paw is on the map, remove it
            checkAndRemoveGraphics();

            FragmentManager manager = getFragmentManager();
            Grid gridDialog = new Grid();
            gridDialog.show(manager, "Grid");

        } //else if (id == R.id.action_clear) {
        // Remove all the marker graphics
        //if (mMapViewHelper != null) {
        //  mMapViewHelper.removeAllGraphics();
        //}
        //return true;
        //}

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if ((mSearchView != null) && (!mSearchView.isIconified())) {
            // Close the search view when tapping back button
            if (searchMenuItem != null) {
                searchMenuItem.collapseActionView();
                invalidateOptionsMenu();
            }
        } else {
            super.onBackPressed();
        }
        //initSearchView();
    }

    // Initialize suggestion cursor
    private void initSuggestionCursor() {
        //can modify the params here to include other columns from the alias table
        //original
        String[] cols = new String[]{BaseColumns._ID, COLUMN_NAME_ADDRESS, COLUMN_NAME_X, COLUMN_NAME_Y};
        //modified
        //String[] cols = new String[]{BaseColumns._ID, COLUMN_NAME_ADDRESS,COLUMN_NAME_BUILDING, COLUMN_NAME_X, COLUMN_NAME_Y};
        mSuggestionCursor = new MatrixCursor(cols);
    }

    // Set the suggestion cursor to an Adapter then set it to the search view
    private void applySuggestionCursor() {
        //original
        String[] cols = new String[]{COLUMN_NAME_ADDRESS};

        //modified
        //String[] cols = new String[]{COLUMN_NAME_ADDRESS,COLUMN_NAME_BUILDING};
        int[] to = new int[]{R.id.suggestion_item_address};

        SimpleCursorAdapter mSuggestionAdapter = new SimpleCursorAdapter(mMapView.getContext(), R.layout.suggestion_item, mSuggestionCursor, cols, to, 0);
        mSearchView.setSuggestionsAdapter(mSuggestionAdapter);
        mSuggestionAdapter.notifyDataSetChanged();
    }

    // Initialize search view and add event listeners to handle query text changes and suggestion
    private void initSearchView() {
        if (mMapView == null || !mMapView.isLoaded())
            return;

        mSearchView = new SearchView(this);
        mSearchView.setFocusable(true);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint(getResources().getString(R.string.search_hint));// search bar hint

        // Open the soft keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!suggestClickFlag && !searchClickFlag) {
                    searchClickFlag = true;
                    onSearchButtonClicked(query);
                    mSearchView.clearFocus();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (mLocator == null)
                    return false;
                getSuggestions(newText); //lets user see suggestions while he types
                return true;
            }
        });
        //original
        //mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                // Obtain the content of the selected suggesting place via cursor
                MatrixCursor cursor = (MatrixCursor) mSearchView.getSuggestionsAdapter().getItem(position);
                int indexColumnSuggestion = cursor.getColumnIndex(COLUMN_NAME_ADDRESS);
                final String address = cursor.getString(indexColumnSuggestion);
                suggestClickFlag = true;

                //added the toast below that works to display the address searched

                //if (mMapViewHelper != null) {
                //  mMapViewHelper.removeAllGraphics();
                // }
                //uncommented this on 4/12 1:46 pm to check if this was error for crashing

                // setFloor(address);                            //8/8 modification removed this line for testing
                // Find the Location of the suggestion
                new FindLocationTask(address).execute(address);
                mSearchView.setQuery(address,false);
                Toast.makeText(getBaseContext(), address , Toast.LENGTH_SHORT).show();

                cursor.close();

                return true;
            }
        });
    }//end initSearchView

    //enabling labels
    //public void setLabelsEnabled (boolean enabled){

    // }
    //function checks for the address the user types and displays a toast message with the
    // corresponding floor number
    public void setFloor(String address){
        //Gets set to true if the string address contains a number
        boolean noNum = false;

        for(int i = 0; i < address.length(); i++){
            //right now we will iterate through the address until the first number if found
            //the first number will represent which floor level to set the layer to using setFloors
            char c = address.charAt(i);
            if(c == '0'){
                noNum = true;
                setBasementFloors();
                break;
            }
            else if(c == '1'){
                noNum = true;
                setFirstFloors();
                break;
            }
            else if(c == '2'){
                noNum = true;
                setSecondFloors();
                break;
            }
            else if(c == '3'){
                noNum = true;
                setThirdFloors();
                break;
            }
            else if(c == '4'){
                noNum = true;
                setFourthFloors();
                break;
            }
            else if(c == '5'){
                noNum = true;
                setFifthFloors();
                break;
            }
            else {
                //Does nothing
            }
        }
        //if it doesn not, then that means a building was searched so set
        //to first floor by default
        if(noNum == false){
            setFirstFloors();
        }
        //show the buttons to know which floor the user is currently on
        isOpen = false;
        showButtons(isOpen);
        return;
    }


    /**
     * Called from search_layout.xml when user presses Search button.
     *
     * @param address The text in the searchbar to be geocoded
     *
     */
    //handles the search button being clicked from the keyboard
    public void onSearchButtonClicked(String address) {
        hideKeyboard();
        // mMapViewHelper.removeAllGraphics();

        searchClickFlag = true;
        searchMiss = true;

        //setFloor(address);

        new FindLocationTask(address).execute(address);
        Toast.makeText(getBaseContext(), address , Toast.LENGTH_SHORT).show();

        // new FindLocationTask(address).execute(address);

        if(searchMiss == false) {
            if (searchMenuItem != null) {
                //searchMenuItem.collapseActionView();
                invalidateOptionsMenu();
            }
        }
        return;
        //setFloor(address);
        //executeLocatorTask(address);
        //new FindLocationTask(address).execute(address);
    }

    private void executeLocatorTask(String address) {

        //Create Locator parameters from single line address string
        // locatorParams(FIND_PLACE, address);
        //Toast.makeText(getBaseContext(), address , Toast.LENGTH_SHORT ).show();

        //Execute async task to find the address
        // LocatorAsyncTask locatorTask = new LocatorAsyncTask();
        // locatorTask.execute(findParams);

    }


    /*
       * This class provides an AsyncTask that performs a geolocation request on a
       * background thread and displays the first result on the map on the UI
       * thread.
       */
    private class LocatorAsyncTask extends
            AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {

        private Exception mException;

        public LocatorAsyncTask() {
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.setMessage(getString(R.string.address_search));
            mProgressDialog.show();
        }

        @Override
        protected List<LocatorGeocodeResult> doInBackground(
                LocatorFindParameters... params) {
            // Perform routing request on background thread
            mException = null;
            List<LocatorGeocodeResult> results = null;

            // Create locator using default online geocoding service and tell it
            // to
            // find the given address
            String url1 = getResources().getString(R.string.CSUSBLocator);
            //String url2 = getResources().getString(R.string.TestingLocator);
            // String esriurl = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";

            Locator locator = Locator.createOnlineLocator(url1);
            try {
                results = locator.find(params[0]);
            } catch (Exception e) {
                mException = e;
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<LocatorGeocodeResult> result) {
            // Display results on UI thread
            mProgressDialog.dismiss();
            if (mException != null) {
                Log.w(TAG, "LocatorSyncTask failed with:");
                mException.printStackTrace();
                Toast.makeText(MainActivity.this,
                        getString(R.string.addressSearchFailed),
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (result.size() == 0) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.noResultsFound), Toast.LENGTH_LONG)
                        .show();
            } else {
                // Use first result in the list
                LocatorGeocodeResult geocodeResult = result.get(0);

                // get return geometry from geocode result
                Point resultPoint = geocodeResult.getLocation();

                double x = resultPoint.getX();
                double y = resultPoint.getY();

                // Get the address
                String address = geocodeResult.getAddress();

                if (mMapViewHelper != null) {
                    mMapViewHelper.removeAllGraphics();
                }
                // Display the result on the map
                displaySearchResult(x, y, address);
                hideKeyboard();

            }
        }

    }


    //Fetch the Location from the Map and display it
    private class FindLocationTask extends AsyncTask<String, Void, Point> {
        private Point resultPoint = null;
        private String resultAddress;
        private Point temp = null;
        private Point temp2 = null;
        private Point temp3 = null;
        private String tempAddress;
        private Point point = null;

        public FindLocationTask(String address) {
            resultAddress = address;
        }


        @Override
        protected Point doInBackground(String... params) {
            // get the Location for the suggestion from the map
            //return null if nothing is found

            //this causes the search to fail when the text from the mSearchView and the text from
            //the suggestion list do not match
            //must use what the user selects every time
            for (LocatorSuggestionResult result : suggestionsList) {
                if(resultAddress.matches(result.getText())){
                    try {
                        //using find(LocatorSuggestionResult result, int maxLocations, List<String> outfields, SpatialReference outSR)
                        temp = ((mLocator.find(result, 2, null, mapSpatialReference)).get(0)).getLocation();
                        //Log.e(TAG,result.toString());
                        //using find(String address, Locator loc, CallbackListener<List<LocatorGeocodeResult>> callback)
                    } catch (Exception e) {
                        Log.e(TAG, "Exception in FIND");
                        Log.e(TAG, e.getMessage());
                    }
                }
            }//end for
            if(temp == null){
                //locatorParams(FIND_PLACE,resultAddress);
                findParams = new LocatorFindParameters(resultAddress);
                try {
                    // mLocator.find(findParams);
                    //temp = findParams.getLocation();
                    LocatorGeocodeResult loc;
                    SpatialReference spr = SpatialReference.create(102100);
                    loc = mLocator.find(findParams).get(0);
                    loc.setSpatialreference(spr);
                    //temp = (Point) GeometryEngine.project(temp,loc.getSpatialreference(),wm);
                    // temp = loc.getLocation();
                    temp = loc.getLocation();
                    //Log.e(TAG, "value of loc " + loc.toString());
                    //CoordinateConversion conversion = new CoordinateConversion();
                    //String tempString = conversion.pointToDecimalDegrees(temp,spr,20);
                    //temp = conversion.decimalDegreesToPoint(String.valueOf(temp),spr);
                    Point mapPoint = (Point) GeometryEngine.project(temp, SpatialReference.create(4326), mapSpatialReference);
                    //mapPoint = loc.getLocation();

                    //pass mapPoint getX and getY and pass them to the setXY method below!!!!



                    //temp.setXY(-13060611.247,4053208.076);
                    temp.setXY(mapPoint.getX(),mapPoint.getY());
                    //Log.e(TAG, "mapPoint");
                    //Log.e(TAG, String.valueOf(temp.getX()));
                    //Log.e(TAG, mapPoint.toString());
                    //Log.e(TAG,tempString);
                    // return mapPoint;
                    //loc = mLocator.find(findParams).get(0);
                    // spr = loc.getSpatialreference();
                    // temp = loc.getLocation();
                    // temp = (mLocator.find(findParams)).get(0).getLocation();

                } catch (Exception e) {
                    Log.e(TAG, "No find parameters found");
                    Log.e(TAG, e.getMessage());
                }
            }
            //Log.e(TAG, String.valueOf(temp.getX()));
            //Log.e(TAG,String.valueOf(mapSpatialReference.getID()));
            return temp;                                 //8/8 mod
        }

        @Override
        protected void onPreExecute() {
            // Display progress dialog on UI thread
            mProgressDialog.setMessage(getString(R.string.address_search));
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Point resultPoint) {
            // Dismiss progress dialog
            mProgressDialog.dismiss();   // --> modified this 6/27 got rid of the window leak
            if (mMapViewHelper != null) {
                mMapViewHelper.removeAllGraphics();
            }
            //commented out line 891 and placed it on 915
            if (temp == null) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.noResultsFoundPostExecute), Toast.LENGTH_LONG)
                        .show();

                //  locatorParams(FIND_PLACE, resultAddress);
                // findParams = new LocatorFindParameters(resultAddress);
                // LocatorAsyncTask locatorTask = new LocatorAsyncTask();
                //  locatorTask.execute(findParams);
                //if (searchMenuItem != null) {
                //searchMenuItem.collapseActionView();
                // invalidateOptionsMenu();
                // }
                hideKeyboard();
                //toSetFloors = false;
                return;
            }
            //end if

            // if (mMapViewHelper != null) {
            //    mMapViewHelper.removeAllGraphics();
            //}

            //8/7 mod finished here
            // Display the result
            displaySearchResult(temp.getX(), temp.getY(), resultAddress);


            hideKeyboard();
            //return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mProgressDialog.dismiss();  //-> modified 6/27 added this line removed window leak
        mMapViewState = mMapView.retainState();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start the MapView running again
        if (mMapView != null) {
            mMapView.unpause();
            if (mMapViewState != null) {
                mMapView.restoreState(mMapViewState);
            }
        }
    }

    /**
     * When the user types on the search bar, the following code suggests addresses for him
     *
     * @param suggestText String the user typed so far to fetch the suggestions
     */

    protected void getSuggestions(String suggestText) {
        final CallbackListener<List<LocatorSuggestionResult>> suggestCallback = new CallbackListener<List<LocatorSuggestionResult>>() {
            @Override
            public void onCallback(List<LocatorSuggestionResult> locatorSuggestionResults) {
                final List<LocatorSuggestionResult> locSuggestionResults = locatorSuggestionResults;
                if (locatorSuggestionResults == null)
                    return;
                suggestionsList = new ArrayList<>();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int key = 0;
                        if (locSuggestionResults.size() > 0) {
                            // Add suggestion list to a cursor
                            initSuggestionCursor();
                            for (final LocatorSuggestionResult result : locSuggestionResults) {
                                suggestionsList.add(result);

                                // Add the suggestion results to the cursor
                                mSuggestionCursor.addRow(new Object[]{key++, result.getText(), "0", "0"});
                            }
                            applySuggestionCursor();
                        }
                    }

                });

            }

            @Override
            public void onError(Throwable throwable) {
                //Log the error
                Log.e(MainActivity.class.getSimpleName(), "No results found (get suggestions class)");
                Log.e(MainActivity.class.getSimpleName(), throwable.getMessage());
            }
        };
        try {
            // Initialize the LocatorSuggestion parameters
            locatorParams(SUGGEST_PLACE, suggestText);
            //8/7 modification
            // suggestParams.setMaxSuggestions(3);     //added this line 8/7
            mLocator.suggest(suggestParams, suggestCallback);

        } catch (Exception e) {
            Log.e(MainActivity.class.getSimpleName(), "No results found(get suggestions class2)");
            Log.e(MainActivity.class.getSimpleName(), e.getMessage());
        }
    } //END getSuggestion()

    /**
     * Initialize the LocatorSuggestionParameters or LocatorFindParameters
     *
     * @param query The string for which the locator parameters are to be initialized
     *
     *
     */
    protected void locatorParams(String TYPE, String query) {

        if (TYPE.contentEquals(SUGGEST_PLACE)) {
            suggestParams = new LocatorSuggestionParameters(query);
            // Use the centre of the current map extent as the suggest location point
            suggestParams.setLocation(mMapView.getCenter(), mMapView.getSpatialReference());
            // Set the radial search distance in meters
            suggestParams.setDistance(500.0);
        } else if (TYPE.contentEquals(FIND_PLACE)) {
            findParams = new LocatorFindParameters(query);
            //Use the center of the current map extent as the find point
            findParams.setLocation(mMapView.getCenter(), mMapView.getSpatialReference());
            // Set the radial search distance in meters
            findParams.setDistance(500.0);
        }
    }

    /**
     * Display the search location on the map
     *
     * @param x       Longitude of the place
     * @param y       Latitude of the place
     * @param address The address of the location
     *
     */
    protected void displaySearchResult(double x, double y, String address) {

        // Add a marker at the found place
        //set the correct floor level first
        setFloor(address);

        PictureMarkerSymbol pawMarker = new PictureMarkerSymbol(getResources().getDrawable(R.drawable.coyote_paw));
        Point graphicPoint = new Point(x,y);

        //Add the paw graphic to the GraphicsLayer
        Graphic pointGraphic = new Graphic(graphicPoint,pawMarker);
        pawGraphicsLayer.addGraphic(pointGraphic);

        mMapView.centerAt(graphicPoint,false);
        mMapView.zoomToScale(graphicPoint,1500);
        mMapView.zoomin();

        mSearchView.setQuery(address, true);
        searchClickFlag = false;
        suggestClickFlag = false;

    }

    protected void hideKeyboard() {

        // Hide soft keyboard
        mSearchView.clearFocus();
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
    }



}