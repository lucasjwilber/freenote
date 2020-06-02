package com.lucasjwilber.freenote.models

// This class is used to make queries which populate the list of saved notes lightweight.
class NoteDescriptor(var id: Long, var title: String, var type: Int) { }