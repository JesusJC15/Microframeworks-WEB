package com.arep.microframeworks_web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class AppTest {

    @BeforeEach
    public void resetHttpServerState() throws Exception {
        Field endPointsField = HttpServer.class.getDeclaredField("endPoints");
        endPointsField.setAccessible(true);
        Map<String, WebMethod> endPoints = (Map<String, WebMethod>) endPointsField.get(null);
        endPoints.clear();

        HttpServer.staticfiles("webroot/public");
        HttpServer.setPort(8080);
    }

    @Test
    public void shouldExtractQueryValuesFromRequest() {
        HttpRequest request = new HttpRequest("GET", "/App/hello", "name=Pedro&city=Bogota%20DC");

        assertEquals("GET", request.getMethod());
        assertEquals("/App/hello", request.getPath());
        assertEquals("Pedro", request.getValues("name"));
        assertEquals("Bogota DC", request.getValue("city"));
        assertEquals(2, request.getQueryParams().size());
    }

    @Test
    public void shouldRegisterGetEndpointAndExecuteLambda() throws Exception {
        HttpServer.get("/hello", (req, res) -> "hello " + req.getValues("name"));

        Field endPointsField = HttpServer.class.getDeclaredField("endPoints");
        endPointsField.setAccessible(true);
        Map<String, WebMethod> endPoints = (Map<String, WebMethod>) endPointsField.get(null);

        WebMethod method = endPoints.get("/hello");
        assertNotNull(method);

        String result = method.execute(new HttpRequest("GET", "/hello", "name=Pedro"), new HttpResponse());
        assertEquals("hello Pedro", result);
    }

    @Test
    public void shouldConfigureStaticFilesFolder() throws Exception {
        HttpServer.staticfiles("/webroot");

        Field locationField = HttpServer.class.getDeclaredField("staticFilesLocation");
        locationField.setAccessible(true);

        String staticLocation = (String) locationField.get(null);
        assertEquals("/webroot", staticLocation);
    }
}
