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
      session.writeTransaction(tx -> tx.run("MERGE (a:actor {Name: $x, id: $y})"
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
      session.writeTransaction(tx -> tx.run("MERGE (a:movie {Name: $x, id: $y})"
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
        tx.close();
      }catch(Exception e){ 
        return 1; 
      }
      session.writeTransaction(tx -> tx.run("MATCH (a:actor {id:$x})," + "(b:movie {id:$y})\n" + "MERGE (a)-[r:WORK]->(b)\n" + "RETURN r", parameters("x", actorID, "y", movieID))); 
      session.close(); 
      return 0;
    }
    catch(Exception e){
      return 1;
    }
  }
  
  public int getActor(String actorID) { 
    this.Response = "";
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){ 
        Result result = tx.run("MATCH (j:actor {id:$x}) \nRETURN j.Name", parameters("x", actorID)); 
        if(!result.hasNext()) {
          return 2; 
        }
        int temp = 0; 
        //System.out.println("HERE " + result.next().toString());
//        Value letsee = result.next().get("j");
//        System.out.println("HERE: " + letsee.toString());
        
        
        this.Response = "{\n    \"actorId\": \""  + actorID + "\", \n    \"name\": \"" + result.next().get("j.Name").asString() + "\",\n    \"movies\": [\n" ;
        
        Result result2 = tx.run("MATCH (:actor {id:$x})-->(movie) \nRETURN movie.id", parameters("x", actorID)); 
        
        //System.out.println("HERE2 " + result2.next().toString());
        //Result result2 = tx.run("MATCH (a:actor {id:$x})-[:WORK]->(movie) RETURN a,movie", parameters("x", actorID)); 
        while(result2.hasNext()){ 
          temp = 1; 
          this.Response += "          \"" + result2.next().get("movie.id").asString() + "\",\n"; 
        }
        if(temp == 1) { 
          this.Response += "          ...\n"; 
        }
        this.Response += "    ]\n}\n"; 
        System.out.println(this.Response); 
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
    this.Response = "";
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){
        
        Result result = tx.run("MATCH (j:movie {id:$x}) \n RETURN j.Name", parameters("x", movieID));
        if(!result.hasNext()) {
          return 2;
        }
        int temp = 0;
        
        this.Response = "{\n    \"movieId\": \""  + movieID + "\", \n    \"name\": \"" + result.next().get("j.Name").asString() + "\",\n    \"actors\": [\n" ;
        
         
        Result result2 = tx.run("MATCH (:movie {id:$x})<--(actor) \n return actor.id", parameters("x", movieID));
        //System.out.println("HERE: " + result2.next().toString());
        while(result2.hasNext()) {
          temp = 1;
          this.Response += "          \"" + result2.next().get("actor.id").asString() + "\",\n"; 
        }
        if(temp == 1) {
          this.Response += "          ...\n"; 
        }
        this.Response += "    ]\n}\n";
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
    this.Response = "";
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){
        
        Result result = tx.run("MATCH (j:actor{id:$x}) -[r]->(m:movie{id:$y}) RETURN r", parameters("x", actorID, "y", movieID));
        
        this.Response += "{\n    \"actorId\": \"" + actorID + "\", \n    \"movieId: \"" + movieID + "\"\n    \"hasRelationship\": ";
        
        
        if(result.hasNext()) {
          tx.close();
          session.close();
          this.Response += "true\n";
          this.Response += "}\n";
          return 0;
        }
        else {
          tx.close();
          session.close();
          this.Response += "false\n";
          this.Response += "}\n";
          return 1;
        }
        
      }catch(Exception e) {
        session.close();
        return 2;
      }

    }catch(Exception e) {
      return 2;
     
    }
  }
  
  
  
  public int computeBaconNumber(String actorID) {
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){
        
        Result result = tx.run("MATCH (start:actor {id:$x}),(KevBac:actor {Name: 'Kevin Bacon' }), p = shortestPath((start)-[*..]-(KevBac)) RETURN p", parameters("x", actorID));
        

        
        int size = result.next().get("p").size();
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
        
        Result result = tx.run("MATCH (start:actor {id:$x}),(KevBac:actor {Name: 'Kevin Bacon' }), p = shortestPath((start)-[*..]-(KevBac)) return [node in nodes(p) | node.id] as nodesInPath", parameters("x", actorID));
        
        Value temp = result.next().get("nodesInPath");

        
        ArrayList<JSONObject> tracker = new ArrayList<>();
        
        ArrayList<String> nodes = new ArrayList<>();
        ArrayList<String> movies = new ArrayList<>();
        
        
        for(int i = 0; i < temp.size()-1; i+=2) {
          if(i%2 == 0) {
            nodes.add(temp.get(i).toString().replaceAll("\"", ""));
          }
          else {
            movies.add(temp.get(i).toString().replaceAll("\"", ""));
          }
        }
        
        for(int i = 0; i < nodes.size(); i++) {
//          if(i > 0 && i != nodes.size()) {
//            
//          }
          JSONObject temporary = new JSONObject();
          int movieTracker = -1;
          if(i%2 == 0 & (temp.size() > (movieTracker + 2))) {
            movieTracker += 2;
          }
          temporary.put("actorId", nodes.get(i));
          temporary.put("movieId", temp.get(movieTracker));
          tracker.add(temporary);
        }
        
        
        
//        
//        for(int i = 0; i < temp.size()-1; i++) {
//          int movieTracker = -1;
//          if(i%2 == 0 && (temp.size() > (movieTracker + 2)) ) {
//            movieTracker += 2;
//          }
//          
//          JSONObject temporary = new JSONObject();
//          
//          String toRead = temp.get(i).toString().replaceAll("\"", "");
//          String movieValue = temp.get(movieTracker).toString().replaceAll("\"", "");
//          //System.out.println("HERE: " + toRead);
//          temporary.put("actorId", toRead);
//          temporary.put("movieId", movieValue);
//          tracker.add(temporary);
//          
//        }
        
        deserialized.put("baconPath", tracker);
        
        //System.out.println(result.next().get("nodesInPath").toString());
        
        //System.out.println("HERE: " + result.next().toString());
        return 0;
        
        
      }catch(Exception e) {
        System.out.println(e);
      }
      
    }catch(Exception e) {
      System.out.println(e);
    }
    
    
    return 1;
    
    
  }
  
  
  
  public String getResponse() { 
    return this.Response; 
  }
  
  public JSONObject getJSON() {
    return this.deserialized;
  }
}
