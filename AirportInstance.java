public class AirportInstance {
    Airport airport;
    long reachingTime;
    AirportInstance parentAirportInstance;
    double costOfReaching;

    // to use the heuristic method
    double distanceToDestination;

    public AirportInstance(Airport airport, long reachingTime, double costOfReaching, AirportInstance parentAirportInstance, double distanceToDestination) {
        this.airport = airport;
        this.reachingTime = reachingTime;
        this.costOfReaching = costOfReaching;
        this.parentAirportInstance = parentAirportInstance;
        this.distanceToDestination = distanceToDestination;
    }

    public double getCost() {
        return costOfReaching + distanceToDestination;
    }

    @Override
    public int hashCode() {
        return airport.hashCode() + (""+reachingTime).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return airport.airportCode.equals(((Airport) obj).airportCode);
    }

}
