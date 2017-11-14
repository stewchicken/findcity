
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author yw
 */
public class FindNearbyDECity {

    static LinkedHashMap<String, String> citymaps = new LinkedHashMap<String, String>();

    //here you give cities with name and postcode format
    static {
        citymaps.put("FrankfurtSued", "60596+DE");
        citymaps.put("FrankfurtRiedberg", "60438+DE");
        citymaps.put("FrankfurtEuropaViertel", "60327+DE");
        citymaps.put("Raunheim", "65479+DE");
        citymaps.put("Langen", "63225+DE");
        citymaps.put("Wiesbaden", "65183+DE");
        citymaps.put("Darmstadt", "64283+DE");
        citymaps.put("Kelsterbach", "65451+DE");
        citymaps.put("BadSoden", "65812+DE");
        citymaps.put("BadHomburg", "61348+DE");
    }

    public static String getNearByDECity(String postcode) throws Exception {
        //here you set proxy for httpclient if needed
        HttpHost proxy = new HttpHost("10.50.39.40", 8081, "http");
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

        String destinations = "";
        for (Map.Entry<String, String> entry : citymaps.entrySet()) {
            //System.out.println( entry.getValue());
            if (!destinations.equals("")) {
                destinations = destinations + "|";
            }
            destinations = destinations + entry.getValue();
        }
        // here use URIBuilder to create URL with parameters which contains speical characters 
        // it uses google map api rest service 
        URI uri = new URIBuilder("http://maps.googleapis.com/maps/api/distancematrix/json")
                .addParameter("origins", postcode + "+DE")
                .addParameter("destinations", destinations)
                .addParameter("mode", "driving")
                .addParameter("language", "en-EN")
                .addParameter("sensor", "false")
                .build();

        HttpGet getRequest = new HttpGet(uri);
        getRequest.addHeader("accept", "application/json");
        HttpResponse response = httpClient.execute(getRequest);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatusLine().getStatusCode());
        }
        BufferedReader br = new BufferedReader(
                new InputStreamReader((response.getEntity().getContent())));
        String output;
        StringBuilder content = new StringBuilder();
        while ((output = br.readLine()) != null) {
            content.append(output);
        }
        httpClient.getConnectionManager().shutdown();
        
        //here you parse jsonobject
        JSONObject jsonobj = new JSONObject(content.toString());
        JSONArray jsonarray = (JSONArray) jsonobj.get("rows");
        jsonobj = jsonarray.getJSONObject(0);
        jsonarray = (JSONArray) jsonobj.get("elements");
        int index = 0;
        int tempValue = -1;
        for (int i = 0; i < jsonarray.length(); i++) {
            jsonobj = (JSONObject) jsonarray.get(i);
            jsonobj = (JSONObject) jsonobj.get("distance");
            if (i == 0) {
                tempValue = (int) jsonobj.get("value");
                index = 0;
            }
            if (tempValue > (int) jsonobj.get("value")) {
                tempValue = (int) jsonobj.get("value");
                index = i;
            }
        }
        List<Object> list = new ArrayList<Object>(citymaps.keySet());
        System.out.println("among " + list);
        System.out.println("nearest to " + postcode + "  is   ");
        //here you return nearest city
        return (String) list.get(index);
    }

    public static void main(String args[]) throws Exception {
        System.out.println(getNearByDECity("55116"));
    }
}
