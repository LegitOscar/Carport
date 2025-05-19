package app.services;

import app.entities.*;

import java.util.ArrayList;
import java.util.List;

public class Calculator {

    private final List<WoodVariant> woodVariants;

    public  Calculator(List<WoodVariant> woodVariants) {
        this.woodVariants = woodVariants;
    }

    public List<OrderItem> calculateMaterials(Carport carport) {
        List<OrderItem> result = new ArrayList<>();

        result.addAll(calculatePoles(carport));
        result.addAll(calculateRafters(carport));
        result.addAll(calculateBeams(carport));

        return result;
    }

    public double calculateTotalPrice(List<OrderItem> orderItems) {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getTotalPrice();  // antal * pris
        }
        return total;
    }

    private List<OrderItem> calculatePoles(Carport carport) {
        List<OrderItem> items = new ArrayList<>();

        int needed = 4;
        if (carport.getLengthCm() > 500) {
            needed += 2;
        }

        WoodVariant variant = findVariantByLength("Stolpe", 300);
        if (variant != null) {
            items.add(new OrderItem(variant, needed));
        }

        return items;
    }

    private List<OrderItem> calculateRafters(Carport carport) {
        List<OrderItem> items = new ArrayList<>();

        int spacing = 55;
        int needed = carport.getLengthCm() / spacing;

        WoodVariant variant = findVariantByLength("Spær", carport.getWidthCm());
        if (variant != null) {
            items.add(new OrderItem(variant, needed));
        }

        return items;
    }

    private List<OrderItem> calculateBeams(Carport carport) {
        List<OrderItem> items = new ArrayList<>();

        int neededLength = carport.getLengthCm();

        // Todo ret kalkulering på stopler (Er mindst 2 -)
        for (int i = 0; i < 2; i++) {
            List<WoodVariant> selected = findVariant("Rem", neededLength);

            for (WoodVariant variant : selected) {
                items.add(new OrderItem(variant, 1));
            }
        }

        return items;
    }

    private WoodVariant findVariantByLength(String materialName, int lengthCm) {
        for (WoodVariant variant : woodVariants) {
            if (variant.getMaterial().getName().equalsIgnoreCase(materialName)
                    && variant.getLengthCm() == lengthCm) {
                return variant;
            }
        }
        return null;
    }


    private List<WoodVariant> findVariant(String materialName, int totalLength) {
        List<WoodVariant> result = new ArrayList<>();
        int remaining = totalLength;

        // Find varianter - sorter længste først
        List<WoodVariant> matching = new ArrayList<>();
        for (WoodVariant variant : woodVariants) {
            if (variant.getMaterial().getName().equalsIgnoreCase(materialName)) {
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

        // Remaining = hvis det mangler længde (op til 780)
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

    // todo - ændres helt.
    private String getMaterialNameById(int materialId) {
        // Idiotisk at være en switch case.
        switch (materialId) {
            case 1: return "Stolpe";
            case 2: return "Spær";
            case 3: return "Rem";
            default: return "";
        }
    }

}



