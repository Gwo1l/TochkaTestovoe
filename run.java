import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;


public class Main {
    public static boolean checkCapacity(int maxCapacity, List<Map<String, String>> guests) {
        List<Event> events = new ArrayList<>();
        setEventsTimeline(events, guests);
        Collections.sort(events);
        int currentCapacity = 0;
        for (Event event : events) {
            if (event.isCheckIn()) {
                currentCapacity++;
            }
            else {
                currentCapacity--;
            }
            if (currentCapacity > maxCapacity) {
                return false;
            }
        }
        return true;
    }


    public static void setEventsTimeline(List<Event> events, List<Map<String, String>> guests) {
        for (Map<String, String> guest : guests) {
            LocalDate dateIn = LocalDate.parse(guest.get("check-in"));
            LocalDate dateOut = LocalDate.parse(guest.get("check-out"));
            events.add(new Event(dateIn, true));
            events.add(new Event(dateOut, false));
        }
    }


    private static Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.substring(1, json.length() - 1);


        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim().replace("\"", "");
            String value = keyValue[1].trim().replace("\"", "");
            map.put(key, value);
        }

        return map;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);



        int maxCapacity = Integer.parseInt(scanner.nextLine());



        int n = Integer.parseInt(scanner.nextLine());


        List<Map<String, String>> guests = new ArrayList<>();



        for (int i = 0; i < n; i++) {
            String jsonGuest = scanner.nextLine();

            Map<String, String> guest = parseJsonToMap(jsonGuest);
            guests.add(guest);
        }



        boolean result = checkCapacity(maxCapacity, guests);



        System.out.println(result ? "True" : "False");


        scanner.close();
    }
    private static class Event implements Comparable<Event> {
        private final LocalDate date;
        private final boolean isCheckIn;

        public Event(LocalDate date, boolean isCheckIn) {
            this.date = date;
            this.isCheckIn = isCheckIn;
        }

        public LocalDate getDate() {
            return date;
        }

        public boolean isCheckIn() {
            return isCheckIn;
        }

        @Override
        public int compareTo(Event o) {
            int result = this.date.compareTo(o.getDate());
            if (result == 0) {
                if (o.isCheckIn()) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
            return result;
        }
    }
}


