package com.lucasjwilber.freenote

import androidx.lifecycle.LiveData

class NoteDescriptorsLiveData(noteDescriptor: NoteDescriptor): LiveData<NoteDescriptor>() {
    private val listener = {

    }

    override fun onActive() {
//        stockManager.requestPriceUpdates(listener)
    }

    override fun onInactive() {
//        stockManager.removeUpdates(listener)
    }
}