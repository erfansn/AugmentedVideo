/*
 * Copyright 2023 Erfan Sn
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

package ir.erfansn.augmentedvideo

import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView

fun ArSceneView.setupAvConfigurations() {
    configureSession { arSession, config ->
        config.focusMode = Config.FocusMode.FIXED
        config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
        config.lightEstimationMode = Config.LightEstimationMode.DISABLED
        config.augmentedImageDatabase = resources.openRawResource(R.raw.av_db).use { database ->
            AugmentedImageDatabase.deserialize(arSession, database)
        }
    }
}
