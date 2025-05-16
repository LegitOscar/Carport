package app.entities;

import java.security.PrivateKey;

public class CustomerProfile {
    private int customerId;
    private String name;
    private String email;
    private int phone;
    private String address;
    private int postcode;
    private String city;
    private String password;
    private String role;

    public int getCustomerId(){
        return customerId;
    }

    public void setCustomerId(int customerId){
        this.customerId = customerId;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public int getPhone(){
        return phone;
    }

    public void setPhone(int phone){
        this.phone = phone;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public int getPostcode(){
        return postcode;
    }

    public void setPostcode(int postcode){
        this.postcode = postcode;
    }

    public String getCity(){
        return city;
    }

    public void setCity(String city){
        this.city = city;
    }

    public String getPassword(){
        return password;
    }

    public void setRole(String role){ this.role = role; }

    public String getRole(){ return role; }

    public void setPassword(String password){
        this.password = password;
    }
}
