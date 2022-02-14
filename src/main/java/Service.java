import static spark.Spark.*;
import java.util.ArrayList;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;

import com.ontotext.graphdb.example.util.EmbeddedGraphDB;

import java.io.IOException;
import java.net.*;

public class Service {
	
	//Método auxiliar al que pasándole la query a realizar hace todo lo requerido 
	public static String llamadaDBGraph(HTTPRepository repository,String query) {
		//Creamos la conexión
		RepositoryConnection conexion = repository.getConnection();
		System.out.println("Conexión realizada");
		//Se prepara y evalúa query que pide id, nombre y URL de las ontolgías de OntologyDirectory.
		TupleQuery tupleQuery = conexion.prepareTupleQuery(QueryLanguage.SPARQL,query);
        TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
        System.out.println("Query evaluada");
        // Each result is represented by a BindingSet, which corresponds to a result row
        String res= null;
        //Al ser iterable almacenamos los diferentes elementos en un string a devolver.
        while(tupleQueryResult.hasNext()) {
        	BindingSet bindingSet = tupleQueryResult.next();
            // Each BindingSet contains one or more Bindings
            for (Binding binding : bindingSet) {
                // Each Binding contains the variable name and the value for this result row
                String name = binding.getName();
                Value value = binding.getValue();
               res += name + " = " + value + "\n";       
            }
            res += "\n";
        }
        System.out.println("Respuesta impresa");
        //Cerramos la tupla y las conexiones.
        tupleQueryResult.close();
        conexion.close();
        System.out.println("Conexiones y querys cerradas");
		return res;
	}
	
	
	
	public static void main (String[] args) {
		
		//Iniciamos la conexión con el servicio de SPARQL de GraphDB para hacer la query.
		HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/OntologyDirectory");
		
		
		//GET para que devuelva el conjunto de URLs guardadas
		get("/OD", (request, response) -> {
			String query = "BASE <http://localhost:4567/>\r\n"
	                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
	                + "SELECT ?x ?name ?url\r\n"
	                + "WHERE {\r\n"
	                + "    ?x foaf:name ?name.\r\n"
	                + "    ?x foaf:homepage ?url\r\n"
	                + "}\r\n"
	                + "";
			String res = llamadaDBGraph(repository,query);
			return res;
	     });

		
		//GET Para que busque un elemento dentro de la lista al incluirlo en la URL.
		get("/OD/*", (request, response) -> {
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<request.splat().length;i++)
					sb.append(request.splat()[i]);
				String name = sb.toString();
				System.out.println(name);
				String query = "BASE <http://localhost:4567/>\r\n"
		                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
		                + "SELECT ?x ?url\r\n"
		                + "WHERE {\r\n"
		                + "    ?x foaf:name \""+ name +"\".\r\n"
		                + "    ?x foaf:homepage ?url\r\n"
		                + "}\r\n"
		                + "";
				System.out.println(query);
				String res = llamadaDBGraph(repository,query);
				return res;
	     });
		
		
		//TODO: Adaptarlo a GraphDB
		//POST Para crear una URL dentro de la lista de estas. 
		post("/OD/", (request, response) -> {
			String url = request.queryParams("url");
			String name = request.queryParams("name"); 
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n"
					+ "	<http://localhost:4567/#"+name.replace(" ","")+"> foaf:name \""+name+"\"; \r\n"
					+ "    							foaf:homepage <"+url+">.\r\n"
					+ "} ";
			System.out.println(query);
			String res = llamadaDBGraph(repository,query);
		    return res;
		});
		
		//TODO: Adaptarlo a GraphDB
		///DELETE Para eliminar una URL de dentro de la lista de estas. 
		delete("/OD/del/", (request, response) -> {
		/*   for (int k = 0; k<list.size(); k++) {
			   if (list.get(k).toString().equals(request.queryParams("URL"))) {
				   list.remove(k);
				   return "URL Removed";
			   }
		   }*/
		   return "URL not present in list";
		});
		
		//TODO: Hacer PUT para Graph DB.
		//PUT a estas alturas no tiene mucho sentido debido a que las URLs solo contienen un valor y actualizarlas sería equivalente a eliminar una y añadir otra asique no vamos a implementar la operación de momento.
			
		//--------------------------------------------------------------------------------------------------------------------------------------------------------------------
		//AQUÍ COMENTA LA PARTE DEL SERVICIO EN LA QUE SE TRABAJA CON OWL COMO SE ESPECIFICA EN EL UC2
		
		}
	}
