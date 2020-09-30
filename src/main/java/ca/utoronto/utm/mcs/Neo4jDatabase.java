package ca.utoronto.utm.mcs;

import static org.neo4j.driver.Values.parameters;
import java.io.OutputStream;
import java.util.List;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;


public class Neo4jDatabase {
  
  private Driver driver;
  private String uriDb;
  private String Response;
  
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
    try(Session session = driver.session()){
      try(Transaction tx = session.beginTransaction()){ 
        Result result = tx.run("MATCH (j:actor {id:$x}) \nRETURN j", parameters("x", actorID)); 
        if(!result.hasNext()) {
          return 2; 
        }
        int temp = 0; 
        this.Response = "{\n    \"actorId\": \""  + actorID + "\", \n    \"name\": \"" + result.next().get("j.Name").asString() + "\",\n    \"movies\": [\n" ;
        Result result2 = tx.run("MATCH (:actor {id:$x})-->(movie) \nRETURN movie.id", parameters("x", actorID)); 
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
  
  public String getResponce() { 
    return this.Response; 
  }
}
