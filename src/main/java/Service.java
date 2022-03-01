import static spark.Spark.*;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.eclipse.jetty.server.Response;

import sparql.streamline.core.EndpointConfiguration;
import sparql.streamline.core.SparqlEndpoint;
import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Iterator;

public class Service {

	
	public static void main (String[] args) {
		//Iniciamos la conexi�n con el servicio de SPARQL de GraphDB para hacer la query.
		//HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/OntologyDirectory");
	
		//Probamos con la librer�a de Andrea
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
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
			res=res0.toString();
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
				ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
				res=res0.toString();
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
			
			SparqlEndpoint.update(query);
			return "Tripleta A�adida";
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
		
		
		//PUT para modificar los valores de la URL de una determinada ontolog�a 
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
		//AQU� COMIENZA LA PARTE DEL SERVICIO EN LA QUE SE TRABAJA CON OWL COMO SE ESPECIFICA EN EL UC2
		
		//GET Para que de una ontolog�a dada extraiga todas las tripletas presentes 
		get("/OD/*/triplets", (request, response) -> {
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<request.splat().length;i++)
					sb.append(request.splat()[i]);
				String name = sb.toString();
				String query = "SELECT * FROM NAMED <http://localhost:4567/OD/"+name+">\r\n"
						+ "{ GRAPH ?g { ?s ?p ?o } }";
				String res = null;
					ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
				res=res0.toString();
				return res;
	     });
		
		//TODO: Crear grafo vac�o y llenarlo con un LOAD?
		//POST Para que la creaci�n de un grafo partiendo del c�digo OWL de una ontolog�a dada y lo llene de estas tripletas.
		post("/OD/*/triplets", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String SOURCE = request.queryParams("SOURCE");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "LOAD <"+SOURCE+"> INTO GRAPH <http://localhost:4567/OD/"+name+">";
			SparqlEndpoint.update(query);	
			return "Grafo a�adido";
		});
		
		//DELETE, se pasa nombre de un grafo que contiene una ontolog�a y este se elimina.
		delete("/OD/*/triplets", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String query ="BASE <http://localhost:4567/>\r\n"
					+ "DROP GRAPH <http://localhost:4567/OD/"+name+">;\r\n";
			SparqlEndpoint.update(query);
		    return "Grafo Eliminado";
		});
				
		//PUT, se pasa nombre e IRI de origen y este modifica una ontolog�a existente. 
		put("/OD/*/triplets", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String SOURCE = request.queryParams("SOURCE");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "LOAD <"+SOURCE+"> INTO GRAPH <http://localhost:4567/OD/"+name+">";
			SparqlEndpoint.update(query);
			return "Grafo Actualizado";
		});
		
		
		//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		//UC3: UC3: Registration of a service/endpoint and (automatic?) extraction of the explicit ontology
		//TODO: GET de los endpoints
		//TODO: POST de los endpoints
		//TODO: DELETE de los endpoints
		//TODO: PUT de los endpoints
		//TODO: GET de los servicios
		//TODO: POST de los servicios
		//TODO: DELETE de los servicios
		//TODO: PUT de los servicios
		
		
		}
	}
