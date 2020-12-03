package com.example.thumb;

public class UserInformation {
    private String name;
    private String lastName;
    private String id;
    private String personalNumber;
    private String releseDate;


    public UserInformation(){
    }
    public UserInformation(String name, String lastName, String id,String personalNumber,String releseDate) {
        this.name = name;
        this.lastName = lastName;
        this.id = id;
        this.personalNumber = personalNumber;
        this.releseDate=releseDate;

    }
    public String getName() {
        return name;
    }
    public String getLastName() {
        return lastName;
    }
    public String getId() {
        return id;
    }
    public String getPersonalNumber() {
        return personalNumber;
    }
    public String getReleseDate() {
        return releseDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String LastName) {
        this.lastName = lastName;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setPersonalNumber(String PersonalNumber) {
        this.personalNumber = PersonalNumber;
    }
    public void setReleseDate(String ReleseDate) {
        this.releseDate = ReleseDate;
    }







}
