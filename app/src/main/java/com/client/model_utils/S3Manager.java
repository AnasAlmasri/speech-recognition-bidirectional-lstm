package com.client.model_utils;

import android.content.Context;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import java.net.URL;

public class S3Manager {

    private AmazonS3Client s3Client;

    public S3Manager() {
        s3Client = new AmazonS3Client(CredentialsService.getCredentials());

    }

    public boolean checkCertificateAvailability() {

        return false;
    }

    public URL getCertificateURL() {
        return s3Client.getUrl("noon-speech-bucket", "noon_ec2.pem");
    }
}
