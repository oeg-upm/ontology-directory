import static spark.Spark.*;

import org.apache.jena.sparql.resultset.ResultsFormat;

import sparql.streamline.core.EndpointConfiguration;
import sparql.streamline.core.SparqlEndpoint;
import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;

import java.io.ByteArrayOutputStream;

public class Service {
	/*
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
	
	*/
	
	public static void main (String[] args) {
		//Iniciamos la conexión con el servicio de SPARQL de GraphDB para hacer la query.
		//HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/OntologyDirectory");
	
		//Probamos con la librería de Andrea
		//Creamos objeto Endpoint Configuration para acceder a DBGraph
		EndpointConfiguration ec = new EndpointConfiguration();
		String endpoint = "http://localhost:7200/repositories/OntologyDirectory";
		String endpointupdate = "http://localhost:7200/repositories/OntologyDirectory/statements";
		ec.setSparqlQuery(endpoint);
		ec.setSparqlUpdate(endpointupdate);
		SparqlEndpoint.setConfiguration(ec);	

		
		//GET para que devuelva el conjunto de tripletas guardadas
		get("/OD", (request, response) -> {
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?x ?name ?url\r\n"
					+ "WHERE {\r\n"
					+ "    ?x foaf:name ?name.\r\n"
					+ "    ?x foaf:homepage ?url\r\n"
					+ "}";
		
			//Creamos objeto SPARQLEndpoint
			String res = null;
			try {
				ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
				res=res0.toString();
			} catch (SparqlQuerySyntaxException e) {
				e.printStackTrace();
			} catch (SparqlRemoteEndpointException e) {
				e.printStackTrace();
			}
			return res;
	     });

		
		//GET Para que busque un elemento dentro de la lista al incluirlo en la URL.
		get("/OD/*/URL", (request, response) -> {
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<request.splat().length;i++)
					sb.append(request.splat()[i]);
				String name = sb.toString();
				String query = "BASE <http://localhost:4567/>\r\n"
		                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
		                + "SELECT ?x ?url\r\n"
		                + "WHERE {\r\n"
		                + "    ?x foaf:name \""+ name +"\".\r\n"
		                + "    ?x foaf:homepage ?url\r\n"
		                + "}\r\n"
		                + "";
				String res = null;
				try {
					ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
					res=res0.toString();
				} catch (SparqlQuerySyntaxException e) {
					e.printStackTrace();
				} catch (SparqlRemoteEndpointException e) {
					e.printStackTrace();
				}
				return res;
	     });
		
		
		//POST Para crear una URL dentro de la lista de estas. 
		post("/OD/*/URL", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String url = request.queryParams("url"); 
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n"
					+ "	<http://localhost:4567/"+name.replace(" ","")+"> foaf:name \""+name+"\"; \r\n"
					+ "    							foaf:homepage <"+url+">.\r\n"
					+ "} ";
			try {
				SparqlEndpoint.update(query);
			} catch (SparqlQuerySyntaxException e) {
				e.printStackTrace();
			} catch (SparqlRemoteEndpointException e) {
				e.printStackTrace();
			}
			return "Tripleta Añadida";
		});
		
		
		
		///DELETE Para eliminar una URL de dentro de la lista de estas. 
		delete("/OD/*/URL", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String query ="BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "DELETE {\r\n"
					+ "    ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "}\r\n"
					+ "WHERE{\r\n"
					+ "     ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "     FILTER(str(?name) = \""+name+"\") \r\n"
					+ "}";
			SparqlEndpoint.update(query);
		   return "Tripleta Eliminada";
		});
		
		//TODO: Hacer PUT para Graph DB.
		//PUT para modificar los valores de la URL de una determinada ontología 
		put("/OD/*/URL", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String url = request.queryParams("url");
			String query ="BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "DELETE {\r\n"
					+ "    ?x foaf:homepage ?url.\r\n"
					+ "}\r\n"
					+ "WHERE{\r\n"
					+ "     ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "     FILTER(str(?name) = \""+name+"\") \r\n"
					+ "}";
			System.out.println(query);
			SparqlEndpoint.update(query);
			String query1 = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n"
					+ "	<http://localhost:4567/"+name.replace(" ","")+"> foaf:homepage <"+url+">.\r\n"
					+ "} ";
			System.out.println(query1);
			SparqlEndpoint.update(query1);
		   return "Actualizada";
		});
		
				
		//--------------------------------------------------------------------------------------------------------------------------------------------------------------------
		//AQUÍ COMIENZA LA PARTE DEL SERVICIO EN LA QUE SE TRABAJA CON OWL COMO SE ESPECIFICA EN EL UC2
		
		}
	}
