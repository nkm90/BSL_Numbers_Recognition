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

package com.example.HearMeWhenYouCanNotSeeMe;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.HearMeWhenYouCanNotSeeMe.basic.BasicActivity;
import com.google.mediapipe.formats.proto.LandmarkProto;
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
    private static final String OUTPUT_HAND_RECT = "multi_hand_rects";
    private List<NormalizedLandmarkList> multiHandLandmarks;

    private TextView gesture;
    private TextView moveGesture;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gesture = findViewById(R.id.gesture);
        moveGesture = findViewById(R.id.move_gesture);
        result = findViewById(R.id.resultString);
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = result.getText().toString();
                Intent intent = new Intent();
                intent.putExtra("MESSAGE", message);
                setResult(1, intent);
                finish();
            }
        });

        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);


        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    Log.d(TAG, "Received multi-hand landmarks packet.");
                    multiHandLandmarks =
                            PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gesture.setText(handGestureCalculator(multiHandLandmarks));
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

    private String getMultiHandLandmarksDebugString(List<NormalizedLandmarkList> multiHandLandmarks) {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand landmarks";
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

    private String handGestureCalculator(List<NormalizedLandmarkList> multiHandLandmarks) {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand detected";
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

            Log.d("Foot", "" + landmarkList.get(0).getY() + " " + landmarkList.get(1).getY() + " " + landmarkList.get(20).getY());

            float pseudoFixKeyPoint;
            /* FINGERS CONDITIONS
             * To identify when a finger is straight up or straight down.
             * Each of the following conditions allowed me to create the state straightUp on each finger.
             * INDEX_FINGER */
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
            pseudoFixKeyPoint = landmarkList.get(4).getX();
            if (getEuclideanDistanceAB(pseudoFixKeyPoint,landmarkList.get(4).getY(), landmarkList.get(9).getX(), landmarkList.get(9).getY()) <=
                    getEuclideanDistanceAB(pseudoFixKeyPoint,landmarkList.get(4).getY(), landmarkList.get(2).getX(), landmarkList.get(2).getY()) &&
                    landmarkList.get(4).getY() >= landmarkList.get(3).getY()){
                thumbIsBend = true;
            }else {
                thumbIsOpen = true;
            }

            // Hand gesture recognition
            if (thumbIsOpen && indexStraightUp && middleStraightUp && ringStraightUp && pinkyStraightUp)
            {
                return "FIVE";
            }
            else if (thumbIsBend && indexStraightUp && middleStraightUp && ringStraightUp && pinkyStraightUp)
            {
                return "FOUR";
            }
            else if (thumbIsOpen && indexStraightUp && middleStraightUp && ringStraightDown && pinkyStraightDown)
            {
                return "TREE";
            }
            else if (thumbIsOpen && indexStraightUp && middleStraightDown && ringStraightDown && pinkyStraightDown)
            {
                return "TWO";
            }
            else if (!thumbIsOpen && indexStraightUp && middleStraightDown && ringStraightDown && pinkyStraightDown)
            {
                return "ONE";
            }
            else {
                String info = "thumbIsOpen " + thumbIsOpen + ", thumbIsBend " + thumbIsBend
                        + ", indexStraightUp " + indexStraightUp + ", indexStraightDown " + indexStraightDown
                        + ", middleStraightUp " + middleStraightUp + ", middleStraightDown " + middleStraightDown
                        + ", ringStraightUp " + ringStraightUp + ", ringStraightDown " + ringStraightDown
                        + ", pinkyStraightUp " + pinkyStraightUp + ", pinkyStraightDown " + pinkyStraightDown;
                Log.d(TAG, "handGestureCalculator: == " + info);
                return " ";
            }
        }
        return " ";
    }
    private boolean isThumbNearFirstFinger(LandmarkProto.NormalizedLandmark point1, LandmarkProto.NormalizedLandmark point2) {
        double distance = getEuclideanDistanceAB(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        return distance < 0.1;
    }

    private double getEuclideanDistanceAB(double a_x, double a_y, double b_x, double b_y) {
        double dist = Math.pow(a_x - b_x, 2) + Math.pow(a_y - b_y, 2);
        return Math.sqrt(dist);
    }

    private double getAngleABC(double a_x, double a_y, double b_x, double b_y, double c_x, double c_y) {
        double ab_x = b_x - a_x;
        double ab_y = b_y - a_y;
        double cb_x = b_x - c_x;
        double cb_y = b_y - c_y;

        double dot = (ab_x * cb_x + ab_y * cb_y);   // dot product
        double cross = (ab_x * cb_y - ab_y * cb_x); // cross product

        return Math.atan2(cross, dot);
    }

    private int radianToDegree(double radian) {
        return (int) Math.floor(radian * 180. / Math.PI + 0.5);
    }
}
