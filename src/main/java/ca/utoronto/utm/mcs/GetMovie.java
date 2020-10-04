package ca.utoronto.utm.mcs;
import java.io.IOException;
import java.sql.Driver;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;


public class GetMovie implements HttpHandler{

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

  private void handleGet(HttpExchange r) throws IOException, JSONException{
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);
    
    String actorID = ""; 
    if(deserialized.has("movieId")) {
      actorID = deserialized.getString("movieId");
    }
    
    if(!deserialized.has("movieId")) {
      r.sendResponseHeaders(400, -1);
    }
    else { 
      Neo4jDatabase neo = new Neo4jDatabase();
      int neoReturn = neo.getMovie(actorID);
      JSONObject response = neo.getJSON(); 
      
      if(neoReturn == 1) {
        r.sendResponseHeaders(500, -1);
      }
      else if(neoReturn == 2) { 
        r.sendResponseHeaders(404, -1);
      }
      else {
        r.sendResponseHeaders(200, response.toString().length());
        OutputStream os = r.getResponseBody();
        os.write((response.toString()).getBytes());
        os.close();
      }
    }  
  }

}
