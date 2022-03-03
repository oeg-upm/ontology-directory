import static spark.Spark.*;
import org.apache.jena.sparql.resultset.ResultsFormat;
import sparql.streamline.core.EndpointConfiguration;
import sparql.streamline.core.SparqlEndpoint;
import java.io.ByteArrayOutputStream;

public class Service {

	
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
		
		//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		//AQUÍ COMIENZA LA PARTE DEL UC1
		
		//GET para que devuelva el conjunto de tripletas guardadas
		get("/OD/Ontologies", (request, response) -> {
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?x ?name ?url\r\n"
					+ "WHERE {\r\n"
					+ "    ?x foaf:name ?name.\r\n"
					+ "    ?x foaf:homepage ?url\r\n"
					+ "}";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
			return res0.toString();
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
				ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
				return res0.toString();
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
			return "Tripleta Añadida";
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
		
		
		//PUT para modificar los valores de la URL de una determinada ontología 
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
		//AQUÍ COMIENZA LA PARTE DEL SERVICIO EN LA QUE SE TRABAJA CON OWL COMO SE ESPECIFICA EN EL UC2
		
		//GET Para que de una ontología dada extraiga todas las tripletas presentes 
		get("/OD/Ontologies/*/triplets", (request, response) -> {
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<request.splat().length;i++)
					sb.append(request.splat()[i]);
				String name = sb.toString();
				String query = "SELECT * FROM NAMED <http://localhost:4567/OD/"+name+">\r\n"
						+ "{ GRAPH ?g { ?s ?p ?o } }";
				ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
				return res0.toString();
	     });
		
		
		//POST Para que la creación de un grafo partiendo del código OWL de una ontología dada y lo llene de estas tripletas.
		post("/OD/Ontologies/*/triplets", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String SOURCE = request.queryParams("SOURCE");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "LOAD <"+SOURCE+"> INTO GRAPH <http://localhost:4567/OD/"+name+">";
			SparqlEndpoint.update(query);	
			return "Grafo añadido";
		});
		
		
		//DELETE, se pasa nombre de un grafo que contiene una ontología y este se elimina.
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
		
		
		//PUT, se pasa nombre e IRI de origen y este modifica una ontología existente. 
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
		
		//GET: Obtención de todos los endpoints registrados 
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
		
		
		//GET de un endpoint concreto junto con su conjunto de términos asociado
		//Funciona el método pero en el ejemplo de wikidata tarda tanto tiempo que el servicio se cae. 
		get("/OD/Endpoints/*", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?x ?url ?s ?p ?o\r\n"
					+ "WHERE {\r\n"
					+ "    GRAPH <http://localhost:4567/OD/Endpoints>{\r\n"
					+ "	?x foaf:name \""+name+"\".\r\n"
					+ "	?x foaf:homepage ?url.\r\n"
					+ "	}\r\n"
					+ "    GRAPH ?url {\r\n"
					+ "	?s ?p ?o.\r\n"
					+ "	}\r\n"
					+ "}";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});
		
		
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
			return "Lista de términos añadida";
		});
		
		//Quizá podríamos hacer primero query para obtener la url y así no se pasa como parámetro
		//Por otro lado teniendo en cuenta que el NG tiene el nombre de la URL 
		//¿No deberíamos directamente prescindir del nombre como atributo ya que tiene una presencia testimonial?
		// DELETE de la información del endpoint en el grafo de endpoints y borrado de su grafo correspondiente.
		delete("/OD/Endpoints/*", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String url = request.queryParams("url"); 
			String query1 = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "DROP GRAPH <"+url+">;\r\n"
					+ "DELETE {\r\n"
					+ "GRAPH <http://localhost:4567/OD/Endpoints> {\r\n"
					+ "    ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "}"
					+ "}\r\n"
					+ "WHERE{\r\n"
					+ "     ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "     FILTER(str(?name) = \""+name+"\") \r\n"
					+ "}\r\n";
			//System.out.println(query1);
			SparqlEndpoint.update(query1);
		    return "Lista de términos y entrada eliminados";
		});
		
		//Preguntar a Andrea y Raúl que hacer con el tema de que hacer con el nombre de los grafos ya que va asociado al servicio y quizá al cambiar el nombre del grafo se pierde algo
		//al asociarlo a la dirección de donde se obtienen los datos.
		//PUT de los endpoints
		put("/OD/Endpoints/*", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<request.splat().length;i++)
				sb.append(request.splat()[i]);
			String name = sb.toString();
			String SOURCE = request.queryParams("SOURCE");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "LOAD <"+SOURCE+"> INTO GRAPH <"+name+">";
			System.out.println(query);
			SparqlEndpoint.update(query);
			String query1 ="BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "DELETE {\r\n"
					+ "GRAPH <http://localhost:4567/OD/Endpoints> {\r\n"
					+ "    ?x foaf:homepage ?url.\r\n"
					+ "}\r\n"
					+ "}"
					+ "WHERE{\r\n"
					+ "     ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "     FILTER(str(?name) = \""+name+"\") \r\n"
					+ "}";
			System.out.println(query1);
			SparqlEndpoint.update(query1);
			String query2 = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n"
					+ "GRAPH <http://localhost:4567/OD/Endpoints> {\r\n"
					+ "	<http://localhost:4567/"+name.replace(" ","")+"> foaf:homepage <"+SOURCE+">.\r\n"
					+ "}"
					+ "} ";
			System.out.println(query2);
			SparqlEndpoint.update(query2);
			return "Lista de tripletas actualizadaS";
		});
		
		}
	}
