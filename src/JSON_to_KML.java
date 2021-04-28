import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.Vector;

import org.graalvm.polyglot.*;


public class JSON_to_KML {
    Vector<double[]> path; // this Vector of pairs holds the longitudes and latitudes from the json file

    public JSON_to_KML(){
        this.path = new Vector<>();
    }

    /* this function takes as input the directory of the json file
       with the coordinates and fills up the vector this.path      */
    public void parse_json(String json_dir){
        File json_file = new File(json_dir);
        try{
            Scanner f_reader = new Scanner(json_file);
            while (f_reader.hasNextLine()) {
                String map_point = f_reader.nextLine();
                map_point = map_point.split("}")[0] + "}\n";
                if (map_point.length() > 5){
                    Context context = Context.create("js");
                    Value parse = context.eval("js", "JSON.parse");
                    Value data = parse.execute(map_point);
                    double lng = data.getMember("lng").asDouble();
                    double lat = data.getMember("lat").asDouble();
                    this.path.add(new double[] {lng, lat});
                }
            }
        }
        catch (Exception e){
            System.err.println("Error in JSON_to_KML.parse_json()");
            e.printStackTrace();
        }
    }

    /* this function takes as input the directory of the kml file
       we want to create and creates that kml file using the vector this.path */
    public void build_kml(String kml_dir, String kml_name){
        File kml_file = new File(kml_dir);
        try {
            if (kml_file.createNewFile()){
                FileWriter fw = new FileWriter(kml_file);
                String opening =
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                                "  <Document>\n" +
                                "    <name>"+ kml_name + "</name>\n" +
                                "    <Placemark>\n" +
                                "      <LineString>\n" +
                                "        <coordinates>\n";

                String closing =
                        "        </coordinates>\n" +
                                "      </LineString>\n" +
                                "    </Placemark>\n" +
                                "  </Document>\n" +
                                "</kml>";

                StringBuilder coords = new StringBuilder();
                for (double[] coord : this.path){
                    coords.append("\t\t  ").append(coord[0]).append(", ").append(coord[1]).append("\n");
                }
                fw.write(opening + coords.toString() + closing);
                fw.close();
            }
            else if (kml_file.exists()){
                if (kml_file.delete()) {
                    System.out.println("Updating " + kml_dir);
                    this.build_kml(kml_dir, kml_name);
                } else {
                    throw new Exception("Failed to delete " + kml_file.getName());
                }
            }
            else{
                throw new Exception("Could not create file" + kml_dir);
            }
        }
        catch(Exception e){
            System.err.println("Error in JSON_to_KML.build_kml()");
            e.printStackTrace();

        }
    }

    public static void main(String[] args){
        JSON_to_KML jk = new JSON_to_KML();
        String json_dir = "../json_traj/" + args[0] + ".json";
        String kml_dir = "../kml_traj/" + args[0] + ".kml";
        jk.parse_json(json_dir);
        jk.build_kml(kml_dir, args[0]);
    }
}