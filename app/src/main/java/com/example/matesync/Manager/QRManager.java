package com.example.matesync.Manager;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import java.util.HashMap;
import java.util.Map;

public class QRManager {

    private static final int ANCHO_QR = 400;
    private static final int ALTO_QR = 400;
    private static final int COLOR_QR_NEGRO = Color.BLACK;
    private static final int COLOR_QR_BLANCO = Color.WHITE;

    public static Bitmap generarCodigoQR(String texto, int ancho, int alto) throws Exception {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto no puede estar vacío");
        }

        MultiFormatWriter escritor = new MultiFormatWriter();

        // Configuración de pistas para mejor calidad
        Map<EncodeHintType, Object> pistas = new HashMap<>();
        pistas.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        pistas.put(EncodeHintType.MARGIN, 1);

        try {
            BitMatrix matrizBits = escritor.encode(texto, BarcodeFormat.QR_CODE, ancho, alto, pistas);
            return matrizBitsABitmap(matrizBits);
        } catch (Exception e) {
            throw new Exception("Error al generar código QR: " + e.getMessage());
        }
    }

    /**
     * Genera un código QR con dimensiones por defecto
     * @param texto Texto a codificar
     * @return Bitmap con el código QR
     * @throws Exception Si hay error en la generación
     */
    public static Bitmap generarCodigoQR(String texto) throws Exception {
        return generarCodigoQR(texto, ANCHO_QR, ALTO_QR);
    }

    /**
     * Lee un código QR desde un Bitmap
     * @param bitmap Imagen que contiene el código QR
     * @return Texto decodificado del QR
     * @throws Exception Si no se puede leer el QR
     */
    public static String leerCodigoQR(Bitmap bitmap) throws Exception {
        if (bitmap == null) {
            throw new IllegalArgumentException("El bitmap no puede ser nulo");
        }

        try {
            int[] pixeles = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(pixeles, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            RGBLuminanceSource fuente = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixeles);
            BinaryBitmap bitmapBinario = new BinaryBitmap(new HybridBinarizer(fuente));

            MultiFormatReader lector = new MultiFormatReader();
            Result resultado = lector.decode(bitmapBinario);

            return resultado.getText();
        } catch (Exception e) {
            throw new Exception("Error al leer código QR: " + e.getMessage());
        }
    }

    /**
     * Valida si un texto es un código de invitación válido
     * @param codigoInvitacion Código a validar
     * @return true si es válido, false en caso contrario
     */
    public static boolean esCodigoInvitacionValido(String codigoInvitacion) {
        return codigoInvitacion != null &&
                !codigoInvitacion.trim().isEmpty() &&
                codigoInvitacion.length() >= 6; // Mínimo 6 caracteres
    }

    /**
     * Genera un código de invitación único para un grupo
     * @param idGrupo ID del grupo
     * @param nombreGrupo Nombre del grupo
     * @return Código de invitación formateado
     */
    public static String generarCodigoInvitacion(String idGrupo, String nombreGrupo) {
        if (idGrupo == null || nombreGrupo == null) {
            throw new IllegalArgumentException("IdGrupo y NombreGrupo no pueden ser nulos");
        }

        // Formato: GRUPO_[ID]_[HASH_NOMBRE]
        String hashNombre = String.valueOf(nombreGrupo.hashCode()).replace("-", "");
        return "GRUPO_" + idGrupo + "_" + hashNombre;
    }

    /**
     * Extrae el ID del grupo desde un código de invitación
     * @param codigoInvitacion Código de invitación
     * @return ID del grupo o null si el formato es inválido
     */
    public static String extraerIdGrupoDeInvitacion(String codigoInvitacion) {
        if (!esCodigoInvitacionValido(codigoInvitacion) || !codigoInvitacion.startsWith("GRUPO_")) {
            return null;
        }

        try {
            String[] partes = codigoInvitacion.split("_");
            if (partes.length >= 3) {
                return partes[1]; // El ID está en la segunda posición
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    /**
     * Convierte BitMatrix a Bitmap
     * @param matriz BitMatrix del QR
     * @return Bitmap resultante
     */
    private static Bitmap matrizBitsABitmap(BitMatrix matriz) {
        int ancho = matriz.getWidth();
        int alto = matriz.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.RGB_565);

        for (int x = 0; x < ancho; x++) {
            for (int y = 0; y < alto; y++) {
                bitmap.setPixel(x, y, matriz.get(x, y) ? COLOR_QR_NEGRO : COLOR_QR_BLANCO);
            }
        }

        return bitmap;
    }
}