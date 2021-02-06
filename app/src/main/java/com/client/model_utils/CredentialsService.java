package com.client.model_utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

/**
 * Created by neymoura on 04/09/17.
 */

class CredentialsService {

    static AWSCredentials getCredentials(){

        AWSCredentials basicCredentials = new BasicAWSCredentials(
                "cred1",
                "cred2");

        return  basicCredentials;

    }

}
