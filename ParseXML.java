// Example code for parsing XML
// Dr. Moushumi Sharmin (CSCI 345)
//
// Extended for Deadwood XML parsing (board.xml + cards.xml)
// Compatible with partner code: Board.getLocationByName, Board.addLocation,
// Set.addOffCardRole, SceneCard.addOnCardRole, ShotTracker.registerSet, Deck.add/shuffle

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;

public class ParseXML {

    // Build a Document from an XML file
    public Document getDocFromFile(String filename) throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = null;

        try {
            doc = db.parse(new File(filename)); // path or filename in current folder
        } catch (Exception ex) {
            System.out.println("XML parse failure: " + filename);
            ex.printStackTrace();
        }

        return doc;
    }

    // Professor demo method
    public void readBookData(Document d) {
        Element root = d.getDocumentElement();
        NodeList books = root.getElementsByTagName("book");

        for (int i = 0; i < books.getLength(); i++) {
            System.out.println("Printing information for book " + (i + 1));

            Node book = books.item(i);
            String bookCategory = book.getAttributes().getNamedItem("category").getNodeValue();
            System.out.println("Category = " + bookCategory);

            NodeList children = book.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node sub = children.item(j);

                if ("title".equals(sub.getNodeName())) {
                    String bookLanguage = sub.getAttributes().getNamedItem("lang").getNodeValue();
                    System.out.println("Language = " + bookLanguage);
                    System.out.println("Title = " + sub.getTextContent());

                } else if ("author".equals(sub.getNodeName())) {
                    System.out.println("Author = " + sub.getTextContent());

                } else if ("year".equals(sub.getNodeName())) {
                    System.out.println("Publication Year = " + sub.getTextContent());

                } else if ("price".equals(sub.getNodeName())) {
                    System.out.println("Price = " + sub.getTextContent());
                }
            }

            System.out.println();
        }
    }

    // Deadwood: board.xml
    public void parseBoardXML(String boardXmlFile, Board board, ShotTracker shots, Bank bank) throws Exception {
        if (boardXmlFile == null || boardXmlFile.isBlank()) {
            throw new IllegalArgumentException("boardXmlFile required.");
        }
        if (board == null) throw new IllegalArgumentException("Board required.");
        if (shots == null) throw new IllegalArgumentException("ShotTracker required.");
        if (bank == null) throw new IllegalArgumentException("Bank required.");

        Document d = getDocFromFile(boardXmlFile);
        if (d == null) throw new IllegalStateException("Could not parse: " + boardXmlFile);

        Element root = d.getDocumentElement(); // <board>

        // Step 1: Create all locations first (sets + trailers + office)
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (!(n instanceof Element)) continue;

            Element el = (Element) n;
            String tag = el.getTagName();

            if ("set".equals(tag)) {
                String name = el.getAttribute("name");
                Set set = new Set(name);
                board.addLocation(set);

                // Shots (count <take> nodes under <takes>)
                Element takes = firstChildElement(el, "takes");
                int count = 0;
                if (takes != null) {
                    count = takes.getElementsByTagName("take").getLength();
                }
                shots.registerSet(set, count);

                // Off-card roles: <parts><part ...>
                Element parts = firstChildElement(el, "parts");
                if (parts != null) {
                    NodeList partNodes = parts.getElementsByTagName("part");
                    for (int p = 0; p < partNodes.getLength(); p++) {
                        Element partEl = (Element) partNodes.item(p);

                        String roleName = partEl.getAttribute("name");
                        int level = parseIntSafe(partEl.getAttribute("level"), 1);
                        String line = textOfFirst(partEl, "line");

                        // Default payouts
                        // Off-card success: +$1, fail: +1 credit
                        OffCardRole r = new OffCardRole(roleName, level, 1, 0, 0, 1, line);
                        set.addOffCardRole(r);
                    }
                }

            } else if ("trailer".equals(tag)) {
                Location trailers = new Location("Trailers");
                board.addLocation(trailers);

            } else if ("office".equals(tag)) {
                Location office = new Location("Casting Office");
                board.addLocation(office);
            }
        }

        // Step 2: Wire neighbors now that everything exists
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (!(n instanceof Element)) continue;

            Element el = (Element) n;
            String tag = el.getTagName();

            Location from = null;
            if ("set".equals(tag)) {
                from = board.getLocationByName(el.getAttribute("name"));
            } else if ("trailer".equals(tag)) {
                from = board.getLocationByName("Trailers");
            } else if ("office".equals(tag)) {
                from = board.getLocationByName("Casting Office");
            }

            if (from == null) continue;

            Element neigh = firstChildElement(el, "neighbors");
            if (neigh == null) continue;

            NodeList neighborNodes = neigh.getElementsByTagName("neighbor");
            for (int k = 0; k < neighborNodes.getLength(); k++) {
                Element nbEl = (Element) neighborNodes.item(k);
                String nbName = nbEl.getAttribute("name");

                // board.xml may use name="office"/"trailer"
                Location to;
                if ("office".equalsIgnoreCase(nbName)) {
                    to = board.getLocationByName("Casting Office");
                } else if ("trailer".equalsIgnoreCase(nbName)) {
                    to = board.getLocationByName("Trailers");
                } else {
                    to = board.getLocationByName(nbName);
                }

                if (to != null) {
                    from.addNeighbor(to); // Location handles bidirectional
                }
            }
        }
    }

    // Deadwood: cards.xml
    public void parseCardsXML(String cardsXmlFile, Deck deck) throws Exception {
        if (cardsXmlFile == null || cardsXmlFile.isBlank()) {
            throw new IllegalArgumentException("cardsXmlFile required.");
        }
        if (deck == null) throw new IllegalArgumentException("Deck required.");

        Document d = getDocFromFile(cardsXmlFile);
        if (d == null) throw new IllegalStateException("Could not parse: " + cardsXmlFile);

        Element root = d.getDocumentElement(); // <cards>
        NodeList cards = root.getElementsByTagName("card");

        for (int i = 0; i < cards.getLength(); i++) {
            Element cardEl = (Element) cards.item(i);

            String title = cardEl.getAttribute("name");
            int budget = parseIntSafe(cardEl.getAttribute("budget"), 0);

            SceneCard card = new SceneCard(title, budget);

            // On-card roles are nested <part> nodes
            NodeList parts = cardEl.getElementsByTagName("part");
            for (int p = 0; p < parts.getLength(); p++) {
                Element partEl = (Element) parts.item(p);

                String roleName = partEl.getAttribute("name");
                int level = parseIntSafe(partEl.getAttribute("level"), 1);
                String line = textOfFirst(partEl, "line");

                // Default payouts
                // On-card success: +2 credits, fail: +0
                OnCardRole r = new OnCardRole(roleName, level, 0, 2, 0, 0, line);
                card.addOnCardRole(r);
            }

            deck.add(card);
        }

        deck.shuffle();
    }

    // ---------------- XML Helpers ----------------

    private static Element firstChildElement(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) return null;
        return (Element) list.item(0);
    }

    private static String textOfFirst(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) return "";
        return list.item(0).getTextContent().trim();
    }

    private static int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }
}