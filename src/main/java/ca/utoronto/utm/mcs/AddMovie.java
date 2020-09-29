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
    if(deserialized.has("Name")) {
      movie = deserialized.getString("Name");
      
    }
    if(deserialized.has("id")) {
      movieID = deserialized.getString("id");
    }
    
    
    if(!deserialized.has("Name") || !deserialized.has("id")) {
      r.sendResponseHeaders(400, 16);
      OutputStream os = r.getResponseBody();
      os.write("400 BAD REQUEST\n".getBytes());
      os.close();
    }
    else {
      Neo4jDatabase neo = new Neo4jDatabase();
      
      
      int neoReturn = neo.insertMovie(movie, movieID);
      if(neoReturn == 1) {
        r.sendResponseHeaders(500, 26);
        OutputStream os = r.getResponseBody();
        os.write("500 INTERNAL SERVER ERROR\n".getBytes());
        os.close();
      }
      else {
        r.sendResponseHeaders(200, 7);
        OutputStream os = r.getResponseBody();
        os.write("200 Ok\n".getBytes());
        os.close();
      }
    }
    

    
  }
}
