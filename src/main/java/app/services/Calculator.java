package app.services;

import app.entities.Carport;
import app.entities.Order;
import app.entities.OrderItem;
import app.entities.Order;
import app.entities.WoodVariant;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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

    public List<OrderItem> calculateMaterials(Carport carport, Order order) {
        List<OrderItem> result = new ArrayList<>();

        result.addAll(calculatePoles(carport, order));
        result.addAll(calculateRafters(carport, order));
        result.addAll(calculateBeams(carport, order));
        result.addAll(calculateWallBraces(carport, order));
        result.addAll(calculateStern(carport, order));

        return result;
    }

    public double calculateTotalPrice(List<OrderItem> orderItems) {
        double total = 0;
        for (OrderItem item : orderItems) {
            total += item.getQuantity() * item.getUnitPrice();
        }
        return total;
    }

    private List<OrderItem> calculatePoles(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();

        int needed = 4;
        if (carport.getLengthCm() > 500) {
            needed += 2;
        }

        WoodVariant variant = findVariantByLength(MATERIALID_STOLPE, 300);
        if (variant != null) {
            items.add(new OrderItem(order, variant, needed, variant.getPrice()));
        }

        return items;
    }

    private List<OrderItem> calculateRafters(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();

        int spacing = 55;
        int needed = carport.getLengthCm() / spacing;

        WoodVariant variant = findVariantByLength(MATERIALID_SPÆR, carport.getWidthCm());
        if (variant != null) {
            items.add(new OrderItem(order, variant, needed, variant.getPrice()));
        }

        return items;
    }

    private List<OrderItem> calculateBeams(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();

        int neededLength = carport.getLengthCm();

        // 2 remme – hver skal dække hele længden
        for (int i = 0; i < 2; i++) {
            List<WoodVariant> selected = findVariantSet(MATERIALID_REM, neededLength);

            for (WoodVariant variant : selected) {
                items.add(new OrderItem(order, variant, 1, variant.getPrice()));
            }
        }

        return items;
    }

    private List<OrderItem> calculateWallBraces(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();
        int quantity = 4;
        WoodVariant variant = findVariantByLength(MATERIALID_LØSHOLTER, carport.getLengthCm());
        if (variant != null) {
            items.add(new OrderItem(order, variant, quantity, variant.getPrice()));
        }
        return items;
    }

    private List<OrderItem> calculateStern(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();

        // Forside og bagside stern
        WoodVariant fb = findVariantByLength(MATERIALID_STERN, carport.getWidthCm());
        if (fb != null) {
            items.add(new OrderItem(order, fb, 2, fb.getPrice()));
        }

        // Side stern
        WoodVariant sides = findVariantByLength(MATERIALID_STERN, carport.getLengthCm());
        if (sides != null) {
            items.add(new OrderItem(order, sides, 2, sides.getPrice()));
        }

        return items;
    }

    private WoodVariant findVariantByLength(int materialId, int requiredLength) {
        WoodVariant bestMatch = null;

        for (WoodVariant variant : woodVariants) {
            if (variant.getMaterialId() == materialId && variant.getLengthCm() >= requiredLength) {

                if (bestMatch == null || variant.getLengthCm() < bestMatch.getLengthCm()) {
                    bestMatch = variant;
                }
            }
        }
        return bestMatch;
    }
    private List<WoodVariant> findVariantSet(int materialId, int totalLength) {
        List<WoodVariant> result = new ArrayList<>();
        int remaining = totalLength;
        // Find alle varianter med det ønskede materialId
        List<WoodVariant> matching = new ArrayList<>();
        for (WoodVariant variant : woodVariants) {
            if (variant.getMaterialId() == materialId) {
                matching.add(variant);
            }
        }
        // Sorter matching manuelt fra længst til kortest
        for (int i = 0; i < matching.size(); i++) {
            for (int j = i + 1; j < matching.size(); j++) {
                if (matching.get(i).getLengthCm() < matching.get(j).getLengthCm()) {
                    WoodVariant temp = matching.get(i);
                    matching.set(i, matching.get(j));
                    matching.set(j, temp);
                }
            }
        }
        // Tilføj længste stykker først, indtil total længde er dækket
        for (WoodVariant variant : matching) {
            while (remaining >= variant.getLengthCm()) {
                result.add(variant);
                remaining -= variant.getLengthCm();
            }
        }
        // Hvis der mangler lidt, tilføj et passende stykke
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
        return calculateMaterials(carport, dummyOrder);
    }

}
