package com.lucasjwilber.freenote.models

import com.lucasjwilber.freenote.DeletedSegment
import com.lucasjwilber.freenote.LIST
import com.lucasjwilber.freenote.NOTE
import com.lucasjwilber.freenote.SEGMENT_DELIMITER
import java.util.*
import kotlin.collections.ArrayList

class CurrentNote(note: Note) {

    var id: Long? = note.id
    var type: Int = note.type
    var title: String = note.title
    var segments = if (note.segments.contains(SEGMENT_DELIMITER)) note.segments.split(SEGMENT_DELIMITER) else note.segments

    constructor(type: Int): this(Note(null, type, "", "", 4)) {
        var id: Long? = null
        var title: String = ""
        var segments = ArrayList<String>()
    }


    var isNew: Boolean = false
    var deletedSegments: Stack<DeletedSegment> = Stack()
    var newSegmentText: String = ""
    var body: String = ""
    var currentlyEditedSegmentPosition: Int? = null
    var hasBeenChanged: Boolean = false
    var titleWasSet: Boolean = false //this is used to focus the title ET on new Lists, but only one time, so new segment auto focus still works

}