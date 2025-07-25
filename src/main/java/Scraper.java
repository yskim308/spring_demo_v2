import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Scraper {

    public static void main(String[] args) {
        try {
            FileWriter fileWriter = new FileWriter("atcoder_results.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (int contestnumber = 42; contestnumber < 416; contestnumber++) {
                String url = "https://atcoder.jp/contests/abc" + String.format("%03d", contestnumber) + "/tasks";
                try {
                    Document contestdocument = null;
                    int maxRetries = 3;
                    int retryCount = 0;

                    while (contestdocument == null && retryCount < maxRetries) {
                        try {
                            contestdocument = Jsoup.connect(url).get();
                        } catch (org.jsoup.HttpStatusException e) {
                            if (e.getStatusCode() == 429 || e.getStatusCode() == 403) {
                                retryCount++;
                                System.out.println("Rate limited (429). Waiting 5 seconds before retry " + retryCount + "/" + maxRetries);
                                Thread.sleep(5000); // Wait 5 seconds
                                if (retryCount >= maxRetries) {
                                    System.out.println("Max retries reached for: " + url);
                                    continue; // Skip to next contest
                                }
                            } else {
                                throw e; // Re-throw other HTTP errors
                            }
                        }
                    }

                    if (contestdocument == null) {
                        continue; // Skip this contest if we couldn't get the document
                    }

                    Elements rows = contestdocument.select("tbody tr");

                    System.out.println("======================================");
                    System.out.println("contest number: " + contestnumber);
                    System.out.println("contest URL: " + url);
                    System.out.println("======================================");
                    printWriter.println("======================================");
                    printWriter.println("contest number: " + contestnumber);
                    printWriter.println("contest URL: " + url);
                    printWriter.println("======================================" + "\n");
                    for (Element row : rows) {
                        Element link = row.selectFirst("a[href]");
                        if (link != null) {
                            String relativeurl = link.attr("href");
                            String tasktitle = link.text();

                            String taskurl = "https://atcoder.jp" + relativeurl;
                            System.out.println("Task: " + tasktitle);
                            System.out.println("URL: " + taskurl);
                            printWriter.println("Task: " + tasktitle + "\n");
                            printWriter.println("URL: " + taskurl + "\n");

                            try {
                                Document taskdocument = Jsoup.connect(taskurl).get();

                                // Extract title - AtCoder uses span with class "h2" for the main title
                                Element titleElement = taskdocument.selectFirst("span.h2");
                                String title = titleElement != null ? titleElement.text().replace("Editorial", "").trim() : "Title not found";

                                // Extract points/score - Looking for the specific pattern in AtCoder
                                String points = "Points not found";

                                // Try multiple selectors for score
                                Elements scoreElements = taskdocument.select("p:contains(points)");
                                for (Element scoreElem : scoreElements) {
                                    String scoreText = scoreElem.text();
                                    if (scoreText.contains("points")) {
                                        // Extract number before "points"
                                        points = scoreText.replaceAll(".*?(\\d+)\\s*points.*", "$1");
                                        break;
                                    }
                                }


                                // Extract problem statement
                                Element problemSection = taskdocument.selectFirst("h3:contains(Problem Statement)");
                                String problemStatement = "Problem statement not found";

                                if (problemSection != null) {
                                    Element parentDiv = problemSection.parent();
                                    if (parentDiv != null) {
                                        // Get the next sibling elements until we hit another h3
                                        Elements problemContent = new Elements();
                                        Element nextSibling = problemSection.nextElementSibling();

                                        while (nextSibling != null && !nextSibling.tagName().equals("h3")) {
                                            problemContent.add(nextSibling);
                                            nextSibling = nextSibling.nextElementSibling();
                                        }

                                        StringBuilder sb = new StringBuilder();
                                        for (Element elem : problemContent) {
                                            sb.append(elem.text()).append(" ");
                                        }
                                        problemStatement = sb.toString().trim();
                                    }
                                }

                                System.out.println("Title: " + title);
                                System.out.println("Points: " + points);
                                System.out.println("Problem Statement: " + problemStatement);
                                System.out.println("---");
                                printWriter.println("Title: " + title + "\n");
                                printWriter.println("Points: " + points + "\n");
                                printWriter.println("Problem Statement: " + problemStatement + "\n");
                                printWriter.println("---" + "\n");
                                try {
                                    Thread.sleep(100); // Wait 0.5 second between each contest
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            } catch (IOException e) {
                                System.out.println("Error fetching task page: " + e.getMessage());
                                printWriter.println("Error fetching task page: " + e.getMessage());
                            }
                        }
                    }
                    System.out.println("======================================");
                    printWriter.println("======================================" + "\n" + "\n");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Sleep interrupted");
                    printWriter.println("Sleep interrupted");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error creating or writing to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
