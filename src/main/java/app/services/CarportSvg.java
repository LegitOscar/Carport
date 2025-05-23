package app.services;

public class CarportSvg
{
    private int width;
    private int length;
    private Svg carportSvg;

    public CarportSvg(int width, int length, int shedWidth, int shedLength) {
        this.width = width;
        this.length = length;
        // Use length as width, width as height — match your coordinates accordingly
        carportSvg = new Svg("0 0 " + length + " " + width, "100%", Integer.toString(width));
        carportSvg.addRectangle(0, 0, 600, 780, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        addBeams();
        addRafters();
        addShed(shedWidth, shedLength);
    }


    private void addBeams(){
        carportSvg.addRectangle(0,35,4.5, 780, "stroke-width:1px; stroke:#000000; fill: #ffffff");
        carportSvg.addRectangle(0,565,4.5, 780, "stroke-width:1px; stroke:#000000; fill: #ffffff");
    }

    private void addRafters(){
        for (int i = 0; i < 780; i+= 55.714)
        {
            carportSvg.addRectangle(i, 0.0, 600, 4.5,"stroke:#000000; fill: #ffffff" );
        }
    }
    private void addShed(int shedWidth, int shedlength){
        double x = length-shedlength;
        double y = 35;
        carportSvg.addRectangle(x,y,530,shedlength,"stroke-width:5px; stroke:#865431; fill: #ffffff");
    }

    @Override
    public String toString()
    {
        return carportSvg.toString();
    }
}