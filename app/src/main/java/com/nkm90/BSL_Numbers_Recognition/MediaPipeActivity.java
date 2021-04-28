// Copyright 2019 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.nkm90.BSL_Numbers_Recognition;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import com.nkm90.BSL_Numbers_Recognition.basic.BasicActivity;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.framework.PacketGetter;

import java.util.List;

/**
 * Activity of MediaPipe multi-hand tracking app.
 */
public class MediaPipeActivity extends BasicActivity {

    private static final String TAG = "MediaPipeActivity";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "multi_hand_landmarks";
    private List<NormalizedLandmarkList> multiHandLandmarks;

    private TextView gesture;
    private TextView result;
    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gesture = findViewById(R.id.gesture);
        result = findViewById(R.id.resultString);
        timestamp = System.currentTimeMillis();

        /*
         * When the result TextView area is pressed, the String thanks is stored
         * as message, passed back to the onActivityResult, and closing the
         * current Activity
         */
        result.setOnClickListener(v -> {
            String message = getResources().getString(R.string.thanks);
            Intent intent = new Intent();
            intent.putExtra("MESSAGE", message);
            setResult(1, intent);
            finish();
        });

        // keep screen on and the orientation to portrait
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // Frame processor received from the basicActivity of MediaPipe library
        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    Log.d(TAG, "Received multi-hand landmarks packet.");
                    multiHandLandmarks =
                            PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gesture.setText(handGestureCalculator(multiHandLandmarks)); //display gesture on top
                            String number = handGestureCalculator(multiHandLandmarks); //set the gesture as a String to be used
                            // Adding timestamp to add the number to the bottom, it will help with building sentences with the results when needed
                            if (timestamp + 2000 < System.currentTimeMillis() && !number.equals(getResources().getString(R.string.noHands)) && !number.equals("no gesture")){
                                result.setText(number);
                                timestamp = System.currentTimeMillis();
                            }
                        }
                    });
                    Log.d(
                            TAG,
                            "[TS:"
                                    + packet.getTimestamp()
                                    + "] "
                                    + getMultiHandLandmarksDebugString(multiHandLandmarks));
                });
    }

    /**
     * The getMultiHandLandmarksDebugString method helps building a readable String for the
     * debugger, keeping track of the different points positions obtained from the multiHandLandmarks
     * of MediaPipe.
     *
     * @param multiHandLandmarks list on inputs with the different points positions obtained from MediaPipe
     * @return a String with the points and their respective X, Y and Z positions for each hand recognised
     */
    private String getMultiHandLandmarksDebugString(List<NormalizedLandmarkList> multiHandLandmarks) {
        if (multiHandLandmarks.isEmpty()) {
            return getResources().getString(R.string.noHands);
        }
        String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
        int handIndex = 0;
        for (NormalizedLandmarkList landmarks : multiHandLandmarks) {
            multiHandLandmarksStr +=
                    "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
            int landmarkIndex = 0;
            for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
                multiHandLandmarksStr +=
                        "\t\tLandmark ["
                                + landmarkIndex
                                + "]: ("
                                + landmark.getX()
                                + ", "
                                + landmark.getY()
                                + ", "
                                + landmark.getZ()
                                + ")\n";
                ++landmarkIndex;
            }
            ++handIndex;
        }
        return multiHandLandmarksStr;
    }

    /**
     * When the back button is pressed, we return the message thanks form the strings.xml to the menu
     * and close the activity.
     */
    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent();
        backIntent.putExtra("MESSAGE", getResources().getString(R.string.thanks));
        setResult(1, backIntent);
        finish();
    }

    /**
     * The handGestureCalculator method takes the different position of the points obtained from
     * MediaPipe in post to return a string that contains the number for that gesture.
     *
     * @param multiHandLandmarks array of normalised landmarks points obtained from MediaPipe
     * @return String value with the number for the recognised sign
     */
    private String handGestureCalculator(List<NormalizedLandmarkList> multiHandLandmarks) {
        if (multiHandLandmarks.isEmpty()) {
            return getResources().getString(R.string.noHands);
        }
        boolean thumbIsOpen = false;
        boolean thumbIsBend = false;
        boolean indexStraightUp = false;
        boolean indexStraightDown = false;
        boolean middleStraightUp = false;
        boolean middleStraightDown = false;
        boolean ringStraightUp = false;
        boolean ringStraightDown = false;
        boolean pinkyStraightUp = false;
        boolean pinkyStraightDown = false;

        for (NormalizedLandmarkList landmarks : multiHandLandmarks) {
            List<NormalizedLandmark> landmarkList = landmarks.getLandmarkList();

            //Logging to the console the X-axis points that make the base of the palm and do not move like the ones on the fingers
            Log.d("Palm base", "" + landmarkList.get(0).getX() + " " + landmarkList.get(1).getX() + " " + landmarkList.get(2).getX() + " " + landmarkList.get(17).getX());
            //Logging to the console the Y-axis points that make the base of the palm and do not move like the ones on the fingers
            Log.d("Palm base", "" + landmarkList.get(0).getY() + " " + landmarkList.get(1).getY() + " " + landmarkList.get(2).getY() + " " + landmarkList.get(17).getY());

            /* FINGERS CONDITIONS
             * To identify when a finger is straight up or straight down.
             * Each of the following conditions allowed me to create the state straightUp on each finger.*/

            /*INDEX_FINGER*/
            if (landmarkList.get(8).getY() < landmarkList.get(7).getY()
                    && landmarkList.get(7).getY() < landmarkList.get(6).getY()
                    && landmarkList.get(6).getY() < landmarkList.get(5).getY()){
                indexStraightUp = true;
            }else if (getEuclideanDistanceAB(landmarkList.get(8).getX(),landmarkList.get(8).getY(), landmarkList.get(0).getX(), landmarkList.get(0).getY()) <
                    getEuclideanDistanceAB(landmarkList.get(5).getX(),landmarkList.get(5).getY(), landmarkList.get(0).getX(), landmarkList.get(0).getY())){
                indexStraightDown = true;
            }

            /*MIDDLE_FINGER */
            if (landmarkList.get(12).getY() < landmarkList.get(11).getY()
                    && landmarkList.get(11).getY() < landmarkList.get(10).getY()
                    && landmarkList.get(10).getY() < landmarkList.get(9).getY()){
                middleStraightUp = true;
            }else if (getEuclideanDistanceAB(landmarkList.get(12).getX(),landmarkList.get(12).getY(), landmarkList.get(0).getX(), landmarkList.get(0).getY()) <
                    getEuclideanDistanceAB(landmarkList.get(9).getX(),landmarkList.get(9).getY(), landmarkList.get(0).getX(), landmarkList.get(0).getY())){
                middleStraightDown = true;
            }

            /*RING_FINGER */
            if (landmarkList.get(16).getY() < landmarkList.get(15).getY()
                    && landmarkList.get(15).getY() < landmarkList.get(14).getY()
                    && landmarkList.get(14).getY() < landmarkList.get(13).getY()){
                ringStraightUp = true;
            } else if (getEuclideanDistanceAB(landmarkList.get(16).getX(),landmarkList.get(16).getY(), landmarkList.get(0).getX(), landmarkList.get(0).getY()) <
                    getEuclideanDistanceAB(landmarkList.get(13).getX(),landmarkList.get(13).getY(), landmarkList.get(0).getX(), landmarkList.get(0).getY())){
                ringStraightDown = true;
            }

            /*PINKY_FINGER */
            if (landmarkList.get(20).getY() < landmarkList.get(19).getY()
                    && landmarkList.get(19).getY() < landmarkList.get(18).getY()
                    && landmarkList.get(18).getY() < landmarkList.get(17).getY()){
                pinkyStraightUp = true;
            } else if (getEuclideanDistanceAB(landmarkList.get(20).getX(),landmarkList.get(20).getY(), landmarkList.get(0).getX(), landmarkList.get(0).getY()) <
                    getEuclideanDistanceAB(landmarkList.get(17).getX(),landmarkList.get(17).getY(), landmarkList.get(0).getX(), landmarkList.get(0).getY())){
                pinkyStraightDown = true;
            }

            /*THUMB */
            if (getEuclideanDistanceAB(landmarkList.get(4).getX(),landmarkList.get(4).getY(), landmarkList.get(9).getX(), landmarkList.get(9).getY())
                    < getEuclideanDistanceAB(landmarkList.get(3).getX(),landmarkList.get(3).getY(), landmarkList.get(9).getX(), landmarkList.get(9).getY())){
                thumbIsBend = true;
            }else {
                thumbIsOpen = true;
            }

            // Hand gesture recognition based on the position of the fingers
            if (thumbIsOpen){
                if (indexStraightUp && middleStraightUp && ringStraightUp && pinkyStraightUp){
                    return getResources().getString(R.string.five);
                }else if (indexStraightUp && middleStraightUp && ringStraightUp && pinkyStraightDown){
                    return getResources().getString(R.string.nine);
                }else if (indexStraightUp && middleStraightUp && ringStraightDown && pinkyStraightDown){
                    return getResources().getString(R.string.eight);
                }else if (radianToDegree(getAngleABC(landmarkList.get(4).getX(), landmarkList.get(4).getY(),
                        landmarkList.get(2).getX(), landmarkList.get(2).getY(),
                        landmarkList.get(8).getX(),landmarkList.get(8).getX())) >= 65//1.0210176124167 radians
                        && indexStraightUp && middleStraightDown && ringStraightDown && pinkyStraightDown
                        || landmarkList.get(2).getX() > landmarkList.get(17).getX() && // the hand is left
                        radianToDegree(getAngleABC(landmarkList.get(4).getX(), landmarkList.get(4).getY(),
                        landmarkList.get(2).getX(), landmarkList.get(2).getY(),
                        landmarkList.get(8).getX(),landmarkList.get(8).getX())) <= 65 //1.0210176124167 radians
                        && indexStraightUp && middleStraightDown && ringStraightDown && pinkyStraightDown){
                    return getResources().getString(R.string.seven);
                }else  if (indexStraightDown && middleStraightDown && ringStraightDown && pinkyStraightDown){
                    return getResources().getString(R.string.six);
                }
            } else if (thumbIsBend){
                if (indexStraightDown && middleStraightDown && ringStraightDown && pinkyStraightDown){
                    return getResources().getString(R.string.zero);
                }
                else if (indexStraightUp && middleStraightDown && ringStraightDown && pinkyStraightDown){
                    return getResources().getString(R.string.one);
                }else if (indexStraightUp && middleStraightUp && ringStraightDown && pinkyStraightDown){
                    return getResources().getString(R.string.two);
                }else if (indexStraightUp && middleStraightUp && ringStraightUp && pinkyStraightDown){
                    return getResources().getString(R.string.three);
                }else if (indexStraightUp && middleStraightUp && ringStraightUp && pinkyStraightUp){
                    return getResources().getString(R.string.four);
                }
            }
            else {
                //When different position is found, the fingers positions conditions are logged on the console
                String info = "thumbIsOpen " + thumbIsOpen + ", thumbIsBend " + thumbIsBend
                        + ", indexStraightUp " + indexStraightUp + ", indexStraightDown " + indexStraightDown
                        + ", middleStraightUp " + middleStraightUp + ", middleStraightDown " + middleStraightDown
                        + ", ringStraightUp " + ringStraightUp + ", ringStraightDown " + ringStraightDown
                        + ", pinkyStraightUp " + pinkyStraightUp + ", pinkyStraightDown " + pinkyStraightDown;
                Log.d(TAG, "handGestureCalculator: == " + info);
                return ""; // nothing is displayed on the screen
            }
        }
        return " "; // nothing is displayed on the screen
    }

    /**
     * The following method calculates the distance between 2 points (A and B) using euclidean distance
     * formula.
     *
     * @param a_x Value of X for the given position of point A
     * @param a_y Value of Y for the given position of point A
     * @param b_x Value of X for the given position of point B
     * @param b_y Value of Y for the given position of point B
     * @return Euclidean distance result
     */
    private double getEuclideanDistanceAB(double a_x, double a_y, double b_x, double b_y) {
        double dist = Math.pow(a_x - b_x, 2) + Math.pow(a_y - b_y, 2);
        return Math.sqrt(dist);
    }

    /**
     * This method calculates the angle between 3 given points (A,B,C)
     * using the angle between vectors formula. The vector 1 is made
     * with points AB and vector 2 is made with points BC, being point B
     * the vertex.
     *
     * @param a_x Value of X for the given position of A
     * @param a_y Value of Y for the given position of A
     * @param b_x Value of X for the given position of B
     * @param b_y Value of Y for the given position of B
     * @param c_x Value of X for the given position of C
     * @param c_y Value of Y for the given position of C
     * @return Angle in radians
     */
    private double getAngleABC(double a_x, double a_y,
                               double b_x, double b_y,
                               double c_x, double c_y) {
        double ab_x = b_x - a_x;
        double ab_y = b_y - a_y;
        double cb_x = b_x - c_x;
        double cb_y = b_y - c_y;

        double dot = (ab_x * cb_x + ab_y * cb_y);   // dot product
        double cross = (ab_x * cb_y - ab_y * cb_x); // cross product

        return Math.atan2(cross, dot);
    }

    /**
     * Method to convert radian to degree based on the formula
     * @param radian Value of radians to convert
     * @return Angle in degrees
     */
    private int radianToDegree(double radian) {
        return (int) Math.floor(radian * 180. / Math.PI + 0.5);
    }
}
