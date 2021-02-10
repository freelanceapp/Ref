package com.apps.ref.models;

import java.io.Serializable;

public class PackageResponse implements Serializable {
    private String url;
    private String succes_url;
    private String canceled_url;
    private String declined_url;

    public String getUrl() {
        return url;
    }

    public String getSucces_url() {
        return succes_url;
    }

    public String getCanceled_url() {
        return canceled_url;
    }

    public String getDeclined_url() {
        return declined_url;
    }
}
