package com.bartovapps.employeescanner.model;

/**
 * Created by BartovMoti on 10/02/16.
 */
public class Employee{

    private String tag_id;
    private transient long id; //transient will make gson to ignore it

    private String name;
    private String address;
    private String imageUri;
    private boolean arrived;

    public boolean isArrived() {
        return arrived;
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }



    public Employee() {
    }

    public Employee(String tag_id, String name, String address, String imageUri, boolean arrived) {
        this.tag_id = tag_id;
        this.name = name;
        this.address = address;
        this.arrived = arrived;
        this.imageUri = imageUri;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
