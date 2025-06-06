package app.controllers;


import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.*;
import app.services.Calculator;
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
        app.get("/orderSite", ctx -> ctx.render("orderSite.html"));
        app.get("/orderConfirmation", ctx -> ctx.render("orderConfirmation.html"));
        app.get("/customerprofile", ctx -> CustomerProfileController.showProfile(ctx, connectionPool));
        app.post("/createuser", ctx -> UserController.createUser(ctx, connectionPool));
        app.post("/profile/edit", ctx -> CustomerProfileController.editProfile(ctx, connectionPool));;
        app.get("/getcity", ctx -> UserController.getCityByPostcode(ctx, connectionPool));
        app.get("/admin", ctx -> UserController.showAdminPage(ctx, connectionPool));
        app.post("/admin/update-role", ctx -> UserController.updateUserRole(ctx, connectionPool));
        app.post("/createuserorder", ctx -> UserController.createUserOrder(ctx, connectionPool));
        app.get("/admin/create-worker", ctx -> ctx.render("createworker.html"));
        app.post("/admin/create-worker", ctx -> createWorkerFromAdmin(ctx, connectionPool));
        app.post("/admin/delete-worker", ctx -> UserController.deleteWorkerFromAdmin(ctx, connectionPool));
    }

    public static void createUser(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
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
        User loggedInUser = UserMapper.login(email, password1, connectionPool);
        ctx.sessionAttribute("currentUser", loggedInUser);
        ctx.redirect("/customerprofile");
    }

    public static void createUserOrder(Context ctx, ConnectionPool connectionPool) {
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
                ctx.render("orderSite3.html");
                return;
            }
            User user = new User(navn, adresse, postnummer, by, telefon, email, password1);
            UserMapper userMapper = new UserMapper(connectionPool);
            try {
                userMapper.createUser(user);
                ctx.attribute("message", "Bruger oprettet!");
            } catch (Exception e) {
                ctx.attribute("message", "Fejl under oprettelse af bruger: " + e.getMessage());
                ctx.render("orderSite3.html");
                return;
            }

            try {
                User loggedInUser = UserMapper.login(email, password1, connectionPool);
                ctx.sessionAttribute("currentUser", loggedInUser);

                Carport carport = ctx.sessionAttribute("pendingCarport");
                if (carport == null) {
                    ctx.status(400).result("Ingen carport fundet i session");
                    return;
                }

                CarportMapper.createCarport(carport, connectionPool);
                Order order = OrderMapper.createOrder(loggedInUser, carport.getCarportId(), connectionPool);
                List<WoodVariant> woodVariants = WoodVariantMapper.getAllWoodVariants(connectionPool);
                List<FittingsAndScrews> fittingsAndScrews = FittingsAndScrewsMapper.getAllFittingsAndScrews(connectionPool);
                Calculator calculator = new Calculator(woodVariants);
                List<OrderItem> orderItems = calculator.calculateMaterials(carport, order, fittingsAndScrews);

                for (OrderItem item : orderItems) {
                    OrderItemMapper.insertOrderItem(item, connectionPool);
                }

                double totalPrice = calculator.calculateTotalPrice(orderItems);
                order.setTotalPrice(totalPrice);
                OrderMapper.updateTotalPrice(order.getOrderId(), totalPrice, connectionPool);

                ctx.sessionAttribute("totalPrice", totalPrice);
                ctx.attribute("order", order);
                ctx.attribute("carport", carport);
                ctx.attribute("orderItems", orderItems);
                ctx.redirect("/orderConfirmation");

            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Fejl under ordreoprettelse: " + e.getMessage());
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
            if (user == null) {
                ctx.attribute("message", "Forkert brugernavn eller adgangskode");
                ctx.render("login.html");
                return;
            }
            ctx.sessionAttribute("currentUser", user);
            ctx.sessionAttribute("workerId", user.getId());
            Integer roleId = user.getRoleId();
            System.out.println("User logged in with roleId: " + roleId);
            if (roleId == null) {
                ctx.redirect("/customerprofile");
            } else {
                switch (roleId){
                    case 1:
                        ctx.redirect("/customerprofile");
                        break;
                    case 2:
                        ctx.redirect("/sellerdashboard");
                        break;
                    case 3:
                        ctx.redirect("/admin");
                        break;
                    default:
                        ctx.redirect("/index");
                        break;
                }
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
            e.printStackTrace();
        }
    }

    public static void showAdminPage(Context ctx, ConnectionPool connectionPool) {
        User currentUser = ctx.sessionAttribute("currentUser");

        if (currentUser == null || currentUser.getRoleId() != 3) {
            ctx.redirect("/login");
            return;
        }

        try {
            // Use getUsersByRoleId instead of separate getAllCustomers/getAllWorkers methods
            List<User> customers = UserMapper.getUsersByRoleId(1, connectionPool);
            List<User> workers = UserMapper.getUsersByRoleId(2, connectionPool);
            List<User> admins = UserMapper.getUsersByRoleId(3, connectionPool);

            ctx.attribute("customers", customers);
            ctx.attribute("workers", workers);
            ctx.attribute("admins", admins);
            ctx.render("admin.html");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved hentning af brugere: " + e.getMessage());
        }
    }

    public static void updateUserRole(Context ctx, ConnectionPool connectionPool) {
        User currentUser = ctx.sessionAttribute("currentUser");


        if (currentUser == null || currentUser.getRoleId() != 3) {
            ctx.redirect("/login");
            return;
        }

        int workerId = Integer.parseInt(ctx.formParam("workerId"));
        int newRoleId = Integer.parseInt(ctx.formParam("newRoleId"));

        if (workerId == currentUser.getId() && newRoleId != 3) {
            ctx.status(400).result("Du kan ikke fjerne din egen admin-rolle.");
            return;
        }

        try {
            UserMapper.updateWorkerRole(workerId, newRoleId, connectionPool);
            ctx.redirect("/admin");
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved opdatering af rolle: " + e.getMessage());
        }
    }
    public static void createWorkerFromAdmin(Context ctx, ConnectionPool connectionPool) {
        String name = ctx.formParam("name");
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");
        int phone = Integer.parseInt(ctx.formParam("phone"));
        int roleId = 2;

        if (!password1.equals(password2)) {
            ctx.attribute("message", "Passwords do not match.");
            ctx.render("createworker.html");
            return;
        }

        User user = new User(name, email, password1, phone, roleId);
        UserMapper userMapper = new UserMapper(connectionPool);

        try {
            userMapper.createWorker(user);
            ctx.redirect("/admin");
        } catch (SQLException e) {
            ctx.status(500).result("Fejl ved oprettelse af medarbejder: " + e.getMessage());
        }
    }

    public static void deleteWorkerFromAdmin(Context ctx, ConnectionPool connectionPool) {
        int workerId = Integer.parseInt(ctx.formParam("workerId"));
        UserMapper userMapper = new UserMapper(connectionPool);

        try {
            userMapper.deleteWorker(workerId);
            ctx.redirect("/admin");
        } catch (SQLException e) {
            ctx.status(500).result("Fejl ved sletning af medarbejder: " + e.getMessage());
        }
    }
}