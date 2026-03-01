package com.arep.microframeworks_web.app_examples;

import static com.arep.microframeworks_web.HttpServer.get;
import static com.arep.microframeworks_web.HttpServer.staticfiles;

import java.io.IOException;
import java.net.URISyntaxException;

import com.arep.microframeworks_web.HttpServer;

public class MathServices {
    public static void main(String[] args) throws IOException, URISyntaxException {
        staticfiles("webroot/public");

        get("/App/hello", (req, res) -> "Hello " + req.getValues("name"));
        get("/App/pi", (req, res) -> String.valueOf(Math.PI));
        get("/App/euler", (req, res) -> getEuler());

        HttpServer.main(args);
    }

    private static String getEuler() {
        return "e: " + Math.E;
    }

}
