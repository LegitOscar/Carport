
    package app.controllers;

    import app.entities.*;
    import app.exceptions.DatabaseException;
    import app.persistence.*;
   // import app.services.Calculator;
    import app.services.Calculator;
    import io.javalin.Javalin;
    import io.javalin.http.Context;


    import java.sql.*;
    import java.time.LocalDate;
    import java.util.ArrayList;
    import java.util.List;

    public class OrderController {

        private final ConnectionPool connectionPool;

        public OrderController(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
        }


        public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
            app.post("deleteorder", ctx -> deleteOrder(ctx, connectionPool));
            app.post("updateorder", ctx -> updateOrder(ctx, connectionPool));
            app.get("getorders", ctx -> getOrdersForUser(ctx, connectionPool));
            app.get("sellerdashboard", ctx -> getAllOrdersPerWorker(ctx, connectionPool));

        }


        private static void deleteOrder(Context ctx, ConnectionPool connectionPool) {
            try {
                int orderId = Integer.parseInt(ctx.formParam("orderId"));
                OrderMapper.deleteOrder(orderId, connectionPool);
                ctx.status(200).result("Ordre slettet med ID: " + orderId);
            } catch (NumberFormatException e) {
                ctx.status(400).result("Ugyldigt order ID");
            } catch (DatabaseException e) {
                ctx.status(500).result("Fejl ved sletning af ordre: " + e.getMessage());
            }
        }


        private static void createOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException, SQLException {
            User user = ctx.sessionAttribute("currentUser");
            if (user == null) {
                ctx.status(401).result("Du er ikke logget ind.");
                return;
            }

            int carportId = Integer.parseInt(ctx.formParam("carportId"));
            Order order = OrderMapper.createOrder(user, carportId, connectionPool);

            Carport carport = CarportMapper.getCarportById(carportId, connectionPool);

            List<WoodVariant> woodVariants = WoodVariantMapper.getAllWoodVariants(connectionPool);
            Calculator calculator = new Calculator(woodVariants);

            List<OrderItem> itemList = calculator.generateBillOfMaterials(carport);

            for (OrderItem item : itemList) {
                item.setOrder(order);
                OrderItemMapper.insertOrderItem(item, connectionPool);
            }

            ctx.status(201).result("Ordre og stykliste oprettet. Ordre ID: " + order.getOrderId());
        }


        private static void updateOrder(Context ctx, ConnectionPool connectionPool) {
            try {

                User currentUser = ctx.sessionAttribute("currentUser");

                if (currentUser == null) {
                    ctx.status(401).result("Ingen bruger er logget ind.");
                    return;
                }

                int customerId = currentUser.getId();


                int orderId = Integer.parseInt(ctx.formParam("orderId"));
                LocalDate orderDate = LocalDate.parse(ctx.formParam("orderDate"));
                double totalPrice = Double.parseDouble(ctx.formParam("totalPrice"));
                String orderStatus = ctx.formParam("orderStatus");


                int workerId = 0;
                int carportId = Integer.parseInt(ctx.formParam("carportId")); // Adjust based on your form


                Order order = new Order(orderId, orderDate, totalPrice, orderStatus, customerId, workerId, carportId);
                OrderMapper.updateOrder(order, connectionPool);

                ctx.status(200).result("Ordre opdateret");

            } catch (Exception e) {
                ctx.status(400).result("Fejl ved opdatering af ordre: " + e.getMessage());
            }
        }



        private static void getOrdersForUser(Context ctx, ConnectionPool connectionPool) {
            User user = ctx.sessionAttribute("currentUser");
            if (user == null) {
                ctx.status(401).result("Du er ikke logget ind.");
                return;
            }

            try {
                List<Order> orders = OrderMapper.getAllOrdersPerUser(user.getId(), connectionPool);
                ctx.attribute("orders", orders);
                ctx.render("orders.html"); //todo ændre muligvis HTML
            } catch (DatabaseException e) {
                ctx.status(500).result("Fejl ved hentning af ordrer: " + e.getMessage());
            }
        }

        private static void getAllOrdersPerWorker(Context ctx, ConnectionPool connectionPool) {
            User currentUser = ctx.sessionAttribute("currentUser");

            try {
                int workerId = currentUser.getId();
                System.out.println("Logged-in workerId: " + workerId);

                List<Order> orders = OrderMapper.getAllOrdersPerWorker(workerId, connectionPool);

                System.out.println("Orders to render: " + orders.size());
                orders.forEach(System.out::println);

                if (orders == null) {
                    System.out.println("Orders is null!");
                } else {
                    System.out.println("Orders count: " + orders.size());
                }
                ctx.attribute("orders", orders);
                ctx.render("sellerdashboard.html");
            } catch (DatabaseException e) {
                ctx.status(500).result("Fejl ved hentning af ordrer for medarbejder: " + e.getMessage());
            }
        }


        public static List<Order> getAllOrders(ConnectionPool connectionPool) throws DatabaseException {
            List<Order> orderList = new ArrayList<>();
            String sql = "SELECT * FROM orders";

            try (
                    Connection connection = connectionPool.getConnection();
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()
            ) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    Date orderDate = rs.getDate("order_date");  // java.sql.Date
                    double totalPrice = rs.getDouble("total_price");
                    String orderStatus = rs.getString("order_status");
                    int customerId = rs.getInt("customer_id");
                    int workerId = rs.getInt("worker_id");
                    int carportId = rs.getInt("carport_id");

                    // Convert java.sql.Date to LocalDate
                    LocalDate localOrderDate = orderDate.toLocalDate();

                    Order order = new Order(orderId, localOrderDate, totalPrice, orderStatus, customerId, workerId, carportId);
                    orderList.add(order);
                }
            } catch (SQLException e) {
                throw new DatabaseException("Error fetching all orders", e.getMessage());
            }

            return orderList;

        }


    }

