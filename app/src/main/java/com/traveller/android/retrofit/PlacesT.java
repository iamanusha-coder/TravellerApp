package com.traveller.android.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlacesT {

    @Expose
    @SerializedName("status")
    public String status;
    @Expose
    @SerializedName("results")
    public List<ResultsEntity> results;
    @Expose
    @SerializedName("html_attributions")
    public List<String> html_attributions;

    public static class ResultsEntity {
        @Expose
        @SerializedName("formatted_address")
        public String formatted_address;
        @Expose
        @SerializedName("types")
        public List<String> types;
        @Expose
        @SerializedName("reference")
        public String reference;
        @Expose
        @SerializedName("rating")
        public String rating;
        @Expose
        @SerializedName("place_id")
        public String place_id;
        @Expose
        @SerializedName("photos")
        public List<PhotosEntity> photos;
        @Expose
        @SerializedName("opening_hours")
        public Opening_hoursEntity opening_hours;
        @Expose
        @SerializedName("name")
        public String name;
        @Expose
        @SerializedName("id")
        public String id;
        @Expose
        @SerializedName("icon")
        public String icon;
        @Expose
        @SerializedName("geometry")
        public GeometryEntity geometry;
    }

    public static class PhotosEntity {
        @Expose
        @SerializedName("width")
        public int width;
        @Expose
        @SerializedName("photo_reference")
        public String photo_reference;
        @Expose
        @SerializedName("html_attributions")
        public List<String> html_attributions;
        @Expose
        @SerializedName("height")
        public int height;
    }

    public static class Opening_hoursEntity {
        @Expose
        @SerializedName("open_now")
        public boolean open_now;
    }

    public static class GeometryEntity {
        @Expose
        @SerializedName("location")
        public LocationEntity location;
    }

    public static class LocationEntity {
        @Expose
        @SerializedName("lng")
        public double lng;
        @Expose
        @SerializedName("lat")
        public double lat;
    }
}
