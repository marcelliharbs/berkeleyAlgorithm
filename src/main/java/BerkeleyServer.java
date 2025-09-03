import java.io.*;
import java.net.*;
import java.util.*;

public class BerkeleyServer {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Uso: java BerkeleyServer <porta> <numClientes>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int numClientes = Integer.parseInt(args[1]);

        List<Socket> clients = new ArrayList<>();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("[SERVIDOR] Aguardando " + numClientes + " clientes na porta " + port + "...");

        while (clients.size() < numClientes) {
            Socket client = serverSocket.accept();
            clients.add(client);
            System.out.println("[SERVIDOR] Cliente conectado: " + client.getRemoteSocketAddress());
        }

        long serverTime = System.currentTimeMillis();
        for (Socket client : clients) {
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println("TIME_REQUEST:" + serverTime);
        }

        Map<Socket, Long> clientOffsets = new HashMap<>();
        List<Long> offsets = new ArrayList<>();
        offsets.add(0L);

        for (Socket client : clients) {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line = in.readLine();
            long off = 0;
            if (line != null && line.startsWith("OFFSET:")) {
                off = Long.parseLong(line.substring(7));
                offsets.add(off);
                clientOffsets.put(client, off);
                System.out.println("[SERVIDOR] Recebido OFFSET do cliente: " + off);
            }
        }

        long avg = Math.round(offsets.stream().mapToDouble(Long::doubleValue).average().orElse(0.0));
        System.out.println("[SERVIDOR] Média dos offsets = " + avg);

        for (Socket client : clients) {
            long off = clientOffsets.getOrDefault(client, 0L);
            long adjust = avg - off;
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println("ADJUST:" + adjust);
            System.out.println("[SERVIDOR] Enviado ADJUST=" + adjust);
            client.close();
        }

        serverSocket.close();
        System.out.println("[SERVIDOR] Rodada finalizada.");
    }
}
