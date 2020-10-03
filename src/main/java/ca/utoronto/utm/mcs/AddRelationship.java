package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.sql.Driver;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class AddRelationship implements HttpHandler{

  @Override
  public void handle(HttpExchange r) throws IOException {
    try {
      if(r.getRequestMethod().equals("PUT")) {
        handlePut(r);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handlePut(HttpExchange r) throws IOException, JSONException{
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
    }
    else {
      Neo4jDatabase neo = new Neo4jDatabase();
      int neoReturn = neo.insertRelationship(actorID, movieID);
      
      if(neoReturn == 1) {
        r.sendResponseHeaders(500, 26);
      }
      else if(neoReturn == 2) { 
        r.sendResponseHeaders(404, 16);
      }
      else {
        r.sendResponseHeaders(200, 7);
      }
    }
  }
  
}
