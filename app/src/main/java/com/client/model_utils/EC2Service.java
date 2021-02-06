package com.client.model_utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.util.IOUtils;
import com.google.cloud.android.speech.MainActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by neymoura on 04/09/17.
 */

public class EC2Service {

    //Singleton Block

    private static AmazonEC2 ec2Instance;
    static Context CONTEXT;
    static String TAG = "EC2Service";
   static String username ;
   static String password ;
   static String connIP;
    static String hostFile ;
    static byte[] pemFile;
    static MainActivity mainActivity = new MainActivity();
    private static AmazonEC2 getEC2Client(){

        if (ec2Instance != null) {
            return ec2Instance;
        } else {
            ec2Instance = new AmazonEC2Client(CredentialsService.getCredentials());
            ec2Instance.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
        }

        return ec2Instance;

    }

    //Utility Block

    private static void postEvent(EC2ServiceEvent.Type type, Object data) {
        EventBus.getDefault().post(new EC2ServiceEvent(type, data));
    }

    //Business Block

    /**
     * Start a EC2 Instance with the specified parameters
     *
     * @param imageID
     * @param instanceType
     * @param keyName
     */
    public static void requestInstance(final Context context, final String imageID, final String instanceType, final String keyName/*, final String securityGroup*/){

        CONTEXT = context;

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                AmazonEC2 ec2 = getEC2Client();

                RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
                runInstancesRequest.withImageId(imageID)
//                        .withSecurityGroups(securityGroup)
                        .withInstanceType(instanceType)
                        .withKeyName(keyName)
                        .withMinCount(1)
                        .withMaxCount(1)
                        .withSubnetId("subnet-e83b228f");

                Reservation reservation = ec2.runInstances(runInstancesRequest).getReservation();

                postEvent(EC2ServiceEvent.Type.REQUEST_INSTANCE, reservation);

                requestInstances(context);


                return null;

            }

        }.execute();

    }

    /**
     * Describe the available EC2 images
     */
    public static void requestImages(){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                AmazonEC2 ec2 = getEC2Client();

                Filter filter = new Filter();
                filter.withName("name").withValues("ubuntu");

                Collection<Filter> filterCollection = new ArrayList<>();
                filterCollection.add(filter);

                DescribeImagesRequest imagesRequest = new DescribeImagesRequest();
                imagesRequest.setFilters(filterCollection);

                DescribeImagesResult describeImagesResult = ec2.describeImages(imagesRequest);

                List<Image> images = describeImagesResult.getImages();

                postEvent(EC2ServiceEvent.Type.REQUEST_IMAGES, images);

                return null;

            }

        }.execute();

    }

    /**
     * Describe the existing KeyPairs
     */
    public static void requestKeyPairs(){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                AmazonEC2 ec2 = getEC2Client();

                List<KeyPairInfo> keyPairs = ec2.describeKeyPairs().getKeyPairs();

                postEvent(EC2ServiceEvent.Type.REQUEST_KEY_PAIRS, keyPairs);

                return null;

            }

        }.execute();

    }

    /**
     * Describe the existing SecutrityGroups
     */
    public static void requestSecurityGroups(){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                AmazonEC2 ec2 = getEC2Client();

                List<SecurityGroup> securityGroups = ec2.describeSecurityGroups().getSecurityGroups();

                postEvent(EC2ServiceEvent.Type.REQUEST_SECURITY_GROUPS, securityGroups);

                return null;

            }

        }.execute();

    }

    /**
     * Request the current instances
     */
    public static void requestInstances(final Context context){
        CONTEXT = context;

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                AmazonEC2 ec2 = getEC2Client();

                List<Reservation> reservations = ec2.describeInstances().getReservations();

                List<Instance> instances = new ArrayList<>();

                int runningInstanceIndex = -1;

                for (Reservation reservation : reservations) {
                    instances.addAll(reservation.getInstances());
                    //Log.d("instances", reservation.toString());
                }

                for (int i = 0; i < instances.size(); i++) {
                    if (instances.get(i).getState().getCode() == 16) {
                        runningInstanceIndex = i;
                    }
                }

                if (runningInstanceIndex == -1) {
                    Log.d(TAG, "NO RUNNING STATE FOUND");
                } else {
                    if (context != null) {
                        String username = "ec2-user";
                        String password = "ec2-password";
                        String connIP = instances.get(runningInstanceIndex).getPublicIpAddress();
                        String hostFile = instances.get(runningInstanceIndex).getPublicDnsName();
                        //AssetManager assetManager = context.getAssets();
                        byte[] pemFile;

                        AssetManager assetManager = context.getResources().getAssets();
                        InputStream inputStream = null;

                        try {
                            //inputStream = assetManager.open("noon_ec2.pem");
                            //pemFile = IOUtils.toByteArray(inputStream);

                            //new SSHManager(context, hostFile);


                            SSHManager sshManager = new SSHManager(context, username, password,
                                    connIP, hostFile);



                        } catch (Exception e) { e.printStackTrace(); }


                        /*
                        new SSHRequestManager(
                                context,
                                instances.get(runningInstanceIndex).getPublicIpAddress());
                        */
                    }
                }

                postEvent(EC2ServiceEvent.Type.REQUEST_INSTANCES, instances);

                return null;

            }

        }.execute();

    }


    /**
     * Try to start a instance
     * @param instanceId instance to start
     */

    public static void requestStart(final String instanceId){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                AmazonEC2 ec2 = getEC2Client();

                StartInstancesRequest request = new StartInstancesRequest();
                request.withInstanceIds(instanceId);

                StartInstancesResult startInstancesResult = ec2.startInstances(request);

                postEvent(EC2ServiceEvent.Type.REQUEST_START, startInstancesResult);

                return null;

            }

        }.execute();

    }

    /**
     * Try to stop a instance
     * @param instanceId instance to stop
     */
    public static void requestStop(final String instanceId){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                AmazonEC2 ec2 = getEC2Client();

                StopInstancesRequest request = new StopInstancesRequest();
                request.withInstanceIds(instanceId);

                StopInstancesResult stopInstancesResult = ec2.stopInstances(request);

                postEvent(EC2ServiceEvent.Type.REQUEST_STOP, stopInstancesResult);

                return null;

            }

        }.execute();

    }

    /**
     * Try to reboot a instance
     * @param instanceId instance to reboot
     */
    public static void requestReboot(final String instanceId){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                AmazonEC2 ec2 = getEC2Client();

                RebootInstancesRequest request = new RebootInstancesRequest();
                request.withInstanceIds(instanceId);

                ec2.rebootInstances(request);

                postEvent(EC2ServiceEvent.Type.REQUEST_REBOOT, null);

                return null;

            }

        }.execute();

    }

    /**
     * Try to terminate a instance
     * @param instanceId instance to terminate
     */
    public static void requestTerminate(final String instanceId){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                AmazonEC2 ec2 = getEC2Client();

                TerminateInstancesRequest request = new TerminateInstancesRequest();
                request.withInstanceIds(instanceId);

                TerminateInstancesResult terminateInstancesResult = ec2.terminateInstances(request);

                postEvent(EC2ServiceEvent.Type.REQUEST_TERMINATE, terminateInstancesResult);

                return null;

            }

        }.execute();

    }

}
