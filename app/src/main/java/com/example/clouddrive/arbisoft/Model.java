package com.example.clouddrive.arbisoft;

public class Model {
    public int shift; // will change on shift
    public String name;
    public int businessValue;

    public Model(String name, int businessValue) {
        this.name = name;
        this.businessValue = businessValue;
    }

    // incase we get businessValue in String, we'll parse the integer value
    public Model(String name, String businessValue) {
        this.name = name;
        this.businessValue = Integer.parseInt(businessValue);
    }

    public boolean compare(Model model) {
        return this.businessValue < model.businessValue;
    }

}
