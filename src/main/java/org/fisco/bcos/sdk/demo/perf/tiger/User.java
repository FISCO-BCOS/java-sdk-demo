package org.fisco.bcos.sdk.demo.perf.tiger;

class User {
    public String getOpenID() {
        return openID;
    }

    public void setOpenID(String openID) {
        this.openID = openID;
    }

    public int getTigerID() {
        return tigerID;
    }

    public void setTigerID(int tigerID) {
        this.tigerID = tigerID;
    }

    private String openID;
    private int tigerID;
}
