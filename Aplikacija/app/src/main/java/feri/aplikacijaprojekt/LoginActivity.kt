package feri.aplikacijaprojekt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etUserName: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUserName = findViewById(R.id.et_user_name)
        etPassword = findViewById(R.id.et_password)
        btnSubmit = findViewById(R.id.btn_submit)

        btnSubmit.setOnClickListener {
            val userName = etUserName.text.toString()
            Toast.makeText(this, "Pozdravljen $userName! Prosim zajemi sliko za nadaljevanje.", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, PhotoActivity::class.java)
            startActivity(intent)
        }

    }
}
