package com.yarrtest.balloon.screens.game.views

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.yarrtest.balloon.screens.game.Layer
import com.yarrtest.balloon.screens.game.LayerHandler

/**
 * Created by etb on 22.08.2018.
 */


class Ring(
        private val layerHandler: LayerHandler,
        frontTexture: Texture,
        backTexture: Texture
) : BaseView {

    override fun isShown() = frontImage.parent != null && backImage.parent != null

    private val frontImage = Image(frontTexture)
    private val backImage = Image(backTexture)

    override fun show() {
        layerHandler.addActorOnLayer(frontImage, Layer.FOREGROUND)
        layerHandler.addActorOnLayer(backImage, Layer.BACKGROUND)
    }

    override fun hide() {
        frontImage.remove()
        backImage.remove()
    }

    override fun setPosition(x: Float, y: Float) {
        frontImage.setPosition(x, y - frontImage.height)
        backImage.setPosition(x, y)
    }

    override fun resize(width: Float, height: Float) {
        frontImage.setSize(width, height)
        backImage.setSize(width, height)
    }

}