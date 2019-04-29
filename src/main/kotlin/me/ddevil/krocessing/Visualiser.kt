package me.ddevil.krocessing

import ddf.minim.AudioMetaData
import ddf.minim.AudioPlayer
import g4p_controls.*
import com.mpatric.mp3agic.Mp3File
import com.mpatric.mp3agic.ID3v1Tag
import com.mpatric.mp3agic.ID3v1
import ddf.minim.AudioListener
import java.io.File
import kotlin.math.roundToInt


import ddf.minim.*;
import ddf.minim.ugens.*;
class AudioAnalyzer : Entity() {
    lateinit var panel: GPanel
    lateinit var lLabel: GLabel
    lateinit var rLabel: GLabel
    lateinit var bufLabel: GLabel
    lateinit var indexLabel: GLabel
    override fun setup(krocessing: Krocessing) {
        lLabel = GLabel(krocessing, 0.0F, 0.0F, kLineWidth, kLineHeight, "Left sample")
        rLabel = GLabel(krocessing, 0.0F, 0.0F, kLineWidth, kLineHeight, "Right sample")
        bufLabel = GLabel(krocessing, 0.0F, 0.0F, kLineWidth, kLineHeight, "Buffer Size")
        indexLabel = GLabel(krocessing, 0.0F, 0.0F, kLineWidth, kLineHeight, "Sample Index")

        val widgets =
            listOf(lLabel, rLabel, bufLabel, indexLabel)
        panel = GPanel(krocessing, 500.0F, 300.0F, kLineWidth, (widgets.size + 1.5F) * kLineHeight, "Analyzer")
        build(panel, widgets)
    }

    private var currentIndex = 0
    override fun draw(krocessing: Krocessing) {
        val p = krocessing.player ?: return
        if (!p.isPlaying) {
            return
        }
        currentIndex++
        if (currentIndex >= p.bufferSize()) {
            currentIndex = 0
        }
        val lSample = p.left.get(currentIndex)
        val rSample = p.right.get(currentIndex)
        lLabel.text = "Left Sample: $lSample, ${Integer.toHexString(lSample.toRawBits())}"
        rLabel.text = "Right Sample: $rSample, ${Integer.toHexString(rSample.toRawBits())}"
        bufLabel.text = "Buffer Size: ${p.bufferSize()}"
        indexLabel.text = "Sample Index: $currentIndex"
        val lStr = lSample.toString()
        val rStr = rSample.toString()
        krocessing.text(lStr, krocessing.width - krocessing.textWidth(lStr) - 50, 50.0F)
        krocessing.text(rStr, krocessing.width - krocessing.textWidth(rStr) - 50, 150.0F)
    }
}

class AnimatedBackground : Entity() {

    private var currentIndex = 0
    override fun draw(krocessing: Krocessing) {
        val p = krocessing.player
        if (p == null || !p.isPlaying) {
            krocessing.background(0xFF0000, 255.0F)
            return
        }
        currentIndex++
        if (currentIndex >= p.bufferSize()) {
            currentIndex = 0
        }
        val lSample = p.left.get(currentIndex)
        val rSample = p.right.get(currentIndex)
        val fl = (1 + lSample) / 2
        val fr = (1 + rSample) / 2
        krocessing.background(fl * 255)

        krocessing.stroke(rSample.toRawBits())

    }
}

class AudioWaveRenderer : Entity() {
    override fun draw(krocessing: Krocessing) {
        val p = krocessing.player ?: return
        for (i in 0 until p.bufferSize() - 1) {
            krocessing.line(i.toFloat(), 50 + p.left.get(i) * 50, 50 + p.left.get(i) * 50, 50 + p.left.get(i + 1) * 50)
            krocessing.line(i.toFloat(), 150 + p.right.get(i) * 50, (i + 1).toFloat(), 150 + p.right.get(i + 1) * 50)
        }
    }
}

const val kSaveWidth = 50.0F
const val kLineHeight = 30.0F
const val kLineWidth = 300.0F

class AudioMetaEditor : Entity() {
    private var audioMeta: AudioMeta? = null
    private lateinit var metaPanel: GPanel
    private lateinit var trackField: GTextField
    private lateinit var nameField: GTextField
    private lateinit var albumField: GTextField
    //private lateinit var discField: GTextField
    private lateinit var dateField: GTextField
    private lateinit var genreField: GTextField
    private lateinit var titleField: GTextField
    override fun onChangedFile(krocessing: Krocessing, file: File) {
        audioMeta = null
    }

