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
        app.get("/design", ctx -> ctx.render("design.html"));
        app.post("createuser", ctx -> createUser(ctx, connectionPool));
        app.get("/seller", ctx -> ctx.render("sellerdashboard.html"));
        app.get("/customerprofile", ctx -> CustomerProfileController.showProfile(ctx, connectionPool));

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
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        try {
            User user = UserMapper.login(email, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);

            Integer roleId = user.getRoleId();

            if (roleId == null) {
                ctx.redirect("/customerprofile");
            } else if (roleId == 1) {
                ctx.redirect("/seller");
            } else if (roleId == 2) {
                ctx.redirect("/admin");
            } else {
                ctx.redirect("/customerprofile"); // fallback
            }

        } catch (DatabaseException e) {
            ctx.attribute("message", "Forkert brugernavn eller adgangskode");
            ctx.render("login.html");
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




