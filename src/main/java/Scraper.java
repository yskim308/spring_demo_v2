import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Scraper {
    private static String URL = "https://www.daangn.com/kr/buy-sell/?in=%EB%82%A8%EA%B0%80%EC%A2%8C%EC%A0%9C1%EB%8F%99-217&search=%EB%AA%A8%EB%8B%88%ED%84%B0";
    private static int SLEEP_MINUTES = 5; // 5 minutes, not seconds!
    private static int MAX_PRICE = 30000;

    // Use a Map to properly track item states
    private static Map<String, ItemInfo> previousItems = new HashMap<>();

    // Helper class to store item information
    static class ItemInfo {
        String price;
        long lastSeen;

        ItemInfo(String price) {
            this.price = price;
            this.lastSeen = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) {
        configureSettings();
        System.out.println("Checking every " + SLEEP_MINUTES + " minutes");
        System.out.println("Max price: " + MAX_PRICE + "원\n");

        int runCount = 0;

        while (true) {
            try {
                runCount++;
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.out.println("=== RUN #" + runCount + " [" + timestamp + "] ===");

                scrapeWithFixedLogic();

                System.out.println("Sleeping for " + SLEEP_MINUTES + " minutes...\n");

                // FIX: Convert minutes to milliseconds correctly
                Thread.sleep(SLEEP_MINUTES * 1000L);

            } catch (InterruptedException e) {
                System.out.println("\n❌ Scraper stopped.");
                break;
            } catch (Exception e) {
                System.out.println("⚠️ Error: " + e.getMessage());
                try {
                    Thread.sleep(60000); // Wait 1 minute on error
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }

    private static void configureSettings() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter URL to scrape the shit out of: ");
        String inputurl = scanner.nextLine().trim();
        if (!inputurl.isEmpty()) {
            try {
                URL = inputurl;
            } catch (NumberFormatException e) {
                System.out.println("Using default: Bikes lol");
            }
        }

        System.out.print("Enter check interval in minutes (default 5): ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                SLEEP_MINUTES = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Using default: 5 minutes");
            }
        }

        System.out.print("Enter maximum price in won (default 30000): ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                MAX_PRICE = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Using default: 30000원");
            }
        }

        System.out.println();
    }

    private static void scrapeWithFixedLogic() throws IOException {
        Document document = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();

        Elements monitors = document.select("a[data-gtm='search_article']");
        Map<String, ItemInfo> currentItems = new HashMap<>();

        int newItems = 0;
        int priceChanges = 0;
        int affordableCount = 0;

        for (Element monitor : monitors) {
            String title = extractTitle(monitor);
            String price = extractPrice(monitor);

            if (title.isEmpty() || price.isEmpty()) continue;

            // Check if affordable
            if (!isAffordable(price)) continue;
            affordableCount++;

            // FIX: Create unique key to handle duplicate titles
            String itemKey = title + "_" + price; // Include price in key to differentiate
            currentItems.put(itemKey, new ItemInfo(price));

            if (!previousItems.containsKey(itemKey)) {
                // Truly new item
                System.out.println("🆕 NEW: " + title + " - " + price);
                newItems++;
            } else {
                // Check for actual price change
                String oldPrice = previousItems.get(itemKey).price;
                if (!oldPrice.equals(price)) {
                    System.out.println("💱 PRICE CHANGE: " + title);
                    System.out.println("   Old: " + oldPrice + " → New: " + price);
                    priceChanges++;
                }
            }
        }

        // FIX: Properly update the previous items map
        previousItems.clear();
        previousItems.putAll(currentItems);

        System.out.println("📊 Summary: " + affordableCount + " affordable items, " +
                newItems + " new, " + priceChanges + " price changes");
    }

    private static boolean isAffordable(String priceText) {
        if (priceText.equals("나눔")) return true; // Free items

        if (priceText.contains("원")) {
            try {
                String cleanPrice = priceText.replace("원", "").replace(",", "").trim();
                double price = Double.parseDouble(cleanPrice);
                return price <= MAX_PRICE;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private static String extractTitle(Element monitor) {
        Element titleElement = monitor.selectFirst("span[class*='fontSize_200']");
        if (titleElement == null) {
            titleElement = monitor.selectFirst("div[class*='color_neutral']");
        }
        if (titleElement == null) {
            titleElement = monitor.selectFirst("div[class*='sprinkles_width_full']");
        }
        return titleElement != null ? titleElement.text().trim() : "";
    }

    private static String extractPrice(Element monitor) {
        Elements spans = monitor.select("span");
        for (Element span : spans) {
            String text = span.text();
            if (text.contains("원") || text.equals("나눔")) {
                return text.trim();
            }
        }
        return "";
    }
}
