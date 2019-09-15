package biz.aliustaoglu.mapbox.MapBoxModule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import biz.aliustaoglu.mapbox.GenericMapModule.GenericMapLayout;
import biz.aliustaoglu.mapbox.R;

import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;


public class MapBoxMapView extends GenericMapLayout implements OnMapReadyCallback, Style.OnStyleLoaded {
    MapboxMap mapInstance = null;
    MapView mapView = null;
    ReadableMap coords;
    Style style;
    SymbolManager symbolManager;

    public MapBoxMapView(@NonNull Context context) {
        super(context);
        mapView = new MapView(context);
        this.addView(mapView);
        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.isMapReady = true;
        mapInstance = mapboxMap;
        mapInstance.setStyle(Style.MAPBOX_STREETS);
        reactNativeEvent("onMapReady", null);



        // Init props
        if (this.zoom != null) setZoom(this.zoom);
        if (this.lat != null && this.lng != null) setRegion(this.lat, this.lng);
        if (this.mapStyle != null) setMapStyle(this.mapStyle);
    }

    @Override
    public void setZoom(Integer zoom) {
        super.setZoom(zoom);
        if (this.isMapReady) {
            CameraPosition position = new CameraPosition.Builder().zoom(this.zoom).build();
            mapInstance.setCameraPosition(position);
        }
    }

    @Override
    public void setRegion(Double lat, Double lng) {
        super.setRegion(lat, lng);
        if (this.isMapReady) {
            CameraPosition position = new CameraPosition.Builder().target(new LatLng(this.lat, this.lng)).build();
            mapInstance.setCameraPosition(position);
        }
    }

    @Override
    public void setData(ReadableMap data) {
        super.setData(data);
    }

    @Override
    public void setMapStyle(String mapStyle) {
        super.setMapStyle(mapStyle);
        if (this.isMapReady) {
            try {
                String strStyle = this.mapStyle;
                if (!this.mapStyle.startsWith("mapbox://style")) {
                    strStyle = Style.class.getField(this.mapStyle).get(new Object()).toString();
                }
                mapInstance.setStyle(strStyle);
            } catch (Exception e) {

            }
            mapInstance.getStyle(this);
        }
    }

    @Override
    public void setMarkers(ReadableArray markers) {
        super.setMarkers(markers);

        if (this.isMapReady && this.style!=null) {
            symbolManager = new SymbolManager(mapView, mapInstance, this.style);

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_compass_icon);
            mapInstance.getStyle().addImage("my-marker", bm);
            symbolManager.create(new SymbolOptions()
                    .withLatLng(new LatLng(-36, 174))
                    //set the below attributes according to your requirements
                    .withIconImage("my-marker")
                    .withIconSize(1.5f)
                    .withIconOffset(new Float[] {0f,-1.5f})
                    .withTextField("DENEME")
                    .withTextHaloColor("rgba(255, 255, 255, 100)")
                    .withTextHaloWidth(5.0f)
                    .withTextAnchor("top")
                    .withTextOffset(new Float[] {0f, 1.5f})
            );



        }
    }


    @Override
    public void locateUser(ReadableArray args) {
        coords = args.getMap(0);
        if (this.style != null && this.isMapReady) {
            LocationComponent locationComponent = mapInstance.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(activity, style).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            CameraPosition position = new CameraPosition.Builder().target(new LatLng(coords.getDouble("latitude"), coords.getDouble("longitude"))).zoom(zoom).build();
            mapInstance.setCameraPosition(position);
        }
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) throws SecurityException {
        this.style = style;


        //symbolManager.setIconAllowOverlap(true);
        //symbolManager.setTextAllowOverlap(true);

        if (this.markers != null) this.setMarkers(this.markers);
    }
}
