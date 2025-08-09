package com.imersa.warnu.ui.seller.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.imersa.warnu.databinding.FragmentAddProductBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSimpanProduk.setOnClickListener {
            viewModel.addProduct(
                name = binding.etNamaProduk.text.toString().trim().ifEmpty { null },
                price = binding.etHarga.text.toString().trim().ifEmpty { null },
                description = binding.etDeskripsi.text.toString().trim().ifEmpty { null },
                stock = binding.etStok.text.toString().trim().ifEmpty { null },
                category = binding.actvKategori.text.toString().trim().ifEmpty { null },
                imageUrl = binding.btnPilihGambar.text.toString().trim().ifEmpty { null }
            )
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is AddProductState.Idle -> Unit
                    is AddProductState.Success -> {
                        Toast.makeText(requireContext(), "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        clearForm()
                        viewModel.resetState()
                    }
                    is AddProductState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun clearForm() {
        binding.etNamaProduk.text?.clear()
        binding.etHarga.text?.clear()
        binding.etDeskripsi.text?.clear()
        binding.etStok.text?.clear()
        binding.actvKategori.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
