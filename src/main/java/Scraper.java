import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Scraper {

    public static void main(String[] args) {

        for (int contestnumber = 42; contestnumber < 416; contestnumber++) {
            String url = "https://atcoder.jp/contests/abc" + String.format("%03d", contestnumber) + "/tasks";
            try {
                Document document = Jsoup.connect(url).get();
                Elements rows = document.select("tbody tr");

                System.out.println("======================================");
                System.out.println("contest number: " + contestnumber);
                System.out.println("======================================");
                for (Element row : rows) {
                    Element link = row.selectFirst("a[href]");
                    if(link != null){
                        String relativeurl = link.attr("href");
                        String tasktitle = link.text();

                        String gotourl = "https://atcoder.jp" + relativeurl;
                        System.out.println("Task: " + tasktitle);
                        System.out.println("URL: " + gotourl);
                        System.out.println("---");
                    }
                }
                System.out.println("======================================");
            } catch (IOException e) {
                e.printStackTrace(); // creatint this instead of a getmessage is good because there is a hierarchy in printstacktrace
            }
        }
    }
}