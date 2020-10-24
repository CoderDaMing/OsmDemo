package com.ming.googlemap;

import android.os.Parcel;
import android.os.Parcelable;

public class Offline implements Parcelable {
    private String txtMapType;
    private String txtTime;
    private String txtSize;
    private String txtContent;
    private String txtPath;
    private double zoomMin = 1.0d;
    private double zoomMax = 19.0d;
    private double north = 0.0d;
    private double east = 0.0d;
    private double south = 0.0d;
    private double west = 0.0d;
    private boolean useDataConnection;


    public Offline() {

    }

    protected Offline(Parcel in) {
        txtMapType = in.readString();
        txtTime = in.readString();
        txtSize = in.readString();
        txtContent = in.readString();
        txtPath = in.readString();
        zoomMin = in.readDouble();
        zoomMax = in.readDouble();
        north = in.readDouble();
        east = in.readDouble();
        south = in.readDouble();
        west = in.readDouble();
        useDataConnection = in.readByte() != 0;
    }

    public static final Creator<Offline> CREATOR = new Creator<Offline>() {
        @Override
        public Offline createFromParcel(Parcel in) {
            return new Offline(in);
        }

        @Override
        public Offline[] newArray(int size) {
            return new Offline[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(txtMapType);
        parcel.writeString(txtTime);
        parcel.writeString(txtSize);
        parcel.writeString(txtContent);
        parcel.writeString(txtPath);
        parcel.writeDouble(zoomMin);
        parcel.writeDouble(zoomMax);
        parcel.writeDouble(north);
        parcel.writeDouble(east);
        parcel.writeDouble(south);
        parcel.writeDouble(west);
        parcel.writeByte((byte) (useDataConnection ? 1 : 0));
    }

    public String getTxtMapType() {
        return txtMapType;
    }

    public void setTxtMapType(String txtMapType) {
        this.txtMapType = txtMapType;
    }

    public String getTxtTime() {
        return txtTime;
    }

    public void setTxtTime(String txtTime) {
        this.txtTime = txtTime;
    }

    public String getTxtSize() {
        return txtSize;
    }

    public void setTxtSize(String txtSize) {
        this.txtSize = txtSize;
    }

    public String getTxtContent() {
        return txtContent;
    }

    public void setTxtContent(String txtContent) {
        this.txtContent = txtContent;
    }

    public String getTxtPath() {
        return txtPath;
    }

    public void setTxtPath(String txtPath) {
        this.txtPath = txtPath;
    }

    public double getZoomMin() {
        return zoomMin;
    }

    public void setZoomMin(double zoomMin) {
        this.zoomMin = zoomMin;
    }

    public double getZoomMax() {
        return zoomMax;
    }

    public void setZoomMax(double zoomMax) {
        this.zoomMax = zoomMax;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public double getEast() {
        return east;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public double getSouth() {
        return south;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public double getWest() {
        return west;
    }

    public void setWest(double west) {
        this.west = west;
    }

    public boolean isUseDataConnection() {
        return useDataConnection;
    }

    public void setUseDataConnection(boolean useDataConnection) {
        this.useDataConnection = useDataConnection;
    }
}
