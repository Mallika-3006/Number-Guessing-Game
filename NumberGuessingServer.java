import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class NumberGuessingServer {

    static int randomNumber;
    static int attempts = 0;
    static int maxRange = 100;

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            byte[] response = java.nio.file.Files.readAllBytes(new File("index.html").toPath());
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        server.createContext("/start", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String difficulty = query.split("=")[1];

            if (difficulty.equals("easy")) {
                maxRange = 50;
            } else if (difficulty.equals("medium")) {
                maxRange = 100;
            } else {
                maxRange = 200;
            }

            randomNumber = new Random().nextInt(maxRange) + 1;
            attempts = 0;

            String res = "Game started! Guess a number between 1 and " + maxRange;
            exchange.sendResponseHeaders(200, res.length());
            exchange.getResponseBody().write(res.getBytes());
            exchange.close();
        });

        server.createContext("/guess", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String[] parts = query.split("&");

            int guess = Integer.parseInt(parts[0].split("=")[1]);
            String username = parts[1].split("=")[1];
            long timeTaken = Long.parseLong(parts[2].split("=")[1]);

            attempts++;

            String result;

            if (guess == randomNumber) {
                result = "Correct! You guessed the number in " + attempts +
                        " attempts. Time taken: " + timeTaken + " seconds.";

                saveScore(username, attempts, timeTaken);

                randomNumber = new Random().nextInt(maxRange) + 1;
                attempts = 0;
            } else {
                String direction;

                if (guess < randomNumber) {
                    direction = "Too Low!";
                } else {
                    direction = "Too High!";
                }

                String hint = generateHint(guess);
                result = direction + " Hint: " + hint;
            }

            exchange.sendResponseHeaders(200, result.length());
            exchange.getResponseBody().write(result.getBytes());
            exchange.close();
        });

        server.createContext("/scores", exchange -> {
            File file = new File("scores.txt");
            String content = "";

            if (file.exists()) {
                content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            }

            exchange.sendResponseHeaders(200, content.length());
            exchange.getResponseBody().write(content.getBytes());
            exchange.close();
        });

        server.start();
        System.out.println("Server running at http://localhost:8080");
    }

    static String generateHint(int guess) {
        String hint = "";

        if (randomNumber % 2 == 0) {
            hint += "The number is even. ";
        } else {
            hint += "The number is odd. ";
        }

        if (randomNumber % 10 == 0) {
            hint += "It is divisible by 10. ";
        } else if (randomNumber % 5 == 0) {
            hint += "It is divisible by 5. ";
        } else if (randomNumber % 3 == 0) {
            hint += "It is divisible by 3. ";
        } else {
            hint += "It is not divisible by 3, 5, or 10. ";
        }

        int difference = Math.abs(randomNumber - guess);

        if (difference <= 5) {
            hint += "You are very close!";
        } else if (difference <= 15) {
            hint += "You are close!";
        } else {
            hint += "You are far away!";
        }

        return hint;
    }

    static void saveScore(String name, int attempts, long time) {
        try (FileWriter fw = new FileWriter("scores.txt", true)) {
            fw.write(name + "," + attempts + "," + time + " seconds\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}