package com.imersa.warnu.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = firebaseAuth.currentUser?.uid
                    if (uid != null) {
                        checkUserRole(uid)
                    } else {
                        _loginState.value = LoginState.Error("User ID not found.")
                    }
                } else {
                    _loginState.value = LoginState.Error(task.exception?.message ?: "Login failed.")
                }
            }
    }

    private fun checkUserRole(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                if (role != null) {
                    _loginState.value = LoginState.Success(role)
                } else {
                    _loginState.value = LoginState.Error("Role not defined.")
                }
            }
            .addOnFailureListener {
                _loginState.value = LoginState.Error(it.message ?: "Failed to get user role.")
            }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class Error(val message: String) : LoginState()
}
