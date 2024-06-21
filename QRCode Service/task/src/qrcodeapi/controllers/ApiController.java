package qrcodeapi.controllers;

import com.google.zxing.WriterException;
import java.awt.image.BufferedImage;
import java.util.List;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties.Http;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qrcodeapi.common.ErrorResult;
import qrcodeapi.common.Result;
import qrcodeapi.services.ImageService;

@RestController
@RequestMapping("/api/")
public class ApiController {

  private final static List<String> ALLOWED_TYPES = List.of("png", "jpeg", "gif");
  private final static String DEFALUT_TYPE ="png";

  private final ImageService imageService;

  public ApiController(ImageService imageService) {
    this.imageService = imageService;
  }

  @GetMapping("health")
  public ResponseEntity<Void> health() {
    return ResponseEntity.ok().build();
  }

  @GetMapping("qrcode")
  public ResponseEntity<?> qrCode(
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String contents,
      @RequestParam(required = false) String correction) throws WriterException {

    System.out.println(
        "qrCode endpoint called. Provided params -> contents: " + contents + ", size: " + size + ", type: " + type);

    Result<BufferedImage> imageResult = ImageService.generateQrCodeImage(contents, size, size, correction);
    if (imageResult.isError()) {
      return ResponseEntity.status(imageResult.getHttpStatus()).body(imageResult.getError());
    }

    type = type != null ? type : DEFALUT_TYPE;
    if (!ALLOWED_TYPES.contains(type)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ErrorResult("Only png, jpeg and gif image types are supported"));
    }

    Result<byte[]> imageAsByteArray = ImageService.getImageAsByteArray(
        imageResult.getSuccess().getData(), type);
    return ResponseEntity.ok().contentType(getContentTypeFromString(type))
        .body(imageAsByteArray.getSuccess().getData());
  }

  private MediaType getContentTypeFromString(String type) {
    return switch (type) {
      case "png" -> MediaType.IMAGE_PNG;
      case "jpeg" -> MediaType.IMAGE_JPEG;
      case "gif" -> MediaType.IMAGE_GIF;
      default -> throw new IllegalStateException("Unexpected value: " + type);
    };
  }

}
