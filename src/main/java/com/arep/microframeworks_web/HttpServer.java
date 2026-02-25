package com.arep.microframeworks_web;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class HttpServer {

    static Map<String, WebMethod> endPoints = new HashMap<>();

    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        Socket clientSocket = null;
        boolean running = true;

        while (running) {

            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;

            boolean isFirstLine = true;

            String reqpath = "";
            String reqquery = "";

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);

                if (isFirstLine) {
                    String[] flTokens = inputLine.split(" ");
                    String method = flTokens[0];
                    String sturiPath = flTokens[1];
                    String protocolVersion = flTokens[2];

                    URI uripath = new URI(sturiPath);
                    reqpath = uripath.getPath();
                    reqquery = uripath.getQuery();

                    System.out.println("Method: " + method);
                    System.out.println("Path: " + reqpath);
                    System.out.println("Protocol Version: " + protocolVersion);

                    isFirstLine = false;
                }

                if (!in.ready()) {
                    break;
                }
            }

            WebMethod currentWm = endPoints.get(reqpath);

            if (currentWm != null) {
                outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + "<!DOCTYPE html>"
                        + "<html>"
                        + "<head>"
                        + "<meta charset=\"UTF-8\">"
                        + "<title>Backend Service</title>\n"
                        + "</head>"
                        + "<body>"
                        + currentWm.execute(re)
                        + "</body>"
                        + "</html>" + inputLine;
            } else {
                outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + "<!DOCTYPE html>"
                        + "<html>"
                        + "<head>"
                        + "<meta charset=\"UTF-8\">"
                        + "<title>Title of the document</title>\n"
                        + "</head>"
                        + "<body>"
                        + "My Web Site"
                        + "</body>"
                        + "</html>" + inputLine;
            }
            out.println(outputLine);

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    public static void get(String path, WebMethod wm) {
        endPoints.put(path, wm);
    }
}
