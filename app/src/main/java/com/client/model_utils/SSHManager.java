package com.client.model_utils;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.util.IOUtils;
import com.google.cloud.android.speech.MainActivity;
import com.google.cloud.android.speech.R;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHManager {

    private Context CONTEXT;
    private static JSch jschSSHChannel;
    private String strUserName;
    private String strConnectionIP;
    private int intConnectionPort;
    private String strPassword;
    private static Session sesConnection;
    private int intTimeOut;
    private String strHostName;
    private byte[] privateKey;
    private boolean isConnectedToServer = false;

    public SSHManager(Context context, String userName, String password,
                      String connectionIP, String knownHostsFileName/*, byte[] privateKey*/) {

        CONTEXT = context;
        strUserName = userName;
        strPassword = password;
        strConnectionIP = connectionIP;
        strHostName = knownHostsFileName;
        this.privateKey = privateKey;
        intConnectionPort = 22;
        intTimeOut = 15000;

        connect();

        while (!isConnectedToServer) {}
    }

    public boolean connect() {

        //File file = new File("resources/raw/noon_ec2.pem");
        //String absolutePath = file.getAbsolutePath();
        //String absolutePath = CONTEXT.getResources().getResourceName(R.raw.noon_ec2);

        //S3Manager s3Manager = new S3Manager();
        //String absolutePath = s3Manager.getCertificateURL().toString();
        //Log.d("fileeeee", absolutePath);

        AssetManager assetManager = EC2Service.CONTEXT.getResources().getAssets();
        InputStream inputStream = null;

        File file = new File(EC2Service.CONTEXT.getFilesDir(), "noon_ec2.pem");

        try {

            InputStream in = assetManager.open("noon_ec2.pem");
            OutputStream out = new FileOutputStream(file);

            byte[] buffer = new byte[65536 * 2];
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();

            System.out.println(new String(buffer));

            File newPem = new File(Environment.getExternalStorageDirectory(), "ec2_auth.pem");

            try {
                FileOutputStream fos = new FileOutputStream(newPem);
                fos.write(buffer);
                fos.close();
                System.out.println("NEW PEM FILE SAVED!!!!!!!!!!!!!!!!!!!!!!!!!!");
            } catch (Exception e) { e.printStackTrace(); }

            jschSSHChannel = new JSch();
            //  jschSSHChannel.addIdentity("noon_ec2.pem", privateKey);
            // String name =  getResources().getResourceName(R.raw.noon_ec2);
            jschSSHChannel.addIdentity(Environment.getExternalStorageDirectory() + "/ec2_auth.pem");

            ///home/anas/Final Year Project/Noon/app/src/main/res/raw/noon_ec2.pem
            Log.d("SSHManager", "CREATING SESSION");

            System.out.printf("USERNAME:\t%s\nHOSTNAME:\t%s\nPORT:\t%s\n",
                    strUserName, strHostName, String.valueOf(intConnectionPort));

            sesConnection = jschSSHChannel.getSession(strUserName, strHostName, intConnectionPort);

            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            sesConnection.setConfig(prop);

            Log.d("SSHManager", "CONNECTING TO SESSION");

            sesConnection.connect(intTimeOut);

            isConnectedToServer = true;
            return true;

        } catch (Exception jschX) {
            jschX.printStackTrace();
        }
        return false;
    }


    public String sendCommand(String command) {
        StringBuilder outputBuffer = new StringBuilder();

        if (sesConnection != null) {
            try {
                Channel channel = sesConnection.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                InputStream commandOutput = channel.getInputStream();
                channel.connect();
                int readByte = commandOutput.read();

                while (readByte != 0xffffffff) {
                    outputBuffer.append((char) readByte);
                    readByte = commandOutput.read();
                }

                channel.disconnect();
            } catch (Exception ioX) {
                ioX.printStackTrace();
                return null;
            }

            return outputBuffer.toString();
        } else {
            return "SESSION IS NULL";
        }
    }

    public void close() {
        sesConnection.disconnect();
    }


}
