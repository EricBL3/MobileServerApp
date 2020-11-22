package mx.tec.mobileserverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private TextView serverMessages;
    private Button startBtn, saveBtn;

    //variable de instancia
    private ServerSocket server;
    private ObjectOutputStream out;
    private ObjectInputStream in;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverMessages = (TextView) findViewById(R.id.serverMessages);
        startBtn = (Button) findViewById(R.id.startBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Creamos socket servidor escuchando en el mismo puerto donde se comunica el cliente
                            server = new ServerSocket(9999);
                            serverMessages.append("The server started successfully!\n");

                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                            @SuppressWarnings("deprecation")
                            String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
                            serverMessages.append("To connect to this device use the IP Address:"+ ipAddress +"\n");

                            serverMessages.append("Waiting for a connection...\n");

                            // Creamos el socket para el servidor
                            Socket servidor = server.accept();
                            serverMessages.append("Client has connected...\n");
                            // Creamos flujo de entrada para leer los datos que envia el cliente
                            in = new ObjectInputStream(servidor.getInputStream());
                            serverMessages.append("Getting"+ in.available() +" info from client...\n");
                            String data = in.readUTF();

                            //Obtener nombre del cliente
                            //String name = data.toString();
                            serverMessages.append("Client: " + data + " has connected successfully.\n");
                            serverMessages.append("Waiting for files...\n");

                            // Obtenemos el archivo mediante la lectura de bytes enviados
                            while (true) {
                                File f = (File) in.readObject();
                                //Agregar el archivo recibido a la lista de archivos
                                //files.addElement(data);

                                String filename = f.getName();
                                serverMessages.append("File " + filename + " has been received successfully.\n");

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });



    }
}