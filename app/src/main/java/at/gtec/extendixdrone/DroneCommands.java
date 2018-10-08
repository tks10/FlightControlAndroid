package at.gtec.extendixdrone;

import android.util.Log;

import at.gtec.droneapi.ParrotDrone;

/**
 * <h1>CommandsDrone class</h1>
 * class providing basic commands for connected drone to execute.
 * @author  g.tec medical engineering GmbH
 * @version 1.0.0
 * @since   1.0.0
 */

public class DroneCommands {

    /**
     * Drone tries takes off.
     * @param _drone The drone connected.
     */
    public static void DroneTakeOff(ParrotDrone _drone){
        try {
            _drone.TakeOff();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Drone tries to land.
     * @param _drone The drone connected.
     */
    public static void DroneLand(ParrotDrone _drone){
        try {
            _drone.Land();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Drone moves either forward or back depending on the given parameters.
     * @param _drone The drone connected.
     * @param value The speed of the movement given in byte.
     * @param time The time in ms that the drone will execute the command
     */
    public static void DroneMove(ParrotDrone _drone,Byte value,int time){
        try {
            _drone.DisableStabilization();
            _drone.Pitch(value,time);
            Log.e("Pitch", "value: " + Integer.toString((int)value) + ", time: " + Integer.toString(time));
            //_drone.EnableStabilization();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Drone moves either right or left depending on the given parameters.
     * @param _drone The drone connected.
     * @param value The speed of the movement given in byte.
     * @param time The time in ms that the drone will execute the command
     */
    public static void DroneTurn(ParrotDrone _drone,Byte value,int time){
        try {
            _drone.DisableStabilization();
            _drone.Roll(value,time);
            Log.e("Turn", "value: " + Integer.toString((int)value) + ", time: " + Integer.toString(time));
            // _drone.EnableStabilization();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Drone rotates either left or right depending on the given parameters.
     * @param _drone The drone connected.
     * @param value The speed of the movement given in byte.
     * @param time The time in ms that the drone will execute the command
     */
    public static void DroneRotate(ParrotDrone _drone, Byte value,int time){
        try {
            _drone.DisableStabilization();
            _drone.Yaw(value,time);
            Log.e("Rotate", "value: " + Integer.toString((int)value) + ", time: " + Integer.toString(time));
            _drone.EnableStabilization();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to start a video stream.
     * @param _drone The drone connected.
     */
    public static void DroneVideoStream(ParrotDrone _drone){
        try {

            _drone.StartVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to stop the video streaming.
     * @param _drone The drone connected.
     */
    public static void DroneVideoStopStream(ParrotDrone _drone){
        try {
            _drone.StopVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Tries to take a picture with the drone camera.
     * @param _drone The drone connected.
     */
    public static void DroneTakePicture(ParrotDrone _drone){
        try {
            _drone.TakePicture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Moves the Drone camera up.
     * @param _drone The drone connected.
     * @param tilt The number of degrees that the drone camera is moving up.
     * @param pan The number that the camera pans.
     */
    public static void DroneChangeCameraOrientation(ParrotDrone _drone,byte tilt,byte pan ){
        try {
            _drone.ChangeCameraOrientation(tilt,pan);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
