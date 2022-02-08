import static spark.Spark.*;
import java.util.ArrayList;
import java.net.*;

public class Service {
	public static void main (String[] args) {
		//Problema: Como Se incluyen las relaciones implícitas de las ontologías contenidas por la URL.
		//TODO: ¿ Y si en vez de esto y que sean variables que terminen con la ejecución lo hacemos de modo que escriba en un fichero los valores y los lea también o quizás sería mejor emplear una base de datos relacional?
		ArrayList<URL> list = new ArrayList<URL>();	
		URL ex0 = null;
		URL ex1 = null;
		try {
			ex0 = new URL(" https://www.google.es");
			ex1 = new URL(" https://web.whatsapp.com");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		list.add(ex0);
		list.add(ex1);
		
		//GET para que devuelva el conjunto de URLs guardadas
		get("/URL", (request, response) -> {
			return list;
	     });
		
		//GET Para que busque un elemento dentro de la lista al incluirlo en la URL.
		get("/URL/", (request, response) -> {
			for (int i=0; i<list.size(); i++) {
			    System.out.println(list.get(i).toString());
				//System.out.println(request.splat()[0]);
				String res = request.queryParams("URL");
				System.out.println(res);
				System.out.println(request.queryParams());
			   // if(list.get(i).toString().equals(request.splat()[0]))
				//	return list.get(i);
			    if(list.get(i).toString().equals(res))
					return list.get(i);
			}	
			return "El elemento introducido no se haya en la lista";
		//TODO: Preguntar a Raúl y Andrea porque no sirve como parámetro de query pero sí como valor de atributo.
	     });
		
		//POST Para crear una URL dentro de la lista de estas. 
		post("/URL/add/", (request, response) -> {
			URL elem = new URL(request.queryParams("URL"));
			list.add(elem);
			System.out.println(list);
		    return "URL Uploaded";
		});
		
		///DELETE Para eliminar una URL de dentro de la lista de estas. 
		delete("/URL/del/", (request, response) -> {
		   for (int k = 0; k<list.size(); k++) {
			   if (list.get(k).toString().equals(request.queryParams("URL"))) {
				   list.remove(k);
				   return "URL Removed";
			   }
		   }
		   return "URL not present in list";
		});
		
		//PUT a estas alturas no tiene mucho sentido debido a que las URLs solo contienen un valor y actualizarlas sería equivalente a eliminar una y añadir otra asique no vamos a implementar la operación de momento.
			
		//--------------------------------------------------------------------------------------------------------------------------------------------------------------------
		//AQUÍ COMENTA LA PARTE DEL SERVICIO EN LA QUE SE TRABAJA CON OWL COMO SE ESPECIFICA EN EL UC2
		//TODO: Aprender a emplear GRAPHDB y conectarlo con el servicio
		
		
	}
}
