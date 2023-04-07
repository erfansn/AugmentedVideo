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

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.*
import com.google.android.filament.MaterialInstance
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.rendering.ExternalTexture
import io.github.sceneview.ar.arcore.isTracking
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.material.MaterialLoader
import io.github.sceneview.material.destroy
import io.github.sceneview.material.setExternalTexture
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.model.*
import io.github.sceneview.node.ModelNode
import io.github.sceneview.utils.getResourceUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _node = MutableStateFlow<ArNode?>(null)
    val node = _node.asStateFlow()

    private lateinit var avMaterialInstance: MaterialInstance
    private lateinit var avModelInstance: ModelInstance
    private lateinit var externalTexture: ExternalTexture
    private lateinit var mediaPlayer: MediaPlayer

    init {
        viewModelScope.launch {
            avModelInstance = GLTFLoader.loadModel(
                context = application,
                gltfFileLocation = application.getResourceUri(R.raw.av_model),
            )!!.instance
            avMaterialInstance = MaterialLoader.loadMaterial(
                context = application,
                lifecycle = dummyLifecycle,
                filamatFileLocation = application.getResourceUri(R.raw.av_material)
            )!!

            externalTexture = ExternalTexture(null)
            avMaterialInstance.setExternalTexture("videoTexture", externalTexture.filamentTexture)

            mediaPlayer = MediaPlayer.create(getApplication(), R.raw.matrix).apply {
                setSurface(externalTexture.surface)
                isLooping = true
            }
        }
    }

    fun renderVideo(augmentedImages: List<AugmentedImage>) {
        if (_node.value != null) return

        val trackedAugmentedImage = augmentedImages.firstOrNull { it.isTracking } ?: return

        _node.update {
            ArNode().apply {
                applyPoseRotation = true
                anchor = trackedAugmentedImage.let {
                    it.createAnchor(it.centerPose)
                }

                modelScale = Scale(
                    x = trackedAugmentedImage.extentX,
                    z = trackedAugmentedImage.extentZ
                )
                modelRotation = Rotation(x = 180f)

                addChild(ModelNode().apply {
                    setModelInstance(avModelInstance)
                    setMaterial(avMaterialInstance)
                    setReceiveShadows(false)
                    setCastShadows(false)

                    mediaPlayer.start()
                })
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        avMaterialInstance.destroy()
        avModelInstance.destroy()
        _node.value?.destroy()
        mediaPlayer.release()
        externalTexture.destroy()
    }
}

private val dummyLifecycle = object : Lifecycle() {
    override val currentState: State = State.INITIALIZED
    override fun addObserver(observer: LifecycleObserver) = Unit
    override fun removeObserver(observer: LifecycleObserver) = Unit
}