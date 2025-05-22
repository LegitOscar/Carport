package app.entities;

public class User {
    private int id; // for worker_id or customer_id
    private String name;
    private String address;
    private int postcode;
    private String city;
    private int phone;
    private String email;
    private String password;
    private Integer roleId; // nullable

    // Constructor for worker login
    public User(int id, String email, String password, Integer roleId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
    }

    public User(String name, String email, String password, int phone, int roleId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.roleId = roleId;
    }


    // Constructor for customer creation
    public User(String name, String address, int postcode, String city, int phone, String email, String password) {
        this.name = name;
        this.address = address;
        this.postcode = postcode;
        this.city = city;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    // Getters
    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPostcode() {
        return postcode;
    }

    public void setPostcode(int postcode){ this.postcode = this.postcode;}

    public String getCity() {
        return city;
    }

    public void setCity(String city) { this.city = city; }

    public int getPhone() {
        return phone;
    }

    public void setName(String name){ this.name = name; }

    public void setPhone(int phone){ this.phone = phone; }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId){
        this.roleId = roleId;
    }
}