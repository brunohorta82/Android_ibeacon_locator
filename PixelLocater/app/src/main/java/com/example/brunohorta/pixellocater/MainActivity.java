package com.example.brunohorta.pixellocater;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;

import java.util.Collection;


public class MainActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "MonitoringActivity";
    private BeaconManager beaconManager;
    private Region region;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    TextView  t ;
    TextView  t1 ;
    TextView  t2 ;
    TextView  t3 ;
    protected double norm(Point p) // get the norm of a vector
    {
        return Math.pow(Math.pow(p.x, 2) + Math.pow(p.y, 2), .5);
    }

    protected Point trilateration(Point point1, Point point2, Point point3) {
        Point resultPose = new Point();
        //unit vector in a direction from point1 to point 2
        double p2p1Distance = Math.pow(Math.pow(point2.x - point1.x, 2) + Math.pow(point2.y - point1.y, 2), 0.5);
        Point ex = new Point((point2.x - point1.x) / p2p1Distance, (point2.y - point1.y) / p2p1Distance);
        Point aux = new Point(point3.x - point1.x, point3.y - point1.y);
        //signed magnitude of the x component
        double i = ex.x * aux.x + ex.y * aux.y;
        //the unit vector in the y direction.
        Point aux2 = new Point(point3.x - point1.x - i * ex.x, point3.y - point1.y - i * ex.y);
        Point ey = new Point(aux2.x / norm(aux2), aux2.y / norm(aux2));
        //the signed magnitude of the y component
        double j = ey.x * aux.x + ey.y * aux.y;
        //coordinates
        double x = (Math.pow(point1.distance, 2) - Math.pow(point2.distance, 2) + Math.pow(p2p1Distance, 2)) / (2 * p2p1Distance);
        double y = (Math.pow(point1.distance, 2) - Math.pow(point3.distance, 2) + Math.pow(i, 2) + Math.pow(j, 2)) / (2 * j) - i * x / j;
        //result coordinates
        double finalX = point1.x + x * ex.x + y * ey.x;
        double finalY = point1.y + x * ex.y + y * ey.y;
        resultPose.x = finalX;
        resultPose.y = finalY;
        return resultPose;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {

                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

                    }
                });

                builder.show();
            }
        }
        setContentView(R.layout.activity_main);
        t = findViewById(R.id.resultLbl);
        t1 = findViewById(R.id.t1);
        t2 = findViewById(R.id.t2);
        t3 = findViewById(R.id.t3);

        RangedBeacon.setSampleExpirationMilliseconds(1000);
        //BeaconManager.setDebug(true);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setForegroundScanPeriod(500L);
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        final Point p1 = new Point(0, 0, "e20a39f4-73f5-4bc4-a12f-17d1ad07a961");
        final Point p2 = new Point(0, 9, "e20a39f4-73f5-4bc4-a12f-17d1ad07a962");
        final Point p3 = new Point(9, 0, "e20a39f4-73f5-4bc4-a12f-17d1ad07a963");

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Point b1 = null;
                Point b2 = null;
                Point b3 = null;

                for (Beacon beacon : beacons) {
                    if (beacon.getId1().toString().equals(p1.id)) {
                        Log.i(TAG, "POINT ONE " + beacon.getDistance());
                        b1 = p1;
                        b1.distance = beacon.getDistance();
                    } else if (beacon.getId1().toString().equals(p2.id)) {
                        b2 = p2;
                        b2.distance = beacon.getDistance();
                        Log.i(TAG, "POINT TWO " + beacon.getDistance());
                    }
                    if (beacon.getId1().toString().equals(p3.id)) {
                        b3 = p3;
                        b3.distance = beacon.getDistance();
                        Log.i(TAG, "POINT THREE " + beacon.getDistance());
                    }
                }
                if(b1 != null && b2 != null && b3 != null){

                    final Point trilateration = trilateration(b1, b2, b3);
                    final Point finalB1 = b1;
                    final Point finalB2 = b2;
                    final Point finalB3 = b3;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            t.setText("X: "+trilateration.x+" Y: "+trilateration.y);
                            t1.setText("B1 "+finalB1.distance+" mts");
                            t2.setText("B2 "+finalB2.distance+" mts");
                            t3.setText("B3 "+finalB3.distance+" mts");

                        }
                    });

                    Log.i(TAG,    "X: "+trilateration.x+" Y: "+trilateration.y);
                    Log.i(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                }



            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("com.example.brunohorta.pixellocater", null, null, null));
        } catch (RemoteException e) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

}
