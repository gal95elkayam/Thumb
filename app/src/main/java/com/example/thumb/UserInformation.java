package com.example.thumb;

public class UserInformation {
    private String name;
    private String lastName;
    private String id;
    private String phoneNumber;
    private String typeUser;
    private String  age;


    public UserInformation(){
    }
    public UserInformation(String name, String lastName, String id,String phoneNumber ,String typeUser) {
        this.name = name;
        this.lastName = lastName;
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.typeUser=typeUser;
    }
    public UserInformation(String name, String lastName, String id,String phoneNumber ,String typeUser, String age) {
        this.name = name;
        this.lastName = lastName;
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.typeUser=typeUser;
        this.age=age;
    }
    public String getTypeUser(){return typeUser;}
    public String getName() {
        return name;
    }
    public String getLastName() {
        return lastName;
    }
    public String getId() {
        return id;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

}
