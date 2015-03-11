package com.bumptech.glide.load.engine;

import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.GlideContext;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.data.DataRewinder;
import com.bumptech.glide.load.data.LoadDataSet;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.request.BaseRequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Context for individual requests and decodes that contains and exposes classes necessary to
 * obtain, decode, and encode resources.
 *
 * @param <Model> the type of model used to load resources by this object.
 * @param <TranscodeClass> The type of resources returned using classes from this object.
 */
public class RequestContext<Model, TranscodeClass> extends ContextWrapper {
  private static final int UNSET = -1;
  private final GlideContext glideContext;
  private final Model model;
  private final Class<TranscodeClass> transcodeClass;
  private final BaseRequestOptions<?> requestOptions;
  private final Priority priority;
  private final int overrideWidth;
  private final int overrideHeight;
  private LoadDataSet<Model> loadDataSet;
  private Drawable errorDrawable;
  private Drawable placeholderDrawable;
  private int width = UNSET;
  private int height = UNSET;
  private List<Key> cacheKeys;
  private List<Key> sourceKeys;

  public RequestContext(GlideContext glideContext, Model model,
      Class<TranscodeClass> transcodeClass, BaseRequestOptions<?> requestOptions, Priority priority,
      int overrideWidth, int overrideHeight) {
    super(glideContext);
    this.glideContext = glideContext;
    this.model = model;
    this.transcodeClass = transcodeClass;
    this.requestOptions = requestOptions;
    this.priority = priority;
    this.overrideWidth = overrideWidth;
    this.overrideHeight = overrideHeight;
  }

  <Data> LoadPath<Data, ?, TranscodeClass> getLoadPath(Class<Data> dataClass) {
    return glideContext.getRegistry().getLoadPath(dataClass, getResourceClass(), transcodeClass);
  }

  synchronized void setDimens(int width, int height) {
    this.width = width;
    this.height = height;
  }

  synchronized boolean isSourceKey(Key key) {
    collectKeys();
    return sourceKeys.contains(key);
  }

  synchronized List<Key> getCacheKeys() {
    collectKeys();
    return cacheKeys;
  }

  private synchronized void collectKeys() {
    if (cacheKeys == null) {
      List<Key> cacheKeys = new ArrayList<>();
      List<Key> sourceKeys = new ArrayList<>();

      for (ModelLoader.LoadData<?> data : getLoadDataSet()) {
        if (data != null) {
          cacheKeys.add(data.sourceKey);
          sourceKeys.add(data.sourceKey);
          cacheKeys.addAll(data.alternateKeys);
        }
      }
      this.cacheKeys = Collections.unmodifiableList(cacheKeys);
      this.sourceKeys = Collections.unmodifiableList(sourceKeys);
    }
  }

  synchronized LoadDataSet<Model> getLoadDataSet() {
    if (width == UNSET || height == UNSET) {
      throw new IllegalStateException("Width and/or height are unset.");
    }
    if (loadDataSet == null) {
      loadDataSet = glideContext.getRegistry().getLoadDataSet(model, width, height,
          glideContext.getOptions().getOptions());
    }
    return loadDataSet;
  }

  Key getSignature() {
    return requestOptions.getSignature();
  }

  List<Class<?>> getRegisteredResourceClasses() {
    return glideContext.getRegistry()
        .getRegisteredResourceClasses(model.getClass(), requestOptions.getResourceClass());
  }

  Class<?> getResourceClass() {
    return requestOptions.getResourceClass();
  }

  <DecodedResource> Transformation<DecodedResource> getTransformation(
      Class<DecodedResource> decodedResourceClass) {
    return requestOptions.getTransformation(decodedResourceClass);
  }

  Map<Class<?>, Transformation<?>> getTransformations() {
    return requestOptions.getTransformations();
  }

  Options getOptions() {
    return requestOptions.getOptions();
  }

  boolean isMemoryCacheable() {
    return requestOptions.isMemoryCacheable();
  }

  DiskCacheStrategy getDiskCacheStrategy() {
    return requestOptions.getDiskCacheStrategy();
  }

  Priority getPriority() {
    return priority;
  }

  <X> DataRewinder<X> getRewinder(X data) {
    return glideContext.getRegistry().getRewinder(data);
  }

  boolean isResourceEncoderAvailable(Resource<?> resource) {
    return glideContext.getRegistry().isResourceEncoderAvailable(resource);
  }

  <ResourceClass> ResourceEncoder<ResourceClass> getResultEncoder(Resource<ResourceClass> resource)
      throws Registry.NoResultEncoderAvailableException {
    return glideContext.getRegistry().getResultEncoder(resource);
  }

  <X> Encoder<X> getSourceEncoder(X data) throws Registry.NoSourceEncoderAvailableException {
    return glideContext.getRegistry().getSourceEncoder(data);
  }

  LoadDataSet<File> getDataFetchers(File file, int width, int height)
      throws Registry.NoModelLoaderAvailableException {
    return glideContext.getRegistry().getLoadDataSet(file, width, height,
        glideContext.getOptions().getOptions());
  }

  public int getOverrideWidth() {
    return overrideWidth;
  }

  public int getOverrideHeight() {
    return overrideHeight;
  }

  public Model getModel() {
    return model;
  }

  public Class<TranscodeClass> getTranscodeClass() {
    return transcodeClass;
  }

  public float getSizeMultiplier() {
    return requestOptions.getSizeMultiplier();
  }

  public Drawable getErrorDrawable() {
    if (errorDrawable == null) {
      errorDrawable = requestOptions.getErrorPlaceholder();
      if (errorDrawable == null && requestOptions.getErrorId() > 0) {
        errorDrawable = getResources().getDrawable(requestOptions.getErrorId());
      }
    }
    return errorDrawable;
  }

  public Drawable getPlaceholderDrawable() {
     if (placeholderDrawable == null) {
      placeholderDrawable = requestOptions.getPlaceholderDrawable();
      if (placeholderDrawable == null && requestOptions.getPlaceholderId() > 0) {
        placeholderDrawable = getResources().getDrawable(requestOptions.getPlaceholderId());
      }
    }
    return placeholderDrawable;
  }
}
