import static spark.Spark.*;
import org.apache.jena.sparql.resultset.ResultsFormat;
import sparql.streamline.core.EndpointConfiguration;
import sparql.streamline.core.SparqlEndpoint;
import java.io.ByteArrayOutputStream;

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
		
		//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		//AQU� COMIENZA LA PARTE DEL UC1
		
		//GET para que devuelva el conjunto de tripletas guardadas
		get("/OD/Ontologies", (request, response) -> {
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
		get("/OD/Ontologies/*/URL", (request, response) -> {
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
		post("/OD/Ontologies/*/URL", (request, response) -> {
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
		delete("/OD/Ontologies/*/URL", (request, response) -> {
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
		put("/OD/Ontologies/*/URL", (request, response) -> {
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
		get("/OD/Ontologies/*/triplets", (request, response) -> {
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
		
		//POST Para que la creaci�n de un grafo partiendo del c�digo OWL de una ontolog�a dada y lo llene de estas tripletas.
		post("/OD/Ontologies/*/triplets", (request, response) -> {
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
		delete("/OD/Ontologies/*/triplets", (request, response) -> {
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
		put("/OD/Ontologies/*/triplets", (request, response) -> {
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
		
		//GET: Obtenci�n de todos los endpoints registrados 
		get("/OD/Endpoints", (request, response) -> {
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?x ?name ?url\r\n"
					+ "WHERE{\r\n"
					+ "    GRAPH <http://localhost:4567/OD/Endpoints> {\r\n"
					+ "    ?x foaf:name ?name.\r\n"
					+ "    ?x foaf:homepage ?url\r\n"
					+ "}    \r\n"
					+ "}";		
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
			return res0.toString();
	     });
		
		
		//GET de un endpoint concreto junto con su conjunto de t�rminos asociado
		//Funciona el m�todo pero en el ejemplo de wikidata tarda tanto tiempo que el servicio se cae. 
		//TODO: Pedir a Andrea ejemplos de endpoints para testear esto.
		get("/OD/Endpoints/*", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String url = request.queryParams("url");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?x ?url ?s ?p ?o\r\n"
					+ "WHERE {\r\n"
					+ "    GRAPH <http://localhost:4567/OD/Endpoints>{\r\n"
					+ "	?x foaf:name \""+name+"\".\r\n"
					+ "	?x foaf:homepage ?url.\r\n"
					+ "	}\r\n"
					+ "    GRAPH <"+url+">{\r\n"
					+ "	?s ?p ?o.\r\n"
					+ "	}\r\n"
					+ "}";			
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});
		
		
		//TODO: Pedir a Andrea ejemplos de endpoints para testear esto.
		//POST de los endpoints y las tripletas que este contiene 
		post("/OD/Endpoints/*", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String url = request.queryParams("url"); 
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n"
					+ "GRAPH <http://localhost:4567/OD/Endpoints> {\r\n"
					+ "	<http://localhost:4567/"+name+"> foaf:name \""+name+"\"; \r\n"
					+ "    							foaf:homepage <"+url+">.\r\n"
					+"}"
					+ "} ";
			SparqlEndpoint.update(query);
			String query1 = "BASE <http://localhost:4567/>\r\n"
					+ "LOAD <"+url+"> INTO GRAPH <"+url+">";
			SparqlEndpoint.update(query1);
			return "Lista de t�rminos a�adida";
		});
		
		
		//TODO: Pedir a Andrea ejemplos de endpoints para testear esto.
		// DELETE de la informaci�n del endpoint en el grafo de endpoints y borrado de su grafo correspondiente.
		delete("/OD/Endpoints/*", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String query ="BASE <http://localhost:4567/>\r\n"
					+ "DROP GRAPH <"+name+">;\r\n";
			SparqlEndpoint.update(query);
			String query1 = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "DELETE {\r\n"
					+ "GRAPH <http://localhost:4567/OD/Endpoints> {\\r\\n"
					+ "    ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "}"
					+ "}\r\n"
					+ "WHERE{\r\n"
					+ "     ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "     FILTER(str(?name) = \""+name+"\") \r\n"
					+ "}";
			SparqlEndpoint.update(query1);
		    return "Lista de t�rminos y entrada eliminados";
		});
		
		
		//TODO: PUT de los endpoints
		put("/OD/Endpoints/*/", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String SOURCE = request.queryParams("SOURCE");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "LOAD <"+SOURCE+"> INTO GRAPH <"+name+">";
			SparqlEndpoint.update(query);
			String query1 ="BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "DELETE {\r\n"
					+ "GRAPH <http://localhost:4567/OD/Endpoints> {\\r\\n"
					+ "    ?x foaf:homepage ?url.\r\n"
					+ "}\r\n"
					+ "}"
					+ "WHERE{\r\n"
					+ "     ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "     FILTER(str(?name) = \""+name+"\") \r\n"
					+ "}";
			SparqlEndpoint.update(query1);
			String query2 = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n"
					+ "GRAPH <http://localhost:4567/OD/Endpoints> {\\r\\n"
					+ "	<http://localhost:4567/"+name.replace(" ","")+"> foaf:homepage <"+SOURCE+">.\r\n"
					+ "}"
					+ "} ";
			SparqlEndpoint.update(query2);
			return "Lista de tripletas actualizadaS";
		});
		
		}
	}
