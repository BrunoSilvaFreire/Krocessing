package me.ddevil.krocessing

import java.io.File

open class Entity {
    open fun setup(krocessing: Krocessing) {}
    open fun draw(krocessing: Krocessing) {}
    open fun onChangedFile(krocessing: Krocessing, file: File) {}
}

class CompositeEntity(
    val entities: List<Entity>
) : Entity() {
    override fun setup(krocessing: Krocessing) {
        for (entity in entities) {
            entity.setup(krocessing)
        }
    }

    override fun draw(krocessing: Krocessing) {
        for (entity in entities) {
            entity.draw(krocessing)
        }
    }
}