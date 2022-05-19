import static spark.Spark.*;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.util.iterator.ExtendedIterator;

import sparql.streamline.core.EndpointConfiguration;
import sparql.streamline.core.SparqlEndpoint;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Service {

	public static void main(String[] args) {
		// Iniciamos la conexión con el servicio de SPARQL de GraphDB para hacer la
		// query.
		// HTTPRepository repository = new
		// HTTPRepository("http://localhost:7200/repositories/OntologyDirectory");

		// Probamos con la librería de Andrea
		// Creamos objeto Endpoint Configuration para acceder a DBGraph
		EndpointConfiguration ec = new EndpointConfiguration();
		String endpoint = "http://localhost:7200/repositories/OntologyDirectory";
		String endpointupdate = "http://localhost:7200/repositories/OntologyDirectory/statements";
		ec.setSparqlQuery(endpoint);
		ec.setSparqlUpdate(endpointupdate);
		SparqlEndpoint.setConfiguration(ec);

		// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		// OPERACIONES QUE IMPLICAN EXTRAER INFO DEL GRAFO POR DEFECTO Y DE LOS
		// METADATOS. NO ES UN CRUD PORQUE SE REALIZA DE FORMA AUTOMÁTICA CON EL CRUD DE
		// ONTOLOGÍAS.

		// GET para que devuelva el conjunto de tripletas guardadas
		get("/OD/Ontologies", (request, response) -> {
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?x ?name ?url\r\n" + "WHERE {\r\n" + "    ?x foaf:name ?name.\r\n"
					+ "    ?x foaf:homepage ?url\r\n" + "}";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// GET Para que busque un elemento dentro de la lista al incluirlo en la URL y
		// los metadatos.
		get("/OD/Ontology/Metadata", (request, response) -> {
			String uri = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?prop ?obj \r\n" + "WHERE {\r\n" + "    <" + uri + "> ?prop ?obj .\r\n" + "}\r\n" + "";
			System.out.println(query);
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// COMENTAMOS ESTE CÓDIGO PORQUE NO TIENE MUCHO SENTIDO REALIZANDOSE LA
		// INCLUSIÓN DE LA URL Y METADATOS DE FORMA AUTOMÁTICA DEL REGISTRO DE
		// ONTOLOGÍA, DEJAMOS LOS DOS GET DE ARRIBA PORQUE UNO NOS SIRVE PARA DEVOLVER
		// LO QUE CONTIENE EL GRAFO POR DEFECTO Y OTRO PARA DEVOLVER LA INFORMACIÓN DE
		// LOS METADATOS.

//		//POST Para crear una URL dentro de la lista de estas. 
//		post("/OD/Ontologies/*/URL", (request, response) -> {
//			StringBuffer sb = new StringBuffer();
//			for (int i=0; i<request.splat().length;i++)
//				sb.append(request.splat()[i]);
//			String name = sb.toString();
//			String url = request.queryParams("url"); 
//			String query = "BASE <http://localhost:4567/>\r\n"
//					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
//					+ "INSERT DATA { \r\n"
//					+ "	<http://localhost:4567/"+name.replace(" ","")+"> foaf:name \""+name+"\"; \r\n"
//					+ "    							foaf:homepage <"+url+">.\r\n"
//					+ "} ";
//			SparqlEndpoint.update(query);
//			return "Tripleta Añadida";
//		});
//		
//		
//		///DELETE Para eliminar una URL de dentro de la lista de estas. 
//		delete("/OD/Ontologies/*/URL", (request, response) -> {
//			StringBuffer sb = new StringBuffer();
//			for (int i=0; i<request.splat().length;i++)
//				sb.append(request.splat()[i]);
//			String name = sb.toString();
//			String query ="BASE <http://localhost:4567/>\r\n"
//					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
//					+ "DELETE {\r\n"
//					+ "    ?x foaf:name ?name;\r\n"
//					+ "       foaf:homepage ?url.\r\n"
//					+ "}\r\n"
//					+ "WHERE{\r\n"
//					+ "     ?x foaf:name ?name;\r\n"
//					+ "       foaf:homepage ?url.\r\n"
//					+ "     FILTER(str(?name) = \""+name+"\") \r\n"
//					+ "}";
//			SparqlEndpoint.update(query);
//		   return "Tripleta Eliminada";
//		});
//		
//		
//		//PUT para modificar los valores de la URL de una determinada ontología 
//		put("/OD/Ontologies/*/URL", (request, response) -> {
//			StringBuffer sb = new StringBuffer();
//			for (int i=0; i<request.splat().length;i++)
//				sb.append(request.splat()[i]);
//			String name = sb.toString();
//			String url = request.queryParams("url");
//			String query ="BASE <http://localhost:4567/>\r\n"
//					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
//					+ "DELETE {\r\n"
//					+ "    ?x foaf:homepage ?url.\r\n"
//					+ "}\r\n"
//					+ "WHERE{\r\n"
//					+ "     ?x foaf:name ?name;\r\n"
//					+ "       foaf:homepage ?url.\r\n"
//					+ "     FILTER(str(?name) = \""+name+"\") \r\n"
//					+ "}";
//			System.out.println(query);
//			SparqlEndpoint.update(query);
//			String query1 = "BASE <http://localhost:4567/>\r\n"
//					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
//					+ "INSERT DATA { \r\n"
//					+ "	<http://localhost:4567/"+name.replace(" ","")+"> foaf:homepage <"+url+">.\r\n"
//					+ "} ";
//			System.out.println(query1);
//			SparqlEndpoint.update(query1);
//		   return "Actualizada";
//		}); */
//				
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//CRUD DE REGISTRO DE ONTOLOGÍAS.		

		// GET de una Ontología concreta
		get("/OD/Ontologies/", (request, response) -> {
			String uri = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?s ?p ?o\r\n" + "WHERE {\r\n" + "    GRAPH <" + uri + "> {\r\n" + "		?s ?p ?o.\r\n"
					+ "	}\r\n" + "}";
			System.out.println(query);
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// Método auxiliar de post de ontología hasta que este esté hecho
		post("/OD/Ontologies/", (request, response) -> {
			String url = request.queryParams("url");
			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			m.read(url);
			// Obtenemos el nombre de la ontología mediante la creación de otro modelo desde
			// uno.
			OntModel mBase = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m.getBaseModel());
			ExtendedIterator<Ontology> it = mBase.listOntologies();
			Ontology o = it.next();
			// Obtención del título de la ontología.
			StmtIterator it1 = m.listStatements();
			Statement elem;
			String pred = "";
			String obj = "";
			String subj = "";
			Boolean found = false;
			while (it1.hasNext() && !found) {
				elem = it1.next();
				pred = elem.getPredicate().toString();
				obj = elem.getObject().toString();
				if (pred.equals("http://purl.org/dc/elements/1.1/title")) {
					System.out.println(obj);
					found = true;
				}
			}
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n" + "	<" + o + "> foaf:name \"" + obj + "\"; \r\n"
					+ "    							foaf:homepage <" + url + ">.\r\n" + "} ";
			System.out.println(query);
			SparqlEndpoint.update(query);

			// Fragmento de código que busca las líneas de metadatos y las inserta en el
			// grafo por defecto.
			it1 = m.listStatements();
			String query2 = " BASE <http://localhost:4567/>\r\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n";
			found = false;
			while (it1.hasNext() && !found) {
				elem = it1.next();
				subj = elem.getSubject().toString();
				if (o.toString().equals(subj)) {
					pred = elem.getPredicate().toString();
					obj = elem.getObject().toString();
					if (obj.startsWith("http"))
						query2 += "	<" + o + "> <" + pred + "> <" + obj + ">. \r\n";
					else
						query2 += "	<" + o + "> <" + pred + "> \"" + obj + "\". \r\n";
				}
			}
			query2 += "} ";
			System.out.println(query2);
			SparqlEndpoint.update(query2);

			// Se añaden las tripletas de la ontología al grafo correspondiente.
			String query1 = "BASE <http://localhost:4567/>\r\n" + "LOAD <" + url + "> INTO GRAPH <" + o + ">";
			System.out.println(query1);
			SparqlEndpoint.update(query1);
			return "Ontología añadida";
		});

		// DELETE de la Ontología
		delete("/OD/Ontologies/", (request, response) -> {
			String uri = request.queryParams("uri");
			String query1 = "BASE <http://localhost:4567/>\r\n" 
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "DROP GRAPH <" + uri + ">;\r\n" 
					+ "DELETE {\r\n" 
					+ "    ?x ?prop ?obj.\r\n" + "}\r\n"
					+ "WHERE{\r\n" + "     ?x ?prop ?obj.\r\n"
					+ "     FILTER(str(?x) = \"" + uri + "\") \r\n"
					+ "}\r\n";
			System.out.println(query1);
			SparqlEndpoint.update(query1);
			return "Ontología eliminada";
		});

		// PUT de las Ontologías.
		put("/OD/Ontologies/", (request, response) -> {
			// Extracción de parámetros
			String uri = request.queryParams("uri");
			String SOURCE = request.queryParams("SOURCE");
			// Se copia el grafo presente en SOURCE en el grafo uri local.
			String query = "BASE <http://localhost:4567/>\r\n" + "CLEAR GRAPH <" + uri + ">";
			System.out.println(query);
			SparqlEndpoint.update(query);
			query = "BASE <http://localhost:4567/>\r\n" + "LOAD <" + SOURCE + "> INTO GRAPH  <" + uri + "> \r\n";
			System.out.println(query);
			SparqlEndpoint.update(query);
			// Se borran los antiguos datos del grafo por defecto (metadatos y fuente)
			String query1 = "BASE <http://localhost:4567/>\r\n" + "DELETE {\r\n" + "    ?x ?prop ?obj.\r\n" + "}"
					+ "WHERE{\r\n" + "     ?x ?prop ?obj.\r\n" + "     FILTER(str(?x) = \"" + uri + "\") \r\n" + "}";
			System.out.println(query1);
			SparqlEndpoint.update(query1);
			// Fragmento de código que busca las líneas de metadatos y las inserta en el
			// grafo por defecto.
			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			m.read(SOURCE);
			// Obtenemos el nombre de la ontología mediante la creación de otro modelo desde
			// uno.
			OntModel mBase = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m.getBaseModel());
			ExtendedIterator<Ontology> it = mBase.listOntologies();
			Ontology o = it.next();
			// Obtención del título de la ontología.
			StmtIterator it1 = m.listStatements();
			Statement elem;
			String pred = "";
			String obj = "";
			String subj = "";
			Boolean found = false;
			it1 = m.listStatements();
			String query2 = " BASE <http://localhost:4567/>\r\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n";
			found = false;
			while (it1.hasNext() && !found) {
				elem = it1.next();
				subj = elem.getSubject().toString();
				if (o.toString().equals(subj)) {
					pred = elem.getPredicate().toString();
					obj = elem.getObject().toString();
					if (obj.startsWith("http"))
						query2 += "	<" + o + "> <" + pred + "> <" + obj + ">. \r\n";
					else
						query2 += "	<" + o + "> <" + pred + "> \"" + obj + "\". \r\n";
				}
			}
			query2 += "} ";
			System.out.println(query2);
			SparqlEndpoint.update(query2);
			return "Lista de tripletas actualizada";
		});

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Métodos de extracción de información de las ontologías existentes.

		// UC5.1 y UC5.2
		// Método para obtener qué términos están presentes en qué ontologías.
		get("OD/Ontologies/TermLocation", (request, response) -> {
			String term = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + "SELECT DISTINCT ?graph  WHERE {\r\n"
					+ "    GRAPH ?graph {\r\n" + "        OPTIONAL {<" + term + "> rdf:type rdfs:Class}\r\n"
					+ "        OPTIONAL {<" + term + "> rdf:type rdf:Property}\r\n" + "    }\r\n" + "} ";
			System.out.println(query);
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// Método term presence que indica mediante operación ask si un término está
		// definida en el servicio
		get("OD/Ontologies/TermPresenceOD", (request, response) -> {
			String term = request.queryParams("term");
			String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + "ASK  { <" + term
					+ "> rdf:type ?prop}\r\n";
			System.out.println(query);
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// UC5.3
		// Método para obtener qué términos de una ontología son ajenos
		get("OD/Ontologies/ForeignTerms", (request, response) -> {
			String ont = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" 
					+ "SELECT DISTINCT ?x\r\n" + " WHERE {\r\n"
					+ "    GRAPH <" + ont + ">{\r\n"
					+ "    {?sub ?pred ?x} UNION {?x ?pred1 ?obj} UNION {?sub1 ?x ?obj1}.\r\n"
					+ "        FILTER(!STRSTARTS(STR(?x), \"" + ont + "\") && isURI(?x))\r\n" + "    }\r\n" + " }";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			System.out.println(res0.toString());
			return res0.toString();
		});

		// UC5.4
		// Porcentaje de términos propios de esta ontología y que porcentaje de términos
		// es de ontologías ajenas
		get("OD/Ontologies/ForeignPercentage", (request, response) -> {
			String ont = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "SELECT DISTINCT ?x\r\n" + " WHERE {\r\n"
					+ "    GRAPH <" + ont + ">{\r\n"
					+ "    {?sub ?pred ?x} UNION {?x ?pred1 ?obj} UNION {?sub1 ?x ?obj1}.\r\n"
					+ "        FILTER(!STRSTARTS(STR(?x), \"" + ont + "\") && isURI(?x))\r\n" + "    }\r\n" + " }";
			String res = SparqlEndpoint.query(query, ResultsFormat.FMT_COUNT).toString();
			res = res.replaceAll("[^0-9]", "");
			int val1 = Integer.parseInt(res);
			System.out.println(val1);
			String query1 = "BASE <http://localhost:4567/>\r\n" + "SELECT DISTINCT ?x\r\n" + " WHERE {\r\n"
					+ "    GRAPH <" + ont + ">{\r\n"
					+ "    {?sub ?pred ?x} UNION {?x ?pred1 ?obj} UNION {?sub1 ?x ?obj1}.\r\n"
					+ "        FILTER(isURI(?x))\r\n" + "    }\r\n" + " }";
			res = SparqlEndpoint.query(query1, ResultsFormat.FMT_COUNT).toString();
			res = res.replaceAll("[^0-9]", "");
			int val2 = Integer.parseInt(res);
			System.out.println(val2);
			return (double) val1 / val2 * 100;
		});

		// UC5.5 Obtención del rango de un object type propery
		get("OD/Ontologies/RangeofProperty", (request, response) -> {
			String prop = request.queryParams("property");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" + "SELECT ?obj\r\n" + " WHERE {\r\n"
					+ "   		<" + prop + "> rdf:type owl:ObjectProperty;\r\n" + "           rdfs:range ?obj.\r\n"
					+ " FILTER (!isBlank(?obj))" + " }";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// UC5.6 ¿Qué ontologías están importadas o reutilizan la una a la otra en OD?
		// 1. Búsqueda en el grafo por defecto la url asociada a la URI de la ontología
		// 2. Teniendo esta se carga en jena mediante el método read.
		// 3. Se obtiene el prefix map de este
		// 4. Del prefix map obtenemos el prefijo que identifica a la ontología
		// 5. Se sustrae del prefix map el prejijo que identifica a la ontología
		// 6. Se vierten los prefijos restantes en un string o array que se pueda
		// devolver
		// 7. Se devuelve la información al usuario.
		get("OD/Ontologies/OntologiesReused", (request, response) -> {
			String uri = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?url\r\n" + "WHERE {\r\n" + "    <" + uri + "> foaf:homepage ?url\r\n" + "}\r\n" + "";
			String res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_TEXT).toString();
			String url = res0.substring(res0.lastIndexOf("<") + 1, res0.indexOf(">"));
			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			m.read(url);
			Map<String, String> pm = m.getNsPrefixMap();
			ArrayList<String> res = new ArrayList<String>();
			for (String clave : pm.keySet()) {
				String valor = pm.get(clave);
				System.out.println(clave);
				System.out.println(valor);
				if (!valor.equals(uri)) {
					res.add(valor);
					res.add("\n");
				}
			}
			return res;
		});

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//CRUD DE LA LISTA DE TÉRMINOS CON EL FORMATO SEGUIDO PARA OD

		// POST de los términos que se añaden a la lista
		post("OD/Services/ListofTerms", (request, response) -> {
			String listofterms = request.body();
			listofterms = listofterms.replace("\"", " ");
			// System.out.println(listofterms);
			Map<String, String> terms = new HashMap<String, String>();
			List<String> matches = new ArrayList<>();
			Matcher m1 = Pattern.compile("http.+").matcher(listofterms);
			while (m1.find())
				matches.add(m1.group().replace(" ", ""));
			// System.out.println(matches);
			for (int i = 0; i < matches.size();)
				terms.put(matches.get(i++), matches.get(i++));
			// System.out.println(terms);
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "INSERT DATA{ \r\n" + "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n";
			for (Map.Entry<String, String> entry : terms.entrySet())
				query += "<" + entry.getKey() + "> dc:source <" + entry.getValue() + ">.\r\n";
			query += "}}";
			System.out.println(query);
			SparqlEndpoint.update(query);
			return "Lista de términos del servicio añadida";
		});

		// GET que devuelve la lista completa de términos asociados a servicios.
		get("OD/Services/ListofTerms", (request, body) -> {
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "SELECT DISTINCT ?term ?service WHERE {\r\n"
					+ "    GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n"
					+ "        ?term dc:source ?service.\r\n" + "    }\r\n" + "}\r\n" + "";
			String res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON).toString();
			return res0;
		});

		// GET devuelve los términos asociados a un servicio determinado
		get("OD/Services/TermsfromService", (request, body) -> {
			String service = request.queryParams("service");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "SELECT DISTINCT ?term WHERE {\r\n"
					+ "    GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n" + "        ?term dc:source <"
					+ service + ">.\r\n" + "    }\r\n" + "}\r\n" + "";
			String res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON).toString();
			return res0;
		});

		// DELETE borra entradas determinadas de la lista de términos
		delete("OD/Services/ListofTerms/Term", (request, response) -> {
			String listofterms = request.body();
			listofterms = listofterms.replace("\"", " ");
			System.out.println(listofterms);
			Map<String, String> terms = new HashMap<String, String>();
			List<String> matches = new ArrayList<>();
			Matcher m1 = Pattern.compile("http.+").matcher(listofterms);
			while (m1.find())
				matches.add(m1.group().replace(" ", ""));
			System.out.println(matches);
			for (int i = 0; i < matches.size();)
				terms.put(matches.get(i++), matches.get(i++));
			System.out.println(terms);
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "DELETE DATA{ \r\n" + "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n";
			for (Map.Entry<String, String> entry : terms.entrySet())
				query += "<" + entry.getKey() + "> dc:source <" + entry.getValue() + ">.\r\n";
			query += "}}";
			System.out.println(query);
			SparqlEndpoint.update(query);
			return "Términos del servicio dados eliminados";
		});

		//DELETE borra las entradas asociadas a un servicio de terminado
		delete("OD/Services/ListofTerms/",(request,response)->{
			String service = request.queryParams("service");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "DELETE  {  \r\n"
					+ "    ?x dc:source ?obj.}\r\n"
					+ "WHERE{\r\n"
					+ "    ?x dc:source ?obj\r\n"
					+ "     FILTER((?obj) = <"+service+">) \r\n"
					+ "}\r\n";
			SparqlEndpoint.update(query);
			return "Lista de términos asociados a un servicio eliminada";
		});
		
		// PRE partimos de la premisa de que no se están introduciendo términos de
		// servicios que no son el dado.
		// PUT, elimina las tripletas referentes a todos los términos asociados a un
		// servicio y los actualiza con los pasados en el body.
		put("OD/Services/TermsfromService", (request, response) -> {
			// Eliminamos las tripletas correspondientes a un servicio.
			String service = request.queryParams("service");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "WITH <http://localhost:4567/OD/TermsfromService>\r\n" + "DELETE { \r\n"
					+ "   		 ?x dc:source <" + service + ">.\r\n" + "    }\r\n" + "    WHERE{\r\n"
					+ "       	  ?x dc:source <" + service + ">.\r\n" + "}";
			System.out.println(query);
			SparqlEndpoint.update(query);

			// Código en el que se añade el nuevo listado de términos.
			String listofterms = request.body();
			listofterms = listofterms.replace("\"", " ");
			// System.out.println(listofterms);
			Map<String, String> terms = new HashMap<String, String>();
			List<String> matches = new ArrayList<>();
			Matcher m1 = Pattern.compile("http.+").matcher(listofterms);
			while (m1.find())
				matches.add(m1.group().replace(" ", ""));
			// System.out.println(matches);
			for (int i = 0; i < matches.size();)
				terms.put(matches.get(i++), matches.get(i++));
			// System.out.println(terms);
			String query1 = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "INSERT DATA{ \r\n" + "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n";
			for (Map.Entry<String, String> entry : terms.entrySet())
				query1 += "<" + entry.getKey() + "> dc:source <" + entry.getValue() + ">.\r\n";
			query1 += "}}";
			System.out.println(query1);
			SparqlEndpoint.update(query1);

			return "Lista de tripletas actualizada";
		});

//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// MÉTODOS DE EXTRACCIÓN DE INFORMACIÓN DE LOS METADATOS DE LAS ONTOLOGÍAS.  

		// Método de extracción de metadatos.
		get("OD/Ontologies/Metadata/License", (request, response) -> {
			String ont = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dct: <http://purl.org/dc/terms/>\r\n"
					+ "SELECT ?license WHERE {\r\n" + "    <" + ont + "> dct:license ?license\r\n" + "}";
			String res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON).toString();
			return res0;
		});

//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// METODOS DE EXTRACCIÓN DE INFORMACIÓN DEL LISTADO DE TÉRMINOS		

		// Método term location para obtener en que servicio está presente un término.
		get("OD/Services/TermLocation", (request, response) -> {
			String term = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "SELECT DISTINCT ?services  WHERE {\r\n"
					+ "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n" + "       <" + term
					+ "> dc:source ?services\r\n" + "    }\r\n" + "\r\n" + "}";
			System.out.println(query);
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// Método de ontologías reutilizadas que obtiene los namespaces a los que los
		// términos presentes pertenecen.
		get("OD/Services/OntologiesReused", (request, response) -> {
			String service = request.queryParams("service");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "SELECT DISTINCT ?x  WHERE {\r\n" + "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n"
					+ "        ?x dc:source <" + service + ">.\r\n" + "    }\r\n" + "}";
			String res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_TEXT).toString();
			// System.out.println(res0);
			List<String> matches = new ArrayList<>();
			Matcher m = Pattern.compile("<([^><]+)>").matcher(res0);
			while (m.find()) {
				matches.add(m.group(1));
			}
			return matches;
		});

		// Método que devuelva que tipo de término es un término dado
		get("OD/Services/TypeofTerm", (request, response) -> {
			String term = request.queryParams("term");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + "SELECT ?type WHERE {\r\n"
					+ "	<" + term + "> rdf:type ?type   \r\n" + "}";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//Métodos de CRUD de la extracción automática de términos		

		post("OD/Services/ListofTerms/Automatic", (request, response) -> {
			// Nos interesa extraer del servicio los términos ontológicos (clases y
			// propertys)
			// De la extracción automática se excluye rdf, rdfs y owl
			// 1. Creación de una lista de los namespaces que debe de excluir, de base
			// serían rdf, rdfs y owl.
			String reserved[] = { "http://www.w3.org/1999/02/22-rdf-syntax-ns", "http://www.w3.org/2000/01/rdf-schema",
					"http://www.w3.org/2002/07/owl#", "http://www.w3.org/2001/XMLSchema#" };
			// 2. Nos bajamos el servicio en formato rdf mediante la librería jena
			String url = request.queryParams("url");
			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			m.read(url);
			OntModel mBase = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m.getBaseModel());
			// 4. Del servicio se extraen los namespaces presentes -> Nos permite obtener
			// aquellos que más adelante no estén presentes
			Map<String, String> ns = mBase.getNsPrefixMap();
			//System.out.println(ns);
			//System.out.println(ns.size());
			// De base el servicio no tiene una uri asociada sino que tiene un conjunto de
			// tripletas sin metadatos de base (partiendo de DBPedia)
			// De cara a añadirlo a OD se incluye la url de la que se extrae el conjunto de
			// términos

			/*
			 * Set<String> imports = m.listImportedOntologyURIs();
			 * System.out.println(imports); La búsqueda de imports alomejor no tiene ningun
			 * sentido ya que estamos hablando de datos y no de otras ontologías pero
			 * esperar a preguntar a raúl y Andrea
			 */
			// Búsqueda de namespaces que sean de la misma fuente que los datos para
			// desestimarlos como ontologías reutilizadas para la extracción de términos.
			URL parsedurl = new URL(url);
			// String parsedurl1 ="http://"+parsedurl.getAuthority()+"/";
			String parsedurl1 = parsedurl.getAuthority();
			//System.out.println(parsedurl1);
			Map<String, String> nsaux = new HashMap<String, String>();
			nsaux.putAll(ns);
			// Añadir líneas para que también excluya las del conjunto st.
			for (String clave : ns.keySet()) {
				String valor = ns.get(clave);
				for (int i = 0; i < reserved.length; i++) {
					if (valor.contains(parsedurl1) || valor.startsWith(reserved[i])) {
						// System.out.println("Clave: " + clave + ", valor: " + valor);
						nsaux.remove(clave);
					}
				}
			}
			// Tenemos el problema de que pilla es.dbpedia.org y habría que extraer de esto
			// el resto de recursos de dbpedia pero no dbería de trastearlo ahora asique
			// debería de quedarse así hasta que consulte a Raúl y Andrea al respecto.
			//System.out.println(nsaux);
			//System.out.println(nsaux.size());
			// 5. Recorriendo las tripletas presentes se extraen aquellos términos que no
			// son literales ni de este namespace
			StmtIterator st = m.listStatements();
			List<String> listofterms = new ArrayList<String>();
			Statement elem;
			// Recorremos el conjunto de los statements para comprobar y añadir los
			// elementos a la lista de términos.
			// Que no esté ya presente en la lista.
			// Que sea una uri
			// Que sea de los namespaces obtenidos antes
			// De forma independiente se trata el sujeto, predicado y objeto.
			// 6. Estos términos se almacenarán en un objeto del tipo lista
			while (st.hasNext()) {
				elem = st.next();
				for (String clave : nsaux.keySet()) {
					if (!listofterms.contains(elem.getSubject().toString())
							&& elem.getSubject().toString().startsWith(nsaux.get(clave))) {
						//System.out.println(nsaux.get(clave));
						//System.out.println(elem.getSubject().toString());
						listofterms.add(elem.getSubject().toString());
					}
					if (!listofterms.contains(elem.getPredicate().toString())
							&& elem.getPredicate().toString().startsWith(nsaux.get(clave))) {
						//System.out.println(nsaux.get(clave));
						//System.out.println(elem.getPredicate().toString());
						listofterms.add(elem.getPredicate().toString());
					}
					if (!listofterms.contains(elem.getObject().toString())
							&& elem.getObject().toString().startsWith(nsaux.get(clave))) {
						//System.out.println(nsaux.get(clave));
						//System.out.println(elem.getObject().toString());
						listofterms.add(elem.getObject().toString());
					}
				}
			}
			//System.out.println(listofterms);
			// 7. Se hace sparql querie en la que se insertan estos términos en el grafo
			// terms from service
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "INSERT DATA{ \r\n" + "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n";
			for (int i = 0; i < listofterms.size(); i++)
				query += "<" + listofterms.get(i) + "> dc:source <" + url + ">.\r\n";
			query += "}}";
			//System.out.println(query);
			//SparqlEndpoint.update(query);
			
			// TODO: Se debería de hacer un método que al añadirlo devuelva si algún namespace no está presente en local al subir el servicio.
			List<String> res = new ArrayList<String>();
			for (String clave : nsaux.keySet()) {
				String query1 = "BASE <http://localhost:4567/>\r\n"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
						+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
						+ "ASK{\r\n"
						+ "    <"+nsaux.get(clave)+"> rdf:type owl:Ontology\r\n"
						+ "}";
				//System.out.println(query1);
				ByteArrayOutputStream res0 = SparqlEndpoint.query(query1, ResultsFormat.FMT_TEXT);
				System.out.println(res0);
				if(res0.toString().equals("no")) {
					res.add(nsaux.get(clave));	
					System.out.println(nsaux.get(clave));
				}
			} 
			System.out.println(res);
			return res.toString();
			
			//return "Conjunto de términos presentes extraidos.";
		});

		
		//Método que dada una url distinta a la dada antes actualiza las tripletas presentes correspondientes a un servicio
		//A efectos prácticos es una suma de las operaciones de delete y create asociadas a un servicio determinado.
		//Si se quisiera hacer a nivel de tripleta está el otro método PUT presente arriba. 
		//Dicho esto no tiene mucho sentido que actualices el contendo de una URL con el de otra URL, para eso es mejor eliminar el servicio y añadir el 
		//nuevo si fuera una actualización del anterior, preguntar a Andrea y Raúl.
		
		

	}
}
