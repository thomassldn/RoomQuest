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
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorSuggestionParameters;
import com.esri.core.tasks.geocode.LocatorSuggestionResult;
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

    private static final String TAG = "PlaceSearch";
    private static final String COLUMN_NAME_ADDRESS = "address";
    private static final String COLUMN_NAME_X = "x";
    private static final String COLUMN_NAME_Y = "y";
    private static final String LOCATION_TITLE = "LOCATION";//was location

    private static final String FIND_PLACE = "Find";
    private static final String SUGGEST_PLACE = "Suggest";
    private static boolean suggestClickFlag = false;
    private static boolean searchClickFlag = false;
    private String mMapViewState;

    // Entry point to ArcGIS for Android Toolkit
    private MapViewHelper mMapViewHelper;

    private SearchView mSearchView;
    private MenuItem searchMenuItem;
    private MatrixCursor mSuggestionCursor;

    private static ProgressDialog mProgressDialog;
    private LocatorSuggestionParameters suggestParams;
    private LocatorFindParameters findParams;

    private SpatialReference mapSpatialReference;
    private static ArrayList<LocatorSuggestionResult> suggestionsList;

    //Graphics are objects held in memory that store a shape (geometry)
    // and are displayed on a map via a GraphicsLayer.
    GraphicsLayer mGraphicsLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.DYNAMIC);
    MapView mMapView;
    Locator mLocator = null;

    //***PLACE SEARCH ***/
    public String url = "http://roomquest.cse.csusb.edu:6080/arcgis/rest/services/FirstRoomsLocator/GeocodeServer";

    //for changing button color
    //default set it to the first floor
    public int whichFloor;
    private static boolean searchMiss = true;

    //Initialization of compass object
    Compass mCompass;

    //Buttons
    //declaring the floating action button variables
    FloatingActionButton plus, base, floor1, floor2, floor3, floor4, floor5;
    Animation FabOpen, FabClose, FabRotateClockwise, FabRotateCounter;
    boolean isOpen;

    //GPS
    public LocationManager manager;
    LocationDisplayManager ldm;
    public static Point mLocation = null;

    //Request GPS
    private int requestCode = 2;
    String[] reqPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    //Labels
    //to display the annotation layers from ArcGIS server that contain the labels for each floor
    //an ArcGISDynamicMapServiceLayer is needed
    ArcGISDynamicMapServiceLayer dynamicMapServiceLayer;
    //Format:
    //int[] layer  = {RoofsLayer,FloorsLayer,RoomsLayer,AnnotationLayer}
    int[] layerId5 = {45,34,33,1};
    int[] layerId4 = {45,36,35,3};
    int[] layerId3 = {45,38,37,5};
    int[] layerId2 = {45,40,39,6};
    int[] layerId  = {45,42,41,8};
    int[] layerId0 = {45,44,43,10};

    //Grid Layers
    int[] bikeRacks               = {45,38,37,5,14};
    int[] parkingDispensers       = {45,38,37,5,15};
    int[] disabilityParkingAreas  = {45,38,37,52,16};
    int[] informationCenters      = {45,38,37,5,17};
    int[] palmDesertShuttle       = {45,38,37,5,18};
    int[] emergencyPhones         = {45,38,37,5,19};
    int[] restRooms               = {45,38,37,5,20};
    int[] evchargingstations      = {45,38,37,5,21};
    int[] healthCenter            = {45,38,37,5,22};
    int[] atm                     = {45,38,37,5,23};
    int[] campusEvacuationSites   = {45,38,37,5,24};
    int[] dining                  = {45,38,37,5,26};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.coyote_head);
        ArcGISRuntime.setClientId("nVzh9JCohNTXQOfu");
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

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
        // Initialize the helper class to use the Toolkit
        mMapViewHelper = new MapViewHelper(mMapView);

        mLocator = Locator.createOnlineLocator(url);//url

        // Setup listener for map initialized
        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (source == mMapView && status == STATUS.INITIALIZED) {
                    mapSpatialReference = mMapView.getSpatialReference();

                    if (mMapViewState == null) {
                        Log.i(TAG, "MapView.setOnStatusChangedListener() status=" + status.toString());
                    } else {
                        mMapView.restoreState(mMapViewState);
                    }
                }
            }
        });

        // Set the MapView to allow the user to rotate the map when as part of a pinch gesture.
        mMapView.setAllowRotationByPinch(true);

        // Enabled wrap around map.
        mMapView.enableWrapAround(true);

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


        //****************BUTTONS  ***********************
        //adding the fab_plus button
        plus = (FloatingActionButton)findViewById(R.id.plus);
        base = (FloatingActionButton)findViewById(R.id.fab_basement);
        base.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayToast("Basements");
                setBasementFloors();

            }
        });

        floor1 = (FloatingActionButton)findViewById(R.id.fab_first);
        floor1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayToast("1st Floors");
                setFirstFloors();
            }
        });

        floor2 = (FloatingActionButton)findViewById(R.id.fab_second);
        floor2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayToast("2nd Floors");
                setSecondFloors();
            }
        });

        floor3 = (FloatingActionButton)findViewById(R.id.fab_third);
        //final Button button3 = (Button) findViewById(R.id.fab_third);
        floor3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayToast("3rd Floors");
                setThirdFloors();
            }
        });

        floor4 = (FloatingActionButton)findViewById(R.id.fab_fourth);
        floor4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayToast("4th Floors");
                setFourthFloors();
            }
        });

        floor5 = (FloatingActionButton)findViewById(R.id.fab_five);
        floor5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayToast("5th Floors");
                setFifthFloors();
            }
        });

        //now adding the animation to the fab buttons
        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        FabRotateClockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        FabRotateCounter = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_counter_clockwise);

        //fab on click listener
        plus.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(isOpen)
                {
                    base.startAnimation(FabClose);
                    floor1.startAnimation(FabClose);
                    floor2.startAnimation(FabClose);
                    floor3.startAnimation(FabClose);
                    floor4.startAnimation(FabClose);
                    floor5.startAnimation(FabClose);
                    plus.startAnimation(FabRotateCounter);
                    base.setClickable(false);
                    floor1.setClickable(false);
                    floor2.setClickable(false);
                    floor3.setClickable(false);
                    floor4.setClickable(false);
                    floor5.setClickable(false);
                    isOpen = false;
                }
                else
                {
                    base.startAnimation(FabOpen);
                    floor1.startAnimation(FabOpen);
                    floor2.startAnimation(FabOpen);
                    floor3.startAnimation(FabOpen);
                    floor4.startAnimation(FabOpen);
                    floor5.startAnimation(FabOpen);
                    plus.startAnimation(FabRotateClockwise);
                    base.setClickable(true);
                    floor1.setClickable(true);
                    floor2.setClickable(true);
                    floor3.setClickable(true);
                    floor4.setClickable(true);
                    floor5.setClickable(true);
                    isOpen = true;
                }
            }
        });
        //****************END BUTTONS CODE  ***********************

        //Initializing Map with Campus Extend
        Envelope myExtents = new Envelope(-13061415.588940706,4052665.227615348,-13059240.709590949,4054092.392969663);//(xmin,ymin,xmax,ymax)
        myExtents = (Envelope) GeometryEngine.project(myExtents, SpatialReference.create(102100), mMapView.getSpatialReference());
        mMapView.setMaxExtent(myExtents);
        mMapView.setMaxScale(600);
        mMapView.setMinScale(20000);

        //set the intial button color to be grey on button1
        whichFloor = 1;
        changeButtonBackgroundColor(whichFloor);

        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId);
        mMapView.addLayer(dynamicMapServiceLayer);

        // Request GPS Permission
        ActivityCompat.requestPermissions(MainActivity.this, reqPermissions, requestCode);

}//END ONCREATE

    //Code for the Buttons Begins Here
    public void setBasementFloors(){

        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId0);
        mMapView.addLayer(dynamicMapServiceLayer);

        //now highlight the button
        whichFloor = 0;
        changeButtonBackgroundColor(whichFloor);

    }

    //set 1st floors
    public void setFirstFloors(){

        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId);
        mMapView.addLayer(dynamicMapServiceLayer);

        whichFloor = 1;
        changeButtonBackgroundColor(whichFloor);

    }//END setFirstFloors()

    //set 2nd floors
    public void setSecondFloors(){
        //code with the labels
        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId2);
        mMapView.addLayer(dynamicMapServiceLayer);

        //now highlight the button
        whichFloor = 2;
        changeButtonBackgroundColor(whichFloor);

    }//END setSecondFloors()

    //set 3rd floors
    public void setThirdFloors(){

        //code with the labels
        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId3);
        mMapView.addLayer(dynamicMapServiceLayer);

        //now highlight the button
        whichFloor = 3;
        changeButtonBackgroundColor(whichFloor);

    }//END setThirdFloors()

    //set 4th floors
    public void setFourthFloors(){

        //code with the labels
        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId4);
        mMapView.addLayer(dynamicMapServiceLayer);

        //now highlight the button
        whichFloor = 4;
        changeButtonBackgroundColor(whichFloor);

    }//END setFourthFloors()

    //set 5th floors
    public void setFifthFloors(){

        mMapView.removeLayer(dynamicMapServiceLayer);
        dynamicMapServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.MapServer),layerId5);
        mMapView.addLayer(dynamicMapServiceLayer);

        //now highlight the button
        whichFloor = 5;
        changeButtonBackgroundColor(whichFloor);

    }//END setFifthFloors()


    public void changeButtonBackgroundColor(int whatFloor)
    {
        //check what floor your in and change the button color to yellow
        //floor3.setBackgroundTintList(ColorStateList.valueOf(Color.YELLOW));
        switch(whatFloor){
            case 0:
                base.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                //set the other colors back to normal
                floor1.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor2.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor3.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor4.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor5.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                break;
            case 1:
                base.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                //set the other colors back to normal
                floor1.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                floor2.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor3.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor4.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor5.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                break;
            case 2:
                base.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                //set the other colors back to normal
                floor1.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor2.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                floor3.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor4.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor5.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                break;
            case 3:
                base.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                //set the other colors back to normal
                floor1.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor2.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor3.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                floor4.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor5.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                break;
            case 4:
                base.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                //set the other colors back to normal
                floor1.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor2.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor3.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor4.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                floor5.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                break;
            case 5:
                base.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                //set the other colors back to normal
                floor1.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor2.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor3.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor4.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                floor5.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
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
        base.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        floor1.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        floor2.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        floor3.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        floor4.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        floor5.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
    }


    //function checks for the address the user types and displays a toast message with the
    // corresponding floor number
    public void setFloor(String address){
        //iterate through the address until the first number is found
        //the first number will represent which floor level to set the layer to using
        //setFloors
        for(int i = 0; i < address.length(); i++){
            char choice = address.charAt(i);

            if(choice == '0'){
                Toast.makeText(getBaseContext(), "Basement" , Toast.LENGTH_SHORT ).show();
                setBasementFloors();
                break;
            } else if(choice == '1'){
                Toast.makeText(getBaseContext(), "First Floor" , Toast.LENGTH_SHORT ).show();
                setFirstFloors();
                break;
            } else if(choice == '2'){
                Toast.makeText(getBaseContext(), "Second Floor" , Toast.LENGTH_SHORT ).show();
                setSecondFloors();
                break;
            } else if(choice == '3'){
                Toast.makeText(getBaseContext(), "Third Floor" , Toast.LENGTH_SHORT ).show();
                setThirdFloors();
                break;
            } else if(choice == '4'){
                Toast.makeText(getBaseContext(), "Fourth Floor" , Toast.LENGTH_SHORT ).show();
                setFourthFloors();
                break;
            } else if(choice == '5'){
                Toast.makeText(getBaseContext(), "Fifth Floor" , Toast.LENGTH_SHORT ).show();
                setFifthFloors();
                break;
            } else {
                //if the address the user types in has no floor number
                //Toast.makeText(getBaseContext(), "Sorry invalid suggestion!" , Toast.LENGTH_LONG ).show();
                //break;
            }
        }
        return;
    }

    //here for testing purpouses
    public void displayToast(String msg){

        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();

    }



    //Code for the Buttons ENDS here

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
    }

    // Initialize suggestion cursor
    private void initSuggestionCursor() {
        String[] cols = new String[]{BaseColumns._ID, COLUMN_NAME_ADDRESS, COLUMN_NAME_X, COLUMN_NAME_Y};
        mSuggestionCursor = new MatrixCursor(cols);
    }

    // Set the suggestion cursor to an Adapter then set it to the search view
    private void applySuggestionCursor() {
        String[] cols = new String[]{COLUMN_NAME_ADDRESS};
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

        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                if(!suggestClickFlag && !searchClickFlag) {
                    searchClickFlag = true;
                    onSearchButtonClicked(query);
                    mSearchView.clearFocus();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if(mLocator == null)
                    return false;
                getSuggestions(newText); //lets user see suggestions while he types
                return true;
            }
        });

        mSearchView.setOnSuggestionListener(new OnSuggestionListener() {

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

                // Find the Location of the suggestion
                new FindLocationTask(address).execute(address);

                cursor.close();

                return true;
            }
        });
    }

    /**
     * Called from search_layout.xml when user presses Search button.
     *
     * @param address The text in the searchbar to be geocoded
     */
    public void onSearchButtonClicked(String address) {
        hideKeyboard();
        setFloor(address);

        //mMapViewHelper.removeAllGraphics();
        executeLocatorTask(address);
    }

    private void executeLocatorTask(String address) {

        //Create Locator parameters from single line address string
        locatorParams(FIND_PLACE, address);

        //Execute async task to find the address
        LocatorAsyncTask locatorTask = new LocatorAsyncTask();
        locatorTask.execute(findParams);

    }

    /*
       * This class provides an AsyncTask that performs a geolocation request on a
       * background thread and displays the first result on the map on the UI
       * thread.
       */
    private class LocatorAsyncTask extends AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {

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
            Locator locator = Locator.createOnlineLocator(url);//url
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

                // Display the result on the map
                displaySearchResult(x,y,address);
                hideKeyboard();
            }
        }
    }

    //Fetch the Location from the Map and display it
    private class FindLocationTask extends AsyncTask<String,Void,Point> {
        private Point resultPoint = null;
        private String resultAddress;
        private Point temp = null;

        public FindLocationTask(String address) {

            resultAddress = address;
        }

        @Override
        protected Point doInBackground(String... params) {

            // get the Location for the suggestion from the map
            for(LocatorSuggestionResult result: suggestionsList) {
                if (resultAddress.matches(result.getText())) {
                    try {
                        temp = ((mLocator.find(result, 2, null, mapSpatialReference)).get(0)).getLocation();
                    } catch (Exception e) {
                        Log.e(TAG,"Exception in FIND");
                        Log.e(TAG,e.getMessage());
                    }
                }
            }

            resultPoint = (Point) GeometryEngine.project(temp, mapSpatialReference, SpatialReference.create(4326));

            return resultPoint;
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
            mProgressDialog.dismiss();
            if (resultPoint == null)
                return;

            // Remove all the marker graphics before setting new marker
            if (mMapViewHelper != null) {
                mMapViewHelper.removeAllGraphics();
            }

            // Display the result
            displaySearchResult(resultPoint.getX(), resultPoint.getY(), resultAddress);

            hideKeyboard();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

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
                        if(locSuggestionResults.size() > 0) {
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
                Log.e(MainActivity.class.getSimpleName(), "No Results found!!");
                Log.e(MainActivity.class.getSimpleName(), throwable.getMessage());
            }
        };

        try {
            // Initialize the LocatorSuggestion parameters
            locatorParams(SUGGEST_PLACE,suggestText);

            mLocator.suggest(suggestParams, suggestCallback);

        } catch (Exception e) {
            Log.e(MainActivity.class.getSimpleName(),"No Results found");
            Log.e(MainActivity.class.getSimpleName(),e.getMessage());
        }
    } //END getSuggestion()

    /**
     * Initialize the LocatorSuggestionParameters or LocatorFindParameters
     *
     * @param query The string for which the locator parameters are to be initialized
     */
    protected void locatorParams(String TYPE, String query) {

        if(TYPE.contentEquals(SUGGEST_PLACE)) {
            suggestParams = new LocatorSuggestionParameters(query);
            // Use the centre of the current map extent as the suggest location point
            suggestParams.setLocation(mMapView.getCenter(), mMapView.getSpatialReference());
            // Set the radial search distance in meters
            suggestParams.setDistance(500.0);
        }
        else if(TYPE.contentEquals(FIND_PLACE)) {
            findParams = new LocatorFindParameters(query);
            //Use the center of the current map extent as the find point
            findParams.setLocation(mMapView.getCenter(), mMapView.getSpatialReference());
            // Set the radial search distance in meters
            findParams.setDistance(500.0);
        }
    }

    /**
     * Display the search location on the map
     * @param x Longitude of the place
     * @param y Latitude of the place
     * @param address The address of the location
     */
    protected void displaySearchResult(double x, double y, String address) {

        //The following line, sets the correct floor on the map
        setFloor(address);

        // Add a marker at the found place. When tapping on the marker, a Callout with the address
        // will be displayed
        mMapViewHelper.addMarkerGraphic(y, x, LOCATION_TITLE, address, R.drawable.coyote_mascot,getResources().getDrawable(R.drawable.coyote_paw) , false, 1);//was android.R.drawable.ic_menu_myplaces

        mMapView.centerAndZoom(y, x, 20);
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