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

package com.facebook.litho;

import static com.facebook.litho.EventDispatcherUtils.dispatchOnContextClick;

import android.view.View;
import androidx.annotation.Nullable;

/** Context click listener that triggers its underlying event handler. */
class ComponentContextClickListener implements View.OnContextClickListener {

  private @Nullable EventHandler<ContextClickEvent> mEventHandler;

  @Override
  public boolean onContextClick(View view) {
    if (mEventHandler != null) {
      return dispatchOnContextClick(mEventHandler, view);
    }

    return false;
  }

  EventHandler<ContextClickEvent> getEventHandler() {
    return mEventHandler;
  }

  void setEventHandler(@Nullable EventHandler<ContextClickEvent> eventHandler) {
    mEventHandler = eventHandler;
  }
}
