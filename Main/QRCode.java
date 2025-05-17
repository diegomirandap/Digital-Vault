package Main;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class QRCode {
    public static void mostrarQRCode(String uri) {
        try {
            int tamanho = 250;
            BitMatrix matrix = new MultiFormatWriter().encode(uri, BarcodeFormat.QR_CODE, tamanho, tamanho);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            ImageIcon icon = new ImageIcon(image);
            JLabel label = new JLabel(icon);
            JOptionPane.showMessageDialog(null, label, "Escaneie o QR Code", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao gerar QR Code: " + e.getMessage());
        }
    }
}

