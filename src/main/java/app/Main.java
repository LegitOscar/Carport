package app;

import app.config.SessionConfig;
import app.config.ThymeleafConfig;
import app.controllers.CarportController;
import app.controllers.CustomerProfileController;
import app.controllers.OrderController;
import app.controllers.UserController;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.CarportMapper;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import io.javalin.http.staticfiles.Location;

import java.util.List;


public class Main {



    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://164.90.223.15:5432/%s?currentSchema=public";
    private static final String DB = "carport";


    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args) throws DatabaseException {

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.directory = "/public";
                staticFiles.hostedPath = "/";
                staticFiles.location = Location.CLASSPATH;
            });
            config.jetty.modifyServletContextHandler(handler ->
                    handler.setSessionHandler(SessionConfig.sessionConfig()));
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
        }).start(7070);

        // Routing
        app.get("/", ctx ->  ctx.render("index.html"));
        UserController.addRoutes(app,connectionPool);
        OrderController.addRoutes(app, connectionPool);
        CarportController.addRoutes(app, connectionPool);
        app.get("/pay/{orderId}", ctx -> CustomerProfileController.showPaymentPage(ctx, connectionPool));
        app.post("/pay/{orderId}", ctx -> CustomerProfileController.processPayment(ctx, connectionPool));

        }
    }

