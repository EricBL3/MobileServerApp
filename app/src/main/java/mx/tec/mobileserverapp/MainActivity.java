package mx.tec.mobileserverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {
    private TextView serverMessages;
    private Button startBtn, saveBtn;

    //variable de instancia
    private ServerSocket server;
    private DataOutputStream out;
    private DataInputStream in;
    private ArrayList<File> files;
    private int fileSize;
    private String fileName;
    private byte[] fileData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverMessages = (TextView) findViewById(R.id.serverMessages);
        startBtn = (Button) findViewById(R.id.startBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        files = new ArrayList<File>();

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
                            // Creamos flujo de entrada para leer los datos que envia el cliente
                            in = new DataInputStream(servidor.getInputStream());

                            serverMessages.append("Client has connected successfully.\n");
                            serverMessages.append("Waiting for files...\n");

                            // Obtenemos el archivo mediante la lectura de bytes enviados
                            while (true) {
                                fileName = in.readUTF();
                                serverMessages.append("Receiving file: "+fileName+"\n");
                                fileSize = in.readInt();
                                serverMessages.append("Receiving "+fileSize+" bytes.\n Please wait until all data is obtained.\n");
                                fileData = new byte[fileSize];

                                in.readFully(fileData);
                                serverMessages.append("File received with "+fileData.length+" bytes\n" +
                                        "To download the file, click Save File and select the destination.\n");

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverMessages.append("Attempting to download the file\n");
                //Checking the availability state of the External Storage.
                String state = Environment.getExternalStorageState();
                if (!Environment.MEDIA_MOUNTED.equals(state)) {
                    //If it isn't mounted - we can't write into it.
                    serverMessages.append("There's not enough space to download the file.\n");

                    return;
                }
                serverMessages.append("Creating file in: '"+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"'\n");

                //This point and below is responsible for the write operation
                try {
                    serverMessages.append("Creating the file that will be downloaded\n");
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    String[] split = fileName.split(".", 2);
                    intent.setType("application/"+split[1]);

                    intent.putExtra(Intent.EXTRA_TITLE, fileName);
                    startActivityForResult(intent, 1);

                } catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.
                try {
                    ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "w");
                    FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor());
                    serverMessages.append("Downloading the file ...\n");
                    outputStream.write(fileData);

                    outputStream.close();
                    Toast.makeText(getApplicationContext(), "File "+fileName+" downloaded successfully",Toast.LENGTH_SHORT).show();
                    serverMessages.append("The file was downloaded successfully.\n");
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }
}