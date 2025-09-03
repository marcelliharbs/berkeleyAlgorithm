import java.io.*;
import java.net.*;

public class BerkeleyClient {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Uso: java BerkeleyClient <host> <porta> <desvioMs>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        long skew = Long.parseLong(args[2]);

        Socket socket = new Socket(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String line = in.readLine();
        if (line != null && line.startsWith("TIME_REQUEST:")) {
            long serverTime = Long.parseLong(line.substring(13));
            long localTime = System.currentTimeMillis() + skew;
            long offset = localTime - serverTime;
            System.out.println("[CLIENTE] TIME_REQUEST recebido. Meu agora=" + localTime + ", OFFSET=" + offset);

            out.println("OFFSET:" + offset);

            line = in.readLine();
            if (line != null && line.startsWith("ADJUST:")) {
                long adjust = Long.parseLong(line.substring(7));
                skew += adjust;
                System.out.println("[CLIENTE] Ajuste recebido = " + adjust + ". Novo relógio = " + (System.currentTimeMillis() + skew));
            }
        }

        socket.close();
    }
}
