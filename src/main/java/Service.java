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
import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
				
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//CRUD DE REGISTRO DE ONTOLOG�AS.		
		
		//GET de una Ontolog�a concreta 
		get("/OD/Ontologies/", (request, response) -> {
			String uri = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT ?s ?p ?o\r\n"
					+ "WHERE {\r\n"
					+ "    GRAPH <"+uri+"> {\r\n"
					+ "		?s ?p ?o.\r\n"
					+ "	}\r\n"
					+ "}";
			System.out.println(query);
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});
		
		//M�todo auxiliar de post de ontolog�a hasta que este est� hecho
		post("/OD/Ontologies/", (request, response) -> {
			String url = request.queryParams("url"); 
			OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
			m.read(url);
			//Obtenemos el nombre de la ontolog�a mediante la creaci�n de otro modelo desde uno. 
			OntModel mBase = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m.getBaseModel());
			ExtendedIterator<Ontology> it = mBase.listOntologies();
			Ontology o = it.next();
			//Obtenci�n del t�tulo de la ontolog�a.
			StmtIterator it1 = m.listStatements();
			Statement elem;
			String pred="";
			String obj="";
			Boolean found = false;
			while(it1.hasNext() && !found) {
				elem = it1.next();
				pred = elem.getPredicate().toString();
				obj = elem.getObject().toString();
				if(pred.equals("http://purl.org/dc/elements/1.1/title")) {
					System.out.println(obj);
					found=true;
				}
			}
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n"
					+ "	<"+o+"> foaf:name \""+obj+"\"; \r\n"
					+ "    							foaf:homepage <"+url+">.\r\n"
					+ "} ";
			System.out.println(query);
			SparqlEndpoint.update(query);
			String query1 = "BASE <http://localhost:4567/>\r\n"
					+ "LOAD <"+url+"> INTO GRAPH <"+o+">";
			System.out.println(query1);
			SparqlEndpoint.update(query1);
			return "Ontolog�a a�adida AUX";
		});		
		
		
		// DELETE de la Ontolog�a
		delete("/OD/Ontologies/", (request, response) -> {
			String uri = request.queryParams("uri"); 
			String query1 = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "DROP GRAPH <"+uri+">;\r\n"
					+ "DELETE {\r\n"
					+ "    ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "}\r\n"
					+ "WHERE{\r\n"
					+ "     ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "     FILTER(str(?x) = \""+uri+"\") \r\n"
					+ "}\r\n";
			System.out.println(query1);
			SparqlEndpoint.update(query1);
		    return "Ontolog�a eliminada";
		});
		
		//PUT de las Ontolog�as.
		put("/OD/Ontologies/", (request, response) -> {
			String uri = request.queryParams("uri");
			String SOURCE = request.queryParams("SOURCE");
			String query ="BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "COPY <"+SOURCE+"> TO <"+uri+"> \r\n";
			System.out.println(query);
			SparqlEndpoint.update(query);
			String query1 ="BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
		            + "DELETE {\r\n"
					+ "    ?x foaf:homepage ?url.\r\n"
					+ "}"
					+ "WHERE{\r\n"
					+ "     ?x foaf:name ?name;\r\n"
					+ "       foaf:homepage ?url.\r\n"
					+ "     FILTER(str(?x) = \""+uri+"\") \r\n"
					+ "}";
			System.out.println(query1);
			SparqlEndpoint.update(query1);
			String query2 = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "INSERT DATA { \r\n"
					+ "	<"+uri+"> foaf:homepage <"+SOURCE+">.\r\n"
					+ "} ";
			System.out.println(query2);
			SparqlEndpoint.update(query2);
			return "Lista de tripletas actualizada";
		});
		
		//UC5.1 y UC5.2
		//M�todo para obtener qu� t�rminos est�n presentes en qu� ontolog�as.
		get("OD/Ontologies/TermLocation",(request,response) -> {
			String term = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
					+ "SELECT DISTINCT ?ont WHERE {\r\n"
					+ "    GRAPH ?ont {\r\n"
					+ "        {?sub ?pred "+term+"} UNION {"+term+" ?pred1 ?obj} UNION {?sub1 "+term+" ?obj1}\r\n"
					+ "    }\r\n"
					+ "}";
			//System.out.println(query);
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
			return res0.toString();			
		});
		
		//UC5.3
		//M�todo para obtener qu� t�rminos de una ontolog�a son ajenos
		get("OD/Ontologies/ForeignTerms",(request,response)->{
			String ont = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "SELECT DISTINCT ?x\r\n"
					+ " WHERE {\r\n"
					+ "    GRAPH <"+ont+">{\r\n"
					+ "    {?sub ?pred ?x} UNION {?x ?pred1 ?obj} UNION {?sub1 ?x ?obj1}.\r\n"
					+ "        FILTER(!STRSTARTS(STR(?x), \""+ont+"\") && isURI(?x))\r\n"
					+ "    }\r\n"
					+ " }";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
			System.out.println(res0.toString());
			return res0.toString();
		});
		
		//UC5.4 
		//Porcentaje de t�rminos propios de esta ontolog�a y que porcentaje de t�rminos es de ontolog�as ajenas
		get("OD/Ontologies/ForeignPercentage",(request,response)->{
			String ont = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "SELECT DISTINCT ?x\r\n"
					+ " WHERE {\r\n"
					+ "    GRAPH <"+ont+">{\r\n"
					+ "    {?sub ?pred ?x} UNION {?x ?pred1 ?obj} UNION {?sub1 ?x ?obj1}.\r\n"
					+ "        FILTER(!STRSTARTS(STR(?x), \""+ont+"\") && isURI(?x))\r\n"
					+ "    }\r\n"
					+ " }";
			String res = SparqlEndpoint.query(query,ResultsFormat.FMT_COUNT).toString();
			res = res.replaceAll("[^0-9]", ""); 
			int val1 = Integer.parseInt(res);
			System.out.println(val1);
			String query1 = "BASE <http://localhost:4567/>\r\n"
					+ "SELECT DISTINCT ?x\r\n"
					+ " WHERE {\r\n"
					+ "    GRAPH <"+ont+">{\r\n"
					+ "    {?sub ?pred ?x} UNION {?x ?pred1 ?obj} UNION {?sub1 ?x ?obj1}.\r\n"
					+ "        FILTER(isURI(?x))\r\n"
					+ "    }\r\n"
					+ " }";
			res = SparqlEndpoint.query(query1,ResultsFormat.FMT_COUNT).toString();
			res = res.replaceAll("[^0-9]", ""); 
			int val2 = Integer.parseInt(res);
			System.out.println(val2);
			return (double)val1/val2*100;
		});
		
		//UC5.5 Obtenci�n del rango de un object type propery
		get("OD/Ontologies/RangeofProperty", (request,response) -> {
			String prop = request.queryParams("property"); 
			String query = "BASE <http://localhost:4567/>\r\n"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n"
					+ "SELECT ?obj\r\n"
					+ " WHERE {\r\n"
					+ "   		<"+prop+"> rdf:type owl:ObjectProperty;\r\n"
					+ "           rdfs:range ?obj.\r\n"
					+ " }";
			ByteArrayOutputStream res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_RS_JSON);
			return res0.toString();
		});
		
		//UC5.6 �Qu� ontolog�as est�n importadas o reutilizan la una a la otra en OD?
		//1. B�squeda en el grafo por defecto la url asociada a la URI de la ontolog�a
		//2. Teniendo esta se carga en jena mediante el m�todo read.
		//3. Se obtiene el prefix map de este
		//4. Del prefix map obtenemos el prefijo que identifica a la ontolog�a
		//5. Se sustrae del prefix map el prejijo que identifica a la ontolog�a
		//6. Se vierten los prefijos restantes en un string o array que se pueda devolver
		//7. Se devuelve la informaci�n al usuario. 
		get("OD/Ontologies/OntologiesReused",(request,response) ->{
			String uri = request.queryParams("uri");
			String query = "BASE <http://localhost:4567/>\r\n"
	                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n"
	                + "SELECT ?url\r\n"
	                + "WHERE {\r\n"
	                + "    <"+uri+"> foaf:homepage ?url\r\n"
	                + "}\r\n"
	                + "";
			String res0 = SparqlEndpoint.query(query,ResultsFormat.FMT_TEXT).toString();
			String url = res0.substring(res0.lastIndexOf("<") + 1, res0.indexOf(">"));
			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			m.read(url);
			Map<String,String> pm = m.getNsPrefixMap();
			ArrayList<String> res = new ArrayList<String>();
			for(String clave: pm.keySet()) {
				String valor = pm.get(clave);
				System.out.println(clave);
				System.out.println(valor);
				if(!valor.equals(uri)) {
					res.add(valor);	
					res.add("\n");
				}
			}
			return res;
		});
		
		}
	
		public static String dependencyextraction(String onturl) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {
			//Flujo de c�digo:
			//1. Tenemos nombre de la ontolog�a de la que se quiere leer las dependencias
			//2. Leemos c�digo del que extrer las referencias del nombre
			//3. De ah� se obtiene o bien la l�nea que define el namespace para el atajo o se busca el ":" para hayar las ontolog�as
			//4. Se comprueba si est� presente en el repositorio de GraphDB
			//4.1 Si est� se contin�a
			//4.2 Si no est� se a�ade al grafo por defecto y se carga el grafo correspondiente mediante b�squeda en un buscador de ontolog�as
			//5. En un array de strings se a�aden la dependencia hallada
			//6. Se repiten los pasos 3-5 si hay m�s dependencias 
			//7. Se devuelve String que contiene las dependencias. 
			ArrayList<String> dependencies = new ArrayList<String>();
			List<String> dependenciesaux = Arrays.asList("rdf","rdfs","owl");
			dependenciesaux.addAll(dependenciesaux);
			OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
			m.read(onturl);
			Map<String,String> pm = m.getNsPrefixMap();
			boolean found = false;
			//Comprobamos si pertenecen a los namespaces de dependencies. 
			for(String clave:pm.keySet()) {
				//String value = pm.get(clave);
				for(String elem:dependencies){
					if(elem.equals(clave)) {
						//Est� en este conjunto de dependencias asique salimos del bucle
						found = true;
						break;
					}
					if(!found) {
						//�COMO ACCEDO A LOS PREFIXES?????
						//LA �NICA FORMA QUE SE ME OCURRE DE HACERLO ES MEDIANTE PARSEO DEL TEXTO PARA LUEGO HACER LA COMPARACI�N YA QUE NO PUEDO HACERLO DESDE GRAPHDB.
					}	
				}	
			}
			return null;			
		}
	
	}
