package me.ddevil.krocessing

import ddf.minim.AudioPlayer
import ddf.minim.Minim
import processing.core.PApplet
import processing.core.PConstants
import java.io.File


fun main() {
    Krocessing.run()
}

class Krocessing(e: List<Entity>) : PApplet() {

    companion object {
        fun run() {
            val k = Krocessing(
                listOf(
                    AnimatedBackground(),
                    AudioWaveRenderer(),
                    AudioMetaEditor(),
                    AudioAnalyzer()
                )
            )
            k.setSize(800, 600)
            k.runSketch()
        }
    }

    private val entities: List<Entity>
    val minim = Minim(this)
    override fun setup() {

        loop()

        for (entity in entities) {
            entity.setup(this)
        }
    }

    override fun draw() {
        for (entity in entities) {
            entity.draw(this)
        }
        text("Shift + O to open file", 16F, 16F)
    }

    var player: AudioPlayer? = null
        private set


    fun selectedFile(file: File?) {
        file ?: return
        println("Using file '${file.absoluteFile}'")

        val p = minim.loadFile(file.absolutePath) ?: return
        player = p
        println("Setting player to $p")
        for (e in entities) {
            e.onChangedFile(this, file)
        }
        p.play()
    }

    override fun keyPressed() {
        if (key == 'O') {
            selectInput("Select file", Krocessing::selectedFile::name.get())
        }
        if (key == PConstants.ESC) {
            finished = true
        }
    }

    init {
        entities = ArrayList(e)
    }
}