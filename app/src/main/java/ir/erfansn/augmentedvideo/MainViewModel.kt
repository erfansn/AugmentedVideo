package ir.erfansn.augmentedvideo

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.*
import com.google.android.filament.MaterialInstance
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import io.github.sceneview.ar.arcore.isTracking
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.material.MaterialLoader
import io.github.sceneview.material.destroy
import io.github.sceneview.material.setExternalTexture
import io.github.sceneview.model.GLTFLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.destroy
import io.github.sceneview.node.ModelNode
import io.github.sceneview.utils.getResourceUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _node = MutableStateFlow<ArNode?>(null)
    val node = _node.asStateFlow()

    private lateinit var materialInstance: MaterialInstance
    private lateinit var modelInstance: ModelInstance

    init {
        viewModelScope.launch {
            materialInstance = MaterialLoader.loadMaterial(
                context = application,
                lifecycle = dummyLifecycle,
                filamatFileLocation = application.getResourceUri(R.raw.av_material)
            )!!
            modelInstance = GLTFLoader.loadModel(
                context = application,
                gltfFileLocation = application.getResourceUri(R.raw.av_model),
            )!!.instance
        }
    }

    fun renderVideo(augmentedImages: List<AugmentedImage>) {
        if (_node.value != null) return

        val trackedAugmentedImage = augmentedImages.firstOrNull { it.isTracking } ?: return

        _node.update {
            ArNode().apply {
                applyPosePosition = true
                anchor = trackedAugmentedImage.let {
                    it.createAnchor(it.centerPose)
                }

                ModelNode().apply {
                    val externalTexture = ExternalTexture(null)
                    materialInstance.setExternalTexture("videoTexture", externalTexture.filamentTexture)

                    setMaterial(materialInstance)
                    setModelInstance(modelInstance)

                    val mediaPlayer = MediaPlayer.create(getApplication(), R.raw.matrix)
                    mediaPlayer.setSurface(externalTexture.surface)
                    mediaPlayer.isLooping = true
                    mediaPlayer.start()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        materialInstance.destroy()
        modelInstance.destroy()
        _node.value?.destroy()
    }
}

private val dummyLifecycle = object : Lifecycle() {
    override fun addObserver(observer: LifecycleObserver) = Unit
    override fun removeObserver(observer: LifecycleObserver) = Unit
    override fun getCurrentState() = State.INITIALIZED
}