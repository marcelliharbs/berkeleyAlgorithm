import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        String line = in.readLine();
        if (line != null && line.startsWith("TIME_REQUEST:")) {
            long serverTime = Long.parseLong(line.substring(13));
            long localTime = System.currentTimeMillis() + skew;
            long offset = localTime - serverTime;

            String horaLocal = formatMillis(localTime, formatter);
            String horaServidor = formatMillis(serverTime, formatter);

            System.out.println("[CLIENTE] TIME_REQUEST recebido.");
            System.out.println(" -> Meu agora        = " + horaLocal);
            System.out.println(" -> Horário do servidor = " + horaServidor);
            System.out.println(" -> OFFSET calculado = " + offset + " ms");

            out.println("OFFSET:" + offset);

            line = in.readLine();
            if (line != null && line.startsWith("ADJUST:")) {
                long adjust = Long.parseLong(line.substring(7));
                skew += adjust;
                long adjustedTime = System.currentTimeMillis() + skew;
                String adjustedTimeFormatted = formatMillis(adjustedTime, formatter);

                System.out.println("[CLIENTE] Ajuste recebido = " + adjust + " ms.");
                System.out.println(" -> Novo relógio = " + adjustedTimeFormatted);
            }
        }

        socket.close();
    }

    private static String formatMillis(long millis, DateTimeFormatter formatter) {
        Instant instant = Instant.ofEpochMilli(millis);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return dateTime.format(formatter);
    }
}
