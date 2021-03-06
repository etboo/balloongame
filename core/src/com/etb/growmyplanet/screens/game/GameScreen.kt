package com.etb.growmyplanet.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.viewport.FitViewport
import com.etb.growmyplanet.GrowMyPlanetGame
import com.etb.growmyplanet.base.AppConfig
import com.etb.growmyplanet.base.BaseScreen
import com.etb.growmyplanet.screens.di.ScreenScope
import com.etb.growmyplanet.screens.game.di.ControllersProvider
import com.etb.growmyplanet.screens.game.di.GameScreenComponent
import com.etb.growmyplanet.screens.game.usecases.SwapLevelsUseCase
import com.etb.growmyplanet.screens.game.views.BlackHole
import com.etb.growmyplanet.screens.game.views.Planet
import com.etb.growmyplanet.screens.game.views.Ring
import dagger.Module
import dagger.Provides
import javax.inject.Inject

/**
 * Created by etb on 21.08.2018.
 */
@Module
class GameScreen(
        game: GrowMyPlanetGame,
        private val config: AppConfig
) : BaseScreen<GrowMyPlanetGame, FitViewport>(
        game,
        FitViewport(config.camWidth, config.camHeight)
) {

    var component: GameScreenComponent? = null
        private set

    @Inject lateinit var controller: GameController

    private var isShown = false
    private val layers by lazy(LazyThreadSafetyMode.NONE) {
        mutableMapOf<Layer, Group>().also { map ->
            createAndPutLayersInto(map)
        }
    }

    private val gameObjectsLayers: Map<Layer, Group>
        get() = layers.filterKeys { it.isGameObjectsLayer() }


    private val layerHandler = object : LayerHandler {
        override fun addActorOnLayer(actor: Actor, layer: Layer) {
            layers[layer]?.addActor(actor)
                    ?: Gdx.app.error(TAG, "Can't find group for layer: $layer")
        }
    }

    init {
        component = game.component!!.plus(
                this,
                ControllersProvider()
        ).also {
            it.inject(this)
        }
        lifecycleListener = controller
        addInputListener(controller)
    }


    override fun prepareStage(stage: Stage) {
        if (isShown.not()) {
            addBackgroundImage()
            controller.start(this)
            isShown = true
        }
    }

    override fun resume() {
        super.resume()
        addLayerGroups()
    }

    override fun pause() {
        super.pause()
    }

    override fun render(delta: Float) {
        super.render(delta)
        controller.update(delta)
    }

    @ScreenScope
    @Provides
    fun createBlackHole(): BlackHole {
        val texture = Texture(Gdx.files.internal("gfx/blackhole.png"))
        return BlackHole(layerHandler, texture)
    }

    @ScreenScope
    @Provides
    fun createPlanetView(): Planet {
        val texture = Texture(Gdx.files.internal("gfx/planet.png"))
        return Planet(layerHandler, texture)
    }

    @ScreenScope
    @Provides
    fun createRingView(): Ring {
        val backTexture = Texture(Gdx.files.internal("gfx/ring1.png"))
        val frontTexture = Texture(Gdx.files.internal("gfx/ring2.png"))
        return Ring(layerHandler, frontTexture, backTexture)
    }

    @ScreenScope
    @Provides
    fun provideSwapLevelsUseCase(player: Planet): SwapLevelsUseCase {
        return SwapLevelsUseCase(
                player,
                { layers.filterKeys { it.isGameObjectsLayer() }.values },
                { swapGameObjectsLayersWithNewOnes() },
                { addLayerGroups() }
        )
    }

    private fun swapGameObjectsLayersWithNewOnes() {
        gameObjectsLayers.values.forEach {
            it.clearChildren()
        }

        gameObjectsLayers.keys.forEach {
            layers.remove(it)
        }

        createAndPutLayersInto(layers)
    }

    private fun addLayerGroups() {
        layers.values.forEach {
            stage?.addActor(it)
        }
    }

    private fun createAndPutLayersInto(map: MutableMap<Layer, Group>) {
        Layer.values().forEach {
            map[it] = Group().also {
                it.width = config.camWidth
                it.height = config.camHeight
            }
        }
    }

    private fun addBackgroundImage() {
        val texture = Texture(Gdx.files.internal("gfx/background.png"))
        val actor = Image(texture).also {
            it.width = config.camWidth
            it.height = config.camHeight
        }
        layerHandler.addActorOnLayer(actor, Layer.BACKGROUND)
    }

}

private fun <K, V> Map<K, V>.copyValues() = HashSet(values)

private const val TAG = "com.etb.growmyplanet.screens.game.GameScreen"
