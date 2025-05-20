package app.services;

import app.entities.Carport;
import app.entities.OrderItem;
import app.entities.Orders;
import app.entities.WoodVariant;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Calculator {

    private final List<WoodVariant> woodVariants;

    public Calculator(List<WoodVariant> woodVariants) {
        this.woodVariants = woodVariants;
    }

    public List<OrderItem> calculateMaterials(Carport carport, Orders order) {
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

    private List<OrderItem> calculatePoles(Carport carport, Orders order) {
        List<OrderItem> items = new ArrayList<>();

        int needed = 4;
        if (carport.getLengthCm() > 500) {
            needed += 2;
        }

        WoodVariant variant = findVariantByLength("Stolpe", 300);
        if (variant != null) {
            items.add(new OrderItem(order, variant, needed, variant.getPrice()));
        }

        return items;
    }

    private List<OrderItem> calculateRafters(Carport carport, Orders order) {
        List<OrderItem> items = new ArrayList<>();

        int spacing = 55;
        int needed = carport.getLengthCm() / spacing;

        WoodVariant variant = findVariantByLength("Spær", carport.getWidthCm());
        if (variant != null) {
            items.add(new OrderItem(order, variant, needed, variant.getPrice()));
        }

        return items;
    }

    private List<OrderItem> calculateBeams(Carport carport, Orders order) {
        List<OrderItem> items = new ArrayList<>();

        int neededLength = carport.getLengthCm();

        // 2 remme – hver skal dække hele længden
        for (int i = 0; i < 2; i++) {
            List<WoodVariant> selected = findVariantSet("Rem", neededLength);

            for (WoodVariant variant : selected) {
                items.add(new OrderItem(order, variant, 1, variant.getPrice()));
            }
        }

        return items;
    }

    private List<OrderItem> calculateWallBraces(Carport carport, Orders order) {
        List<OrderItem> items = new ArrayList<>();
        int quantity = 4;
        WoodVariant variant = findVariantByLength("Løsholter", carport.getLengthCm());
        if (variant != null) {
            items.add(new OrderItem(order, variant, quantity, variant.getPrice()));
        }
        return items;
    }

    private List<OrderItem> calculateStern(Carport carport, Orders order) {
        List<OrderItem> items = new ArrayList<>();

        // Forside og bagside stern
        WoodVariant fb = findVariantByLength("Stern", carport.getWidthCm());
        if (fb != null) {
            items.add(new OrderItem(order, fb, 2, fb.getPrice()));
        }

        // Side stern
        WoodVariant sides = findVariantByLength("Stern", carport.getLengthCm());
        if (sides != null) {
            items.add(new OrderItem(order, sides, 2, sides.getPrice()));
        }

        return items;
    }

    private WoodVariant findVariantByLength(String materialName, int requiredLength) {
        WoodVariant bestMatch = null;

        for (WoodVariant variant : woodVariants) {
            if (variant.getMaterialName().equalsIgnoreCase(materialName) &&
                    variant.getLengthCm() >= requiredLength) {

                if (bestMatch == null || variant.getLengthCm() < bestMatch.getLengthCm()) {
                    bestMatch = variant;
                }
            }
        }

        return bestMatch;
    }
    private List<WoodVariant> findVariantSet(String materialName, int totalLength) {
        List<WoodVariant> result = new ArrayList<>();
        int remaining = totalLength;

        List<WoodVariant> matching = new ArrayList<>();
        for (WoodVariant variant : woodVariants) {
            if (variant.getMaterialName().equalsIgnoreCase(materialName)) {
                matching.add(variant);
            }
        }

        matching.sort(Comparator.comparingInt(WoodVariant::getLengthCm).reversed());

        for (WoodVariant variant : matching) {
            while (remaining >= variant.getLengthCm()) {
                result.add(variant);
                remaining -= variant.getLengthCm();
            }
        }

        if (remaining > 0) {
            for (WoodVariant variant : matching) {
                if (variant.getLengthCm() >= remaining) {
                    result.add(variant);
                    break;
                }
            }
        }

        return result;
    }
    public List<OrderItem> generateBillOfMaterials(Carport carport) {
        Orders dummyOrder = new Orders(0, LocalDate.now(), 0, null, 0, 0, carport.getCarportId());
        return calculateMaterials(carport, dummyOrder);
    }

}
