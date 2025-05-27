package app.services;

import app.entities.*;
import app.persistence.ConnectionPool;
import app.persistence.FittingsAndScrewsMapper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Calculator {

    private final List<WoodVariant> woodVariants;
    private static final int MATERIALID_STOLPE = 12;
    private static final int MATERIALID_SPÆR = 13;
    private static final int MATERIALID_REM = 13;
    private static final int MATERIALID_LØSHOLTER = 14;
    private static final int MATERIALID_STERN = 16;

    public Calculator(List<WoodVariant> woodVariants) {
        this.woodVariants = woodVariants;
    }

    public List<OrderItem> calculateMaterials(Carport carport, Order order, List<FittingsAndScrews> fittingsList) {
        List<OrderItem> result = new ArrayList<>();

        result.addAll(calculatePoles(carport, order));
        result.addAll(calculateRafters(carport, order));
        result.addAll(calculateBeams(carport, order));
        result.addAll(calculateWallBraces(carport, order));
        result.addAll(calculateStern(carport, order));

        result.addAll(calculateFittingsAndScrews(order, fittingsList, result));

        return result;
    }

    public double calculateTotalPrice(List<OrderItem> orderItems) {
        double total = 0;
        for (OrderItem item : orderItems) {
            total += item.getQuantity() * item.getUnitPrice();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    private List<OrderItem> calculatePoles(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();
        int needed = 4;
        if (carport.getLengthCm() > 500) needed += 2;

        for (WoodVariant variant : woodVariants) {
            if (variant.getMaterialId() == MATERIALID_STOLPE) {
                String description = "Stolper til hjørner (fast længde)";
                items.add(new OrderItem(order, variant, needed, variant.getPrice(), description));
                break;
            }
        }

        return items;
    }

    private List<OrderItem> calculateRafters(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();
        int spacing = 55;
        int needed = carport.getLengthCm() / spacing;

        List<WoodVariant> variants = findVariantsByLength(MATERIALID_SPÆR, carport.getWidthCm());
        for (WoodVariant variant : variants) {
            String description = "Spær (skal dække carportens bredde)";
            items.add(new OrderItem(order, variant, needed, variant.getPrice(), description));
        }

        return items;
    }

    private List<OrderItem> calculateBeams(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();
        int neededLength = carport.getLengthCm();

        for (int i = 0; i < 2; i++) {
            List<WoodVariant> selected = findVariantSet(MATERIALID_REM, neededLength);
            for (WoodVariant variant : selected) {
                String description = "Rem (del af samlet længde på " + neededLength + " cm)";
                items.add(new OrderItem(order, variant, 1, variant.getPrice(), description));
            }
        }

        return items;
    }

    private List<OrderItem> calculateWallBraces(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();
        int neededSets = 4;

        for (int i = 0; i < neededSets; i++) {
            List<WoodVariant> selected = findVariantSet(MATERIALID_LØSHOLTER, carport.getLengthCm());
            for (WoodVariant variant : selected) {
                String description = "Løsholt (del af side)";
                items.add(new OrderItem(order, variant, 1, variant.getPrice(), description));
            }
        }
        return items;
    }

    private List<OrderItem> calculateStern(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();

        List<WoodVariant> frontBack = findVariantsByLength(MATERIALID_STERN, carport.getWidthCm());
        for (WoodVariant variant : frontBack) {
            String description = "Sternbræt til for- og bagside";
            items.add(new OrderItem(order, variant, 2, variant.getPrice(), description));
        }

        List<WoodVariant> sides = findVariantsByLength(MATERIALID_STERN, carport.getLengthCm());
        for (WoodVariant variant : sides) {
            String description = "Sternbræt til sider";
            items.add(new OrderItem(order, variant, 2, variant.getPrice(), description));
        }

        return items;
    }

    private List<OrderItem> calculateFittingsAndScrews(Order order, List<FittingsAndScrews> fittingsList, List<OrderItem> woodItems) {
        List<OrderItem> items = new ArrayList<>();

        for (FittingsAndScrews fitting : fittingsList) {
            int quantityPerPackage = fitting.getQuantityPerPackage();
            String fittingSize = fitting.getSizeFS().toLowerCase();

            int totalNeeded = 0;
            String usageDescription = "";

            for (OrderItem woodItem : woodItems) {
                String desc = woodItem.getDescription().toLowerCase();

                int neededPerPiece = 0;

                if (fittingSize.contains("beslag")) {
                    if (desc.contains("spær")) {
                        neededPerPiece = 2;
                        usageDescription = "montering af spær";
                    } else if (desc.contains("rem")) {
                        neededPerPiece = 2;
                        usageDescription = "montering af rem";
                    }
                } else if (fittingSize.contains("vinkel") || fittingSize.contains("skruer")) {
                    if (desc.contains("løsholt")) {
                        neededPerPiece = 2;
                        usageDescription = "løsholter";
                    } else if (desc.contains("stern")) {
                        neededPerPiece = 4;
                        usageDescription = "sternbrædder";
                    } else if (desc.contains("stolpe")) {
                        neededPerPiece = 4;
                        usageDescription = "stolper";
                    }
                }

                totalNeeded += woodItem.getQuantity() * neededPerPiece;
            }

            int packages = (int) Math.ceil((double) totalNeeded / quantityPerPackage);
            if (packages > 0 && !usageDescription.isEmpty()) {
                String description = fitting.getSizeFS() + " til " + usageDescription;
                items.add(new OrderItem(order, null, packages, fitting.getPriceFS(), description));
            }
        }

        return items;
    }

    private List<WoodVariant> findVariantsByLength(int materialId, int requiredLength) {
        List<WoodVariant> matches = new ArrayList<>();
        List<WoodVariant> candidates = new ArrayList<>();

        for (WoodVariant variant : woodVariants) {
            if (variant.getMaterialId() == materialId) {
                candidates.add(variant);
            }
        }
        candidates.sort((a, b) -> b.getLengthCm() - a.getLengthCm());

        int total = 0;
        for (WoodVariant variant : candidates) {
            if (total >= requiredLength) break;
            matches.add(variant);
            total += variant.getLengthCm();
        }
        return matches;
    }

    private List<WoodVariant> findVariantSet(int materialId, int totalLength) {
        List<WoodVariant> result = new ArrayList<>();
        int remaining = totalLength;

        List<WoodVariant> matching = new ArrayList<>();
        for (WoodVariant variant : woodVariants) {
            if (variant.getMaterialId() == materialId) {
                matching.add(variant);
            }
        }
        matching.sort((a, b) -> b.getLengthCm() - a.getLengthCm());

        for (WoodVariant variant : matching) {
            while (remaining >= variant.getLengthCm()) {
                result.add(variant);
                remaining -= variant.getLengthCm();
            }
        }
        for (WoodVariant variant : matching) {
            if (variant.getLengthCm() >= remaining) {
                result.add(variant);
                break;
            }
        }

        return result;
    }

    public List<OrderItem> generateBillOfMaterials(Carport carport) {
        Order dummyOrder = new Order(0, LocalDate.now(), 0, null, 0, 0);
        FittingsAndScrewsMapper mapper = new FittingsAndScrewsMapper();
        List<FittingsAndScrews> fittingsList;

        try {
            ConnectionPool pool = new ConnectionPool();
            fittingsList = mapper.getAllFittingsAndScrews(pool);
        } catch (SQLException e) {
            throw new RuntimeException("Fejl ved hentning af fittings og skruer", e);
        }

        return calculateMaterials(carport, dummyOrder, fittingsList);
    }
}
