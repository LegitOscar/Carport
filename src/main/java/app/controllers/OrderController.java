package app.controllers;

import app.entities.User;
import app.persistence.ConnectionPool;
import io.javalin.Javalin;

import io.javalin.http.Context;

public class OrderController {
    public static void addRoutes(Javalin app, ConnectionPool connectionPool)
    {
        app.post("addorder", ctx -> addOrder(ctx, connectionPool));
        app.post("deleteorder", ctx -> deleteOrder(ctx, connectionPool));
        app.post("editorder", ctx -> editOrder(ctx, connectionPool));
        app.post("updateorder", ctx -> updateOrder(ctx, connectionPool));

    }

    private static void editOrder(Context ctx, ConnectionPool connectionPool){
        //User user = ctx.sessionAttribute("currentUser");
    }

    private static void deleteOrder(Context ctx, ConnectionPool connectionPool){
        //User user = ctx.sessionAttribute("currentUser");
    }

    private static void addOrder(Context ctx, ConnectionPool connectionPool){
        //Todo find ud af hvad order består af.
    }

    private static void updateOrder(Context ctx, ConnectionPool connectionPool) {
        //User user = ctx.sessionAttribute("currentUser");
    }

}
