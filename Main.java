import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {

        HashMap<String,Airfield> airfieldHashmap = new HashMap<>();
        HashMap<String,Airport> airportHashMap = new HashMap<>();

        File airportsFile = new File(args[0]);
        File directionsFile = new File(args[1]);
        File weatherFile = new File(args[2]);
        File missionsFile = new File(args[3]);
        File task1outFile = new File(args[4]);
        File task2outFile = new File(args[5]);

        // first read the weather file and initialize the airfields
        try (BufferedReader weatherReader = new BufferedReader(new FileReader(weatherFile))) {
            weatherReader.readLine(); // Skip the first line (header)

            String currentLine;
            while ((currentLine = weatherReader.readLine()) != null) {
                String[] currentLineList = currentLine.split(",");
                String airfieldCode = currentLineList[0];

                if (airfieldHashmap.containsKey(airfieldCode)) {
                    airfieldHashmap.get(airfieldCode).timeWeatherCodeTable.put(
                            Long.parseLong(currentLineList[1]), Integer.parseInt(currentLineList[2]));
                }
                else {
                    airfieldHashmap.put(airfieldCode, new Airfield(airfieldCode));
                    airfieldHashmap.get(airfieldCode).timeWeatherCodeTable.put(
                            Long.parseLong(currentLineList[1]), Integer.parseInt(currentLineList[2]));
                }
            }
        } catch (Exception ignored) {
            // end of the file
        }

        // next read the airport file and initialize the airports
        try (BufferedReader airportsReader = new BufferedReader(new FileReader(airportsFile))) {
            airportsReader.readLine(); // Skip the first line (header)

            String currentLine;
            while ((currentLine = airportsReader.readLine()) != null) {
                String[] currentLineList = currentLine.split(",");
                String airportCode = currentLineList[0];
                Airfield belongingAirfield = airfieldHashmap.get(currentLineList[1]);
                double latitude = Double.parseDouble(currentLineList[2]);
                double longitude = Double.parseDouble(currentLineList[3]);
                int parkingCost = Integer.parseInt(currentLineList[4]);

                airportHashMap.put(airportCode, new Airport(airportCode, latitude, longitude, belongingAirfield, parkingCost));
            }
        } catch (Exception ignored) {
            // end of the file
        }

        // next read the directions and construct the adjacency lists for airports
        try (BufferedReader directionsReader = new BufferedReader(new FileReader(directionsFile))) {
            directionsReader.readLine(); // Skip the first line (header)

            String currentLine;
            while ((currentLine = directionsReader.readLine()) != null) {
                // read the neighbours and add the airports to their lists.
                Airport from = airportHashMap.get(currentLine.split(",")[0]);
                Airport to = airportHashMap.get(currentLine.split(",")[1]);
                from.neighbouringAirports.add(to);
            }
        } catch (Exception ignored) {
            // end of the file
        }

        // next step is reading the missions
        Scanner missionsScanner = new Scanner(missionsFile);
        String planeName = missionsScanner.nextLine();
        int planeType = 0;
        if (planeName.equals("Orion III")) planeType = 1;
        else if (planeName.equals("Skyfleet S570")) planeType = 2;
        else if (planeName.equals("T-16 Skyhopper")) planeType = 3;

        ArrayList<Mission> missionsList = new ArrayList<>();
        while (missionsScanner.hasNextLine()) {
            // read the neighbours and add the airports to their lists.
            String currentLine = missionsScanner.nextLine();
            String[] currentLineList = currentLine.split(" ");
            Airport from = airportHashMap.get(currentLineList[0]);
            Airport to = airportHashMap.get(currentLineList[1]);

            long initialTime = Long.parseLong(currentLineList[2]);
            long deadlineTime = Long.parseLong(currentLineList[3]);

            missionsList.add(new Mission(from,to,initialTime,deadlineTime));
        }
        missionsScanner.close();

        FileWriter task1writer = new FileWriter(task1outFile);
        FileWriter task2writer = new FileWriter(task2outFile);

        // everything is ready, run the shortest path algorithms
        for (Mission mission : missionsList) {
            task1writer.write(shortestPathNoDeadline(airportHashMap,mission.departureAirport,mission.destinationAirport,mission.startingTime) + "\n");
            task2writer.write(shortestPathWithDeadline(airportHashMap,mission.departureAirport,mission.destinationAirport
                    ,mission.startingTime, mission.deadlineTime, planeType) + "\n");
        }

        task1writer.close();
        task2writer.close();

    }

    // method for the task 1
    public static Path shortestPathNoDeadline (HashMap<String,Airport> airportHashMap
            , Airport departureAirport, Airport destinationAirport, long initialTime) {

        HashSet<Airport> reachedAirports = new HashSet<>();
        HashMap<Airport, Airport> parentAirports = new HashMap<>();
        HashMap<Airport, Double> costsToAirports = new HashMap<>();

        for (Map.Entry<String,Airport> entrySet: airportHashMap.entrySet()) {
            costsToAirports.put(entrySet.getValue(),Double.MAX_VALUE);
        }
        costsToAirports.replace(departureAirport,0.0);
        departureAirport.cost = 0.0;

        Comparator<Airport> costComparator = Comparator.comparingDouble(Airport::getCost);
        PriorityQueue<Airport> priorityQueue = new PriorityQueue<>(costComparator);
        priorityQueue.add(departureAirport);

        while (!priorityQueue.isEmpty()) {
            Airport currentAirport = priorityQueue.poll();
            reachedAirports.add(currentAirport);

            if (reachedAirports.contains(destinationAirport)) // if the destination is reached
                return constructPath(parentAirports,departureAirport,destinationAirport
                        ,Double.parseDouble(String.format(Locale.US, "%.5f", destinationAirport.cost)));

            // destination is not reached, continue the search from the neighbours of the currentAirport
            for (Airport neighbouringAirport : currentAirport.neighbouringAirports) {
                if (!reachedAirports.contains(neighbouringAirport)) {
                    double edgeCost = calculateCost1(currentAirport, neighbouringAirport, initialTime);
                    if (costsToAirports.get(neighbouringAirport) > currentAirport.cost + edgeCost) { // if we have found a better path to that airport
                        costsToAirports.put(neighbouringAirport, currentAirport.cost + edgeCost);
                        parentAirports.put(neighbouringAirport, currentAirport);
                        neighbouringAirport.cost = currentAirport.cost + edgeCost;

                        // Remove and re-add to update priority queue
                        priorityQueue.remove(neighbouringAirport);
                        priorityQueue.add(neighbouringAirport);
                    }
                }
            }
        }
        return new Path(-1); // no path has been found
    }

    // method to calculate the path for the task2
    public static Path shortestPathWithDeadline (HashMap<String,Airport> airportHashMap, Airport departureAirport, Airport destinationAirport
            , long initialTime, long deadline, int planeType) {

        Comparator<AirportInstance> costComparator = Comparator.comparingDouble(AirportInstance::getCost);
        PriorityQueue<AirportInstance> priorityQueue = new PriorityQueue<>(costComparator);
        priorityQueue.add(new AirportInstance(departureAirport,initialTime, 0.0,null,
                calculateHaversineDistance(departureAirport,destinationAirport)));

        // we use a hashmap to store the cheapest path for each time stamp, this way we don't end up with loops.
        HashMap<Airport,HashMap<Long,Double>> airportsTimeCostTable = new HashMap<>();
        airportsTimeCostTable.put(departureAirport,new HashMap<>());
        airportsTimeCostTable.get(departureAirport).put(initialTime,0.0);

        // priority queue of the airport instances
        while (!priorityQueue.isEmpty()) {
            AirportInstance currentAirportInst = priorityQueue.poll();
            Airport currentAirport = currentAirportInst.airport;
            long currentTime = currentAirportInst.reachingTime;
            double currentCost = currentAirportInst.costOfReaching;

            if (currentAirport == destinationAirport) {
                Path path = new Path(Double.parseDouble(String.format(Locale.US, "%.5f", currentCost)));
                while (currentAirportInst != null) {
                    path.pathList.add(currentAirportInst.airport);
                    currentAirportInst = currentAirportInst.parentAirportInstance;
                }
                return path;
            }

            // if the polled element is not settled in the hashmap before.
            if ((airportsTimeCostTable.get(currentAirport) == null) || (airportsTimeCostTable.get(currentAirport).get(currentTime) == null) ||
                    (airportsTimeCostTable.get(currentAirport).get(currentTime) >= currentCost)) {

                // park if we haven't reached to that airport at the same time with a lower cost
                // currentTime + 21600 is the new time when we do parking.
                if (currentTime + 21600 <= deadline) {
                    if ((airportsTimeCostTable.get(currentAirport).get(currentTime + 21600) == null) ||
                            airportsTimeCostTable.get(currentAirport).get(currentTime + 21600) > currentCost + currentAirport.parkingCost) {

                        airportsTimeCostTable.get(currentAirport).put(currentTime + 21600, currentCost + currentAirport.parkingCost);

                        priorityQueue.add(new AirportInstance(currentAirport, currentTime + 21600,
                                currentCost + currentAirport.parkingCost, currentAirportInst,calculateHaversineDistance(currentAirport,destinationAirport)));

                    }
                }

                // then check for the neighbours of the instances
                for (Airport neighbouringAirport : airportHashMap.get(currentAirport.airportCode).neighbouringAirports) {
                    airportsTimeCostTable.putIfAbsent(neighbouringAirport, new HashMap<>());

                    double distance = calculateHaversineDistance(currentAirport, neighbouringAirport);
                    long reachingTimeForNeighbour = calculateTime(currentTime, planeType, distance);

                    double newCost;
                    if (reachingTimeForNeighbour <= deadline) {
                        newCost = calculateCost2(currentAirport, neighbouringAirport, currentTime, reachingTimeForNeighbour, distance);
                    } else {
                        newCost = Double.MAX_VALUE;
                    }

                    if ((reachingTimeForNeighbour <= deadline) &&
                            ((airportsTimeCostTable.get(neighbouringAirport).get(reachingTimeForNeighbour) == null) ||
                                    (airportsTimeCostTable.get(neighbouringAirport).get(reachingTimeForNeighbour) > currentCost + newCost))) {

                        airportsTimeCostTable.get(neighbouringAirport).put(reachingTimeForNeighbour, currentCost + newCost);

                        priorityQueue.add(new AirportInstance(neighbouringAirport, reachingTimeForNeighbour,
                                currentCost + newCost, currentAirportInst, calculateHaversineDistance(neighbouringAirport,destinationAirport)));

                    }
                }
            }
        }
        return new Path(-1);
    }

    // method to return the path to the destination
    public static Path constructPath(HashMap<Airport, Airport> parentsMap, Airport departureAirport, Airport destinationAirport, double cost) {
        Path returnPath = new Path(cost);

        Airport currentAirport = destinationAirport;
        while (parentsMap.get(currentAirport)!= null) {
            returnPath.pathList.add(currentAirport);
            currentAirport = parentsMap.get(currentAirport);
            if (currentAirport.equals(departureAirport)) returnPath.pathList.add(currentAirport);
        }
        return returnPath;
    }

    // calculate costs for changing time, here plane types are:
    // 0 -> Carreidas 160, for TR flights
    // 1 -> Orion III, for AS flights
    // 2 -> Skyfleet S570, for EU flights
    // 3 -> T-16 Skyhopper, for INTER flights
    public static double calculateCost1(Airport departureAirport, Airport destinationAirport, long initialTime) {
        double distance = calculateHaversineDistance(departureAirport,destinationAirport);
        double ap1weather = departureAirport.belongingAirfield.getWeatherMultiplier(initialTime);
        double ap2weather = destinationAirport.belongingAirfield.getWeatherMultiplier(initialTime);
        return (300*ap1weather*ap2weather) + distance;
    }
    public static double calculateCost2(Airport departureAirport, Airport destinationAirport, long initialTime, long landingTime, double distance) {
        double ap1weather = departureAirport.belongingAirfield.getWeatherMultiplier(initialTime);
        double ap2weather = destinationAirport.belongingAirfield.getWeatherMultiplier(landingTime);

        return (300*ap1weather*ap2weather) + distance;
    }

    // method to find the flight duration
    public static long calculateTime(long initialTime, int planeType, double distance){

        if (planeType == 0) { // if the plane is Carreidas 160
            if (distance <= 175) return initialTime + 21600;
            else if (distance <= 350) return initialTime + 43200;
            else return initialTime + 64800;
        }
        else if (planeType == 1) { // if the plane is Orion III
            if (distance <= 1500) return initialTime + 21600;
            else if (distance <= 3000) return initialTime + 43200;
            else return initialTime + 64800;
        }
        else if (planeType == 2) { // if the plane is Skyfleet S570
            if (distance <= 500) return initialTime + 21600;
            else if (distance <= 1000) return initialTime + 43200;
            else return initialTime + 64800;
        }
        else { // if the plane is T-16 Skyhopper
            if (distance <= 2500) return initialTime + 21600;
            else if (distance <= 5000) return initialTime + 43200;
            else return initialTime + 64800;
        }
    }

    // method to find the distance in between two airports
    public static double calculateHaversineDistance(Airport airport1, Airport airport2) {
        double earthRadius = 6371.0;
        // Convert latitude and longitude from degrees to radians
        double lat1 = Math.toRadians(airport1.latitude);
        double lon1 = Math.toRadians(airport1.longitude);
        double lat2 = Math.toRadians(airport2.latitude);
        double lon2 = Math.toRadians(airport2.longitude);

        // Haversine formula
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }
}