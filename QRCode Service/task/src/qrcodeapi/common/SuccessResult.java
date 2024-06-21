package qrcodeapi.common;

public class SuccessResult<T> {

  T data;

  public SuccessResult(T data) {
    this.data = data;
  }

  public T getData() {
    return data;
  }
}
