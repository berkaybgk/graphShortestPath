import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Airport{

    String airportCode;
    double latitude;
    double longitude;
    Airfield belongingAirfield;
    int parkingCost;
    LinkedList<Airport> neighbouringAirports;
    HashSet<Airport> ancestorsSet;
    HashMap<Airport,Integer> reachedAndParked;

    double cost;

    public Airport() {
    }

    public Airport(String airportCode,double latitude, double longitude, Airfield belongingAirfield, int parkingCost) {
        this.airportCode = airportCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.belongingAirfield = belongingAirfield;
        this.parkingCost = parkingCost;
        this.neighbouringAirports = new LinkedList<>();
        this.cost = 0;
        this.ancestorsSet = new HashSet<>();
        this.reachedAndParked = new HashMap<>();
    }

    @Override
    public String toString() {
        return airportCode+"/"+belongingAirfield;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public int hashCode() {
        return airportCode.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return airportCode.equals(((Airport) obj).airportCode);
    }
}
