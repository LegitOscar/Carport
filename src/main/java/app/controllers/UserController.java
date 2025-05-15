package app.controllers;


import app.entities.CustomerProfile;
import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class UserController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {

        app.post("login", ctx -> login(ctx, connectionPool));
        app.get("logout", ctx -> logout(ctx));
        app.get("/login", ctx -> ctx.render("login.html"));
        app.get("createuser", ctx -> ctx.render("createuser.html"));
        app.post("createuser", ctx -> createUser(ctx, connectionPool));
        app.get("/customerprofile", ctx -> {
            User user = ctx.sessionAttribute("currentUser");
            if (user == null){
                ctx.redirect("/");
                return;
            }

            try{
                CustomerProfile profile = UserMapper.getCustomerProfileById(user.getUserId(), connectionPool);
                ctx.attribute("profile", profile);
                ctx.render("customerprofile.html");
            } catch (DatabaseException e){
                ctx.status(500).result("Error retrieving profile: " + e.getMessage());
            }
        });
        app.get("/testprofile", ctx -> {
            int testCustomerId = 6;

            try{
                CustomerProfile profile = UserMapper.getCustomerProfileById(testCustomerId, connectionPool);
                String expectedEmail = "jon@example.com";
                String expectedPassword = "1234";


                if (profile.getEmail().trim().equalsIgnoreCase(expectedEmail.trim()) && profile.getPassword().trim().equals(expectedPassword.trim())){
                    User testUser = new User(testCustomerId, profile.getEmail(), expectedPassword, "customer");
                    ctx.sessionAttribute("currentUser", testUser);

                    ctx.redirect("/customerprofile");
                } else {
                    ctx.status(401).result("Email or password is incorrect");
                }
            } catch (DatabaseException e){
                ctx.status(500).result("Error retrieving user: " + e.getMessage());
            }
        });
        app.get("/sellerdashboard", ctx -> {
            User user = ctx.sessionAttribute("currentUser");

            if (user == null || !user.getRole().equals("seller")){
                ctx.redirect("/");
                return;
            }

            try {
                List<Order> orders = OrderMapper.getAllOrders(connectionPool);
                ctx.attribute("orders", orders);
                ctx.render("sellerdashboard.html");
            } catch (DatabaseException e){
                ctx.status(500).result("Error retrieving orders: " + e.getMessage());

            }
        });

        app.get("/testsellerlogin", ctx ->{
            User testSeller = new User(99, "seller@example.com","1234", "seller");
            ctx.sessionAttribute("currentUser", testSeller);

            ctx.redirect("/sellerdashboard");
                });
    }

    public static void createUser(Context ctx, ConnectionPool connectionPool) {

        String username = ctx.formParam("username");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");

        if (password1.equals(password2)) {
            try {
                createUser(username, password1, password2, connectionPool);
                ctx.attribute("message", "Du er hermed blevet oprettet med brugernavnet" + username + ". Du kan nu logge på.");
                ctx.render("index.html");
            }catch (IllegalArgumentException e) {
                ctx.attribute("message", "Dine to passwords stemmer ikke overens.");
                ctx.render("createuser.html");
            }

        } else {
            ctx.attribute("message", "Dine to passwords stemmer ikke overens, sørg for at du har stavet korrekt og prøv igen, eller log ind");
            ctx.render("createuser.html");
        }
    }

    public static void createUser(String username, String password1, String password2, ConnectionPool connectionPool) {
        if (!password1.equals(password2)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        try {
            UserMapper.createUser(username, password1, "customer", connectionPool);
            // You can add logging here if needed
        } catch (DatabaseException e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }


    private static void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");

    }

    public static void login(Context ctx, ConnectionPool connectionPool) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");



        try {
            User user = UserMapper.login(username, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);

            if (user.getRole().equals("seller")){
                ctx.redirect("/sellerdashboard");
            }else{
                ctx.redirect("/customerprofile");
            }

        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void displayCustomerProfile(Context ctx, ConnectionPool connectionPool){
        User currentUser = ctx.sessionAttribute("currentUser");

        if (currentUser == null){
            ctx.redirect("/login");
            return;
        }

        try {
            int customerId = currentUser.getUserId();
            CustomerProfile profile = UserMapper.getCustomerProfileById(customerId, connectionPool);
            ctx.attribute("profile", profile);
            ctx.render("customerprofile.html");
        } catch (DatabaseException e){
            ctx.attribute("message", "Error getting the customer profile.");
            ctx.render("error.html");
        }

    }

}




