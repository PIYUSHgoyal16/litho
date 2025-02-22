/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.rendercore.extensions;

import android.graphics.Rect;
import androidx.annotation.Nullable;
import com.facebook.rendercore.Host;
import com.facebook.rendercore.MountDelegate;
import com.facebook.rendercore.RenderTreeNode;
import com.facebook.rendercore.RenderUnit;
import com.facebook.rendercore.Systracer;
import java.util.HashSet;
import java.util.Set;

public class ExtensionState<State> {

  private final MountExtension mExtension;
  private final MountDelegate mMountDelegate;
  private final State mState;
  private final Set<Long> mLayoutOutputMountRefs = new HashSet<>();

  ExtensionState(
      final MountExtension extension, final MountDelegate mountDelegate, final State state) {
    mExtension = extension;
    mMountDelegate = mountDelegate;
    mState = state;
  }

  public int getRenderStateId() {
    return mMountDelegate.getMountDelegateTarget().getRenderStateId();
  }

  public Host getRootHost() {
    return mMountDelegate.getMountDelegateTarget().getRootHost();
  }

  public MountExtension<?, State> getExtension() {
    return mExtension;
  }

  public MountDelegate getMountDelegate() {
    return mMountDelegate;
  }

  public Systracer getTracer() {
    return getMountDelegate().getTracer();
  }

  public State getState() {
    return mState;
  }

  public void releaseAllAcquiredReferences() {
    for (Long id : mLayoutOutputMountRefs) {
      mMountDelegate.releaseMountRef(id);
    }
    mLayoutOutputMountRefs.clear();
  }

  public void acquireMountReference(final long id, final boolean isMounting) {
    final boolean alreadyOwnedRef = !mLayoutOutputMountRefs.add(id);
    if (alreadyOwnedRef) {
      throw new IllegalStateException("Cannot acquire the same reference more than once.");
    }

    if (isMounting) {
      mMountDelegate.acquireAndMountRef(id);
    } else {
      mMountDelegate.acquireMountRef(id);
    }
  }

  public void releaseMountReference(final long id, final boolean isMounting) {
    final boolean ownedRef = mLayoutOutputMountRefs.remove(id);
    if (!ownedRef) {
      throw new IllegalStateException("Trying to release a reference that wasn't acquired.");
    }

    if (isMounting) {
      mMountDelegate.releaseAndUnmountRef(id);
    } else {
      mMountDelegate.releaseMountRef(id);
    }
  }

  public boolean ownsReference(final long id) {
    return mLayoutOutputMountRefs.contains(id);
  }

  public void beforeMount(Rect localVisibleRect, @Nullable Object input) {
    mExtension.beforeMount(this, input, localVisibleRect);
  }

  public void afterMount() {
    mExtension.afterMount(this);
  }

  public void beforeMountItem(RenderTreeNode renderTreeNode, int index) {
    mExtension.beforeMountItem(this, renderTreeNode, index);
  }

  public void onVisibleBoundsChanged(Rect rect) {
    mExtension.onVisibleBoundsChanged(this, rect);
  }

  public void onUnbind() {
    mExtension.onUnbind(this);
  }

  public void onUnmount() {
    mExtension.onUnmount(this);
  }

  public void onBindItem(
      final RenderUnit renderUnit,
      final Object content,
      final Object layoutData,
      final Systracer tracer) {
    final boolean isTracing = tracer.isTracing();
    if (isTracing) {
      tracer.beginSection("Extension:onBindItem " + mExtension.getName());
    }
    mExtension.onBindItem(this, renderUnit, content, layoutData);
    if (isTracing) {
      tracer.endSection();
    }
  }

  public void onUnbindItem(
      final RenderUnit renderUnit,
      final Object content,
      final Object layoutData,
      final Systracer tracer) {
    final boolean isTracing = tracer.isTracing();
    if (isTracing) {
      tracer.beginSection("Extension:onUnbindItem " + mExtension.getName());
    }
    mExtension.onUnbindItem(this, renderUnit, content, layoutData);
    if (isTracing) {
      tracer.endSection();
    }
  }

  public void onUnmountItem(
      final RenderUnit<?> renderUnit,
      final Object content,
      final Object layoutData,
      final Systracer tracer) {
    final boolean isTracing = tracer.isTracing();
    if (isTracing) {
      tracer.beginSection("Extension:onUnmountItem " + mExtension.getName());
    }
    mExtension.onUnmountItem(this, renderUnit, content, layoutData);
    if (isTracing) {
      tracer.endSection();
    }
  }

  public void onMountItem(
      final RenderUnit<?> renderUnit,
      final Object content,
      final Object layoutData,
      final Systracer tracer) {
    final boolean isTracing = tracer.isTracing();
    if (isTracing) {
      tracer.beginSection("Extension:onMountItem " + mExtension.getName());
    }
    mExtension.onMountItem(this, renderUnit, content, layoutData);
    if (isTracing) {
      tracer.endSection();
    }
  }

  public void onBoundsAppliedToItem(
      final RenderUnit renderUnit,
      final Object content,
      final Object layoutData,
      final Systracer tracer) {
    final boolean isTracing = tracer.isTracing();
    if (isTracing) {
      tracer.beginSection("Extension:onBoundsAppliedToItem " + mExtension.getName());
    }
    mExtension.onBoundsAppliedToItem(this, renderUnit, content, layoutData);
    if (isTracing) {
      tracer.endSection();
    }
  }

  public boolean shouldUpdateItem(
      final RenderUnit<?> previousRenderUnit,
      final Object previousLayoutData,
      final RenderUnit<?> nextRenderUnit,
      final Object nextLayoutData,
      final Systracer tracer) {
    final boolean shouldUpdate;
    final boolean isTracing = tracer.isTracing();
    if (isTracing) {
      tracer.beginSection("Extension:shouldUpdateItem " + mExtension.getName());
    }
    shouldUpdate =
        mExtension.shouldUpdateItem(
            this, previousRenderUnit, previousLayoutData, nextRenderUnit, nextLayoutData);
    if (isTracing) {
      tracer.endSection();
    }
    return shouldUpdate;
  }
}
