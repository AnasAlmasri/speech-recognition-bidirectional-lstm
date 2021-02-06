package com.client.model_utils;

/**
 * Created by neymoura on 10/09/17.
 */

public class EC2ServiceEvent {

    public enum Type{
        REQUEST_INSTANCE,
        REQUEST_IMAGES,
        REQUEST_KEY_PAIRS,
        REQUEST_SECURITY_GROUPS,
        REQUEST_INSTANCES,
        REQUEST_START,
        REQUEST_STOP,
        REQUEST_REBOOT,
        REQUEST_TERMINATE
    }

    private Type type;
    private Object data;

    public EC2ServiceEvent(Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

}
