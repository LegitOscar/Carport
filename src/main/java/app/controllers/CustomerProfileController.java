package app.controllers;

import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.persistence.UserMapper;
import io.javalin.http.Context;

import java.util.List;

public class CustomerProfileController {

public static void showProfile(Context ctx, ConnectionPool connectionPool) {

    User user = ctx.sessionAttribute("currentUser");


    if (user == null) {
        System.out.println("[DEBUG] No user in session — redirecting to home.");
        ctx.redirect("/");
        return;
    }

    try {

        User profile = UserMapper.getCustomerProfileById(user.getId(), connectionPool);
        ctx.attribute("profile", profile);

        List<Order> orders = OrderMapper.getAllOrdersPerUser(user.getId(), connectionPool);
        ctx.attribute("orders", orders);

        System.out.println("[DEBUG] Loaded profile for user ID: " + user.getId());
        System.out.println("[DEBUG] Number of orders: " + orders.size());

        ctx.render("customerprofile.html");

    } catch (Exception e) {
        e.printStackTrace();
        ctx.status(500).result("Serverfejl ved hentning af kundeprofil: " + e.getMessage());
    }
}


    public static void editProfile(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        User user = ctx.sessionAttribute("currentUser");
        if (user == null) {
            ctx.redirect("/");
            return;
        }

        // Fetch profile and orders
        User profile = UserMapper.getCustomerProfileById(user.getId(), connectionPool);
        List<Order> orders = OrderMapper.getAllOrdersPerUser(user.getId(), connectionPool);

        // Set all required attributes for Thymeleaf
        ctx.attribute("profile", profile);
        ctx.attribute("orders", orders);
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
            List<Order> orders = OrderMapper.getAllOrdersPerUser(id, connectionPool);  // Fetch orders again
            ctx.attribute("profile", currentUserData);
            ctx.attribute("orders", orders); // Include orders even when showing validation error
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

        List<Order> orders = OrderMapper.getAllOrdersPerUser(id, connectionPool); // ✅ Add this line
        ctx.attribute("profile", updatedUser);
        ctx.attribute("orders", orders); // ✅ And this
        ctx.attribute("mode", "view");
        ctx.render("customerprofile.html");
    }


    public static void showUserOrders(Context ctx, ConnectionPool connectionPool) {
        User user = ctx.sessionAttribute("currentUser");

        if (user == null) {
            ctx.status(401).result("Du er ikke logget ind.");
            return;
        }

        try {
            List<Order> orders = OrderMapper.getAllOrdersPerUser(user.getId(), connectionPool);
            ctx.attribute("orders", orders);
            ctx.render("orders.html");  // Or your template for showing orders
        } catch (DatabaseException e) {
            ctx.status(500).result("Fejl ved hentning af ordrer: " + e.getMessage());
        }
    }

}