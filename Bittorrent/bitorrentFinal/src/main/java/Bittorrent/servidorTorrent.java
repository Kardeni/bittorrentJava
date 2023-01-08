/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Bittorrent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author karladbenitezquiroz
 */
public class servidorTorrent {
    
    static class verificarArchivo extends Thread{
    //Defenimos variables para controlar los tiempos
    float initTime=0,tiempInicial=0,tiempoFInal=0,tiempoTotal=0;
    
    int cliente;
    Socket miSocket=null;
    ObjectInputStream entrada;
    ObjectOutputStream salida;
    DataInputStream entradaDatos;
    DataOutputStream salidaDatos;
    File miArchivo=null;
    
    verificarArchivo(Socket socket, int cliente){
        this.miSocket = socket;
        this.cliente = cliente;
    }
    //Funcion para verificar si existe el archivo dentro de la biblioteca
    boolean existe(String archivoivo){
        miArchivo = new File("biblioteca/"+archivoivo);
        Boolean res;
        res = miArchivo.exists();
        return res;
    }
     
    public void run(){
    try{
    //Construimos la entrada y salida de los datos hacia el cliente
    entradaDatos = new DataInputStream(miSocket.getInputStream());
    salidaDatos = new DataOutputStream(miSocket.getOutputStream());
    String archivo = entradaDatos.readUTF();
    //Aqui se verifica si el archivo esta en la biblioteca con la funcion existe
    if(existe(archivo)==true){
        tiempInicial=(System.currentTimeMillis()-this.initTime);
        System.out.println("Enviando archivo torrent al cliente con ID: "+cliente+"en un tiempo de: "+(System.currentTimeMillis()-this.initTime)+"ms");
        salidaDatos.writeBoolean(true);
        salidaDatos.writeUTF("Archivo:" + archivo + "existente en la biblioteca del servidor");
        salidaDatos.writeUTF("Archivo con tamaño de: " + (miArchivo.length() / 1024) + " KB   Archivo" + miArchivo.getName());
        salidaDatos.writeInt((int) miArchivo.length());
        salidaDatos.writeUTF(archivo);
        System.out.println("Enviando archivo:" + archivo + " a la direccion: " + miSocket.getInetAddress());
        FileInputStream misBytes = new FileInputStream(miArchivo);
        BufferedInputStream lectura = new BufferedInputStream(misBytes);//obtain input bytes from a file in a file system
                    
                    BufferedOutputStream salida = new BufferedOutputStream(miSocket.getOutputStream()); 
                    byte[] arreglo = new byte[(int) miArchivo.length()];
                    lectura.read(arreglo);

                    for (int i = 0; i < arreglo.length; i++) {
                        salida.write(arreglo[i]);
                    }
                    tiempoFInal=(System.currentTimeMillis() - this.initTime);
                    tiempoTotal=tiempoFInal-tiempInicial;
                    
                    System.out.println("Archivo Enviado a cliente:" + cliente);

                    System.out.println("El servidor termino el envio al cliente: " + cliente + " en:  "
                            + tiempoTotal + " ms");
                    System.out.println("Tiempo del cliente: "+cliente +": ("+(System.currentTimeMillis() - this.initTime)+") ms");

                    salida.flush();
                    salida.flush();
                    salida.close();
                    misBytes.close();
                }

                if (existe(archivo) == false) {//Si el archivo no existe en la biblioteca
                    salidaDatos.writeBoolean(false);
                    salidaDatos.writeUTF("El archivo: " + archivo + " no se encuentra en la bilbioteca del servidor");
                    //System.out.println("Respuesta enviada al cliente");
                }

    
    
    }catch(Exception ex){
    System.out.println("Error: "+ex.getMessage()+" para el cliente con id: "+cliente);
    }finally {
                try {
                    if (salida != null) {
                        salida.close();
                    }
                    if (entrada != null) {
                        entrada.close();
                    }
                    if (miSocket != null) {
                        miSocket.close();
                    }
                    System.out.println("Envio exitoso al cliente: "+cliente);

                } catch (Exception e) {
                    System.out.println("Mensaje error:  "+e.getMessage());
                }
            }
    }
    }
   
    public static void main(String[] args) {
        Socket socket = null;
        ServerSocket miServidor = null;
        verificarArchivo verificar;
        int puerto=6000;
        int id = 0;

        try {
            miServidor = new ServerSocket(puerto);
            while(true){
                System.out.println("Socket abierto en el puerto 6000 esperando clientes");
                socket = miServidor.accept();
                id++;
                System.out.println("\nSe conecto el cliente con el id: " + id + " desde la IP: " + socket.getInetAddress());
                verificar = new verificarArchivo(socket, id);
                verificar.start();                
            }

        } catch (IOException e) {
            System.out.println(e.getMessage() + " Del servidor");
            System.exit(3);
        } finally {
        }
    }
}
