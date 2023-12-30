public class Mission {
    // mission class to hold and execute the missions
    Airport departureAirport;
    Airport destinationAirport;
    long startingTime;
    long deadlineTime;

    public Mission(Airport departureAirport, Airport destinationAirport, long startingTime, long deadlineTime) {
        this.departureAirport = departureAirport;
        this.destinationAirport = destinationAirport;
        this.startingTime = startingTime;
        this.deadlineTime = deadlineTime;
    }

    @Override
    public String toString(){
        return departureAirport + " --> " + destinationAirport;
    }
}
