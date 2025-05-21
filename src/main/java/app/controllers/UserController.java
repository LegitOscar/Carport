package app.controllers;


import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class UserController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {

        app.post("login", ctx -> login(ctx, connectionPool));
        app.get("logout", ctx -> logout(ctx));
        app.get("login", ctx ->ctx.render("login.html"));
        app.get("createuser", ctx -> ctx.render("createuser.html"));
        app.post("createuser", ctx -> createUser(ctx, connectionPool));

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
            UserMapper.createUser(username, password1, "user", connectionPool);
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

        //Checks if the user already exists in the database, with the given username and password.

        try {
            User user = UserMapper.login(username, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);
            // If the user exists, then it will continue.
            //LOOK IN THE FOURTHINGSPLUS PROJECT, IT NEEDS TO BE ROUTED DIFFERENTLY, ACCORDING TO WHAT
            //WE CHOOSE TO NAME THE PATH AND METHODS
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

}




