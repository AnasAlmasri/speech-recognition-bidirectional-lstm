package com.client.model_utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

public class SSHRequestManager {

    String password = "MIIEowIBAAKCAQEAiadPofXiYjGKfwBjlTLMPFMSeDeNPHwrecyMtNTK9/caGOjHYKf1gskOPiOw" +
            "4zChNgueVOUbFZQjMsq18aiZWWKv9E8pVx1AteIz74nXryqbMGrgun7zG1V+dNsOPCeHsB5ggovP" +
            "HlRkU97NwWyMymabB8Yrt1YSDZsXr65cyeCW+6/7bZ3ruMvbBXuDC+PPjqnbtWFYiNFaCCXpbNzj" +
            "poemWSQVAlfiPwcm5/179un0714R66DXmAGUHVucqL7Esuo/fhkFGX7xf2TagBZfY0MwVqARvYXy" +
            "dRS5n9Ho7bB4zRi+vKMzjveQShHCVXij8mtQLoWYYo8iD09U+tCyHQIDAQABAoIBAGXseqxDskJe" +
            "yFvNX+8DU/J+2Dgnee6FSnO/H5htz3PXhX1YLvjEpKcYU/Fy3/D4+wVXiP73PRy3OXDMY/fxjMqA" +
            "piRR0RxtzpJeUXJWMFvFQm+RU6EDiuJ8ZUwFPv8Pjgq8E13WQ6ADgN4RJWzJAV6EptoUxB3WHzvv" +
            "0JQGmTHoeZKfEjKLK5XTpMiovPUV5iAz5eOjFjLLApznXyftUFrDTxJHqfOmTIHb19IviKlWC65H" +
            "HPnEliZwglDDlk+Q7T6w72t06NCpJ42dCBBHYh6FRLpE5mV9b8cmsqRUhY4SxbDUJg/ZYQ0kbH98" +
            "DqJtBnND9hHG6QQDmPJ0HZvw88ECgYEA8HkuEQbZDhMrwCO5nwumhuf1ePG1+cQMwfWPdVjfoZFk" +
            "CQ/hYF9C8LK6qWGDaJR9UKwwN46idOXx4z59MfOiff/SGpWEZ9TBUsb7EpCksNyvsJT9mA8MyiWr" +
            "gfvlvAY79KxCxR6s0nPAWV7bfnZCqIlKDsiQq6aAtMBnNeBZLKkCgYEAkoqbd0fxM1vARZyr8kxs" +
            "iRiv/EuHsq6FHI1OlwbfMlxx4gdFPJSP763/OgppGDkKGZ63PfiU6u+PUqoE5sAAzrnFrTckUXC/" +
            "0EDjTJDLJwpMc8iMLW1U/gokER+jUjA5VijS5mdSTkqCG5TeriZpPrbYw0MMeHJX8QxdwThKrlUC" +
            "gYBdHI7ZyO0CpimqnCqjir9QPUCvM+hibacC8zI5HdB5nvI9EAkUZ85jpNDiBP/83oInWoTQ/kmp" +
            "OeAPPYL8dHnWj9eqs3oxOhAhHSraZWdflBPzK72Fw04Qd8WQ7xhlVYShmJHCAb6pPmicMj6LGP92" +
            "grJKRx69WBs94cIU+mmNAQKBgEn+eynrxbstJRUBW0FpnZRMRvCaItbykHwuTfSsn58KVubOzQMd" +
            "8OARd8KS5yY4BwWQwu4jyu4mMLSI7Rim8sFvxIUGRuxohjJd0Wgj5LC93oTISx/VCQmNjVCoLQuf" +
            "FhSPYI6+tSybKr9KmHZPJu1n3mQaQv2+nvE0UVLzXaJRAoGBAKNJHFdBsAeMyR2iq+WY8vGK4p1/" +
            "GwuGT4M4zTVyqVMZdloluDrBihNWKUHcqYArgyd7UbPPg4iVu7ZCpDjJ4ErrPGxH0FAU4e/cPPWl" +
            "Xhv8gQBkMM+58Iz09TuLnx7bw7wG6y7slvxdtMOflFYRAZp58n/0g2PUZBOgpx0QgPH3";


    String TAG = "SSHRequestManager";
    Context CONTEXT;
    String instanceIP;
    JSch jsch;

    public SSHRequestManager(Context context, String instanceIP) {

        CONTEXT = context;
        this.instanceIP = instanceIP;

        new SSHRequestTask().execute();

    }

    private class SSHRequestTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            String username = "ec2-user";
            String hostname = instanceIP;

            AssetManager assetManager = CONTEXT.getAssets();
            //InputStream inputStream = assetManager.open("noon_ec2.pem");

            int port = 22;

            try {

                String privateKey = "noon_ec2.pem";
                //Log.d(TAG, privateKey);

                jsch = new JSch();
                //jsch.addIdentity(inputStream);

                Session session = jsch.getSession(username, hostname, port);
                //session.setPassword(password);

                // Avoid asking for key confirmation
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);

                session.connect();

                // SSH Channel
                ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                channelssh.setOutputStream(baos);

                // Execute command
                channelssh.setCommand("ls");
                channelssh.connect();
                channelssh.disconnect();

                Log.d("return string", baos.toString());
                /*
                jsch = new JSch();


                String privateKey = "noon_ec2.pem";

                jsch.addIdentity(privateKey);
                Log.d(TAG, "identity added ");

                Session session = jsch.getSession(user, host, port);
                Log.d(TAG, "session created.");

                // disabling StrictHostKeyChecking may help to make connection but makes it insecure
                // see http://stackoverflow.com/questions/30178936/jsch-sftp-security-with-session-setconfigstricthostkeychecking-no
                //
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);

                session.connect();

                Channel channel = session.openChannel("shell");

                // Enable agent-forwarding.
                //((ChannelShell)channel).setAgentForwarding(true);

                channel.setInputStream(System.in);

                channel.setOutputStream(System.out);

                //channel.connect();
                channel.connect(3*1000);
                */
            } catch(Exception e) { e.printStackTrace(); }

            return null;
        }
    }

    /*
    private void initializeChannel() {
        jsch = new JSch();
        jsch.addIdentity("my key",
                privateKey.getBytes(),
                publicKey.getBytes(),
                passphrase.getBytes());

    }

    for (Reservation reservation : reservations) {
        instances.addAll(reservation.getInstances());
        //obtain public DNs of the instance that was just created
        if(reservation.getInstances().get(0).getPrivateIpAddress()!= null &&
                reservation.getInstances().get(0).getInstanceId().equals(createdInstanceId))
        {
            publicDNS = reservation.getInstances().get(0).getPublicDnsName();
            publicIP = reservation.getInstances().get(0).getPublicIpAddress();
            System.out.println("Public DNS: "+publicDNS);
            System.out.println("Public IP: "+publicIP);
        }
    }
    */

}
