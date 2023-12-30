import java.util.HashMap;

public class Airfield {
    String airfieldName;
    HashMap<Long,Integer> timeWeatherCodeTable;
    HashMap<Long,Double> timeWeatherMultiplierTable;

    Airfield(String airfieldName) {
        this.airfieldName = airfieldName;
        this.timeWeatherCodeTable = new HashMap<>();
        this.timeWeatherMultiplierTable = new HashMap<>();
    }

    public double getWeatherMultiplier (long unixTime) {
        if (timeWeatherMultiplierTable.containsKey(unixTime)) return timeWeatherMultiplierTable.get(unixTime);
        else { // calculate the time from the weather code, insert it to the mult. table and then return it
            timeWeatherMultiplierTable.put(unixTime,convertWeatherCodeToMultiplier(timeWeatherCodeTable.get(unixTime)));
            return timeWeatherMultiplierTable.get(unixTime);
        }
    }

    public static double convertWeatherCodeToMultiplier(int weatherCode) {
        // Convert decimal weather code to binary representation
        String binaryWeatherCode = Integer.toBinaryString(weatherCode);

        // Pad the binary representation with leading zeros to ensure a 5-bit representation
        while (binaryWeatherCode.length() < 5) {
            binaryWeatherCode = "0" + binaryWeatherCode;
        }

        // Extract individual bits from the binary representation
        int Bw = Character.getNumericValue(binaryWeatherCode.charAt(0));
        int Br = Character.getNumericValue(binaryWeatherCode.charAt(1));
        int Bs = Character.getNumericValue(binaryWeatherCode.charAt(2));
        int Bh = Character.getNumericValue(binaryWeatherCode.charAt(3));
        int Bb = Character.getNumericValue(binaryWeatherCode.charAt(4));

        // Calculate the weather multiplier using the provided formula
        double W = (Bw * 1.05 + (1 - Bw)) * (Br * 1.05 + (1 - Br)) * (Bs * 1.10 + (1 - Bs))
                * (Bh * 1.15 + (1 - Bh)) * (Bb * 1.20 + (1 - Bb));

        // Format the result to have 5 digits after the decimal point
        return W;
    }

    @Override
    public String toString() {
        return airfieldName;
    }
}