package roomquest.cse.csusb.edu.roomquest;
/**

 *
 * Modified By: Jose Banuelos, Thomas Saldana
 * Date: 6 Feb 17
 *
 */
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.toolkit.map.MapViewHelper;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorSuggestionParameters;
import com.esri.core.tasks.geocode.LocatorSuggestionResult;
import com.esri.core.tasks.na.NAFeaturesAsFeature;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

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
    private static final String LOCATION_TITLE = "";//was location

    private static final String FIND_PLACE = "Find";
    private static final String SUGGEST_PLACE = "Suggest";
    private static boolean suggestClickFlag = false;
    private static boolean searchClickFlag = false;

    //private MapView mMapView;  DONT FORGET TO UNCOMMENT IF DEL "ADDED CODE"
    private String mMapViewState;
    // Entry point to ArcGIS for Android Toolkit
    private MapViewHelper mMapViewHelper;

    //private Locator mLocator;   DONT FORGET TO UNCOMMENT IF DEL "ADDED CODE"
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

    //***********ADDED CODE ***************

    MapView mMapView;
    //final String extern = Environment.getExternalStorageDirectory().getPath();
    //final String tpkPath = "/ArcGIS/samples/OfflineRouting/SanDiego.tpk";


    //GraphicsLayer mGraphicsLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.DYNAMIC);

    // RouteTask mRouteTask = null;
    //NAFeaturesAsFeature mStops = new NAFeaturesAsFeature();

    Locator mLocator = null;
    // View mCallout = null;
    //Spinner dSpinner;

    //***********END ADDED CODE ***************


    //***PLACE SEARCH ***/
    public String url = "http://roomquest.research.cse:6080/arcgis/rest/services/FirstRoomsLocator2/GeocodeServer";






    //********JOSES CODE ***************/
    public ArcGISFeatureLayer mFeatureLayer;//leave
    public ArcGISFeatureLayer mFeatureLayer2;
    public ArcGISFeatureLayer mFeatureLayer3;
    public String mFeatureServiceUrl;//leave
    public String mFeatureServiceUrl2;
    public String mFeatureServiceUrl3;

    //for changing button color
    //default set it to the first floor
    public int whichFloor = 1;


    //********END JOSES CODE ***************/



    //Initialization of compass object
    Compass mCompass;

    //GPS
    LocationDisplayManager ldm;
    public static Point mLocation = null;
    int mProgress;


    //Buttons
    //declaring the floating action button variables
    FloatingActionButton plus, base, floor1, floor2, floor3, floor4, floor5;
    Animation FabOpen, FabClose, FabRotateClockwise, FabRotateCounter;



    boolean isOpen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.coyote_head);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);




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
        // Create the default ArcGIS online Locator. If you want to provide your own {@code Locator},
        // user other methods of Locator.


        String extern = Environment.getExternalStorageDirectory().getPath();
        mLocator = Locator.createOnlineLocator(url);
        //mLocator = Locator.createOnlineLocator();

        // set logo and enable wrap around
        mMapView.setEsriLogoVisible(true);
        mMapView.enableWrapAround(true);

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




        //**********Campus Code Starts Here**********

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
        //the button ids were changed to the fab_button ids
        //final Button buttonb = (Button) findViewById(R.id.fab_basement);
        base.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // displayToast("Basements");
                //base.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_coloring));
                //base.setBackground(getResources().getDrawable(R.drawable.button_coloring));
                //base.setVisibility(View.INVISIBLE);

                setBasementFloors();

            }
        });

        floor1 = (FloatingActionButton)findViewById(R.id.fab_first);
        //final Button button1 = (Button) findViewById(R.id.fab_first);
        floor1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // displayToast("1st Floors");
                setFirstFloors();
                //button1.setBackgroundColor(getResources().getColor(R.color.yellow));
            }
        });

        floor2 = (FloatingActionButton)findViewById(R.id.fab_second);
        //final Button button2 = (Button) findViewById(R.id.fab_second);
        floor2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // displayToast("2nd Floors");
                setSecondFloors();
            }
        });

        floor3 = (FloatingActionButton)findViewById(R.id.fab_third);
        //final Button button3 = (Button) findViewById(R.id.fab_third);
        floor3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // displayToast("3rd Floors");
                // floor3.setBackgroundTintList(ColorStateList.valueOf(Color.YELLOW));
                setThirdFloors();
            }
        });

        floor4 = (FloatingActionButton)findViewById(R.id.fab_fourth);
        //final Button button4 = (Button) findViewById(R.id.button4);
        floor4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // displayToast("4th Floors");
                setFourthFloors();
            }
        });

        floor5 = (FloatingActionButton)findViewById(R.id.fab_five);
        //final Button button5 = (Button) findViewById(R.id.button5);
        floor5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // displayToast("Floor 5");
                setFifthFloors();
            }
        });

        //for changing button color
        //changeButtonBackgroundColor(whichFloor);


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


        //Initialize map with first floor layer
        String firstFloorLines = getString(R.string.firstFloorLineLayer);
        mFeatureServiceUrl2 = firstFloorLines;
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl2,ArcGISFeatureLayer.MODE.ONDEMAND);
        mMapView.addLayer(mFeatureLayer);

        //adding the polygon layer
        String firstFloorPolygons = getString(R.string.firstFloorPolygonLayer);
        mFeatureServiceUrl = firstFloorPolygons;
        mFeatureLayer2 = new ArcGISFeatureLayer(mFeatureServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer2);

        whichFloor = 1;
        changeButtonBackgroundColor(whichFloor);


    //***************END BUTTONS **********************//


    }//END ONCREATE

    public void onDialogMessage(int boxNum) { //box number of the grid
        mMapView.removeLayer(mFeatureLayer);


        //Map links grid box numbers to url strings
        Map<Integer, String> gridMap = new HashMap<Integer, String>();

        //set key to strings for the grid buttons
        gridMap.put(1, this.getResources().getString(R.string.bikeRacks));
        gridMap.put(2, this.getResources().getString(R.string.parkingDispensers));
        gridMap.put(3, this.getResources().getString(R.string.disabilityParkingAreas));
        gridMap.put(4, this.getResources().getString(R.string.informationCenters));
        gridMap.put(5, this.getResources().getString(R.string.palmDesertShuttle));
        gridMap.put(6, this.getResources().getString(R.string.emergencyPhones));
        gridMap.put(7, this.getResources().getString(R.string.restRooms));
        gridMap.put(8, this.getResources().getString(R.string.vendingMachines));
        gridMap.put(9, this.getResources().getString(R.string.healthCenter));
        gridMap.put(10, this.getResources().getString(R.string.atm));
        gridMap.put(11, this.getResources().getString(R.string.campusEvacuationSites));
        gridMap.put(12, this.getResources().getString(R.string.dining));


        mFeatureServiceUrl = gridMap.get(boxNum);//ex) gridMap.get(2);
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer);


    }//END onDIalog

    //*******************Joses Buttons *********************************/


    //BEGIN ADDING FLOORS TO MAP
    public void setBasementFloors(){
        mMapView.removeLayer(mFeatureLayer);
        mMapView.removeLayer(mFeatureLayer2);
        //mMapView.removeLayer(mFeatureLayer3);

        //add the lines layer on top
        String  basementFloorLines = getString(R.string.basementFloorLineLayer);
        mFeatureServiceUrl = basementFloorLines;
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl,ArcGISFeatureLayer.MODE.ONDEMAND);
        mMapView.addLayer(mFeatureLayer);

        //adding the polygon layer
        String  basementFloorPolygons = getString(R.string.basementFloorPolygonLayer);
        mFeatureServiceUrl2 = basementFloorPolygons ;
        mFeatureLayer2 = new ArcGISFeatureLayer(mFeatureServiceUrl2, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer2);

        //now highlight the button
        whichFloor = 0;
        changeButtonBackgroundColor(whichFloor);

    }

    //set 1st floors
    public void setFirstFloors(){
        mMapView.removeLayer(mFeatureLayer);
        mMapView.removeLayer(mFeatureLayer2);
        //mMapView.removeLayer(mFeatureLayer3);


        //add the lines layer on top
        String firstFloorLines = getString(R.string.firstFloorLineLayer);
        mFeatureServiceUrl2 = firstFloorLines;
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl2,ArcGISFeatureLayer.MODE.ONDEMAND);
        mMapView.addLayer(mFeatureLayer);

        //adding the polygon layer
        String firstFloorPolygons = getString(R.string.firstFloorPolygonLayer);
        mFeatureServiceUrl = firstFloorPolygons;
        mFeatureLayer2 = new ArcGISFeatureLayer(mFeatureServiceUrl, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer2);

        whichFloor = 1;
        changeButtonBackgroundColor(whichFloor);

    }//END setFirstFloors()


    //set 2nd floors
    public void setSecondFloors(){
        mMapView.removeLayer(mFeatureLayer);
        mMapView.removeLayer(mFeatureLayer2);
        //mMapView.removeLayer(mFeatureLayer3);

        //add the lines layer on top
        String secondFloorLines = getString(R.string.secondFloorLineLayer);
        mFeatureServiceUrl = secondFloorLines;
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl,ArcGISFeatureLayer.MODE.ONDEMAND);
        mMapView.addLayer(mFeatureLayer);

        //adding the polygon layer
        String secondFloorPolygons = getString(R.string.secondFloorPolygonLayer);
        mFeatureServiceUrl2 = secondFloorPolygons;
        mFeatureLayer2 = new ArcGISFeatureLayer(mFeatureServiceUrl2, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer2);


        //now highlight the button
        whichFloor = 2;
        changeButtonBackgroundColor(whichFloor);


    }//END setSecondFloors()


    //set 3rd floors
    public void setThirdFloors(){
        mMapView.removeLayer(mFeatureLayer);
        mMapView.removeLayer(mFeatureLayer2);
       // mMapView.removeLayer(mFeatureLayer3);



        //add the lines layer on top
        String thirdFloorLines = getString(R.string.thirdFloorLineLayer);
        mFeatureServiceUrl = thirdFloorLines;
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl,ArcGISFeatureLayer.MODE.ONDEMAND);
        mMapView.addLayer(mFeatureLayer);

        //adding the polygon layer
        String thirdFloorPolygons = getString(R.string.thirdFloorPolygonLayer);
        mFeatureServiceUrl2 = thirdFloorPolygons ;
        mFeatureLayer2 = new ArcGISFeatureLayer(mFeatureServiceUrl2, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer2);


        //now highlight the button
        whichFloor = 3;
        changeButtonBackgroundColor(whichFloor);


    }//END setThirdFloors()


    //set 4th floors
    public void setFourthFloors(){
        mMapView.removeLayer(mFeatureLayer);
        mMapView.removeLayer(mFeatureLayer2);
        //mMapView.removeLayer(mFeatureLayer3);

        //add the lines layer on top
        String fourthFloorLines = getString(R.string.fourthFloorLineLayer);
        mFeatureServiceUrl = fourthFloorLines;
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl,ArcGISFeatureLayer.MODE.ONDEMAND);
        mMapView.addLayer(mFeatureLayer);

        //adding the polygon layer
        String  fourthFloorPolygons = getString(R.string.fourthFloorPolygonLayer);
        mFeatureServiceUrl2 = fourthFloorPolygons ;
        mFeatureLayer2 = new ArcGISFeatureLayer(mFeatureServiceUrl2, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer2);

        //now highlight the button
        whichFloor = 4;
        changeButtonBackgroundColor(whichFloor);

    }//END setFourthFloors()


    //set 5th floors
    public void setFifthFloors(){
        mMapView.removeLayer(mFeatureLayer);
        mMapView.removeLayer(mFeatureLayer2);
        //mMapView.removeLayer(mFeatureLayer3);

        //add the lines layer on top
        String fifthFloorLines = getString(R.string.fifthFloorLineLayer);
        mFeatureServiceUrl = fifthFloorLines;
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceUrl,ArcGISFeatureLayer.MODE.ONDEMAND);
        mMapView.addLayer(mFeatureLayer);

        //adding the polygon layer
        String fifthFloorPolygons = getString(R.string.fifthFloorPolygonLayer);
        mFeatureServiceUrl2 = fifthFloorPolygons;
        mFeatureLayer2 = new ArcGISFeatureLayer(mFeatureServiceUrl2, ArcGISFeatureLayer.MODE.ONDEMAND);//leave
        mMapView.addLayer(mFeatureLayer2);


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



    //*******************END Joses Buttons *********************************/
    public void displayToast(String msg){

        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

        //start
        //MenuInflater menuInflater = getMenuInflater();
        //menuInflater.inflate(R.menu.menu_main, menu);
        //return super.onCreateOptionsMenu(menu);

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

        } else if (id == R.id.action_clear) {
            // Remove all the marker graphics
            if (mMapViewHelper != null) {
                mMapViewHelper.removeAllGraphics();
            }
            return true;
        }

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

                //**********Joses Code **********
                setFloor(address);
                //**********END Joses Code *********/

                // Find the Location of the suggestion
                new FindLocationTask(address).execute(address);

                cursor.close();

                return true;
            }
        });
    }


    //*****************JOSES CODE ************************/
    //function checks for the address the user types and displays a toast message with the
    // corresponding floor number
    public void setFloor(String address){
        for(int i = 0; i < address.length(); i++){
            //right now we will iterate through the address until the first number if found
            //the first number will represent which floor level to set the layer to using
            //setFloors
            char c = address.charAt(i);
            if(c == '1'){
                Toast.makeText(getBaseContext(), "First Floor" , Toast.LENGTH_SHORT ).show();
                setFirstFloors();
                break;
            }
            else if(c == '2'){
                Toast.makeText(getBaseContext(), "Second Floor" , Toast.LENGTH_SHORT ).show();
                setSecondFloors();
                break;
            }
            else if(c == '3'){
                Toast.makeText(getBaseContext(), "Third Floor" , Toast.LENGTH_SHORT ).show();
                setThirdFloors();
                break;
            }
            else if(c == '4'){
                Toast.makeText(getBaseContext(), "Fourth Floor" , Toast.LENGTH_SHORT ).show();
                setFourthFloors();
                break;
            }
            else if(c == '5'){
                Toast.makeText(getBaseContext(), "Fifth Floor" , Toast.LENGTH_SHORT ).show();
                setFifthFloors();
                break;
            }
            else {
                //if the address the user types in has no floor number
                Toast.makeText(getBaseContext(), "Sorry invalid suggestion!" , Toast.LENGTH_LONG ).show();
                break;
            }
        }
        return;
    }


    //**********************END JOSES CODE *************************/

    /**
     * Called from search_layout.xml when user presses Search button.
     *
     * @param address The text in the searchbar to be geocoded
     */
    public void onSearchButtonClicked(String address) {
        hideKeyboard();
        mMapViewHelper.removeAllGraphics();
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
            //String url = "http://roomquest.research.cse:6080/arcgis/rest/services/Rooms_CreateAddressLocator/GeocodeServer";
            String extern = Environment.getExternalStorageDirectory().getPath();
            Locator locator = Locator.createOnlineLocator(url);
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
    //CALLOUT
    protected void displaySearchResult(double x, double y, String address) {
        // Add a marker at the found place. When tapping on the marker, a Callout with the address
        // will be displayed
        //mMapViewHelper
        mMapViewHelper.addMarkerGraphic(y, x, LOCATION_TITLE, address, R.drawable.coyote_mascot, null, false, 1);//was android.R.drawable.ic_menu_myplaces
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