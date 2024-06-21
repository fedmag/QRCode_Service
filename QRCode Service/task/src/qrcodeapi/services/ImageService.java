package qrcodeapi.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import qrcodeapi.common.ErrorResult;
import qrcodeapi.common.Result;
import qrcodeapi.common.SuccessResult;

@Service
public class ImageService {

  private final static int MIN_IMAGE_SIZE = 150;
  private final static int MAX_IMAGE_SIZE = 350;
  public static final int DEFAULT_IMAGE_SIZE = 250;
  private final static String DEFAULT_FORMAT_TYPE = "png";
  private final static String DEFAULT_CORRECTION_LEVEL = "l";
  private final static Map<String, ErrorCorrectionLevel> CORRECTION_LEVEL_MAP = Map.of(
      "L", ErrorCorrectionLevel.L,
      "M", ErrorCorrectionLevel.M,
      "Q", ErrorCorrectionLevel.Q,
      "H", ErrorCorrectionLevel.H
  );

  private final static QRCodeWriter qrCodeWriter = new QRCodeWriter();

  public static Result<BufferedImage> generateQrCodeImage(String data,
      Integer width,
      Integer height,
      String correctionLevel) throws WriterException {

    if (data == null || data.replace("\"", "").isBlank()) {
      return new Result<>(new ErrorResult("Contents cannot be null or blank"),
          HttpStatus.BAD_REQUEST);
    }

    width = width == null ? DEFAULT_IMAGE_SIZE : width;
    height = height == null ? DEFAULT_IMAGE_SIZE : height;
    if (!sizesAreValid(width, height)) {
      return new Result<>(
          new ErrorResult("Image size must be between 150 and 350 pixels"),
          HttpStatus.BAD_REQUEST);
    }

    correctionLevel = correctionLevel == null? DEFAULT_CORRECTION_LEVEL : correctionLevel;
    ErrorCorrectionLevel selectedLevel = CORRECTION_LEVEL_MAP.getOrDefault(correctionLevel.toUpperCase(), null);
    if (selectedLevel == null) {
      return new Result<>(new ErrorResult("Permitted error correction levels are L, M, Q, H"), HttpStatus.BAD_REQUEST);
    }
    Map<EncodeHintType, ?> hints = Map.of(EncodeHintType.ERROR_CORRECTION, selectedLevel);

    BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
    BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
    return new Result<>(new SuccessResult<>(image), HttpStatus.OK);
  }

  private static boolean sizesAreValid(Integer width, Integer height) {
    if (width < MIN_IMAGE_SIZE || width > MAX_IMAGE_SIZE) {
      return false;
    }
    return height >= MIN_IMAGE_SIZE && height <= MAX_IMAGE_SIZE;
  }

  public static Result<byte[]> getImageAsByteArray(BufferedImage image, String formatName) {
    formatName = formatName == null ? DEFAULT_FORMAT_TYPE : formatName;
    try (var baos = new ByteArrayOutputStream()) {
      ImageIO.write(image, formatName, baos); // writing the image in the PNG format
      return new Result<>(new SuccessResult<>(baos.toByteArray()), HttpStatus.OK);
    } catch (IOException e) {
      System.out.println("Unable to convert image to bytes.");
      throw new RuntimeException(e);
    }
  }
}
