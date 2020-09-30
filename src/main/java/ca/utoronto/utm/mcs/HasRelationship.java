package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.sql.Driver;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class HasRelationship implements HttpHandler{

  @Override
  public void handle(HttpExchange r) throws IOException {
    try {
      if(r.getRequestMethod().equals("GET")) {
        handleGet(r);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  private void handleGet(HttpExchange r) throws IOException, JSONException {
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);
    
    String actorID = ""; 
    String movieID = "";
    if(deserialized.has("actorId")) {
      actorID = deserialized.getString("actorId");
    }
    
    if(deserialized.has("movieId")) {
      movieID = deserialized.getString("movieId");
    }
    
    
    if(!deserialized.has("actorId") || !deserialized.has("movieId")) {
      r.sendResponseHeaders(400, 16);
      OutputStream os = r.getResponseBody();
      os.write("400 BAD REQUEST\n".getBytes());
      os.close();
    }
    else {
      Neo4jDatabase neo = new Neo4jDatabase();
      int neoReturn = neo.hasRelationship(actorID, movieID);
      String response = neo.getResponse(); 
      
      if(neoReturn == 1) {
        r.sendResponseHeaders(500, 26);
        OutputStream os = r.getResponseBody();
        os.write("500 INTERNAL SERVER ERROR\n".getBytes());
        os.close();
      }
      else if(neoReturn == 2) { 
        r.sendResponseHeaders(404, 16);
        OutputStream os = r.getResponseBody(); 
        os.write("404 BAD REQUEST\n".getBytes());
        os.close(); 
      }
      else {
        r.sendResponseHeaders(200, 7 + response.length());
        OutputStream os = r.getResponseBody();
        os.write(("200 ok\n" + response).getBytes());
        os.close();
      }
    }
    
    
    
    
    
  }

}
