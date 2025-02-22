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

package com.facebook.rendercore.transitions;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.facebook.litho.Transition;
import com.facebook.litho.TransitionId;
import com.facebook.litho.TransitionSet;
import com.facebook.litho.animation.AnimatedProperty;
import java.util.ArrayList;
import java.util.List;

public class TransitionUtils {
  public interface BoundsCallback {
    void onWidthHeightBoundsApplied(int width, int height);

    void onXYBoundsApplied(int x, int y);
  }

  public static void applySizeToDrawableForAnimation(Drawable drawable, int width, int height) {
    final Rect bounds = drawable.getBounds();
    drawable.setBounds(bounds.left, bounds.top, bounds.left + width, bounds.top + height);

    if (drawable instanceof TransitionUtils.BoundsCallback) {
      ((TransitionUtils.BoundsCallback) drawable).onWidthHeightBoundsApplied(width, height);
    }
  }

  public static void applyXYToDrawableForAnimation(Drawable drawable, int x, int y) {
    final Rect bounds = drawable.getBounds();
    drawable.setBounds(x, y, bounds.width() + x, bounds.height() + y);
    if (drawable instanceof TransitionUtils.BoundsCallback) {
      ((TransitionUtils.BoundsCallback) drawable).onXYBoundsApplied(x, y);
    }
  }

  /**
   * Collects info about root component bounds animation, specifically whether it has animations for
   * width and height also {@link Transition.TransitionUnit} for appear animation if such is
   * defined.
   */
  public static void collectRootBoundsTransitions(
      final TransitionId rootTransitionId,
      final Transition transition,
      final AnimatedProperty property,
      final Transition.RootBoundsTransition outRootBoundsTransition) {
    if (transition instanceof TransitionSet) {
      ArrayList<Transition> children = ((TransitionSet) transition).getChildren();
      for (int i = 0, size = children.size(); i < size; i++) {
        collectRootBoundsTransitions(
            rootTransitionId, children.get(i), property, outRootBoundsTransition);
      }
    } else if (transition instanceof Transition.TransitionUnit) {
      final Transition.TransitionUnit transitionUnit = (Transition.TransitionUnit) transition;
      if (transitionUnit.targets(rootTransitionId) && transitionUnit.targetsProperty(property)) {
        outRootBoundsTransition.hasTransition = true;
        if (transitionUnit.hasAppearAnimation()) {
          outRootBoundsTransition.appearTransition = transitionUnit;
        }
      }
    } else if (transition instanceof Transition.BaseTransitionUnitsBuilder) {
      final Transition.BaseTransitionUnitsBuilder builder =
          (Transition.BaseTransitionUnitsBuilder) transition;
      ArrayList<Transition.TransitionUnit> units = builder.getTransitionUnits();
      for (int i = 0, size = units.size(); i < size; i++) {
        collectRootBoundsTransitions(
            rootTransitionId, units.get(i), property, outRootBoundsTransition);
      }
    } else {
      throw new RuntimeException("Unhandled transition type: " + transition);
    }
  }

  public static @Nullable TransitionId createTransitionId(
      @Nullable final String transitionKey,
      @Nullable final Transition.TransitionKeyType transitionKeyType,
      @Nullable final String transitionOwnerKey,
      @Nullable String transitionGlobalKey) {
    final @TransitionId.Type int type;
    final String reference;
    final String extraData;

    if (!TextUtils.isEmpty(transitionKey)) {
      reference = transitionKey;
      if (transitionKeyType == Transition.TransitionKeyType.GLOBAL) {
        type = TransitionId.Type.GLOBAL;
        extraData = null;
      } else if (transitionKeyType == Transition.TransitionKeyType.LOCAL) {
        type = TransitionId.Type.SCOPED;
        extraData = transitionOwnerKey;
      } else {
        throw new IllegalArgumentException("Unhandled transition key type " + transitionKeyType);
      }
    } else {
      type = TransitionId.Type.AUTOGENERATED;
      reference = transitionGlobalKey;
      extraData = null;
    }

    return reference != null ? new TransitionId(type, reference, extraData) : null;
  }

  public static void addTransitions(
      Transition transition, List<Transition> outList, @Nullable String logContext) {
    if (transition instanceof Transition.BaseTransitionUnitsBuilder) {
      outList.addAll(((Transition.BaseTransitionUnitsBuilder) transition).getTransitionUnits());
    } else if (transition != null) {
      outList.add(transition);
    } else {
      throw new IllegalStateException(
          "[" + logContext + "] Adding null to transition list is not allowed.");
    }
  }

  public static void setOwnerKey(Transition transition, @Nullable String ownerKey) {
    if (transition instanceof Transition.TransitionUnit) {
      ((Transition.TransitionUnit) transition).setOwnerKey(ownerKey);
    } else if (transition instanceof TransitionSet) {
      ArrayList<Transition> children = ((TransitionSet) transition).getChildren();
      for (int index = children.size() - 1; index >= 0; index--) {
        setOwnerKey(children.get(index), ownerKey);
      }
    } else if (transition instanceof Transition.BaseTransitionUnitsBuilder) {
      ArrayList<Transition.TransitionUnit> units =
          ((Transition.BaseTransitionUnitsBuilder) transition).getTransitionUnits();
      for (int index = units.size() - 1; index >= 0; index--) {
        units.get(index).setOwnerKey(ownerKey);
      }
    } else {
      throw new RuntimeException("Unhandled transition type: " + transition);
    }
  }
}
