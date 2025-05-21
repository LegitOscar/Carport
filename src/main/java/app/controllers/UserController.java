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
import jakarta.servlet.http.HttpSession;
import org.jetbrains.annotations.NotNull;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UserController {

    private final ConnectionPool connectionPool;

    public UserController(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {

        app.post("login", ctx -> login(ctx, connectionPool));
        app.get("logout", ctx -> logout(ctx));
        app.get("/login", ctx -> ctx.render("login.html"));
        app.get("createuser", ctx -> ctx.render("createuser.html"));
        app.get("/design", ctx -> ctx.render("design.html"));
        app.get("/customerprofile", ctx -> CustomerProfileController.showProfile(ctx, connectionPool));
        app.post("createuser", ctx -> UserController.createUser(ctx, connectionPool));
        app.post("/profile/edit", ctx -> CustomerProfileController.editProfile(ctx, connectionPool));
        app.post("/profile/update", ctx -> CustomerProfileController.updateProfile(ctx, connectionPool));
        app.get("/getcity", ctx -> UserController.getCityByPostcode(ctx, connectionPool));


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

        ctx.render("login.html");
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
            if (user == null) {
                ctx.attribute("message", "Forkert brugernavn eller adgangskode");
                ctx.render("login.html");
                return;
            }

            ctx.sessionAttribute("currentUser", user);
            ctx.sessionAttribute("workerId", user.getId());  // Store user ID in session for workers/sellers

            Integer roleId = user.getRoleId();
            System.out.println("User logged in with roleId: " + roleId);

            if (roleId == null) {
                ctx.redirect("/customerprofile");
            } else if (roleId == 1) {
                ctx.redirect("/admin");
            } else if (roleId == 2) {
                ctx.redirect("/sellerdashboard");
            } else {
                ctx.redirect("/customerprofile");  // fallback
            }

        } catch (DatabaseException e) {
            ctx.attribute("message", "Forkert brugernavn eller adgangskode");
            ctx.render("login.html");
        }
    }


    public static void getCityByPostcode(Context ctx, ConnectionPool connectionPool) {
        String postcodeParam = ctx.queryParam("postcode");

        if (postcodeParam == null || postcodeParam.isEmpty()) {
            ctx.status(400).result("Postnummer mangler.");
            return;
        }

        int postcode;
        try {
            postcode = Integer.parseInt(postcodeParam);
        } catch (NumberFormatException e) {
            ctx.status(400).result("Ugyldigt postnummer format.");
            return;
        }

        String sql = "SELECT city FROM postcode WHERE postcode = ?";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, postcode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String city = rs.getString("city");
                ctx.status(200).result(city);
            } else {
                ctx.status(404).result("Postnummer ikke fundet.");
            }

        } catch (SQLException e) {
            ctx.status(500).result("Fejl ved opslag af postnummer: " + e.getMessage());
            e.printStackTrace();  // Log full error stack trace to your server console/log
        }
    }



}