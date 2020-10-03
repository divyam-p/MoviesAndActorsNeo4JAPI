package ca.utoronto.utm.mcs;

import static org.neo4j.driver.Values.parameters;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Path;


public class Neo4jDatabase {
  
  private Driver driver;
  private String uriDb;
  private String Response = "";
  private JSONObject deserialized = new JSONObject();
  
  public Neo4jDatabase() {
    uriDb = "bolt://localhost:7687";
    driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j","1234"));
  }
  
  public int insertActor(String name, String id) {
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){ 
        Result result = tx.run("MATCH (j:actor {id:$x}) \nRETURN j", parameters("x", id)); 
        Result result2 = tx.run("MATCH (j:actor {name:$x}) \nRETURN j", parameters("x", name)); 
        if(result.hasNext() || result2.hasNext()){
          return 2;
        }
      }catch(Exception e) {
        return 1;
      }
      session.writeTransaction(tx -> tx.run("MERGE (a:actor {name: $x, id: $y})"
          , parameters("x", name, "y", id)));
      session.close();
      return 0;
    }
    catch(Exception e){
      return 1;
    }
  }
 
  public int insertMovie(String name, String id) {
    try(Session session = driver.session()){
      
      try(Transaction tx = session.beginTransaction()){ 
        Result result = tx.run("MATCH (j:movie {id:$x}) \nRETURN j", parameters("x", id)); 
        Result result2 = tx.run("MATCH (j:movie {name:$x}) \nRETURN j", parameters("x", name)); 
        if(result.hasNext() || result2.hasNext()){
          return 2;
        }
      }catch(Exception e) {
        return 1;
      }
      session.writeTransaction(tx -> tx.run("MERGE (a:movie {name: $x, id: $y})"
          , parameters("x", name, "y", id)));
      session.close();
      return 0;
    }
    catch(Exception e){
      return 1;
    }
  }
  
  
  public int insertRelationship(String actorID, String movieID) { 
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){ 
        Result result = tx.run("MATCH (j:actor {id:$x}) \nRETURN j", parameters("x", actorID)); 
        if(!result.hasNext()) {
          return 2; 
        }
        Result result2 = tx.run("MATCH (k:movie {id:$x}) \nRETURN k", parameters("x", movieID)); 
        if(!result2.hasNext()) { 
          return 2; 
        }
        Result result3 = tx.run("MATCH path=(a:actor {id: $x})-[r:ACTED_IN]-(b:movie {id: $y})\r\n" + 
            "RETURN path;", parameters("x", actorID, "y", movieID)); 
        if(result3.hasNext()) {
          return 3;
        }   
        tx.close();
      }catch(Exception e){ 
        return 1; 
      }
      session.writeTransaction(tx -> tx.run("MATCH (a:actor {id:$x})," + "(b:movie {id:$y})\n" + "MERGE (a)-[r:ACTED_IN]->(b)\n" + "RETURN r", parameters("x", actorID, "y", movieID))); 
      session.close(); 
      return 0;
    }
    catch(Exception e){
      return 1;
    }
  }
  
  public int getActor(String actorID) { 
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){ 
        Result result = tx.run("MATCH (j:actor {id:$x}) \nRETURN j.name", parameters("x", actorID)); 
        if(!result.hasNext()) {
          return 2; 
        }

        deserialized.put("actorId", actorID);
        deserialized.put("name", result.next().get("j.name").asString());
        
        Result result2 = tx.run("MATCH (:actor {id:$x})-->(movie) \nRETURN movie.id", parameters("x", actorID)); 
        ArrayList<String> movieTracker = new ArrayList<>();
        while(result2.hasNext()){ 
          movieTracker.add(result2.next().get("movie.id").asString());
        } 
        
        deserialized.put("movies", movieTracker);
        tx.close();
      }catch(Exception e){ 
        return 1; 
      }
      session.close(); 
      return 0;
    }
    catch(Exception e){
      return 1;
    }
  }
  
  public int getMovie(String movieID) {
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){
        
        Result result = tx.run("MATCH (j:movie {id:$x}) \n RETURN j.name", parameters("x", movieID));
        if(!result.hasNext()) {
          return 2;
        }
        
        deserialized.put("name", result.next().get("j.name").asString());
        deserialized.put("movieId", movieID);
        
        Result result2 = tx.run("MATCH (:movie {id:$x})<--(actor) \n return actor.id", parameters("x", movieID));
        
        ArrayList<String> actorTracker = new ArrayList<>();
        
        while(result2.hasNext()) {
          actorTracker.add(result2.next().get("actor.id").asString());
        }
        deserialized.put("actors", actorTracker);
        tx.close();
      }catch(Exception e) {
        return 1;
      }
      session.close();
      return 0;
      
    }catch(Exception e) {
      return 1;
    }
  }
  
  
  public int hasRelationship(String actorID, String movieID) {
    //this.Response = "";
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){
        
        
        Result result2 = tx.run("MATCH (j:actor {id:$x}) \nRETURN j", parameters("x", actorID)); 
        Result result3 = tx.run("MATCH (j:movie {id:$x}) \nRETURN j", parameters("x", movieID)); 
        
        if(!result2.hasNext() || !result3.hasNext()) {
          return 2;
        }
        
        
        Result result = tx.run("MATCH (j:actor{id:$x}) -[r]->(m:movie{id:$y}) RETURN r", parameters("x", actorID, "y", movieID));
        
        
        deserialized.put("actorId", actorID);
        deserialized.put("movieId", movieID);
        
        
        
        if(result.hasNext()) {
          tx.close();
          session.close();
          deserialized.put("hasRelationship", true);
          this.Response += "true\n";
          return 0;
        }
        else {
          tx.close();
          session.close();
          deserialized.put("hasRelationship", false);
          return 0;
        }
        
      }catch(Exception e) {
        return 1;
      }

    }catch(Exception e) {
      return 1;
     
    }
  }
  
  
  
  public int computeBaconNumber(String actorID) {
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){
        Result result2 = tx.run("MATCH (j:actor {id:$x}) \nRETURN j.id", parameters("x", actorID)); 
        if(!result2.hasNext()) { 
          return 3; 
        }
        else if(result2.next().get("j.id").toString().equals("\"nm0000102\"")){ 
          deserialized.put("baconNumber", "0");
          tx.close();
          session.close();
          return 0;
        }
        Result result = tx.run("MATCH (start:actor {id:$x}),(KevBac:actor "
            + "{id: \"nm0000102\" }), p = "
            + "shortestPath((start)-[*..]-(KevBac)) RETURN p", 
            parameters("x", actorID));
        
        if(!result.hasNext()) {
          return 2;
        }
        int size = result.next().get("p").size()/2;
        String newSize = Integer.toString(size);    
        deserialized.put("baconNumber", newSize);
        tx.close();
        session.close();
        return 0;
      }catch(Exception e) {
        return 1;
      }
    }catch(Exception e) {
      return 1;
    }
  }
  
  
  
  public int computeBaconPath(String actorID) {
    deserialized = new JSONObject();
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){
        ArrayList<JSONObject> tracker = new ArrayList<>(); 
        
        int returnValue = this.computeBaconNumber(actorID); 
        if(returnValue == 1) { 
          return 1; 
        }
        else if(returnValue == 2) {
          return 2;
        }
        else if(returnValue == 3) {
          return 3;
        }
        
        if(this.deserialized.getInt("baconNumber") == 0) { 
          Result result2 = tx.run("MATCH (:actor {id:$x})-->(movie) \nRETURN movie.id", parameters("x", actorID)); 
          JSONObject temporary = new JSONObject();
          temporary.put("actorId", actorID);
          //what if no movie exists!!! ? ? ? ? ?
          temporary.put("movieId", result2.next().get("movie.id").asString());
          tracker.add(temporary);
          deserialized.put("baconPath", tracker);
          tx.close();
          session.close();
          return 0; 
        }
        Result result = tx.run("MATCH (start:actor {id:$x}),(KevBac:actor {id: \"nm0000102\" }), p = shortestPath((start)-[*..]-(KevBac)) return [node in nodes(p) | node.id] as nodesInPath", parameters("x", actorID));
        
        Value temp = result.next().get("nodesInPath");
        
        for(int i = 1; i < temp.size(); i+=2) { 
          if(i%2==1) { 
              JSONObject temporary = new JSONObject();
              temporary.put("actorId", temp.get(i-1).toString().replaceAll("\"", ""));
              temporary.put("movieId", temp.get(i).toString().replaceAll("\"", ""));
              tracker.add(temporary);
              JSONObject temporary2 = new JSONObject();
              temporary2.put("actorId", temp.get(i+1).toString().replaceAll("\"", ""));
              temporary2.put("movieId", temp.get(i).toString().replaceAll("\"", ""));
              tracker.add(temporary2);
          }
        }
        
        deserialized.put("baconPath", tracker);
        tx.close();
        session.close();
        return 0;
      }catch(Exception e) {
        return 1; 
      }
      
    }catch(Exception e) {
      return 1; 
    }
  }

  public String getResponse() { 
    return this.Response; 
  }
  
  public JSONObject getJSON() {
    return this.deserialized;
  }
}
