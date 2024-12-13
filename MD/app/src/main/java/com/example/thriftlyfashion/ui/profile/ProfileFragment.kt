package com.example.thriftlyfashion.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.remote.SharedPrefManager
import com.example.thriftlyfashion.ui.history.TransactionHistoryActivity
import com.example.thriftlyfashion.ui.payment.AddPaymentMethodActivity
import com.example.thriftlyfashion.ui.help.HelpCenterActivity
import com.example.thriftlyfashion.ui.guide.UserGuideActivity
import com.example.thriftlyfashion.ui.login.LoginActivity
import com.example.thriftlyfashion.ui.policy.PrivacyPolicyActivity
import com.example.thriftlyfashion.ui.shopowner.ShopOwnerActivity

class ProfileFragment : Fragment() {
    private lateinit var usernameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        usernameTextView = view.findViewById(R.id.username)
        updateUsername()

        val riwayatTransaksiCard: CardView = view.findViewById(R.id.id_riwayatTransaksi)
        val addMethodPayCard: CardView = view.findViewById(R.id.id_addMethodPay)
        val pusatBantuanCard: CardView = view.findViewById(R.id.id_pusatBantuan)
        val panduanPenggunaCard: CardView = view.findViewById(R.id.id_panduanPengguna)
        val kebijakanPrivasiCard: CardView = view.findViewById(R.id.id_kebijakanPrivasi)
        val updateProfileCard: LinearLayout = view.findViewById(R.id.id_updateProfile)
        val myStoreCard: CardView = view.findViewById(R.id.id_mystore)
        val logoutTextView: TextView = view.findViewById(R.id.logout)

        riwayatTransaksiCard.setOnClickListener {
            onRiwayatTransaksiClicked()
        }

        addMethodPayCard.setOnClickListener {
            onTambahMetodePembayaranClicked()
        }

        pusatBantuanCard.setOnClickListener {
            onPusatBantuanClicked()
        }

        panduanPenggunaCard.setOnClickListener {
            onPanduanPenggunaClicked()
        }

        kebijakanPrivasiCard.setOnClickListener {
            onKebijakanPrivasiClicked()
        }

        updateProfileCard.setOnClickListener {
            onUpdateProfileClicked()
        }

        myStoreCard.setOnClickListener {
            onMyStore()
        }

        logoutTextView.setOnClickListener {
            onLogoutClicked()
        }

        return view
    }

    private fun updateUsername() {
        val sharedPrefManager = SharedPrefManager(requireContext())
        val userName = sharedPrefManager.getUserName()

        usernameTextView.text = userName ?: "Pengguna"
    }

    private fun onLogoutClicked() {
        val sharedPrefManager = SharedPrefManager(requireContext())
        sharedPrefManager.clearAll()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        requireActivity().finish()
    }

    private fun onRiwayatTransaksiClicked() {
        val intent = Intent(requireContext(), TransactionHistoryActivity::class.java)
        startActivity(intent)
    }

    private fun onMyStore() {
        val intent = Intent(requireContext(), ShopOwnerActivity::class.java)
        startActivity(intent)
    }

    private fun onTambahMetodePembayaranClicked() {
        val intent = Intent(requireContext(), AddPaymentMethodActivity::class.java)
        startActivity(intent)
    }

    private fun onPusatBantuanClicked() {
        val intent = Intent(requireContext(), HelpCenterActivity::class.java)
        startActivity(intent)
    }

    private fun onPanduanPenggunaClicked() {
        val intent = Intent(requireContext(), UserGuideActivity::class.java)
        startActivity(intent)
    }

    private fun onKebijakanPrivasiClicked() {
        val intent = Intent(requireContext(), PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    private fun onUpdateProfileClicked() {
        val intent = Intent(requireContext(), ProfileEditActivity::class.java)
        startActivity(intent)
    }
}
