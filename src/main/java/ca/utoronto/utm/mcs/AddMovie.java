package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.sql.Driver;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class AddMovie implements HttpHandler{
  public void handle(HttpExchange r) {
    try {
      if(r.getRequestMethod().equals("PUT")) {
        handlePut(r);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  public void handlePut(HttpExchange r) throws IOException, JSONException{
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);
    
    // Don't add two actors with the same ID!!!!!!!!
    
    String movie = "";
    String movieID = "";
    if(deserialized.has("name")) {
      movie = deserialized.getString("name");
      
    }
    if(deserialized.has("movieId")) {
      movieID = deserialized.getString("movieId");
    }
    
    
    if(!deserialized.has("name") || !deserialized.has("movieId")) {
      r.sendResponseHeaders(400, -1);
    }
    else {
      Neo4jDatabase neo = new Neo4jDatabase();
      
      
      int neoReturn = neo.insertMovie(movie, movieID);
      if(neoReturn == 1) {
        r.sendResponseHeaders(500, -1);
      }
      else if(neoReturn == 2) { 
        r.sendResponseHeaders(400, -1);
      }
      else {
        r.sendResponseHeaders(200, -1);
      }
    }
  }
}
