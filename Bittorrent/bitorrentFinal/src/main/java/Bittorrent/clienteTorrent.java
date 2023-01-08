/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Bittorrent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.*;

/**
 *
 * @author karladbenitezquiroz
 */
public class clienteTorrent {
    DataOutputStream mensajeServidor;
    BufferedInputStream archivoDeEntrada = null;
    BufferedOutputStream archivoDeSalida= null;
    
    FileOutputStream rutaArchivoSalida = null;
    
    
    boolean aux = false;
    int tamanioMensjae = 0;
    String nombreNuevoArchivo;
    //String IpTracker="localhost";
    String direccionTorrent;
    int puertoUsado=6000;
    Scanner lectura = new Scanner(System.in);
    

    public static void main(String[] args) {
        clienteTorrent client = new clienteTorrent();
        client.conectar();
    }
    public void conectar(){ //Hacemos la conexion al servidor
    String miArchivo;
    try{
        System.out.println("Inserte la direccion del torrent que desea enviar ");
        direccionTorrent=lectura.next();
        //Aqui obtenemos la direccion y puerto del servidor / tracker
        System.out.println("Conexion a la direccion ip: "+obtenerIPServer(direccionTorrent)+" en el puerto: "+obtenerPuertoServer(direccionTorrent));
        
        //Abrimos la conexion del socket
        Socket socket1 = new Socket(obtenerIPServer(direccionTorrent),obtenerPuertoServer(direccionTorrent));
        DataOutputStream mensajesalida = new DataOutputStream(socket1.getOutputStream());
        DataInputStream mensajeentrada = new DataInputStream(socket1.getInputStream());
        mensajeServidor = mensajesalida;
        //Obtenemos y enviamos el nombre del archivo torrent que queremos buscar en el server
        miArchivo = nombreTorrent(direccionTorrent);
        
        mensajeServidor.writeUTF(miArchivo);
        
        //Aqui responde el servidor
        aux = mensajeentrada.readBoolean();
        if(aux == true ){ //Si el servidor si responde
        System.out.println("Mensaje recibido del servidor: "+mensajeentrada.readUTF());
        System.out.println("Mensaje recibido del servidor: "+mensajeentrada.readUTF());
        //Obtenemos el archivo descargado
        tamanioMensjae=mensajeentrada.readInt();
        nombreNuevoArchivo = mensajeentrada.readUTF();
        rutaArchivoSalida = new FileOutputStream("Descargas/descarga-"+nombreNuevoArchivo); 
        archivoDeSalida = new BufferedOutputStream(rutaArchivoSalida);
        archivoDeEntrada = new BufferedInputStream(socket1.getInputStream());
        //Aqui se escribe el archivo en la ruta deseada
            byte[] buffer = new byte[tamanioMensjae];
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (byte) archivoDeEntrada.read();
                }
        
        archivoDeSalida.write(buffer);
        archivoDeSalida.flush();
        //VERIFICAMOS
        verificar();
        archivoDeEntrada.close();
        archivoDeSalida.close();
        socket1.close();
        }else { //Si el servidor no responde
                System.out.println("Mensaje del servidor: " + mensajeentrada.readUTF());
                recuperarConexion();
            }
    }catch(Exception e){
        System.out.println("Error en la conexion de socket"+e.getMessage());
        //Recuperamos la conexion del socket con el servidor  
        recuperarConexion();
    }
    }
    
    public void recuperarConexion(){
    String archivo;
    try{
        System.out.println("Inserte la direccion del torrent que desea enviar");
        direccionTorrent=lectura.next();
        System.out.println("Conexion a la direccion ip:"+obtenerIPServer(direccionTorrent)+" en el puerto: "+obtenerPuertoServer(direccionTorrent));
        //Se abre la conexion con socket
        Socket socket1 = new Socket(obtenerIPServer(direccionTorrent),obtenerPuertoServer(direccionTorrent));
        DataOutputStream mensajesalida = new DataOutputStream(socket1.getOutputStream());
        DataInputStream mensajeentrada = new DataInputStream(socket1.getInputStream());
        mensajeServidor = mensajesalida;
        archivo = nombreTorrent(direccionTorrent);
        
        //Obtenemos y enviamos el nombre del archivo torrent que queremos buscar en el server
        mensajeServidor.writeUTF(archivo);
        //Aqui responde el servidor
        aux = mensajeentrada.readBoolean();
        if(aux == true ){
        System.out.println("Mensaje recibido del servidor: "+mensajeentrada.readUTF());
        System.out.println("Mensaje recibido del servidor: "+mensajeentrada.readUTF());
        tamanioMensjae=mensajeentrada.readInt();
        nombreNuevoArchivo = mensajeentrada.readUTF();
        
        rutaArchivoSalida = new FileOutputStream("Descargas/descarga-"+nombreNuevoArchivo); 
        archivoDeSalida = new BufferedOutputStream(rutaArchivoSalida);
        archivoDeEntrada = new BufferedInputStream(socket1.getInputStream());
        //Aqui se escribe el archivo en la ruta deseada
            byte[] buffer = new byte[tamanioMensjae];
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (byte) archivoDeEntrada.read();
                }
        
        archivoDeSalida.write(buffer);
        archivoDeSalida.flush();
        //VERIFICAMOS
        verificar();
        archivoDeEntrada.close();
        archivoDeSalida.close();
        socket1.close();
        }else {
                System.out.println("Mensaje recibido del servidor: " + mensajeentrada.readUTF());
                conectar();
            }
    }catch(Exception e){
        System.out.println("Error en la conexion del socket: "+e.getMessage());
        //AQUI SE VUELVE A INCIAR LA CONEXION  
        conectar();
    }
    }
    
    
    public void verificar(){
        int nuevoId=0;
        Socket socket2;
        ServerSocket socketServidor;
        servidorTorrent.verificarArchivo verificacion;
        
        try{
        socketServidor = new ServerSocket(puertoUsado);
        while(true){
        
        System.out.println("Socket abierto en el puerto: "+puertoUsado);
        socket2= socketServidor.accept();
        nuevoId++;
        System.out.println("\nSe conecto el cliente con ID: " + nuevoId + " desde la direccion: " + socket2.getInetAddress());
        
        verificacion = new servidorTorrent.verificarArchivo(socket2, nuevoId);
        verificacion.start();  
        }
    
        }catch(IOException e){

        
        }finally{}
    }
    
    
    
    public String nombreTorrent(String direccion){
    JSONParser  parser = new JSONParser();  
    String nombre=null;
    try{
         Object obj = parser.parse(new FileReader(direccion));
         JSONObject objetJ = new JSONObject(obj.toString());
         nombre = (String)objetJ.getString("name");
    }catch(Exception e){
    e.printStackTrace();
    }
    return nombre;
    }
    
    public String obtenerIPServer(String direccion){
    JSONParser  parser = new JSONParser();  
    String tracker=null;
    try{
         Object obj = parser.parse(new FileReader(direccion));
         JSONObject objetJ = new JSONObject(obj.toString());
         tracker = (String)objetJ.getString("tracker");
    }catch(Exception e){
    e.printStackTrace();
    }
    return tracker;
    }
    
    public Integer obtenerPuertoServer(String direccion){
    JSONParser  parser = new JSONParser();  
    int puerto=0;
    
    try{
         Object obj = parser.parse(new FileReader(direccion));
         JSONObject objetJ = new JSONObject(obj.toString());
         String pp = (String)objetJ.getString("puertoTracker");
         puerto = Integer.parseInt(pp);
    }catch(Exception e){
    e.printStackTrace();
    }
    return puerto;
    }
}
