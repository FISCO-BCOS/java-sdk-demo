package org.fisco.bcos.sdk.demo.perf.dgms;

public class User {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return assetID;
    }

    public void setValue(int value) {
        this.assetID = value;
    }

    private String name;
    private int assetID;
}
