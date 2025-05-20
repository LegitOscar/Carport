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
        app.get("/seller", ctx -> ctx.render("sellerdashboard.html"));
        app.get("/customerprofile", ctx -> CustomerProfileController.showProfile(ctx, connectionPool));
        app.post("createuser", ctx -> UserController.createUser(ctx, connectionPool));



    }

    public static void createUser(Context ctx, ConnectionPool connectionPool) {
        String navn = ctx.formParam("navn");
        String adresse = ctx.formParam("adresse");
        int postnummer = Integer.parseInt(ctx.formParam("postnummer"));
        String by = ctx.formParam("by");
        int telefon = Integer.parseInt(ctx.formParam("telefon"));
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");

        if (!password1.equals(password2)) {
            ctx.attribute("message", "Passwords do not match.");
            ctx.render("createuser.html");
            return;
        }

        User user = new User(navn, adresse, postnummer, by, telefon, email, password1);
        UserMapper userMapper = new UserMapper(connectionPool);

        try {
            userMapper.createUser(user);
            ctx.attribute("message", "Bruger oprettet!");
        } catch (Exception e) {
            ctx.attribute("message", "Fejl under oprettelse af bruger: " + e.getMessage());
        }

        ctx.render("createuser.html");
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




