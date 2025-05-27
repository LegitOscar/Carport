package app.services;

public class CarportSvg {
    private int width;
    private int length;
    private Svg carportSvg;

    public CarportSvg(int width, int length, int shedWidth, int shedLength) {
        this.width = width;
        this.length = length;
        int viewBoxWidth = length + 100;
        int viewBoxHeight = width + 100;

        carportSvg = new Svg("0 0 " + viewBoxWidth + " " + viewBoxHeight,
                String.valueOf(viewBoxWidth), String.valueOf(viewBoxHeight));

        carportSvg.addRectangle(0, 0, length, width, "stroke-width:1px; stroke:#000000; fill: #ffffff");

        addBeams();
        addRafters();
        addShed(shedWidth, shedLength);
        addDimensions(width, length);
    }

    private void addBeams() {
        carportSvg.addRectangle(0, 35, length, 4.5, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(0, width - 35, length, 4.5, "stroke-width:1px; stroke:#000000; fill: #ffffff");
    }

    private void addRafters() {
        double spacing = 55;
        for (double i = 0; i < length; i += spacing) {
            carportSvg.addRectangle(i, 0, 4.5, width, "stroke:#000000; fill: #ffffff");
        }
    }

    private void addShed(int shedWidth, int shedLength) {
        double x = 0;
        double y = 35;
        carportSvg.addRectangle(x, y, shedLength, shedWidth, "stroke-width:5px; stroke:#865431; fill: #ffffff");
    }

    private void addDimensions(int width, int length) {

        carportSvg.addArrow(length + 20, 0, length + 20, width, "stroke:black; stroke-width:2px");
        carportSvg.addArrow(0, width + 20, length, width + 20, "stroke:black; stroke-width:2px");

        carportSvg.addText(length + 25, width / 2, 90, length + " cm");
        carportSvg.addText(length / 2, width + 35, 0, width + " cm");
    }

    @Override
    public String toString() {
        return carportSvg.toString();
    }
}