    override fun setup(krocessing: Krocessing) {
        trackField = textField("Track", krocessing)
        nameField = textField("Author", krocessing)
        albumField = textField("Album", krocessing)
        //discField = textField("Disc", krocessing)
        //composerField = textField("Composer", krocessing)
        dateField = textField("Date", krocessing)
        genreField = textField("Genre", krocessing)
        titleField = textField("Title", krocessing)
        val widgets =
            listOf(trackField, titleField, nameField, albumField, dateField, genreField)
        val editor = this
        metaPanel =
            GPanel(krocessing, 0.0F, 150.0F, kLineWidth, (widgets.size + 1.5F) * kLineHeight, "MetaData").apply {
                val p = this
                build(this, widgets)
                this.addControl(GButton(krocessing, 0.0F, 0.0F, kSaveWidth, kLineHeight, "Save").apply {
                    this.alignRightOf(p, kSaveWidth, kLineHeight)
                    this.addEventHandler(editor, AudioMetaEditor::save.name)
                })

            }
    }

    open fun save(button: GButton, event: GEvent) {
        println("Saving...")
        val s = p
        if (s == null) {
            println("Couldn find audio player")
            return
        }
        val m = audioMeta ?: createFrom(s.metaData)
        m.author = nameField.text
        m.album = albumField.text
        //discField.text = m.disc
        //composerField.text = m.composer
        m.date = dateField.text
        m.genre = genreField.text
        m.title = titleField.text
        m.trackName = trackField.text
        //metaPanel.draw()

        val mp3 = Mp3File(s.metaData.fileName())
        val id3v1Tag: ID3v1
        if (mp3.hasId3v1Tag()) {
            id3v1Tag = mp3.id3v1Tag
        } else {
            // mp3 does not have an ID3v1 tag, let's create one..
            id3v1Tag = ID3v1Tag()
            mp3.id3v1Tag = id3v1Tag
        }
        id3v1Tag.track = m.trackName
        id3v1Tag.artist = m.author
        id3v1Tag.title = m.title
        id3v1Tag.album = m.album

        val file = toUniqueFileName(s.metaData.fileName())
        mp3.save(file.absolutePath)
        print("Saved to ${file.absolutePath}")
    }

    private fun createFrom(metaData: AudioMetaData): AudioMeta {
        val m = AudioMeta.from(metaData)
        audioMeta = m
        return m
    }

    private fun toUniqueFileName(fileName: String): File {
        val original = File(fileName)
        var f = File(fileName)
        var i = 0
        while (f.exists()) {
            f = File("${original.nameWithoutExtension}_${++i}.${original.extension}")
        }
        return f
    }

    private fun textField(name: String, krocessing: Krocessing) =
        GTextField(krocessing, 0.0F, 0.0F, kLineWidth, kLineHeight).apply {
            this.promptText = name
        }


    private var p: AudioPlayer? = null

    override fun draw(krocessing: Krocessing) {
        val pc = krocessing.player
        metaPanel.isVisible = pc != null
        val s = p ?: (pc ?: return)
        if (p == null) {
            p = s
        }
        val m = audioMeta ?: createFrom(s.metaData)
        nameField.text = m.author
        trackField.text = m.trackName
        albumField.text = m.album
        dateField.text = m.date
        genreField.text = m.genre
    }
}

data class AudioMeta(
    var trackName: String,
    var title: String,
    var author: String,
    var album: String,
    var disc: String,
    var composer: String,
    var date: String,
    var genre: String
) {
    companion object {
        fun from(other: AudioMetaData): AudioMeta {
            print(other.track() + ", " + other.title())
            return AudioMeta(
                other.track(),
                other.title(),
                other.author(),
                other.album(),
                other.disc(),
                other.composer(),
                other.date(),
                other.genre()
            )
        }
    }
}

fun GAbstractControl.alignRightOf(panel: GPanel, width: Float, height: Float) {
    this.moveTo(panel.width - width, (panel.tabHeight / 2.0F) + panel.height - height)
}

private fun build(metaPanel: GPanel, fields: List<GAbstractControl>) {
    fields.forEachIndexed { index, gTextField ->
        metaPanel.addControl(gTextField, 0.0F, index * kLineHeight + metaPanel.tabHeight)
    }
}