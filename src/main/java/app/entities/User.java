package app.entities;

public class User {

    private int userId;
    private String userName;
    private String password;
    private Integer roleId;


    public User(int userId, String userName, String passwordt, Integer roleId) {

    this.userId = userId;
    this.userName = userName;
    this.password = password;
    this.roleId = roleId;

    }

    public int getUserId()
    {
        return userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getPassword()
    {
        return password;
    }
    public Integer getRoleId() { return roleId; }


    @Override
    public String toString()
    {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}
