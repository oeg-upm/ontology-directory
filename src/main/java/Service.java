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
		// Iniciamos la conexión con el servicio de SPARQL de GraphDB para hacer la query.
		// Creamos objeto Endpoint Configuration para acceder a DBGraph
		EndpointConfiguration ec = new EndpointConfiguration();
		String endpoint = "http://localhost:7200/repositories/OntologyDirectory";
		String endpointupdate = "http://localhost:7200/repositories/OntologyDirectory/statements";
		ec.setSparqlQuery(endpoint);
		ec.setSparqlUpdate(endpointupdate);
		SparqlEndpoint.setConfiguration(ec);
				
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//CRUD DE REGISTRO DE ONTOLOGÍAS.		

		// GET para que devuelva el conjunto de Ontologias guardadas
		get("/OTI/Ontologies", (request, response) -> {
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "SELECT DISTINCT ?graph {\r\n"
					+ "    GRAPH ?graph{\r\n"
					+ "    	 ?a ?b ?c.\r\n"
					+ "	}\r\n"
					+ "    FILTER (?graph != <http://localhost:4567/OD/TermsfromService>)\r\n"
					+ "}";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});
		
		// GET de una Ontología concreta
		get("/OTI/Ontology/", (request, response) -> {
			String uri = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?s ?p ?o\r\n" + "WHERE {\r\n" + "    GRAPH <" + uri + "> {\r\n" + "		?s ?p ?o.\r\n"
					+ "	}\r\n" + "}";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		//POST de una Ontología, 
		post("/OTI/Ontologies/", (request, response) -> {
			String url = request.queryParams("url");
			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			m.read(url);
			// Obtenemos el nombre de la ontología mediante la creación de otro modelo
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
				if (pred.equals("http://purl.org/dc/elements/1.1/title")) 
					found = true;
				else if (pred.equals("http://www.w3.org/2002/07/owl#Ontology"))
					found = true;
			}
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n" + "	<" + o + "> foaf:name \"" + obj + "\"; \r\n"
					+ "    							foaf:homepage <" + url + ">.\r\n" + "} ";
			SparqlEndpoint.update(query);
			// Fragmento de código que busca las líneas de metadatos y las inserta en el grafo por defecto.
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
			SparqlEndpoint.update(query2);
			// Se añaden las tripletas de la ontología al grafo correspondiente.
			String query1 = "BASE <http://localhost:4567/>\r\n" + "LOAD <" + url + "> INTO GRAPH <" + o + ">";
			SparqlEndpoint.update(query1);
			return "Ontology Added";
		});

		// DELETE de la Ontología
		delete("/OTI/Ontologies/", (request, response) -> {
			String uri = request.queryParams("uri");
			String query1 = "BASE <http://localhost:4567/>\r\n" 
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "DROP GRAPH <" + uri + ">;\r\n" 
					+ "DELETE {\r\n" 
					+ "    ?x ?prop ?obj.\r\n" + "}\r\n"
					+ "WHERE{\r\n" + "     ?x ?prop ?obj.\r\n"
					+ "     FILTER(str(?x) = \"" + uri + "\") \r\n"
					+ "}\r\n";
			SparqlEndpoint.update(query1);
			return "Ontology Deleted";
		});

		// PUT de las Ontologías.
		put("/OTI/Ontologies/", (request, response) -> {
			// Extracción de parámetros
			String uri = request.queryParams("uri");
			String SOURCE = request.queryParams("SOURCE");
			// Se copia el grafo presente en SOURCE en el grafo uri local.
			String query = "BASE <http://localhost:4567/>\r\n" + "CLEAR GRAPH <" + uri + ">";
			SparqlEndpoint.update(query);
			query = "BASE <http://localhost:4567/>\r\n" + "LOAD <" + SOURCE + "> INTO GRAPH  <" + uri + "> \r\n";
			SparqlEndpoint.update(query);
			// Se borran los antiguos datos del grafo por defecto (metadatos y fuente)
			String query1 = "BASE <http://localhost:4567/>\r\n" + "DELETE {\r\n" + "    ?x ?prop ?obj.\r\n" + "}"
					+ "WHERE{\r\n" + "     ?x ?prop ?obj.\r\n" + "     FILTER(str(?x) = \"" + uri + "\") \r\n" + "}";
			SparqlEndpoint.update(query1);
			// Fragmento de código que busca las líneas de metadatos y las inserta en el grafo por defecto.
			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			m.read(SOURCE);
			// Obtenemos el nombre de la ontología mediante la creación de otro modelo desde uno.
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
			SparqlEndpoint.update(query2);
			return "Ontology Updated";
		});

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Métodos de extracción de información de las ontologías existentes.

		// Método para obtener qué términos están presentes en qué ontologías.
		get("OTI/Ontologies/TermLocation", (request, response) -> {
			String term = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + "SELECT DISTINCT ?graph  WHERE {\r\n"
					+ "    GRAPH ?graph {\r\n" + "        OPTIONAL {<" + term + "> rdf:type rdfs:Class}\r\n"
					+ "        OPTIONAL {<" + term + "> rdf:type rdf:Property}\r\n" + "    }\r\n" + "} ";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// Método term presence que indica mediante operación ask si un término está definida en el servicio
		get("OTI/Ontologies/TermPresenceinOTI", (request, response) -> {
			String term = request.queryParams("term");
			String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + "ASK  { <" + term
					+ "> rdf:type ?prop}\r\n";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// Método para obtener qué términos de una ontología son ajenos
		get("OTI/Ontologies/ForeignTerms", (request, response) -> {
			String ont = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" 
					+ "SELECT DISTINCT ?x\r\n" + " WHERE {\r\n"
					+ "    GRAPH <" + ont + ">{\r\n"
					+ "    {?sub ?pred ?x} UNION {?x ?pred1 ?obj} UNION {?sub1 ?x ?obj1}.\r\n"
					+ "        FILTER(!STRSTARTS(STR(?x), \"" + ont + "\") && isURI(?x))\r\n" + "    }\r\n" + " }";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// Porcentaje de términos propios de esta ontología y que porcentaje de términos es de ontologías ajenas
		get("OTI/Ontologies/ForeignPercentage", (request, response) -> {
			String ont = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "SELECT DISTINCT ?x\r\n" + " WHERE {\r\n"
					+ "    GRAPH <" + ont + ">{\r\n"
					+ "    {?sub ?pred ?x} UNION {?x ?pred1 ?obj} UNION {?sub1 ?x ?obj1}.\r\n"
					+ "        FILTER(!STRSTARTS(STR(?x), \"" + ont + "\") && isURI(?x))\r\n" + "    }\r\n" + " }";
			String res = SparqlEndpoint.query(query, ResultsFormat.FMT_COUNT).toString();
			res = res.replaceAll("[^0-9]","");
			int val1 = Integer.parseInt(res);
			String query1 = "BASE <http://localhost:4567/>\r\n" + "SELECT DISTINCT ?x\r\n" + " WHERE {\r\n"
					+ "    GRAPH <" + ont + ">{\r\n"
					+ "    {?sub ?pred ?x} UNION {?x ?pred1 ?obj} UNION {?sub1 ?x ?obj1}.\r\n"
					+ "        FILTER(isURI(?x))\r\n" + "    }\r\n" + " }";
			res = SparqlEndpoint.query(query1, ResultsFormat.FMT_COUNT).toString();
			res = res.replaceAll("[^0-9]", "");
			int val2 = Integer.parseInt(res);
			return (double) val1 / val2 * 100;
		});

		//Obtención del rango de un object type property
		get("OTI/Ontologies/RangeofProperty", (request, response) -> {
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
		get("OTI/Ontologies/OntologiesReused", (request, response) -> {
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
				if (!valor.equals(uri)) 
					res.add(valor+"\n");
			}
			return res;
		});

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//CRUD DE LA LISTA DE TÉRMINOS MANUAL 

		// POST de los términos que se añaden a la lista
		post("OTI/Services/ListofTerms", (request, response) -> {
			String listofterms = request.body();
			listofterms = listofterms.replace("\"", " ");
			Map<String, String> terms = new HashMap<String, String>();
			List<String> matches = new ArrayList<>();
			Matcher m1 = Pattern.compile("http.+").matcher(listofterms);
			while (m1.find())
				matches.add(m1.group().replace(" ", ""));
			for (int i = 0; i < matches.size();)
				terms.put(matches.get(i++), matches.get(i++));
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "INSERT DATA{ \r\n" + "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n";
			for (Map.Entry<String, String> entry : terms.entrySet())
				query += "<" + entry.getKey() + "> dc:source <" + entry.getValue() + ">.\r\n";
			query += "}}";
			SparqlEndpoint.update(query);
			return "List of Terms from a Service Added";
		});

		// GET que devuelve la lista completa de términos asociados a servicios.
		get("OTI/Services/ListofTerms", (request, body) -> {
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "SELECT DISTINCT ?term ?service WHERE {\r\n"
					+ "    GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n"
					+ "        ?term dc:source ?service.\r\n" + "    }\r\n" + "}\r\n" + "";
			String res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON).toString();
			return res0;
		});

		// GET devuelve los términos asociados a un servicio determinado
		get("OTI/Services/TermsfromService", (request, body) -> {
			String service = request.queryParams("service");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "SELECT DISTINCT ?term WHERE {\r\n"
					+ "    GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n" + "        ?term dc:source <"
					+ service + ">.\r\n" + "    }\r\n" + "}\r\n" + "";
			String res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON).toString();
			return res0;
		});

		// DELETE borra entradas determinadas de la lista de términos
		delete("OTI/Services/ListofTerms/Term", (request, response) -> {
			String listofterms = request.body();
			listofterms = listofterms.replace("\""," ");
			Map<String, String> terms = new HashMap<String, String>();
			List<String> matches = new ArrayList<>();
			Matcher m1 = Pattern.compile("http.+").matcher(listofterms);
			while (m1.find())
				matches.add(m1.group().replace(" ", ""));
			for (int i = 0; i < matches.size();)
				terms.put(matches.get(i++), matches.get(i++));
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "DELETE DATA{ \r\n" + "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n";
			for (Map.Entry<String, String> entry : terms.entrySet())
				query += "<" + entry.getKey() + "> dc:source <" + entry.getValue() + ">.\r\n";
			query += "}}";
			SparqlEndpoint.update(query);
			return "Given Terms Deleted";
		});

		//DELETE borra las entradas asociadas a un servicio de terminado
		delete("OTI/Services/ListofTerms/",(request,response)->{
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
			return "List of Terms from a given Service Deleted";
		});
		
		// PRE partimos de la premisa de que no se están introduciendo términos de servicios que no son el dado.
		// PUT, elimina las tripletas referentes a todos los términos asociados a un servicio y los actualiza con los pasados en el body.
		put("OTI/Services/TermsfromService", (request, response) -> {
			// Eliminamos las tripletas correspondientes a un servicio.
			String service = request.queryParams("service");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "WITH <http://localhost:4567/OD/TermsfromService>\r\n" + "DELETE { \r\n"
					+ "   		 ?x dc:source <" + service + ">.\r\n" + "    }\r\n" + "    WHERE{\r\n"
					+ "       	  ?x dc:source <" + service + ">.\r\n" + "}";
			SparqlEndpoint.update(query);
			// Código en el que se añade el nuevo listado de términos.
			String listofterms = request.body();
			listofterms = listofterms.replace("\"", " ");
			Map<String, String> terms = new HashMap<String, String>();
			List<String> matches = new ArrayList<>();
			Matcher m1 = Pattern.compile("http.+").matcher(listofterms);
			while (m1.find())
				matches.add(m1.group().replace(" ", ""));
			for (int i = 0; i < matches.size();)
				terms.put(matches.get(i++), matches.get(i++));
			String query1 = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "INSERT DATA{ \r\n" + "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n";
			for (Map.Entry<String, String> entry : terms.entrySet())
				query1 += "<" + entry.getKey() + "> dc:source <" + entry.getValue() + ">.\r\n";
			query1 += "}}";
			SparqlEndpoint.update(query1);
			return "The List of Terms has been Updated";
		});

//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// MÉTODOS DE EXTRACCIÓN DE INFORMACIÓN DE LOS METADATOS DE LAS ONTOLOGÍAS.  

		// GET de los metadatos asociados a la URI de una ontología.
		get("/OTI/Ontology/Metadata", (request, response) -> {
			String uri = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?prop ?obj \r\n" + "WHERE {\r\n" + "    <" + uri + "> ?prop ?obj .\r\n" + "}\r\n";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});
		
		// Método de extracción de la licencia de una ontología.
		get("OTI/Ontologies/Metadata/License", (request, response) -> {
			String ont = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dct: <http://purl.org/dc/terms/>\r\n"
					+ "SELECT ?license WHERE {\r\n" + "    <" + ont + "> dct:license ?license\r\n" + "}";
			String res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON).toString();
			return res0;
		});

//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// METODOS DE EXTRACCIÓN DE INFORMACIÓN DEL LISTADO DE TÉRMINOS		

		// Método term location para obtener en que servicio está presente un término.
		get("OTI/Services/TermLocation", (request, response) -> {
			String term = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "SELECT DISTINCT ?services  WHERE {\r\n"
					+ "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n" + "       <" + term
					+ "> dc:source ?services\r\n" + "    }\r\n" + "\r\n" + "}";
			System.out.println(query);
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

		// Método de ontologías reutilizadas que obtiene los namespaces a los que los términos presentes pertenecen.
		get("OTI/Services/OntologiesReused", (request, response) -> {
			String service = request.queryParams("service");
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "SELECT DISTINCT ?x  WHERE {\r\n" + "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n"
					+ "        ?x dc:source <" + service + ">.\r\n" + "    }\r\n" + "}";
			String res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_TEXT).toString();
			List<String> matches = new ArrayList<>();
			Matcher m = Pattern.compile("<([^><]+)>").matcher(res0);
			while (m.find()) {
				matches.add(m.group(1));
			}
			return matches;
		});

		// Método que devuelva que tipo de término es un término dado
		get("OTI/Services/TypeofTerm", (request, response) -> {
			String term = request.queryParams("term");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + "SELECT ?type WHERE {\r\n"
					+ "	<" + term + "> rdf:type ?type   \r\n" + "}";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//Métodos de CRUD de la extracción automática de términos		

		post("OTI/Services/ListofTerms/Automatic", (request, response) -> {
			// Nos interesa extraer del servicio los términos ontológicos (clases y propertys)
			// De la extracción automática se excluye rdf, rdfs y owl
			// 1. Creación de una lista de los namespaces que debe de excluir, de base serían rdf, rdfs y owl.
			String reserved[] = { "http://www.w3.org/1999/02/22-rdf-syntax-ns", "http://www.w3.org/2000/01/rdf-schema","http://www.w3.org/2002/07/owl#", "http://www.w3.org/2001/XMLSchema#" };
			// 2. Nos bajamos el servicio en formato rdf mediante la librería jena
			String url = request.queryParams("url");
			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			m.read(url);
			OntModel mBase = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m.getBaseModel());
			// 3. Del servicio se extraen los namespaces presentes -> Nos permite obtener aquellos que más adelante no estén presentes
			Map<String, String> ns = mBase.getNsPrefixMap();
			// De base el servicio no tiene una uri asociada sino que tiene un conjunto de tripletas sin metadatos de base (partiendo de DBPedia)
			// De cara a añadirlo a OD se incluye la url de la que se extrae el conjunto de términos
			// Búsqueda de namespaces que sean de la misma fuente que los datos para desestimarlos como ontologías reutilizadas para la extracción de términos.
			URL parsedurl = new URL(url);
			String parsedurl1 = parsedurl.getAuthority();
			Map<String, String> nsaux = new HashMap<String, String>();
			nsaux.putAll(ns);
			// Añadir líneas para que también excluya las del conjunto st.
			for (String clave : ns.keySet()) {
				String valor = ns.get(clave);
				for (int i = 0; i < reserved.length; i++) {
					if (valor.contains(parsedurl1) || valor.startsWith(reserved[i])) 
						nsaux.remove(clave);
				}
			}
			// 4. Recorriendo las tripletas presentes se extraen aquellos términos que no son literales ni de este namespace
			StmtIterator st = m.listStatements();
			List<String> listofterms = new ArrayList<String>();
			Statement elem;
			// 5. Estos términos se almacenarán en una lista comprobando una serie de condiciones.
			while (st.hasNext()) {
				elem = st.next();
				for (String clave : nsaux.keySet()) {
					if (!listofterms.contains(elem.getSubject().toString()) && elem.getSubject().toString().startsWith(nsaux.get(clave))) 
						listofterms.add(elem.getSubject().toString());
					if (!listofterms.contains(elem.getPredicate().toString()) && elem.getPredicate().toString().startsWith(nsaux.get(clave))) 
						listofterms.add(elem.getPredicate().toString());
					if (!listofterms.contains(elem.getObject().toString()) && elem.getObject().toString().startsWith(nsaux.get(clave))) 
						listofterms.add(elem.getObject().toString());
				}
			}
			// 6. Se hace sparql querie en la que se insertan estos términos en el grafo terms from service
			String query = "BASE <http://localhost:4567/>\r\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n"
					+ "INSERT DATA{ \r\n" + "     GRAPH <http://localhost:4567/OD/TermsfromService> {\r\n";
			for (int i = 0; i < listofterms.size(); i++)
				query += "<" + listofterms.get(i) + "> dc:source <" + url + ">.\r\n";
			query += "}}";
			SparqlEndpoint.update(query);			
			// Método que al añadirlo devuelva si algún namespace no está presente en local al subir el servicio.
			List<String> res = new ArrayList<String>();
			res.add("Conjunto de ontologías no presentes en el servicio \r\n");
			for (String clave : nsaux.keySet()) {
				String query1 = "BASE <http://localhost:4567/>\r\n"
						+ "ASK{\r\n"
						+ "    GRAPH <"+nsaux.get(clave)+">{\r\n"
						+ "    	 ?a ?b ?c.\r\n"
						+ "	}\r\n"
						+ "}";
				ByteArrayOutputStream res0 = SparqlEndpoint.query(query1, ResultsFormat.FMT_TEXT);
				String res1 = res0.toString().trim();
				if(res1.equals("no")) 
					res.add(nsaux.get(clave)+"\r\n");	
			} 
			return res;			
		});
				
//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//MÉTODOS QUE PERMITEN LA RESOLUCIÓN DE SPARQL QUERIES
		
		//Método GET que permite realizar queries en el raw de la petición para obtener respuestas de sparql del servicio.
		get("OTI/Query",(request,response)->{
			//String query = request.raw().toString();
			String query = request.queryParams("query");
			//System.out.println(query);
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query, ResultsFormat.FMT_RDF_TURTLE);
			return res0.toString();
		});
		
		//Método GET que permite realizar queries en el raw de la petición para actualizar la BD mediante una query
		get("OTI/Update",(request,response)->{
			String query = request.queryParams("query");
			SparqlEndpoint.update(query);
			return "Successful Update";
		});

	}
}
