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

        List<WoodVariant> variants = findVariantsByLength(MATERIALID_STOLPE, 300);
        for (WoodVariant variant : variants) {
            String description = "Stolper til hjørner (min. 300 cm)";
            items.add(new OrderItem(order, variant, needed, variant.getPrice(), description));
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

        // 2 remme – hver skal dække hele længden
        for (int i = 0; i < 2; i++) {
            List<WoodVariant> selected = findVariantSet(MATERIALID_REM, neededLength);

            for (WoodVariant variant : selected) {
                String description = "BESKRIVELSE BEAMS";
                items.add(new OrderItem(order, variant, 1, variant.getPrice(),description));
            }
        }

        return items;
    }

    private List<OrderItem> calculateWallBraces(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();

        int quantityPerPiece = 1;
        int totalPieces = 4;

        List<WoodVariant> variants = findVariantsByLength(MATERIALID_LØSHOLTER, carport.getLengthCm());
        for (WoodVariant variant : variants) {
            String description = "Løsholter til sider";
            items.add(new OrderItem(order, variant, totalPieces, variant.getPrice(), description));
        }

        return items;
    }

    private List<OrderItem> calculateStern(Carport carport, Order order) {
        List<OrderItem> items = new ArrayList<>();

        // Forside og bagside stern
        List<WoodVariant> frontBack = findVariantsByLength(MATERIALID_STERN, carport.getWidthCm());
        for (WoodVariant variant : frontBack) {
            String description = "Sternbræt til for- og bagside";
            items.add(new OrderItem(order, variant, 2, variant.getPrice(), description));
        }

        // Side stern
        List<WoodVariant> sides = findVariantsByLength(MATERIALID_STERN, carport.getLengthCm());
        for (WoodVariant variant : sides) {
            String description = "Sternbræt til sider";
            items.add(new OrderItem(order, variant, 2, variant.getPrice(), description));
        }

        return items;
    }

    public List<OrderItem> calculateFittingsAndScrews(Order order, List<FittingsAndScrews> fittingsList, List<OrderItem> woodItems) {
        List<OrderItem> items = new ArrayList<>();

        for (FittingsAndScrews fitting : fittingsList) {
            int materialId = fitting.getMaterialId();
            int quantityPerPackage = fitting.getQuantityPerPackage();

            int totalMatchingWoodQuantity = 0;

            for (OrderItem item : woodItems) {
                if (item.getWoodVariant() != null && item.getWoodVariant().getMaterialId() == materialId) {
                    int fittingsPerPiece = estimateFittingsNeededPerMaterial(materialId, item.getDescription());
                    totalMatchingWoodQuantity += item.getQuantity() * fittingsPerPiece;
                }
            }

            int packagesNeeded = (int) Math.ceil((double) totalMatchingWoodQuantity / quantityPerPackage);

            if (packagesNeeded > 0) {
                String description = "Beslag/skruer: " + fitting.getSizeFS() + " til materialId " + materialId;
                items.add(new OrderItem(order, null, packagesNeeded, fitting.getPriceFS(), description));
            }
        }

        return items;
    }

    private int estimateFittingsNeededPerMaterial(int materialId, String woodDescription) {
        if (woodDescription == null) return 4;
        woodDescription = woodDescription.toLowerCase();
        if (woodDescription.contains("stolpe")) return 4; // fx 4 beslag per stolpe
        if (woodDescription.contains("spær")) return 3;
        if (woodDescription.contains("rem")) return 6;
        if (woodDescription.contains("løsholt")) return 2;
        if (woodDescription.contains("stern")) return 4;
        if (woodDescription.contains("vandbræt")) return 3;

        return 4;
    }

    private List<WoodVariant> findVariantsByLength(int materialId, int requiredLength) {
        List<WoodVariant> matches = new ArrayList<>();

        List<WoodVariant> candidates = new ArrayList<>();
        for (WoodVariant variant : woodVariants) {
            if (variant.getMaterialId() == materialId) {
                candidates.add(variant);
            }
        }

        for (int i = 0; i < candidates.size(); i++) {
            for (int j = i + 1; j < candidates.size(); j++) {
                if (candidates.get(j).getLengthCm() > candidates.get(i).getLengthCm()) {
                    WoodVariant temp = candidates.get(i);
                    candidates.set(i, candidates.get(j));
                    candidates.set(j, temp);
                }
            }
        }


        int total = 0;
        for (WoodVariant variant : candidates) {
            if (total >= requiredLength) break;
            matches.add(variant);
            total += variant.getLengthCm();
        }

        if (total < requiredLength) {
            System.out.println("Kunne ikke dække længde " + requiredLength + " for materialId " + materialId);
            return new ArrayList<>();
        }

        // Test
        System.out.print("Valgte varianter til materialId " + materialId + ", requiredLength " + requiredLength + ": ");
        for (WoodVariant wv : matches) {
            System.out.print(wv.getLengthCm() + "cm ");
        }
        System.out.println();

        return matches;
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

    /*public List<OrderItem> generateBillOfMaterials(Carport carport) {
        Order dummyOrder = new Order(0, LocalDate.now(), 0, null, 0, 0);
        return calculateMaterials(carport, dummyOrder);
    }*/

    public List<OrderItem> generateBillOfMaterials(Carport carport) {
        Order dummyOrder = new Order(0, LocalDate.now(), 0, null, 0, 0);

        List<OrderItem> woodItems = calculateMaterials(carport, dummyOrder);
        FittingsAndScrewsMapper mapper = new FittingsAndScrewsMapper();

        List<FittingsAndScrews> fittingsList;
        try {
            ConnectionPool pool = new ConnectionPool();
            fittingsList = mapper.getAllFittingsAndScrews(pool);
        } catch (SQLException e) {
            throw new RuntimeException("Fejl ved hentning af fittings og skruer", e);
        }

        List<OrderItem> fittingsItems = calculateFittingsAndScrews(dummyOrder, fittingsList, woodItems);

        List<OrderItem> result = new ArrayList<>();
        result.addAll(woodItems);
        result.addAll(fittingsItems);

        return result;
    }
}
