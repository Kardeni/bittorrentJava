package Bittorrent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author karladbenitezquiroz
 */

public class Torrent {
    String id;
    String tracker;
    int puertoTracker;
    int pedazos;
    int ultimo_pedazo;
    String nombre;
    String archivo1;
    Boolean[]obtenidos;
    String[]checksum;
    static final int PEDAZO_TAM=102400;//en bytes
    static final int MAX_PETICION=10;
    static final String HASH_ALGORITHM="MD5";

    
    public static void main(String[] args) throws NoSuchAlgorithmException {
        String ruta, ip, puerto;
        Scanner scan=new Scanner(System.in);
        System.out.println("Inserte la ruta del archivo");
        ruta=scan.next();
        System.out.println("Inserte la ip del servidor");
        ip=scan.next();
        System.out.println("Inserte el puerto del servidor");
        puerto=scan.next();
    try{                                        
	MessageDigest m = MessageDigest.getInstance(HASH_ALGORITHM);
	String file_path=ruta;
        File fileObj = new File(file_path);
	
	if(fileObj.exists()){//si el archivo existe
            String fileName = fileObj.getName();
            int pos = fileName.lastIndexOf(".");
            if (pos > 0) {
		fileName = fileName.substring(0,pos);
            }
                                        
            //Se calcula la cantidad de fragmentos y el tamaño del ultimo fragmento
            double fileSize = fileObj.length();
            int piecesQty = (int)Math.ceil(fileSize/Torrent.PEDAZO_TAM);
            int lastPiece = 0;
            if(piecesQty*Torrent.PEDAZO_TAM > fileSize){
		lastPiece = (int)(fileSize - (piecesQty-1)*Torrent.PEDAZO_TAM);
            }
            //Obtener el checksum de cada fragmento
            JSONArray checksum = new JSONArray();
            InputStream is = new FileInputStream(file_path);
            for(int i = 0; i < piecesQty; i++){
		byte[] data = new byte[Torrent.PEDAZO_TAM];
		if (i < piecesQty-1) {
                    is.read(data, 0, Torrent.PEDAZO_TAM);
		}else if(i == piecesQty - 1){
                    data = new byte[lastPiece];
                    is.read(data, 0, lastPiece);
		}
		
		checksum.put(hash(m, data));
            }
            is.close();
            //Aqui ya se crea el archivo.torrent
            FileOutputStream fos = new FileOutputStream("archivosTorrent/"+fileName+".torrent");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            //Crea el torrent como un JSON
            JSONObject torrentObj = new JSONObject();
            try{
		torrentObj.put("tracker",ip);
		torrentObj.put("pieces", piecesQty);
		torrentObj.put("lastPiece", lastPiece);
		torrentObj.put("filepath", file_path);
		torrentObj.put("name", fileObj.getName());
		torrentObj.put("puertoTracker", puerto);
		torrentObj.put("checksum", checksum);
		//Genera ID unico para el objeto torrent
		torrentObj.put("id", hash(m, fileName.getBytes()));
            }catch(JSONException e){
		System.out.println("Error al escribir el Torrent"+e.getMessage());
            }
            //Guardamos en el archivo torrent
            bw.write(torrentObj.toString());
            bw.close();
            fos.close();
            System.out.println("Torrent creado exitosamente");
	}else{
            System.out.println("No se encontro el archivo");
	}
    }catch(FileNotFoundException fe){fe.printStackTrace();}
        catch(IOException ie){ie.printStackTrace();}
}
    
    public JSONObject Torrent(String rutaTorrent) throws IOException{
    File archivo = new File(rutaTorrent);
    FileReader archivolectura= new FileReader(archivo);
    BufferedReader bufferlectura = new BufferedReader(archivolectura);
    String linea = bufferlectura.readLine();
    if(linea!=null){
    JSONObject objeto = new JSONObject(linea);
    nombre=objeto.getString("name");
    pedazos=objeto.getInt("pieces");
    id=objeto.getString("id");
    tracker=objeto.getString("tracker");
    puertoTracker=objeto.getInt("puertoTracker");
    ultimo_pedazo=objeto.getInt("lastPiece");
    archivo1=objeto.getString("filepath");
    System.out.print("Id:\n"+id+"tracker:\n"+tracker+"PuertoTracker:\n"+puertoTracker+"Pedazos:\n"+pedazos+"Nombre:\n"+nombre+"Ruta:\n"+archivo);
    JSONArray checksumJSON = objeto.getJSONArray("checksum");
        checksum = new String[checksumJSON.length()];
        for(int i = 0, l = checksumJSON.length(); i < l; i++){
            checksum[i] = checksumJSON.getString(i);
        }
        return objeto;
    }
                bufferlectura.close();
		archivolectura.close(); 
                return null;
    }
    
    public Boolean isPieceValid(byte[]piece,int index){
                try{
			MessageDigest m = MessageDigest.getInstance(HASH_ALGORITHM);
			return hash(m, piece).equals(checksum[index]);
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
			return false;
		}
    }
    
    public static String hash(MessageDigest messageDigest,byte[]datos)throws NoSuchAlgorithmException{
    messageDigest.reset();
    messageDigest.update(datos);
    byte[]digest=messageDigest.digest();
    BigInteger bigInt = new BigInteger(1,digest);
    String hashtext=bigInt.toString(16);
    while(hashtext.length() < 32){
    hashtext = "0"+hashtext;
    }
    return hashtext;        
    }
}