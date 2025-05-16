package app.controllers;

import app.entities.CustomerProfile;
import app.entities.User;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import io.javalin.http.Context; // ✅ Correct import

public class CustomerProfileController {

    public static void showProfile(Context ctx, ConnectionPool connectionPool) {
        User user = ctx.sessionAttribute("currentUser");

        if (user == null){
            ctx.redirect("/");
            return;
        }

        try {
            CustomerProfile profile = UserMapper.getCustomerProfileById(user.getUserId(), connectionPool);
            ctx.attribute("profile", profile);
            ctx.render("customerprofile.html");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Server error ved hentning af kundeprofil: " + e.getMessage());
        }
    }
}
