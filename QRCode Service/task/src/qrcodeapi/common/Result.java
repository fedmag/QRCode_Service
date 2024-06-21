package qrcodeapi.common;

import org.springframework.http.HttpStatus;

public class Result<T> {

  private final ErrorResult error;
  private final SuccessResult<T> success;
  private HttpStatus httpStatus = HttpStatus.OK;

  public Result(ErrorResult error, HttpStatus httpStatus) {
    this.error = error;
    this.success = null;
    if (httpStatus != null) {
      this.httpStatus = httpStatus;
    }
  }

  public Result(SuccessResult<T> success, HttpStatus httpStatus) {
    this.error = null;
    this.success = success;
    if (httpStatus != null) {
      this.httpStatus = httpStatus;
    }
  }

  public boolean isError() {
    return error != null;
  }

  public boolean isSuccess() {
    return success != null;
  }

  public ErrorResult getError() {
    return error;
  }

  public SuccessResult<T> getSuccess() {
    return success;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }
}