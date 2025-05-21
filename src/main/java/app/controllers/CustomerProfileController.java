package app.controllers;

import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import io.javalin.http.Context;

public class CustomerProfileController {

    public static void showProfile(Context ctx, ConnectionPool connectionPool) {
        User user = ctx.sessionAttribute("currentUser");

        if (user == null) {
            ctx.redirect("/");
            return;
        }

        try {
            User profile = UserMapper.getCustomerProfileById(user.getId(), connectionPool);
            ctx.attribute("profile", profile);
            ctx.render("customerprofile.html");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Server error ved hentning af kundeprofil: " + e.getMessage());
        }
    }

    public static void editProfile(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int userId = Integer.parseInt(ctx.formParam("id"));
        User user = UserMapper.getCustomerProfileById(userId, connectionPool);
        ctx.attribute("profile", user);
        ctx.attribute("mode", "edit");
        ctx.render("customerprofile.html");
    }

    public static void updateProfile(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        int id = Integer.parseInt(ctx.formParam("id"));
        String name = ctx.formParam("name");
        String address = ctx.formParam("address");
        int postcode = Integer.parseInt(ctx.formParam("postcode"));
        String city = ctx.formParam("city");
        int phone = Integer.parseInt(ctx.formParam("phone"));
        String email = ctx.formParam("email");


        String validCity = UserMapper.getCityByPostcode(postcode, connectionPool);

        if (validCity == null || validCity.isEmpty()) {
            User currentUserData = UserMapper.getCustomerProfileById(id, connectionPool);
            ctx.attribute("profile", currentUserData);
            ctx.attribute("mode", "edit");
            ctx.attribute("message", "Du skal vælge en gyldig postkode.");
            ctx.render("customerprofile.html");
            return;
        }

        if (!validCity.equals(city)) {
            city = validCity;
        }

        User updatedUser = new User(name, address, postcode, city, phone, email, "dummy"); // password not updated here
        updatedUser.setId(id);
        UserMapper.updateUser(updatedUser, connectionPool);

        ctx.attribute("profile", updatedUser);
        ctx.attribute("mode", "view");
        ctx.render("customerprofile.html");
    }
}
