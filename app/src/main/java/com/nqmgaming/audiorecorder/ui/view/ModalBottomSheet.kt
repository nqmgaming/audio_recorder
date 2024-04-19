package com.nqmgaming.audiorecorder.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nqmgaming.audiorecorder.databinding.BottomSheetBinding
import com.nqmgaming.audiorecorder.util.OnNameChangedListener
import java.io.File

class ModalBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    var listener: OnNameChangedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val fileName = arguments?.getString("fileName")
        val dirPath = arguments?.getString("dirPath")
        binding.etNameRecord.setText(fileName)

        binding.btnCancel.setOnClickListener {
            deleteFile(fileName, dirPath)
            dismiss()
        }

        binding.btnOk.setOnClickListener {
            val newName = binding.etNameRecord.text.toString().trim()
            if (newName.isNotEmpty()) {
                listener?.onNameChanged(newName)
                dismiss()
            } else {
                binding.etNameRecord.error = "Name cannot be empty"
            }
        }

        dialog?.setOnCancelListener {
            deleteFile(fileName, dirPath)
        }

    }

    private fun deleteFile(fileName: String?, dirPath: String?) {
        val file = File("$dirPath$fileName")
        val result = file.delete()
        if (result) {
            Log.d("File", "File deleted successfully")
        } else {
            Log.d("File", "File not deleted")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}