import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Scraper {

    public static void main(String[] args) {

        for (int contestnumber = 415; contestnumber < 416; contestnumber++) {
            String url = "https://atcoder.jp/contests/abc" + String.format("%03d", contestnumber) + "/tasks";
            try {
                Document contestdocument = Jsoup.connect(url).get();
                Elements rows = contestdocument.select("tbody tr");

                System.out.println("======================================");
                System.out.println("contest number: " + contestnumber);
                System.out.println("======================================");

                for (Element row : rows) {
                    Element link = row.selectFirst("a[href]");
                    if(link != null){
                        String relativeurl = link.attr("href");
                        String tasktitle = link.text();

                        String taskurl = "https://atcoder.jp" + relativeurl;
                        System.out.println("Task: " + tasktitle);
                        System.out.println("URL: " + taskurl);

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

                        } catch (IOException e) {
                            System.out.println("Error fetching task page: " + e.getMessage());
                        }
                    }
                }
                System.out.println("======================================");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
