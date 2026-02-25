package com.arep.microframeworks_web.app_examples;

import static com.arep.microframeworks_web.HttpServer.get;

import java.io.IOException;
import java.net.URISyntaxException;

import com.arep.microframeworks_web.HttpServer;

public class MathServices {
    public static void main(String[] args) throws IOException, URISyntaxException {
        get("/pi", (req, res) -> "PI= " + Math.PI);
        get("/hello", (req, res) -> "Hello: " + req.getValue("name"));
        get("/euler", (req, res) -> getEuler());

        HttpServer.main(args);
    }

    private static String getEuler() {
        return "e: " + Math.E;
    }

}
