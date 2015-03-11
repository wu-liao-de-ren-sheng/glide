package com.bumptech.glide.load.resource.gif;

import android.util.Log;

import com.bumptech.glide.Logs;
import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * A relatively inefficient decoder for {@link com.bumptech.glide.load.resource.gif.GifDrawable}
 * that converts {@link java.io.InputStream}s to {@link java.nio.ByteBuffer}s and then passes
 * the buffer to a wrapped decoder.
 */
public class StreamGifDecoder implements ResourceDecoder<InputStream, GifDrawable> {
  /**
   * If set to {@code true}, disables this decoder
   * ({@link #handles(InputStream, Options)} will return {@code false}). Defaults to
   * {@code false}.
   */
  public static final Option<Boolean> DISABLE_ANIMATION = Option.memory(
      "com.bumptech.glide.load.resource.gif.ByteBufferGifDecoder.DisableAnimation", false);

  private final ResourceDecoder<ByteBuffer, GifDrawable> byteBufferDecoder;

  public StreamGifDecoder(ResourceDecoder<ByteBuffer, GifDrawable> byteBufferDecoder) {
    this.byteBufferDecoder = byteBufferDecoder;
  }

  @Override
  public boolean handles(InputStream source, Options options) throws IOException {
    return !options.get(DISABLE_ANIMATION)
        && new ImageHeaderParser(source).getType() == ImageHeaderParser.ImageType.GIF;
  }

  @Override
  public Resource<GifDrawable> decode(InputStream source, int width, int height,
      Options options) throws IOException {
    byte[] data = inputStreamToBytes(source);
    if (data == null) {
      return null;
    }
    ByteBuffer byteBuffer = ByteBuffer.wrap(data);
    return byteBufferDecoder.decode(byteBuffer, width, height, options);
  }

  private static byte[] inputStreamToBytes(InputStream is) {
    final int bufferSize = 16384;
    ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufferSize);
    try {
      int nRead;
      byte[] data = new byte[bufferSize];
      while ((nRead = is.read(data)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
    } catch (IOException e) {
      if (Logs.isEnabled(Log.WARN)) {
        Logs.log(Log.WARN, "Error reading data from stream", e);
      }
      return null;
    }
    return buffer.toByteArray();
  }
}
