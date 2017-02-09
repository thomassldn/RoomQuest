package roomquest.cse.csusb.edu.roomquest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
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

import java.util.List;
import java.util.ArrayList;

/**
 * PlaceSearch app uses the geocoding service to convert addresses to and from
 * geographic coordinates and place them on the map.  Search for places, addresses,
 * etc. and get suggestions as you type.
 *
 *
 *
 * Modified By: Jose Banuelos, Thomas Saldana
 * Date: 6 Feb 17
 *
 */

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "PlaceSearch";
    private static final String COLUMN_NAME_ADDRESS = "address";
    private static final String COLUMN_NAME_X = "x";
    private static final String COLUMN_NAME_Y = "y";
    private static final String LOCATION_TITLE = "Location";
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





    //***********ADDED CODE ***************

    //MapView mMapView;//leave

    public ArcGISFeatureLayer mFeatureLayer;//leave
    public String mFeatureServiceUrl;//leave
    //public GraphicsLayer mGraphicsLayer;//leave
    // Define ArcGIS Elements

    MapView mMapView;
    final String extern = Environment.getExternalStorageDirectory().getPath();
    //final String tpkPath = "/ArcGIS/samples/OfflineRouting/SanDiego.tpk";


    GraphicsLayer mGraphicsLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.DYNAMIC);

    // RouteTask mRouteTask = null;
    NAFeaturesAsFeature mStops = new NAFeaturesAsFeature();

    Locator mLocator = null;
    View mCallout = null;
    Spinner dSpinner;

    //***********END ADDED CODE ***************


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
        // Initialize the helper class to use the Toolkit
        mMapViewHelper = new MapViewHelper(mMapView);
        // Create the default ArcGIS online Locator. If you want to provide your own {@code Locator},
        // user other methods of Locator.
        String url = "http://roomquest.research.cse:6080/arcgis/rest/services/Rooms_CreateAddressLocator/GeocodeServer";

        String extern = Environment.getExternalStorageDirectory().getPath();
        mLocator = Locator.createOnlineLocator();
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

    }//END onCreate()

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
        } else if (id == R.id.action_clear) {//action_clear
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
            String url = "http://roomquest.research.cse:6080/arcgis/rest/services/Rooms_CreateAddressLocator/GeocodeServer";
            String extern = Environment.getExternalStorageDirectory().getPath();
            Locator locator = Locator.createOnlineLocator();
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
    protected void displaySearchResult(double x, double y, String address) {
        // Add a marker at the found place. When tapping on the marker, a Callout with the address
        // will be displayed
        mMapViewHelper.addMarkerGraphic(y, x, LOCATION_TITLE, address, android.R.drawable.ic_menu_myplaces, null, false, 1);
        mMapView.centerAndZoom(y, x, 14);
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
