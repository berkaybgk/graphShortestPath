import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

public class Path {
    // path class to convert the final route into output.

    LinkedList<Airport> pathList;
    double cost;

    public Path(double cost) {
        this.pathList = new LinkedList<>();
        this.cost = cost;
    }

    @Override
    public String toString() {
        Collections.reverse(pathList);
        StringBuilder stringBuilder = new StringBuilder();

        String parentCode = "";
        for (Airport airport: pathList) {
            if (airport.airportCode.equals(parentCode)) {
                stringBuilder.append("PARK");
            }
            else {
                stringBuilder.append(airport.airportCode);
                parentCode = airport.airportCode;
            }
            stringBuilder.append(" ");
        }
        if (stringBuilder.toString().isEmpty()) return "No possible solution.";
        return stringBuilder.toString() + String.format(Locale.US, "%.5f", cost);
    }
}
